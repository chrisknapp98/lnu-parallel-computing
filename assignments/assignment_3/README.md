# Assignment 3

## Problem 1 - Approximating Pi



## Problem 2 - Applying Filters on an Image
<!-- 
Serial
```log
Running with radius 1...
    Execution time: 41.35 seconds
Running with radius 3...
    Execution time: 42.38 seconds
Running with radius 5...
    Execution time: 43.47 seconds
Running with radius 7...
    Execution time: 47.54 seconds
Running with radius 9...
    Execution time: 49.49 seconds
```

T4
```log
Running with (16, 16) threads per block...
    Execution time: 0.75 seconds, radius: 5
    Execution time: 0.18 seconds, radius: 10
    Execution time: 0.27 seconds, radius: 20
    Execution time: 0.87 seconds, radius: 40
    Execution time: 3.42 seconds, radius: 80
Running with (32, 8) threads per block...
    Execution time: 0.04 seconds, radius: 5
    Execution time: 0.07 seconds, radius: 10
    Execution time: 0.24 seconds, radius: 20
    Execution time: 0.92 seconds, radius: 40
    Execution time: 3.73 seconds, radius: 80
Running with (8, 32) threads per block...
    Execution time: 0.04 seconds, radius: 5
    Execution time: 0.07 seconds, radius: 10
    Execution time: 0.25 seconds, radius: 20
    Execution time: 0.88 seconds, radius: 40
    Execution time: 3.50 seconds, radius: 80
Running with (32, 32) threads per block...
    Execution time: 0.04 seconds, radius: 5
    Execution time: 0.08 seconds, radius: 10
    Execution time: 0.27 seconds, radius: 20
    Execution time: 1.02 seconds, radius: 40
    Execution time: 4.34 seconds, radius: 80
Running with (10, 10) threads per block...
    Execution time: 0.04 seconds, radius: 5
    Execution time: 0.09 seconds, radius: 10
    Execution time: 0.29 seconds, radius: 20
    Execution time: 1.09 seconds, radius: 40
    Execution time: 4.27 seconds, radius: 80
```

A100
```log
Running with (8, 8) threads per block...
    Execution time: 0.25 seconds, radius: 5
    Execution time: 0.04 seconds, radius: 10
    Execution time: 0.08 seconds, radius: 20
    Execution time: 0.22 seconds, radius: 40
    Execution time: 0.71 seconds, radius: 80
Running with (16, 16) threads per block...
    Execution time: 0.02 seconds, radius: 5
    Execution time: 0.03 seconds, radius: 10
    Execution time: 0.07 seconds, radius: 20
    Execution time: 0.20 seconds, radius: 40
    Execution time: 0.72 seconds, radius: 80
Running with (32, 4) threads per block...
    Execution time: 0.02 seconds, radius: 5
    Execution time: 0.03 seconds, radius: 10
    Execution time: 0.06 seconds, radius: 20
    Execution time: 0.20 seconds, radius: 40
    Execution time: 0.71 seconds, radius: 80
Running with (4, 32) threads per block...
    Execution time: 0.02 seconds, radius: 5
    Execution time: 0.03 seconds, radius: 10
    Execution time: 0.06 seconds, radius: 20
    Execution time: 0.20 seconds, radius: 40
    Execution time: 0.71 seconds, radius: 80
Running with (14, 14) threads per block...
    Execution time: 0.02 seconds, radius: 5
    Execution time: 0.03 seconds, radius: 10
    Execution time: 0.07 seconds, radius: 20
    Execution time: 0.22 seconds, radius: 40
    Execution time: 0.84 seconds, radius: 80
```

V100
```log
Running with (8, 8) threads per block...
    Execution time: 1.45 seconds, radius: 5
    Execution time: 0.04 seconds, radius: 10
    Execution time: 0.08 seconds, radius: 20
    Execution time: 0.26 seconds, radius: 40
    Execution time: 0.89 seconds, radius: 80
Running with (16, 16) threads per block...
    Execution time: 0.02 seconds, radius: 5
    Execution time: 0.03 seconds, radius: 10
    Execution time: 0.08 seconds, radius: 20
    Execution time: 0.24 seconds, radius: 40
    Execution time: 0.91 seconds, radius: 80
Running with (32, 4) threads per block...
    Execution time: 0.02 seconds, radius: 5
    Execution time: 0.03 seconds, radius: 10
    Execution time: 0.08 seconds, radius: 20
    Execution time: 0.24 seconds, radius: 40
    Execution time: 0.93 seconds, radius: 80
Running with (4, 32) threads per block...
    Execution time: 0.03 seconds, radius: 5
    Execution time: 0.04 seconds, radius: 10
    Execution time: 0.09 seconds, radius: 20
    Execution time: 0.25 seconds, radius: 40
    Execution time: 0.91 seconds, radius: 80
Running with (14, 14) threads per block...
    Execution time: 0.03 seconds, radius: 5
    Execution time: 0.05 seconds, radius: 10
    Execution time: 0.09 seconds, radius: 20
    Execution time: 0.31 seconds, radius: 40
    Execution time: 1.16 seconds, radius: 80
```
 -->

### Description
To execute and test the written algorithms we again use Google Colab with an NVIDIA GPU. The T4 GPU was mainly used in development, but the code was also tested on an A100 and V100.



### Run code
To run the code, an NVIDIA GPU needs to be available and the dependencies must be installed. Then the single code block inside the jupyter notebook can just be executed with the wanted method.
Using Google Colab to run the code is recommended as we used it during development and it was very easy to use.
Just make sure to create an assets folder and upload an image and change the image's name in the python code to match the file name.


### Results

| Radius | Execution Time (seconds) |
| ------ | ------------------------ |
| 1      | 41.35                    |
| 3      | 42.38                    |
| 5      | 43.47                    |
| 7      | 47.54                    |
| 9      | 49.49                    |


| Threads per Block | Radius | Execution Time (seconds) |
| ----------------- | ------ | ------------------------ |
| (16, 16)          | 5      | 0.75                     |
| (16, 16)          | 10     | 0.18                     |
| (16, 16)          | 20     | 0.27                     |
| (16, 16)          | 40     | 0.87                     |
| (16, 16)          | 80     | 3.42                     |
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


Here are some results running on different GPUs with lower total thread size due to 

| Threads per Block | Radius | A100 Time (s) | V100 Time (s) |
| ----------------- | ------ | ------------- | ------------- |
| (8, 8)            | 5      | 0.25          | 1.45          |
| (8, 8)            | 10     | 0.04          | 0.04          |
| (8, 8)            | 20     | 0.08          | 0.08          |
| (8, 8)            | 40     | 0.22          | 0.26          |
| (8, 8)            | 80     | 0.71          | 0.89          |
| (16, 16)          | 5      | 0.02          | 0.02          |
| (16, 16)          | 10     | 0.03          | 0.03          |
| (16, 16)          | 20     | 0.07          | 0.08          |
| (16, 16)          | 40     | 0.20          | 0.24          |
| (16, 16)          | 80     | 0.72          | 0.91          |
| (32, 4)           | 5      | 0.02          | 0.02          |
| (32, 4)           | 10     | 0.03          | 0.03          |
| (32, 4)           | 20     | 0.06          | 0.08          |
| (32, 4)           | 40     | 0.20          | 0.24          |
| (32, 4)           | 80     | 0.71          | 0.93          |
| (4, 32)           | 5      | 0.02          | 0.03          |
| (4, 32)           | 10     | 0.03          | 0.04          |
| (4, 32)           | 20     | 0.06          | 0.09          |
| (4, 32)           | 40     | 0.20          | 0.25          |
| (4, 32)           | 80     | 0.71          | 0.91          |
| (14, 14)          | 5      | 0.02          | 0.03          |
| (14, 14)          | 10     | 0.03          | 0.05          |
| (14, 14)          | 20     | 0.07          | 0.09          |
| (14, 14)          | 40     | 0.22          | 0.31          |
| (14, 14)          | 80     | 0.84          | 1.16          |



<!-- 
| Threads per Block | Radius | Execution Time (seconds) |
| ----------------- | ------ | ------------------------ |
| (16, 16)          | 5      | 0.75                     |
| (16, 16)          | 10     | 0.18                     |
| (16, 16)          | 20     | 0.27                     |
| (16, 16)          | 40     | 0.87                     |
| (16, 16)          | 80     | 3.42                     |
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
 -->

## Problem 3 - Iterative Solver

