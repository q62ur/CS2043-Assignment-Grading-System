import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


public class Coordinator {

    private final ListOfTestCases testCaseList;
    private final ListOfTestSuites testSuiteList;  // V1: holds only ONE suite
    private final ListOfPrograms programList;

    /************************************************************************************
    * Creates a Coordinator that manages test cases, test suites, and programs.
    * Initializes the internal lists used for Version 1 and Version 2 of the tool.
    ************************************************************************************/
    public Coordinator() 
    {
        testCaseList = new ListOfTestCases();
        testSuiteList = new ListOfTestSuites();    
        programList  = new ListOfPrograms();
    }

    // -------------------------------------------------------
    // TEST CASE MANAGEMENT
    // -------------------------------------------------------

    /*********************************************************************************
    * Creates a new TestCase based on user-provided title, input, and expected output.
    * The created TestCase is automatically added to the internal TestCase list.
    *
    * @param title name of the test case
    * @param input input data for the student program
    * @param expectedOutput expected output after running the student program
    * @return newly created TestCase object
    ***********************************************************************************/
    public TestCase createTestCase(String title, String input, String expectedOutput) {
        TestCase t = new TestCase(title, input, expectedOutput);
        testCaseList.add(t);
        return t;
    }
    
    /**************************************************************************
    * Saves an existing TestCase to two disk files: one containing input
    * and one containing expected output. TestCase is located by its title.
    *
    * @param title test case title to search
    * @param inputFile file to write input content to
    * @param expectedFile file to write expected output content to
    *****************************************************************************/
    public void saveTestCase(String title, File inputFile, File expectedFile) {
        TestCase t = testCaseList.searchByTitle(title);
        if (t == null) {
            System.err.println("ERROR: TestCase \"" + title + "\" not found.");
            return;
        }

        try {
            t.saveToFiles(inputFile, expectedFile);
        } catch (IOException e) {
            System.err.println("ERROR saving test case to files: " + e.getMessage());
        }
    }

    /**************************************************************************************
    * Loads a TestCase from disk using a file containing title, input, and expected output.
    * The loaded TestCase is added to the internal TestCase list for later use.
    *
    * @param filename file path containing test case data
    * @return loaded TestCase object or null on failure
    ***************************************************************************************/
    public TestCase getTestCase(String filename)
    {
        TestCase t = new TestCase("", "", "");
        try
        {
            t.initFromFile(filename);
        }
        catch (IOException e)
        {
            System.err.println("ERROR loading test case from file: " + e.getMessage());
            return null;
        }
    
        //store it in the list so it can be used later (add to suite, etc.)
        testCaseList.add(t);
        return t;
    }

    
    // -------------------------------------------------------
    // TEST SUITE MANAGEMENT
    // -------------------------------------------------------

    /*********************************************************************
    * Creates a new TestSuite with a given name and stores it internally.
    * V1 style: only one suite is expected to be created.
    *
    * @param name name of the test suite
    * @return created TestSuite instance
    **********************************************************************/
    public TestSuite createTestSuite(String name) {
        TestSuite ts = new TestSuite(name);
        testSuiteList.add(ts);  // stores only this one in V1
        return ts;
    }

    /***************************************************************************
    * Adds a test case (located by its title) to the currently stored TestSuite.
    * Displays simple error messages if TestSuite or TestCase cannot be found.
    *
    * @param testCaseTitle title of the test case to associate with suite
    *******************************************************************************/
    public void addTestCaseToSuite(String testCaseTitle) {

        TestSuite ts = testSuiteList.getSuite();  // V1: always the same suite
        if (ts == null) {
            System.err.println("ERROR: TestSuite does not exist yet.");
            return;
        }

        TestCase t = testCaseList.searchByTitle(testCaseTitle);
        if (t == null) {
            System.err.println("ERROR: TestCase \"" + testCaseTitle + "\" not found.");
            return;
        }

        ts.addTestCase(t);
    }

    // -------------------------------------------------------
    // EXECUTE TEST SUITE
    // -------------------------------------------------------

    /******************************************************************************
    * Triggers execution of a named TestSuite against all student programs found
    * in a root folder path. Each subfolder represents one program (one student).
    * Compilation and execution are done via ListOfPrograms, and this method returns
    * formatted execution text for UI display.
    *
    * @param suiteName name of test suite to locate
    * @param rootFolderPath path containing one student folder per program
    * @return formatted execution output for UI display
    *********************************************************************************/
    public String executeTestSuite(String suiteName, String rootFolderPath) {
        TestSuite ts = testSuiteList.search(suiteName);
        if (ts == null) {
            System.err.println("ERROR: TestSuite \"" + suiteName + "\" not found.");
            return "No such test suite.";
        }

        File rootFolder = new File(rootFolderPath);
        programList.generateProgramsFromSubfolders(rootFolder);

        return programList.executeTS(ts);
    }

    /************************************************************************************
    * V2: Executes the given test suite and returns a summary based on Result objects.
    * Uses ListOfPrograms.buildResultsForSuite to compute pass/fail statistics for
    * each student program instead of listing raw outputs for every test case.
    *
    * @param suiteName name of test suite to locate
    * @param rootFolderPath path containing one folder per student program
    * @return formatted summary built from Result objects
    ************************************************************************************/
    public String executeTestSuiteWithResults(String suiteName,
                                              String rootFolderPath,
                                              File optionalResultFile) {
        TestSuite ts = testSuiteList.search(suiteName);
        if (ts == null) {
            System.err.println("ERROR: TestSuite \"" + suiteName + "\" not found.");
            return "No such test suite.";
        }

        File rootFolder = new File(rootFolderPath);
        programList.generateProgramsFromSubfolders(rootFolder);

        List<Result> results = programList.buildResultsForSuite(ts);

        // 1) Save results to .ser
        try {
            saveResultsToFile(results, ts, rootFolder, optionalResultFile);
        } catch (IOException e) {
            System.err.println("ERROR saving results: " + e.getMessage());
        }

        // 2) Build human-readable summary
        StringBuilder summary = new StringBuilder();
        for (Result r : results) {
            summary.append("Program / Student: ").append(r.getStudentName()).append("\n");
            summary.append("  Passed: ").append(r.getPassed())
                   .append(" out of ").append(r.getTotal()).append("\n");
            summary.append("  Status: ").append(r.getStatus()).append("\n\n");
        }

        if (results.isEmpty()) {
            summary.append("No programs or no results available.\n");
        }

        return summary.toString();
    }

    /**
     * Saves a List<Result> to a .ser file.
     * If outFile is null, builds a default path:
     *   <root parent>/Results/<suite>_RESULTS_for_<rootFolderName>.ser
     */
    public void saveResultsToFile(List<Result> results,
                                  TestSuite ts,
                                  File rootFolder,
                                  File outFile) throws IOException {

        if (results == null || ts == null || rootFolder == null) {
            return;
        }

        File rootParent = rootFolder.getParentFile();
        File resultsDir = new File(rootParent, "Results");
        if (!resultsDir.exists()) {
            resultsDir.mkdir();
        }

        if (outFile == null) {
            String safeRootName = rootFolder.getName();
            String safeSuiteName = ts.getSuiteName().replaceAll("\\s+", "_");
            String fileName = safeSuiteName + "_RESULTS_for_" + safeRootName + ".ser";
            outFile = new File(resultsDir, fileName);
        }

        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(outFile))) {
            out.writeObject(results);
        }
    }

    // ---------- LOAD + DISPLAY RESULTS (for UI "view results") ----------

    @SuppressWarnings("unchecked")
    public List<Result> loadResultsFromFile(File resultFile) {
        if (resultFile == null || !resultFile.exists()) {
            return null;
        }
        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(resultFile))) {
            Object obj = in.readObject();
            if (obj instanceof List<?>) {
                return (List<Result>) obj;
            }
        } catch (Exception e) {
            System.err.println("ERROR loading results: " + e.getMessage());
        }
        return null;
    }

    public String displayResultsFromFile(File resultFile) {
        List<Result> results = loadResultsFromFile(resultFile);
        if (results == null || results.isEmpty()) {
            return "No results found in file.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Results from file: ").append(resultFile.getName()).append("\n\n");
        for (Result r : results) {
            sb.append("Student: ").append(r.getStudentName()).append("\n");
            sb.append("  Passed: ").append(r.getPassed())
              .append(" out of ").append(r.getTotal()).append("\n");
            sb.append("  Status: ").append(r.getStatus()).append("\n\n");
        }
        return sb.toString();
    }

    // ---------- COMPARE TWO RESULT FILES ----------

    /**
     * Compare success rates from 2 serialized result files.
     * Returns text with one line per student: name + a/b vs c/b or a code.
     */
    public String compareResultFiles(File firstFile, File secondFile) {
        List<Result> first = loadResultsFromFile(firstFile);
        List<Result> second = loadResultsFromFile(secondFile);

        if (first == null || second == null) {
            return "Error: one or both result files could not be loaded.";
        }

        // map student -> Result
        Map<String, Result> map1 = new HashMap<>();
        for (Result r : first) {
            map1.put(r.getStudentName(), r);
        }
        Map<String, Result> map2 = new HashMap<>();
        for (Result r : second) {
            map2.put(r.getStudentName(), r);
        }

        // union of student names
        StringBuilder sb = new StringBuilder();
        sb.append("Comparing: ").append(firstFile.getName())
          .append("  vs  ").append(secondFile.getName()).append("\n\n");

        for (String student : map1.keySet()) {
            Result r1 = map1.get(student);
            Result r2 = map2.get(student);

            sb.append("Student: ").append(student).append("\n");

            if (r1 == null) {
                sb.append("  First:  NO_SUBMISSION\n");
            } else {
                sb.append("  First:  ")
                  .append(r1.getPassed()).append("/").append(r1.getTotal())
                  .append("   (").append(r1.getStatus()).append(")\n");
            }

            if (r2 == null) {
                sb.append("  Second: NO_SUBMISSION\n");
            } else {
                sb.append("  Second: ")
                  .append(r2.getPassed()).append("/").append(r2.getTotal())
                  .append("   (").append(r2.getStatus()).append(")\n");
            }

            sb.append("\n");
        }

        // Students present only in second
        for (String student : map2.keySet()) {
            if (!map1.containsKey(student)) {
                Result r2 = map2.get(student);
                sb.append("Student: ").append(student).append("\n");
                sb.append("  First:  NO_SUBMISSION\n");
                sb.append("  Second: ")
                  .append(r2.getPassed()).append("/").append(r2.getTotal())
                  .append("   (").append(r2.getStatus()).append(")\n\n");
            }
        }

        return sb.toString();
    }
}
