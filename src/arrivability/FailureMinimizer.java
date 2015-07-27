package arrivability;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * 
 * @author yuhanlyu
 *
 */
public class FailureMinimizer {
	
	private static final Logger logger = Logger.getLogger(FailureMinimizer.class.getName());

	private FailureRate model;
	private Point source;
	private Point target;
	private int numberOfRobots;
	private int numberOfPaths;
	
	/**
	 * Constructor
	 * @param m failure rate model
	 * @param n the number of robots
	 * @param k the number of paths required to reach
	 * @param s source point
	 * @param t target point
	 */
	public FailureMinimizer(FailureRate m, int n, int k, Point s, Point t) {
		if (n != 2) {
			logger.severe("Only can find two paths");
			throw new UnsupportedOperationException("Only can find two paths");
		}
		if (k != 1) {
			logger.severe("Only can maximize 1-arrivability");
			throw new UnsupportedOperationException("Only can maximize 1-arrivability");
		}
		model = m;
		source = s;
		target = t;
		numberOfRobots = n;
		numberOfPaths = k;
	}
	
	/**
	 * Find an optimal solution
	 * @return a set of numberOfPaths paths reaching the target while minimizing the failure rate
	 */
	public Collection<Path<Point>> getSolution() {
		logger.info("Start to find an optimal solution");
		long startTime = System.nanoTime();
		List<Path<Point>> solution = compute();
		long endTime = System.nanoTime();
    	long duration = (endTime - startTime) / 1000000;
    	logger.info("Search completed");
    	logger.info("Minimum failure rate: " + (1 - model.failureRate(solution)) + " time is: " + duration);
		return solution;
	}
	
	/**
	 * Use A* algorithm to find an optimal solution
	 * @return a set of numberOfPaths paths reaching the target while minimizing the failure rate
	 */
	private List<Path<Point>> compute() {
		Queue<SearchNode> queue = new PriorityQueue<>();
		queue.offer(initialNode());
		int count = 0;
		while (!queue.isEmpty()) {
			++count;
			if (count % 10000 == 0)
				System.out.println("The " + count + "-th iteration.");
			SearchNode node = queue.poll();
			logger.fine("Dequeue a node " + node.toString());
			if (isTarget(node)) {
				logger.info("A* finished with " + count + " iterations");
				return getPaths(node);
			}
			for (SearchNode successor : successors(node)) {
				logger.finer("Enqueue a node " + successor.toString());
				queue.offer(successor);
			}
		}
		return null;
	}
	
	/**
	 * Compute the successors of a search node
	 * @param node a search node
	 * @return successors of the search node
	 */
	private Collection<SearchNode> successors(SearchNode node) {
		List<SearchNode> result = new ArrayList<>();
		List<Path<Point>> oldPaths = getPaths(node);
		List<Collection<Point>> oldAreas = model.forbiddenAreas(oldPaths);
		List<List<Point>> newEnds = extendEnds(oldPaths, node.getEnds());
		for (List<Point> ends : newEnds) {
			List<Path<Point>> paths = clonePaths(oldPaths);
			List<Collection<Point>> forbiddenAreas = cloneAreas(oldAreas);
			attachEnds(paths, ends, forbiddenAreas);
			SearchNode newNode = createNode(node, paths, ends, forbiddenAreas);
			result.add(newNode);
		}
		return result;
	}
	
	/**
	 * Extend all paths from the ends without intersecting itself
	 * @param paths paths to be extends
	 * @param ends ends of the paths
	 * @return all possible extensions
	 */
	private List<List<Point>> extendEnds(List<Path<Point>> paths, List<Point> ends){
		assert(paths.size() != ends.size());
		List<List<Point>> newEnds = new ArrayList<>(paths.size());
		// Compute all possible extensions for each path
		for (int i = 0; i < paths.size(); ++i) {
			newEnds.add(extendEnd(paths.get(i), ends.get(i)));
		}
		return combineEnds(newEnds);
	}
	
	/**
	 * From the lists of ends, create all combination of ends
	 * @param listOfEnds lists of ends
	 * @return lists of end
	 */
	private static List<List<Point>> combineEnds(List<List<Point>> listOfEnds) {
		int[] limits = new int[listOfEnds.size()];
		int count = 1;
		for (int i = 0; i < listOfEnds.size(); ++i) {
			limits[i] = listOfEnds.get(i).size();
			if (limits[i] == 0)
				return Collections.<List<Point>>emptyList();
			count *= limits[i];
		}
		List<List<Point>> result = new ArrayList<>();
		int[] cur = new int[listOfEnds.size()];
		while (true) {
			Point[] ends = new Point[listOfEnds.size()];
			for (int i = 0; i < listOfEnds.size(); ++i)
				ends[i] = listOfEnds.get(i).get(cur[i]);
			result.add(Arrays.asList(ends));
			int i = 0;
			for (i = 0; i < listOfEnds.size() && cur[i] == limits[i] - 1; ++i)
				cur[i] = 0;
			if (i == listOfEnds.size())
				break;
			++cur[i];
		}
		assert(count == result.size());
		return result;
	}
	
	/**
	 * Extend one path from the end without intersecting itself
	 * @param path a path
	 * @param end the end of the path
	 * @return possible extensions. If the end is already in the target, the extension is still in the target.
	 */
	private List<Point> extendEnd(Path<Point> path, Point end) {
		if (target.equals(end))
			return Arrays.asList(target);
		List<Point> result = new ArrayList<>();
		for (Point p : model.getNeighbors(end)) {
			if (!path.contains(p))
				result.add(p);
		}
		return result.size() > 0 ? result : Collections.<Point>emptyList();
	}
	
	/**
	 * Attach ends to the paths
	 * @param paths the paths to be attached to
	 * @param ends the ends to be attached
	 */
	private void attachEnds(List<Path<Point>> paths, List<Point> ends, List<Collection<Point>> forbiddenArea) {
		assert(paths.size() == ends.size());
		for (int i = 0; i < paths.size(); ++i) {
			paths.get(i).addVertex(ends.get(i));
			forbiddenArea.get(i).addAll(model.getForbiddenArea(ends.get(i)));
		}
	}
	
	/**
	 * Test whether a node reach the target
	 * @param node a search node
	 * @return true if the node reaches the target, false otherwise
	 */
	private boolean isTarget(SearchNode node) {
		for (int i = 0; i < node.getEnds().size(); ++i)
			if (!target.equals(node.getEnd(i)))
				return false;
		return true;
	}
	
	/**
	 * Create a initial node for the search
	 * @return the initial search node
	 */
	private SearchNode initialNode() {
		Path<Point>[] paths = new Path[numberOfRobots];
		Point[] ends = new Point[numberOfRobots];
		for (int i = 0; i < numberOfRobots; ++i) {
			Path<Point> path = new Path<>();
			path.addVertex(source);
			ends[i] = source;
			paths[i] = path;
		}
		return createNode(null, Arrays.asList(paths), Arrays.asList(ends));
	}
	
	/**
	 * Create a search node
	 * @param parent parent node
	 * @param paths paths
	 * @param ends ends
	 * @return a search node
	 */
	private SearchNode createNode(SearchNode parent, List<Path<Point>> paths, List<Point> ends) {
		Collection<Point>[] forbiddenAreas = new Collection[paths.size()];
		for (int i = 0; i < paths.size(); ++i) {
			forbiddenAreas[i] = model.forbiddenArea(paths.get(i));
		}
		return createNode(parent, paths, ends, Arrays.asList(forbiddenAreas));
	}
	
	/**
	 * Create a search node
	 * @param parent parent node
	 * @param paths paths
	 * @param ends ends
	 * @param forbiddenAreas forbidden areas
	 * @return a search node
	 */
	private SearchNode createNode(SearchNode parent, List<Path<Point>> paths, 
			                      List<Point> ends, List<Collection<Point>> forbiddenAreas) {
		assert(paths.size() == ends.size() && paths.size() == forbiddenAreas.size());
		double failureRate = model.failureRateFromForbidden(forbiddenAreas);
		double failureRateLB = model.failureRateLB(paths, ends, forbiddenAreas, target);
		double heuristic = failureRateLB >= failureRate ? failureRateLB - failureRate: 0.0;
		return new SearchNode(parent, failureRate, heuristic, ends, forbiddenAreas);
	}
	
	/**
	 * Search node class used in A* search
	 * @author yuhanlyu
	 *
	 */
	private static final class SearchNode implements Comparable<SearchNode> {
		private final SearchNode parent;                       // parent node
		private final double cost;                             // failure rate so far
		private final double heuristic;                        // lower bound of the failure rate needed to reach the goal
		private final List<Point> ends;                        // end points of paths
		
		/**
		 * Constructor
		 * @param arg_parent parent search node
		 * @param arg_cost failure rate so far
		 * @param arg_heuristic lower bound of the failure rate needed to reach the goal
		 * @param arg_ends end points of paths
		 * @param arg_paths paths
		 * @param arg_forbiddenAreas forbidden areas for each path
		 */
		public SearchNode(SearchNode arg_parent, double arg_cost, double arg_heuristic, 
				          List<Point> arg_ends, List<Collection<Point>> arg_forbiddenAreas) {
			parent = arg_parent;
			cost = arg_cost;
			heuristic = arg_heuristic;
			ends = arg_ends;
		}
				
		/**
		 * Get all ends
		 * @return all ends
		 */
		public List<Point> getEnds() {
			return ends;
		}
		
		
		/**
		 * Get the index-th end
		 * @param index the index of the end
		 * @return the index-th end
		 */
		public Point getEnd(int index) {
			return ends.get(index);
		}
		
		/**
		 * Get the heuristic
		 * @return heuristic
		 */
		public double getHeuristic() {
			return heuristic;
		}
				
		/**
		 * Get the failure rate so far
		 * @return failure rate so far
		 */
		public double getCost() {
			return cost;
		}
		
		/**
		 * Get the parent search node
		 * @return parent search node
		 */
		public SearchNode getParent() {
			return parent;
		}

		@Override
		public int compareTo(SearchNode o) {
			if (getCost() + getHeuristic() < o.getCost() + o.getHeuristic())
				return -1;
			else if (getCost() + getHeuristic()  > o.getCost() + o.getHeuristic())
				return 1;
			else
				return 0;
		}
		
		@Override
		public String toString() {
			return  " heuristic is " + heuristic + 
					" priority is " + (cost + heuristic);
		}
	}
	
	/**
	 * Clone a list of paths
	 * @param paths a list of paths to be cloned
	 * @return a duplication of the input paths
	 */
	private static List<Path<Point>> clonePaths(List<Path<Point>> paths) {
		Path<Point>[] duplicates = new Path[paths.size()];
		for (int i = 0; i < paths.size(); ++i)
			duplicates[i] = paths.get(i).duplicate();
		return Arrays.asList(duplicates);
	}
	
	/**
	 * Clone a list of paths
	 * @param forbiddenAreas a list of forbidden areas to be cloned
	 * @return a duplication of the input areas
	 */
	private static List<Collection<Point>> cloneAreas(List<Collection<Point>> forbiddenAreas) {
		Collection<Point>[] duplicates = new Collection[forbiddenAreas.size()];
		for (int i = 0; i < forbiddenAreas.size(); ++i)
			duplicates[i] = new HashSet<>(forbiddenAreas.get(i));
		return Arrays.asList(duplicates);
	}
	
	/**
	 * Build paths from a search node
	 * @param node a search node
	 * @return a set of paths
	 */
	private List<Path<Point>> getPaths(SearchNode node) {
		Deque<Point>[] stacks = new ArrayDeque[numberOfRobots];
		Point[] previousEnds = new Point[numberOfRobots];
		// Initialize stacks
		for (int i = 0; i < numberOfRobots; ++i) {
			stacks[i] = new ArrayDeque<>();
		}
		// Traceback
    	for(SearchNode current = node; current != null; current = current.getParent()) {
    		for (int i = 0; i < numberOfRobots; ++i) {
    			Point point = current.getEnd(i);
    			if (previousEnds[i] == null || ! previousEnds[i].equals(point)) {
    				stacks[i].push(point);
        			previousEnds[i] = point;
    			}
    		}
    	}

    	// Initialize paths
    	Path<Point>[] paths = new Path[numberOfRobots];
    	for (int i = 0; i < numberOfRobots; ++i) {
			paths[i] = new Path<>();
		}
    	// Put nodes		
    	for (int i = 0; i < paths.length; ++i) {
    		while (!stacks[i].isEmpty()) {
    			paths[i].addVertex(stacks[i].pop());
    		}
    	}
    	return Arrays.asList(paths);
	}
}
