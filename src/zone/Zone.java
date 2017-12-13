package zone;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Zone {
    private int id;

    private WateringStatus wateringStatus;
    private LocalDateTime firstWatering;
    private LocalTime wateringInterval;
    private int waterVolume;
    private int wateringDuration;
    private Map.Entry<Integer, Integer> humidityRange;

    private boolean waterSensorNotResponding;

    private boolean fertilizerSensorNotResponding;

    private int humidityValue;

    private LocalTime sensorsCheckInterval;

    private FertilizingStatus fertilizingStatus;
    private int fertilizerVolume;

    public Zone(int id) {
        this.id = id;
        sensorsCheckInterval = LocalTime.of(0, 5);
        wateringStatus = WateringStatus.NOT_INITIALISED;
        fertilizingStatus = FertilizingStatus.NOT_INITIALISED;
    }

    public int getId() {
        return id;
    }

    public WateringStatus getWateringStatus() {
        return wateringStatus;
    }

    public void setWateringStatus(WateringStatus wateringStatus) {
        this.wateringStatus = wateringStatus;
    }

    public LocalDateTime getFirstWatering() {
        return firstWatering;
    }

    public void setFirstWatering(LocalDateTime firstWatering) {
        this.firstWatering = firstWatering;
    }

    public LocalTime getWateringInterval() {
        return wateringInterval;
    }

    public void setWateringInterval(LocalTime wateringInterval) {
        this.wateringInterval = wateringInterval;
    }

    public int getWaterVolume() {
        return waterVolume;
    }

    public void setWaterVolume(int waterVolume) {
        this.waterVolume = waterVolume;
    }

    public int getWateringDuration() {
        return wateringDuration;
    }

    public void setWateringDuration(int wateringDuration) {
        this.wateringDuration = wateringDuration;
    }

    public Map.Entry<Integer, Integer> getHumidityRange() {
        return humidityRange;
    }

    public void setHumidityRange(Map.Entry<Integer, Integer> humidityRange) {
        this.humidityRange = humidityRange;
    }

    public boolean isWaterSensorNotResponding() {
        return waterSensorNotResponding;
    }

    public void setWaterSensorNotResponding(boolean waterSensorNotResponding) {
        this.waterSensorNotResponding = waterSensorNotResponding;
    }

    public boolean isFertilizerSensorNotResponding() {
        return fertilizerSensorNotResponding;
    }

    public void setFertilizerSensorNotResponding(boolean fertilizerSensorNotResponding) {
        this.fertilizerSensorNotResponding = fertilizerSensorNotResponding;
    }

    public int getHumidityValue() {
        int min;
        int max;

        if (humidityRange != null) {
            min = humidityRange.getKey();
            max = humidityRange.getValue() + 1;
        } else {
            min = 0;
            max = 100;
        }

        humidityValue = ThreadLocalRandom.current().nextInt(min, max);
        return humidityValue;
    }

    public void setHumidityValue(int humidityValue) {
        this.humidityValue = humidityValue;
    }

    public LocalTime getSensorsCheckInterval() {
        return sensorsCheckInterval;
    }

    public void setSensorsCheckInterval(LocalTime sensorsCheckInterval) {
        this.sensorsCheckInterval = sensorsCheckInterval;
    }

    public FertilizingStatus getFertilizingStatus() {
        return fertilizingStatus;
    }

    public void setFertilizingStatus(FertilizingStatus fertilizingStatus) {
        this.fertilizingStatus = fertilizingStatus;
    }

    public int getFertilizerVolume() {
        return fertilizerVolume;
    }

    public void setFertilizerVolume(int fertilizerVolume) {
        this.fertilizerVolume = fertilizerVolume;
    }
}
