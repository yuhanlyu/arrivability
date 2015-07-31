package arrivability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A class for maximizing arrivability
 * @author yuhanlyu
 *
 */
public class MaximizeArrivability {

	private static final Logger logger = Logger.getLogger(MaximizeArrivability.class.getName());
	private static final int NUMBER_OF_GENERATED_PATHS = 100;
	private int numberOfRobots;
	private PathGeneration pg;
	private PathSelection ps;
	private PathImprovement pi;
	
	/**
	 * Constructor
	 * @param g path generator
	 * @param s path selector
	 * @param i path enhance
	 */
	public MaximizeArrivability(Graph<Point> g, FailureRate fr, int number) {
		Map<Point, Map<Point, Point>> next = new HashMap<>();
		Map<Point, Map<Point, Double>> distance = g.allPairsSP(next);
		Map<Point, Map<Point, Point>> parent = new HashMap<>();
		Map<Point, Map<Point, Double>> distance2 = g.unweightedAPSP(parent);
		numberOfRobots = number;
		pg = new PathGeneration(g);
		ps = new PathSelection(fr, distance);
		pi = new PathImprovement(g, fr, distance, next);
	}
	
	/**
	 * Get solution
	 * @param source source point
	 * @param target target point
	 * @return a list of paths
	 */
	public List<Path<Point>> getSolution(Point source, Point target) {
		logger.info("Start to find an optimal solution");
		long startTime = System.nanoTime();
		List<Path<Point>> paths = pg.getPaths(NUMBER_OF_GENERATED_PATHS, source, target);
		long generationTime = System.nanoTime();
		List<Path<Point>> solution = ps.select(paths, numberOfRobots);
		long selectionTime = System.nanoTime();
		List<Path<Point>> improved = pi.improve(solution);
		long endTime = System.nanoTime();
    	long duration = (endTime - startTime) / 1000000;
    	logger.info("Search completed in " + duration + " milliseconds");
    	logger.info("Path generation takes " + (generationTime - startTime) / 1000000 + " milliseconds");
    	logger.info("Path selection takes " + (selectionTime - generationTime) / 1000000 + " milliseconds");
    	logger.info("Path improvement takes " + (endTime - selectionTime) / 1000000 + " milliseconds");
    	return improved;
	}
}
