import java.io.File;
import java.util.ArrayList;
import java.util.List;

/************************************************************************************
* Maintains a list of Program objects, each representing one student submission.
* Responsible for scanning subfolders, building the list of programs,
* executing a test suite across all programs and (in V2) producing Result summaries.
*************************************************************************************/
public class ListOfPrograms
{
    private List<Program> listOfPrograms;

    /**
    * Initializes an empty list of Program objects.
    */
    public ListOfPrograms() {
        listOfPrograms = new ArrayList<>();
    }

    public List<Program> getListOfPrograms() {
        return listOfPrograms;
    }

    /*************************************************************************
    * Scans a root folder and creates one Program object for each subfolder.
    * Subfolder name is used as the program/student name.
    *
    * @param rootFolder path that contains one subfolder per student program
    ***************************************************************************/
    public void generateProgramsFromSubfolders(File rootFolder) {
        listOfPrograms.clear();

        if (rootFolder == null || !rootFolder.isDirectory()) {
            return;
        }

        File[] subfolders = rootFolder.listFiles(File::isDirectory);
        if (subfolders == null) {
            return;
        }

        for (File folder : subfolders) {
            String programName = folder.getName();     // subfolder name = Program name
            Program p = new Program(programName, folder);
            listOfPrograms.add(p);
        }
    }

    /*******************************************************************************
    * Executes the given TestSuite against all programs in the list.
    * For each program: compile(), run test cases, and return formatted output text.
    * This is V1 formatting intended for UI display, not structured result analysis.
    *
    * @param ts test suite being executed
    * @return combined human-readable execution text
    ********************************************************************************/
    public String executeTS(TestSuite ts) {
        StringBuilder resText = new StringBuilder();

        for (Program p : listOfPrograms) {
            boolean ok = p.compile();   // COMPILE()

            resText.append("Program ").append(p.getProgramName()).append(":\n");

            if (!ok) {
                resText.append("  COMPILATION ERROR\n\n");
                continue;
            }

            p.runTS(ts); // RUN(TS)

            // build simple result text for each test case
            for (TestCase tc : ts.getListOfTestCases()) {
                String output = p.getOutputFor(tc);
                // you might have tc.isPass() or similar in your TestCase – add if you do
                resText.append("  TestCase \"")
                        .append(tc.getTitle())          // or getName() depending on your class
                        .append("\" -> output: ")
                        .append(output)
                        .append("\n");
            }
            resText.append("\n");
        }

        return resText.toString();
    }

    /**************************************************************************
    * V2: builds a Result object for this suite for every Program in the list.
    *
    * @param ts test suite used to compute pass / fail statistics
    * @return list of Result objects, one per Program
    ***************************************************************************/
    public List<Result> buildResultsForSuite(TestSuite ts) {
        List<Result> results = new ArrayList<>();

        if (ts == null) {
            return results;
        }

        int total = ts.getListOfTestCases().size();

        for (Program p : listOfPrograms) {
            boolean ok = p.compile();

            if (!ok) {
                Result r = new Result(p.getProgramName(), 0, total, "COMP_ERR");
                results.add(r);
                continue;
            }

            p.runTS(ts);
            Result r = p.buildResult(ts);
            results.add(r);
        }

        return results;
    }

}
