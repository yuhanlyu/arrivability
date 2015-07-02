package arrivability;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FixedRadiusHardDisk extends Arrivability {
	
	private int numberOfBlockers;
	private int radius;
	
	/**
	 * Constructor
	 * @param n
	 * @param m
	 * @param blockers
	 * @param arg_radius
	 */
	public FixedRadiusHardDisk(int n, int m, int blockers, int arg_radius) {
		super(n, m);
		numberOfBlockers = blockers;
		radius = arg_radius;
	}
	
	/**
	 * Return the number of blocker
	 * @return
	 */
	public int getNumberOfBlocker() {
		return numberOfBlockers;
	}
	
	/**
	 * Return the radius
	 * @return
	 */
	public int getRadius() {
		return radius;
	}
	
	@Override
	protected double arrivability(Iterable<Point> vertexset) {
		Map<Point, Double> distanceMap = g.distance(vertexset);
		int number = 0;
		for (Point vertex : g.vertexSet()) {
			number += distanceMap.get(vertex) <= radius ? 1 : 0;
		}
		return Math.pow(1.0 - (double)(number) / g.vertexSet().size(), numberOfBlockers);
	}

	public static final void main(String[] args) {
		FixedRadiusHardDisk model = new FixedRadiusHardDisk(10, 10, 1, 1);
		Point source = new Point(5, 2), target = new Point(5, 8);
		for (int i = 1; i < 4; ++i) {
			//System.out.println(model.computeArrivability(model.generateKPaths(source, target, i)));
		}	
	}

	@Override
	protected Collection<Point> forbiddenArea(Iterable<Point> vertexset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double heuristic(List<Path<Point>> vertexset, Point target) {
		// TODO Auto-generated method stub
		return 0;
	}
}
