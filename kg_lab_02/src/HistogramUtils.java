import java.awt.*;
import java.awt.image.BufferedImage;

public class HistogramUtils {
    
    // Комбинированная RGB гистограмма + яркость
    public static BufferedImage createCombinedHistogramImage(BufferedImage image) {
        int width = 600;
        int height = 400;
        BufferedImage histImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = histImage.createGraphics();
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Белый фон
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // Вычисляем гистограммы для каждого канала и яркости
        int[] histRed = calculateChannelHistogram(image, 'R');
        int[] histGreen = calculateChannelHistogram(image, 'G');
        int[] histBlue = calculateChannelHistogram(image, 'B');
        int[] histLuminance = calculateLuminanceHistogram(image);
        
        // Находим максимум для нормализации
        int max = Math.max(Math.max(
            Math.max(findMax(histRed), findMax(histGreen)),
            Math.max(findMax(histBlue), findMax(histLuminance))
        ), 1);
        
        // Рисуем фон
        g.setColor(new Color(245, 245, 245));
        g.fillRect(50, 50, width - 80, height - 100);
        
        // Рисуем сетку
        drawGrid(g, 50, 50, width - 80, height - 100);
        
        // Рисуем все гистограммы на одном графике
        int graphWidth = width - 80;
        int graphHeight = height - 100;
        
        // Красный канал
        drawHistogramLine(g, histRed, max, 50, 50, graphWidth, graphHeight, Color.RED);
        // Зеленый канал
        drawHistogramLine(g, histGreen, max, 50, 50, graphWidth, graphHeight, Color.GREEN);
        // Синий канал
        drawHistogramLine(g, histBlue, max, 50, 50, graphWidth, graphHeight, Color.BLUE);
        // Яркость
        drawHistogramLine(g, histLuminance, max, 50, 50, graphWidth, graphHeight, Color.BLACK);
        
        // Рисуем оси и подписи
        drawAxes(g, width, height);
        
        // Рисуем легенду
        drawLegend(g, width);
        
        // Заголовок
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("RGB + Luminance Histogram", width / 2 - 100, 30);
        
        g.dispose();
        return histImage;
    }
    
    // Отдельные гистограммы для каждого канала
    public static BufferedImage createRedHistogramImage(BufferedImage image) {
        int[] histogram = calculateChannelHistogram(image, 'R');
        return createSingleChannelHistogram(histogram, "Red Channel", Color.RED);
    }
    
    public static BufferedImage createGreenHistogramImage(BufferedImage image) {
        int[] histogram = calculateChannelHistogram(image, 'G');
        return createSingleChannelHistogram(histogram, "Green Channel", Color.GREEN);
    }
    
    public static BufferedImage createBlueHistogramImage(BufferedImage image) {
        int[] histogram = calculateChannelHistogram(image, 'B');
        return createSingleChannelHistogram(histogram, "Blue Channel", Color.BLUE);
    }
    
    // Гистограмма яркости
    public static BufferedImage createLuminanceHistogramImage(BufferedImage image) {
        int[] histogram = calculateLuminanceHistogram(image);
        return createSingleChannelHistogram(histogram, "Luminance (Brightness)", Color.BLACK);
    }
    
    // Вычисляет гистограмму яркости (luminance)
    private static int[] calculateLuminanceHistogram(BufferedImage image) {
        int[] histogram = new int[256];
        int width = image.getWidth();
        int height = image.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Формула для вычисления яркости (luminance)
                // Стандартная формула: Y = 0.299*R + 0.587*G + 0.114*B
                int luminance = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                
                histogram[luminance]++;
            }
        }
        
        return histogram;
    }
    
    // Вычисляет гистограмму для указанного канала
    private static int[] calculateChannelHistogram(BufferedImage image, char channel) {
        int[] histogram = new int[256];
        int width = image.getWidth();
        int height = image.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int value = 0;
                
                switch (channel) {
                    case 'R': // Красный канал
                        value = (rgb >> 16) & 0xFF;
                        break;
                    case 'G': // Зеленый канал
                        value = (rgb >> 8) & 0xFF;
                        break;
                    case 'B': // Синий канал
                        value = rgb & 0xFF;
                        break;
                }
                
                histogram[value]++;
            }
        }
        
        return histogram;
    }
    
    // Создает изображение гистограммы для одного канала
    private static BufferedImage createSingleChannelHistogram(int[] histogram, String channelName, Color color) {
        int width = 400;
        int height = 300;
        BufferedImage histImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = histImage.createGraphics();
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Белый фон
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // Рисуем гистограмму
        drawSingleHistogram(g, histogram, 30, 40, width - 60, height - 80, color, channelName);
        
        // Статистика
        drawChannelStatistics(g, histogram, width, height, channelName);
        
        g.dispose();
        return histImage;
    }
    
    private static void drawHistogramLine(Graphics2D g, int[] histogram, int max, int x, int y, 
                                        int width, int height, Color color) {
        g.setColor(color);
        
        for (int i = 0; i < 255; i++) {
            int x1 = x + i * width / 256;
            int x2 = x + (i + 1) * width / 256;
            
            int value1 = (int) (histogram[i] * height / (double) max);
            int value2 = (int) (histogram[i + 1] * height / (double) max);
            
            g.drawLine(x1, y + height - value1, x2, y + height - value2);
        }
    }
    
    private static void drawSingleHistogram(Graphics2D g, int[] histogram, int x, int y, 
                                          int width, int height, Color color, String title) {
        int max = findMax(histogram);
        if (max == 0) max = 1;
        
        // Фон
        g.setColor(new Color(245, 245, 245));
        g.fillRect(x, y, width, height);
        
        // Сетка
        drawGrid(g, x, y, width, height);
        
        // Столбцы
        g.setColor(color);
        int barWidth = Math.max(1, width / 256);
        
        for (int i = 0; i < 256; i++) {
            int barHeight = (int) (histogram[i] * height / (double) max);
            int barX = x + i * barWidth;
            int barY = y + height - barHeight;
            
            g.fillRect(barX, barY, barWidth, barHeight);
        }
        
        // Контур
        g.setColor(Color.GRAY);
        g.drawRect(x, y, width, height);
        
        // Заголовок
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString(title, x + 10, y - 10);
        
        // Оси
        drawSingleHistogramAxes(g, x, y, width, height);
    }
    
    private static void drawGrid(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(new Color(220, 220, 220));
        
        // Вертикальные линии
        for (int i = 1; i < 8; i++) {
            int lineX = x + i * width / 8;
            g.drawLine(lineX, y, lineX, y + height);
        }
        
        // Горизонтальные линии
        for (int i = 1; i < 4; i++) {
            int lineY = y + i * height / 4;
            g.drawLine(x, lineY, x + width, lineY);
        }
    }
    
    private static void drawAxes(Graphics2D g, int width, int height) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        
        // Ось X
        g.drawLine(50, height - 50, width - 30, height - 50);
        
        // Ось Y
        g.drawLine(50, 50, 50, height - 50);
        
        // Подписи оси X
        for (int i = 0; i <= 255; i += 32) {
            int x = 50 + i * (width - 80) / 256;
            g.drawLine(x, height - 50, x, height - 45);
            g.drawString(String.valueOf(i), x - 5, height - 35);
        }
        
        // Подписи оси Y
        for (int i = 0; i <= 4; i++) {
            int y = height - 50 - (i * (height - 100) / 4);
            g.drawLine(45, y, 50, y);
            int percentage = i * 25;
            g.drawString(percentage + "%", 25, y + 5);
        }
    }
    
    private static void drawSingleHistogramAxes(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        
        // Подписи оси X
        for (int i = 0; i <= 255; i += 32) {
            int labelX = x + i * width / 256;
            g.drawLine(labelX, y + height, labelX, y + height + 5);
            g.drawString(String.valueOf(i), labelX - 5, y + height + 15);
        }
        
        // Подписи оси Y
        for (int i = 0; i <= 4; i++) {
            int labelY = y + height - (i * height / 4);
            g.drawLine(x - 5, labelY, x, labelY);
            int percentage = i * 25;
            g.drawString(percentage + "%", x - 25, labelY + 5);
        }
    }
    
    private static void drawLegend(Graphics2D g, int width) {
        int legendX = width - 150;
        int legendY = 70;
        
        g.setColor(new Color(255, 255, 255, 200));
        g.fillRect(legendX, legendY, 130, 100);
        g.setColor(Color.GRAY);
        g.drawRect(legendX, legendY, 130, 100);
        
        g.setFont(new Font("Arial", Font.BOLD, 12));
        
        g.setColor(Color.RED);
        g.drawString("___", legendX + 10, legendY + 20);
        g.setColor(Color.BLACK);
        g.drawString("Red", legendX + 40, legendY + 20);
        
        g.setColor(Color.GREEN);
        g.drawString("___", legendX + 10, legendY + 35);
        g.setColor(Color.BLACK);
        g.drawString("Green", legendX + 40, legendY + 35);
        
        g.setColor(Color.BLUE);
        g.drawString("___", legendX + 10, legendY + 50);
        g.setColor(Color.BLACK);
        g.drawString("Blue", legendX + 40, legendY + 50);
        
        g.setColor(Color.BLACK);
        g.drawString("___", legendX + 10, legendY + 65);
        g.drawString("Luminance", legendX + 40, legendY + 65);
    }
    
    private static void drawChannelStatistics(Graphics2D g, int[] histogram, int width, int height, String channelName) {
        double mean = calculateMean(histogram);
        double std = calculateStd(histogram, mean);
        int mode = findMode(histogram);
        int median = findMedian(histogram);
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        
        int startY = height - 60;
        g.drawString(channelName + " Statistics:", 20, startY);
        g.drawString(String.format("Mean: %.2f", mean), 40, startY + 15);
        g.drawString(String.format("Std Dev: %.2f", std), 40, startY + 30);
        g.drawString("Mode: " + mode, 40, startY + 45);
        g.drawString("Median: " + median, 40, startY + 60);
    }
    
    private static int findMax(int[] histogram) {
        int max = 0;
        for (int value : histogram) {
            if (value > max) max = value;
        }
        return max;
    }
    
    private static int findMode(int[] histogram) {
        int mode = 0;
        int maxCount = 0;
        for (int i = 0; i < histogram.length; i++) {
            if (histogram[i] > maxCount) {
                maxCount = histogram[i];
                mode = i;
            }
        }
        return mode;
    }
    
    private static int findMedian(int[] histogram) {
        long total = 0;
        for (int value : histogram) {
            total += value;
        }
        
        long halfTotal = total / 2;
        long runningTotal = 0;
        
        for (int i = 0; i < histogram.length; i++) {
            runningTotal += histogram[i];
            if (runningTotal >= halfTotal) {
                return i;
            }
        }
        return 127;
    }
    
    public static String getImageStatistics(BufferedImage image) {
        int[] histRed = calculateChannelHistogram(image, 'R');
        int[] histGreen = calculateChannelHistogram(image, 'G');
        int[] histBlue = calculateChannelHistogram(image, 'B');
        int[] histLuminance = calculateLuminanceHistogram(image);
        
        double meanRed = calculateMean(histRed);
        double meanGreen = calculateMean(histGreen);
        double meanBlue = calculateMean(histBlue);
        double meanLuminance = calculateMean(histLuminance);
        
        double stdRed = calculateStd(histRed, meanRed);
        double stdGreen = calculateStd(histGreen, meanGreen);
        double stdBlue = calculateStd(histBlue, meanBlue);
        double stdLuminance = calculateStd(histLuminance, meanLuminance);
        
        return String.format(
            "RGB + Luminance Statistics:\n" +
            "Red:       μ=%.1f σ=%.1f\n" +
            "Green:     μ=%.1f σ=%.1f\n" +
            "Blue:      μ=%.1f σ=%.1f\n" +
            "Luminance: μ=%.1f σ=%.1f",
            meanRed, stdRed, meanGreen, stdGreen, meanBlue, stdBlue, meanLuminance, stdLuminance
        );
    }
    
    private static double calculateMean(int[] histogram) {
        long sum = 0;
        long count = 0;
        for (int i = 0; i < histogram.length; i++) {
            sum += i * histogram[i];
            count += histogram[i];
        }
        return count > 0 ? (double) sum / count : 0;
    }
    
    private static double calculateStd(int[] histogram, double mean) {
        double sumSq = 0;
        long count = 0;
        for (int i = 0; i < histogram.length; i++) {
            double diff = i - mean;
            sumSq += diff * diff * histogram[i];
            count += histogram[i];
        }
        return count > 0 ? Math.sqrt(sumSq / count) : 0;
    }
}