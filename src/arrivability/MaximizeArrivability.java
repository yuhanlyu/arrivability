package arrivability;

import java.util.Collection;
import java.util.List;

public class MaximizeArrivability {

	private static final int NUMBER_OF_GENERATED_PATHS = 50;
	private int numberOfRobots = 2;
	private PathGeneration pg;
	private PathSelection ps;
	private PathImprovement pi;
	
	public MaximizeArrivability(PathGeneration g, PathSelection s, PathImprovement i) {
		pg = g;
		ps = s;
		pi = i;
	}
	
	public List<Path<Point>> getSolution(Point source, Point target) {
		List<Path<Point>> paths = pg.getPaths(NUMBER_OF_GENERATED_PATHS, source, target);
		List<Path<Point>> solution = ps.select(paths, numberOfRobots);
		return pi.improve(solution);
	}
}
