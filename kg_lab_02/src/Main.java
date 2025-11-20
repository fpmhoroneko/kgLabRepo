import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ImageContrastApp app = new ImageContrastApp();
            app.setVisible(true);
        });
    }
}