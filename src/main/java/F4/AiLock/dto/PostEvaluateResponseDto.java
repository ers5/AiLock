package F4.AiLock.dto;

import F4.AiLock.enums.Status;

public record PostEvaluateResponseDto(
        String supportMessage,
        Integer allowedTime,
        Status status
) {
}
