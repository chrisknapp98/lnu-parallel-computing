import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import javax.imageio.ImageIO;

public class problem_2 {

    public static void main(String[] args) {
        String fileName = "squidward_painting";
        String fileExtension = "jpg";
        // createBlurredImage(fileName, fileExtension);
        createImageWithSharpEdges(fileName, fileExtension);
    }

    public static void createBlurredImage(String fileName, String fileExtension) {
        String inputImagePath = "../assets/" + fileName + "." + fileExtension;
        String outputImagePath = "outputs/" + fileName + "_blurred." + fileExtension;
        BufferedImage inputImage = ImageTools.readImage(inputImagePath);
        if (inputImage == null) {
            System.out.println("Error: The input image could not be read");
            return;
        }
        int radius = 20;
        double sigma = 20.0;
        // BufferedImage blurredImage = gaussianBlurImage(inputImage, radius, sigma);
        BufferedImage blurredImage = applyFilterInParallel(inputImage, FilterType.GAUSSIAN_BLUR, radius, sigma);
        ImageTools.writeImage(blurredImage, outputImagePath);
    }

    public static void createImageWithSharpEdges(String fileName, String fileExtension) {
        String inputImagePath = "../assets/" + fileName + "." + fileExtension;
        String outputImagePath = "outputs/" + fileName + "_sobel." + fileExtension;
        BufferedImage inputImage = ImageTools.readImage(inputImagePath);
        if (inputImage == null) {
            System.out.println("Error: The input image could not be read");
            return;
        }
        // BufferedImage imageWithSharpEdges = applySobelFilter(inputImage);
        BufferedImage imageWithSharpEdges = applyFilterInParallel(inputImage,
                FilterType.SOBEL_EDGE_DETECTION, 0, 0);
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
            double sigma) {
        double[][] kernel = null;
        if (filterType == FilterType.GAUSSIAN_BLUR) {
            kernel = ImageTools.generateGaussianBlurKernel(radius, sigma);
        }

        int cores = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(cores);
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