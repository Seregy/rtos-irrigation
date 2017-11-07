package core;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class Utils {
    public static  <T> T peek(ListIterator<T> iterator) throws NoSuchElementException {
        T element = iterator.next();
        iterator.previous();
        return element;
    }
}
