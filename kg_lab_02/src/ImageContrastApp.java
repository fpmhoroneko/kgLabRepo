import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageContrastApp extends JFrame {
    private JLabel originalImageLabel;
    private JLabel resultImageLabel;
    private JLabel originalHistogramLabel;
    private JLabel resultHistogramLabel;
    private JLabel originalRedHistLabel, originalGreenHistLabel, originalBlueHistLabel, originalLuminanceHistLabel;
    private JLabel resultRedHistLabel, resultGreenHistLabel, resultBlueHistLabel, resultLuminanceHistLabel;
    private JTabbedPane tabbedPane;
    private BufferedImage originalImage;
    private JComboBox<String> methodComboBox;
    private JComboBox<String> colorSpaceComboBox;
    private JComboBox<String> segmentationComboBox;
    private JSlider thresholdSlider;
    private JPanel segmentationPanel;
    private ImageProcessor imageProcessor;
    
    public ImageContrastApp() {
        imageProcessor = new ImageProcessor();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Обработка изображений: Контрастирование и Сегментация");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Панель управления
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        segmentationPanel = createSegmentationControlsPanel();
        add(segmentationPanel, BorderLayout.SOUTH);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Общий вид", createMainPanel());
        tabbedPane.addTab("Подробные гистограммы", createDetailedHistogramsPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        setSize(1600, 2000);
        setLocationRelativeTo(null);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        JButton loadButton = new JButton("Загрузить изображение");
        loadButton.addActionListener(e -> loadImage());
        
        methodComboBox = new JComboBox<>(new String[]{
            "Линейное контрастирование", "Выравнивание гистограммы"
        });
        
        colorSpaceComboBox = new JComboBox<>(new String[]{
            "RGB", "HSV"
        });
        
        segmentationComboBox = new JComboBox<>(new String[]{
            "Без сегментации",
            "Обнаружение точек",
            "Обнаружение линий", 
            "Обнаружение перепадов яркости"
        });
        segmentationComboBox.addActionListener(e -> updateSegmentationControls());
        
        JButton processButton = new JButton("Обработать");
        processButton.addActionListener(e -> processImage());
        
        JButton resetButton = new JButton("Сброс");
        resetButton.addActionListener(e -> resetImages());
        
        panel.add(loadButton);
        panel.add(new JLabel("Метод контрастирования:"));
        panel.add(methodComboBox);
        panel.add(new JLabel("Цветовое пространство:"));
        panel.add(colorSpaceComboBox);
        panel.add(new JLabel("Сегментация:"));
        panel.add(segmentationComboBox);
        panel.add(processButton);
        panel.add(resetButton);
        
        return panel;
    }
    
    private JPanel createSegmentationControlsPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Параметры сегментации"));
        
        // Инициализация слайдеров
        thresholdSlider = new JSlider(0, 255, 50);
        thresholdSlider.setPreferredSize(new Dimension(150, 20));
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setPaintLabels(true);
        thresholdSlider.setMajorTickSpacing(50);
                
        // Добавляем компоненты в панель
        panel.add(new JLabel("Порог:"));
        panel.add(thresholdSlider);       
        return panel;
    }
    
    private void updateSegmentationControls() {
        String selectedSegmentation = (String) segmentationComboBox.getSelectedItem();
        
        if (selectedSegmentation.equals("Без сегментации")) {
            segmentationPanel.setVisible(false);
        } else {
            segmentationPanel.setVisible(true);
            
            // Показываем/скрываем соответствующие контролы
            boolean showThreshold = selectedSegmentation.equals("Обнаружение точек") ||
                                  selectedSegmentation.equals("Обнаружение перепадов яркости");
                                  selectedSegmentation.equals("Обнаружение линий");
            
            // Показываем/скрываем компоненты
            thresholdSlider.setVisible(showThreshold);
            thresholdSlider.getParent().getComponent(0).setVisible(showThreshold); // Label "Порог:"
        }
        
        revalidate();
        repaint();
    }
    
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Панель изображений
        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 10));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        originalImageLabel = createImageLabel("Исходное изображение");
        resultImageLabel = createImageLabel("Результат обработки");
        
        imagePanel.add(originalImageLabel);
        imagePanel.add(resultImageLabel);
        
        // Панель гистограмм
        JPanel histogramPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        histogramPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        originalHistogramLabel = createImageLabel("RGB + Яркость (исходное)");
        resultHistogramLabel = createImageLabel("RGB + Яркость (результат)");
        
        histogramPanel.add(originalHistogramLabel);
        histogramPanel.add(resultHistogramLabel);
        
        panel.add(imagePanel, BorderLayout.NORTH);
        panel.add(histogramPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createDetailedHistogramsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Панель для исходного изображения
        JPanel originalHistPanel = createChannelHistogramsPanel("Исходное изображение", true);
        // Панель для результата
        JPanel resultHistPanel = createChannelHistogramsPanel("Результат обработки", false);
        
        panel.add(originalHistPanel);
        panel.add(resultHistPanel);
        
        return panel;
    }
    
    private JPanel createChannelHistogramsPanel(String title, boolean isOriginal) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        JPanel channelsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        if (isOriginal) {
            originalRedHistLabel = createHistogramLabel("Red Channel");
            originalGreenHistLabel = createHistogramLabel("Green Channel");
            originalBlueHistLabel = createHistogramLabel("Blue Channel");
            originalLuminanceHistLabel = createHistogramLabel("Luminance");
            
            channelsPanel.add(originalRedHistLabel);
            channelsPanel.add(originalGreenHistLabel);
            channelsPanel.add(originalBlueHistLabel);
            channelsPanel.add(originalLuminanceHistLabel);
        } else {
            resultRedHistLabel = createHistogramLabel("Red Channel");
            resultGreenHistLabel = createHistogramLabel("Green Channel");
            resultBlueHistLabel = createHistogramLabel("Blue Channel");
            resultLuminanceHistLabel = createHistogramLabel("Luminance");
            
            channelsPanel.add(resultRedHistLabel);
            channelsPanel.add(resultGreenHistLabel);
            channelsPanel.add(resultBlueHistLabel);
            channelsPanel.add(resultLuminanceHistLabel);
        }
        
        panel.add(channelsPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JLabel createImageLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.TOP);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setPreferredSize(new Dimension(600, 400));
        return label;
    }
    
    private JLabel createHistogramLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.TOP);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        label.setPreferredSize(new Dimension(400, 300));
        return label;
    }
    
    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Images", "jpg", "jpeg", "png", "gif", "bmp"));
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                originalImage = ImageUtils.loadImage(fileChooser.getSelectedFile().getPath());
                displayOriginalImage();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки изображения: " + e.getMessage());
            }
        }
    }
    
    private void displayOriginalImage() {
        if (originalImage != null) {
            ImageIcon icon = new ImageIcon(ImageUtils.scaleImage(originalImage, 600, 400));
            originalImageLabel.setIcon(icon);

            BufferedImage combinedHist = HistogramUtils.createCombinedHistogramImage(originalImage);
            originalHistogramLabel.setIcon(new ImageIcon(combinedHist));
            
            displayChannelHistograms(originalImage, true);
        }
    }
    
    private void displayChannelHistograms(BufferedImage image, boolean isOriginal) {
        BufferedImage redHist = HistogramUtils.createRedHistogramImage(image);
        BufferedImage greenHist = HistogramUtils.createGreenHistogramImage(image);
        BufferedImage blueHist = HistogramUtils.createBlueHistogramImage(image);
        BufferedImage luminanceHist = HistogramUtils.createLuminanceHistogramImage(image);
        
        if (isOriginal) {
            if (originalRedHistLabel != null) originalRedHistLabel.setIcon(new ImageIcon(redHist));
            if (originalGreenHistLabel != null) originalGreenHistLabel.setIcon(new ImageIcon(greenHist));
            if (originalBlueHistLabel != null) originalBlueHistLabel.setIcon(new ImageIcon(blueHist));
            if (originalLuminanceHistLabel != null) originalLuminanceHistLabel.setIcon(new ImageIcon(luminanceHist));
        } else {
            if (resultRedHistLabel != null) resultRedHistLabel.setIcon(new ImageIcon(redHist));
            if (resultGreenHistLabel != null) resultGreenHistLabel.setIcon(new ImageIcon(greenHist));
            if (resultBlueHistLabel != null) resultBlueHistLabel.setIcon(new ImageIcon(blueHist));
            if (resultLuminanceHistLabel != null) resultLuminanceHistLabel.setIcon(new ImageIcon(luminanceHist));
        }
    }
    
    private void processImage() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Сначала загрузите изображение");
            return;
        }
        
        String method = (String) methodComboBox.getSelectedItem();
        String colorSpace = (String) colorSpaceComboBox.getSelectedItem();
        String segmentation = (String) segmentationComboBox.getSelectedItem();
        
        try {
            BufferedImage resultImage = imageProcessor.processImage(
                originalImage, method, colorSpace);
            
            if (!segmentation.equals("Без сегментации")) {
                resultImage = applySegmentation(resultImage, segmentation);
            }

            saveResultImage(resultImage);
            
            ImageIcon resultIcon = new ImageIcon(ImageUtils.scaleImage(resultImage, 600, 400));
            resultImageLabel.setIcon(resultIcon);
            
            BufferedImage resultCombinedHist = HistogramUtils.createCombinedHistogramImage(resultImage);
            resultHistogramLabel.setIcon(new ImageIcon(resultCombinedHist));
            
            displayChannelHistograms(resultImage, false);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка обработки изображения: " + e.getMessage());
            e.printStackTrace();
        }

    }
    
    private BufferedImage applySegmentation(BufferedImage image, String segmentationType) {
        switch (segmentationType) {
            case "Обнаружение точек":
                return SegmentationUtils.detectPoints(image, thresholdSlider.getValue());
                
            case "Обнаружение линий":
                return SegmentationUtils.detectLines(image, thresholdSlider.getValue());
                
            case "Обнаружение перепадов яркости":
                return SegmentationUtils.detectEdges(image, thresholdSlider.getValue());
                
            default:
                return image;
        }
    }
    
    private void resetImages() {
        originalImageLabel.setIcon(null);
        resultImageLabel.setIcon(null);
        originalHistogramLabel.setIcon(null);
        resultHistogramLabel.setIcon(null);
        
        if (originalRedHistLabel != null) originalRedHistLabel.setIcon(null);
        if (originalGreenHistLabel != null) originalGreenHistLabel.setIcon(null);
        if (originalBlueHistLabel != null) originalBlueHistLabel.setIcon(null);
        if (originalLuminanceHistLabel != null) originalLuminanceHistLabel.setIcon(null);
        
        if (resultRedHistLabel != null) resultRedHistLabel.setIcon(null);
        if (resultGreenHistLabel != null) resultGreenHistLabel.setIcon(null);
        if (resultBlueHistLabel != null) resultBlueHistLabel.setIcon(null);
        if (resultLuminanceHistLabel != null) resultLuminanceHistLabel.setIcon(null);
        
        originalImage = null;
    }

    private void saveResultImage(BufferedImage image) {
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить результат");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "PNG Images", "png"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "JPEG Images", "jpg", "jpeg"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "BMP Images", "bmp"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath() + ".png";
                fileToSave = new File(filePath);
                
                // Сохраняем изображение
                boolean success = ImageIO.write(image, "png", fileToSave);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Изображение успешно сохранено:\n" + filePath);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Ошибка: не удалось сохранить изображение в формате .png");
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Ошибка при сохранении изображения: " + e.getMessage());
            }
        }
    }
}