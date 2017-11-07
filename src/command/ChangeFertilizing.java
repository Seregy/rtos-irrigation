package command;

public class ChangeFertilizing extends BasicCommand {
    public static final String NAME = "ЗмінитиУдобрювання";
    private Integer fertilizerVolume;

    public ChangeFertilizing(int[] zones, Integer fertilizerVolume) {
        super(zones);
        this.fertilizerVolume = fertilizerVolume;
    }

    public Integer getFertilizerVolume() {
        return fertilizerVolume;
    }

    @Override
    public String getName() {
        return ChangeFertilizing.NAME;
    }
}
