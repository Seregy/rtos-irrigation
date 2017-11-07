package command;

public class StopFertilizing extends BasicCommand {
    public static final String NAME = "ЗупинитиУдобрювання";

    public StopFertilizing(int[] zones) {
        super(zones);
    }

    @Override
    public String getName() {
        return StopFertilizing.NAME;
    }
}
