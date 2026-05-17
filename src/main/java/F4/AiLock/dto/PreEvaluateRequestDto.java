package F4.AiLock.dto;

public record PreEvaluateRequestDto(
        String appName,
        String preInput,
        Integer willPowerScore,
        Integer accumUseApp
) {
}
