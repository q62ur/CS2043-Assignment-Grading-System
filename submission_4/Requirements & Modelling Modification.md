# Modifications to Submission #1 

Important: Since it was instructed that we do not need a full re-implementation of our submission, I will mention only the parts of our requirements stage that are going to be modified.

## Class Diagram Modifications:  
NOTE: Anything relating to trace in any class will be removed as it is no longer a feature in our system.  
#### Result Class (New)  
A new class/object will be needed called "Result". A Result Object will have studentName, casesPassed, casesFailed, and status.  
#### Program Class  
will be edited to have a "mainFile" and "mainClassName" to accomodate the multiple files feature being accepted.  
#### Coordinator Class  
We will have 4 new operations: saveResult(), (save the results after running from test suite)
loadResult(), (to load the results for viewing)
formatResult(), (format the name of the file that will be saved)
compareResultFiles() (for multiple test suites or just for side by side comparison for student files)

## Sequence Diagram Modifications:
The only test suite affected is the Execure TS diagram, every other diagram already created (following submission 2 partial solution) is already fine, if there is time we can make a diagram to
save, load or view results files.  
For the Execute TS modification, we will keep the logic up until where we run each submission on a test case, once the test case is done running and we determine pass or fail,
we will save the result in the list with other results. Once we're done with all the programs we save the list in our serializable file for this submission.

## Functional & Non-Functional Requirements Modification:

### Test Suite & Test Case Management 
New FR: Be able to execute the same test suite on a different folder

### Submission Management 
New FR: Be able to reload results and visualize them again  
New FR: Be able to compare the success rates side-by-side upon selection of 2 different result files  
New FR: Be able to handle submitted programs as multiple files rather than a single file only.  
  
### System Compiling 
-- No New FR or NFR --  
  
### Test Case Results 
New FR: Be able to store the results of running a test suite. 




