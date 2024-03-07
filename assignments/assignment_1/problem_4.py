import time
import numpy as np
from math import sqrt
from numba import njit, prange, set_num_threads
import sys
from pathlib import Path
# workaround to be able to import common.heat
parent_dir = Path(__file__).resolve().parents[1]
sys.path.append(str(parent_dir))
import common.heat as heat


@njit(parallel=True)
def gauss_seidel_step(m):
    s = 0.0
    for i in prange(1, m.shape[0]-1):
        for j in range(1, m.shape[1]-1):
            tmp = m[i, j]
            m[i, j] = (m[i, j-1] + m[i, j+1] + m[i-1, j] + m[i+1, j]) / 4
            diff = tmp - m[i, j]
            s += diff**2 # Adjusted for correctness: previously s += diff**2 * diff**2
    return s

@njit
def update_cell(m, i, j):
    """Update a single cell and return the squared difference."""
    tmp = m[i, j]
    m[i, j] = (m[i, j-1] + m[i, j+1] + m[i-1, j] + m[i+1, j]) / 4
    diff = tmp - m[i, j]
    return diff ** 2

@njit(parallel=True)
def update_cells_by_color(m, color):
    """Update either red or black cells based on the color parameter."""
    s = 0.0
    for i in prange(1, m.shape[0]-1):
        for j in range(1, m.shape[1]-1):
            if (i + j) % 2 == color:
                s += update_cell(m, i, j)
    return s

def gauss_seidel_step_chessboard(m):
    (red_color_code, black_color_code) = (0, 1)
    s_red = update_cells_by_color(m, red_color_code) 
    s_black = update_cells_by_color(m, black_color_code)
    return s_red + s_black

def gauss_seidel(m, maxiter, tol):
    for i in range(maxiter):
        # res = gauss_seidel_step(m)
        res = gauss_seidel_step_chessboard(m)
        if res < tol:
            break
    return (res, i)

def run_with_given_params(grid_size, maxiter, tol):
    m = heat.init(heat.heat_sources, grid_size)
    res, i = gauss_seidel(m, maxiter, tol)
    print(f'residual = {res} after {i} iterations')

def trigger_jit_compilation():
    run_with_given_params(10, 1, 1)

def run_and_test_scalability(maxiter, tol):
    grid_sizes = [100, 250, 500, 1000, 1500]
    thread_counts = [1, 2, 4, 8]
    test_scalability(grid_sizes, thread_counts, maxiter, tol)

def test_scalability(grid_sizes, thread_counts, maxiter, tol):
    for num_threads in thread_counts:
        set_num_threads(num_threads)
        print(f"Testing with {num_threads} threads...")
        for size in grid_sizes:
            m = heat.init(heat.heat_sources, size)
            start_time = time.time()
            res, i = gauss_seidel(m, maxiter, tol)
            end_time = time.time()
            
            execution_time = end_time - start_time
            print(f"  Grid Size: {size}x{size}, Execution Time: {execution_time:.2f} seconds, after {i} iterations, residual = {res}")

if __name__ == '__main__':
    maxiter = 25000
    tol = 0.00005
    # run_with_given_params(100, maxiter, tol)
    trigger_jit_compilation()
    run_and_test_scalability(maxiter, tol)
