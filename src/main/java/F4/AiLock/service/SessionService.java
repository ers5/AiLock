package F4.AiLock.service;

import F4.AiLock.dto.SessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final RedisTemplate<String, SessionContext> redisTemplate;

    public String createSession(SessionContext context, Duration ttl) {
        String sessionId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(sessionId, context, ttl);
        return sessionId;
    }

    public SessionContext getSession(String sessionId) {
        SessionContext context = redisTemplate.opsForValue().get(sessionId);
        if (context == null) {throw new IllegalArgumentException("세션이 없거나 만료되었습니다.");}
        return context;
    }

    public void updateTtl(String sessionId, Duration ttl) {
        redisTemplate.expire(sessionId, ttl);
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(sessionId);
    }
}