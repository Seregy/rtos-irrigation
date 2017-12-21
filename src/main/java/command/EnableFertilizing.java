package command;

public class EnableFertilizing extends BasicCommand {
    public static final String NAME = "ПідключитиУдобрювання";
    private int fertilizerVolume;

    public EnableFertilizing(int[] zones, int fertilizerVolume) {
        super(zones);
        this.fertilizerVolume = fertilizerVolume;
    }

    public int getFertilizerVolume() {
        return fertilizerVolume;
    }

    @Override
    public String getName() {
        return EnableFertilizing.NAME;
    }
}
