package ru.ifmo.ctddev.tolmachev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class is implementation of {@code ListIP} interface.
 * <p>
 * It represents functions which use to :
 * find maximum and minimum in list,
 * check if all or any list element satisfy predicate,
 * concatenates all list elements in string representation,
 * filter list by using predicate,
 * convert list by applying the function to each list's element.
 * All given functions are doing computations in parallel by using {@code Worker} and {@code Thread}.
 * <p>
 * It also can uses {@code ParallelMapperImp} to compute result.
 * @author Tolmachev Daniil (Voidmaster)
 * @see ru.ifmo.ctddev.tolmachev.mapper.ParallelMapperImpl
 * @see info.kgeorgiy.java.advanced.concurrent.ListIP
 * @see info.kgeorgiy.java.advanced.concurrent.ScalarIP
 * @see ru.ifmo.ctddev.tolmachev.concurrent.Worker
 * @see java.lang.Thread
 */
public class IterativeParallelism implements ListIP {

    private ParallelMapper mapper;

    /**
     * Constructs new instance of {@code IterativeParallelism} without {@code ParallelMapperImp}.
     */
    public IterativeParallelism() {}

    /**
     * Construct new instance of {@code IterativeParallelism} with {@code ParallelMapperImp}.
     * @param mapper mapper with would be included.
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Concatenates all list elements in string representation and do it in parallel.
     *
     * @param i count of threads which use to compute.
     * @param list  list of elements which would be converted.
     * @return concatenated string.
     * @throws InterruptedException if execution was interrupted.
     */
    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        StringBuilder builder = new StringBuilder();
        map(i, list, Object::toString).forEach(builder::append);

        return builder.toString();
    }

    /**
     * Returns filtered list which elements are satisfy predicate and do it in parallel.
     *
     * @param i count of threads which use to compute.
     * @param list list of elements which would be filtered.
     * @param predicate predicate which use to filter list.
     * @param <T> list element's type.
     * @return  list of filtered elements.
     * @throws InterruptedException if execution was interrupted.
     */
    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<T> result = new ArrayList<>();
        doParallel(i, list, x -> x.stream().filter(predicate).collect(Collectors.toList())).forEach(result::addAll);
        return result;
    }

    /**
     * Returns maximum element of list and do it in parallel.
     *
     * @param i count of threads which use to compute.
     * @param list list in which will search maximum.
     * @param comparator comparator which use to compare list elements.
     * @param <T> list element's type.
     * @return  maximum element in list.
     * @throws InterruptedException if execution was interrupted.
     */
    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> action = x -> Collections.max(x, comparator);
        return action.apply(doParallel(i, list, action));
    }

    /**
     * Returns list which is converted by applying the function to each list's element and do it in parallel.
     *
     * @param i count of threads which use to compute.
     * @param list list to be converted.
     * @param function function which use to convert elements.
     * @param <T> list element's type.
     * @param <U> result type.
     * @return converted list.
     * @throws InterruptedException if execution was interrupted.
     */
    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        List<U> result = new ArrayList<>();
        doParallel(i, list, x -> x.stream().map(function).collect(Collectors.toList())).forEach(result::addAll);
        return result;
    }

    /**
     * Returns minimum element of list and do it in parallel.
     *
     * @param i count of threads which use to compute.
     * @param list list in which will search minimum.
     * @param comparator comparator which use to compare list elements.
     * @param <T> list element's type.
     * @return  minimum element in list.
     * @throws InterruptedException if execution was interrupted.
     */
    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(i, list, comparator.reversed());
    }

    /**
     * Checks if all list elements are satisfy given predicate.
     *
     * @param i count of threads which use to compute.
     * @param list list to be checked.
     * @param predicate predicate which use to check.
     * @param <T> list element's type.
     * @return true if all list elements are satisfy predicate; false otherwise.
     * @throws InterruptedException if execution was interrupted.
     */
    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return doParallel(i, list, x -> x.stream().allMatch(predicate)).stream().allMatch(Predicate.isEqual(true));
    }

    /**
     * Checks if any element of list satisfy given predicate.
     *
     * @param i count of threads which use to compute.
     * @param list list to be checked.
     * @param predicate predicate which use to check.
     * @param <T> list element's type.
     * @return true if any element of list satisfy predicate; false otherwise.
     * @throws InterruptedException if execution was interrupted.
     */
    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return doParallel(i, list, x -> x.stream().anyMatch(predicate)).stream().anyMatch(Predicate.isEqual(true));
    }

    private <T> List<List<? extends T>> getPartsList(List<? extends T> list, int threadsCount) {
        int sizeOfSubList = list.size() / threadsCount;
        List<List<? extends T>> result = new ArrayList<>();
        for (int i = 0; i < threadsCount; i++) {
            int from = i * sizeOfSubList;
            int to = Math.min(list.size(), (i + 1) * sizeOfSubList);
            if (i == threadsCount - 1) {
                to += list.size() % threadsCount;
            }

            result.add(list.subList(from, to));
        }

        return result;
    }

    private <T, R> List<R> doParallel(int threadsCount, List<? extends T> list,
                                      Function<List<? extends T>, R> action) throws InterruptedException {
        List<Worker<T, R>> workers = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        threadsCount = Math.min(list.size(), threadsCount);
        List<List<? extends T>> parts = getPartsList(list, threadsCount);

        if (mapper == null) {
            workers.addAll(parts.stream().map(part -> new Worker<>(part, action)).collect(Collectors.toList()));
            for (int i = 0; i < threadsCount; i++) {
                threads.add(new Thread(workers.get(i)));
                threads.get(i).start();
            }

            for (int i = 0; i < threadsCount; i++) {
                threads.get(i).join();
            }

            return workers.stream().map(Worker::getResult).collect(Collectors.toList());
        } else {
            return mapper.map(action, parts);
        }
    }
}
