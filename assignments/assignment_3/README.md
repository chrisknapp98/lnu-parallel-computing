# Assignment 3

## Problem 1 - Approximating Pi

### Description

To parallelize the code using the gpu, we use the package `cupy`. We can easily calculate the single sums with the `bailey_borwein_plouf` function. To compare it to a not gpu-parallelized version, we change `cp.sum`/`cp.arange` to `np.sum`/`np.arange` and use simple `numpy` to calculate the results. 

### Run Code

The code is provided in `problem_1.ipynb`.

To run the code simply install the necessary packages and run the specific cell.

The first cell with the parallel version can only be run when the computer has a NVDIA gpu and the cuda driver installed. The second cell with the sequential version only needs python and has no other dependency.

### Results

The results are shown below and are calculated using the two different code cells in the jupyter notebook.

numpy
```
Terms:  100000 , Execution time:  7.173  ms, Pi approximation:  3.141592653589793
Terms:  1000000 , Execution time:  52.927  ms, Pi approximation:  3.141592653589793
Terms:  10000000 , Execution time:  689.888  ms, Pi approximation:  3.141592653589793
Terms:  100000000 , Execution time:  5837.467  ms, Pi approximation:  3.141592653589793
```

cupy
```
Terms:  100000 , Execution time:  2.204  ms, Pi approximation:  3.1415926535897936
Terms:  1000000 , Execution time:  5.128  ms, Pi approximation:  3.1415926535897936
Terms:  10000000 , Execution time:  41.811  ms, Pi approximation:  3.1415926535897936
Terms:  100000000 , Execution time:  291.806  ms, Pi approximation:  3.1415926535897936
```

The parallelization works great. The numpy calculation is already very fast and the cupy version can improve the runtime results up to 20 times. We also needed to improve the maximum number of terms compared to the last assignments because the calculation is so fast.

Apparently gpus are pretty good in doing simple calculations like the bailey-borwein-plouf-formula. The many kernels can efficiently be used for calculating the single summands which is what was expected by us.

## Problem 2 - Applying Filters on an Image



## Problem 3 - Iterative Solver

