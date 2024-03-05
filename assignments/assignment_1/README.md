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


## Problem 2 - Applying Filters on an Image 

### Description
The task involves implementing two image processing techniques: Gaussian blur and Sobel filter, with a focus on optimizing the Gaussian blur using parallel processing in Java. The Gaussian blur algorithm applies a kernel to each pixel of the image to produce a blurred effect. This process is computationally intensive, especially for large images or when using a large radius for the blur effect.

The code is structured to allow comparison between a non-parallel (sequential) version and a parallelized version of the Gaussian blur. The parallelized version utilizes Java's ForkJoinPool framework, which enables efficient execution of parallel tasks, particularly beneficial for CPU-bound tasks like image processing. By dividing the image into chunks and processing each chunk in parallel, we aim to significantly reduce the overall execution time. The chunks are disgned to be of similar size, so that the calculation should take roughly the same time for every chunk. 

The execution begins through looping over all tasks and calling `pool.execute(task)`. After completing the loop. We enter into another one to get or await the tasks value through `task.get()`. In this case it's actually just the completion of the `void` returning method. `task.get()` is a blocking call. So as we wait for the calculation of one task in one thread we actually also wait implicitly on all other threads to complete their current task. The thread pool autmatically starts the next task in an available thread, so that waiting time is actually minimal. It is important though to access the tasks value in the same order they were started.
Since the image is passed to every single task, we have to ensure that the tasks do not interfere with each other through simultaneous modification. To avoid this, we use Java's `synchronized` keyword on the `outputImage` object. This ensures that only one thread can modify the image at a time, preventing race conditions and ensuring the integrity of the image processing operation.


### Run Code
To run the code, ensure you have a Java development environment set up with JDK 8 or later. Place your image files in the specified directory (e.g., "../assets/") and adjust the file names in the code accordingly. You can compile the Java files using a command line or an IDE, then run the main class that initiates the image processing tasks.

For the parallel version, adjust the radius and sigma parameters as desired and observe how the ForkJoinPool distributes the work across multiple threads.

### Results
The execution times for the Gaussian blur with different radius values were recorded, highlighting the performance benefits of parallel processing. Here's a summary of the observed results:

Run   | Radius    | Sigma | Non-Parallel Execution Time (s)   |   Parallel Execution Time (s) | 
| --------- | --------- | ----- | --------------------------------- | ----------------------------- | 
| 1         | 10        | 20.0  | 14.94                             | 3.59                          |

The results clearly demonstrate the efficiency of parallelizing the Gaussian blur operation. With a radius of 10 and a sigma of 20.0, the parallelized version of the code executes significantly faster than the non-parallel version, reducing the execution time by approximately 76%.

A higher radius leads to significantly longer execution time the impact of sigma seems to have a lower impact.