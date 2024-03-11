import time
from PIL import Image
import numpy as np
from math import exp, sqrt
import os
from numba import njit, prange, set_num_threads
from mpi4py import MPI

# Adjusted for script directory usage
script_dir = os.path.dirname(os.path.realpath(__file__))

sobel_x = np.array([[-1, 0, 1], 
                    [-2, 0, 2], 
                    [-1, 0, 1]], dtype=np.int32)

sobel_y = np.array([[-1, -2, -1],
                    [ 0,  0,  0],
                    [ 1,  2,  1]], dtype=np.int32)

def read_image(path):
    try:
        return Image.open(path)
    except IOError as e:
        print(f"Error: {e}")
        return None

def write_image(image, path):
    create_outputs_directory_if_needed()
    try:
        image.save(path)
    except IOError as e:
        print(f"Error: {e}")

def create_outputs_directory_if_needed():
    outputs_directory = os.path.join(script_dir, "outputs")
    if not os.path.exists(outputs_directory):
        os.makedirs(outputs_directory)

def apply_gaussian_blur(image, kernel, radius, parallel=False):
    img_array = np.array(image)  # Convert PIL Image to NumPy array
    if parallel:
        blurred_img_array = apply_gaussian_blur_kernel_parallel(img_array, kernel, radius)
    else:
        blurred_img_array = apply_gaussian_blur_kernel_serial(img_array, kernel, radius)
    return Image.fromarray(blurred_img_array)  # Convert back to PIL Image

def apply_sobel(image, parallel=False):
    img_array = np.array(image.convert('L'))  # Convert to grayscale NumPy array
    if parallel:
        edges_img_array = apply_sobel_filter_parallel(img_array)
    else:
        edges_img_array = apply_sobel_filter_serial(img_array)
    return Image.fromarray(edges_img_array).convert('RGB')  # Convert back to PIL Image in RGB

def generate_gaussian_blur_kernel(radius, sigma):
    size = 2 * radius + 1
    kernel = np.zeros((size, size))
    sum_val = 0.0

    for i in range(-radius, radius + 1):
        for j in range(-radius, radius + 1):
            kernel[i + radius, j + radius] = exp(-(i**2 + j**2) / (2 * sigma**2))
            sum_val += kernel[i + radius, j + radius]

    kernel /= sum_val
    return kernel

def apply_gaussian_blur_kernel_serial(img_array, kernel, radius):
    height, width = img_array.shape[:2]
    padded_img = np.pad(img_array, ((radius, radius), (radius, radius), (0, 0)), 'edge')
    blurred_img = np.zeros_like(img_array)

    for y in range(height):
        for x in range(width):
            for c in range(3):  # For each color channel
                acc = np.sum(kernel * padded_img[y:y+2*radius+1, x:x+2*radius+1, c])
                blurred_img[y, x, c] = min(max(int(acc), 0), 255)

    return blurred_img

def apply_sobel_filter_serial(img_array):
    height, width = img_array.shape
    edges_img = np.zeros_like(img_array)

    for y in range(1, height-1):
        for x in range(1, width-1):
            gx = np.sum(sobel_x * img_array[y-1:y+2, x-1:x+2])
            gy = np.sum(sobel_y * img_array[y-1:y+2, x-1:x+2])
            edges_img[y, x] = min(sqrt(gx**2 + gy**2), 255)

    return edges_img

@njit(parallel=True)
def apply_gaussian_blur_kernel_parallel(img_array, kernel, radius):
    height, width = img_array.shape[:2]
    blurred_img = np.zeros_like(img_array)

    for y in prange(height):
        for x in prange(width):
            for c in range(3):  # For each color channel
                sum_val = 0.0
                for ky in range(-radius, radius + 1):
                    for kx in range(-radius, radius + 1):
                        px = min(max(x + kx, 0), width - 1)
                        py = min(max(y + ky, 0), height - 1)
                        sum_val += img_array[py, px, c] * kernel[ky + radius, kx + radius]
                blurred_img[y, x, c] = sum_val
    return blurred_img

@njit(parallel=True)
def apply_sobel_filter_parallel(img_array):
    height, width = img_array.shape
    edges_img = np.zeros((height, width), np.uint8)
    
    for y in prange(1, height-1):
        for x in prange(1, width-1):
            gx = 0.0
            gy = 0.0
            # Apply the Sobel kernel to the grayscale image
            for ky in range(-1, 2):
                for kx in range(-1, 2):
                    val = img_array[y + ky, x + kx]
                    gx += val * sobel_x[ky + 1, kx + 1]
                    gy += val * sobel_y[ky + 1, kx + 1]
            magnitude = int(min(np.sqrt(gx**2 + gy**2), 255))
            edges_img[y, x] = magnitude
    return edges_img

def distribute_chunks_with_overlap(img_array, size, radius):
    height = img_array.shape[0]
    chunk_height = height // size
    chunks = []
    for i in range(size):
        start = i * chunk_height - radius if i > 0 else 0
        end = (i + 1) * chunk_height + radius if i < size - 1 else height
        chunks.append(img_array[start:end])
    return chunks

def trim_and_concatenate_chunks(chunks, radius):
    trimmed_chunks = []
    for i, chunk in enumerate(chunks):
        if i == 0:
            trimmed_chunks.append(chunk[:-radius])
        elif i == len(chunks) - 1:
            trimmed_chunks.append(chunk[radius:])
        else:
            trimmed_chunks.append(chunk[radius:-radius])
    return np.concatenate(trimmed_chunks, axis=0)

def apply_gaussian_blur_chunk(img_array, kernel, radius):
    height, width = img_array.shape[:2]
    blurred_img = np.zeros_like(img_array)
    for y in range(height):
        for x in range(width):
            for c in range(3):
                sum_val = 0.0
                for ky in range(-radius, radius + 1):
                    for kx in range(-radius, radius + 1):
                        py = min(max(y + ky, 0), height - 1)
                        px = min(max(x + kx, 0), width - 1)
                        sum_val += img_array[py, px, c] * kernel[ky + radius, kx + radius]
                blurred_img[y, x, c] = sum_val
    return blurred_img

def apply_gaussian_blur_mpi(image_path, output_path, radius, sigma):
    comm = MPI.COMM_WORLD
    rank = comm.Get_rank()
    size = comm.Get_size()

    if rank == 0:
        image = read_image(image_path).convert('RGB')
        img_array = np.array(image)
        # chunks = np.array_split(img_array, size, axis=0)
        chunks = distribute_chunks_with_overlap(img_array, size, radius)
    else:
        chunks = None

    chunk = comm.scatter(chunks, root=0)
    kernel = generate_gaussian_blur_kernel(radius, sigma)
    blurred_chunk = apply_gaussian_blur_chunk(chunk, kernel, radius)
    # blurred_chunk = chunk * (rank + 1)  # Simple operation for testing
    blurred_chunks = comm.gather(blurred_chunk, root=0)

    if rank == 0:
        # Only the root process will execute this block
        blurred_img_array = trim_and_concatenate_chunks(blurred_chunks, radius)
        blurred_image = Image.fromarray(blurred_img_array.astype('uint8'), 'RGB')
        write_image(blurred_image, output_path)
    else:
        pass

def apply_sobel_chunk(img_array):
    height, width = img_array.shape
    edges_img = np.zeros((height, width), np.uint8)
    for y in range(1, height - 1):
        for x in range(1, width - 1):
            gx = np.sum(sobel_x * img_array[y - 1:y + 2, x - 1:x + 2])
            gy = np.sum(sobel_y * img_array[y - 1:y + 2, x - 1:x + 2])
            magnitude = int(min(np.sqrt(gx**2 + gy**2), 255))
            edges_img[y, x] = magnitude
    return edges_img

def apply_sobel_filter_mpi(image_path, output_path):
    comm = MPI.COMM_WORLD
    rank = comm.Get_rank()
    size = comm.Get_size()

    if rank == 0:
        image = read_image(image_path).convert('L')  # Sobel filter uses grayscale
        img_array = np.array(image)
        chunks = distribute_chunks_with_overlap(img_array, size, 1)
    else:
        chunks = None

    chunk = comm.scatter(chunks, root=0)
    edges_chunk = apply_sobel_chunk(chunk)
    edges_chunks = comm.gather(edges_chunk, root=0)

    if rank == 0:
        edges_img_array = trim_and_concatenate_chunks(edges_chunks, 1)
        edges_image = Image.fromarray(edges_img_array, 'L').convert('RGB')  # Convert back to RGB
        write_image(edges_image, output_path)


def create_blurred_image(image_path, output_path, radius, sigma, parallel):
    input_image = read_image(image_path)
    if input_image is None:
        print("Error: The input image could not be read")
        return
    kernel = generate_gaussian_blur_kernel(radius, sigma)
    blurred_image = apply_gaussian_blur(input_image, kernel, radius, parallel)
    write_image(blurred_image, output_path)

def create_image_with_sharp_edges(image_path, output_path, parallel):
    input_image = read_image(image_path)
    if input_image is None:
        print("Error: The input image could not be read")
        return
    edges_image = apply_sobel(input_image, parallel)
    write_image(edges_image.convert('RGB'), output_path)  # Convert grayscale to RGB before saving

def run_and_test_blur_mpi_scalability(image_path, output_path):
    comm = MPI.COMM_WORLD
    rank = comm.Get_rank()
    size = comm.Get_size()

    sigma = 20.0
    for radius in [1, 3, 5, 7, 9]:
        if rank == 0:
            start_time = time.time()
            print(f"Running with {size} processes and radius {radius}...")

        apply_gaussian_blur_mpi(image_path, output_path, radius, sigma)

        if rank == 0:
            end_time = time.time()
            execution_time = end_time - start_time
            print(f"    Execution time: {execution_time:.2f} seconds")
            
def run_and_test_blur_serial_scalability(image_path, output_path):
    sigma = 20.0
    for radius in [1, 3, 5, 7, 9]:
    # for radius in [1, 5, 7, 10, 20]:
        start_time = time.time()
        print(f"Running with radius {radius}...")
        create_blurred_image(image_path, output_path, radius, sigma, False)
        execution_time = time.time() - start_time
        print(f"    Execution time: {execution_time:.2f} seconds")

def run_and_test_blur_numba_parallel_scalability(image_path, output_path):
    sigma = 20.0
    thread_counts = [1, 2, 4, 8]
    for num_threads in thread_counts:
        set_num_threads(num_threads)
        print(f"Running with {num_threads} threads...")
        for radius in [1, 3, 5, 7, 9]:
            start_time = time.time()
            create_blurred_image(image_path, output_path, radius, sigma, True)
            execution_time = time.time() - start_time
            print(f"    Execution time: {execution_time:.2f} seconds, radius {radius}")

def run_and_test_sobel_mpi_scalability(image_path, output_path):
    comm = MPI.COMM_WORLD
    rank = comm.Get_rank()
    size = comm.Get_size()

    # for i in range(5):
    if rank == 0:
        start_time = time.time()
        print(f"Running with {size} processes...")
    apply_sobel_filter_mpi(image_path, output_path)
    if rank == 0:
        end_time = time.time()
        execution_time = end_time - start_time
        print(f"    Execution time: {execution_time:.2f} seconds")

def run_and_test_sobel_serial(image_path, output_path):
    start_time = time.time()
    create_image_with_sharp_edges(image_path, output_path, False)
    execution_time = time.time() - start_time
    print(f"    Execution time: {execution_time:.2f} seconds")

def run_and_test_sobel_numba_parallel_scalability(image_path, output_path):
    thread_counts = [1, 2, 4, 8]
    for num_threads in thread_counts:
        set_num_threads(num_threads)
        print(f"Running with {num_threads} threads...")
        start_time = time.time()
        create_image_with_sharp_edges(image_path, output_path, True)
        execution_time = time.time() - start_time
        print(f"    Execution time: {execution_time:.2f} seconds")

if __name__ == "__main__":
    file_name = "squidward_painting"
    file_extension = "jpg"
    input_image_path = os.path.join(script_dir, '..', 'assets', f"{file_name}.{file_extension}")
    output_blurred_path = os.path.join(script_dir, 'outputs', f"{file_name}_blurred.{file_extension}")
    output_edges_path = os.path.join(script_dir, 'outputs', f"{file_name}_sobel.{file_extension}")

    radius = 9
    sigma = 20.0
    parallel = True

    # Serial
    # create_blurred_image(input_image_path, output_blurred_path, radius, sigma, parallel)
    # create_image_with_sharp_edges(input_image_path, output_edges_path, parallel)

    # Gaussian blur
    # run_and_test_blur_mpi_scalability(input_image_path, output_blurred_path)
    # run_and_test_blur_serial_scalability(input_image_path, output_blurred_path)
    run_and_test_blur_numba_parallel_scalability(input_image_path, output_blurred_path)
    
    # Sobel filter
    # run_and_test_sobel_mpi_scalability(input_image_path, output_edges_path)
    # run_and_test_sobel_serial(input_image_path, output_edges_path)
    # run_and_test_sobel_numba_parallel_scalability(input_image_path, output_edges_path)