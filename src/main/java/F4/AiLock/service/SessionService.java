package F4.AiLock.service;

import F4.AiLock.dto.SessionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final RedisTemplate<String, SessionContext> redisTemplate;

    public String createSession(SessionContext context, Duration ttl) {
        String sessionId = UUID.randomUUID().toString();
        log.info("현재 세션: {}",sessionId);
        redisTemplate.opsForValue().set(sessionId, context, ttl);
        return sessionId;
    }

    public SessionContext getSession(String sessionId) {
        SessionContext context = redisTemplate.opsForValue().get(sessionId);
        if (context == null) {throw new IllegalArgumentException("세션이 없거나 만료되었습니다.");}
        return context;
    }

    public void update(String sessionId, Long id, Duration ttl) {
        SessionContext context = getSession(sessionId);
        SessionContext updated = context.withDbId(id);
        redisTemplate.opsForValue().set(sessionId, updated, ttl);
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(sessionId);
    }
}