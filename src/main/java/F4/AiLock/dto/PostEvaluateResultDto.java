package F4.AiLock.dto;

import F4.AiLock.enums.Status;

public record PostEvaluateResultDto(
        Status status,
        Integer allowTime,
        Integer totalUse
) {
}
