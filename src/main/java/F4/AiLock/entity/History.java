package F4.AiLock.entity;

import F4.AiLock.dto.PostEvaluateResponseDto;
import F4.AiLock.dto.SessionContext;
import F4.AiLock.enums.SessionType;
import F4.AiLock.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;


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

    @Column(name = "use_minute",nullable = false)
    private Integer totalUseMinute;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "promise_kept", nullable = false)
    private boolean promiseKept = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "embedding", nullable = false, columnDefinition = "vector(1024")
    @Convert(converter = FloatArrayToVectorConverter.class)
    @ColumnTransformer(write = "?::vector")
    float[] embedding;

    public History(
            SessionContext context,
            String postInput,
            PostEvaluateResponseDto responseDto,
            Integer overuseTime,
            boolean promiseKept
    ) {
        this.deviceId = context.deviceId();
        this.appName = context.appName();
        this.preInput = context.preInput();
        this.postInput = postInput;
        this.usageLevel = context.usageLevel();
        this.willPowerLevel = context.willPowerLevel();
        this.status = responseDto.status();
        this.sessionType = SessionType.valueOf(context.sessionType());
        this.targetMinute = context.targetMinute();
        this.requestMinute = context.requestMinute();
        this.allowTime = responseDto.allowTime();
        this.totalUseMinute = context.todayUse();
        this.overuseTime = overuseTime;
        this.promiseKept = promiseKept;
        this.openedAt = LocalDateTime.now();
        this.embedding = context.embedding();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
