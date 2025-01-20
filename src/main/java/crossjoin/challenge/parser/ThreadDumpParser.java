package crossjoin.challenge.parser;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import crossjoin.challenge.model.ThreadInfo;


public class ThreadDumpParser {

    private static final String SEPARATOR = ";";


    public static List<ThreadInfo> parseThreadDump(File threadDumpFile) throws IOException {
        List<ThreadInfo> threadInfos = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(threadDumpFile));
        String line;

        String currentDay = null;
        String currentHour = null;
        String currentMinute = null;
        String currentSecond = null;

        ThreadInfo currentThreadInfo = null;

        // Patterns
        Pattern timestampPattern = Pattern.compile("^([\\d\\-]+) ([\\d]+):([\\d]+):([\\d]+)");
        //Pattern threadPattern = Pattern.compile("\"(.*?)\" #(\\d+) (daemon )?prio=(\\d+) os_prio=(\\d+) cpu=([\\d.]+ms) elapsed=([\\d.]+s) tid=(\\S+) nid=(\\S+) (.*)");
        Pattern threadPattern = Pattern.compile("\"(.*?)\" #(\\d+) (daemon )?prio=(\\d+) os_prio=(\\d+) cpu=([\\d.]+ms) elapsed=([\\d.]+s) tid=(\\S+) nid=(\\S+) (.*?)\\[(.*?)\\]");
        Pattern statePattern = Pattern.compile("java\\.lang\\.Thread\\.State: (\\S+)");
        Pattern callPattern = Pattern.compile("at (.*)");

        while ((line = reader.readLine()) != null) {
            // Check Timestamp
            Matcher timestampMatcher = timestampPattern.matcher(line);
            if (timestampMatcher.find()) {
                currentDay = timestampMatcher.group(1);
                currentHour = timestampMatcher.group(2);
                currentMinute = timestampMatcher.group(3);
                currentSecond = timestampMatcher.group(4);
            }

            // Get Thread information
            Matcher threadMatcher = threadPattern.matcher(line);
            if (threadMatcher.find()) {
                currentThreadInfo = new ThreadInfo();
                currentThreadInfo.setThreadName(threadMatcher.group(1));
                currentThreadInfo.setThreadType(gatherExecutionThreadByName(threadMatcher.group(1)) );
                //Here, with the next two I was trying to see if I could find direct dependencies between threads. Left it but not needed
                currentThreadInfo.setThreadId(threadMatcher.group(8));
                currentThreadInfo.setWaitingOn(threadMatcher.group(11));

                // Set timestamp fields. This is a smart way of using it for search by second, minute, hour, etc!
                currentThreadInfo.setTimestampDay(currentDay + "00:00:00");
                currentThreadInfo.setTimestampHour(currentDay + " " + currentHour+ ":00:00");
                currentThreadInfo.setTimestampMinute(currentDay + " " + currentHour + ":" + currentMinute + ":00");
                currentThreadInfo.setTimestampSecond(currentDay + " " + currentHour + ":" + currentMinute + ":" + currentSecond);
            }

            // Thread state
            Matcher stateMatcher = statePattern.matcher(line);
            if (stateMatcher.find() && currentThreadInfo != null) {
                currentThreadInfo.setThreadState(stateMatcher.group(1));
            }

            // check stacktrace
            Matcher callMatcher = callPattern.matcher(line);
            if (callMatcher.find() && currentThreadInfo != null) {
                String fullMethodCall = callMatcher.group(1);
                currentThreadInfo.setLastCall(fullMethodCall);

                if (isCustomClass(fullMethodCall)) {
                    currentThreadInfo.setLastCustomCall(fullMethodCall);
                }
            }

            //check if thread is blocked. I assumed a blocked thread meant something was struggling to get the resource. Later found IO/Logging was getting stuck
            if (currentThreadInfo != null && "BLOCKED".equals(currentThreadInfo.getThreadState())) {
                currentThreadInfo.setContention(true);

            }

            // clean up for next line
            if (line.trim().isEmpty() && currentThreadInfo != null) {
                threadInfos.add(currentThreadInfo);
                currentThreadInfo = null;
            }
        }

        reader.close();
        return threadInfos;
    }

    private static String gatherExecutionThreadByName(String threadName) {

        return threadName.replaceAll("-\\d+$", ""); // to remove the "-#" at the end of the thread

    }

    public static void parse(String directory) {
        try {
            Path dirPath = Paths.get(directory);
            List<Path> files = new ArrayList<>();

            if (!Files.isDirectory(dirPath)) {
                throw new IOException(directory + " is not a directory");
            }

            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    if (file.getFileName().toString().startsWith("tuxedo")) {
                        System.out.println("Found file: " + file.getFileName().toString());
                        files.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });


            File result = new File(directory, "result3.csv");
            List<ThreadInfo> threadInfos = new ArrayList<>();
            files.forEach(file -> {
                try {
                    threadInfos.addAll(parseThreadDump(file.toFile()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            System.out.println("Found " + threadInfos.size() + " threads in " + files.size() + " files");


            result.createNewFile();
            writeCSVFile(threadInfos, result);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }

    private static void writeCSVFile(List<ThreadInfo> threadInfos, File result) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(result))) {
            // CSV Header
            writer.write("Day;Hour;Minute;second;Thread Type;Thread Name;Thread State;Last Called;Last Custom Call;contention;threadId;waitingOn");
            writer.newLine();


            for (ThreadInfo threadInfo : threadInfos) {
                String line = String.join(SEPARATOR, threadInfo.getTimestampDay(), threadInfo.getTimestampHour(), threadInfo.getTimestampMinute(), threadInfo.getTimestampSecond(), threadInfo.getThreadType(), threadInfo.getThreadName(), threadInfo.getThreadState(), threadInfo.getLastCall(), threadInfo.getLastCustomCall(), String.valueOf(threadInfo.isContention()), String.valueOf(threadInfo.getThreadId()), String.valueOf(threadInfo.getWaitingOn()));
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to write to file: " + e.getMessage());
        }

    }

    private static boolean isCustomClass(String methodCall) {

        return !(methodCall.startsWith("java.") || methodCall.startsWith("javax.") || methodCall.startsWith("sun.") || methodCall.startsWith("jdk.") || methodCall.startsWith("org.") || methodCall.startsWith("io.") || methodCall.startsWith("com.sun."));
    }
}


