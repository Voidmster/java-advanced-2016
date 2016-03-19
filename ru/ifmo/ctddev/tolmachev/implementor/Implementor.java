package ru.ifmo.ctddev.tolmachev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
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
 *
 * @author Tolmachev Daniil (Voidmaster)
 */
public class Implementor implements JarImpler {
    private final static String TAB = "    ";

    private final static Predicate<Method> isAbstract = i -> Modifier.isAbstract(i.getModifiers());
    private final static Predicate<Method> isNotFinal = i -> !Modifier.isFinal(i.getModifiers());

    public static void main(String[] args) throws ImplerException {
        if (args == null || args.length != 3 || !args[0].equals("-jar") || args[1] == null || args[2] == null) {
            throw new ImplerException("args format should be: -jar file-name name.jar");
        } else {
            try {
                Class token = Class.forName(args[1]);
                new Implementor().implementJar(token, Paths.get(args[2]));
            } catch (ClassNotFoundException e) {
                throw new ImplerException(String.format("class %s not found", args[1]));
            }
        }
    }

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

        try (JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(path.toFile()), manifest)){
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

    private String getExceptions(Executable c) {
        Class<?> exceptions[] = c.getExceptionTypes();
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