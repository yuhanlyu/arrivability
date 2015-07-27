package arrivability;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class MaximizeArrivability {

	private Arrivability model;
	private Point source, target;
	private int numberOfPaths;
	
	public MaximizeArrivability(Arrivability arg_model, int n, int k, Point s, Point t) {
		if (!(arg_model instanceof FixedRadiusPoissonHardDisk)) {
			throw new UnsupportedOperationException("Only fixed-radius-Poisson-hard-disk model is supported");
		}
		if (n != 2) {
			throw new UnsupportedOperationException("Only can find two paths");
		}
		if (k != 1) {
			throw new UnsupportedOperationException("Only can maximize 1-arrivability");
		}
		model = arg_model;
		numberOfPaths = n;
		source = s;
		target = t;
	}
	
	/**
	 * Choose subset of paths maximizing the arriability
	 * @param paths
	 * @param numberOfPaths
	 * @return
	 */
    public Collection<Path<Point>> pathSelection(Collection<Path<Point>> paths) {
    	Collection<Path<Point>> solution = new ArrayList<>(2);
    	Collection<Path<Point>> best = new ArrayList<>(2);
    	double bestArrivability = Double.NEGATIVE_INFINITY;
        @SuppressWarnings("unchecked")
		Path<Point>[] pathArray = paths.toArray(new Path[0]);
        System.out.println("Selection");
        // Pick two of them
    	for (int i = 0; i < paths.size(); ++i) {
    		for (int j = i + 1; j < paths.size(); ++j) {
    			solution.add(pathArray[i]);
    			solution.add(pathArray[j]);
    			double arrivability = model.computeArrivability(solution);
    			if (arrivability > bestArrivability) {
    				bestArrivability = arrivability;
    				best.clear();
    				best.addAll(solution);
    			}
    			solution.clear();
    		}
    	}
    	return null;
    }
    
    /**
	 * Use Astar algorithm to find an optimal solution
	 * @return
	 */
	public Collection<Path<Point>> getSolution(int maxUnion) {
		long startTime = System.nanoTime();
		Queue<SearchNode> queue = new PriorityQueue<>();
		Path<Point> path1 = new Path<>();
	    Path<Point> path2 = new Path<>();
	    path1.addVertex(source);
	    path2.addVertex(source);
		List<Path<Point>> paths = Arrays.asList(path1, path2);
		List<Point> ends = Arrays.asList(source, source);
		double heuristic = model.heuristic(paths, ends, target);
		if (!Double.isFinite(heuristic))
			return null;
		double cost = 1 - model.computeArrivability(paths);
		queue.offer(new SearchNode(source, source, cost, heuristic, null));
		List<Path<Point>> solution = Arrays.asList(null, null);
		int count = 0;
    	while (!queue.isEmpty()) {
    		count++;
    	    SearchNode node = queue.poll();
    	    //System.out.println("Delete a node " + node.toString());
    	    getPaths(node, paths);
    	    //System.out.println("Paths are " + paths.get(0).toString() + '\n');
    	    //System.out.println(paths.get(1).toString());
    	    ends.set(0, node.getEnd1());
    	    ends.set(1, node.getEnd2());
    	    if (target.equals(ends.get(0)) && target.equals(ends.get(1))) {
    	    	solution.set(0, path1.duplicate());
    	    	solution.set(1, path2.duplicate());
    	    	cost = node.getCost();
    	    	break;
    	    }
    	    if (!target.equals(ends.get(0)) && !target.equals(ends.get(1))) {
    	    	for (Point neighbor1 : model.getNeighbors(ends.get(0))) {
    	    		if (path1.contains(neighbor1))
    	    			continue;
    	    		path1.addVertex(neighbor1);
    	    		for (Point neighbor2 : model.getNeighbors(ends.get(1))) {
    	    			if (path2.contains(neighbor2))
    	    				continue;
    	    			path2.addVertex(neighbor2);
    	    			heuristic = model.heuristic(paths, ends, target);
    	    			if (Double.isFinite(heuristic)) {
    	    				cost = 1 - model.computeArrivability(paths);
    	    				SearchNode newNode = new SearchNode(neighbor1, neighbor2, cost, heuristic, node);
    	    				queue.offer(newNode);
    	    			}
    	    			path2.undo();
    	    		}
    	    		path1.undo();
    	    	}
    	    } else if (target.equals(ends.get(0))) {
	    		for (Point neighbor2 : model.getNeighbors(ends.get(1))) {
	    			if (path2.contains(neighbor2))
	    				continue;
	    			path2.addVertex(neighbor2);
	    			heuristic = model.heuristic(paths, ends, target);
	    			if (Double.isFinite(heuristic)) {
	    				cost = 1 - model.computeArrivability(paths);
	    				SearchNode newNode = new SearchNode(ends.get(0), neighbor2, cost, heuristic, node);
	    				queue.offer(newNode);
	    			}
	    			path2.undo();
	    		}
    	    } else {
    	    	for (Point neighbor1 : model.getNeighbors(ends.get(0))) {
	    			if (path1.contains(neighbor1))
	    				continue;
	    			path1.addVertex(neighbor1);
	    			heuristic = model.heuristic(paths, ends, target);
	    			if (Double.isFinite(heuristic)) {
	    				cost = 1 - model.computeArrivability(paths);
	    				SearchNode newNode = new SearchNode(neighbor1, ends.get(1), cost, heuristic, node);
	    				queue.offer(newNode);
	    			}
	    			path1.undo();
	    		}
    	    }
        }
    	long endTime = System.nanoTime();
    	long duration = (endTime - startTime) / 1000000;
    	System.out.println("Best arrivability is: " + (1 - cost) + " time is: " + duration + " iterations is " + count);
    	System.out.println("Maximum union bound is " + maxUnion + " actual union size is " + model.forbiddenArea(solution).size());
		return solution;
	}
	
	private static final class SearchNode implements Comparable<SearchNode> {
		private Point end1;
		private Point end2;
		private double cost;
		private double heuristic;
		private SearchNode parent;
		
		public SearchNode(Point arg_end1, Point arg_end2, double arg_cost, 
				double arg_heuristic, SearchNode arg_parent) {
			end1 = arg_end1;
			end2 = arg_end2;
			cost = arg_cost;
			heuristic = arg_heuristic;
			parent = arg_parent;
		}
		
		public Point getEnd1() {
			return end1;
		}
		
		public Point getEnd2() {
			return end2;
		}
		
		public double getHeuristic() {
			return heuristic;
		}
				
		public double getCost() {
			return cost;
		}
		
		public SearchNode getParent() {
			return parent;
		}
		
		@Override
		public String toString() {
			return "End1 is " + end1.toString() + " end2 is " + end2.toString() + " cost is " + cost + 
					" heuristic is " + heuristic + 
					" priority is " + (cost + heuristic);
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
	}
	
	/**
	 * Build paths from a search node
	 * @param node
	 * @return
	 */
	private static List<Path<Point>> getPaths(SearchNode node, List<Path<Point>> paths) {
		Deque<Point> stack1 = new ArrayDeque<>();
		Deque<Point> stack2 = new ArrayDeque<>();
		Point previousEnd1 = null, previousEnd2 = null;
    	for(SearchNode current = node; current != null; current = current.getParent()) {
    		Point end1 = current.getEnd1(), end2 = current.getEnd2();
    		if (previousEnd1 == null || !previousEnd1.equals(end1))
    			stack1.push(end1);
    		if (previousEnd2 == null || !previousEnd2.equals(end2))
    			stack2.push(end2);
    		previousEnd1 = end1;
    		previousEnd2 = end2;
    	}
    	
    	Path<Point> path1 = paths.get(0);
    	path1.clear();
    	while (!stack1.isEmpty()) {
    		path1.addVertex(stack1.pop());
    	}
    	
    	Path<Point> path2 = paths.get(1);
    	path2.clear();
    	while (!stack2.isEmpty()) {
    		path2.addVertex(stack2.pop());
    	}
    	
    	return Arrays.asList(path1, path2);
	}
}
