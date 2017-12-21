package command;

public class ShowHumidity extends BasicCommand {
    public static final String NAME = "ПоказатиРівеньВологості";

    public ShowHumidity(int[] zones) {
        super(zones);
    }

    @Override
    public String getName() {
        return ShowHumidity.NAME;
    }
}
