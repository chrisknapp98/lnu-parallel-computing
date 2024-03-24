import time
from mpi4py import MPI

def approximate_pi(digits, terms):
    if digits < 0:
        raise ValueError("The number of digits must be non-negative")
    sum = 0
    for k in range(terms):
        sum += bailey_borwein_plouf(k)
    return sum

def approximate_pi_parallel(start, end):
    sum = 0
    for k in range(start, end):
        sum += bailey_borwein_plouf(k)
    return sum

def bailey_borwein_plouf(k):
    term1 = 4 / (8 * k + 1)
    term2 = 2 / (8 * k + 4)
    term3 = 1 / (8 * k + 5)
    term4 = 1 / (8 * k + 6)
    term = term1 - term2 - term3 - term4
    term = term * (1/16)**k
    return term

def run_with_given_params(digits, terms):
    try:
        pi_approximation = approximate_pi(digits, terms)
        print("Pi approximation: ", pi_approximation)
    except Exception as e:
        print("Error: ", str(e))

def run_and_test_scalability(digits):
    comm = MPI.COMM_WORLD
    rank = comm.Get_rank()
    size = comm.Get_size()
    terms = [10000, 100000, 500000, 1000000, 2000000]
    for term in terms:
        pi_approximation = 0
        start_time = time.time()
        terms_per_process = term // size
        remaining_terms = term % size
        start = rank * terms_per_process
        end = (rank + 1) * terms_per_process
        if rank == size - 1:
            end += remaining_terms
        local_sum = approximate_pi_parallel(start, end)
        pi_approximation = comm.reduce(local_sum, op=MPI.SUM)
        if rank == 0:
            end_time = time.time()
            execution_time = "{:.2f}".format((end_time - start_time)*1000)
            print("   Terms: ", term, ", Execution time: ", execution_time, " ms, Pi approximation: ", round(pi_approximation, digits))

if __name__ == "__main__":
    digits = 7
    terms = 100000

    #run_with_given_params(digits, terms)
    run_and_test_scalability(digits)