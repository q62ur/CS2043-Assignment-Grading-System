/*-------------------------------------------------------------------
  TestCase Class
  Represents a single tes case used to evaluate student code.
  Stores input, expected output and result information.
-------------------------------------------------------------------*/

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestCase
{
  //Instance Variables
  //unique ID for the test case
  private int testID;
  //a short name/title for the test case
  private String title;
  //input that will be sent to the student's program
  private String input;
  //the expected output to compare with actual program output
  private String expectedOutput;
  //whether the program passed this test case
  private boolean passed;
  //store the most recent actual output for display
  private String lastActualOutput;

  //Constructors
  //full constructor
  public TestCase(int testID, String title, String input, String expectedOutput)
  {
    this.testID = testID;
    this.title = title;
    this.input = input;
    this.expectedOutput = expectedOutput;
    this.passed = false;
    this.lastActualOutput = "";
  }

  //simple constructor without ID
  public TestCase(String title, String input, String expectedOutput)
  {
    this(-1, title, input, expectedOutput);
  }

  //Getters and Setters
  public int getTestID()
  {
    return testID;
  }
  public void setTestID(int testID)
  {
    this.testID = testID;
  }

  public String getTitle()
  {
    return title;
  }
  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getInput()
  {
    return input;
  }
  public void setInput(String input)
  {
    this.input = input;
  }

  public String getExpectedOutput()
  {
    return expectedOutput;
  }
  public void setExpectedOutput(String expectedOutput)
  {
    this.expectedOutput = expectedOutput;
  }

  public boolean isPassed()
  {
    return passed;
  }

  public String getLastActualOutput()
  {
    return lastActualOutput;
  }

  //compares the actual program output to the expected output
  //updates the 'passed' flag accordingly
  public void compareOutput(String actualOutput)
  {
    if(actualOutput == null)
    {
      this.passed = false;
      this.lastActualOutput = null;

      return;
    }

    this.lastActualOutput = actualOutput;

    this.passed = actualOutput.trim().equals(expectedOutput.trim());
  }
  
  //simple v1 format: line0 = title, line1 = input, line2 = expected output.
  public void initFromFile(String filename) throws IOException
  {
    File file = new File(filename);
  
    var lines = Files.readAllLines(file.toPath());
  
    String t   = (lines.size() > 0) ? lines.get(0) : "";
    String in  = (lines.size() > 1) ? lines.get(1) : "";
    String exp = (lines.size() > 2) ? lines.get(2) : "";
  
    this.title          = t.trim();
    this.input          = in;
    this.expectedOutput = exp;
  }

  //saves this test case's input and expected output to the given text files.
  public void saveToFiles(File inputFile, File expectedFile) throws IOException
  {
    if(input != null)
    {
      Files.writeString(inputFile.toPath(), input);
    }

    if(expectedOutput != null)
    {
      Files.writeString(expectedFile.toPath(), expectedOutput);
    }
  }
  
  //For Debugging / Display
  public String toString()
  {
    return "TestCase{" + "testID = " + testID + ", title = '" + title + '\'' + ", input = '" + input + '\'' + ", expectedOutput = '" + expectedOutput + '\'' + ", passed = " + passed + '}';
  }
}
