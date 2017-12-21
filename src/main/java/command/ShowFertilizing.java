package command;

public class ShowFertilizing extends BasicCommand {
    public static final String NAME = "ПоказатиУдобрювання";

    public ShowFertilizing(int[] zones) {
        super(zones);
    }

    @Override
    public String getName() {
        return ShowFertilizing.NAME;
    }
}
