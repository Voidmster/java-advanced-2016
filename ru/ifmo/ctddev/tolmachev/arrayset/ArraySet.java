package ru.ifmo.ctddev.tolmachev.arrayset;

import java.util.*;

/**
 * Created by daniil on 27.02.16.
 */
public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    final private Comparator<? super T> comparator;
    final private List<T> data;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<T> collection, Comparator<? super T> comparator) {
        this.comparator = comparator;
        Set<T> s = new TreeSet<>(comparator);
        s.addAll(collection);
        data = new ArrayList<>();
        data.addAll(s);
    }

    public ArraySet(Collection<T> collection) {
        this(collection, null);
    }

    private ArraySet(List<T> list, Comparator<? super T> comparator) {
        this.data = list;
        this.comparator = comparator;
    }

    private int search(T t) {
        return Collections.binarySearch(data, t, comparator);
    }

    private int lowerIndex(T t) {
        int i = search(t);
        if (i < 0) {
            i = -(i + 1);
        }
        return i - 1;
    }

    @Override
    public T lower(T t) {
        int i = lowerIndex(t);
        return i == -1 ? null : data.get(i);
    }

    private int floorIndex(T t) {
        int i = search(t);
        if (i < 0) {
            i = -(i + 1) - 1;
        }
        return i;
    }

    @Override
    public T floor(T t) {
        int i = floorIndex(t);
        return i == -1 ? null : data.get(i);
    }

    private int ceilingIndex(T t) {
        int i = search(t);
        if (i < 0) {
            i = -(i + 1);
        }
        return i;
    }

    @Override
    public T ceiling(T t) {
        int i = ceilingIndex(t);
        return i == data.size() ? null : data.get(i);
    }

    private int higherIndex(T t) {
        int i = search(t);
        if (i < 0) {
            i = -(i + 1);
        } else {
            ++i;
        }
        return i;
    }

    @Override
    public T higher(T t) {
        int i = higherIndex(t);
        return i == data.size() ? null : data.get(i);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) throw new NullPointerException();
        return search((T) o) >= 0;
    }


    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < data.size();
            }

            @Override
            public T next() {
                return data.get(i++);
            }
        };
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new CustomList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int from = fromInclusive ? ceilingIndex(fromElement) : higherIndex(fromElement);
        int to = toInclusive ? floorIndex(toElement) : lowerIndex(toElement);
        if (from > to) {
            return new ArraySet<>(Collections.emptyList(), comparator);
        }
        return new ArraySet<>(data.subList(from, to + 1), comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        int to = inclusive ? floorIndex(toElement) : lowerIndex(toElement);
        return new ArraySet<>(data.subList(0, to + 1), comparator);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        int from = inclusive ? ceilingIndex(fromElement) : higherIndex(fromElement);
        return new ArraySet<>(data.subList(from, data.size()), comparator);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(0);
    }

    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(size() - 1);
    }
}