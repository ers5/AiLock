package F4.AiLock.controller;

import F4.AiLock.dto.PostEvaluateRequestDto;
import F4.AiLock.dto.PostEvaluateResponseDto;
import F4.AiLock.dto.PreEvaluateRequestDto;
import F4.AiLock.dto.PreEvaluateResponseDto;
import F4.AiLock.service.PostEvaluate;
import F4.AiLock.service.PreEvaluate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class appController {

    private final PreEvaluate preEvaluate;
    private final PostEvaluate postEvaluate;

    @PostMapping("/testPre")
    public PreEvaluateResponseDto testPre(@RequestBody PreEvaluateRequestDto dto) {
        return preEvaluate.judgeStatus(dto);
    }

    @PostMapping("/testPost")
    public PostEvaluateResponseDto testPost(@RequestBody PostEvaluateRequestDto dto) {
        return postEvaluate.postEvaluate(dto);
    }
}
