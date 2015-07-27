package arrivability;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RandomRadiusHardDisk extends Arrivability {
	
	private int numberOfBlockers;
	private double beta;
	
	/**
	 * Constructor
	 * @param n
	 * @param m
	 * @param blockers
	 * @param arg_beta
	 */
	public RandomRadiusHardDisk(int n, int m, int blockers, double arg_beta) {
		super(n, m);
		numberOfBlockers = blockers;
		beta = arg_beta;
	}
	
	/**
	 * Return the number of blocker
	 * @return
	 */
	public int getNumberOfBlocker() {
		return numberOfBlockers;
	}
	
	/**
	 * Return the mean value of the radius
	 * @return
	 */
	public double getBeta() {
		return beta;
	}

	@Override
	protected double arrivability(Iterable<Point> vertexset) {
		Map<Point, Double> distanceMap = g.distance(vertexset);
		double singleFailure = 0.0;
		for (Point vertex : g.vertexSet()) {
			singleFailure += 1 - Math.pow(1 - 1/beta, distanceMap.get(vertex));
		}
		singleFailure /= g.vertexSet().size();
		return Math.pow(singleFailure, numberOfBlockers);
	}

	public static final void main(String[] args) {
		RandomRadiusHardDisk model = new RandomRadiusHardDisk(10, 10, 1, 2);
		Point source = new Point(5, 2), target = new Point(5, 8);
		for (int i = 1; i < 10; ++i) {
			//System.out.println(model.computeArrivability(model.generateKPaths(source, target, i)));
		}
	}
}
