package arrivability;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 
 * @author yuhanlyu
 *
 */
public class PathImprovement {
	
	private static final Logger logger = Logger.getLogger(PathImprovement.class.getName());
	private static final int CACHE_SIZE = 100000;
	private Graph<Point> g;
	private FailureRate fr;
	private Map<ShortCutKey, ShortCutResult> cache = Collections.synchronizedMap(new LinkedHashMap<ShortCutKey, ShortCutResult>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry oldest) {
			return size() > CACHE_SIZE;  
		}  
	});
	
	/**
	 * Constructor
	 * @param arg_g graph
	 * @param arg_fr failure rate computation
	 */
	public PathImprovement(Graph<Point> arg_g, FailureRate arg_fr) {
		g = arg_g;
		fr = arg_fr;
	}

	/**
	 * Improve the solution by short-cut or escape from local maximum
	 * @param solution a solution
	 * @return a probably better solution
	 */
	public List<Path<Point>> improve(List<Path<Point>> solution, PathGeneration pg, int request, int numberOfIterations) {
		logger.info("Start to improve");
		int last = 0;
		List<Path<Point>> globalMax = new ArrayList<>(solution);
		g.reset();
		System.gc();
		double maxArrivability = fr.arrivability(solution, request);
		for (int i = 0; i < numberOfIterations; ++i) {
			while (canImprove(solution, request)) {
				double arrivability = fr.arrivability(solution, request);
				if (arrivability > maxArrivability) {
					logger.info("Improved");
					if (last != i) {
						logger.info("Useful escape");
					}
					last = i;
					maxArrivability = arrivability;
					globalMax = new ArrayList<>(solution);
				}
			}
			escape(solution, pg);
		}
		logger.info("Local improvement completed with arrivability " + maxArrivability);
		return globalMax;
	}
	
	/**
	 * Escape from a local maximum
	 * @param solution a solution
	 */
	private void escape(List<Path<Point>> solution, PathGeneration pg) {
		Random random = new Random();
		int randomIndex = random.nextInt(solution.size());
		Path<Point> path = solution.get(randomIndex);
		solution.set(randomIndex, escape(path, pg));
	}
	
	/**
	 * Replace the path by a new path
	 * @param path a path
	 * @return a new path
	 */
	private Path<Point> escape(Path<Point> path, PathGeneration pg) {
		logger.fine("Escape from " + path.toString());
		Path<Point> result = null;
		Random random = new Random();
		if (random.nextInt(5) < 4) {
			return pg.randomPath(path.get(0), path.get(path.size() - 1));
		}
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
		Path<Point> shortest = g.pathQuery(path.get(randomBegin), path.get(randomEnd));
	    Point midPoint = shortest.get(shortest.size() / 2);
	    List<Point> points = new ArrayList<>();
	    for (Point point : g.vertexSet())
	    	if (g.distanceQuery(midPoint, point) <= shortest.size() - shortest.size() / 2) {
	    		if (!path.contains(point))
	    			points.add(point);
	    	}
	    Random random = new Random();
	    if (points.size() == 0)
	    	return null;
	    
	    int randomIndex = random.nextInt(points.size());
	    Point randomPoint = points.get(randomIndex);
	    Path<Point> first = g.pathQuery(path.get(randomBegin), randomPoint);
	    Path<Point> second = g.pathQuery(randomPoint, path.get(randomEnd));
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
	private boolean canImprove(List<Path<Point>> initial, int request) {
		logger.fine("Try to improve");
		Result result = null;
		if (fr instanceof RandomRadius) {
			result = IntStream.range(0, initial.size()).parallel().mapToObj(i -> {
				List<Path<Point>> copy = new ArrayList(initial);
				double obj = 0.0;
				Path<Point> path = initial.get(i), newPath = null;

				for (int j = 0; j < path.size(); ++j) {
					for (int k = j + 2; k < path.size(); ++k) {
						ShortCutResult r = shortcut(path, j, k);
						if (r == null)
							continue;
						copy.set(i, r.path);
						double arrivability = fr.arrivability(copy, request);
						if (arrivability > obj) {
							obj = arrivability;
							newPath = r.path;
						}
					}
				}
				return new Result(obj, i, newPath);
			}).collect(Collectors.maxBy((a, b) -> Double.compare(a.sum, b.sum))).get();
		}
		else if (request == 1) {
			FixedRadius fr = (FixedRadius) (this.fr);
			result = IntStream.range(0, initial.size()).parallel().mapToObj(i -> {
				List<Path<Point>> initialCopy = new ArrayList<>();
				for (int index = 0; index < initial.size(); ++index)
					if (index != i) {
						initialCopy.add(initial.get(index));
					}
				List<BitSet> bitSuperSet = fr.fromAreasToBitSuperSets(fr.forbiddenAreas(initialCopy));
				double currentArrivability = fr.arrivabilityFromBitSuperSets(bitSuperSet, request);

				double obj = 0.0;
				Path<Point> path = initial.get(i), newPath = null;

				for (int j = 0; j < path.size(); ++j) {
					for (int k = j + 2; k < path.size(); ++k) {
						ShortCutResult r = shortcut(path, j, k);
						if (r == null)
							continue;
						double arrivability = fr.arrivabilityFromBitSuperSets(bitSuperSet, currentArrivability, r.area, request);
						if (arrivability > obj) {
							obj = arrivability;
							newPath = r.path;
						}
					}
				}
				return new Result(obj, i, newPath);
			}).collect(Collectors.maxBy((a, b) -> Double.compare(a.sum, b.sum))).get();
		} else {
			FixedRadius fr = (FixedRadius) (this.fr);
			result = IntStream.range(0, initial.size()).parallel().mapToObj(i -> {
				List<BitSet> areas = fr.fromAreasToBitSets(fr.forbiddenAreas(initial));
				double obj = 0.0;
				Path<Point> path = initial.get(i), newPath = null;

				for (int j = 0; j < path.size(); ++j) {
					for (int k = j + 2; k < path.size(); ++k) {
						ShortCutResult r = shortcut(path, j, k);
						if (r == null)
							continue;
						areas.set(i, r.area);
						double arrivability = fr.arrivabilityFromBitSets(areas, request);
						if (arrivability > obj) {
							obj = arrivability;
							newPath = r.path;
						}
					}
				}
				return new Result(obj, i, newPath);
			}).collect(Collectors.maxBy((a, b) -> Double.compare(a.sum, b.sum))).get();
		}
		
		if (result.sum > fr.arrivability(initial, request) + MaximizeArrivability.EPSILON) {
			initial.set(result.removeIndex, result.newPath);
			return true;
		}
		return false;
	}
	
	/**
	 * Short cut the path
	 * @param path a path to be shortcut
	 * @param j the start index of shortcut
	 * @param k the end index of shortcut
	 * @return a new path if shortcut is available, null otherwise
	 */
	private ShortCutResult shortcut(Path<Point> path, int j, int k) {
		ShortCutKey key = new ShortCutKey(path, j, k);
		if (cache.containsKey(key)) {
			return cache.get(key);				
		}
		Path<Point> subpath = g.pathQuery(path.get(j), path.get(k));
		Path<Point> first = path.slice(0, j);
		if (subpath.contains(first))
			return null;
		Path<Point> last = path.slice(k + 1, path.size());
		if (subpath.contains(last))
			return null;
		first.concate(subpath);
		first.concate(last);
		ShortCutResult r = new ShortCutResult(first, fr.fromPathToBitSet(first));
		cache.put(key, r);
		return r;
	}
	
	/**
	 * Key for the cache
	 * @author yuhanlyu
	 *
	 */
	private static final class ShortCutKey {
		public Path<Point> path;
		public int j;
		public int k;
		
		/**
		 * Constructor
		 * @param p path
		 * @param arg_j begin index
		 * @param arg_k end index
		 */
		public ShortCutKey(Path<Point> p, int arg_j, int arg_k) {
			path = p;
			j = arg_j;
			k = arg_k;
		}
		
		@Override
		public boolean equals(Object obj) {
			ShortCutKey rhs = (ShortCutKey) obj;
			return j == rhs.j && k == rhs.k && path == rhs.path;
		}
		
		@Override
		public int hashCode() {
			return path.hashCode() ^ (j << 16) ^ k;
		}
	}
	
	/**
	 * Result of the short cut
	 * @author yuhanlyu
	 *
	 */
	private static final class ShortCutResult {
		public Path<Point> path;
		public BitSet area;
		
		/**
		 * Constructor
		 * @param p a path
		 * @param a bitset
		 */
		public ShortCutResult(Path<Point> p, BitSet a) {
			path = p;
			area = a;
		}
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
