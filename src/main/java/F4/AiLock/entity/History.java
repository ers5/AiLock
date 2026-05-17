package F4.AiLock.entity;

import F4.AiLock.enums.SessionType;
import F4.AiLock.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", columnDefinition = "TEXT", nullable = false)
    private String deviceId;

    @Column(name = "app_name", columnDefinition = "TEXT", nullable = false)
    private String appName;

    @Column(name = "pre_input", columnDefinition = "TEXT", nullable = false)
    private String preInput;

    @Column(name = "post_input", columnDefinition = "TEXT", nullable = false)
    private String postInput;

    @Column(name = "willpower_level", length = 5, nullable = false)
    private String willPowerLevel;

    @Column(name = "usage_level", length = 5, nullable = false)
    private String usageLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Column(name = "target_minute")
    private Integer targetMinute;

    @Column(name = "request_minute")
    private Integer requestMinute;

    @Column(name = "allow_time", nullable = false)
    private Integer allowTime;

    @Column(name = "overuse_time", nullable = false)
    private Integer overuseTime;

    @Column(name = "promise_kept", nullable = false)
    private boolean promiseKept = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
