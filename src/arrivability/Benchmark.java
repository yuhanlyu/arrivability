package arrivability;

import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Benchmark extends ConsoleHandler {
	
	private static final int ROW = 50;
	private static final int COLUMN = 50;
	private static final int NUMBER_OF_BLOCKERS = 1;
	private static final int NUMBER_OF_ROBOTS = 1;
	private static final double FAILURE_PROBABILITY = (double)NUMBER_OF_BLOCKERS / (ROW * COLUMN);
	
	private int generationTime;
	private int initialSolutionTime;
	private int localImprovementTime;
	private int pathSelectionTime;
	private int pathImprovementTime;
	private int totalTime;
	
	@Override
	public void publish(LogRecord logRecord) {
		try (Scanner scanner = new Scanner(logRecord.getMessage())) {
			if (logRecord.getMessage().startsWith("Path generation takes ")) {
				scanner.skip("Path generation takes ");
				generationTime = scanner.nextInt();
			} else if (logRecord.getMessage().startsWith("Initial solution takes ")) {
				scanner.skip("Initial solution takes ");
				initialSolutionTime = scanner.nextInt();
			} else if (logRecord.getMessage().startsWith("Local improvementn takes ")) {
				scanner.skip("Local improvementn takes ");
				localImprovementTime = scanner.nextInt();
			} else if (logRecord.getMessage().startsWith("Path selection takes ")) {
				scanner.skip("Path selection takes ");
				pathSelectionTime = scanner.nextInt();
			} else if (logRecord.getMessage().startsWith("Path improvement takes ")) {
				scanner.skip("Path improvement takes ");
				pathImprovementTime = scanner.nextInt();
			} else if (logRecord.getMessage().startsWith("Search completed in ")) {
				scanner.skip("Search completed in ");
				totalTime = scanner.nextInt();
			}
		}
    }
	
	public void getResult() {
		System.out.println("Time = " + generationTime + " " + initialSolutionTime + " " + localImprovementTime
				           + " " + pathSelectionTime + " " + pathImprovementTime + " " + totalTime);
	}
	
	public static void main( String[] args ) {

	    // The root logger's handlers default to INFO. We have to
	    // crank them up. We could crank up only some of them
	    // if we wanted, but we will turn them all up.
		Benchmark bm = new Benchmark();
	    Logger.getLogger("").addHandler(bm);
	    GridFailureGroup fg = new GridFailureGroup(ROW, COLUMN);
		Graph<Point> g = GraphLoader.getGraph();
		FailureRate model = new FailureRate(fg, g, FAILURE_PROBABILITY);
		MaximizeArrivability ma = new MaximizeArrivability(g, model, NUMBER_OF_ROBOTS);
		Point source = new Point(ROW / 2, 0);
		Point target = new Point(ROW / 2, COLUMN - 1);
		
		ma.getSolution(source, target);
		bm.getResult();
	}
}
