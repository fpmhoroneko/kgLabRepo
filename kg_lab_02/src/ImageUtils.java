import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {
    
    public static BufferedImage loadImage(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Файл не существует: " + filePath);
        }
        return ImageIO.read(file);
    }
    
    public static Image scaleImage(BufferedImage image, int maxWidth, int maxHeight) {
        double scale = Math.min(
            maxWidth / (double) image.getWidth(),
            maxHeight / (double) image.getHeight()
        );
        
        int newWidth = (int) (image.getWidth() * scale);
        int newHeight = (int) (image.getHeight() * scale);
        
        return image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }
    
    public static void saveImage(BufferedImage image, String filePath) throws IOException {
        String format = filePath.substring(filePath.lastIndexOf('.') + 1).toUpperCase();
        ImageIO.write(image, format, new File(filePath));
    }
}