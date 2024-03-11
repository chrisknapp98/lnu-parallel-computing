# Assignment 2

## Problem 1 - Approximating Pi

### Description

Like in assignment 1, we use the Bailey-Borwein-Plouffe formula. We again define a number of digits and a number of terms. To implement mpi, we use the recommended mpi4py and create an instance with `MPI.COMM_WORLD`.

The parallelization works like in assignment1 and splits the number of terms depending on the number of the available mpi instances. Then, every instance does the calculation for the given parameters. After finishing the calculation, the results are collected using `MPI.COMM_WORLD.reduce` with the operation parameter set to `MPI.SUM` so we receive the expected sum.

The time calculation starts before the term seperation and ends when finishing the calculation so we track the effort of both splitting the work and calculating the sum. The instance with rank 0 then prints the output showing us the execution time for the different amount of terms.

### Run Code

To run the code sequentially, you can use the function `approximate_pi` and just simply run the python file.

```sh
python problem_1.py
```

For the parallel execution with mpi4py you need to use the function `approximate_pi_parallel` and run it with the following command:

```sh
mpiexec -n 4 problem_1.py
```

The parameter n defines the number of instances to use for the calculation.

### Results

The results of the calculation can be seen in the following table. We compare the sequential runtime to the runs using mpi 
with different values for the n parameter that defines the number of mpi instances.

n | 10.000 (s) | 100.000 (s) | 250.000 (s) | 500.000 (s) | 750.000 (s)
--|--|--|--|--|--
seq| 0.03 | 0.59 | 7.35 | 43.95 | 93.90
1| 0.03 | 0.60 | 7.51 | 43.64 | 92.86
2| 0.02 | 0.39 | 6.54 | 36.34 | 90.96
4| 0.01 | 0.22 | 4.35 | 22.47 | 55.43
8| 0.01 | 0.19 | 2.87 | 13.58 | 34.05
12| 0.01 | 0.15 | 2.29 | 11.83 | 26.67
16| 0.06 | 0.19 | 2.44 | 21.32 | 22.24

Running the calculation sequential has nearly the same runtime results as the calculation using mpi with `n=1` which of course makes sense because in both cases there is only one instance doing the calculation.

Running more instances decreases the runtime for nearly all numbers of terms. For example the comparison between `n=1` and `n=2` shortens the runtime for 100.000 terms by almost half. 

Unexpectedly, there is almost no improvement for the higher amount of terms. Only when using a bigger n than 4 we can also reduce the runtime for the high amount of terms. It might happen because the calculation of the partial sum for higher indexes takes longer than using small indexes so splitting the terms in equal parts is not optimal. 

The optimal number of instances is at about 12, which is also the amount of kernels the computer has, on which the calculations were performed. The use of a higher number of instances is worsening the results for instances smaller or equal to 500.000. Interesting is, that the calculation for 750.000 terms is still improved for 16 instances and also nearly the same runtime as for 500.000 terms. The issue we were experiencing with higher indexes of the sum seems to take advantage of higher number of instances so the work split is better for the runtime.

## Problem 2 - Applying Filters on an Image

### Description

#### Gaussian Blur 

As we decided to implement the MPI problems in python, first we implemented the gaussuan blur sequentially without parallelization. Then, for curiosity we also implemented a shared memory version using numba. Compared to the java implementation from aissignment 1 it performed way better and that shows how well optimized the numba module is.
Then it was time to implement a parallel version with the MPI approach. The image is read, a numpy array is being created from that and then we have to split the image into chunks in order to distribute fractions of the image to different workers. Even though a worker only gets a specific area of the image, still those areas need to overlap with other areas to compute the ideal blurred image. That's what the `distribute_chunks_with_overlap` method takes care of. The chunks are being distributed through `comm.scatter(chunks, root=0)` and the blur is applied to each single chunk. The results from each worker are being collected through `comm.gather(blurred_chunk, root=0)` and afterwards the overlapping parts need to be trimmed. Finally, the image needs to be created from the array and written to the target destination. 

#### Sobel Edge Detection

### Run Code 

The script contains a sequential implementation, as well as a parallel numba implemenetation and a parallel MPI implementation.

To run the sequential code or the numba code just run it through 

```sh 
python problem_2.py
```

To run the MPI version though it's important to have `open-mpi` installed. Then it can be run through the code below, while flag n sets the number of cores to use.

```sh
mpiexec -n 8 python problem_2.py
```

### Results 

#### Gaussian Blur

In the sequential approach, where the code runs synchronously, the execution time increases modestly as the radius increases, starting from 14.90 seconds for a radius of 1 and going up to 16.52 seconds for a radius of 9. This gradual increase is expected because a larger radius means more data points are involved in the calculations for each pixel's blur effect, leading to slightly longer execution times.

| Execution Mode | Threads/Processes | Radius | Execution Time (s) |
| -------------- | ----------------- | ------ | ------------------ |
| Sequential     | N/A               | 1      | 14.90              |
| Sequential     | N/A               | 3      | 15.30              |
| Sequential     | N/A               | 5      | 15.67              |
| Sequential     | N/A               | 7      | 16.01              |
| Sequential     | N/A               | 9      | 16.52              |

The use of Numba for parallel execution dramatically improves performance, showcasing the benefits of parallel computing. With just 1 thread, the execution times are significantly reduced compared to the sequential execution, starting at 0.75 seconds for radius 1 and reaching up to 2.05 seconds for radius 9. This already represents a substantial improvement.
Increasing the number of threads further decreases the execution time. With 2 threads, the execution time for radius 1 drops to 0.07 seconds, and with 8 threads, it remains low at 0.05 seconds for the same radius. For larger radii, the execution times also decrease with more threads, demonstrating excellent scalability with the number of threads up to 8, where execution time for radius 9 is just 0.38 seconds.

The performance of the Gaussian blur using MPI for parallel execution, surprisingly, does not follow the trend seen with Numba. Despite parallelizing the computation across multiple processes, the execution times are significantly longer than both the sequential and Numba parallel executions.
For instance, running the operation with a single process (which essentially simulates a sequential execution in an MPI framework) yields a much higher execution time of 31.83 seconds for radius 1 and even longer for radius 3, at 158.23 seconds, before the process was manually aborted due to excessive duration.
Even when increasing the number of processes to 2 and 4, the execution times, although improved, remain much higher than the Numba parallel execution. For example, with 4 processes, the execution time for radius 1 is 8.39 seconds, and for radius 9, it escalates to 301.70 seconds. The performance does improve as the number of processes increases to 8, but the execution times are still not competitive with the Numba implementation, marking 5.29 seconds for radius 1 and 195.39 seconds for radius 9.

### Parallel Execution Efficiency Comparison

| Configuration   | Radius 1         | Radius 3         | Radius 5         | Radius 7         | Radius 9         |
| --------------- | ---------------- | ---------------- | ---------------- | ---------------- | ---------------- |
| Sequential      | 14.90s           | 15.30s           | 15.67s           | 16.01s           | 16.52s           |
| Numba 1 Thread  | 0.75s (-94.96%)  | 0.32s (-97.91%)  | 0.72s (-95.41%)  | 1.28s (-92.00%)  | 2.05s (-87.56%)  |
| Numba 2 Threads | 0.07s (-99.53%)  | 0.17s (-98.89%)  | 0.39s (-97.51%)  | 0.67s (-95.81%)  | 1.06s (-93.59%)  |
| Numba 4 Threads | 0.05s (-99.66%)  | 0.11s (-99.28%)  | 0.21s (-98.66%)  | 0.38s (-97.63%)  | 0.57s (-96.55%)  |
| Numba 8 Threads | 0.05s (-99.66%)  | 0.09s (-99.41%)  | 0.16s (-98.98%)  | 0.26s (-98.38%)  | 0.38s (-97.70%)  |
| MPI 1 Process   | 31.83s (+113%)   | 158.23s (+933%)  | -                | -                | -                |
| MPI 2 Processes | 16.47s (+10.48%) | 81.36s (+431%)   | 196.96s (+1153%) | -                | -                |
| MPI 4 Processes | 8.39s (-43.69%)  | 42.03s (+174%)   | 100.80s (+542%)  | 187.10s (+1067%) | 301.70s (+1725%) |
| MPI 8 Processes | 5.29s (-64.50%)  | 25.76s (+68.56%) | 62.57s (+298%)   | 120.31s (+651%)  | 195.39s (+1082%) |

* "-" indicates the execution was manually aborted due to excessive duration.
* Percentage changes are calculated relative to the sequential execution time for the same radius.

The contrast in performance between Numba and MPI implementations for parallel computing in this case study is noteworthy. Numba's parallel execution with threads demonstrates exceptional efficiency and scalability for the Gaussian blur operation, significantly outperforming the MPI approach, which struggles to achieve similar efficiency gains. This might be surprising given MPI's widespread use in high-performance computing for distributed memory systems. The poor performance of MPI in this context could be attributed to overheads associated with inter-process communication, which become pronounced for operations like Gaussian blur that require intensive data exchange. This analysis underscores the importance of choosing the right parallel computing approach based on the specific nature of the task and the computational resources available.


#### Sobel Edge Detection



## Problem 3 - Sorting

### Description

We used our algorithm of assignment 1 and translated it to python to use mpi4py. The sequential algorithm is still quicksort with the pivot value at the index equalling half the list size.

To parallelize this algorithm we have to keep track of the communication between the single instances. That's why we decided to distribute the list into equal parts and give each instance a part of the original list to sort. After that sorting, the mpi instance with rank 0 gathers the results and merges the single sorted lists together by searching for the smallest first item of all the sorted lists and then adding it to the result.

### Run code

Like in assignment 1, we use random generated lists with different sizes to measure the runtime. The lists are located in the inputs folder and have to look like the file `random_integers.json` (located inside this folder).

The main function of the file iterates over multiple of these files with special names so it might be necessary to either create files with the same names or change the logic of the loops.

To run the code use this command again

```sh
mpiexec -n 4 problem_3.py
```

and adjust the number of instances by changing the n parameter. 

### Results

We get the different results by outputs looking like that:

```
Instance size 10^3

Quicksort: 0.11 ms, Parallel quicksort: 21.39 ms
Quicksort: 0.08 ms, Parallel quicksort: 7.80 ms
Quicksort: 0.08 ms, Parallel quicksort: 15.55 ms
Quicksort: 0.09 ms, Parallel quicksort: 8.30 ms
Quicksort: 0.08 ms, Parallel quicksort: 14.90 ms
```

To keep track of all the different results also with multiple values for the n parameter, we calculated the averages of all five results and put them into the following table.

| n        | 10^3 (ms) | 10^4 (ms) | 10^5 (ms) |
|----------|-----------|-----------|-----------|
| Sequential | 1.05      | 12.33     | 168.55    |
| Sorted   | 0.07      | 0.95      | 14.44     |
| 1        | 1.67      | 22.38     | 831.77    |
| 2        | 1.13      | 14.12     | 401.07    |
| 4        | 0.97      | 9.42      | 217.74    |
| 8        | 1.59      | 13.04     | 216.68    |
| 12       | 2.25      | 15.51     | 212.19    |
| 16       | 14.12      | 48.73     | 356.20    |

The results of this parallelization is not really good. For small number of instances, the effort for the list splitting and merging is bigger than the improvements of the parallel work so we just increase runtime. We have a optimum with slight improvements for the smaller list at `n=4`. With higher n values we keep increasing the runtime, also because it gets more complicated to merge the lists when finishing the sorting. If we compare the runtime to the already predefined `sorted` function in python, there is no reason to parallelize the sorting algorithm because the predefined version is so much faster.

Compared to assignment 1 and using a threadpool to add the single recursive tasks to this pool, the parallel version of quicksort using mpi is just not meaningful. The communication inside a threadpool is so much easier so a new task can be instantly started after finishing one. With mpi, we firstly needed to simplify the parallelization of the algorithm, and then do the not optimal merging between the lists that only one mpi instance can do, so we have a lot of effort for the branch and bound of the task and wait for single instances to finish their work which slows the whole algorithm down.

## Problem 4 - Iterative Solver