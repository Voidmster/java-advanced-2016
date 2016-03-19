package ru.ifmo.ctddev.tolmachev.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 *
 * @author Tolmachev Daniil (Voidmaster)
 */
public class MethodWrapper {

    private final Method method;

    private final Class[] paramsType;

    private final String name;

    public MethodWrapper(Method method) {
        this.method = method;
        paramsType = method.getParameterTypes();
        name = method.getName();
    }

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

    @Override
    public int hashCode() {
        int paramsTypeHashCode = paramsType == null ? 0 : Arrays.hashCode(paramsType);
        int nameHashCode = name == null ? 0 : name.hashCode();
        return 37 * paramsTypeHashCode + nameHashCode;
    }

    public Method toMethod() {
        return method;
    }
}