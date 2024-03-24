from mpi4py import MPI
import json
import time
import numpy as np

def quicksort(lst):
    if len(lst) <= 1:
        return lst
    pivot = lst[len(lst) // 2]
    less = [x for x in lst if x < pivot]
    equal = [x for x in lst if x == pivot]
    greater = [x for x in lst if x > pivot]
    return quicksort(less) + equal + quicksort(greater)

def parallel_quicksort(lst, comm):
    rank = comm.Get_rank()
    lst = comm.bcast(lst, root=0)

    local_lst = distribute_data(lst, comm)
    local_lst_sorted = quicksort(local_lst)

    sorted = comm.gather(local_lst_sorted, root=0)

    if rank == 0:
        final_sorted_list = merge_sorted(sorted)
        return final_sorted_list
    else:
        return None

def distribute_data(lst, comm):
    rank = comm.Get_rank()
    size = comm.Get_size()
    local_n = len(lst) // size
    remainder = len(lst) % size
    local_lst = lst[rank * local_n + min(rank, remainder):(rank + 1) * local_n + min(rank + 1, remainder)]
    return local_lst

def merge_sorted(sorted_lst):
    result = []
    while any(sorted_lst):
        min_index = min(i for i in range(len(sorted_lst)) if sorted_lst[i])
        result.append(sorted_lst[min_index].pop(0))
    return result

def read_list_from_json_file(file_name):
    with open(file_name, 'r') as f:
        data = json.load(f)
    return list(map(int, data))

def main():
    comm = MPI.COMM_WORLD

    for i in range(3, 6):
        if comm.Get_rank() == 0: print(f"Instance size 10^{i}\n")
        for j in range(1, 6):
            numbers = read_list_from_json_file(f"assignments/assignment_1/inputs/random_integers_{i}_{j}.json")
            if comm.Get_rank() == 0:
                start_time = time.time()
                quicksort(numbers)
                #sorted(numbers)
                print(f"Quicksort: {((time.time() - start_time) * 1000):.2f} ms, ", end='')
            comm.barrier()

            if comm.Get_rank() == 0: start_time = time.time()
            numbers = parallel_quicksort(numbers, comm)
            if comm.Get_rank() == 0: print(f"Parallel quicksort: {((time.time() - start_time) * 1000):.2f} ms")
        if comm.Get_rank() == 0: print()

if __name__ == "__main__":
    main()