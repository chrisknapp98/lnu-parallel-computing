import time
import numpy as np
from math import sqrt, ceil, floor
from mpi4py import MPI
import sys
from pathlib import Path
# workaround to be able to import common.heat
parent_dir = Path(__file__).resolve().parents[1]
sys.path.append(str(parent_dir))
import common.heat as heat


def update_cell(m, i, j):
    """Update a single cell and return the squared difference."""
    tmp = m[i, j]
    m[i, j] = (m[i, j-1] + m[i, j+1] + m[i-1, j] + m[i+1, j]) / 4
    diff = tmp - m[i, j]
    return diff ** 2

def update_cells_by_color(m, color):
    """Update either red or black cells based on the color parameter."""
    s = 0.0
    for i in range(1, m.shape[0]-1):
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
        res = gauss_seidel_step_chessboard(m)
        if res < tol:
            break
    return (res, i)

def init_section(grid_size, rank, size):
    """
    Initialize the grid and return the section of the grid that the process will work on.
    Rows are overlapping between processes.
    """
    full_grid = heat.init(heat.heat_sources, grid_size)
    grid_size = full_grid.shape[0]
    local_grid_size = ceil(grid_size / size)
    remainder = grid_size - (size - 1) * local_grid_size
    
    local_grid = None

    local_grid_start = rank * local_grid_size
    if rank != 0:
        local_grid_start -= 1
    local_grid_end = (rank + 1) * local_grid_size
    if rank != size - 1:
        local_grid_end += 1

    if remainder > 0:
        if rank == size - 1:
            local_grid = full_grid[-remainder:, :]
        else:
            local_grid = full_grid[local_grid_start:local_grid_end, :]
    else:
        local_grid = full_grid[local_grid_start:local_grid_end, :]

    return local_grid

def gauss_seidel_mpi(grid_size, maxiter, tol):
    """
    Gauss-Seidel method with MPI parallelization.
    Rows are overlapping and communicated between processes on every iteration.
    As different sections of the grid are computed in parallel by different processes, 
    the residual will not be as accurate as computing sequentially.
    """
    comm = MPI.COMM_WORLD
    rank = comm.Get_rank()
    size = comm.Get_size()

    local_grid = init_section(grid_size, rank, size)

    iterations = 0
    global_residual = float('inf')
    done = False

    while not done:
        if rank > 0:
            comm.send(local_grid[1, :], dest=rank-1)
            local_grid[0, :] = comm.recv(source=rank-1)
        
        if rank < size - 1:
            comm.send(local_grid[-2, :], dest=rank+1)
            local_grid[-1, :] = comm.recv(source=rank+1)

        local_residual = gauss_seidel_step_chessboard(local_grid)
        global_residual = comm.allreduce(local_residual, op=MPI.SUM)

        if rank == 0:
            if global_residual < tol or iterations >= maxiter:
                done = True

        done = comm.bcast(done, root=0)
        iterations += 1

    return global_residual, iterations


def gauss_seidel_mpi_chessboard_with_color_sync(grid_size, maxiter, tol):
    """
    Gauss-Seidel method with chessboard ordering and color synchronization. 
    It might make the calculation more accurate, but it is a lot slower. 
    Also this will still not be as accurate as computing sequentially, 
    what makes the use of this implementation hard to justify.
    """
    comm = MPI.COMM_WORLD
    rank = comm.Get_rank()
    size = comm.Get_size()
    
    local_grid = init_section(grid_size, rank, size)

    iterations = 0
    done = False
    while not done and iterations < maxiter:
        for color in [0, 1]:
            local_residual = update_cells_by_color(local_grid, color)
            
            comm.Barrier()

            if rank != 0:
                send_data = local_grid[1, :]
                recv_data = np.empty_like(send_data)
                comm.Sendrecv(send_data, dest=rank-1, recvbuf=recv_data, source=rank-1)
                local_grid[0, :] = recv_data
            
            if rank != size-1:
                send_data = local_grid[-2, :]
                recv_data = np.empty_like(send_data)
                comm.Sendrecv(send_data, dest=rank+1, recvbuf=recv_data, source=rank+1)
                local_grid[-1, :] = recv_data 
            
            comm.Barrier()

        global_residual = comm.allreduce(local_residual, op=MPI.SUM)
        
        if rank == 0:
            if global_residual < tol:
                done = True
        done = comm.bcast(done, root=0)

        iterations += 1

    return global_residual, iterations


def run_mpi_with_given_params(grid_size, maxiter, tol):
    comm = MPI.COMM_WORLD
    rank = comm.Get_rank()

    res, i = gauss_seidel_mpi(grid_size, maxiter, tol)
    if rank == 0:
        print(f'Residual = {res} after {i} iterations')

def run_with_given_params(grid_size, maxiter, tol):
    m = heat.init(heat.heat_sources, grid_size)
    res, i = gauss_seidel(m, maxiter, tol)
    print(f'Residual = {res} after {i} iterations')

def run_and_test_mpi_scalability(maxiter, tol):
    grid_sizes = [50, 75, 100, 150]
    test_mpi_scalability(grid_sizes, maxiter, tol)

def test_mpi_scalability(grid_sizes, maxiter, tol):
    comm = MPI.COMM_WORLD
    rank = comm.Get_rank()
    size = comm.Get_size()

    if rank == 0:
        print(f"Testing with {size} processes...")
    for size in grid_sizes:
        if rank == 0:
            start_time = time.time()
        res, i = gauss_seidel_mpi(size, maxiter, tol)
        if rank == 0:
            end_time = time.time()
            execution_time = end_time - start_time
            print(f"  Grid Size: {size}x{size}, Execution Time: {execution_time:.2f} seconds, after {i} iterations, residual = {res}")

def run_and_test_serial_scalability(maxiter, tol):
    grid_sizes = [50, 75, 100, 150]
    test_serial_scalability(grid_sizes, maxiter, tol)

def test_serial_scalability(grid_sizes, maxiter, tol):
    for size in grid_sizes:
        m = heat.init(heat.heat_sources, size)
        start_time = time.time()
        res, i = gauss_seidel(m, maxiter, tol)
        # res, i = heat.gauss_seidel(m, maxiter, tol)
        end_time = time.time()
        execution_time = end_time - start_time
        print(f"Grid Size: {size}x{size}, Execution Time: {execution_time:.2f} seconds, after {i} iterations, residual = {res}")

if __name__ == '__main__':
    grid_size = 100
    maxiter = 25000
    tol = 0.00005

    # MPI implementation
    # run_mpi_with_given_params(grid_size, maxiter, tol)
    run_and_test_mpi_scalability(maxiter, tol)

    # Serial impleentation
    # run_and_test_serial_scalability(maxiter, tol)
