# Assignment 3

## Problem 1 - Approximating Pi



## Problem 2 - Applying Filters on an Image

### Description
To execute and test the written algorithms we again use Google Colab with an NVIDIA GPU. The T4 GPU was mainly used in development, but the code was also tested on an A100 and V100.

The codes for the gaussian blur and the sobel filter are very similar. The only difference is that for the guassian blur we need a kernel in addition to the calculation. 
We read the image, create a zero matrix like the image matrix for the resulting image. We copy the readed image matrix, the kernel and the resulting image matrix to the device. 
After that we setup the grid and block sizes. This part is specific to the used GPU. Executing the code we used for the NVIDIA T4 lead to a crash on the A100 and V100 GPUs therefore the total amount of threads had to be lowered.
The function to apply the kernel in parallel with `@cuda.jit` annotation is executed and we can achieve great parallelization with that, what can be seen results.


### Run code
To run the code, an NVIDIA GPU needs to be available and the dependencies must be installed. Then the single code block inside the jupyter notebook can just be executed with the wanted method.
Using Google Colab to run the code is recommended as we used it during development and it was very easy to use.
Just make sure to create an assets folder and upload an image and change the image's name in the python code to match the file name.


### Results

#### Gaussian Blur
First we simply ran the serial version of the code to see the CPU performance on those machines. As expected, it's not great.

| Radius | Execution Time (seconds) |
| ------ | ------------------------ |
| 1      | 41.35                    |
| 3      | 42.38                    |
| 5      | 43.47                    |
| 7      | 47.54                    |
| 9      | 49.49                    |

Then we ran the parallel implementation on the NVIDIA T4 and got outstanding execution times for different threads per block and different radii. The best results were achieved with a quadratic distribution of threads per block creating a total of 256 threads with (16, 16). As it handled radii <20 so fast, we highered the numbers up to 80 and achieved a very fast execution time of only 3.42 seconds. On the CPU the same calculation would have taken significantly longer 

| Threads per Block | Radius | Execution Time (seconds) |
| ----------------- | ------ | ------------------------ |
| (16, 16)          | 5      | 0.75                     |
| (16, 16)          | 10     | 0.18                     |
| (16, 16)          | 20     | 0.27                     |
| (16, 16)          | 40     | 0.87                     |
| (16, 16)          | 80     | 3.42 (best config)       |
| (32, 8)           | 5      | 0.04                     |
| (32, 8)           | 10     | 0.07                     |
| (32, 8)           | 20     | 0.24                     |
| (32, 8)           | 40     | 0.92                     |
| (32, 8)           | 80     | 3.73                     |
| (8, 32)           | 5      | 0.04                     |
| (8, 32)           | 10     | 0.07                     |
| (8, 32)           | 20     | 0.25                     |
| (8, 32)           | 40     | 0.88                     |
| (8, 32)           | 80     | 3.50                     |
| (32, 32)          | 5      | 0.04                     |
| (32, 32)          | 10     | 0.08                     |
| (32, 32)          | 20     | 0.27                     |
| (32, 32)          | 40     | 1.02                     |
| (32, 32)          | 80     | 4.34                     |
| (10, 10)          | 5      | 0.04                     |
| (10, 10)          | 10     | 0.09                     |
| (10, 10)          | 20     | 0.29                     |
| (10, 10)          | 40     | 1.09                     |
| (10, 10)          | 80     | 4.27                     |


The NVIDIA A100 and V100 show even lower execution times than the T4 GPU with an ideal configuration of, this time a lower amount of total threads being 64 and (8, 8). The same good execution time can also be seen with a total of 265 threads and the configuration (16, 16) and 128 threads and (32, 4) and (4, 32). It takes even less than a second for the GPUs to apply the gaussian blur on an image with a radius of 80, what is quite impressive.

| Threads per Block | Radius | A100 Time (s) | V100 Time (s) |
| ----------------- | ------ | ------------- | ------------- |
| (8, 8)            | 5      | 0.25          | 1.45          |
| (8, 8)            | 10     | 0.04          | 0.04          |
| (8, 8)            | 20     | 0.08          | 0.08          |
| (8, 8)            | 40     | 0.22          | 0.26          |
| (8, 8)            | 80     | 0.71 (best)   | 0.89          |
| (16, 16)          | 5      | 0.02          | 0.02          |
| (16, 16)          | 10     | 0.03          | 0.03          |
| (16, 16)          | 20     | 0.07          | 0.08          |
| (16, 16)          | 40     | 0.20          | 0.24          |
| (16, 16)          | 80     | 0.72          | 0.91          |
| (32, 4)           | 5      | 0.02          | 0.02          |
| (32, 4)           | 10     | 0.03          | 0.03          |
| (32, 4)           | 20     | 0.06          | 0.08          |
| (32, 4)           | 40     | 0.20          | 0.24          |
| (32, 4)           | 80     | 0.71 (best)   | 0.93          |
| (4, 32)           | 5      | 0.02          | 0.03          |
| (4, 32)           | 10     | 0.03          | 0.04          |
| (4, 32)           | 20     | 0.06          | 0.09          |
| (4, 32)           | 40     | 0.20          | 0.25          |
| (4, 32)           | 80     | 0.71 (best)   | 0.91          |
| (14, 14)          | 5      | 0.02          | 0.03          |
| (14, 14)          | 10     | 0.03          | 0.05          |
| (14, 14)          | 20     | 0.07          | 0.09          |
| (14, 14)          | 40     | 0.22          | 0.31          |
| (14, 14)          | 80     | 0.84          | 1.16          |

#### Sobel Filter
Since we have
The sobel filter has less variability through different radii whatsoever. As we have seen in the results of the gaussian blur, the Google Colab GPU machines do not have very well performing CPUs. So the run times of the sequential sobel edge filter is between 28 and 30 seconds while on a MacBook Pro with M1 Pro chip it takes less than 10 seconds.

| Run Number | Execution Time (seconds) |
| ---------- | ------------------------ |
| 1          | 28.46                    |
| 2          | 29.42                    |
| 3          | 28.43                    |
| 4          | 28.38                    |
| 5          | 29.55                    |


Running the filter in parallel on the GPU also gives us quite a performance improvement over running it sequentially on the CPU. For all of the tried configurations we get an impressive execution time of only 0.02 seconds. That is even faster than the numba implementation for the CPU on a MacBook Pro with M1 Pro chip which took 0.04 seconds.

| Threads per Block | Execution Time (seconds) |
| ----------------- | ------------------------ |
| (16, 16)          | 0.02                     |
| (32, 8)           | 0.02                     |
| (8, 32)           | 0.02                     |
| (32, 32)          | 0.02                     |
| (10, 10)          | 0.02                     |


## Problem 3 - Iterative Solver

