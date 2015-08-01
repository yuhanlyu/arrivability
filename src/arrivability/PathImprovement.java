package arrivability;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PathImprovement {
	
	private static final Logger logger = Logger.getLogger(PathImprovement.class.getName());
	private static final int NUMBER_OF_ITERATIONS = 100;
	private Graph<Point> g;
	private FailureRate fr;
	private Map<Point, Map<Point, Double>> distance;
	private Map<Point, Map<Point, Point>> parent;
	
	/**
	 * Constructor
	 * @param arg_g graph
	 * @param arg_fr failure rate computation
	 * @param arg_d distance between all pairs
	 * @param arg_p parent mapping
	 */
	public PathImprovement(Graph<Point> arg_g, FailureRate arg_fr,
			Map<Point, Map<Point, Double>> arg_d, Map<Point, Map<Point, Point>> arg_n) {
		g = arg_g;
		fr = arg_fr;
		distance = arg_d;
		parent = arg_n;
	}

	/**
	 * Improve the solution by short-cut or escape from local maximum
	 * @param solution a solution
	 * @return a probably better solution
	 */
	public List<Path<Point>> improve(List<Path<Point>> solution) {
		logger.info("Start to improve");
		List<Path<Point>> globalMax = Path.clonePaths(solution);
		
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
					globalMax = Path.clonePaths(solution);
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
		solution.set(randomIndex, escape(path));
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
		Path<Point> shortest = g.buildPathBackward(path.get(randomBegin), path.get(randomEnd), parent);
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
	    Path<Point> first = g.buildPathBackward(path.get(randomBegin), randomPoint, parent);
	    Path<Point> second = g.buildPathBackward(randomPoint, path.get(randomEnd), parent);
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
	private boolean canImprove(List<Path<Point>> initial) {
		logger.fine("Try to improve");
		Result result = IntStream.range(0, initial.size()).parallel().mapToObj(i -> {
			List<BitSet> bitsets = fr.fromAreasToBitSets(fr.forbiddenAreas(initial));
			double obj = 0.0;
			Path<Point> path = initial.get(i), newPath = null;
			for (int j = 0; j < path.size(); ++j) {
				for (int k = j + 2; k < path.size(); ++k) {
					Path<Point> subpath = g.buildPathBackward(path.get(j), path.get(k), parent);
					Path<Point> first = path.slice(0, j);
					if (subpath.contains(first))
						continue;
					Path<Point> last = path.slice(k + 1, path.size());
					if (subpath.contains(last))
						continue;
					first.concate(subpath);
					first.concate(last);
					bitsets.set(i, fr.fromPathToBitSet(first));
					double arrivability = fr.arrivabilityFromBitSets(bitsets);
					if (arrivability > obj) {
						obj = arrivability;
						newPath = first;
					}
				}
			}
			return new Result(obj, i, newPath);
		}).collect(Collectors.maxBy((a, b) -> Double.compare(a.sum, b.sum))).get();
		
		if (result.sum > fr.arrivability(initial)) {
			initial.set(result.removeIndex, result.newPath);
			return true;
		}
		return false;
	}
	
	/**
	 * A class for holding result
	 * @author yuhanlyu
	 *
	 */
	private static final class Result {
		public double sum;
		public int removeIndex;
		public Path<Point> newPath;
		
		/**
		 * Constructor
		 * @param s objective value
		 * @param ri remove index
		 * @param np new path
		 */
		public Result(double s, int ri, Path<Point> np) {
			sum = s;
			removeIndex = ri;
			newPath = np;
		}
	}
}
