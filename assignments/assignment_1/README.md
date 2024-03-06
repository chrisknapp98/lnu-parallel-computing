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

For the Sobel filter, running the calculations in parallel results in only slight improvements. This is primarily due to the inherently lower complexity of the Sobel filter algorithm compared to the Gaussian blur. The Gaussian blur's computational load can be significantly increased by parameters such as `radius`, which directly impacts the size of the convolution kernel and, consequently, the number of calculations required per pixel. In contrast, the Sobel filter uses a fixed-size kernel (typically 3x3), leading to a relatively consistent and lower computational load regardless of the image size. Therefore the overhead associated with managing parallel tasks can offset the gains from distributing this workload across multiple processors.


## Problem 4 - Iterative Solver

### Description

For this task we decided to switch to python since the provided code was in python, but also because the numpy module in python just makes it easier to work with matrices. First we took a look at the provided code and tried to parallelize that one by simply utilizing the numba library. Decorating the `gauss_seidel_step` method with the `@njit` annotation setting the `parallel` flag to `True` and using `prange` for the outer loop improved the performance significantly. 

During the initial examination of the provided code, we identified a potential optimization in the gauss_seidel_step method. The original increment for updating the grid was modified from `diff**2 * diff**2` to `diff**2`, aligning with standard practices for calculating residuals in iterative solvers. This correction ensures that the algorithm accurately measures convergence towards the solution.

Then we implemented the red-black aka. chessboard strategy from the lecture. We first calculate all the red cells and then all the black cells and add the accumulated results together. For each color we iterate over the rows in parallel and over the columns in the same thread, and calculate the partial results.

Numba's JIT compliation takes quite some time when a method is executed for the first time. For that reason, we execute the calculation first with minimal numbers only for the sake of triggering the JIT compilation for numba. Thus, no numbers are influenced by that.

### Run code 

To run the code, just execute the files main method, or call the modules `gauss_seidel_step` method for a parallelized version of the provided code or `gauss_seidel_step_chessboard` for the chessboard strategy. It takes a two dimensional matrix and returns the numeric residual value.

### Results

Overall, we can see significant improvements with the parallelization. However, the results might not be what we expect at first thought. 

- Chessboard Strategy: 
  - we can clearly see that if our grid size is ≤ 500, then an increased number of threads is not beneficial and even leads to increased execution times. But if the grid size is higher than that, then execution times get significantly lower. The reason for this is that a thread should have enough work to compute in order to overshadow the cost of creating a new thread. 
  - in conclusion we can say, that this algorithm and this implementation only scale well with more threads if the grid size also is higher than a respective value
- Parallelized version of provided code:
  - here we don't see such a threshold needed to be past in order to be worth the overhead of the parallelism. 
  - for very low grid sizes it performs slightly better than the chessboard approach, but for higher grid sizes it's a lot less performant.

Below we can see the execution times with different thread numbers and grid sizes. We get consistent results ±1sec running the code on a MacBook Pro with M1 Pro chip.

#### Chessboard Strategy
```log
Testing with 1 threads...
  Grid Size: 100x100, Execution Time: 0.24 seconds, after 2904 iterations, residual = 4.993483841261299e-05
  Grid Size: 250x250, Execution Time: 2.23 seconds, after 12128 iterations, residual = 4.9990415024837096e-05
  Grid Size: 500x500, Execution Time: 15.41 seconds, after 24999 iterations, residual = 8.015803490575582e-05
  Grid Size: 1000x1000, Execution Time: 54.77 seconds, after 24999 iterations, residual = 0.00017701935925512637
  Grid Size: 1500x1500, Execution Time: 113.55 seconds, after 24999 iterations, residual = 0.00026821604825312214
Testing with 2 threads...
  Grid Size: 100x100, Execution Time: 0.20 seconds, after 2904 iterations, residual = 4.993483841261314e-05
  Grid Size: 250x250, Execution Time: 1.59 seconds, after 12128 iterations, residual = 4.9990415024836974e-05
  Grid Size: 500x500, Execution Time: 9.50 seconds, after 24999 iterations, residual = 8.015803490575511e-05
  Grid Size: 1000x1000, Execution Time: 28.89 seconds, after 24999 iterations, residual = 0.0001770193592551322
  Grid Size: 1500x1500, Execution Time: 59.32 seconds, after 24999 iterations, residual = 0.0002682160482531211
Testing with 4 threads...
  Grid Size: 100x100, Execution Time: 0.22 seconds, after 2904 iterations, residual = 4.993483841261314e-05
  Grid Size: 250x250, Execution Time: 1.69 seconds, after 12128 iterations, residual = 4.999041502483697e-05
  Grid Size: 500x500, Execution Time: 8.02 seconds, after 24999 iterations, residual = 8.015803490575514e-05
  Grid Size: 1000x1000, Execution Time: 16.91 seconds, after 24999 iterations, residual = 0.00017701935925512879
  Grid Size: 1500x1500, Execution Time: 33.38 seconds, after 24999 iterations, residual = 0.0002682160482531188
Testing with 8 threads...
  Grid Size: 100x100, Execution Time: 0.29 seconds, after 2904 iterations, residual = 4.993483841261314e-05
  Grid Size: 250x250, Execution Time: 1.77 seconds, after 12128 iterations, residual = 4.999041502483697e-05
  Grid Size: 500x500, Execution Time: 7.15 seconds, after 24999 iterations, residual = 8.015803490575536e-05
  Grid Size: 1000x1000, Execution Time: 16.85 seconds, after 24999 iterations, residual = 0.00017701935925512846
  Grid Size: 1500x1500, Execution Time: 32.55 seconds, after 24999 iterations, residual = 0.00026821604825311574
```

#### Parallelized version of the provided code
```log 
Testing with 1 threads...
  Grid Size: 100x100, Execution Time: 0.20 seconds, after 2902 iterations, residual = 4.99338287640659e-05
  Grid Size: 250x250, Execution Time: 4.13 seconds, after 12123 iterations, residual = 4.998575676770169e-05
  Grid Size: 500x500, Execution Time: 29.66 seconds, after 24999 iterations, residual = 8.010501626631217e-05
  Grid Size: 1000x1000, Execution Time: 111.54 seconds, after 24999 iterations, residual = 0.0001769222934951842
  Grid Size: 1500x1500, Execution Time: 253.31 seconds, after 24999 iterations, residual = 0.0002680653919971762
Testing with 2 threads...
  Grid Size: 100x100, Execution Time: 0.16 seconds, after 2902 iterations, residual = 4.974224284810511e-05
  Grid Size: 250x250, Execution Time: 2.48 seconds, after 12123 iterations, residual = 4.9984882231965e-05
  Grid Size: 500x500, Execution Time: 17.24 seconds, after 24999 iterations, residual = 8.010502600403332e-05
  Grid Size: 1000x1000, Execution Time: 63.39 seconds, after 24999 iterations, residual = 0.00017692228049845992
  Grid Size: 1500x1500, Execution Time: 127.47 seconds, after 24999 iterations, residual = 0.00026806539305078176
Testing with 4 threads...
  Grid Size: 100x100, Execution Time: 0.16 seconds, after 2906 iterations, residual = 4.942460380654088e-05
  Grid Size: 250x250, Execution Time: 1.85 seconds, after 12123 iterations, residual = 4.999237730010826e-05
  Grid Size: 500x500, Execution Time: 9.63 seconds, after 24999 iterations, residual = 8.010443312557069e-05
  Grid Size: 1000x1000, Execution Time: 38.34 seconds, after 24999 iterations, residual = 0.000176922487854811
  Grid Size: 1500x1500, Execution Time: 77.40 seconds, after 24999 iterations, residual = 0.00026806546187836905
Testing with 8 threads...
  Grid Size: 100x100, Execution Time: 0.15 seconds, after 2906 iterations, residual = 4.9627793433316564e-05
  Grid Size: 250x250, Execution Time: 1.84 seconds, after 12117 iterations, residual = 4.9966760155853664e-05
  Grid Size: 500x500, Execution Time: 8.56 seconds, after 24999 iterations, residual = 8.011078764496609e-05
  Grid Size: 1000x1000, Execution Time: 26.33 seconds, after 24999 iterations, residual = 0.00017692432345149545
  Grid Size: 1500x1500, Execution Time: 55.79 seconds, after 24999 iterations, residual = 0.00026806714792968096
```