package command;

public class ResumeWatering extends BasicCommand {
    public static final String NAME = "ВідновитиПолив";

    private int[] zones;

    public ResumeWatering(int[] zones) {
        super(zones);
    }

    @Override
    public String getName() {
        return ResumeWatering.NAME;
    }
}
