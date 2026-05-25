package F4.AiLock.dto;

import F4.AiLock.enums.Status;

public record PreEvaluateResultDto(
        Status status,
        String reason,
        String summary
) {
}
