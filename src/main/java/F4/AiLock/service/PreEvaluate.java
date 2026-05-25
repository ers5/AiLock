package F4.AiLock.service;

import F4.AiLock.dto.PreEvaluateRequestDto;
import F4.AiLock.dto.PreEvaluateResponseDto;
import F4.AiLock.dto.PreEvaluateResultDto;
import F4.AiLock.dto.SessionContext;
import F4.AiLock.entity.History;
import F4.AiLock.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreEvaluate {
    private final HistoryService historyService;
    private final ChatModel chatModel;
    private final UserContextConvertService userContextConvertService;
    private final ObjectMapper objectMapper;
    private final SessionService sessionService;

    private final int MAX_RETRY=3;


    public PreEvaluateResponseDto judgeStatus(PreEvaluateRequestDto dto) {
        List<History> histories = historyService.getHistory(dto);

        String usageLevel = userContextConvertService.convertToUsageLevel(dto.deviseId(), dto.appName(), dto.todayUse());
        String willpowerLevel = userContextConvertService.convertToWillpowerLevel(dto.willPowerScore());
        Status status = decideStatus(usageLevel, willpowerLevel);
        String ragInfo = toRagInfo(histories);

        String prompt = String.format("""
            너는 사용자의 스마트폰 과사용 위험도를 판단하는 AI야.
            현재 사용량, 의지력, 과거 유사 기록을 참고해서 상태를 판단해.

            [현재 데이터]
            - 앱 이름: %s
            - 초기 상태: %s
            - 사용량 수준: %s
            - 의지력 수준: %s

            [과거 유사 기록]
            %s

            [상태 단계]
            CRITICAL ← OVERUSE ← WARNING ← OPTIMAL

            [판단 규칙]
            1. initial_status는 기본값이야.
            2. 과거 유사 기록에서 promiseKept=false가 많으면 왼쪽(CRITICAL 방향)으로 이동해.
            3. 과거 유사 기록에서 overuseTime이 10분 이상이면 왼쪽(CRITICAL 방향)으로 이동해.
            4. 같은 앱에서 반복 실패 기록이 있으면 왼쪽(CRITICAL 방향)으로 이동해.
            5. 사용량이 HIGH면 왼쪽(CRITICAL 방향)으로 이동해.
            6. 의지력이 LOW면 왼쪽(CRITICAL 방향)으로 이동해.
            7. 사용량이 LOW이고 의지력이 HIGH면 오른쪽(OPTIMAL 방향)으로 이동해.
            8. 단, CRITICAL보다 더 왼쪽으로 갈 수 없고, OPTIMAL보다 더 오른쪽으로 갈 수 없어.

            [과거 기록 요약 규칙]
            - summary에는 과거 유사 기록의 패턴을 1~2문장으로 요약해.
            - 반복 실패, 초과 사용, 같은 앱 반복 여부를 중심으로 요약해.
            - 과거 기록이 없으면 "과거 기록이 없습니다." 라고 작성해.

            [상태 정의]
            - OPTIMAL: 건강한 사용, 과사용 위험 낮음
            - WARNING: 약한 과사용 징후, 주의 필요
            - OVERUSE: 과사용 가능성 높음, 제한 필요
            - CRITICAL: 과사용 상태 확정, 강한 제한 필요

            [출력 형식]
            반드시 JSON만 반환해.
            {
              "status": "OPTIMAL | WARNING | OVERUSE | CRITICAL",
              "reason": "판단 이유를 1~2문장으로 작성",
              "summary": "과거 유사 기록 패턴 요약"
            }
            """,
                dto.appName(),
                status.name(),
                usageLevel,
                willpowerLevel,
                ragInfo
        );
        for (int i = 0; i < MAX_RETRY; i++) {
            String preResult = chatModel.call(prompt);
            log.info("현재 판단: {}",preResult);

            try {
                String json = extractJson(preResult);
                PreEvaluateResultDto result = objectMapper.readValue(json, PreEvaluateResultDto.class);
                SessionContext sessionContext = new SessionContext(
                        dto.deviseId(),
                        dto.appName(),
                        dto.preInput(),
                        result.status(),
                        result.summary(),
                        usageLevel,
                        willpowerLevel,
                        dto.sessionType(),
                        dto.requestUseTime()
                );
                Duration ttl=Duration.ofMinutes(dto.requestUseTime()+5);
                String sessionId = sessionService.createSession(sessionContext, ttl);
                return new PreEvaluateResponseDto(sessionId);
            } catch (Exception e) {
                log.warn("preEvalute 파싱 실패 {}회",i+1,e);
            }
        }
        String failLLMHistorySummary = makeFallbackSummary(histories);
        SessionContext failContext = new SessionContext(
                dto.deviseId(),
                dto.appName(),
                dto.preInput(),
                status,
                failLLMHistorySummary,
                usageLevel,
                willpowerLevel,
                dto.sessionType(),
                dto.requestUseTime()
        );
        Duration ttl=Duration.ofMinutes(dto.requestUseTime()+5);
        String sessionId = sessionService.createSession(failContext, ttl);

        log.info("");
        return new PreEvaluateResponseDto(sessionId);
    }


    private Status decideStatus(String usage, String willPower) {
        log.info("판단 전: "+"usage: {}, willPower: {}", usage, willPower);
        if ("HIGH".equals(usage) && "LOW".equals(willPower)) return Status.CRITICAL;
        else if ("LOW".equals(usage) && "HIGH".equals(willPower)) return Status.OPTIMAL;
        else if ("HIGH".equals(usage) || "LOW".equals(willPower)) return Status.OVERUSE;
        return Status.WARNING;
    }

    private String extractJson(String result) {
        int start = result.indexOf("{");
        int end = result.lastIndexOf("}");

        if (start == -1 || end == -1 || start > end) {
            throw new IllegalArgumentException("JSON 객체가 없습니다: " + result);
        }

        return result.substring(start, end + 1);
    }

    private String toRagInfo(List<History> histories) {
        if (histories == null || histories.isEmpty()) {
            return "과거 유사 기록 없음";
        }
        StringBuilder sb = new StringBuilder();
        for (History h : histories) {
            sb.append("- 앱: ").append(h.getAppName()).append("\n")
                    .append("  왜 이 앱을 덜 쓰고 싶어하는 지: ").append(h.getPreInput()).append("\n")
                    .append("  왜 앱을 더 쓰고 싶은지: ").append(h.getPostInput()).append("\n")
                    .append("  약속 지킴 여부: ").append(h.isPromiseKept()).append("\n")
                    .append("  초과 사용 시간: ").append(h.getOveruseTime()).append("분\n\n");
        }
        return sb.toString();
    }

    private String makeFallbackSummary(List<History> histories) {
        if (histories == null || histories.isEmpty()) {
            return "과거 유사 기록 없음";
        }

        long failCount = histories.stream()
                .filter(h -> !h.isPromiseKept())
                .count();

        return String.format(
                "과거 유사 기록 %d개 중 약속 미준수 %d회",
                histories.size(),
                failCount
        );
    }
}
