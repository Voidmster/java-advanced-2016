package ru.ifmo.ctddev.tolmachev.mapper;

import java.util.function.Function;

/**
 * This class is representation of task which would be executed by {@code ParallelMapperImpl}.
 * <p>
 * Contains {@code Function} which would be applying to given {@code argument}
 * and {@code Counter} to understand if it should stop.
 *
 * @author Tolmachev Daniil (Voidmaster)
 * @see java.util.function.Function
 * @see ru.ifmo.ctddev.tolmachev.mapper.Counter
 */
public class Task<T, R> {
    private final Function<T, R> function;
    private final Counter counter;
    private final T argument;
    private R result;

    /**
     * Constructs {@code Task} which includes given {@code Function}, {@code argument} and {@code Counter}.
     *
     * @param function {@code Function} which would be included.
     * @param argument {@code argument} which would be included.
     * @param counter {@code Counter} which would be included.
     */
    public Task(Function<T, R> function, T argument, Counter counter) {
        this.function = function;
        this.argument = argument;
        this.counter = counter;
    }

    /**
     * This method is apply included {@code function} to an argument.
     */
    public void compute() {
        result = function.apply(argument);
    }

    /**
     * Returns included {@code Counter}.
     * @return included {@code Counter}.
     */
    public Counter getCounter() {
        return counter;
    }

    public R getResult() {
        return result;
    }
}
