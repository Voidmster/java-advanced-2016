package ru.ifmo.ctddev.tolmachev.mapper;

/**
 * This class is wrapper over the int which use to control computation inside {@code ParallelMapper}.
 *
 * @author Tolmachev Daniil (Voidmaster)
 */
public class Counter {
    private int count;

    /**
     * Constructs {@code Counter} which wraps given int.
     * @param count the int to be wrapped by {@code Counter}.
     */
    public Counter(int count) {
        this.count = count;
    }

    /**
     * This method checks if value of wrapped int is zero.
     * @return  true if value of wrapped int is zero, false otherwise.
     */
    public boolean isZero() {
        return count == 0;
    }

    /**
     * This method decreases value of wrapped int by one.
     */
    public void decrease() {
        count--;
    }

    /**
     * This method sets value of wrapped int by zero.
     */
    public void setToZero() {
        count = 0;
    }
}
