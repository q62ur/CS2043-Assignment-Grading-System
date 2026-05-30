import java.io.Serializable;

/**
 * Represents the overall result of running a program on a full Test Suite.
 * - studentName = name of the program (folder name)
 * - casesPassed = number of test cases passed
 * - casesFailed = number of test cases failed
 * - status = "PASSED", "FAILED", etc.
 *
 * Serializable so it can be written to .ser files.
 */
public class Result implements Serializable {

    private String studentName;
    private int passed;
    private int total;
    private String status;  // "PASSED", "FAILED", etc.

    public Result(String studentName, int passed, int total, String status) {
        this.studentName = studentName;
        this.passed = passed;
        this.total = total;
        this.status = status;
    }

    // ----------------- GETTERS -----------------
    public String getStudentName() {
        return studentName;
    }

    public int getPassed() {
        return passed;
    }

    public int getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Result{ Student =" + studentName + ", Cases Passed=" + passed + "/" + total + ", Status =" + status + " }";
    }
}
