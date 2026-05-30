/**
* Created a simplified ListOfTestSuites Class
* Edited for simplicity when using the UI
*
****/
public class ListOfTestSuites {

    /** 
     * our test suite instance
     */
    private TestSuite x;

    /**
     * Store (or replace) the system's single TestSuite.
     * Called by Coordinator.createTestSuite().
     */
    public void add(TestSuite ts) {
        this.x = ts;
    }

    /**
     * V1 behaviour: search(name) simply returns the stored suite.
     * subm3 reqs said only 1 test case so we just return x
     */
    public TestSuite search(String name) {
        return x;
    }

    /**
     * Returns the single instance of TestSuite. 
     * same reasoning as search(name) 
     */
    public TestSuite getSuite() {
        return x;
    }
}
