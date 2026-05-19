package F4.AiLock.repository;

import F4.AiLock.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.*;

public interface HistoryRepository extends JpaRepository<History,Long> {
    List<History> findByDeviceIdAndAppNameAndCreatedAtAfter(String deviceId, String appName, LocalDateTime createdAt);

    List<History> findByDeviceIdAndCreatedAtAfter(String deviceId, LocalDateTime createdAt);
}

