package arrivability;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class FailureRate {
	private static final Logger logger = Logger.getLogger(FailureRate.class.getName());
	protected FailureGroup<Point> fg;
	protected Graph<Point> g;
	protected Map<Point, Integer> indexMap = new HashMap<>();
	
	/**
	 * 
	 * @param group
	 * @param arg_g
	 */
	public FailureRate(FailureGroup<Point> group, Graph<Point> arg_g) {
		this.fg = group;
		this.g = arg_g;
		int i = 0;
		for (Point point : fg.vertexSet()) {
			indexMap.put(point, i);
			++i;
		}
	}
	
	/**
	 * Compute the arrivability for a set of vertices
	 * @param vertexset a set of vertices
	 * @return arrivability
	 */
	abstract public double arrivability(Iterable<Point> vertexset);
	
	/**
	 * Compute the arrivability of a set of paths
	 * @param paths a set of paths
	 * @param resuest number of requested robots
	 * @return the arrivability
	 */
	abstract public double arrivability(List<Path<Point>> paths, int request);
	
	
	
	/**
	 * Return the set of vertices in the failure group
	 * @return a set of vertices
	 */
	public Collection<Point> vertexSet() {
		return g.vertexSet();
	}
	
	/**
	 * Return the neighbors of a vertex
	 * @param vertex a vertex
	 * @return vertex's neighbors
	 */
	public Collection<Point> getNeighbors(Point vertex) {
		return g.getNeighbors(vertex);
	}
	
	/**
	 * Return the forbidden area of a vertex
	 * @param vertex a vertex
	 * @return vertex's forbidden area
	 */
	public Collection<Point> getForbiddenArea(Point vertex) {
		return fg.getForbiddenArea(vertex);
	}
	
	/**
     * Compute the forbidden area for a vertex set
     * @param vertexset a set of vertices
     * @return forbidden are for the vertex set
     */
    public Collection<Point> forbiddenArea(Iterable<Point> vertexset) {
    	HashSet<Point> result = new HashSet<>();
    	for (Point vertex : vertexset) {
    		result.addAll(fg.getForbiddenArea(vertex));
    		result.add(vertex);
    	}
    	return result;
    }
    
    /**
     * Compute the forbidden area for a set of paths
     * @param paths a set of paths
     * @return forbidden area for the paths
     */
    public Collection<Point> forbiddenArea(List<Path<Point>> paths) {
		Collection<Point> vertexset = new HashSet<>();
		for (Path<Point> path: paths)
			vertexset.addAll(path.toCollection());
		return forbiddenArea(vertexset);
	}

    /**
     * Compute the forbidden areas for a list of paths
     * @param paths a list of paths
     * @return forbidden areas for the paths
     */
    public List<Collection<Point>> forbiddenAreas(List<Path<Point>> paths) {
    	Collection<Point>[] forbiddenAreas = new Collection[paths.size()];
		for (int i = 0; i < paths.size(); ++i) {
			forbiddenAreas[i] = forbiddenArea(paths.get(i));
		}
    	return Arrays.asList(forbiddenAreas);
    }
    
    /**
	 * Compute the unweighted distance from vertexset to a target
	 * @param vertexset a vertex set
	 * @param target target
	 * @return the unweighted distance
	 */
	public double unweightedDistance(Collection<Point> vertexset, Point target) {
		return g.unweightedDistance(vertexset, target);
	}
	
	/**
	 * Test whether a point is a real node
	 * @param point a point
	 * @return true if the point is a real node, false otherwise
	 */
	protected boolean isFake(Point point) {
		return fg.vertexSet().contains(point) && !g.vertexSet().contains(point);
	}
	
	/**
	 * Compute the union of areas
	 * @param areas a set of areas
	 * @return union of all areas
	 */
	protected static Collection<Point> unionOfAreas(List<Collection<Point>> areas) {
		Collection<Point> union = new HashSet<>(areas.get(0));
		for (int i = 1; i < areas.size(); ++i)
			union.addAll(areas.get(i));
		return union;
	}
	
	/**
	 * Clone a list of paths
	 * @param forbiddenAreas a list of forbidden areas to be cloned
	 * @return a duplication of the input areas
	 */
	public static List<Collection<Point>> cloneAreas(List<Collection<Point>> forbiddenAreas) {
		Collection<Point>[] duplicates = new Collection[forbiddenAreas.size()];
		for (int i = 0; i < forbiddenAreas.size(); ++i)
			duplicates[i] = new HashSet<>(forbiddenAreas.get(i));
		return Arrays.asList(duplicates);
	}
	
	/**
	 * Convert from forbidden areas to bitsets
	 * @param forbiddenAreas a list of forbidden areas
	 * @return a list of bitsets
	 */
	public List<BitSet> fromAreasToBitSets(List<Collection<Point>> forbiddenAreas) { 
		BitSet[] result = new BitSet[forbiddenAreas.size()];
		for (int i = 0; i < forbiddenAreas.size(); ++i)
			result[i] = fromAreaToBitSet(forbiddenAreas.get(i));
		return Arrays.asList(result);
	}
	
	/**
	 * Convert from forbiden area
	 * @param forbiddenArea forbidden area
	 * @return a bitset corresponding to forbidden area
	 */
	public BitSet fromAreaToBitSet(Collection<Point> forbiddenArea) {
		BitSet result = new BitSet(fg.vertexSet().size());
		int i = 0;
		for (Point point : fg.vertexSet()) {
			if (forbiddenArea.contains(point))
				result.set(i);
			++i;
		}
		return result;
	}
	
	/**
	 * Convert a path to a bitset representing path's forbidden area
	 * @param path a path
	 * @return a bitset
	 */
	public BitSet fromPathToBitSet(Path<Point> path) {
		BitSet result = new BitSet(fg.vertexSet().size());
		for (Point point : path) {
			for (Point neighbor : fg.getForbiddenArea(point)) {
				int index = indexMap.get(neighbor);
				result.set(index);
			}
		}
		return result;
	}
	
	/**
	 * Convert from forbidden areas to super set of bitsets
	 * @param forbiddenAreas a list of forbidden areas
	 * @return a list of bitsets
	 */
	public List<BitSet> fromAreasToBitSuperSets(List<Collection<Point>> forbiddenAreas) { 
		BitSet[] temp = new BitSet[forbiddenAreas.size()];
		for (int i = 0; i < forbiddenAreas.size(); ++i)
			temp[i] = fromAreaToBitSet(forbiddenAreas.get(i));
		BitSet[] result = new BitSet[1 << forbiddenAreas.size()];
		for (int i = 0; i < 1 << forbiddenAreas.size(); ++i) {
			result[i] = new BitSet(fg.vertexSet().size());
			for (int j = 0; j < forbiddenAreas.size(); ++j) {
				if (((i >> j) & 1) == 1) {
					result[i].or(temp[j]);
				}
			}
		}
		return Arrays.asList(result);
	}
}
