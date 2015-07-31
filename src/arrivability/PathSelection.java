package arrivability;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PathSelection {
	
	private FailureRate fr;
	private Map<Point, Map<Point, Double>> distance;
	
	/**
	 * Constructor
	 * @param arg_g graph
	 * @param arg_fr failure rate computation
	 * @param arg_d distance between all pairs
	 */
	public PathSelection(FailureRate arg_fr, Map<Point, Map<Point, Double>> arg_d) {
		fr = arg_fr;
		distance = arg_d;
	}

	/**
	 * Select a subset with a given size maximizing arrivability
	 * @param candidates all candidates
	 * @param numberOfRobots number of robots
	 * @return a list of paths
	 */
	public List<Path<Point>> select(List<Path<Point>> candidates, int numberOfRobots) {
		List<Path<Point>> sols = initialSolution(candidates, numberOfRobots);
		return localImprovement(sols, candidates);
	}
	
	/**
	 * Find a good initial solution
	 * @param candidates all candidates
	 * @param numberOfRobots number of robots
	 * @return an initial solution
	 */
	private List<Path<Point>> initialSolution(List<Path<Point>> candidates, int numberOfRobots) {
		//return randomK(paths, numberOfRobots);
		return maxNeighborDistance(candidates, numberOfRobots);
	}
	
	/**
	 * Find a local maximum of sum of distance to nearest neighbors
	 * @param candidates candidate paths
	 * @param numberOfRobots number of robots
	 * @return a local maximum solution
	 */
	private List<Path<Point>> maxNeighborDistance(List<Path<Point>> candidates, int numberOfRobots) {

		List<Path<Point>> initial = randomK(candidates, numberOfRobots);
		
		while (increaseNeighbor(initial, candidates)) {
			;
		}
		return initial;
	}
	
	/**
	 * Increase the sum of distance to nearest neighbors
	 * @param initial solution
	 * @param candidates candidate paths
	 * @return true if sum of distance is increased, false otherwise
	 */
	private boolean increaseNeighbor(List<Path<Point>> initial, List<Path<Point>> candidates) {
		double sum = sumNeighborDistance(initial);
		double max = Double.NEGATIVE_INFINITY;
		int removeIndex = -1, newIndex = -1;
		for (int i = 0; i < initial.size(); ++i) {
			Path<Point> oldPath = initial.get(i);
			for (int j = 0; j < candidates.size(); ++j) {
				initial.set(i, candidates.get(j));
				double newSum = sumNeighborDistance(initial);
				if (newSum > max) {
					max = newSum;
					removeIndex = i;
					newIndex = j;
				}
			}
			initial.set(i, oldPath);
		}
		if (max > sum) {
			initial.set(removeIndex, candidates.get(newIndex));
			return true;
		}
		return false;
	}
	
	/**
	 * Compute the sum of distance to nearest neighbors
	 * @param paths solution
	 * @return the sum of distance to nearest neighbors
	 */
	private double sumNeighborDistance(List<Path<Point>> paths) {
		double sum = 0.0;
		for (int i = 0; i < paths.size(); ++i) {
			double min = Double.POSITIVE_INFINITY;
			for (int j = 0; j < paths.size(); ++j) {
				if (i == j) continue;
				double distance = distance(paths.get(i), paths.get(j));
				if (distance < min)
					min = distance;
			}
			sum += min;
		}
		return sum;
	}
	
	/**
	 * Find a local maximum of minimum distance
	 * @param candidates candidate paths
	 * @param numberOfRobots number of robots
	 * @return a solution
	 */
	private List<Path<Point>> maxMinDistance(List<Path<Point>> candidates, int numberOfRobots) {
		List<Path<Point>> initial = randomK(candidates, numberOfRobots);
		
		while (increaseMin(initial, candidates)) {
			;
		}
		return initial;
	}
	
	/**
	 * Increase the minimum distance
	 * @param initial initial solution
	 * @param candidates candidates
	 * @return true if minimum distance is increased, false otherwise
	 */
	private boolean increaseMin(List<Path<Point>> initial, List<Path<Point>> candidates) {
		double original = minDistance(initial);
		double min = Double.NEGATIVE_INFINITY;
		int removeIndex = -1, newIndex = -1;
		for (int i = 0; i < initial.size(); ++i) {
			Path<Point> oldPath = initial.get(i);
			for (int j = 0; j < candidates.size(); ++j) {
				initial.set(i, candidates.get(j));
				double newMin = minDistance(initial);
				if (newMin > min) {
					min = newMin;
					removeIndex = i;
					newIndex = j;
				}
			}
			initial.set(i, oldPath);
		}
		if (min > original) {
			initial.set(removeIndex, candidates.get(newIndex));
			return true;
		}
		return false;
	}
	
	/**
	 * Compute the sum of distance
	 * @param paths a list of paths
	 * @return the sum of distance
	 */
	private double minDistance(List<Path<Point>> paths) {
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < paths.size(); ++i)
			for (int j = i + 1; j < paths.size(); ++j) {
				double distance = distance(paths.get(i), paths.get(j));
				if (distance < min)
					min = distance;
			}
		return min;
	}
	
	/**
	 * Find a local maximum of sum of distances
	 * @param candidates candidate paths
	 * @param numberOfRobots number of robots
	 * @return a solution
	 */
	private List<Path<Point>> maxSumDistance(List<Path<Point>> candidates, int numberOfRobots) {
		List<Path<Point>> initial = randomK(candidates, numberOfRobots);
		
		while (increaseSum(initial, candidates)) {
			;
		}
		return initial;
	}
	
	/**
	 * Compute the sum of distance
	 * @param paths a list of paths
	 * @return the sum of distance
	 */
	private double sumDistance(List<Path<Point>> paths) {
		double sum = 0.0;
		for (Path<Point> path1 : paths) {
			for (Path<Point> path2 : paths)
				sum += distance(path1, path2);
		}
		return sum;
	}
	
	/**
	 * Increase the sum of total distance
	 * @param initial initial solution
	 * @param candidates candidate paths
	 * @return true if total sum is increased, false otherwise
	 */
	private boolean increaseSum(List<Path<Point>> initial, List<Path<Point>> candidates) {
		double sum = sumDistance(initial);
		double max = Double.NEGATIVE_INFINITY;
		int removeIndex = -1, newIndex = -1;
		for (int i = 0; i < initial.size(); ++i) {
			Path<Point> oldPath = initial.get(i);			
			for (int j = 0; j < candidates.size(); ++j) {
				initial.set(i, candidates.get(j));
				double newSum = sumDistance(initial);
				if (newSum > max) {
					max = newSum;
					removeIndex = i;
					newIndex = j;
				}
			}
			initial.set(i, oldPath);
		}
		if (max > sum) {
			initial.set(removeIndex, candidates.get(newIndex));
			return true;
		}
		return false;
	}
	
	/**
	 * Compute the distance between two paths
	 * @param path1 first path
	 * @param path2 second path
	 * @return the distance
	 */
	private double distance(Path<Point> path1, Path<Point> path2) {
		int m1 = path1.size(), m2 = path2.size();
		double[][] fre = new double[m1 + 1][m2 + 1];
		for (int i = 1; i <= m2; i++) 
			fre[0][i] = Double.POSITIVE_INFINITY;
    	for (int i=1; i <= m1; i++) 
    		fre[i][0] = Double.POSITIVE_INFINITY;
    	
    	int i = 1;
    	for (Point p1 : path1) {
    		int j = 1;
    		for (Point p2 : path2) {
    			fre[i][j] = Math.max(distance.get(p1).get(p2), 
    					Math.min(Math.min(fre[i][j-1], fre[i-1][j]), fre[i-1][j-1]));
    			++j;
    		}
    		++i;
    	}
    	return fre[m1][m2];
	}
	
	/**
	 * Pick k random paths
	 * @param candidates a set of paths
	 * @param numberOfRoboots number of selected paths
	 * @return a set of paths
	 */
	private static List<Path<Point>> randomK(List<Path<Point>> candidates, int numberOfRobots) {
		Path<Point>[] result = new Path[numberOfRobots];
		Random random = new Random();
		for (int count = 0; count < numberOfRobots; ++count) {
			result[count] = candidates.get(random.nextInt(candidates.size()));
		}
		return Arrays.asList(result);
	}
	
	/**
	 * Pick first K paths
	 * @param candidates a set of paths
	 * @param numberOfRoboots number of selected paths
	 * @return a set of paths
	 */
	private List<Path<Point>> firstK(List<Path<Point>> candidates, int numberOfRobots) {
		Path<Point>[] result = new Path[numberOfRobots];
		int count = 0;
		for (Path<Point> path : candidates) {
			result[count] = path;
			++count;
			if (count == numberOfRobots)
				break;
		}
		return Arrays.asList(result);
	}
	
	/**
	 * Improve arrivability
	 * @param initial an initial solution
	 * @param candidates candidate paths
	 * @return an improved solution
	 */
	private List<Path<Point>> localImprovement(List<Path<Point>> initial, List<Path<Point>> candidates) {
		while (canImprove(initial, candidates)) {
			;
		}
		return initial;
	}
	
	/**
	 * Improve arrivability
	 * @param initial an initial solution
	 * @param candidates candidate paths
	 * @return true if initial solution is improved, false otherwise
	 */
	private boolean canImprove(List<Path<Point>> initial, List<Path<Point>> candidates) {
		double original = fr.arrivability(initial);
		double max = Double.NEGATIVE_INFINITY;
		int removeIndex = -1, newIndex = -1;
		for (int i = 0; i < initial.size(); ++i) {
			Path<Point> oldPath = initial.get(i);
			for (int j = 0; j < candidates.size(); ++j) {
				initial.set(i, candidates.get(j));
				double newSum = fr.arrivability(initial);
				if (newSum > max) {
					max = newSum;
					removeIndex = i;
					newIndex = j;
				}
			}
			initial.set(i, oldPath);
		}
		if (max > original) {
			initial.set(removeIndex, candidates.get(newIndex));
			return true;
		}
		return false;
	}
}
