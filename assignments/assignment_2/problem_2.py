from PIL import Image
import numpy as np
from math import exp, sqrt
import os

# Adjusted for script directory usage
script_dir = os.path.dirname(os.path.realpath(__file__))

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

def apply_gaussian_blur_kernel(image, kernel, radius):
    img_array = np.asarray(image)
    height, width = img_array.shape[:2]
    padded_img = np.pad(img_array, ((radius, radius), (radius, radius), (0, 0)), 'edge')
    blurred_img = np.zeros_like(img_array)

    for y in range(height):
        for x in range(width):
            for c in range(3):  # For each color channel
                acc = np.sum(kernel * padded_img[y:y+2*radius+1, x:x+2*radius+1, c])
                blurred_img[y, x, c] = min(max(int(acc), 0), 255)

    return Image.fromarray(blurred_img)

def apply_sobel_filter(image):
    img_array = np.asarray(image.convert('L'))  # Convert to grayscale
    sobel_x = np.array([[-1, 0, 1], [-2, 0, 2], [-1, 0, 1]])
    sobel_y = np.array([[-1, -2, -1], [0, 0, 0], [1, 2, 1]])

    height, width = img_array.shape
    edges_img = np.zeros_like(img_array)

    for y in range(1, height-1):
        for x in range(1, width-1):
            gx = np.sum(sobel_x * img_array[y-1:y+2, x-1:x+2])
            gy = np.sum(sobel_y * img_array[y-1:y+2, x-1:x+2])
            edges_img[y, x] = min(sqrt(gx**2 + gy**2), 255)

    return Image.fromarray(edges_img).convert('RGB')

def create_blurred_image(image_path, output_path, radius, sigma):
    input_image = read_image(image_path)
    if input_image is None:
        print("Error: The input image could not be read")
        return
    kernel = generate_gaussian_blur_kernel(radius, sigma)
    blurred_image = apply_gaussian_blur_kernel(input_image, kernel, radius)
    write_image(blurred_image, output_path)

def create_image_with_sharp_edges(image_path, output_path):
    input_image = read_image(image_path)
    if input_image is None:
        print("Error: The input image could not be read")
        return
    image_with_sharp_edges = apply_sobel_filter(input_image)
    write_image(image_with_sharp_edges, output_path)


if __name__ == "__main__":
    file_name = "squidward_painting"
    file_extension = "jpg"
    input_image_path = os.path.join(script_dir, '..', 'assets', f"{file_name}.{file_extension}")
    output_blurred_path = os.path.join(script_dir, 'outputs', f"{file_name}_blurred.{file_extension}")
    output_edges_path = os.path.join(script_dir, 'outputs', f"{file_name}_sobel.{file_extension}")

    # Ensure the outputs directory exists
    create_outputs_directory_if_needed()

    create_blurred_image(input_image_path, output_blurred_path, 20, 20.0)
    create_image_with_sharp_edges(input_image_path, output_edges_path)
