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

n | 10.000 | 100.000 | 250.000 | 500.000 | 750.000
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



## Problem 3 - Sorting

