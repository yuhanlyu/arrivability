package arrivability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class PathImprovement {
	
	private static final Logger logger = Logger.getLogger(PathImprovement.class.getName());
	private static final int NUMBER_OF_ITERATIONS = 100;
	private Graph<Point> g;
	private FailureRate fr;
	private Map<Point, Map<Point, Double>> distance;
	private Map<Point, Map<Point, Point>> next = new HashMap<>();
	
	/**
	 * Constructor
	 * @param arg_g graph
	 * @param arg_fr failure rate computation
	 * @param arg_d distance between all pairs
	 * @param arg_n next mapping
	 */
	public PathImprovement(Graph<Point> arg_g, FailureRate arg_fr,
			Map<Point, Map<Point, Double>> arg_d, Map<Point, Map<Point, Point>> arg_n) {
		g = arg_g;
		fr = arg_fr;
		distance = arg_d;
		next = arg_n;
	}

	/**
	 * Improve the solution by short-cut or escape from local maximum
	 * @param solution a solution
	 * @return a probably better solution
	 */
	public List<Path<Point>> improve(List<Path<Point>> solution) {
		logger.info("Start to improve");
		List<Path<Point>> globalMax = new ArrayList<>();
		globalMax.addAll(solution);
		double maxArrivability = fr.arrivability(solution);
		for (int i = 0; i < NUMBER_OF_ITERATIONS; ++i) {
			if (!canImprove(solution)) {
				logger.fine("Cannot improve, escape instead");
				escape(solution);
			} else {
				double arrivability = fr.arrivability(solution);
				if (arrivability > maxArrivability) {
					logger.info("Improved");
					maxArrivability = arrivability;
					globalMax.clear();
					globalMax.addAll(solution);
				}
			} 
		}
		logger.info("Local improvement completed with arrivability " + maxArrivability);
		return globalMax;
	}
	
	/**
	 * Escape from a local maximum
	 * @param solution a solution
	 */
	private void escape(List<Path<Point>> solution) {
		Random random = new Random();
		int randomIndex = random.nextInt(solution.size());
		Path<Point> path = solution.get(randomIndex);
		solution.remove(randomIndex);
		solution.add(escape(path));
	}
	
	/**
	 * Replace the path by a new path
	 * @param path a path
	 * @return a new path
	 */
	private Path<Point> escape(Path<Point> path) {
		logger.fine("Escape from " + path.toString());
		Path<Point> result = null;
		Random random = new Random();
		int randomBegin = random.nextInt(path.size() - 2);
		int randomEnd = random.nextInt(path.size() - randomBegin - 1) + randomBegin + 1;
		assert(0 <= randomEnd && randomEnd < path.size());
		result = path.slice(0, randomBegin);
		Path<Point> last = path.slice(randomEnd + 1, path.size());
		Path<Point> subpath = escape(path, randomBegin, randomEnd);
		if (subpath == null || result.contains(subpath) || last.contains(subpath))
			return path;
		result.concate(subpath);
		result.concate(last);
		return result;
	}
	
	/**
	 * Replace the subpath from randomBegin to randomEnd by a new path
	 * @param path path
	 * @param randomBegin the begin index of subpath to be replaced
	 * @param randomEnd the end index of subpath to be replaced
	 * @return a new path
	 */
	private Path<Point> escape(Path<Point> path, int randomBegin, int randomEnd) {
		logger.finer("Escape from " + path.toString() + " " + randomBegin + " " + randomEnd);
		Path<Point> shortest = shortestPath(path.get(randomBegin), path.get(randomEnd));
	    Point midPoint = shortest.get(shortest.size() / 2);
	    List<Point> points = new ArrayList<>();
	    for (Point point : g.vertexSet())
	    	if (distance.get(midPoint).get(point) <= shortest.size() - shortest.size() / 2) {
	    		if (!path.contains(point))
	    			points.add(point);
	    	}
	    Random random = new Random();
	    if (points.size() == 0)
	    	return null;
	    
	    int randomIndex = random.nextInt(points.size());
	    Point randomPoint = points.get(randomIndex);
	    Path<Point> first = shortestPath(path.get(randomBegin), randomPoint);
	    Path<Point> second = shortestPath(randomPoint, path.get(randomEnd));
	    second = second.slice(1, second.size());
	    if (first.contains(second))
	    	return null;
	    first.concate(second);
	    return first;
	}
	
	/**
	 * Try to improve current solution
	 * @param solution current solution
	 * @return true if solution is improved, false otherwise 
	 */
	private boolean canImprove(List<Path<Point>> solution) {
		logger.fine("Try to improve");
		double original = fr.arrivability(solution);
		double max = Double.NEGATIVE_INFINITY;
		List<Collection<Point>> forbiddenAreas = fr.forbiddenAreas(solution);
		Path<Point> newPath = null;
		int removeIndex = -1;
		for (int i = 0; i < solution.size(); ++i) {
			Collection<Point> oldArea = forbiddenAreas.get(i);
			Path<Point> path = solution.get(i);
			for (int j = 0; j < path.size(); ++j) {
				for (int k = j + 2; k < path.size(); ++k) {
					Path<Point> subpath = shortestPath(path.get(j), path.get(k));
					Path<Point> first = path.slice(0, j);
					if (subpath.contains(first))
						continue;
					Path<Point> last = path.slice(k + 1, path.size());
					if (subpath.contains(last))
						continue;
					first.concate(subpath);
					first.concate(last);
					forbiddenAreas.set(i, fr.forbiddenArea(first));
					double arrivability = fr.arrivabilityFromForbidden(forbiddenAreas);
					if (arrivability > max) {
						max = arrivability;
						removeIndex = i;
						newPath = first;
					}
				}
			}
			forbiddenAreas.set(i, oldArea);
		}
		if (max > original) {
			solution.remove(removeIndex);
			solution.add(newPath);
			return true;
		}
		return false;
	}
	
	/**
	 * Reconstruct the path from next mapping
	 * @param source source point
	 * @param target target point
	 * @return a shortest path from source to target
	 */
	private Path<Point> shortestPath(Point source, Point target) {
		Point current = source;
		Path<Point> result = new Path<>();
		result.addVertex(source);
		while (!current.equals(target)) {
			current = next.get(current).get(target);
			result.addVertex(current);
		}
		return result;
	}
}
