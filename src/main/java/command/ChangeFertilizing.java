package command;

public class ChangeFertilizing extends BasicCommand {
    public static final String NAME = "ЗмінитиУдобрювання";
    private int fertilizerVolume;

    public ChangeFertilizing(int[] zones, int fertilizerVolume) {
        super(zones);
        this.fertilizerVolume = fertilizerVolume;
    }

    public int getFertilizerVolume() {
        return fertilizerVolume;
    }

    @Override
    public String getName() {
        return ChangeFertilizing.NAME;
    }
}
