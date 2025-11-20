//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.image.BufferedImage;
//
//public class Main extends JFrame {
//
//    private DrawPanel canvas;
//    private JComboBox<String> algorithmSelect;
//    private JSlider gridSizeSlider;
//    private JLabel status;
//
//    private int cellSize = 20;
//
//    private Integer startX = null, startY = null;
//    private Integer endX = null, endY = null;
//
//    public Main() {
//        super("Растровые Алгоритмы (Swing)");
//
//        canvas = new DrawPanel();
//        canvas.setPreferredSize(new Dimension(700, 700));
//
//        algorithmSelect = new JComboBox<>(new String[]{
//                "step",
//                "dda",
//                "bresenham",
//                "bresenham_circle"
//        });
//
//        gridSizeSlider = new JSlider(20, 50, 20);
//        gridSizeSlider.setMajorTickSpacing(5);
//        gridSizeSlider.setPaintTicks(true);
//        gridSizeSlider.setPaintLabels(true);
//        gridSizeSlider.addChangeListener(e -> updateGridSize());
//
//        JButton drawButton = new JButton("Нарисовать");
//        drawButton.addActionListener(e -> draw());
//
//        status = new JLabel("Нажмите на полотно, чтобы выбрать точки.");
//
//        JPanel controls = new JPanel();
//        controls.add(new JLabel("Алгоритм:"));
//        controls.add(algorithmSelect);
//        controls.add(new JLabel("Размер сетки:"));
//        controls.add(gridSizeSlider);
//        controls.add(drawButton);
//
//        add(controls, BorderLayout.NORTH);
//        add(canvas, BorderLayout.CENTER);
//        add(status, BorderLayout.SOUTH);
//
//        pack();
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
//        setLocationRelativeTo(null);
//        setVisible(true);
//    }
//
//    private void updateGridSize() {
//        cellSize = gridSizeSlider.getValue();
//        startX = startY = endX = endY = null;
//        canvas.clearImage();
//        canvas.repaint();
//        status.setText("Нажмите на полотно, чтобы выбрать точки.");
//    }
//
//    private void draw() {
//        if (startX == null || endX == null) {
//            status.setText("Выберите начальную и конечную точки.");
//            return;
//        }
//
//        String algo = (String) algorithmSelect.getSelectedItem();
//        switch (algo) {
//            case "step":
//                canvas.stepAlgorithm(startX, startY, endX, endY);
//                break;
//            case "dda":
//                canvas.ddaAlgorithm(startX, startY, endX, endY);
//                break;
//            case "bresenham":
//                canvas.bresenhamLine(startX, startY, endX, endY);
//                break;
//            case "bresenham_circle":
//                int r = (int) Math.round(Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2)));
//                canvas.bresenhamCircle(startX, startY, r);
//                break;
//        }
//
//        canvas.repaint();
//
//        startX = startY = endX = endY = null;
//        status.setText("Нажмите на полотно, чтобы выбрать точки.");
//    }
//
//
//    // ------------------------------------------------------------
//    // DRAWING PANEL WITH DOUBLE BUFFERING
//    // ------------------------------------------------------------
//
//    private class DrawPanel extends JPanel {
//
//        private BufferedImage img;
//        private Graphics2D g2;
//
//        public DrawPanel() {
//            // создаём буфер
//            img = new BufferedImage(700, 700, BufferedImage.TYPE_INT_ARGB);
//            g2 = img.createGraphics();
//            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//            clearImage();
//
//            addMouseListener(new MouseAdapter() {
//                @Override
//                public void mouseClicked(MouseEvent e) {
//                    int x = e.getX() / cellSize;
//                    int y = e.getY() / cellSize;
//
//                    if (startX == null) {
//                        startX = x;
//                        startY = y;
//                        status.setText("Начальная: (" + x + ", " + y + ")");
//                    } else {
//                        endX = x;
//                        endY = y;
//                        status.setText("Конечная: (" + x + ", " + y + "). Нажмите 'Нарисовать'.");
//                    }
//                    repaint();
//                }
//            });
//        }
//
//        public void clearImage() {
//            g2.setColor(Color.WHITE);
//            g2.fillRect(0, 0, img.getWidth(), img.getHeight());
//        }
//
//        @Override
//        protected void paintComponent(Graphics g) {
//            super.paintComponent(g);
//
//            g.drawImage(img, 0, 0, null);
//
//            // рисуем сетку поверх
//            g.setColor(Color.LIGHT_GRAY);
//            for (int x = 0; x < getWidth(); x += cellSize) {
//                g.drawLine(x, 0, x, getHeight());
//                g.drawString(String.valueOf(x / cellSize), x + 2, 12);
//            }
//            for (int y = 0; y < getHeight(); y += cellSize) {
//                g.drawLine(0, y, getWidth(), y);
//                g.drawString(String.valueOf(y / cellSize), 2, y + 12);
//            }
//
//            // старт и конец
//            if (startX != null) {
//                g.setColor(Color.GREEN);
//                g.fillRect(startX * cellSize, startY * cellSize, cellSize, cellSize);
//            }
//            if (endX != null) {
//                g.setColor(Color.RED);
//                g.fillRect(endX * cellSize, endY * cellSize, cellSize, cellSize);
//            }
//        }
//
//        // ALGORITHMS DRAW ON img (NOT PANEL)
//        private void drawCell(int x, int y, Color c) {
//            g2.setColor(c);
//            g2.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
//        }
//
//        public void stepAlgorithm(int x0, int y0, int x1, int y1) {
//            int dx = Math.abs(x1 - x0);
//            int dy = Math.abs(y1 - y0);
//            int sx = x0 < x1 ? 1 : -1;
//            int sy = y0 < y1 ? 1 : -1;
//
//            int x = x0, y = y0;
//
//            if (dx > dy) {
//                int error = dx / 2;
//                for (int i = 0; i <= dx; i++) {
//                    drawCell(x, y, Color.BLACK);
//                    x += sx;
//                    error -= dy;
//                    if (error < 0) {
//                        y += sy;
//                        error += dx;
//                    }
//                }
//            } else {
//                int error = dy / 2;
//                for (int i = 0; i <= dy; i++) {
//                    drawCell(x, y, Color.BLACK);
//                    y += sy;
//                    error -= dx;
//                    if (error < 0) {
//                        x += sx;
//                        error += dy;
//                    }
//                }
//            }
//        }
//
//        public void ddaAlgorithm(int x0, int y0, int x1, int y1) {
//            int dx = x1 - x0;
//            int dy = y1 - y0;
//            int steps = Math.max(Math.abs(dx), Math.abs(dy));
//
//            double x = x0;
//            double y = y0;
//            double xStep = dx / (double) steps;
//            double yStep = dy / (double) steps;
//
//            for (int i = 0; i <= steps; i++) {
//                drawCell((int) Math.round(x), (int) Math.round(y), Color.RED);
//                x += xStep;
//                y += yStep;
//            }
//        }
//
//        public void bresenhamLine(int x0, int y0, int x1, int y1) {
//            int dx = Math.abs(x1 - x0);
//            int dy = Math.abs(y1 - y0);
//
//            int sx = x0 < x1 ? 1 : -1;
//            int sy = y0 < y1 ? 1 : -1;
//
//            int err = dx - dy;
//
//            while (true) {
//                drawCell(x0, y0, Color.BLUE);
//                if (x0 == x1 && y0 == y1) break;
//
//                int e2 = 2 * err;
//                if (e2 > -dy) {
//                    err -= dy;
//                    x0 += sx;
//                }
//                if (e2 < dx) {
//                    err += dx;
//                    y0 += sy;
//                }
//            }
//        }
//
//        public void bresenhamCircle(int xc, int yc, int r) {
//            int x = 0;
//            int y = r;
//            int d = 3 - 2 * r;
//
//            circlePoints(xc, yc, x, y);
//
//            while (y >= x) {
//                x++;
//                if (d > 0) {
//                    d += 4 * (x - y) + 10;
//                    y--;
//                } else {
//                    d += 4 * x + 6;
//                }
//                circlePoints(xc, yc, x, y);
//            }
//        }
//
//        private void circlePoints(int xc, int yc, int x, int y) {
//            drawCell(xc + x, yc + y, new Color(150, 75, 0));
//            drawCell(xc - x, yc + y, new Color(150, 75, 0));
//            drawCell(xc + x, yc - y, new Color(150, 75, 0));
//            drawCell(xc - x, yc - y, new Color(150, 75, 0));
//            drawCell(xc + y, yc + x, new Color(150, 75, 0));
//            drawCell(xc - y, yc + x, new Color(150, 75, 0));
//            drawCell(xc + y, yc - x, new Color(150, 75, 0));
//            drawCell(xc - y, yc - x, new Color(150, 75, 0));
//        }
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(Main::new);
//    }
//}
