package command;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

public class ChangeWatering  extends BasicCommand {
    public static final String NAME = "ЗмінитиПолив";
    private LocalDateTime firstWatering;
    private LocalTime wateringInterval;
    private Integer waterVolume;
    private Integer wateringDuration;
    private Map.Entry<Integer, Integer> humidityRange;

    public ChangeWatering(int[] zones,
                          LocalDateTime firstWatering,
                          LocalTime wateringInterval,
                          Integer waterVolume,
                          Integer wateringDuration,
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

    public Integer getWaterVolume() {
        return waterVolume;
    }

    public Integer getWateringDuration() {
        return wateringDuration;
    }

    public Map.Entry<Integer, Integer> getHumidityRange() {
        return humidityRange;
    }

    @Override
    public String getName() {
        return ChangeWatering.NAME;
    }
}
