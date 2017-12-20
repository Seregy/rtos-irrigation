package command;

public class StopWatering extends BasicCommand {
    public static final String NAME = "ЗупинитиПолив";

    public StopWatering(int[] zones) {
        super(zones);
    }

    @Override
    public String getName() {
        return StopWatering.NAME;
    }
}
