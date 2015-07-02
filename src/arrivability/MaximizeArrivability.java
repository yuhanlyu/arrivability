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
	public Collection<Path<Point>> getSolution() {
		long startTime = System.nanoTime();
		Queue<SearchNode> queue = new PriorityQueue<>();
		Path<Point> path = new Path<>();
		path.addVertex(source);
		double heuristic = model.heuristic(Arrays.asList(path, path), target);
		double cost = 1 - model.computeArrivability(path);
		queue.offer(new SearchNode(source, source, cost, heuristic, null));
		List<Path<Point>> solution = Arrays.asList(null, null);
		Path<Point> path1 = new Path<>();
	    Path<Point> path2 = new Path<>();
		List<Path<Point>> paths = Arrays.asList(path1, path2);
		double currentCost = Double.POSITIVE_INFINITY;
    	while (!queue.isEmpty()) {
    	    SearchNode node = queue.poll();
    	    if (node.getCost() >= currentCost)
    	    	continue;
    	    //System.out.println("Delete a node " + node.toString());
    	    getPaths(node, paths);
    	    //System.out.println("Paths are " + paths.get(0).toString() + '\n');
    	    //System.out.println(paths.get(1).toString());
    	    Point end1 = node.getEnd1(), end2 = node.getEnd2();
    	    if (target.equals(end1) && target.equals(end2)) {
    	    	solution.set(0, path1.duplicate());
    	    	solution.set(1, path2.duplicate());
    	    	currentCost = node.getCost();
    	    	System.out.println("Current arrivability is " + (1 - currentCost));
    	    	break;
    	    }
    	    if (!target.equals(end1) && !target.equals(end2)) {
    	    	for (Point neighbor1 : model.getNeighbors(end1)) {
    	    		if (path1.contains(neighbor1))
    	    			continue;
    	    		path1.addVertex(neighbor1);
    	    		for (Point neighbor2 : model.getNeighbors(end2)) {
    	    			if (path2.contains(neighbor2))
    	    				continue;
    	    			path2.addVertex(neighbor2);
    	    			heuristic = model.heuristic(paths, target);
    	    			cost = 1 - model.computeArrivability(paths);
    	    			if (cost + heuristic < currentCost) {
    	    				SearchNode newNode = new SearchNode(neighbor1, neighbor2, cost, heuristic, node);
    	    				queue.offer(newNode);
    	    			}
    	    			path2.undo();
    	    		}
    	    		path1.undo();
    	    	}
    	    } else if (target.equals(end1)) {
	    		for (Point neighbor2 : model.getNeighbors(end2)) {
	    			if (path2.contains(neighbor2))
	    				continue;
	    			path2.addVertex(neighbor2);
	    			heuristic = model.heuristic(paths, target);
	    			cost = 1 - model.computeArrivability(paths);
	    			if (cost + heuristic < currentCost) {
	    				SearchNode newNode = new SearchNode(end1, neighbor2, cost, heuristic, node);
	    				queue.offer(newNode);
	    			}
	    			path2.undo();
	    		}
    	    } else {
    	    	for (Point neighbor1 : model.getNeighbors(end1)) {
	    			if (path1.contains(neighbor1))
	    				continue;
	    			path1.addVertex(neighbor1);
	    			heuristic = model.heuristic(paths, target);
	    			cost = 1 - model.computeArrivability(paths);
	    			if (cost + heuristic < currentCost) {
	    				SearchNode newNode = new SearchNode(neighbor1, end2, cost, heuristic, node);
	    				queue.offer(newNode);
	    			}
	    			path1.undo();
	    		}
    	    }
        }
    	long endTime = System.nanoTime();
    	long duration = (endTime - startTime) / 1000000;
    	System.out.println("Best arrivability is: " + (1 - currentCost) + " time is: " + duration);
		return solution;
	}
	
	private class SearchNode implements Comparable<SearchNode>{
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
		List<List<Integer>> list = new ArrayList<>();
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
