import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents a single student program (folder).
 * Can compile, execute test cases, support tracing, and now
 * generate Result objects and save them as .ser files.
 */
public class Program 
{

    private String programName;        // folder / "student" name
    private File programFolder;        // folder containing .java files

    // detected main file + class name
    private File mainFile;
    private String mainClassName;
    // Stores output for each test case (older requirement)
    private Map<TestCase, String> outputsByTestCase = new HashMap<>();

    // For tracing feature
    private Program traceClone;

    public Program(String programName, File programFolder) 
    {
        this.programName = programName;
        this.programFolder = programFolder;
    }

    public String getProgramName() 
    {
        return programName;
    }

    public File getProgramFolder() 
    {
        return programFolder;
    }

    public String getOutputFor(TestCase tc) 
    {
        return outputsByTestCase.get(tc);

    }

    private boolean detectMainFile() {
        File[] javaFiles = programFolder.listFiles((dir, name) -> name.endsWith(".java"));
        if (javaFiles == null) {
            return false;
        }

        for (File f : javaFiles) {
            try {
                String content = Files.readString(f.toPath());
                if (content.contains("public static void main(")) {
                    mainFile = f;
                    String fileName = f.getName();
                    int dot = fileName.lastIndexOf('.');
                    mainClassName = (dot > 0) ? fileName.substring(0, dot) : fileName;
                    return true;
                }
            } catch (Exception e) {
                System.err.println("ERROR: " + e.getMessage());
            }
        }
        return false; // no main found
    }
    
    public boolean compile() {
        if (!detectMainFile()) {
            System.err.println("No main method found in folder " + programFolder.getName());
            return false;
        }

        List<String> cmd = new ArrayList<>();
        cmd.add("javac");
        cmd.add(mainFile.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();

            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {

                String line;
                while ((line = r.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exit = p.waitFor();
            return exit == 0;

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return false;
        }
    }



    public String exec(String inputStr) {
        if (!detectMainFile()) {
            return "";
        }

        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        cmd.add("-cp");
        cmd.add(programFolder.getAbsolutePath());
        cmd.add(mainClassName);

        ProcessBuilder pb = new ProcessBuilder(cmd);

        StringBuilder output = new StringBuilder();

        try {
            Process process = pb.start();

            if (inputStr != null) {
                try (OutputStream out = process.getOutputStream()) {
                    out.write(inputStr.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
            } else {
                process.getOutputStream().close();
            }

            try (BufferedReader reader = new BufferedReader(
                     new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            process.waitFor();
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return "";
        }

        return output.toString().trim();
    }
/**
    // -----------------------------------------------------------
    //                         EXECUTE TS
    //  Updated: creates Result object, counts pass/fail,
    //  adds to list, and saves .ser file to Results/ folder.
    // -----------------------------------------------------------
    public List<Result> oldRunTS(TestSuite ts) 
    {

        List<Result> results = new ArrayList<>();

        // Create result entry for this program/student
        Result res = new Result(programName);

        // Run every test case
        for (TestCase tc : ts.getListOfTestCases()) 
            {

            String inputStr = tc.getInput();
            String output = exec(inputStr);

            // Save the output internally
            outputsByTestCase.put(tc, output);

            // Compare actual vs expected
            tc.compareOutput(output);

            // Count pass/fail
            if (tc.isPassed()) {
                res.incrementPassed();
            } else {
                res.incrementFailed();
            }
        }

        // Determine program status
        if (res.getCasesFailed() == 0) 
            {
            res.setStatus("PASSED");
        } else {
            res.setStatus("FAILED");
        }

        // Add to list (only one Result per program)
        results.add(res);

        // ------------------------------------------------------
        // SAVE RESULT TO SERIALIZED FILE
        // ------------------------------------------------------
        try {
            // Parent folder of this program directory
            File rootFolder = programFolder.getParentFile();

            // Results/ folder next to the Programs/ folder
            File resultsFolder = new File(rootFolder, "Results");

            if (!resultsFolder.exists()) {
                resultsFolder.mkdir();
            }

            // Example: Results/JohnProgram.ser
            File outFile = new File(resultsFolder, programName + ".ser");

            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(outFile))) {

                out.writeObject(res);
                System.out.println("✔ Result saved to: " + outFile.getAbsolutePath());
            }

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.out.println("ERROR: Could not save results.");
        }

        return results;
    }
**/
    public void runTS(TestSuite ts) {
            for (TestCase tc : ts.getListOfTestCases()) {
                String inputStr = tc.getInput();
                String output = exec(inputStr);
    
                outputsByTestCase.put(tc, output);
                tc.compareOutput(output);
        }
    }

    public Result buildResult(TestSuite ts) {
        int passed = 0;
        int total = ts.getListOfTestCases().size();

        for (TestCase tc : ts.getListOfTestCases()) {
            if (tc.isPassed()) {
                passed++;
            }
        }
        // status "OK" assumes compile+run succeeded; caller can override.
        return new Result(programName, passed, total, "OK");
    }
}
