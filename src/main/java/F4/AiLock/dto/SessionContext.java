package F4.AiLock.dto;

import java.util.List;

public record SessionContext(
        String deviceId,
        String appName,
        String preInput,
        String status,
        String sessionType,
        Integer targetMinute,
        Integer requestMinute,
        List<RagHistory> ragHistoryList
) {
    public record RagHistory(
            String appName,
            String preInput,
            String occurredAt,
            Boolean promiseKept,
            Integer overUseTime
    ) {}
}
