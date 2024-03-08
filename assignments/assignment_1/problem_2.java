import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import javax.imageio.ImageIO;

public class problem_2 {

    public static void main(String[] args) {
        final String fileName = "squidward_painting";
        final String fileExtension = "jpg";

        createBlurredImageSerialWithPresetParams(fileName, fileExtension);

        // createImageWithSharpEdgesSerial(fileName, fileExtension);

        // runAndTestScalability(fileName, fileExtension);
    }

    public static void createBlurredImageSerialWithPresetParams(String fileName, String fileExtension) {
        final int radius = 20;
        final double sigma = 20.0;
        createBlurredImage(fileName, fileExtension, radius, sigma, null);
    }

    public static void createImageWithSharpEdgesSerial(String fileName, String fileExtension) {
        createImageWithSharpEdges(fileName, fileExtension, null);
    }

    public static void runAndTestScalability(String fileName, String fileExtension) {
        final int availableCores = Runtime.getRuntime().availableProcessors();
        List<Integer> threadCounts = new ArrayList<>();

        for (int i = 1; i <= availableCores; i *= 2) {
            threadCounts.add(i);
        }

        testBlurScalability(fileName, fileExtension, threadCounts);

        testEdgeDetectionScalability(fileName, fileExtension, threadCounts);
    }

    private static void testBlurScalability(
            String fileName,
            String fileExtension,
            List<Integer> threadCounts) {
        int[] radii = { 1, 5, 7, 10, 20 };
        double sigma = 20.0;
        for (int threadCount : threadCounts) {
            System.out.println("Testing parallel gaussian blur with " + threadCount + " threads...");
            for (int radius : radii) {
                ForkJoinPool pool = new ForkJoinPool(threadCount);
                long startTime = System.currentTimeMillis();
                createBlurredImage(fileName, fileExtension, radius, sigma, Optional.of(pool));
                long endTime = System.currentTimeMillis();
                String executionTime = String.format("%.2f", (endTime - startTime) / 1000.0);
                System.out.println(
                        "   Radius: " + radius + ", Execution time: " + executionTime + " seconds");
                pool.shutdown();
            }
        }
    }

    private static void testEdgeDetectionScalability(
            String fileName,
            String fileExtension,
            List<Integer> threadCounts) {
        for (int threadCount : threadCounts) {
            System.out.println("Testing parallel sobel edge detection with " + threadCount + " threads...");
            ForkJoinPool pool = new ForkJoinPool(threadCount);
            long startTime = System.currentTimeMillis();
            createImageWithSharpEdges(fileName, fileExtension, Optional.of(pool));
            long endTime = System.currentTimeMillis();
            String executionTime = String.format("%.2f", (endTime - startTime) / 1000.0);
            System.out.println(
                    "   Execution time: " + executionTime + " seconds");
            pool.shutdown();
        }
    }

    public static void createBlurredImage(
            String fileName,
            String fileExtension,
            int radius,
            double sigma,
            Optional<ForkJoinPool> pool) {
        String inputImagePath = "../assets/" + fileName + "." + fileExtension;
        String outputImagePath = "outputs/" + fileName + "_blurred." + fileExtension;
        BufferedImage inputImage = ImageTools.readImage(inputImagePath);
        if (inputImage == null) {
            System.out.println("Error: The input image could not be read");
            return;
        }
        BufferedImage blurredImage;
        if (pool == null || !pool.isPresent()) {
            blurredImage = gaussianBlurImage(inputImage, radius, sigma);
        } else {
            blurredImage = applyFilterInParallel(inputImage, FilterType.GAUSSIAN_BLUR, radius, sigma, pool.get());
        }
        ImageTools.writeImage(blurredImage, outputImagePath);
    }

    public static void createImageWithSharpEdges(
            String fileName,
            String fileExtension,
            Optional<ForkJoinPool> pool) {
        String inputImagePath = "../assets/" + fileName + "." + fileExtension;
        String outputImagePath = "outputs/" + fileName + "_sobel." + fileExtension;
        BufferedImage inputImage = ImageTools.readImage(inputImagePath);
        if (inputImage == null) {
            System.out.println("Error: The input image could not be read");
            return;
        }
        BufferedImage imageWithSharpEdges;
        if (pool == null || !pool.isPresent()) {
            imageWithSharpEdges = applySobelFilter(inputImage);
        } else {
            imageWithSharpEdges = applyFilterInParallel(inputImage,
                    FilterType.SOBEL_EDGE_DETECTION, 0, 0, pool.get());
        }
        ImageTools.writeImage(imageWithSharpEdges, outputImagePath);
    }

    public static BufferedImage applySobelFilter(BufferedImage inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] rgb = ImageTools.applySobelKernel(inputImage, x, y);
                ImageTools.setRGBValues(outputImage, x, y, rgb);
            }
        }

        return outputImage;
    }

    public static BufferedImage gaussianBlurImage(BufferedImage inputImage, int radius, double sigma) {
        double[][] kernel = ImageTools.generateGaussianBlurKernel(radius, sigma);

        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] rgb = ImageTools.applyGaussianBlurKernel(inputImage, x, y, kernel, radius);
                ImageTools.setRGBValues(outputImage, x, y, rgb);
            }
        }

        return outputImage;
    }

    public static BufferedImage applyFilterInParallel(
            BufferedImage inputImage,
            FilterType filterType,
            int radius,
            double sigma,
            ForkJoinPool pool) {
        double[][] kernel = null;
        if (filterType == FilterType.GAUSSIAN_BLUR) {
            kernel = ImageTools.generateGaussianBlurKernel(radius, sigma);
        }

        BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        List<ImageProcessingTask> tasks = createTasks(inputImage, outputImage, filterType, kernel, radius);

        for (ImageProcessingTask task : tasks) {
            pool.execute(task);
        }

        for (ImageProcessingTask task : tasks) {
            try {
                task.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        pool.shutdown();
        return outputImage;
    }

    private static List<ImageProcessingTask> createTasks(
            BufferedImage inputImage,
            BufferedImage outputImage,
            FilterType filterType,
            double[][] kernel,
            int radius) {
        int cores = Runtime.getRuntime().availableProcessors();
        int width = inputImage.getWidth();
        int chunkWidth = width / cores;
        List<ImageProcessingTask> tasks = new ArrayList<>();

        for (int i = 0; i < cores; i++) {
            int startWidth = i * chunkWidth;
            int endWidth = (i < cores - 1) ? (i + 1) * chunkWidth : width;

            ImageProcessingTask task = new ImageProcessingTask(
                    inputImage,
                    outputImage,
                    startWidth,
                    endWidth,
                    filterType,
                    kernel,
                    radius);
            tasks.add(task);
        }

        return tasks;
    }

}

enum FilterType {
    GAUSSIAN_BLUR,
    SOBEL_EDGE_DETECTION
}

class ImageProcessingTask extends RecursiveTask<Void> {
    private final BufferedImage sourceImage;
    private final BufferedImage outputImage;
    private final int startWidth, endWidth;
    private final FilterType filterType;
    private final double[][] kernel;
    private final int radius; // Relevant for Gaussian blur

    ImageProcessingTask(BufferedImage sourceImage, BufferedImage outputImage, int startWidth, int endWidth,
            FilterType filterType, double[][] kernel, int radius) {
        this.sourceImage = sourceImage;
        this.outputImage = outputImage;
        this.startWidth = startWidth;
        this.endWidth = endWidth;
        this.filterType = filterType;
        this.kernel = kernel;
        this.radius = radius;
    }

    @Override
    protected Void compute() {
        switch (filterType) {
            case GAUSSIAN_BLUR:
                applyGaussianBlur();
                break;
            case SOBEL_EDGE_DETECTION:
                applySobelEdgeDetection();
                break;
        }
        return null;
    }

    private void applyGaussianBlur() {
        for (int y = 0; y < sourceImage.getHeight(); y++) {
            for (int x = startWidth; x < endWidth; x++) {
                int[] rgb = ImageTools.applyGaussianBlurKernel(sourceImage, x, y, kernel, radius);
                synchronized (outputImage) {
                    ImageTools.setRGBValues(outputImage, x, y, rgb);
                }
            }
        }
    }

    private void applySobelEdgeDetection() {
        for (int y = 0; y < sourceImage.getHeight(); y++) {
            for (int x = startWidth; x < endWidth; x++) {
                int[] rgb = ImageTools.applySobelKernel(sourceImage, x, y);
                synchronized (outputImage) {
                    ImageTools.setRGBValues(outputImage, x, y, rgb);
                }
            }
        }
    }
}

class ImageTools {
    public static BufferedImage readImage(String path) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return image;
    }

    public static void writeImage(BufferedImage image, String path) {
        createOutputsDirectoryIfNeeded();
        try {
            File file = new File(path);
            String fileExtension = path.substring(path.lastIndexOf(".") + 1);
            ImageIO.write(image, fileExtension, file);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void createOutputsDirectoryIfNeeded() {
        File outputsDirectory = new File("outputs");
        if (!outputsDirectory.exists()) {
            outputsDirectory.mkdir();
        }
    }

    public static double[][] generateGaussianBlurKernel(int radius, double sigma) {
        int size = 2 * radius + 1;
        double[][] kernel = new double[size][size];
        double sum = 0.0;

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                kernel[i + radius][j + radius] = Math.exp(-(i * i + j * j) / (2 * sigma * sigma));
                sum += kernel[i + radius][j + radius];
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                kernel[i][j] /= sum;
            }
        }

        return kernel;
    }

    public static int[] applyGaussianBlurKernel(BufferedImage image, int x, int y, double[][] kernel, int radius) {
        int width = image.getWidth();
        int height = image.getHeight();
        double sumR = 0, sumG = 0, sumB = 0;

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int newX = x + i;
                int newY = y + j;

                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    int pixel = image.getRGB(newX, newY);
                    double weight = kernel[i + radius][j + radius];

                    sumR += ((pixel >> 16) & 0xFF) * weight;
                    sumG += ((pixel >> 8) & 0xFF) * weight;
                    sumB += (pixel & 0xFF) * weight;
                }
            }
        }

        return new int[] { (int) sumR, (int) sumG, (int) sumB };
    }

    public static void setRGBValues(BufferedImage image, int x, int y, int[] rgb) {
        image.setRGB(x, y, (rgb[0] << 16) | (rgb[1] << 8) | rgb[2]);
    }

    private static final int[][] SOBEL_HORIZONTAL = {
            { -1, 0, 1 },
            { -2, 0, 2 },
            { -1, 0, 1 }
    };

    private static final int[][] SOBEL_VERTICAL = {
            { -1, -2, -1 },
            { 0, 0, 0 },
            { 1, 2, 1 }
    };

    public static int[] applySobelKernel(BufferedImage image, int x, int y) {
        int gx = 0, gy = 0;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (x + i >= 0 && x + i < image.getWidth() && y + j >= 0 && y + j < image.getHeight()) {
                    int pixel = image.getRGB(x + i, y + j);
                    int intensity = (pixel >> 16) & 0xFF;

                    gx += intensity * SOBEL_HORIZONTAL[i + 1][j + 1];
                    gy += intensity * SOBEL_VERTICAL[i + 1][j + 1];
                }
            }
        }

        int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
        magnitude = Math.min(255, magnitude);

        return new int[] { magnitude, magnitude, magnitude };
    }

}