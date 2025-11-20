import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class RasterGraphics extends JFrame {
    private CanvasPanel canvasPanel;
    private JComboBox<String> algorithmSelect;
    private JLabel statusLabel;
    private JLabel timeLabel;
    private JSlider gridSizeSlider;
    private JButton drawButton;
    private JTextArea logArea;
    private JCheckBox showDetailsCheckBox;

    private final int CANVAS_SIZE = 500;

    public RasterGraphics() {
        setTitle("Растровые алгоритмы");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initializeComponents();
        setupLayout();

        pack();
        setLocationRelativeTo(null);
        setSize(800, 700);
        setVisible(true);
    }

    private void initializeComponents() {
        String[] algorithms = {"Пошаговый", "ЦДА", "Брезенхем (линии)", "Брезенхем (окружность)"};
        algorithmSelect = new JComboBox<>(algorithms);

        gridSizeSlider = new JSlider(10, 50, 20);
        gridSizeSlider.setMajorTickSpacing(10);
        gridSizeSlider.setMinorTickSpacing(5);
        gridSizeSlider.setPaintTicks(true);
        gridSizeSlider.setPaintLabels(true);

        drawButton = new JButton("Нарисовать");

        statusLabel = new JLabel("Нажмите на полотно, чтобы выбрать начальную и конечную точки.");
        timeLabel = new JLabel("Время выполнения: -");

        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        canvasPanel = new CanvasPanel(CANVAS_SIZE, gridSizeSlider, statusLabel, algorithmSelect, timeLabel, logArea);

        gridSizeSlider.addChangeListener(e -> canvasPanel.updateGridSize());
        drawButton.addActionListener(e -> canvasPanel.drawAndMeasure());
    }

    private void setupLayout() {
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(new JLabel("Алгоритм:"));
        controlPanel.add(algorithmSelect);
        controlPanel.add(new JLabel("Сетка:"));
        controlPanel.add(gridSizeSlider);
        controlPanel.add(drawButton);
        //controlPanel.add(showDetailsCheckBox);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(statusLabel, BorderLayout.NORTH);
        infoPanel.add(timeLabel, BorderLayout.SOUTH);

        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(300, 150));
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Детали выполнения"));

        add(controlPanel, BorderLayout.NORTH);
        add(canvasPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(infoPanel, BorderLayout.NORTH);
        southPanel.add(logScrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    static class CanvasPanel extends JPanel {
        private int cellSize;
        private Integer startX, startY, endX, endY;
        private List<Point> gridPoints;
        private JSlider gridSizeSlider;
        private JLabel statusLabel;
        private JLabel timeLabel;
        private JComboBox<String> algorithmSelect;
        private JTextArea logArea;
        private long lastExecutionTime;

        public CanvasPanel(int size, JSlider gridSizeSlider, JLabel statusLabel,
                           JComboBox<String> algorithmSelect, JLabel timeLabel, JTextArea logArea) {
            this.gridSizeSlider = gridSizeSlider;
            this.statusLabel = statusLabel;
            this.algorithmSelect = algorithmSelect;
            this.timeLabel = timeLabel;
            this.logArea = logArea;

            setPreferredSize(new Dimension(size, size));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));

            cellSize = gridSizeSlider.getValue();
            gridPoints = new java.util.ArrayList<>();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int x = e.getX() / cellSize;
                    int y = e.getY() / cellSize;

                    if (startX == null) {
                        startX = x;
                        startY = y;
                        statusLabel.setText("Начальная точка выбрана: (" + startX + ", " + startY + "). Выберите конечную точку.");
                    } else {
                        endX = x;
                        endY = y;
                        statusLabel.setText("Конечная точка выбрана: (" + endX + ", " + endY + "). Нажмите \"Нарисовать\".");
                    }
                    repaint();
                }
            });
        }

        public void updateGridSize() {
            cellSize = gridSizeSlider.getValue();
            startX = startY = endX = endY = null;
            gridPoints.clear();
            timeLabel.setText("Время выполнения: -");
            logArea.setText("");
            statusLabel.setText("Нажмите на полотно, чтобы выбрать начальную и конечную точки.");
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawGrid(g);

            // Рисуем точки
            if (startX != null) {
                drawPoint(g, startX, startY, Color.GREEN);
            }
            if (endX != null) {
                drawPoint(g, endX, endY, Color.RED);
            }

            // Рисуем алгоритм
            for (Point p : gridPoints) {
                g.setColor(Color.BLACK);
                g.fillRect(p.x * cellSize, p.y * cellSize, cellSize, cellSize);
            }
        }

        private void drawGrid(Graphics g) {
            g.setColor(Color.LIGHT_GRAY);
            int width = getWidth();
            int height = getHeight();

            // Вертикальные линии и подписи
            for (int x = 0; x < width; x += cellSize) {
                g.drawLine(x, 0, x, height);
                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(x / cellSize), x + 2, 12);
                g.setColor(Color.LIGHT_GRAY);
            }

            // Горизонтальные линии и подписи
            for (int y = 0; y < height; y += cellSize) {
                g.drawLine(0, y, width, y);
                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(y / cellSize), 2, y + 12);
                g.setColor(Color.LIGHT_GRAY);
            }
        }

        private void drawPoint(Graphics g, int x, int y, Color color) {
            g.setColor(color);
            g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
        }

        public void drawAndMeasure() {
            if (startX == null || startY == null || endX == null || endY == null) {
                statusLabel.setText("Выберите начальную и конечную точки.");
                return;
            }

            gridPoints.clear();
            String algorithm = (String) algorithmSelect.getSelectedItem();
            AlgorithmResult result = null;

            logArea.setText(""); // Очищаем лог

            switch (algorithm) {
                case "Пошаговый":
                    result = Algorithms.stepAlgorithm(startX, startY, endX, endY);
                    logStepAlgorithmDetails(startX, startY, endX, endY, result);
                    break;
                case "ЦДА":
                    result = Algorithms.ddaAlgorithm(startX, startY, endX, endY);
                    logDDAAlgorithmDetails(startX, startY, endX, endY, result);
                    break;
                case "Брезенхем (линии)":
                    result = Algorithms.bresenhamLine(startX, startY, endX, endY);
                    logBresenhamLineDetails(startX, startY, endX, endY, result);
                    break;
                case "Брезенхем (окружность)":
                    int radius = (int) Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                    result = Algorithms.bresenhamCircle(startX, startY, radius);
                    logCircleDetails(startX, startY, radius, result);
                    break;
            }

            if (result != null) {
                gridPoints = result.getPoints();
                lastExecutionTime = result.getExecutionTime();
                timeLabel.setText(String.format("Время выполнения: %d мкс", lastExecutionTime));

                // Добавляем общую информацию в лог
                logArea.append("=== ОБЩАЯ ИНФОРМАЦИЯ ===\n");
                logArea.append(String.format("Алгоритм: %s\n", algorithm));
                logArea.append(String.format("Количество точек: %d\n", gridPoints.size()));
                logArea.append(String.format("Время выполнения: %d мкс\n\n", lastExecutionTime));
            }

            startX = startY = endX = endY = null;
            statusLabel.setText("Нажмите на сетку, чтобы выбрать начальную и конечную точки.");
            repaint();
        }

        private void logStepAlgorithmDetails(int x0, int y0, int x1, int y1, AlgorithmResult result) {
            logArea.append("=== ПОШАГОВЫЙ АЛГОРИТМ ===\n");
            logArea.append(String.format("Начальная точка: (%d, %d)\n", x0, y0));
            logArea.append(String.format("Конечная точка: (%d, %d)\n", x1, y1));

            int dx = Math.abs(x1 - x0);
            int dy = Math.abs(y1 - y0);

            logArea.append(String.format("dx = %d, dy = %d\n", dx, dy));
            if (dx > dy) {
                logArea.append("Доминирующая ось: X\n");
            } else {
                logArea.append("Доминирующая ось: Y\n");
            }
            logArea.append(String.format("Количество итераций: %d\n", Math.max(dx, dy)));
        }

        private void logDDAAlgorithmDetails(int x0, int y0, int x1, int y1, AlgorithmResult result) {
            logArea.append("=== АЛГОРИТМ ЦДА ===\n");
            logArea.append(String.format("Начальная точка: (%d, %d)\n", x0, y0));
            logArea.append(String.format("Конечная точка: (%d, %d)\n", x1, y1));

            int dx = x1 - x0;
            int dy = y1 - y0;
            int steps = Math.max(Math.abs(dx), Math.abs(dy));

            logArea.append(String.format("dx = %d, dy = %d\n", dx, dy));
            logArea.append(String.format("Количество шагов: %d\n", steps));
            logArea.append(String.format("Приращение X: %.3f\n", dx / (float)steps));
            logArea.append(String.format("Приращение Y: %.3f\n", dy / (float)steps));
        }

        private void logBresenhamLineDetails(int x0, int y0, int x1, int y1, AlgorithmResult result) {
            logArea.append("=== АЛГОРИТМ БРЕЗЕНХЕМА (ЛИНИЯ) ===\n");
            logArea.append(String.format("Начальная точка: (%d, %d)\n", x0, y0));
            logArea.append(String.format("Конечная точка: (%d, %d)\n", x1, y1));

            int dx = Math.abs(x1 - x0);
            int dy = Math.abs(y1 - y0);

            logArea.append(String.format("dx = %d, dy = %d\n", dx, dy));
            logArea.append(String.format("Начальная ошибка: %d\n", dx - dy));
        }

        private void logCircleDetails(int xc, int yc, int radius, AlgorithmResult result) {
            logArea.append("=== АЛГОРИТМ БРЕЗЕНХЕМА (ОКРУЖНОСТЬ) ===\n");
            logArea.append(String.format("Центр: (%d, %d)\n", xc, yc));
            logArea.append(String.format("Радиус: %d\n", radius));
            logArea.append(String.format("Начальное значение d: %d\n", 3 - 2 * radius));
            logArea.append(String.format("Диаметр: %d\n", radius * 2));
            logArea.append(String.format("Оценочное количество точек: %d\n", (int)(2 * Math.PI * radius)));
        }

        public long getLastExecutionTime() {
            return lastExecutionTime;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RasterGraphics();
        });
    }
}