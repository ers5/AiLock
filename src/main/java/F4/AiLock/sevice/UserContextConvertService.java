package F4.AiLock.sevice;

import F4.AiLock.entity.History;
import F4.AiLock.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserContextConvertService {

    private final HistoryRepository historyRepository;

    public String convertToWillpowerLevel(Integer score) {
        if (score<=50) return "LOW";
        else if (score <=80) return "Mid";
        else return "HIGH";
    }

    public String convertToUsageLevel(String deviceId,String appName,Integer todayUse) {
        LocalDateTime weekAgo=LocalDateTime.now().minusDays(7);

        List<History> histories = historyRepository.findByDeviceIdAndAppNameAndCreatedAtAfter(deviceId, appName,weekAgo);
        if (histories.isEmpty()) return "LOW";

        Double avgUse=histories.stream()
                .mapToInt(History::getTotalUseMinute)
                .average()
                .orElse(0.0);

        double ratio=todayUse/avgUse;

        if (avgUse==0) return "LOW";

        if (ratio<=0.5) return "LOW";
        else if(ratio <=1.2) return "MID";
        else return "HIGH";
    }

    public Integer extractMinutes(String input) {
        if(input==null || input.isBlank()) return null;

        Pattern pattern = Pattern.compile("(\\d+)\\s*(시간|분|초)");
        Matcher matcher = pattern.matcher(input);

        int totalMinute=0;
        boolean found=false;

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "시간" -> totalMinute += value * 60;
                case "분" -> totalMinute += value;
                case "초" -> totalMinute += (int)Math.ceil(value / 60.0);
            }
            found=true;
        }
        if (!found) return null;
        return totalMinute;
    }
}
