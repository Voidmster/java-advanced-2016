package ru.ifmo.ctddev.tolmachev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * The class represents implementation of {@code JarImpler} interface.
 * <p>
 * It can generate implementation for interfaces/classes if they can be implemented/extended.
 * The implemented class will have name of class that is implemented plus 'Impl' suffix, {@code .java} extension
 * and will placed into given path.
 * <p>
 * It can generate {@code jar} archive with given class and place it into given path.
 * <p>
 * If Implementor can't generate implementation or {@code jar} archive,
 * that {@code ImplerException} will be thrown.
 *
 * @author Tolmachev Daniil (Voidmaster)
 * @see info.kgeorgiy.java.advanced.implementor.Impler
 * @see info.kgeorgiy.java.advanced.implementor.JarImpler
 * @see info.kgeorgiy.java.advanced.implementor.ImplerException
 */
public class Implementor implements JarImpler {
    /**
     * String contains text indent.
     */
    private final static String TAB = "    ";

    /**
     * Predicate, that returns true if given method is abstract.
     */
    private final static Predicate<Method> isAbstract = i -> Modifier.isAbstract(i.getModifiers());

    /**
     * Predicate, that returns true if given method is not final.
     */
    private final static Predicate<Method> isNotFinal = i -> !Modifier.isFinal(i.getModifiers());

    /**
     * Creates {@code jar} archive with implementation of class.
     * <p>
     * If {@code args[0]} equals to "-jar", than creates {@code jar} archive,
     * contains implementation of class which name given in {@code args[1]},
     * to file specified by string {@code args[2]}.
     *
     * @param args arguments for main corresponding to format.
     */
    public static void main(String[] args) {
        if (args == null || args.length != 3 || !args[0].equals("-jar") || args[1] == null || args[2] == null) {
            System.err.println("args format should be: -jar file-name name.jar");
        } else {
            try {
                Class token = Class.forName(args[1]);
                new Implementor().implementJar(token, Paths.get(args[2]));
            } catch (ClassNotFoundException e) {
                System.err.println(String.format("class %s not found", args[1]));
                e.printStackTrace();
            } catch (ImplerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructs new instance of {@code Implementor}.
     */
    public Implementor() {

    }

    /**
     * Produces {@code jar} archive implementing class/interface specified by provided {@code token}.
     *
     * @param token class which will be implemented and included into {@code jar} archive.
     * @param path  path where {@code jar} archive will be placed.
     * @throws ImplerException throws when class can't be implemented.
     */
    @Override
    public void implementJar(Class<?> token, Path path) throws ImplerException {
        Path tempDir = Paths.get("implementorTemp");
        implement(token, tempDir);

        String newClassName = token.getSimpleName() + "Impl";
        String newClassPath = token.getPackage().getName().replace('.', File.separatorChar)
                + File.separator + newClassName;

        if (ToolProvider.getSystemJavaCompiler().run(null, null, null,
                tempDir + File.separator + newClassPath + ".java") != 0) {
            throw new ImplerException("can't compile class");
        }

        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.MAIN_CLASS, newClassName);

        try (JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(path.toFile()), manifest)) {
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(tempDir
                    + File.separator + newClassPath + ".class"))) {
                outputStream.putNextEntry(new JarEntry(newClassPath + ".class"));
                byte buffer[] = new byte[2048];
                int size;

                while (true) {
                    size = inputStream.read(buffer, 0, 2048);
                    if (size <= 0) {
                        break;
                    }

                    outputStream.write(buffer, 0, size);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates implementation for interfaces/classes if they can be implemented/extended.
     * <p>
     * The implemented class will have name of class that is implemented plus 'Impl' suffix, {@code .java} extension
     * and will placed into given path.
     *
     * @param token given class.
     * @param root path where implemented class will be placed.
     * @throws ImplerException throws when class can't be implemented.
     * @see info.kgeorgiy.java.advanced.implementor.Impler
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token.getPackage() == null) {
            throw new ImplerException(token.getCanonicalName() + " has null package");
        }

        String newClassPath = root.toString() + File.separator
                + token.getPackage().getName().replace('.', File.separatorChar)
                + File.separator;

        if (!new File(newClassPath).exists()) {
            if (!new File(newClassPath).mkdirs()) {
                throw new ImplerException("can't create directory");
            }
        }

        File newClassFile = new File(newClassPath + token.getSimpleName() + "Impl.java");
        try (PrintWriter out = new PrintWriter(newClassFile)) {
            out.write(getClassImplementation(token));
        } catch (FileNotFoundException e) {
            throw new ImplerException("Can't create file", e);
        } catch (ImplerException e) {
            newClassFile.delete();
            throw e;
        }
    }

    /**
     * Returns string which contains implementation of token.
     * The implemented class will have name of class that is implemented plus 'Impl' suffix.
     *
     * @param token class to get implementation of.
     * @return string which contains implementation of given class.
     * @throws ImplerException throws when class can't be implemented.
     */
    private String getClassImplementation(Class<?> token) throws ImplerException {
        if (token.isPrimitive()) {
            throw new ImplerException(token.getCanonicalName() + " is primitive");
        }
        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException(token.getCanonicalName() + " is final");
        }
        if (token.getDeclaredConstructors().length > 0
                && Arrays.stream(token.getDeclaredConstructors())
                .filter(i -> Modifier.isPrivate(i.getModifiers()))
                .count() == token.getDeclaredConstructors().length) {
            throw new ImplerException(token.getCanonicalName() + " all constructors are private");
        }

        StringBuilder builder = new StringBuilder();

        builder.append("package ").append(token.getPackage().getName()).append(";\n\n");

        builder.append("public class ").append(token.getSimpleName()).append("Impl");
        if (token.isInterface()) {
            builder.append(" implements ").append(token.getSimpleName()).append(" {\n");
        } else {
            builder.append(" extends ").append(token.getSimpleName()).append(" {\n\n");
        }

        StringBuilder superCall = new StringBuilder();
        for (Constructor constructor : token.getConstructors()) {
            superCall.append("super(");
            builder.append(TAB).append(getAllModifiers(constructor.getModifiers())).append(token.getSimpleName()).append("Impl(");

            Class<?> parTypes[] = constructor.getParameterTypes();

            for (int i = 0; i < parTypes.length; i++) {
                builder.append(parTypes[i].getCanonicalName()).append(" p").append(Integer.toString(i));
                superCall.append("p").append(Integer.toString(i));

                if (i != constructor.getParameterCount() - 1) {
                    builder.append(", ");
                    superCall.append(", ");
                }
            }
            builder.append(")").append(getExceptions(constructor)).append(" {\n");
            superCall.append(");\n");

            if (constructor.getParameterCount() != 0) {
                builder.append(TAB).append(TAB).append(superCall);
            }
            builder.append(TAB).append("}\n\n");
            superCall.setLength(0);
        }

        for (Method method : getAllNeededMethods(token)) {
            builder.append(TAB).append(getAllModifiers(method.getModifiers()))
                    .append(method.getReturnType().getCanonicalName()).append(" ")
                    .append(method.getName()).append("(");

            Class<?> parTypes[] = method.getParameterTypes();

            for (int i = 0; i < parTypes.length; i++) {
                builder.append(parTypes[i].getCanonicalName()).append(" p").append(Integer.toString(i));

                if (i != method.getParameterCount() - 1) {
                    builder.append(", ");
                }
            }

            builder.append(")").append(getExceptions(method)).append(" {\n")
                    .append(TAB).append(TAB).append("return ")
                    .append(getDefaultValue(method.getReturnType()))
                    .append(";\n").append(TAB).append("}\n\n");
        }
        builder.append("}");

        return builder.toString();
    }

    /**
     * Returns array of methods of given class which should be overridden.
     * <p>
     * It collects all needed methods from given class, superclasses and implemented interfaces and
     * filter not overridden methods.
     * <p>
     * It use {@code MethodWrapper} to compare methods from different classes with same signature.
     *
     * @param c given class.
     * @return array of methods which should be overridden.
     * @see MethodWrapper
     */
    private Method[] getAllNeededMethods(Class<?> c) {
        Set<MethodWrapper> methods = Arrays.stream(c.getDeclaredMethods())
                .filter(isAbstract).filter(isNotFinal)
                .map(MethodWrapper::new).collect(Collectors.toSet());

        Set<MethodWrapper> overriddenMethods = Arrays.stream(c.getDeclaredMethods())
                .filter(isAbstract.negate())
                .map(MethodWrapper::new).collect(Collectors.toSet());

        Deque<Class<?>> deque = new ArrayDeque<>();
        Class<?> cur;
        deque.push(c);

        while (!deque.isEmpty()) {
            cur = deque.pop();

            if (cur.getSuperclass() != null) {
                deque.addFirst(cur.getSuperclass());
            }

            Arrays.stream(cur.getInterfaces()).forEach(deque::addLast);

            Arrays.stream(cur.getDeclaredMethods())
                    .filter(isAbstract).filter(isNotFinal)
                    .map(MethodWrapper::new)
                    .filter(i -> !overriddenMethods.contains(i))
                    .forEach(methods::add);

            Arrays.stream(cur.getDeclaredMethods())
                    .filter(isAbstract.negate()).filter(isNotFinal)
                    .map(MethodWrapper::new)
                    .forEach(overriddenMethods::add);
        }

        Method neededMethods[] = new Method[methods.size()];
        methods.stream().map(MethodWrapper::toMethod).collect(Collectors.toSet()).toArray(neededMethods);

        return neededMethods;
    }

    /**
     * Returns string representation of default value of given class.
     *
     * @param type the given class.
     * @return string containing the representation of default value specified by given class.
     */
    private String getDefaultValue(Class<?> type) {
        if (type.isArray()) {
            return "new " + type.getCanonicalName().replace("[]", "[0]");
        } else if (type.isPrimitive()) {
            if (type.equals(boolean.class)) {
                return "false";
            } else if (type.equals(void.class)) {
                return "";
            } else {
                return "0";
            }
        } else {
            return "null";
        }
    }

    /**
     * Convert given {@code int} to string representation of the Java language {@code Modifiers}.
     *
     * @param m int representation the Java language {@code Modifiers}.
     * @return string containing representation of the Java language {@code Modifiers} separated by a space.
     * @see java.lang.reflect.Modifier
     */
    private String getAllModifiers(int m) {
        StringBuilder data = new StringBuilder();

        if (Modifier.isPrivate(m)) {
            data.append("private ");
        }
        if (Modifier.isPublic(m)) {
            data.append("public ");
        }
        if (Modifier.isProtected(m)) {
            data.append("protected ");
        }
        if (Modifier.isFinal(m)) {
            data.append("final ");
        }
        if (Modifier.isStatic(m)) {
            data.append("static ");
        }
        if (Modifier.isSynchronized(m)) {
            data.append("synchronized ");
        }

        return data.toString();
    }

    /**
     * Returns string representation of exceptions for given {@code Executable}.
     *
     * @param executable the given {@code Executable}.
     * @return string representation of exceptions for {@code Executable}.
     * @see java.lang.reflect.Executable
     */
    private String getExceptions(Executable executable) {
        Class<?> exceptions[] = executable.getExceptionTypes();
        StringBuilder builder = new StringBuilder();

        if (exceptions.length != 0) {
            builder.append(" throws ");
            for (int i = 0; i < exceptions.length; i++) {
                builder.append(exceptions[i].getCanonicalName());
                if (i != exceptions.length - 1) {
                    builder.append(",  ");
                }
            }
        }

        return builder.toString();
    }
}
