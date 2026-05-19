package F4.AiLock.dto;

import java.util.List;

public record SessionContext(
        String deviceId,
        String appName,
        String preInput,
        String usageLevel,
        String willPowerLevel,
        String status,
        String sessionType,
        Integer targetMinute,
        Integer requestMinute,
        Integer todayUse,
        float[] embedding,
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
