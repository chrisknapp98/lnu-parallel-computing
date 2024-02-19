# Assignment 1 

## Problem 1 - Approximating Pi

### Description

To approximate Pi using the Bailey-Borwein-Plouffe formula, we define a number of digits we want the output to be formatted in, a number of terms we want the cumputation to be run for increased precision and finally we pass an executer service to run the method in parallel. 

We split the work being the terms to the amount of cores the machine has available. This guarantees an even distribution of the calculations across all threads. The tasks get submitted to the executer service and are computed. 

Finally, we iterate synchronously over the futures and add the partial sums to the total sum. Since we distributed the work evenly this can happen synchronously perfectly fine as we exepct all tasks to be finished or almost being finised as soon as the first one completed. 
Thus, the first future of the loop blocks the execution of further code until it completes and by that time most partial sums should be available, so that there's no further waiting. 


### Run Code

To run the code, you can just execute the main method of problem_1 file/class. Or could just run the publicly available method `approximatePiParallel`. 
The non-parallel function `approximatePi` is also available.


### Results

As we would expect, running the `approximatePi` function which is not doing any work in parallel takes much longer than `approximatePiParallel`.
Both methods are executed with the same parameters being
    - `digits`: 7
    - `terms`: 1000000
The function `approximatePi` takes 134.848 seconds while `approximatePiParallel` accomplishes to make the calculation in just 4.883 seconds showing a significant difference in performance between both functions. The code was executed on a MacBook Pro with M1 Pro Chip.