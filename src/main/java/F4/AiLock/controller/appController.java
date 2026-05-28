package F4.AiLock.controller;

import F4.AiLock.dto.*;
import F4.AiLock.service.HistoryService;
import F4.AiLock.service.PostEvaluate;
import F4.AiLock.service.PreEvaluate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class appController {

    private final PreEvaluate preEvaluate;
    private final PostEvaluate postEvaluate;
    private final HistoryService historyService;
    @PostMapping("/testPre")
    public PreEvaluateResponseDto testPre(@RequestBody PreEvaluateRequestDto dto) {
        return preEvaluate.judgeStatus(dto);
    }

    @PostMapping("/testPost")
    public PostEvaluateResponseDto testPost(@RequestBody PostEvaluateRequestDto dto) {
        return postEvaluate.postEvaluate(dto);
    }

    @PostMapping("/testUpdate")
    public void testUpdate(@RequestBody PromiseFeedbackDto dto) {
        historyService.finishOrUpdate(dto.sessionId(),dto.totalUseTime());
    }

    @PostMapping("/testFirst")
    public FirstResponseDTO testFirst() {
        return new FirstResponseDTO(UUID.randomUUID().toString());
    }
}
