import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TestSuite
 * ----------
 * Represents a collection of TestCase objects.
 * 
 * Only ONE TestSuite is used in your assignment
 * (as required by your instructions), but the class
 * supports general list behavior.
 */
public class TestSuite implements Serializable 
{

    // optional naming
    private String suiteName; 
    // list of test cases                 
    private List<TestCase> testCases;    
    // for auto-incrementing test case IDs      
    private int nextID;                        


    public TestSuite(String suiteName) 
    {
        this.suiteName = suiteName;
        this.testCases = new ArrayList<>();
        this.nextID = 1;
    }

    public TestSuite() 
    {
        this("Default Test Suite");
    }


    /**
     * Add a TestCase created by user input.
     * Automatically assigns a unique ID.
     */
    public void addTestCase(String title, String input, String expectedOutput) 
    {
        TestCase tc = new TestCase(nextID++, title, input, expectedOutput);
        testCases.add(tc);
    }

    /**
     * Add a TestCase object directly (if already constructed).
     */
    public void addTestCase(TestCase tc) 
    {
        // Ensure ID is unique
        if (tc.getTestID() < 0) {
            tc.setTestID(nextID++);
        } else {
            nextID = Math.max(nextID, tc.getTestID() + 1);
        }
        testCases.add(tc);
    }


    public String getSuiteName() 
    {
        return suiteName;
    }

    public List<TestCase> getListOfTestCases() 
    {
        return testCases;
    }

    public int getNumberOfTestCases() 
    {
        return testCases.size();
    }

    public TestCase getTestCase(int index) 
    {
        if (index < 0 || index >= testCases.size()) 
            {
            return null;
        }
        return testCases.get(index);
    }

    

    /**
     * Removes test case by ID.
     */
    public boolean removeTestCaseByID(int id) 
    {
        return testCases.removeIf(tc -> tc.getTestID() == id);
    }

    /**
     * Removes test case by index.
     */
    public boolean removeTestCaseAt(int index) 
    {
        if (index < 0 || index >= testCases.size()) return false;
        testCases.remove(index);
        return true;
    }

    /**
     * Clears all test cases.
     */
    public void clear() 
    {
        testCases.clear();
        nextID = 1;
    }

    
    @Override
    public String toString() 
    {
        return "TestSuite{name='" + suiteName +
               "', testCases=" + testCases.size() + "}";
    }
}
