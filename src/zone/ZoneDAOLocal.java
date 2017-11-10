package zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ZoneDAOLocal implements ZoneDAO {
    private HashMap<Integer, Zone> zones = new HashMap<>();

    @Override
    public Zone find(int id) {
        return zones.get(id);
    }

    @Override
    public List<Zone> findAll() {
        return new ArrayList<>(zones.values());
    }

    @Override
    public boolean add(Zone zone) {
        if (zones.containsKey(zone.getId())) {
            return false;
        }

        zones.put(zone.getId(), zone);
        return true;
    }

    @Override
    public boolean update(Zone zone) {
        if (!zones.containsKey(zone.getId())) {
            return false;
        }

        zones.put(zone.getId(), zone);
        return true;
    }

    @Override
    public boolean delete(int id) {
        return (zones.remove(id) != null);
    }
}
