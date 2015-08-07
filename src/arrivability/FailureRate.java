package arrivability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A class for computing failure rate, which is the probability that all paths fail.
 * @author yuhanlyu
 *
 */
public class FailureRate {
	
	private static final Logger logger = Logger.getLogger(FailureRate.class.getName());

	private FailureGroup<Point> fg;
	private Graph<Point> g;
	private double successProbability;
	private Map<Point, Integer> indexMap = new HashMap<>();
	
	/**
	 * Constructor
	 * @param group a failure group
	 * @param failure failure probability for each vertex
	 */
	public FailureRate(FailureGroup<Point> group, Graph<Point> arg_g, double failure) {
		if (!Double.isFinite(failure) || failure < 0 || failure > 1) {
    		logger.severe("Not a valid probaiblity");
    		throw new IllegalArgumentException("Not a valid probaiblity");
    	}
		fg = group;
		g = arg_g;
		successProbability = 1 - failure;
		int i = 0;
		for (Point point : fg.vertexSet()) {
			indexMap.put(point, i);
			++i;
		}
	}
	
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
	 * Compute the failure rate for a list of forbidden areas
	 * @param forbiddenArea a list of forbidden areas
	 * @return failure rate
	 */
	public double failureRateFromForbidden(List<Collection<Point>> forbiddenArea) {
		return 1 - arrivabilityFromForbidden(forbiddenArea, 1);
	}
	
	/**
	 * Compute the failure rate for a set of paths
	 * @param paths a set of paths
	 * @return failure rate
	 */
	public double failureRate(List<Path<Point>> paths) {
		return 1 - arrivability(paths, 1);
	}
	
	/**
	 * Compute the failure rate for a set of forbidden vertices
	 * @param forbiddenArea a set of vertices
	 * @return failure rate
	 */
	public double failureRateFromForbidden(Collection<Point> forbiddenArea) {
		return 1 - arrivabilityFromForbidden(forbiddenArea);
	}
	
	/**
	 * Compute the failure rate for a set of vertices
	 * @param vertexset a set of vertices
	 * @return failure rate
	 */
	public double failureRate(Iterable<Point> vertexset) {
		return 1 - arrivability(vertexset);
	}
	
	/**
	 * Compute the arrivability for a set of vertices
	 * @param vertexset a set of vertices
	 * @return arrivability
	 */
	public double arrivability(Iterable<Point> vertexset) {
		return arrivabilityFromForbidden(forbiddenArea(vertexset));
	}
	
	/**
	 * Compute the arrivability for a set of forbidden vertices
	 * @param forbiddenArea a set of vertices
	 * @return arrivability
	 */
	public double arrivabilityFromForbidden(Collection<Point> forbiddenArea) {
		return Math.pow(successProbability, forbiddenArea.size());
	}
	
	/**
	 * Compute the arrivability for the size of forbidden area
	 * @param size the size of forbidden area
	 * @return arrivability
	 */
	public double arrivabilityFromForbidden(double size) {
		return Math.pow(successProbability, size);
	}
    
    /**
	 * Compute the arrivability of a set of paths
	 * @param paths a set of paths
	 * @param resuest number of requested robots
	 * @return the arrivability
	 */
	public double arrivability(List<Path<Point>> paths, int request) {
		return arrivabilityFromForbidden(forbiddenAreas(paths), request);
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
     * Compute arrivability from a list of forbidden areas
     * @param forbiddenAreas forbidden areas
     * @param request the number of request robots
     * @return arrivability
     */
	public double arrivabilityFromForbidden(List<Collection<Point>> forbiddenAreas, int request) {
		if (request == 1)
			return arrivabilityFromForbidden(forbiddenAreas);
    	int n = forbiddenAreas.size(), k = request;
    	List<Collection<Point>> areas = new ArrayList<>();
    	// Gosper's hack
    	for (int comb = (1 << k) - 1; comb < 1 << n;) {
    		Collection<Point> forbiddenArea = new HashSet<>();
    		for (int i = 0; i < n; ++i) {
    			if (((comb >> i) & 1) == 1)
    				forbiddenArea.addAll(forbiddenAreas.get(i));
    		}
    		areas.add(forbiddenArea);
    		int x = comb & -comb, y = comb + x;
    	    comb = ((comb ^ y) / x >> 2) | y;
    	}
    	double result = arrivabilityFromForbidden(areas);
    	return result;
    }
	
	/**
     * Compute arrivability from a list of forbidden areas
     * @param forbiddenAreas forbidden areas
     * @param request the number of request robots
     * @return arrivability
     */
    private double arrivabilityFromForbidden(List<Collection<Point>> forbiddenAreas) {
    	int[] isSelected = new int[forbiddenAreas.size()];
		isSelected[0] = 1;
		Collection<Point> forbiddenArea = new HashSet<>();
		// enumerate all subset and compute its contribution to arrivability
		for (double arrivability = 0.0; ;) {
			// Compute the arrivability for the subset
			
			int count = 0, i = 0;
			for (Collection<Point> area : forbiddenAreas) {
				if (isSelected[i] == 1) {
					++count;
					forbiddenArea.addAll(area);						
				}
			    ++i;
			}
			arrivability += (count % 2 == 1 ? 1 : -1) * arrivabilityFromForbidden(forbiddenArea);
			// Find the next subset
			for (i = 0; i < forbiddenAreas.size() && isSelected[i] == 1; ++i)
				isSelected[i] = 0;
			if (i == forbiddenAreas.size())
				return arrivability;
			isSelected[i] = 1;
			forbiddenArea.clear();
		}
    }
    
    /**
     * Compute arrivability from a list of forbidden areas
     * @param forbiddenAreas bitset representation of areas
     * @param request the number of request robots
     * @return arrivability
     */
    public double arrivabilityFromBitSets(List<BitSet> forbiddenAreas, int request) {
    	if (request == 1)
			return arrivabilityFromBitSets(forbiddenAreas);
    	int n = forbiddenAreas.size(), k = request;
    	List<BitSet> areas = new ArrayList<>();
    	// Gosper's hack
    	for (int comb = (1 << k) - 1; comb < 1 << n;) {
    		BitSet forbiddenArea = new BitSet(vertexSet().size());
    		for (int i = 0; i < n; ++i) {
    			if (((comb >> i) & 1) == 1)
    				forbiddenArea.or(forbiddenAreas.get(i));
    		}
    		areas.add(forbiddenArea);
    		int x = comb & -comb, y = comb + x;
    	    comb = ((comb ^ y) / x >> 2) | y;
    	}
    	double result = arrivabilityFromBitSets(areas);
    	return result;
    }
    
    /**
     * Compute arrivability from a list of forbidden areas
     * @param forbiddenAreas bitset representation of areas
     * @return arrivability
     */
    private double arrivabilityFromBitSets(List<BitSet> forbiddenAreas) {
    	int[] isSelected = new int[forbiddenAreas.size()];
		isSelected[0] = 1;
		BitSet forbiddenArea = new BitSet(g.vertexSet().size());
		// enumerate all subset and compute its contribution to arrivability
		for (double arrivability = 0.0; ;) {
			// Compute the arrivability for the subset
			
			int count = 0, i = 0;
			for (BitSet area : forbiddenAreas) {
				if (isSelected[i] == 1) {
					++count;
					forbiddenArea.or(area);						
				}
			    ++i;
			}
			arrivability += (count % 2 == 1 ? 1 : -1) * arrivabilityFromForbidden(forbiddenArea.cardinality());
						
			// Find the next subset
			for (i = 0; i < forbiddenAreas.size() && isSelected[i] == 1; ++i)
				isSelected[i] = 0;
			if (i == forbiddenAreas.size())
				return arrivability;
			isSelected[i] = 1;
			forbiddenArea.clear();
		}
    }
    
    /**
	 * Compute the lower bound of the failure rate needed to reach the goal
	 * @return heuristic
	 */
	public double failureRateLB(List<Path<Point>> paths, List<Point> ends, 
			                 List<Collection<Point>> forbiddenAreas, Point target) {
		assert(paths.size() == ends.size() && paths.size() == forbiddenAreas.size());
		return smartLB(paths, ends, forbiddenAreas, target);
	}
	
	/**
	 * Use node-weighted shortest distance to get the lower bound
	 * @param paths current paths
	 * @param ends current ends
	 * @param forbiddenAreas current forbidden areas
	 * @param target target
	 * @return the lower bound of failure rate
	 */
	private double smartLB(List<Path<Point>> paths, List<Point> ends, 
            List<Collection<Point>> forbiddenAreas, Point target) {
		double lb = 1.0, sum = 0.0;
		for (int i = 0; i < paths.size(); ++i) {
			Map<Point, Double> nodeWeight = failureShare(paths.get(i), ends.get(i), forbiddenAreas.get(i));
			double shortestDistance = g.shortestDistance(ends.get(i), target, nodeWeight);
			sum += shortestDistance;
			lb -= Math.pow(successProbability, forbiddenAreas.get(i).size() + shortestDistance);
		}
		Collection<Point> union = unionOfAreas(forbiddenAreas);
		lb += Math.pow(successProbability, sum + union.size());
		return lb;
	}
	
	/**
	 * Share the failure rate of vertices not in forbidden area among all its neighbors not in path
	 * @param path a path
	 * @param end the end of the path
	 * @param forbiddenArea forbidden area
	 * @return weight on nodes
	 */
	private Map<Point, Double> failureShare(Path<Point> path, Point end, Collection<Point> forbiddenArea) {
		Map<Point, Double> weight = new HashMap<>();
		for (Point point : g.vertexSet()) {
			weight.put(point, 0.0);
		}
		for (Point point : path) {
			weight.put(point, Double.POSITIVE_INFINITY);
		}
		weight.put(end, 0.0);
		List<Point> work = new ArrayList<>();
		for (Point point : fg.vertexSet()) {
			if (!forbiddenArea.contains(point)) {
				work.clear();
				// If point is a fake boundary, then don't share it failure to itself
				if (!isFake(point))
					work.add(point);
				for (Point neighbor : fg.getForbiddenArea(point)) {
					// Only share
					if (!path.contains(neighbor) && !isFake(neighbor))
						work.add(neighbor);
				}
				for (Point freeNeighbor : work)
					weight.put(freeNeighbor, weight.get(freeNeighbor) + 1.0 / work.size());
			}
		}
		return weight;
	}

	/**
	 * Non-admissible LB, use the shortest distance from current end to the target
	 * @param paths current paths
	 * @param ends current ends
	 * @param forbiddenAreas current forbidden areas
	 * @param target target
	 * @return the lower bound of failure rate
	 */
	private double nonAdmissibleLB(List<Path<Point>> paths, List<Point> ends, 
            List<Collection<Point>> forbiddenAreas, Point target) {
		double lb = 1.0, sum = 0.0;
		for (int i = 0; i < paths.size(); ++i) {
			double shortestDistance = g.unweightedDistance(ends.get(i), target);
			sum += shortestDistance;
			lb -= Math.pow(successProbability, forbiddenAreas.get(i).size() + shortestDistance);
		}
		Collection<Point> union = unionOfAreas(forbiddenAreas);
		lb += Math.pow(successProbability, sum + union.size());
		return lb;
	}
	
	/**
	 * Simple lower bound, use the shortest distance from current area to the target
	 * @param paths current paths
	 * @param ends current ends
	 * @param forbiddenAreas current forbidden areas
	 * @param target target
	 * @return the lower bound of failure rate
	 */
	private double simpleLB(List<Path<Point>> paths, List<Point> ends, 
            List<Collection<Point>> forbiddenAreas, Point target) {
		double lb = 1.0, sum = 0.0;
		for (int i = 0; i < paths.size(); ++i) {
			double shortestDistance = unweightedDistance(forbiddenAreas.get(i), target);
			sum += shortestDistance;
			lb -= Math.pow(successProbability, forbiddenAreas.get(i).size() + shortestDistance);
		}
		Collection<Point> union = unionOfAreas(forbiddenAreas);
		lb += Math.pow(successProbability, sum + union.size());
		return lb;
	}
	
	/**
	 * Test whether a point is a real node
	 * @param point a point
	 * @return true if the point is a real node, false otherwise
	 */
	private boolean isFake(Point point) {
		return fg.vertexSet().contains(point) && !g.vertexSet().contains(point);
	}
	
	/**
	 * Compute the union of areas
	 * @param areas a set of areas
	 * @return union of all areas
	 */
	private static Collection<Point> unionOfAreas(List<Collection<Point>> areas) {
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
	
	/**
     * Computing arrivability based on the super set of forbidden areas
     * @param areaPowerSet the power set of forbidden areas
     * @param arrivability arrivability of the paths
     * @newSet the forbidden area of the new added path
     * @param request the number of request robots
     * @return arrivability
     */
    public double arrivabilityFromBitSuperSets(List<BitSet> areasPowerSet, double arrivability, BitSet newSet, int request) {
    	for (int i = 0; i < areasPowerSet.size(); ++i) {
    		BitSet temp = (BitSet)areasPowerSet.get(i).clone();
    		temp.or(newSet);
    		if (POPULATION_COUNT[i] + 1 >= request)
    			arrivability += ((POPULATION_COUNT[i]) % 2 == 0 ? 1 : -1) * arrivabilityFromForbidden(temp.cardinality());
    	}
    	return arrivability;
    }
    
    /**
     * Computing arrivability based on the super set of forbidden areas
     * @param areasPowerSet the super of forbidden areas
     * @param request the number of request robots
     * @return arrivability
     */
    public double arrivabilityFromBitSuperSets(List<BitSet> areasPowerSet, int request) {
    	double arrivability = 0.0;
    	for (int i = 1; i < areasPowerSet.size(); ++i) {
    		if (POPULATION_COUNT[i] >= request)
    			arrivability += ((POPULATION_COUNT[i]) % 2 == 1 ? 1 : -1) * arrivabilityFromForbidden(areasPowerSet.get(i).cardinality());
    	}
    	return arrivability;
    }
	
	private static final int[] POPULATION_COUNT = {0, 1, 1, 2, 1, 2, 2, 3, 
			   1, 2, 2, 3, 2, 3, 3, 4, 
			   1, 2, 2, 3, 2, 3, 3, 4, 
			   2, 3, 3, 4, 3, 4, 4, 5, 
			   1, 2, 2, 3, 2, 3, 3, 4, 
			   2, 3, 3, 4, 3, 4, 4, 5, 
			   2, 3, 3, 4, 3, 4, 4, 5, 
			   3, 4, 4, 5, 4, 5, 5, 6, 
			   1, 2, 2, 3, 2, 3, 3, 4, 
			   2, 3, 3, 4, 3, 4, 4, 5, 
			   2, 3, 3, 4, 3, 4, 4, 5, 
			   3, 4, 4, 5, 4, 5, 5, 6, 
			   2, 3, 3, 4, 3, 4, 4, 5, 
			   3, 4, 4, 5, 4, 5, 5, 6, 
			   3, 4, 4, 5, 4, 5, 5, 6, 
			   4, 5, 5, 6, 5, 6, 6, 7, 
			   1, 2, 2, 3, 2, 3, 3, 4, 
			   2, 3, 3, 4, 3, 4, 4, 5, 
			   2, 3, 3, 4, 3, 4, 4, 5, 
			   3, 4, 4, 5, 4, 5, 5, 6, 
			   2, 3, 3, 4, 3, 4, 4, 5, 
			   3, 4, 4, 5, 4, 5, 5, 6, 
			   3, 4, 4, 5, 4, 5, 5, 6, 
			   4, 5, 5, 6, 5, 6, 6, 7, 
			   2, 3, 3, 4, 3, 4, 4, 5, 
			   3, 4, 4, 5, 4, 5, 5, 6, 
			   3, 4, 4, 5, 4, 5, 5, 6, 
			   4, 5, 5, 6, 5, 6, 6, 7, 
			   3, 4, 4, 5, 4, 5, 5, 6, 
			   4, 5, 5, 6, 5, 6, 6, 7, 
			   4, 5, 5, 6, 5, 6, 6, 7, 
			   5, 6, 6, 7, 6, 7, 7, 8}; 
    
    public static void main(String[] args) {
    	GridGraph g = new GridGraph(5, 5);
        FailureRate fr = new FailureRate(new GridFailureGroup(5, 5), g, 0.01);
        Path<Point> path = new Path<>();
        path.addVertex(new Point(3, 0));
        path.addVertex(new Point(3, 1));
        path.addVertex(new Point(3, 2));
        path.addVertex(new Point(3, 3));
        path.addVertex(new Point(3, 4));
        System.out.println(fr.forbiddenArea(path).size());
        System.out.println(fr.arrivability(path));
        
        List<Path<Point>> paths = Arrays.asList(path, path);
        System.out.println(fr.arrivability(paths, 1));
    }
}
