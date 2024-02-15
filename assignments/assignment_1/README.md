# Assignment 1 

## Problem 1 

### Description

To approximate Pi using the Bailey-Borwein-Plouffe formula, we define a number of digits we want the output to be formatted in, a number of terms we want the cumputation to be run for increased precision and finally we pass an executer service to run the method in parallel. 

We split the work being the terms to the amount of cores the machine has available. This guarantees an even distribution of the calculations across all threads. The tasks get submitted to the executer service and are computed. 

Finally, we iterate synchronously over the futures and add the partial sums to the total sum. Since we distributed the work evenly this can happen synchronously perfectly fine as we exepct all tasks to be finished or almost being finised as soon as the first one completed. 
Thus, the first future of the loop blocks the execution of further code until it completes and by that time most partial sums should be available, so that there's no further waiting. 

### Run Code

To run the code, you can just execute the main method of problem_1 file/class. Or could just run the publicly available method `approximatePiParallel` 
