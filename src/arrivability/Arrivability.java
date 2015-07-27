package arrivability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public abstract class Arrivability {
	
	protected OldGraph<Point> g;
	
	abstract protected double arrivability(Iterable<Point> vertexset);
	
	protected Collection<Point> forbiddenArea(Iterable<Point> vertexset) {
		return null;
	}
	
	protected Collection<Point> forbiddenArea(List<Path<Point>> vertexsets) {
		Collection<Point> vertexset = new HashSet<>();
		for (Path<Point> path: vertexsets)
			vertexset.addAll(path.toCollection());
		return forbiddenArea(vertexset);
	}
	
	/**
	 * Analyze vertexset
	 * @param vertexset
	 * @param ends
	 * @return result[0] is the size of overlapped forbidden area
	 *         result[1] is the size of exclusive forbidden area for the first path (
	 *         result[2] is the size of exclusive forbidden area for the second path
	 *         result[3] is the minimum distance from the first path to the target
	 *         result[4] is the minimum distance from the second path to the target
	 *         result[5] is the minimum distance from the first path to the second path
	 *         result[6] is the minimum distance from the second path to the first path
	 */
	public double[] analyze(List<Path<Point>> paths, List<Point> ends, Point target) {
		return null;
	}
	
	/**
	 * Compute the lower bound of failure probability from vertex set to target
	 * @param vertexset
	 * @param ends
	 * @param target
	 * @return
	 */
	public double failureLB(List<Path<Point>> paths, List<Point> ends, Point target) {
		return 0.0; 
	}
	
	/**
	 * Compute the minimum additional failure probability from paths to target
	 * @param paths
	 * @param ends
	 * @param target
	 * @return minimum additional failure probability from paths to target
	 *         if it is not possible from paths to target, return Double.POSITIVE_INFINITY
	 */
	public double heuristic(List<Path<Point>> paths, List<Point> ends, Point target) {
		double result = failureLB(paths, ends, target) - (1 - computeArrivability(paths));
		assert(result > 0.0 || Math.abs(result) < 1e-7);
		return result;
	}
	
	/**
	 * 
	 */
	public Arrivability() {
		g = new OldGraph<>();
	}
	
	/**
	 * Constructor for grid graph
	 * @param n
	 * @param m
	 */
	public Arrivability(int n, int m) {
		//g = new GridGraph(n, m);
	}
	
	/**
	 * Return the set of vertices in the graph
	 * @return
	 */
	public Collection<Point> vertexSet() {
		return g.vertexSet();
	}
	
	/**
	 * Reset the weight
	 */
	public void reset() {
		g.reset();
	}
	
	/**
	 * Generate a random path
	 * @param source
	 * @param target
	 * @return
	 */
	public Path<Point> randomPath(Point source, Point target) {
		return g.randomPath(source, target);
	}
	
	/**
	 * Generate random k paths
	 * @param source
	 * @param target
	 * @param k
	 * @return
	 */
	public Collection<Path<Point>> randomKPaths(Point source, Point target, int k) {
		ArrayList<Path<Point>> result = new ArrayList<>(k);
		for (int i = 0; i < k; ++i) {
			result.add(randomPath(source, target));
		}
		result.trimToSize();
		return result;
	}
	
	/**
	 * Find a shortest path from source to target
	 * @param source
	 * @param target
	 * @return
	 */
	public Path<Point> shortestPath(Point source, Point target) {
		return g.shortestPath(source, target);
	}
	
	/**
	 * Generate k paths from source to target
	 * @param source
	 * @param target
	 * @param k
	 * @return
	 */
	public Collection<Path<Point>> generateKPaths(Point source, Point target, int numberOfPaths) {
		return g.generateKPaths(source, target, numberOfPaths);
	}
	
	/**
	 * Compute the arrivability of a single path
	 * @param path
	 * @return
	 */
	public double computeArrivability(Path<Point> path) {
		return arrivability(path.toCollection());
	}
	
	/**
     * Return the neighbors of v
     * @param v
     * @return
     */
    public Iterable<Point> getNeighbors(Point v) {
    	return g.getNeighbors(v);
    }
    
	/**
	 * Compute the arrivability of a set of paths
	 * @param paths
	 * @return
	 */
	public double computeArrivability(Collection<Path<Point>> paths) {
		int[] isSelected = new int[paths.size()];
		isSelected[0] = 1;
		Collection<Point> vertexset = new HashSet<>();
		// enumerate all subset and compute its contribution to arrivability
		for (double arrivability = 0.0; ;) {
			// Compute the arrivability for the subset
			
			int count = 0, i = 0;
			for (Path<Point> path : paths) {
				if (isSelected[i] == 1) {
					++count;
					vertexset.addAll(path.toCollection());						
				}
			    ++i;
			}
			arrivability += (count % 2 == 1 ? 1 : -1) * arrivability(vertexset);
			// Find the next subset
			for (i = 0; i < paths.size() && isSelected[i] == 1; ++i)
				isSelected[i] = 0;
			if (i == paths.size())
				return arrivability;
			isSelected[i] = 1;
			vertexset.clear();
		}
	}
}
