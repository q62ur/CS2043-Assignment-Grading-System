import java.util.ArrayList;
import java.util.List;

/**
 * simplified list of test case class because
 * (this follows the prof's diagram for submission 3)
 */
public class ListOfTestCases {

    /**
     * internal list that holds all the testcases the user creates
     */
    private List<TestCase> list;

    /**
     * creates an empty list. coordinator will add items to it.
     */
    public ListOfTestCases() {
        list = new ArrayList<>();
    }

    /**
     * add a test case into the list.
     * this is basically the "store" part in the design.
     */
    public void add(TestCase tc) {
        if (tc != null) {
            list.add(tc);
        }
    }

    /**
     * simple search function. in v1 we look up by the test case title
     * because the prof's diagram says "search" but with no complex rules.
     */
    public TestCase searchByTitle(String title) {
        for (TestCase t : list) {
            if (t.getTitle().equals(title)) {
                return t;
            }
        }
        // if not found, return null. no fancy exception needed in v1
        return null;
    }

    /**
     * returns all test cases. used by test suite or ui sometimes.
     */
    public List<TestCase> getAll() {
        return list;
    }
}
