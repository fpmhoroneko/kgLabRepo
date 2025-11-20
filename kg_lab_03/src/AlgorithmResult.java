import java.util.List;

public class AlgorithmResult {
    private List<Point> points;
    private long executionTime; // в микросекундах

    public AlgorithmResult(List<Point> points, long executionTime) {
        this.points = points;
        this.executionTime = executionTime;
    }

    public List<Point> getPoints() {
        return points;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
}