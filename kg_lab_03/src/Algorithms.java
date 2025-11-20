import java.util.ArrayList;
import java.util.List;

public class Algorithms {

    public static AlgorithmResult stepAlgorithm(int x0, int y0, int x1, int y1) {
        System.out.println("Выполняется пошаговый алгоритм");
        List<Point> points = new ArrayList<>();
        long startTime = System.nanoTime();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int x = x0, y = y0;

        if (dx > dy) {
            System.out.println("x - доминирующая ось");
            int error = dx / 2;
            for (int i = 0; i <= dx; i++) {
                points.add(new Point(x, y));
                x += sx;
                error -= dy;
                if (error < 0) {
                    y += sy;
                    error += dx;
                }
            }
        } else {
            System.out.println("y - доминирующая ось");
            int error = dy / 2;
            for (int i = 0; i <= dy; i++) {
                points.add(new Point(x, y));
                y += sy;
                error -= dx;
                if (error < 0) {
                    x += sx;
                    error += dy;
                }
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000;
        return new AlgorithmResult(points, duration);
    }

    public static AlgorithmResult ddaAlgorithm(int x0, int y0, int x1, int y1) {
        List<Point> points = new ArrayList<>();
        long startTime = System.nanoTime();

        int dx = x1 - x0;
        int dy = y1 - y0;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        float xIncrement = dx / (float) steps;
        float yIncrement = dy / (float) steps;

        float x = x0;
        float y = y0;

        for (int i = 0; i <= steps; i++) {
            points.add(new Point(Math.round(x), Math.round(y)));
            x += xIncrement;
            y += yIncrement;
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000;
        return new AlgorithmResult(points, duration);
    }

    public static AlgorithmResult bresenhamLine(int x0, int y0, int x1, int y1) {
        List<Point> points = new ArrayList<>();
        long startTime = System.nanoTime();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            points.add(new Point(x0, y0));
            if (x0 == x1 && y0 == y1) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000;
        return new AlgorithmResult(points, duration);
    }

    public static AlgorithmResult bresenhamCircle(int xc, int yc, int r) {
        List<Point> points = new ArrayList<>();
        long startTime = System.nanoTime();

        int x = 0;
        int y = r;
        int d = 3 - 2 * r;

        drawCirclePoints(points, xc, yc, x, y);

        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            drawCirclePoints(points, xc, yc, x, y);
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000;
        return new AlgorithmResult(points, duration);
    }

    private static void drawCirclePoints(List<Point> points, int xc, int yc, int x, int y) {
        points.add(new Point(xc + x, yc + y));
        points.add(new Point(xc - x, yc + y));
        points.add(new Point(xc + x, yc - y));
        points.add(new Point(xc - x, yc - y));
        points.add(new Point(xc + y, yc + x));
        points.add(new Point(xc - y, yc + x));
        points.add(new Point(xc + y, yc - x));
        points.add(new Point(xc - y, yc - x));
    }
}