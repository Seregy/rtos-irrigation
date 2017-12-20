package command;

public abstract class BasicCommand implements Command {
    private int[] zones;

    public BasicCommand(int[] zones) {
        this.zones = zones;
    }

    public int[] getZones() {
        return zones;
    }

    @Override
    public abstract String getName();
}
