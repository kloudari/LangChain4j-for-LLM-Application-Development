package com.coursera.langchain.chapter6;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

/**
 * Executes a short, self-contained Java statement block provided by the LLM
 * and returns whatever it prints to standard out.
 *
 * <p>This mirrors the Python course's {@code python_repl} tool (which calls
 * {@code exec()} on LLM-generated Python) but runs real Java instead, since
 * the JVM has no built-in Python interpreter: it compiles the snippet
 * in-process with the JDK's own compiler and loads it with a throwaway
 * class loader.
 *
 * <p><strong>Security note:</strong> exactly like the original
 * {@code exec()}-based tool it replaces, this executes arbitrary code with
 * the same privileges as the running process. It is intentionally
 * unsandboxed and meant only for this local, trusted educational demo —
 * never expose it to untrusted input or run it outside a throwaway
 * environment.
 */
public class JavaCodeExecutorTool {

    @Tool("""
            Execute a short Java statement block and return whatever it prints \
            with System.out.println(...). The snippet may use java.util.* \
            (List, Arrays, Comparator, etc.), which is already imported. Do \
            not declare a class or a main method -- just write the statements \
            directly, e.g. `System.out.println("hi");`.""")
    public String executeJavaCode(@P("the Java statements to run") String code) {
        String className = "Snippet" + UUID.randomUUID().toString().replace("-", "");
        String source = "import java.util.*;\n"
                + "public class " + className + " {\n"
                + "    public static void run() throws Exception {\n"
                + code + "\n"
                + "    }\n"
                + "}\n";

        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("java-code-tool-");
        } catch (Exception e) {
            return "Error: could not create temp directory: " + e.getMessage();
        }

        try {
            Path sourceFile = tempDir.resolve(className + ".java");
            Files.writeString(sourceFile, source);

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                return "Error: no Java compiler available (run with a JDK, not a JRE).";
            }

            ByteArrayOutputStream compilerErrors = new ByteArrayOutputStream();
            int result = compiler.run(null, null, new PrintStream(compilerErrors),
                    "-d", tempDir.toString(), sourceFile.toString());
            if (result != 0) {
                return "Compilation error:\n" + compilerErrors;
            }

            try (URLClassLoader classLoader =
                    new URLClassLoader(new URL[] {tempDir.toUri().toURL()}, getClass().getClassLoader())) {
                Class<?> clazz = Class.forName(className, true, classLoader);
                Method run = clazz.getDeclaredMethod("run");

                PrintStream originalOut = System.out;
                ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
                System.setOut(new PrintStream(capturedOut));
                try {
                    run.invoke(null);
                } finally {
                    System.setOut(originalOut);
                }
                String output = capturedOut.toString();
                return output.isBlank() ? "(no output)" : output;
            }
        } catch (Exception e) {
            return "Error: " + e;
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private static void deleteRecursively(Path path) {
        try (var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (Exception ignored) {
                    // best-effort cleanup
                }
            });
        } catch (Exception ignored) {
            // best-effort cleanup
        }
    }
}
