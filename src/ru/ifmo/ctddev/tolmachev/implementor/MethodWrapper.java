package ru.ifmo.ctddev.tolmachev.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * This class is immutable wrapper over the {@link Method} class.
 * <p>
 * Wrapped method can be got by {@link #toMethod()}.
 * Two instance of {@code MethodWrapper} would be equal,
 * if both wrappers have the same name of wrapped method,
 * and their arrays of types of parameters of wrapped methods are equal.
 *
 * @author Tolmachev Daniil (Voidmaster)
 * @see Method
 */
public class MethodWrapper {

    /**
     * The wrapped method.
     */
    private final Method method;

    /**
     * Arrays of types of parameters of wrapped method.
     */
    private final Class[] paramsType;

    /**
     * Name of wrapped method.
     */
    private final String name;

    /**
     * Constructs {@code MethodWrapper} which wraps given method.
     *
     * @param method the method to be wrapped by {@code MethodWrapper}.
     */
    public MethodWrapper(Method method) {
        this.method = method;
        paramsType = method.getParameterTypes();
        name = method.getName();
    }

    /**
     * Compares the specified object with {@code MethodWrapper} for equality.
     * Returns true if and only if the specified object is also a {@code MethodWrapper},
     * both wrappers have the same name of wrapped method,
     * and their arrays of types of parameters of wrapped methods are equal.
     *
     * @param o the object to be compared for equality with {@code MethodWrapper}.
     * @return true if specified object is equal to this {@code MethodWrapper}, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }

        Method temp = ((MethodWrapper) o).toMethod();

        return temp != null && name.equals(temp.getName())
                && Arrays.equals(paramsType, temp.getParameterTypes());
    }

    /**
     * Returns a hash code for this {@code MethodWrapper}.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int paramsTypeHashCode = paramsType == null ? 0 : Arrays.hashCode(paramsType);
        int nameHashCode = name == null ? 0 : name.hashCode();
        return 37 * paramsTypeHashCode + nameHashCode;
    }

    /**
     * Returns a method that has been wrapped by this {@code MethodWrapper}.
     *
     * @return a method that has been wrapped by this {@code MethodWrapper}.
     * @see Method
     */
    public Method toMethod() {
        return method;
    }
}