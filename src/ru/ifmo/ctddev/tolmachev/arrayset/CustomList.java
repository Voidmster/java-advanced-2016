package ru.ifmo.ctddev.tolmachev.arrayset;

import java.util.AbstractList;
import java.util.List;

/**
 * Created by daniil on 07.03.16.
 */
public class CustomList<T> extends AbstractList<T> {
    private final List<T> data;
    private final boolean order;

    public CustomList(List<T> data) {
        if (data instanceof CustomList) {
            CustomList<T> temp = (CustomList<T>) data;
            this.data = temp.data;
            this.order = !temp.order;
        } else {
            this.data = data;
            this.order = false;
        }
    }

    @Override
    public T get(int index) {
        return order ? data.get(index) : data.get(data.size() - index - 1);
    }

    @Override
    public int size() {
        return data.size();
    }
}
