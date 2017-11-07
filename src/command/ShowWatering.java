package command;

public class ShowWatering extends BasicCommand {
    public static final String NAME = "ПоказатиПолив";

    public ShowWatering(int[] zones) {
        super(zones);
    }

    @Override
    public String getName() {
        return ShowWatering.NAME;
    }
}
