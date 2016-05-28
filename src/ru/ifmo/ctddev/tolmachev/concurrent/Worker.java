package ru.ifmo.ctddev.tolmachev.concurrent;

import java.util.List;
import java.util.function.Function;

/** This class is implementation of {@code Runnable} interface which use in {@code IterativeParallelism}
 * to support iterative parallelism.
 *
 * @author Tolmachev Daniil (Voidmaster)
 * @see ru.ifmo.ctddev.tolmachev.concurrent.IterativeParallelism
 * @see java.lang.Runnable
 */
public class Worker<T, R> implements Runnable {
    private Function<List<? extends T>, R> function;
    private List<? extends T> list;
    private R result;

    /**
     * Constructs {@code Worker} which include given list and {@code Function}.
     * @param list list which would be included.
     * @param function function which would be included.
     */
    public Worker(List<? extends T> list, Function<List<? extends T>, R> function) {
        this.function = function;
        this.list = list;
    }

    /**
     * Return result of {@code Worker}.
     * @return result of {@code Worker}.
     */
    public R getResult() {
        return result;
    }

    /**
     * This method uses to compute result.
     */
    @Override
    public void run() {
        result = function.apply(list);
    }
}
