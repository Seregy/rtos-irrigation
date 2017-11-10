package zone;

import java.util.List;

public interface ZoneDAO {
    Zone find(int id);
    List<Zone> findAll();
    boolean add(Zone zone);
    boolean update(Zone zone);
    boolean delete(int id);
}
