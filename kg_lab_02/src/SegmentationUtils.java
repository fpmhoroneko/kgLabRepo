import java.awt.*;
import java.awt.image.BufferedImage;

public class SegmentationUtils {
    
    public static BufferedImage detectPoints(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        BufferedImage grayImage = toGrayscale(image);

        int[][] laplacianMask = {
            {-1, -1, -1},
            {-1,  8, -1},
            {-1, -1, -1}
        };
        
        Graphics g = result.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double response = 0;
                
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int pixel = getGrayValue(grayImage, x + j, y + i);
                        response += pixel * laplacianMask[i + 1][j + 1];
                    }
                }

                if (Math.abs(response) > threshold) {
                    result.setRGB(x, y, Color.RED.getRGB());
                }
            }
        }
        
        return result;
    }
    
    public static BufferedImage detectLines(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage grayImage = toGrayscale(image);
        
        int[][][] masks = {
            // Горизонтальная маска
            {{-1, -1, -1},
             { 2,  2,  2},
             {-1, -1, -1}},
            
            // Вертикальная маска  
            {{-1,  2, -1},
             {-1,  2, -1},
             {-1,  2, -1}},
            
            // Диагональная маска 45°
            {{-1, -1,  2},
             {-1,  2, -1},
             { 2, -1, -1}},
            
            // Диагональная маска 135°
            {{ 2, -1, -1},
             {-1,  2, -1},
             {-1, -1,  2}}
        };

         Graphics g = result.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        Color[] orientationColors = {Color.RED, Color.GREEN, Color.BLUE, Color.CYAN};
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double[] responses = new double[4];
                
                for (int maskIdx = 0; maskIdx < 4; maskIdx++) {
                    double response = 0;
                    
                    for (int ky = -1; ky <= 1; ky++) {
                        for (int kx = -1; kx <= 1; kx++) {
                            int pixel = getGrayValue(grayImage, x + kx, y + ky);
                            response += masks[maskIdx][ky+1][kx+1] * pixel;
                        }
                    }
                    
                    responses[maskIdx] = Math.abs(response);
                }
                

                int maxIndex = 0;
                double maxResponse = responses[0];
                for (int i = 1; i < 4; i++) {
                    if (responses[i] > maxResponse) {
                        maxResponse = responses[i];
                        maxIndex = i;
                    }
                }
                
                boolean isLinePoint = true;
                for (int i = 0; i < 4; i++) {
                    if (i != maxIndex && maxResponse <= responses[i]) {
                        isLinePoint = false;
                        break;
                    }
                }
                
                if (isLinePoint && maxResponse > threshold) {
                    result.setRGB(x, y, orientationColors[maxIndex].getRGB());
                }
            }
        }
        
        return result;
    }

    public static BufferedImage detectEdges(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        BufferedImage grayImage = toGrayscale(image);
        
        int[][] sobelX = {{-1, 0, 1}, 
                          {-2, 0, 2}, 
                          {-1, 0, 1}};

        int[][] sobelY = {{-1, -2, -1}, 
                          {0, 0, 0}, 
                          {1, 2, 1}};
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int gx = 0, gy = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = getGrayValue(grayImage, x + kx, y + ky);
                        gx += sobelX[ky+1][kx+1] * pixel;
                        gy += sobelY[ky+1][kx+1] * pixel;
                    }
                }

                int grad = (int) Math.sqrt(gx * gx + gy * gy);
                
                if (grad > threshold) {
                    result.setRGB(x, y, Color.YELLOW.getRGB());
                } else {
                    result.setRGB(x, y, image.getRGB(x, y));
                }
            }
        }
        
        return result;
    }

    private static BufferedImage toGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage gray = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        Graphics g = gray.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return gray;
    }
    
    private static int getGrayValue(BufferedImage grayImage, int x, int y) {
        if (x < 0 || x >= grayImage.getWidth() || y < 0 || y >= grayImage.getHeight()) {
            return 0;
        }
        return grayImage.getRGB(x, y) & 0xFF;
    }
}