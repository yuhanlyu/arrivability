package arrivability;

import java.util.List;
import java.util.logging.Logger;

/**
 * A class for maximizing arrivability
 * @author yuhanlyu
 *
 */
public class MaximizeArrivability {

	private static final Logger logger = Logger.getLogger(MaximizeArrivability.class.getName());
	public static final double EPSILON = 1e-7;
	private int numberOfRobots;
	private int numberOfRequest;
	private int numberOfGeneratedPaths;
	private int numberOfIteration;
	private PathGeneration pg;
	private PathSelection ps;
	private PathImprovement pi;
	
	/**
	 * Constructor
	 * @param g path generator
	 * @param s path selector
	 * @param i path enhance
	 */
	public MaximizeArrivability(Graph<Point> g, FailureRate fr, int number, int required, int generate, int genMode, int selMode, int iteration) {
		numberOfRobots = number;
		numberOfRequest = required;
		numberOfGeneratedPaths = generate;
		numberOfIteration = iteration;
		pg = new PathGeneration(g, genMode);
		ps = new PathSelection(g, fr, selMode);
		pi = new PathImprovement(g, fr);
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
		List<Path<Point>> paths = pg.getPaths(numberOfGeneratedPaths, source, target);
		long generationTime = System.nanoTime();
		logger.info("Path generation takes " + (generationTime - startTime) / 1000000 + " milliseconds");
		List<Path<Point>> solution = ps.select(paths, numberOfRobots, numberOfRequest);
		long selectionTime = System.nanoTime();
		logger.info("Path selection takes " + (selectionTime - generationTime) / 1000000 + " milliseconds");
		List<Path<Point>> improved = pi.improve(solution, pg, numberOfRequest, numberOfIteration);
		long endTime = System.nanoTime();
		logger.info("Path improvement takes " + (endTime - selectionTime) / 1000000 + " milliseconds");
    	long duration = (endTime - startTime) / 1000000;
    	logger.info("Search completed in " + duration + " milliseconds");    	
    	return improved;
	}
	
	/**
     * Get solution
     * @param source source point
     * @param target target point
     * @return a list of paths
     */
    public List<Path<Point>> getSolution(Point source, Point target, List<Path<Point>> solution) {
        logger.info("Start to find an optimal solution");
        long startTime = System.nanoTime();
        List<Path<Point>> improved = pi.improve(solution, pg, numberOfRequest, numberOfIteration);
        long endTime = System.nanoTime();
        logger.info("Path improvement takes " + (endTime - startTime) / 1000000 + " milliseconds");
        long duration = (endTime - startTime) / 1000000;
        logger.info("Search completed in " + duration + " milliseconds");       
        return improved;
    }
}
