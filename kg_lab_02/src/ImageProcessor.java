import java.awt.image.BufferedImage;

public class ImageProcessor {
    
    public BufferedImage processImage(BufferedImage image, String method, String colorSpace) {
        if (method.equals("Линейное контрастирование")) {
            return applyLinearContrast(image);
        } else {
            switch (colorSpace) {
                case "RGB":
                    return applyHistogramEqualizationRGB(image);
                case "HSV":
                    return applyHistogramEqualizationHSV(image);
                default:
                    return image;
            }
        }
    }
    
    public BufferedImage applyLinearContrast(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        int minRed = 255, maxRed = 0;
        int minGreen = 255, maxGreen = 0;
        int minBlue = 255, maxBlue = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                minRed = Math.min(minRed, r);
                maxRed = Math.max(maxRed, r);
                minGreen = Math.min(minGreen, g);
                maxGreen = Math.max(maxGreen, g);
                minBlue = Math.min(minBlue, b);
                maxBlue = Math.max(maxBlue, b);
            }
        }
        
        if (maxRed == minRed) maxRed = minRed + 1;
        if (maxGreen == minGreen) maxGreen = minGreen + 1;
        if (maxBlue == minBlue) maxBlue = minBlue + 1;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                int newR = (int) ((r - minRed) * 255.0 / (maxRed - minRed));
                int newG = (int) ((g - minGreen) * 255.0 / (maxGreen - minGreen));
                int newB = (int) ((b - minBlue) * 255.0 / (maxBlue - minBlue));
                
                newR = Math.max(0, Math.min(255, newR));
                newG = Math.max(0, Math.min(255, newG));
                newB = Math.max(0, Math.min(255, newB));
                
                int newRGB = (newR << 16) | (newG << 8) | newB;
                result.setRGB(x, y, newRGB);
            }
        }
        
        return result;
    }
    
    public BufferedImage applyHistogramEqualizationRGB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        int[] histRed = new int[256];
        int[] histGreen = new int[256];
        int[] histBlue = new int[256];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                histRed[(rgb >> 16) & 0xFF]++;
                histGreen[(rgb >> 8) & 0xFF]++;
                histBlue[rgb & 0xFF]++;
            }
        }
        
        int totalPixels = width * height;
        int[] cumRed = calculateCumulativeHistogram(histRed, totalPixels);
        int[] cumGreen = calculateCumulativeHistogram(histGreen, totalPixels);
        int[] cumBlue = calculateCumulativeHistogram(histBlue, totalPixels);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                int newR = cumRed[r];
                int newG = cumGreen[g];
                int newB = cumBlue[b];
                
                int newRGB = (newR << 16) | (newG << 8) | newB;
                result.setRGB(x, y, newRGB);
            }
        }
        
        return result;
    }
    
    public BufferedImage applyHistogramEqualizationHSV(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        float[] values = new float[width * height];
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                float[] hsv = ColorSpaceConverter.rgbToHSV(rgb);
                values[index++] = hsv[2];
            }
        }
        
        float[] equalizedValues = equalizeFloatHistogram(values);
        
        index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                float[] hsv = ColorSpaceConverter.rgbToHSV(rgb);
                hsv[2] = equalizedValues[index++];
                int newRGB = ColorSpaceConverter.hsvToRGB(hsv);
                result.setRGB(x, y, newRGB);
            }
        }
        
        return result;
    }
    
    private int[] calculateCumulativeHistogram(int[] histogram, int totalPixels) {
        int[] cumulative = new int[256];
        cumulative[0] = histogram[0];
        
        for (int i = 1; i < 256; i++) {
            cumulative[i] = cumulative[i-1] + histogram[i];
        }
        
        for (int i = 0; i < 256; i++) {
            cumulative[i] = (int) (cumulative[i] * 255.0 / totalPixels);
        }
        
        return cumulative;
    }
    
    private float[] equalizeFloatHistogram(float[] values) {
        int[] histogram = new int[256];
        
        // Строим гистограмму
        for (float value : values) {
            int bin = (int) (value * 255);
            if (bin >= 0 && bin < 256) {
                histogram[bin]++;
            }
        }
        
        // Вычисляем кумулятивную гистограмму
        int[] cumulative = new int[256];
        cumulative[0] = histogram[0];
        for (int i = 1; i < 256; i++) {
            cumulative[i] = cumulative[i-1] + histogram[i];
        }
        
        // Нормализуем
        float[] equalized = new float[values.length];
        int total = values.length;
        
        for (int i = 0; i < values.length; i++) {
            int bin = (int) (values[i] * 255);
            if (bin >= 0 && bin < 256) {
                equalized[i] = cumulative[bin] / (float) total;
            } else {
                equalized[i] = values[i];
            }
        }
        
        return equalized;
    }
}