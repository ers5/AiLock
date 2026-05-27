package F4.AiLock.service;

import F4.AiLock.dto.*;
import F4.AiLock.entity.History;
import F4.AiLock.repository.HistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
@Service
@Slf4j
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final OllamaEmbeddingModel embeddingModel;
    private final UserContextConvertService userContextConvertService;
    private final SessionService sessionService;

    /**
     * 과거 기록 가져오는 가중치인데 현재 가중치가
     * 입력 유사도 3
     * 사용량 일치 2
     * 의지력 일치 2
     * 약속 어기면 4
     * overUse가 10분을 넘기면 4
     **/
    private double score(History history, String appName, String usageLevel, String willpowerLevel, float[] nowEmbed) {
        double score = 0;

        double similarity = calculateCosineSimilarity(history.getEmbedding(), nowEmbed);
        score += similarity * 3.0;

        if (history.getUsageLevel().equals(usageLevel)) score += 2.0;
        if (history.getWillPowerLevel().equals(willpowerLevel)) score += 2.0;
        if (history.getAppName().equals(appName)) score += 3.0;
        if (!history.isPromiseKept()) score += 4.0;
        if (history.getOveruseTime() >= 10) score += 4.0;

        return score;
    }

    public List<History> getHistory(PreEvaluateRequestDto dto) {
        String deviceId = dto.deviseId();
        String appName = dto.appName();
        String preInput = dto.preInput();
        String usageLevel = userContextConvertService.convertToUsageLevel(deviceId, dto.appName(), dto.todayUse());
        String willpowerLevel = userContextConvertService.convertToWillpowerLevel(dto.willPowerScore());
        float[] nowEmbed = embeddingModel.embed(preInput);
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        List<History> histories = historyRepository.findByDeviceIdAndCreatedAtAfter(deviceId, weekAgo);
        return histories.stream()
                .sorted((a, b) -> Double.compare(
                        score(b, appName, usageLevel, willpowerLevel, nowEmbed),
                        score(a, appName, usageLevel, willpowerLevel, nowEmbed)
                ))
                .limit(5)
                .toList();
    }

    public Long saveHistory(SessionContext context, PostEvaluateRequestDto requestDto, PostEvaluateResponseDto responseDto) {
        float[] embed = embeddingModel.embed(requestDto.postInput());
        History history = new History(
                context, requestDto.postInput(), responseDto, 0,0, false,embed);
        History saved = historyRepository.save(history);
        return saved.getId();
    }
    @Transactional
    public void updateHistory(String sessionId,Integer totalUseTime) {
        SessionContext session = sessionService.getSession(sessionId);
        History history=historyRepository.findById(session.dbId())
                .orElseThrow(()->new IllegalArgumentException("히스토리가 없습니다"));
        history.updatePromise(totalUseTime);
        log.info("히스토리 업데이트 약속시간: {}, 총 사용 시간: {}",history.getPlannedUseMinute(),history.getTotalUse());
    }

    private double calculateCosineSimilarity(float[] v1,float[] v2) {
        double dotProduct=0.0;
        double norm1=0.0;
        double norm2=0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct+=v1[i]*v2[i];
            norm1 += Math.pow(v1[i], 2);
            norm2 += Math.pow(v2[i], 2);
        }
        double similarity=dotProduct/(Math.sqrt(norm1)*Math.sqrt(norm2));
        return similarity;

    }
}
