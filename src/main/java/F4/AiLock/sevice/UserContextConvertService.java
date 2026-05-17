package F4.AiLock.sevice;

import F4.AiLock.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserContextConvertService {

    private final HistoryRepository historyRepository;

    public String convertToWillpowerLevel(Integer score) {
        if (score<=50) return "LOW";
        else if (score <=80) return "Mid";
        else return "HIGH";
    }

    public String convertToUsageLevel(String deviceId) {
        
    }
}
