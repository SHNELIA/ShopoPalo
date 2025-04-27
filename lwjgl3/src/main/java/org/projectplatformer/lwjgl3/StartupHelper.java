package org.projectplatformer.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader;
import java.lang.management.ManagementFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StartupHelper {

    private StartupHelper() {
        throw new UnsupportedOperationException();
    }

    public static boolean startNewJvmIfRequired() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("mac")) {
            return handleMacOS();
        } else if (osName.contains("windows")) {
            handleWindows();
        }

        return false;
    }

    private static void handleWindows() {
        try {
            String programData = System.getenv("ProgramData");
            if (programData == null) {
                programData = "C:\\Temp\\";
            }
            String tempDir = programData + "/libGDX-temp";
            String prevTmp = System.getProperty("java.io.tmpdir");
            String prevUser = System.getProperty("user.name");

            System.setProperty("java.io.tmpdir", tempDir);
            System.setProperty("user.name", ("User_" + prevUser.hashCode()).replace('.', '_'));

            Lwjgl3NativesLoader.load();

            System.setProperty("java.io.tmpdir", prevTmp);
            System.setProperty("user.name", prevUser);
        } catch (Exception e) {
            System.err.println("Failed to fix native library loading on Windows:");
            e.printStackTrace();
        }
    }

    private static boolean handleMacOS() {
        String pid = getPid();

        if (!System.getProperty("org.graalvm.nativeimage.imagecode", "").isEmpty()) {
            return false;
        }

        if ("true".equals(System.getProperty("jvmIsRestarted"))) {
            System.err.println("Already restarted JVM once. Possible misconfiguration.");
            return false;
        }

        if ("1".equals(System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid))) {
            return false;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(buildJvmArgs());
            pb.inheritIO();
            pb.start();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to restart JVM with -XstartOnFirstThread:");
            e.printStackTrace();
            return false;
        }
    }

    private static List<String> buildJvmArgs() {
        String separator = System.getProperty("file.separator");
        String javaExec = System.getProperty("java.home") + separator + "bin" + separator + "java";
        List<String> args = new ArrayList<>();

        args.add(javaExec);
        args.add("-XstartOnFirstThread");
        args.add("-DjvmIsRestarted=true");
        args.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));

        String mainClass = System.getenv("JAVA_MAIN_CLASS_" + getPid());
        if (mainClass == null) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            mainClass = trace[trace.length - 1].getClassName();
        }
        args.add(mainClass);

        return args;
    }

    private static String getPid() {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        int index = jvmName.indexOf('@');
        if (index > 0) {
            return jvmName.substring(0, index);
        }
        return "";
    }
}
