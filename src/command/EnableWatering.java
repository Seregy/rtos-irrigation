package command;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

public class EnableWatering extends BasicCommand {
    public static final String NAME = "ПідключитиПолив";
    private LocalDateTime firstWatering;
    private LocalTime wateringInterval;
    private int waterVolume;
    private int wateringDuration;
    private Map.Entry<Integer, Integer> humidityRange;

    public EnableWatering(int[] zones,
                          LocalDateTime firstWatering,
                          LocalTime wateringInterval,
                          int waterVolume,
                          int wateringDuration,
                          Map.Entry<Integer, Integer> humidityRange) {
        super(zones);
        this.firstWatering = firstWatering;
        this.wateringInterval = wateringInterval;
        this.waterVolume = waterVolume;
        this.wateringDuration = wateringDuration;
        this.humidityRange = humidityRange;
    }

    public LocalDateTime getFirstWatering() {
        return firstWatering;
    }

    public LocalTime getWateringInterval() {
        return wateringInterval;
    }

    public int getWaterVolume() {
        return waterVolume;
    }

    public int getWateringDuration() {
        return wateringDuration;
    }

    public Map.Entry<Integer, Integer> getHumidityRange() {
        return humidityRange;
    }

    @Override
    public String getName() {
        return EnableWatering.NAME;
    }
}
