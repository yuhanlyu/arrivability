package arrivability;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
		return Math.pow(1.0 - failureProbability, forbiddenArea(vertexset).size());
	}
	
	@Override
	protected Collection<Point> forbiddenArea(Iterable<Point> vertexset) {
		Map<Point, Integer> distanceMap = g.unweightedDistance(vertexset);
		Collection<Point> result = new HashSet<>();
		for (Point p : distanceMap.keySet()) {
			if (distanceMap.get(p) <= radius)
				result.add(p);
		}
		return result;
	}
	
	@Override
	public double[] analyze(List<Path<Point>> paths, List<Point> ends, Point target) {
		double[] result = new double[7];
		double[] temp = new double[7];
		Collection<Point> firstArea = forbiddenArea(paths.get(0));
		Collection<Point> secondArea = forbiddenArea(paths.get(1));
		Collection<Point> overlappedArea = new HashSet<>(firstArea);
	    
/*		double fail = 1 - Math.pow(1 - failureProbability, result[0]);
		if (result[1] > 0 && result[2] > 0)
			fail += Math.pow(1 - failureProbability, result[0])
		          * (1 - Math.pow(1.0 - failureProbability, result[1]))
		          * (1 - Math.pow(1.0 - failureProbability, result[2]));*/
		result[3] = Math.max(g.unweightedDistance(paths.get(0), ends.get(0), target) - 1, 0);
		result[4] = Math.max(g.unweightedDistance(paths.get(1), ends.get(1), target) - 1, 0);
		//Collection<Point> union = new HashSet<>(firstArea);
		//union.addAll(secondArea);
		//temp[3] = forbiddenAreaLB(paths.get(0), union, ends.get(0), target);
		//temp[4] = forbiddenAreaLB(paths.get(1), union, ends.get(1), target);
		//temp[3] = forbiddenAreaLB(paths.get(0), firstArea, ends.get(0), target);
		//temp[4] = forbiddenAreaLB(paths.get(1), secondArea, ends.get(1), target);
		//result[3] = Math.min(temp[3], result[3]);
		//result[4] = Math.min(temp[4], result[4]);
		//result[3] = temp[3];
		//result[4] = temp[4];
		//if (result[3] > temp[3] && Double.isFinite(temp[3]))
		//	System.out.println(result[3] + " " + temp[3]);
		//if (result[4] < temp[4] && Double.isFinite(temp[4]))
		//	System.out.println(result[4] + " " + temp[4]);
		
		//System.out.println(result[3] + " " + result[4]);
		result[5] = Math.max(g.unweightedDistance(paths.get(0), ends.get(0), paths.get(1)) - 1, 0);
		result[6] = Math.max(g.unweightedDistance(paths.get(1), ends.get(1), paths.get(0)) - 1, 0);
		//result[5] = forbiddenAreaLB(paths.get(0), firstArea, ends.get(0), paths.get(1).toCollection());
		//result[6] = forbiddenAreaLB(paths.get(1), secondArea, ends.get(1), paths.get(0).toCollection());
		//System.out.println(result[5] + " " + result[6]);
		overlappedArea.retainAll(secondArea);
	    firstArea.removeAll(overlappedArea);
	    secondArea.removeAll(overlappedArea);
		result[0] = overlappedArea.size();
		result[1] = firstArea.size();
		result[2] = secondArea.size();
		return result;
	}
	
	public double forbiddenAreaLB(Path<Point> path, Collection<Point> forbiddenArea, Point source, Point target) {
		Map<Point, Double> weight = new HashMap<>();
		for (Point point : g.vertexSet()) {
			weight.put(point, 0.0);
		}
		for (Point point : path) {
			weight.put(point, Double.POSITIVE_INFINITY);
		}
		weight.put(source, 0.0);
		for (Point point : g.vertexSet()) {
			if (!forbiddenArea.contains(point)) {
				int count = 1;
				for (Point neighbor : g.getNeighbors(point)) {
					if (!path.contains(neighbor))
						++count;
				}
				weight.put(point, weight.get(point) + 1.0 / count);
				for (Point neighbor : g.getNeighbors(point)) {
					if (!path.contains(neighbor)) {
						weight.put(neighbor, weight.get(neighbor) + 1.0 / count);
					}
				}
			}
		}
		return g.shortestDistance(source, target, weight);
	}
	
	public double forbiddenAreaLB(Path<Point> path, Collection<Point> forbiddenArea, Point source, Collection<Point> target) {
		Map<Point, Double> weight = new HashMap<>();
		for (Point point : g.vertexSet()) {
			weight.put(point, 0.0);
		}
		for (Point point : path) {
			weight.put(point, Double.POSITIVE_INFINITY);
		}
		weight.put(source, 0.0);
		for (Point point : g.vertexSet()) {
			if (!forbiddenArea.contains(point)) {
				int count = 1;
				for (Point neighbor : g.getNeighbors(point)) {
					if (!path.contains(neighbor))
						++count;
				}
				weight.put(point, weight.get(point) + 1.0 / count);
				for (Point neighbor : g.getNeighbors(point)) {
					if (!path.contains(neighbor)) {
						weight.put(neighbor, weight.get(neighbor) + 1.0 / count);
					}
				}
			}
		}
		return g.shortestDistance(source, target, weight);
	}
	
	@Override
	public double failureLB(List<Path<Point>> paths, List<Point> ends, Point target) {
		double[] analysis = analyze(paths, ends, target);
		//if (analysis[0] + analysis[1] + analysis[2] > maxUnion)
		//	return Double.POSITIVE_INFINITY;
		double result = 1 - Math.pow(1.0 - failureProbability, analysis[0]);
		// First path is closer than the second one
		if (analysis[3] < analysis[4]) {
			result += (Math.pow(1.0 - failureProbability, analysis[0])) 
					* (1 - Math.pow(1.0 - failureProbability, analysis[1] + analysis[3]))
					* (1 - Math.pow(1.0 - failureProbability, analysis[2] + Double.min(analysis[4], analysis[6])));
		// Second path is closer than the first one
		} else {
			result += (Math.pow(1.0 - failureProbability, analysis[0])) 
					* (1 - Math.pow(1.0 - failureProbability, analysis[1] + Double.min(analysis[3], analysis[5])))
					* (1 - Math.pow(1.0 - failureProbability, analysis[2] + analysis[4]));
		}
		//System.out.println(result);
		return result;
	}
	
	/**
	 * Compute the lower bound of forbidden area extending path to target
	 * @param path
	 * @param target
	 * @return
	 */
	public double[] forbiddenAreaBound(Path<Point> path, Point end, Point target) {
		Map<Point, Integer> distanceMap = g.unweightedDistance(path);
		int number = 0;
		for (Point vertex : g.vertexSet()) {
			number += distanceMap.get(vertex) <= radius ? 1 : 0;
		} 
		int distance = g.unweightedDistance(path, end, target);
		double[] result = new double[2];
	    
		if (distance > radius)
	    	result[0] = number + distance - (int)radius; 
	    else
	    	result[0] = number;
	    
		result[1] = distance * (Math.pow(4, radius) + 1);
		return result;
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
