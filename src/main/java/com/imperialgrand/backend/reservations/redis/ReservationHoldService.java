package com.imperialgrand.backend.reservations.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationHoldService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;


    public String saveHold(ReservationHold hold, Duration ttl){
        try{
            String key = keyBuilder(hold.tableId(), String.valueOf(hold.Start()));
            String value = objectMapper.writeValueAsString(hold);
            redisTemplate.opsForValue().set(key, value, ttl);
            return key;
        }catch (Exception ex){
            throw new RuntimeException("Failed to store reservation hold", ex);
        }
    }

    public boolean isheld(long tableId, LocalDateTime start){
        String key = keyBuilder(tableId, String.valueOf(start));
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean isheld(long tableId, String start){
        String key = keyBuilder(tableId, start);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public ReservationHold getHold(String holdKey){
        try{
            String json = redisTemplate.opsForValue().get(holdKey);
            if(json == null) return null;
            return objectMapper.readValue(json, ReservationHold.class);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read reservation hold", ex);
        }

    }

    public void deleteHold(long tableId, String start){
        String key = keyBuilder(tableId, start);
        redisTemplate.delete(key);
    }

    private String keyBuilder(Long tableId, String start){
        return String.format("hold:%s:%s", String.valueOf(tableId), start);
    }

}
