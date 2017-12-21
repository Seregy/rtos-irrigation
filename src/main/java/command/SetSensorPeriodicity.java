package command;

import java.time.LocalTime;

public class SetSensorPeriodicity extends BasicCommand {
    public static final String NAME = "ЗадатиПеріодичністьДатчиків";
    private int[] zones;
    private LocalTime checkInterval;

    public SetSensorPeriodicity(int[] zones, LocalTime checkInterval) {
        super(zones);
        this.checkInterval = checkInterval;
    }

    public LocalTime getCheckInterval() {
        return checkInterval;
    }

    @Override
    public String getName() {
        return SetSensorPeriodicity.NAME;
    }
}
