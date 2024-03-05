import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import javax.imageio.ImageIO;

public class problem_2 {

    public static void main(String[] args) {
        String fileName = "squidward_painting";
        String fileExtension = "jpg";
        blurAndSaveImage(fileName, fileExtension);
    }

    public static void blurAndSaveImage(String fileName, String fileExtension) {
        String inputImagePath = "../assets/" + fileName + "." + fileExtension;
        String outputImagePath = "outputs/" + fileName + "_blurred." + fileExtension;
        BufferedImage inputImage = ImageTools.readImage(inputImagePath);
        if (inputImage == null) {
            System.out.println("Error: The input image could not be read");
            return;
        }
        // BufferedImage blurredImage = gaussianBlurImage(inputImage);
        BufferedImage blurredImage = gaussianBlurImageParallel(inputImage);
        ImageTools.writeImage(blurredImage, outputImagePath);
    }

    public static double[][] generateGaussianKernel(int radius, double sigma) {
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

    public static BufferedImage gaussianBlurImage(BufferedImage inputImage) {
        int radius = 30;
        double sigma = 1.5;
        double[][] kernel = generateGaussianKernel(radius, sigma);

        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] rgb = applyKernel(inputImage, x, y, kernel, radius);
                outputImage.setRGB(x, y, (rgb[0] << 16) | (rgb[1] << 8) | rgb[2]);
            }
        }

        return outputImage;
    }

    public static int[] applyKernel(BufferedImage image, int x, int y, double[][] kernel, int radius) {
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

    public static BufferedImage gaussianBlurImageParallel(BufferedImage inputImage) {
        int cores = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(cores);

        int radius = 20;
        double sigma = 20;
        double[][] kernel = generateGaussianKernel(radius, sigma);

        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int chunkWidth = width / cores;
        ArrayList<GaussianBlurTask> tasks = new ArrayList<>();

        for (int i = 0; i < cores; i++) {
            int startWidth = i * chunkWidth;
            int endWidth = (i == cores - 1) ? width : (i + 1) * chunkWidth;
            GaussianBlurTask task = new GaussianBlurTask(inputImage, startWidth, endWidth, kernel, radius);
            tasks.add(task);
        }

        // Invoke all tasks
        for (GaussianBlurTask task : tasks) {
            pool.execute(task);
        }

        // Wait for all tasks to complete and combine the results
        for (GaussianBlurTask task : tasks) {
            try {
                BufferedImage part = task.get();
                outputImage.getGraphics().drawImage(part, task.startWidth, 0, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        pool.shutdown();

        return outputImage;
    }

}

class GaussianBlurTask extends RecursiveTask<BufferedImage> {
    private final BufferedImage sourceImage;
    final int startWidth;
    private final int endWidth;
    private final double[][] kernel;
    private final int radius;

    GaussianBlurTask(BufferedImage sourceImage, int startWidth, int endWidth, double[][] kernel, int radius) {
        this.sourceImage = sourceImage;
        this.startWidth = startWidth;
        this.endWidth = endWidth;
        this.kernel = kernel;
        this.radius = radius;
    }

    @Override
    protected BufferedImage compute() {
        int width = endWidth - startWidth;
        int height = sourceImage.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = startWidth; x < endWidth; x++) {
                int[] rgb = problem_2.applyKernel(sourceImage, x, y, kernel, radius);
                result.setRGB(x - startWidth, y, (rgb[0] << 16) | (rgb[1] << 8) | rgb[2]);
            }
        }

        return result;
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
}