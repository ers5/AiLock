package F4.AiLock.dto;

public record PreEvaluateRequestDto(
        String deviseId,
        String appName,
        String preInput,
        Integer willPowerScore,
        Integer todayUse
) {
}
