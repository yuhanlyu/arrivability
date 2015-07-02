package arrivability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FixedRadiusPoissonHardDisk extends Arrivability{
	
	private double radius;
	private double lambda;
	private double failureProbability;
	
	/**
	 * Constructor
	 * @param n
	 * @param m
	 * @param arg_radius
	 * @param arg_lambda
	 */
	public FixedRadiusPoissonHardDisk(int n, int m, int arg_radius, double arg_lambda) {
		super(n, m);
		radius = arg_radius;
		lambda = arg_lambda;
		failureProbability = lambda / (n * m);
	}

	@Override
	protected double arrivability(Iterable<Point> vertexset) {
		Map<Point, Integer> distanceMap = g.unweightedDistance(vertexset);
		int number = 0;
		for (Point vertex : g.vertexSet()) {
			if (distanceMap.get(vertex) <= radius)
				++number;
		}
		return Math.pow(1.0 - failureProbability, number);
	}
	
	@Override
	protected Collection<Point> forbiddenArea(Iterable<Point> vertexset) {
		Map<Point, Integer> distanceMap = g.unweightedDistance(vertexset);
		Collection<Point> result = new ArrayList<>();
		for (Point p : distanceMap.keySet()) {
			if (distanceMap.get(p) <= radius)
				result.add(p);
		}
		return result;
	}
	
	@Override
	public double heuristic(List<Path<Point>> vertexsets, Point target) {
		double currentArrivability = computeArrivability(vertexsets);
		
		double upperBound = 0.0;
		for (Path<Point> path : vertexsets)
			upperBound += Math.pow(1.0 - failureProbability, forbiddenAreaLB(path, target));
		//Collection<Point> vertexset = new HashSet(vertexsets.get(0).toCollection());
		//vertexset.addAll(vertexsets.get(1).toCollection());
		//upperBound -= arrivability(vertexset);
		if (upperBound <= currentArrivability)
			return (currentArrivability - upperBound);
		return 0.0;
	}
	
	/**
	 * Compute the lower bound of forbidden area extending path to target
	 * @param path
	 * @param target
	 * @return
	 */
	public double forbiddenAreaLB(Path<Point> path, Point target) {
		Map<Point, Integer> distanceMap = g.unweightedDistance(path);
		int number = 0;
		for (Point vertex : g.vertexSet()) {
			number += distanceMap.get(vertex) <= radius ? 1 : 0;
		} 
		int distance = distanceMap.get(target);
	    if (distance > radius)
	    	return (number + distance - (int)radius);
	    return number;
	}

	public static final void main(String[] args) {
		FixedRadiusPoissonHardDisk model = new FixedRadiusPoissonHardDisk(6, 6, 1, 1);
		Point source = new Point(3, 0), target = new Point(3, 5);
		Path<Point> path1 = new Path<>();
		path1.addVertex(new Point(3, 0));
		path1.addVertex(new Point(2, 0));
		path1.addVertex(new Point(2, 1));
		path1.addVertex(new Point(2, 2));
		path1.addVertex(new Point(2, 3));
		path1.addVertex(new Point(2, 4));
		path1.addVertex(new Point(2, 5));
		path1.addVertex(new Point(3, 5));
		
		Path<Point> path2 = new Path<>();
		path2.addVertex(new Point(3, 0));
		path2.addVertex(new Point(4, 0));
		path2.addVertex(new Point(5, 0));
		path2.addVertex(new Point(5, 1));
		path2.addVertex(new Point(5, 2));
		path2.addVertex(new Point(5, 3));
		path2.addVertex(new Point(5, 4));
		path2.addVertex(new Point(5, 5));
		path2.addVertex(new Point(4, 5));
		path2.addVertex(new Point(3, 5));
		System.out.println(model.computeArrivability(Arrays.asList(path1, path2)));
	}
}
