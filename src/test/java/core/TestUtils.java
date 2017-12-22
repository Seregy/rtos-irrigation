package core;

import zone.Zone;
import zone.ZoneDAO;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {
    public static ZoneDAO mockZoneDAO() {
        ZoneDAO mockedDAO = mock(ZoneDAO.class);
        List<Zone> zones = new ArrayList<>();

        when(mockedDAO.find(anyInt()))
                .thenAnswer(invocation -> zones.get((int) invocation.getArgument(0) - 1));
        when(mockedDAO.update(any()))
                .thenReturn(true);
        when(mockedDAO.add(any()))
                .thenAnswer(invocation -> zones.add(invocation.getArgument(0)));
        when(mockedDAO.delete(anyInt()))
                .thenAnswer(invocation -> (zones.remove(((int) invocation.getArgument(0)) - 1) != null));
        when(mockedDAO.findAll())
                .thenReturn(zones);

        return mockedDAO;
    }
}
