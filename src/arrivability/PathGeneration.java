package arrivability;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * 
 * @author yiningchen
 *
 */
public class PathGeneration {
	private Graph<Point> g; // Graph
	private int mode;
	public static final int RANDOM = 0;
	public static final int REWEIGHT = 1;
	public static final int NUMBER_OF_MODE = 2;
	
	/**
	 * Constructor
	 * @param arg_g graph
	 */
	public PathGeneration(Graph<Point> arg_g, int arg_mode) {
		g = arg_g;
		mode = arg_mode;
	}

	/**
	 * Generate paths
	 * @param numberOfPaths the number of paths to be generated
	 * @param source source point
	 * @param target target point
	 * @return a list of generated paths
	 */
	public List<Path<Point>> getPaths(int numberOfPaths, Point source, Point target) {
		switch (mode) {
			case RANDOM: 
				return randomPaths(numberOfPaths, source, target);
			case REWEIGHT:
				return reweightPaths(numberOfPaths, source, target);
		}
		return null;
	}
	
	/**
	 * Generate a set of paths by reweighting
	 * @param numberOfPaths the number of path to be generated
	 * @param source sourece point
	 * @param target target point
	 * @return a list of paths
	 */
	public List<Path<Point>> reweightPaths(int numberOfPaths, Point source, Point target) {
		Path<Point>[] paths = new Path[numberOfPaths];
		Map<Point, Map<Point, Double>> edgeWeight = new HashMap<>();
		// Initialization of edge weight
		for (Point vertex : g.vertexSet()) {
			edgeWeight.put(vertex, new HashMap<>());
			for (Point neighbor : g.getNeighbors(vertex)) {
				edgeWeight.get(vertex).put(neighbor, 1.0);
			}
		}
		
		// Reweighting
		for (int i = 0; i < numberOfPaths; ++i) {
			Path<Point> path = reweightPath(source, target, edgeWeight);
			paths[i] = path;
			Point previous = null;
			for (Point vertex : path) {
				if (previous == null) {
					previous = vertex;
					continue;
				}
				double oldValue = edgeWeight.get(previous).get(vertex);
				edgeWeight.get(previous).put(vertex, oldValue + 1.0);
				previous = vertex;
			}
		}
		return Arrays.asList(paths);
	}
	
	/**
	 * Generate a path by reweighting
	 * @param source source point
	 * @param target target point
	 * @param edgeWeight edge weight
	 * @return a shortest path w.r.t edge weight
	 */
	private Path<Point> reweightPath(Point source, Point target, 
			Map<Point, Map<Point, Double>> edgeWeight) {
		Path<Point> path = g.shortestPath(source, target, edgeWeight);
		return path;
	}
	
	/**
	 * Generate a set of random paths
	 * @param numberOfPaths the number of paths to be generated
	 * @param source source point 
	 * @param target target point
	 * @return a list of random paths
	 */
	public List<Path<Point>> randomPaths(int numberOfPaths, Point source, Point target) {
		Path<Point>[] paths = new Path[numberOfPaths];
		if (MineField.PARALLEL)
			IntStream.range(0, numberOfPaths).parallel().forEach(i -> {
				paths[i] = randomPath(source, target);
			});
		else {
			for (int i = 0; i < numberOfPaths; ++i)
				paths[i] = randomPath(source, target);
		}
		return Arrays.asList(paths);
	}
	
	/**
	 * Generate a random path
	 * @param source source point
	 * @param target target point
	 * @return a random path
	 */
	public Path<Point> randomPath(Point source, Point target) {		
		Set<Point> inqueue = new HashSet<>();
		Set<Point> inpath = new HashSet<>();
		Map<Point, Point> parent = new HashMap<>();
		List<Point> myqueue = new ArrayList<>();
		inqueue.add(source);
		myqueue.add(source);
		parent.put(source, null);
		Random rand = new Random();
		while (true) {
			int index = rand.nextInt(myqueue.size());
			Point node = myqueue.remove(index);
			inqueue.remove(node);
			inpath.add(node);
			if (node.equals(target))
				break;
			for (Point neighbor : g.getNeighbors(node)) {
				if (!inqueue.contains(neighbor) && !inpath.contains(neighbor)) {
					inqueue.add(neighbor);
					parent.put(neighbor, node);
					myqueue.add(neighbor);
				}
			}
		}
		Deque<Point> stack = new ArrayDeque<>();
		Point current = target;
		while (current != null) {
			stack.push(current);
			current = parent.get(current);
		}
		Path<Point> path = new Path<>();
		while (!stack.isEmpty()) {
			path.addVertex(stack.pop());
		}
		return path;
	}

}
