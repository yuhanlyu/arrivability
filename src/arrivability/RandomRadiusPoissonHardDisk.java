package arrivability;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RandomRadiusPoissonHardDisk extends Arrivability {

	private double lambda; // mean value of the Poission process
	private double beta;   // mean value of the exponential distribution
	private double failureProbability; // failure probability of every node
	
	/**
	 * Constructor
	 * @param n
	 * @param m
	 * @param arg_lambda
	 * @param arg_beta
	 */
	public RandomRadiusPoissonHardDisk(int n, int m, double arg_lambda, double arg_beta) {
		super(n, m);
		lambda = arg_lambda;
		beta = arg_beta;
		failureProbability = lambda / (n * m);
	}
	
	/**
	 * Get the mean value of the Poission process
	 * @return
	 */
	public double getLambda() {
		return lambda;
	}
	
	/**
	 * Get the mean value of the exponential distribution
	 * @return
	 */
	public double getBeta() {
		return beta;
	}
	
	@Override
	protected double arrivability(Iterable<Point> vertexset) {
		Map<Point, Double> distanceMap = g.distance(vertexset);
		double arrivability = 1.0;
		for (Point vertex : g.vertexSet()) {
			arrivability *= 1 - failureProbability * Math.pow(1 - 1/beta, distanceMap.get(vertex));
		}
		return arrivability;
	}
	
	private static final void easyTest() {
		RandomRadiusPoissonHardDisk model = new RandomRadiusPoissonHardDisk(10, 10, 2, 2);
		Collection<Path<Point>> paths = new LinkedList<>();
		
		Path<Point> path = new Path<>();
		path.addVertex(new Point(3, 3));
		path.addVertex(new Point(3, 4));
		path.addVertex(new Point(3, 5));
		path.addVertex(new Point(3, 6));
		path.addVertex(new Point(3, 7));
		
		Path<Point> path2 = new Path<>();
		path2.addVertex(new Point(3, 3));
		path2.addVertex(new Point(4, 3));
		path2.addVertex(new Point(5, 3));
		path2.addVertex(new Point(5, 4));
		path2.addVertex(new Point(5, 5));
		path2.addVertex(new Point(5, 6));
		path2.addVertex(new Point(5, 7));
		path2.addVertex(new Point(4, 7));
		path2.addVertex(new Point(3, 7));
		
		Path<Point> path3 = new Path<>();
		path3.addVertex(new Point(3, 3));
		path3.addVertex(new Point(2, 3));
		path3.addVertex(new Point(1, 3));
		path3.addVertex(new Point(1, 4));
		path3.addVertex(new Point(1, 5));
		path3.addVertex(new Point(1, 6));
		path3.addVertex(new Point(1, 7));
		path3.addVertex(new Point(2, 7));
		path3.addVertex(new Point(3, 7));
		
		paths = new LinkedList<>();
		paths.add(path);
		paths.add(path2);
		paths.add(path3);
		System.out.println(model.computeArrivability(path));
		System.out.println(model.computeArrivability(path2));
		//System.out.println(model.computeArrivability(paths));
	}
	
	public static final void main(String[] args) {
		RandomRadiusPoissonHardDisk model = new RandomRadiusPoissonHardDisk(10, 10, 2, 2);
		Point source = new Point(5, 2), target = new Point(5, 8);
		for (int i = 1; i < 10; ++i) {
			//System.out.println(model.computeArrivability(model.generateKPaths(source, target, i)));
		}	
	}
}
