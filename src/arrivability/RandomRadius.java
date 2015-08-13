package arrivability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RandomRadius extends FailureRate {
	private static final Logger logger = Logger.getLogger(RandomRadius.class.getName());
	private double failureProbability;
	private double geometricParameter;

	/**
	 * Constructor 
	 * @param group a failure group
	 * @param arg_g graph
	 * @param failure failure probability for each vertex
	 * @param radius the expected value of the radius
	 */
	public RandomRadius(FailureGroup<Point> group, Graph<Point> arg_g, double failure, double radius) {
		super(group, arg_g);
		if (!Double.isFinite(failure) || failure < 0 || failure > 1) {
    		logger.severe("Not a valid probaiblity");
    		throw new IllegalArgumentException("Not a valid probaiblity");
    	}
		if (!Double.isFinite(radius) || radius <= 0) {
    		logger.severe("Not a valid radius");
    		throw new IllegalArgumentException("Not a valid radius");
    	}
		failureProbability = failure;
		geometricParameter = 1.0 / (radius); 
	}

	@Override
	public double arrivability(Iterable<Point> vertexset) {
		Map<Point, Integer> distanceMap = fg.unweightedDistance(vertexset);
		double arrivability = 1.0;
		for (Point point : vertexSet()) {
			arrivability *= (1 - failureProbability * (Math.pow(1 - geometricParameter, distanceMap.get(point))));
		}
		return arrivability;
	}

	@Override
	public double arrivability(List<Path<Point>> paths, int request) {
		List<Collection<Point>> areas = new ArrayList<>();
		for (int i = 0; i < paths.size(); ++i)
			areas.add(paths.get(i).toCollection());
		if (request == 1) {
			return arrivability(areas);
		}
		int n = paths.size(), k = request;
    	List<Collection<Point>> combineAreas = new ArrayList<>();
    	// Gosper's hack
    	for (int comb = (1 << k) - 1; comb < 1 << n;) {
    		Collection<Point> forbiddenArea = new HashSet<>();
    		for (int i = 0; i < n; ++i) {
    			if (((comb >> i) & 1) == 1)
    				forbiddenArea.addAll(areas.get(i));
    		}
    		combineAreas.add(forbiddenArea);
    		int x = comb & -comb, y = comb + x;
    	    comb = ((comb ^ y) / x >> 2) | y;
    	}
    	double result = arrivability(combineAreas);
    	return result;
	}
	
	/**
	 * Compute the arrivability of a list of paths
	 * @param paths a list of paths
	 * @return arrivability
	 */
	private double arrivability(List<Collection<Point>> areas) {
		int[] isSelected = new int[areas.size()];
		isSelected[0] = 1;
		Collection<Point> forbiddenArea = new HashSet<>();
		// enumerate all subset and compute its contribution to arrivability
		for (double arrivability = 0.0; ;) {
			// Compute the arrivability for the subset
			
			int count = 0, i = 0;
			for (Collection<Point> area : areas) {
				if (isSelected[i] == 1) {
					++count;
					forbiddenArea.addAll(area);						
				}
			    ++i;
			}
			arrivability += (count % 2 == 1 ? 1 : -1) * arrivability(forbiddenArea);
			// Find the next subset
			for (i = 0; i < areas.size() && isSelected[i] == 1; ++i)
				isSelected[i] = 0;
			if (i == areas.size())
				return arrivability;
			isSelected[i] = 1;
			forbiddenArea.clear();
		}
	}
	
	public static void main(String[] args) {
		RandomRadius rfr = new RandomRadius(new GridFailureGroup(5, 5, 1), new GridGraph(5, 5), 0.01, 2);
		Path<Point> path = new Path<>();
        path.addVertex(new Point(2, 0));
        path.addVertex(new Point(2, 1));
        path.addVertex(new Point(2, 2));
        path.addVertex(new Point(2, 3));
        path.addVertex(new Point(2, 4));
        //System.out.println(rfr.arrivability(path));
        
        Path<Point> path2 = new Path<>();
        path2.addVertex(new Point(2, 0));
        path2.addVertex(new Point(3, 0));
        path2.addVertex(new Point(3, 1));
        path2.addVertex(new Point(3, 2));
        path2.addVertex(new Point(3, 3));
        path2.addVertex(new Point(3, 4));
        path2.addVertex(new Point(2, 4));
        
        System.out.println(rfr.arrivability(Arrays.asList(path, path2), 1));
	}
}
