package arrivability;

import java.util.List;
import java.util.logging.Logger;

public class RandomRadius extends FailureRate {
	private static final Logger logger = Logger.getLogger(RandomRadius.class.getName());
	private double successProbability;
	private double expectedRadius;

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
		if (!Double.isFinite(failure) || failure <= 0) {
    		logger.severe("Not a valid radius");
    		throw new IllegalArgumentException("Not a valid radius");
    	}
		successProbability = 1 - failure;
		expectedRadius = radius;
	}

	@Override
	public double arrivability(Iterable<Point> vertexset) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double arrivability(List<Path<Point>> paths, int request) {
		// TODO Auto-generated method stub
		return 0;
	}
}
