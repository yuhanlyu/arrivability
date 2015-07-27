package arrivability;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.TreeSet;

/**
 * Undirected graph
 * @author yuhanlyu
 *
 * @param <V>
 */
public class Graph <V extends Comparable<V>> {
	private Collection<V> vertices = new HashSet<>();                    // all vertices
	private Map<V, Map<V, Double>> neighbors = new HashMap<>();         // adjacent lists
	
	/**
     * Return the vertexset
     * @return
     */
    public Collection<V> vertexSet() {
    	return vertices;
    }
    
    /**
     * Add a vertex
     * @param vertex
     */
    public void addVertex(V vertex) {
    	if (vertices.contains(vertex))
    		throw new IllegalArgumentException("Insert a duplicate vertex.");
    	vertices.add(vertex);
    	neighbors.put(vertex, new HashMap<>());
    }
    
    /**
     * Add an edge between source and target
     * @param source
     * @param target
     */
    public void addEdge(V source, V target) {
    	if (!vertices.contains(source) || !vertices.contains(target))
    		throw new IllegalArgumentException("Insert an edge between non-existence vertices.");

    	//edges.put(new E(source, target), 1.0);
    	//edges.put(new E(target, source), 1.0);
    	// Initialize unit cost edges
    	neighbors.get(source).put(target, 1.0);
    	neighbors.get(target).put(source, 1.0);
    }
    
    /**
     * 
     * @param source
     * @param target
     * @param weight
     */
    public void addEdge(V source, V target, double weight) {
    	if (!vertices.contains(source) || !vertices.contains(target))
    		throw new IllegalArgumentException("Insert an edge between non-existence vertices.");
    	neighbors.get(source).put(target, weight);
    	neighbors.get(target).put(source, weight);
    }
    
    /**
     * Test whether source and target are adjacent
     * @param source
     * @param target
     * @return
     */
    public boolean isAdjacent(V source, V target) {
    	if (!vertices.contains(source) || !vertices.contains(target))
    		throw new IllegalArgumentException("Test an edge between non-existence vertices.");
    	return neighbors.get(source).keySet().contains(target);
    }
    
    /**
     * Return the weight between source and target
     * @param source
     * @param target
     * @return
     */
    public double getWeight(V source, V target) {
    	if (!vertices.contains(source) || !vertices.contains(target))
    		throw new IllegalArgumentException("Test an edge between non-existence vertices.");
    	return neighbors.get(source).get(target);
    }
    
    /**
     * Return the neighbors of v
     * @param v
     * @return
     */
    public Collection<V> getNeighbors(V v) {
    	return neighbors.get(v).keySet();
    }
    
    /**
     * Return the minimum distance (number of edges) from a vertex set to a target 
     * @param target
     * @return
     */
    public double unweightedDistance(Iterable<V> vertexset, V target) {
    	for (V v : vertexset) {
    		if (!vertices.contains(v))
        		throw new IllegalArgumentException("Path does not exist.");
    	}
    	
    	Map<V, Integer> distanceMap = new HashMap<>();
    	Queue<V> queue = new ArrayDeque<>();
    	for (V v : vertices) {
    		distanceMap.put(v, Integer.MAX_VALUE);
    	}
    	for (V v : vertexset) {
    		distanceMap.put(v, 0);
    		queue.add(v);
    	}
    	
    	while (!queue.isEmpty()) {
    	    V node = queue.poll();
    	    
    	    if (node.equals(target))
    	    	break;
    	    int newDistance = distanceMap.get(node) + 1;
            // Visit each edge exiting u
            for (V neighbor : getNeighbors(node)) {
                
                if (distanceMap.get(neighbor) == Integer.MAX_VALUE) {
    		        distanceMap.put(neighbor, newDistance);
    		        queue.add(neighbor);
    		    }
            }
        }
    	return distanceMap.get(target);
    }
    
    /**
     * Compute the shortest distance from source to target
     * @param source source point
     * @param target target point
     * @return shortest distance from source to target
     */
    public double unweightedDistance(V source, V target) {
    	if (!vertices.contains(source) || !vertices.contains(target))
    		throw new IllegalArgumentException("Source or target does not exist.");
    	Map<V, Double> distanceMap = new HashMap<>();
    	Queue<V> queue = new ArrayDeque<>();
    	for (V v : vertices) {
    		distanceMap.put(v, Double.POSITIVE_INFINITY);
    	}
    	distanceMap.put(source, 0.0);
    	queue.add(source);
    	
    	while (!queue.isEmpty()) {
    	    V node = queue.poll();
    	    
    	    if (node.equals(target))
    	    	break;
    	    double newDistance = distanceMap.get(node) + 1.0;
            // Visit each edge exiting u
            for (V neighbor : getNeighbors(node)) {
                
                if (Double.isInfinite(distanceMap.get(neighbor))) {
    		        distanceMap.put(neighbor, newDistance);
    		        queue.add(neighbor);
    		    }
            }
        }
    	return distanceMap.get(target);
    }
    
    /**
     * Find the shortest distance from source to target with node-weights
     * @param source
     * @param target
     * @return
     */
    public double shortestDistance(V source, V target, Map<V, Double> vertexWeight) {
    	if (!vertices.contains(source) || !vertices.contains(target))
    		throw new IllegalArgumentException("Source or target does not exist.");
    	Map<V, V> parentMap = new HashMap<>();
    	Map<V, Double> distanceMap = new HashMap<>();
    	
    	NavigableSet<V> queue = new TreeSet<>(new Comparator<V>() {
            public int compare(V o1, V o2) {
            	int result = Double.compare(distanceMap.get(o1), distanceMap.get(o2));
            	return result == 0 ? o1.compareTo(o2) : result;
            }});
    	for (V v : vertices) {
    		distanceMap.put(v, v.equals(source) ? 0.0 : Double.POSITIVE_INFINITY);
    		parentMap.put(v, null);
    		queue.add(v);
    	}
    	while (!queue.isEmpty()) {
    	    V node = queue.pollFirst();
    	    
    	    if (distanceMap.get(node).isInfinite())
    	    	break;
    	    if (node.equals(target))
    	    	break;
            double distance = distanceMap.get(node);
            // Visit each edge exiting u
            for (V neighbor : getNeighbors(node)) {
                double newDistance = distance + vertexWeight.get(neighbor);
                
                if (newDistance < distanceMap.get(neighbor)) {
                	queue.remove(neighbor);
    		        parentMap.put(neighbor, node);
    		        distanceMap.put(neighbor, newDistance);
    		        queue.add(neighbor);
    		    }
            }
        }
    	return distanceMap.get(target);
    }
}
