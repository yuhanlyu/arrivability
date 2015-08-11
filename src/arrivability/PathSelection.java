package arrivability;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 
 * @author yuhanlyu
 *
 */
public class PathSelection {
	
	private static final Logger logger = Logger.getLogger(PathSelection.class.getName());
	private static final int CACHE_SIZE = 100000;
	private Graph<Point> g;
	private FailureRate fr;
	private Map<Path<Point>, Map<Path<Point>, Double>> distanceMap = Collections.synchronizedMap(new LinkedHashMap<Path<Point>, Map<Path<Point>, Double>>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry oldest) {
			return size() > CACHE_SIZE;  
		}  
	});
	
	/**
	 * Constructor
	 * @param arg_g graph
	 * @param arg_fr failure rate computation
	 */
	public PathSelection(Graph<Point> arg_g, FailureRate arg_fr) {
		g = arg_g;
		fr = arg_fr;
	}

	/**
	 * Select a subset with a given size maximizing arrivability
	 * @param candidates all candidates
	 * @param numberOfRobots number of robots
	 * @return a list of paths
	 */
	public List<Path<Point>> select(List<Path<Point>> candidates, int numberOfRobots, int numberOfRequest) {
		long startTime = System.nanoTime();
		List<Path<Point>> sols = initialSolution(candidates, numberOfRobots);
		long initialTime = System.nanoTime();
		logger.info("Initial solution takes " + (initialTime - startTime) / 1000000 + " milliseconds");
		distanceMap = Collections.synchronizedMap(new LinkedHashMap<Path<Point>, Map<Path<Point>, Double>>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry oldest) {
				return size() > CACHE_SIZE;  
			}  
		});
		List<Path<Point>> result = localImprovement(sols, candidates, numberOfRequest);
		long improveTime = System.nanoTime();
		logger.info("Local improvementn takes " + (improveTime - initialTime) / 1000000 + " milliseconds");
		return result;
	}
	
	/**
	 * Find a good initial solution
	 * @param candidates all candidates
	 * @param numberOfRobots number of robots
	 * @return an initial solution
	 */
	private List<Path<Point>> initialSolution(List<Path<Point>> candidates, int numberOfRobots) {
		//return randomK(paths, numberOfRobots);
		//return maxNeighborDistance(candidates, numberOfRobots);
		return maxObj(candidates, numberOfRobots, this::sumNeighborDistance);
	}
	
	/**
	 * Find a good initial solution based on maximum diversity problem
	 * @param candidates all candidates
	 * @param numberOfRobots number of robots
	 * @param objective objective function
	 * @return an initial solution
	 */
	private List<Path<Point>> maxObj(List<Path<Point>> candidates, int numberOfRobots, 
			Function<List<Path<Point>>, Double> objective) {

		List<Path<Point>> initial = randomK(candidates, numberOfRobots);
		
		while (increase(initial, candidates, objective)) {
			;
		}
		return initial;
	}
	
	/**
	 * Try to increase the objective function
	 * @param initial an initial solution
	 * @param candidates all candidates
	 * @param objective objective function
	 * @return true if improved, flase otherwise
	 */
	private static boolean increase(List<Path<Point>> initial, List<Path<Point>> candidates, 
			Function<List<Path<Point>>, Double> objective) {
		Result result = IntStream.range(0, initial.size()).parallel().mapToObj(i -> {
			List<Path<Point>> solution = Path.clonePaths(initial);
			double obj = 0.0;
			int index = 0;
			for (int j = 0; j < candidates.size(); ++j) {
				solution.set(i, candidates.get(j));
				double newSum = objective.apply(solution);
				if (newSum > obj) {
					obj = newSum;
					index = j;
				}
			}
			return new Result(obj, i, index);
		}).collect(Collectors.maxBy((a, b) -> Double.compare(a.sum, b.sum))).get();
		
		if (result.sum > objective.apply(initial)) {
			initial.set(result.removeIndex, candidates.get(result.newIndex));
			return true;
		}
		return false;
	}
	
	/**
	 * Improve arrivability
	 * @param initial an initial solution
	 * @param candidates candidate paths
	 * @return an improved solution
	 */
	private List<Path<Point>> localImprovement(List<Path<Point>> initial, List<Path<Point>> candidates, int numberOfRequest) {
		while (canImprove(initial, candidates, numberOfRequest)) {
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
	private boolean canImprove(List<Path<Point>> initial, List<Path<Point>> candidates, int numberOfRequest) {
		Result result = IntStream.range(0, initial.size()).parallel().mapToObj(i -> {
			List<Path<Point>> solution = Path.clonePaths(initial);
			double obj = 0.0;
			int index = 0;
			for (int j = 0; j < candidates.size(); ++j) {
				solution.set(i, candidates.get(j));
				double newSum = fr.arrivability(solution, numberOfRequest);
				if (newSum > obj) {
					obj = newSum;
					index = j;
				}
			}
			return new Result(obj, i, index);
		}).collect(Collectors.maxBy((a, b) -> Double.compare(a.sum, b.sum))).get();
		
		if (result.sum > fr.arrivability(initial, numberOfRequest)) {
			initial.set(result.removeIndex, candidates.get(result.newIndex));
			return true;
		}
		return false;
	}
	
	/**
	 * A class for holding result
	 * @author yuhanlyu
	 *
	 */
	private static final class Result {
		public double sum;
		public int removeIndex;
		public int newIndex;
		
		/**
		 * Constructor
		 * @param s objective value
		 * @param ri remove index
		 * @param ni new index
		 */
		public Result(double s, int ri, int ni) {
			sum = s;
			removeIndex = ri;
			newIndex = ni;
		}
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
	 * Compute the distance between two paths
	 * @param path1 first path
	 * @param path2 second path
	 * @return the distance
	 */
	private double distance(Path<Point> path1, Path<Point> path2) {
		if (distanceMap.containsKey(path1) && distanceMap.get(path1).containsKey(path2)) {
			return distanceMap.get(path1).get(path2);
		}
		int m1 = path1.size(), m2 = path2.size();
		double[][] fre = new double[m1 + 1][m2 + 1];
		for (int i = 1; i <= m2; i++) 
			fre[0][i] = Double.POSITIVE_INFINITY;
    	for (int i = 1; i <= m1; i++) 
    		fre[i][0] = Double.POSITIVE_INFINITY;
    	
    	int i = 1;
    	for (Point p1 : path1) {
    		int j = 1;
    		for (Point p2 : path2) {
    			fre[i][j] = Math.max(g.distanceQuery(p1, p2), 
    					Math.min(Math.min(fre[i][j-1], fre[i-1][j]), fre[i-1][j-1]));
    			++j;
    		}
    		++i;
    	}
    	if (!distanceMap.containsKey(path1)) {
    		distanceMap.put(path1, new ConcurrentHashMap<>());
    	}
    	if (!distanceMap.containsKey(path2)) {
    		distanceMap.put(path2, new ConcurrentHashMap<>());
    	}
    	distanceMap.get(path1).put(path2, fre[m1][m2]);
    	distanceMap.get(path2).put(path1, fre[m1][m2]);
    	return fre[m1][m2];
	}
	
	/**
	 * Compute survivability of a set of paths
	 * @param paths
	 * @return survivability
	 */
	private double survivability(List<Path<Point>> paths) {
		double sum = 0.0;
		for (Path<Point> path1 : paths) 
			for (Point obstacle: path1) 
				for (int radius = 0; radius <g.vertexSet().size(); radius++) {
					int affectedNum = 0;
					for (Path<Point> path2 : paths)
						if (!path1.equals(path2)) {
							for (Point point: path2)
								if (g.distanceQuery(obstacle,point)<=radius){
									affectedNum++;
									break;
								}

						}
					sum+=affectedNum/(paths.size()-1)/path1.size();
				}
		return sum/paths.size();
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
	 * Find a local maximum of sum of distance to nearest neighbors
	 * @param candidates candidate paths
	 * @param numberOfRobots number of robots
	 * @return a local maximum solution
	 */
	/*
	private List<Path<Point>> maxNeighborDistance(List<Path<Point>> candidates, int numberOfRobots) {

		List<Path<Point>> initial = randomK(candidates, numberOfRobots);
		
		while (increaseNeighbor(initial, candidates)) {
			;
		}
		return initial;
	}*/
	
	/**
	 * Increase the sum of distance to nearest neighbors
	 * @param initial solution
	 * @param candidates candidate paths
	 * @return true if sum of distance is increased, false otherwise
	 */
	/*
	private boolean increaseNeighbor(List<Path<Point>> initial, List<Path<Point>> candidates) {
		double[] sums = new double[initial.size()];
		int[] newIndices = new int[initial.size()];
		if (MineField.PARALLEL) {
			IntStream.range(0, initial.size()).parallel().forEach(i -> {
				List<Path<Point>> solution = Path.clonePaths(initial);
				for (int j = 0; j < candidates.size(); ++j) {
					solution.set(i, candidates.get(j));
					double newSum = sumNeighborDistance(solution);
					if (newSum > sums[i]) {
						sums[i] = newSum;
						newIndices[i] = j;
					}
				}
			});
		} else {
			for (int i = 0; i < initial.size(); ++i) {
				Path<Point> oldPath = initial.get(i);
				for (int j = 0; j < candidates.size(); ++j) {
					initial.set(i, candidates.get(j));
					double newSum = sumNeighborDistance(initial);
					if (newSum > sums[i]) {
						sums[i] = newSum;
						newIndices[i] = j;
					}
				}
				initial.set(i, oldPath);
			}
		}
		double sum = sumNeighborDistance(initial);
		double max = Double.NEGATIVE_INFINITY;
		int removeIndex = -1, newIndex = -1;
		for (int i = 0; i < initial.size(); ++i) {
			if (sums[i] > max) {
				max = sums[i];
				removeIndex = i;
				newIndex = newIndices[i];
			}
		}
		if (max > sum) {
			initial.set(removeIndex, candidates.get(newIndex));
			return true;
		}
		return false;
	}*/
	
	/**
	 * Find a local maximum of minimum distance
	 * @param candidates candidate paths
	 * @param numberOfRobots number of robots
	 * @return a solution
	 */
	/*
	private List<Path<Point>> maxMinDistance(List<Path<Point>> candidates, int numberOfRobots) {
		List<Path<Point>> initial = randomK(candidates, numberOfRobots);
		
		while (increaseMin(initial, candidates)) {
			;
		}
		return initial;
	}*/
	
	/**
	 * Increase the minimum distance
	 * @param initial initial solution
	 * @param candidates candidates
	 * @return true if minimum distance is increased, false otherwise
	 */
	/*
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
	}*/
	
	/**
	 * Find a local maximum of sum of distances
	 * @param candidates candidate paths
	 * @param numberOfRobots number of robots
	 * @return a solution
	 */
	/*
	private List<Path<Point>> maxSumDistance(List<Path<Point>> candidates, int numberOfRobots) {
		List<Path<Point>> initial = randomK(candidates, numberOfRobots);
		
		while (increaseSum(initial, candidates)) {
			;
		}
		return initial;
	}*/
	
	/**
	 * Increase the sum of total distance
	 * @param initial initial solution
	 * @param candidates candidate paths
	 * @return true if total sum is increased, false otherwise
	 */
	/*
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
	}*/
}
