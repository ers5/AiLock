package F4.AiLock.dto;

public record PromiseFeedbackDto(
        String sessionId,
        Boolean promiseKept,
        Integer overUse
) {
}
