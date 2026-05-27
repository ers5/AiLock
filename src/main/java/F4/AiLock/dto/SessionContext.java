package F4.AiLock.dto;

import F4.AiLock.enums.Status;

public record SessionContext(
        String deviceId,
        String appName,
        String preInput,
        Status status,
        String historySummary,
        String usageLevel,
        String willPowerLevel,
        String sessionType,
        Integer plannedUseMinute,
        Long dbId
) {
    public SessionContext withDbId(Long dbId) {
        return new SessionContext(
                deviceId,
                appName,
                preInput,
                status,
                historySummary,
                usageLevel,
                willPowerLevel,
                sessionType,
                plannedUseMinute,
                dbId
        );
    }
}
