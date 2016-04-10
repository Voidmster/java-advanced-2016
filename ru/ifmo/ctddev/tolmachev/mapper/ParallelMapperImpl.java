package ru.ifmo.ctddev.tolmachev.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class provides iterative parallelism with with fixed count of threads.
 * <p>
 * It provides computation some {@code Function} on {@code List}
 * in more then one {@code Thread} by using {@code Counter} and {@code Task}.
 *
 * @author Tolmachev Daniil (Voidmaster)
 * @see ru.ifmo.ctddev.tolmachev.mapper.Counter
 * @see java.util.function.Function
 * @see java.lang.Thread
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threads = new ArrayList<>();
    private final Deque<Task<?, ?>> tasks = new LinkedList<>();
    private volatile boolean isClosed = false;

    /**
     * Constructs {@code ParallelMapperImpl} with fixed count of {@code Threads}.
     * @param countOfThreads count of threads.
     */
    public ParallelMapperImpl(int countOfThreads) {
        for (int i = 0; i < countOfThreads; i++) {
            threads.add(new Thread(new Worker()));
            threads.get(i).start();
        }
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            while (true) {
                Task<?, ?> currentTask;
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    currentTask = tasks.removeFirst();
                }

                if (!isClosed) {
                    currentTask.compute();
                }

                synchronized (currentTask.getCounter()) {
                    if (isClosed) {
                        currentTask.getCounter().setToZero();
                        currentTask.getCounter().notify();
                        break;
                    } else {
                        currentTask.getCounter().decrease();
                        if (currentTask.getCounter().isZero()) {
                            currentTask.getCounter().notify();
                        }
                    }
                }
            }
        }
    }

    /**
     * Maps {@code List} by {@code Function} and do it parallel.
     *
     * @param f {@code Function} which would be apply.
     * @param args {@code List} which would be mapped.
     * @param <T> type of list elements.
     * @param <R> type of result list.
     * @return  result of mapping.
     * @throws InterruptedException if something went wrong.
     * @see java.util.function.Function
     * @see java.util.List
     */
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        if (isClosed) {
            throw new IllegalStateException("Mapper already closed.");
        }


        final List<Task<?, ? extends R>> container = new ArrayList<>();
        final Counter counter = new Counter(args.size());

        synchronized (tasks) {
            for (T arg : args) {
                Task<?, ? extends R> task = new Task<>(f, arg, counter);
                tasks.addLast(task);
                container.add(task);
            }

            tasks.notifyAll();
        }

        synchronized (counter) {
            while (!counter.isZero()) {
                counter.wait();
            }

            return container.stream().map(Task::getResult).collect(Collectors.toList());
        }
    }

    /**
     * Stops {@code ParallelMapperImpl}.
     * @throws InterruptedException if can't stop ParallelMapperImp.
     */
    @Override
    public void close() throws InterruptedException {
        if (!isClosed) {
            isClosed = true;
        }

        for (Thread thread : threads) {
            thread.interrupt();
            thread.join();
        }
    }
}
