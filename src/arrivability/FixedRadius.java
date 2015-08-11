package arrivability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * A class for computing failure rate, which is the probability that all paths fail.
 * @author yuhanlyu
 *
 */
public class FixedRadius extends FailureRate {
	
	private static final Logger logger = Logger.getLogger(FixedRadius.class.getName());
	private double successProbability;
	
	/**
	 * Constructor
	 * @param group a failure group
	 * @param arg_g graph
	 * @param failure failure probability for each vertex
	 */
	public FixedRadius(FailureGroup<Point> group, Graph<Point> arg_g, double failure) {
		super(group, arg_g);
		if (!Double.isFinite(failure) || failure < 0 || failure > 1) {
    		logger.severe("Not a valid probaiblity");
    		throw new IllegalArgumentException("Not a valid probaiblity");
    	}
		successProbability = 1 - failure;
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
	
	@Override
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
     * Approximate arrivability by Monte Carlo
     * @param NumofTimes number of iterations
     * @param TargetSurvivals target number of survivals
     * @param paths selected paths
     * @return approximated arrivability
     */
    public double arrivabilityFromMonteCarlo(int NumofTimes, int TargetSurvivals,List<Path<Point>> paths) {
    	int totSuccess = 0;
    	Random random = new Random();
    	boolean isAlive[] = new boolean[paths.size()];
    	for (int i = 0; i<NumofTimes; i++) {
    		for (int j=0; j<paths.size();j++) isAlive[j]=true;
    		for (Point v : fg.vertexSet()) {
    			double mytry = random.nextDouble();
    			if (mytry>successProbability) {
    				for (int j=0; j<paths.size();j++) {
    					if (paths.get(j).contains(v)) isAlive[j]=false;
    					if (isAlive[j])
    						for (Point neighbor: fg.getForbiddenArea(v))
    							if (paths.get(j).contains(neighbor)) {
    								isAlive[j]=false; break;
    							}
    				}
    			}
    		}
    		int totSurvivals = 0;
    		for (int j=0; j<paths.size();j++) 
    			if (isAlive[j]) totSurvivals++;
    		if (totSurvivals>=TargetSurvivals) totSuccess++;
    	}
    	return (double) totSuccess/ NumofTimes;
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
    	GridGraph g = new GridGraph(5, 5, 1);
        FixedRadius fr = new FixedRadius(new GridFailureGroup(5, 5, 1), g, 0.01);
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
