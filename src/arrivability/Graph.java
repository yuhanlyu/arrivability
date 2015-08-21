package arrivability;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Undirected graph
 * @author yuhanlyu
 *
 * @param <V>
 */
public class Graph <V extends Comparable<V>> {
	private static final Logger logger = Logger.getLogger(Graph.class.getName());
	private static final int CACHE_SIZE = 100000;
	private Collection<V> vertices = new HashSet<>();                        // all vertices
	private Map<V, Map<V, Double>> neighbors = new HashMap<>();              // adjacent lists
	// unweighted all pairs shortest path distance
	private Map<V, Map<V, Double>> apspUnweightedDistance = null;
	// unweighted all pairs shortes path parent mapping
	private Map<V, Map<V, V>> apspUnweightedParent = null;
	
	/**
	 * Constructor
	 */
	public Graph() {
		reset();
	}
		
	/**
	 * Clear cache data
	 */
	public void reset() {
		apspUnweightedDistance = Collections.synchronizedMap(new LinkedHashMap<V, Map<V, Double>>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry oldest) {
				return size() > CACHE_SIZE;  
			}  
		});
		// unweighted all pairs shortes path parent mapping
		apspUnweightedParent = Collections.synchronizedMap(new LinkedHashMap<V, Map<V, V>>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry oldest) {
			    return size() > CACHE_SIZE;  
			}  
		});
	}
	
	/**
     * Return the vertex set
     * @return the vertex set
     */
    public Collection<V> vertexSet() {
    	return vertices;
    }
    
    /**
     * Test whether a vertex is in the vertex set
     * @param vertex a vertex
     * @return true if the graph contains vertex, false otherwise
     */
    public boolean contains(V vertex) {
    	return vertexSet().contains(vertex);
    }
    
    /**
     * Add a vertex
     * @param vertex the vertex to be added
     */
    public void addVertex(V vertex) {
    	if (contains(vertex))
    		throw new IllegalArgumentException("Insert a duplicate vertex");
    	vertices.add(vertex);
    	neighbors.put(vertex, new HashMap<>());
    }
    
    /**
     * Remove a vertex
     * @param vertex the vertex to be removed
     */
    public void removeVertex(V vertex) {
    	if (!contains(vertex))
    		throw new IllegalArgumentException("Remove a non-existing vertex");
    	for (V neighbor : getNeighbors(vertex)) {
    		neighbors.get(neighbor).remove(vertex);
    	}
    	neighbors.remove(vertex);
    	vertices.remove(vertex);
    }
    
    /**
     * Add an edge between source and target
     * @param source a vertex
     * @param target a vertex
     */
    public void addEdge(V source, V target) {
    	if (!contains(source) || !vertices.contains(target))
    		throw new IllegalArgumentException("Insert an edge between non-existence vertices");
    	neighbors.get(source).put(target, 1.0);
    	neighbors.get(target).put(source, 1.0);
    }
    
    /**
     * Add a new edge
     * @param source source vertex
     * @param target target vertex
     * @param weight weight of the edge
     */
    public void addEdge(V source, V target, double weight) {
    	if (!contains(source) || !contains(target))
    		throw new IllegalArgumentException("Insert an edge between non-existence vertices");
    	neighbors.get(source).put(target, weight);
    	neighbors.get(target).put(source, weight);
    }
    
    /**
     * Test whether source and target are adjacent
     * @param source a vertex
     * @param target a vertex
     * @return true if two vertices are adjacent, false otherwise
     */
    public boolean isAdjacent(V source, V target) {
    	if (!contains(source) || !contains(target))
    		throw new IllegalArgumentException("Test an edge between non-existence vertices");
    	return neighbors.get(source).keySet().contains(target);
    }
    
    /**
     * Return the weight between source and target
     * @param source a vertex
     * @param target a vertex
     * @return the weight between two vertices
     */
    public double getWeight(V source, V target) {
    	if (!contains(source) || !contains(target))
    		throw new IllegalArgumentException("Test an edge between non-existence vertices");
    	return neighbors.get(source).get(target);
    }
    
    /**
     * Return the neighbors of v
     * @param v a vertex
     * @return the neighbors of v
     */
    public Collection<V> getNeighbors(V vertex) {
    	if (!contains(vertex))
    		throw new IllegalArgumentException("Find neighbors of a non-existing vertex");
    	return neighbors.get(vertex).keySet();
    }
    
    /**
     * Return the minimum distance (number of edges) from a vertex set to a target 
     * @param target target point
     * @return the distance from vertex set to the target
     */
    public double unweightedDistance(Iterable<V> vertexset, V target) {
    	for (V v : vertexset) {
    		if (!contains(v))
        		throw new IllegalArgumentException("Path does not exist");
    	}
    	
    	Map<V, Integer> distanceMap = new HashMap<>();
    	Queue<V> queue = new ArrayDeque<>();
    	for (V v : vertexSet()) {
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
    	if (!contains(source) || !contains(target))
    		throw new IllegalArgumentException("Source or target does not exist");
    	if (apspUnweightedDistance.containsKey(source)) {
    		return apspUnweightedDistance.get(source).get(target);
    	}
    	Map<V, Double> distanceMap = new HashMap<>();
    	Queue<V> queue = new ArrayDeque<>();
    	for (V v : vertexSet()) {
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
     * Compute unweighted shoretst paths from source to all vertices
     * @param source source node
     * @param parent mapping of parent
     * @return distance map
     */
    public Map<V, Double> unweightedShortestPath(V source, Map<V, V> parent) {
    	if (!contains(source))
    		throw new IllegalArgumentException("Source or target does not exist");
    	Map<V, Double> distanceMap = new HashMap<>();
    	Queue<V> queue = new ArrayDeque<>();
    	for (V v : vertexSet()) {
    		distanceMap.put(v, Double.POSITIVE_INFINITY);
    	}
    	distanceMap.put(source, 0.0);
    	parent.put(source, null);
    	queue.add(source);
    	
    	while (!queue.isEmpty()) {
    	    V node = queue.poll();
    	    
    	    double newDistance = distanceMap.get(node) + 1.0;
            // Visit each edge exiting u
            for (V neighbor : getNeighbors(node)) {
                
                if (Double.isInfinite(distanceMap.get(neighbor))) {
    		        distanceMap.put(neighbor, newDistance);
    		        queue.add(neighbor);
    		        parent.put(neighbor, node);
    		    }
            }
        }
    	return distanceMap;
    }
    
    /**
     * Path query between two points
     * @param source source point
     * @param target target point
     * @return a path connecting source and target
     */
    public Path<V> pathQuery(V source, V target) {
    	Map<V, V> d = apspUnweightedParent.computeIfAbsent(source, s -> {
    		Map<V, V> parent = new HashMap<>();
    		Map<V, Double> distance = unweightedShortestPath(source, parent);
    		apspUnweightedDistance.put(source, distance);
        	return parent;
    	});
    	return buildPathBackward(source, target, apspUnweightedParent);
    }
    
    /**
     * Distance query between two points
     * @param source source point
     * @param target target point
     * @return distance
     */
    public double distanceQuery(V source, V target) {
    	Map<V, Double> d = apspUnweightedDistance.computeIfAbsent(source, s -> {
    		Map<V, V> parent = new HashMap<>();
        	Map<V, Double> distance = unweightedShortestPath(s, parent);
        	apspUnweightedParent.put(s, parent);
        	return distance;
    	});
    	return d.get(target);
    }
    
    /**
     * Find the shortest distance from source to target with node-weights
     * @param source the source node
     * @param target the target node
     * @return the distance from source to target
     */
    public double shortestDistance(V source, V target, Map<V, Double> vertexWeight) {
    	if (!contains(source) || !contains(target))
    		throw new IllegalArgumentException("Source or target does not exist");
    	Map<V, Double> distanceMap = new HashMap<>();
    	
    	NavigableSet<V> queue = new TreeSet<>(new NodeComparator<>(distanceMap));
    	for (V v : vertexSet()) {
    		distanceMap.put(v, v.equals(source) ? 0.0 : Double.POSITIVE_INFINITY);
    		queue.add(v);
    	}
    	while (!queue.isEmpty()) {
    	    V node = queue.pollFirst();
    	    if (node.equals(target))
    	    	break;
    	    double distance = distanceMap.get(node);
    	    if (Double.isInfinite(distance))
    	    	break;
    	    
            // Visit each edge exiting u
            for (V neighbor : getNeighbors(node)) {
                double newDistance = distance + vertexWeight.get(neighbor);
                
                if (newDistance < distanceMap.get(neighbor)) {
                	queue.remove(neighbor);
    		        distanceMap.put(neighbor, newDistance);
    		        queue.add(neighbor);
    		    }
            }
        }
    	return distanceMap.get(target);
    }
    
    /**
     * A comparator for nodes
     * @author yuhanlyu
     *
     * @param <V>
     */
    private static final class NodeComparator<V extends Comparable<V>> implements Comparator<V> {
    	private final Map<V, Double> distanceMap; // distance from the source
    	
    	/**
    	 * Constructor
    	 * @param map a distance map
    	 */
    	public NodeComparator(Map<V, Double> map) {
    		distanceMap = map;
    	}
    	
		@Override
		public int compare(V o1, V o2) {
			int result = Double.compare(distanceMap.get(o1), distanceMap.get(o2));
        	return result == 0 ? o1.compareTo(o2) : result;
		}
    }
    
    /**
     * Find the shortest distance from source to target with node-weights
     * @param source the source node
     * @param target the target node
     * @return the distance from source to target
     */
    public Path<V> shortestPath(V source, V target, Map<V, Map<V, Double>> edgeWeight) {
    	if (!contains(source) || !contains(target))
    		throw new IllegalArgumentException("Source or target does not exist");
    	Map<V, Double> distanceMap = new HashMap<>();
    	Map<V, V> parentMap = new HashMap<>();
    	
    	NavigableSet<V> queue = new TreeSet<>(new NodeComparator<>(distanceMap));
    	for (V v : vertexSet()) {
    		distanceMap.put(v, v.equals(source) ? 0.0 : Double.POSITIVE_INFINITY);
    		queue.add(v);
    	}
    	parentMap.put(source, null);
    	while (!queue.isEmpty()) {
    	    V node = queue.pollFirst();
    	    if (node.equals(target))
    	    	break;
    	    double distance = distanceMap.get(node);
    	    if (Double.isInfinite(distance))
    	    	break;
    	    
            // Visit each edge exiting u
            for (V neighbor : getNeighbors(node)) {
                double newDistance = distance + edgeWeight.get(node).get(neighbor);
                
                if (newDistance < distanceMap.get(neighbor)) {
                	queue.remove(neighbor);
    		        distanceMap.put(neighbor, newDistance);
    		        queue.add(neighbor);
    		        parentMap.put(neighbor, node);
    		    }
            }
        }
    	Deque<V> stack = new ArrayDeque<>();
		V current = target;
		while (current != null) {
			stack.push(current);
			current = parentMap.get(current);
		}
		Path<V> path = new Path<>();
		while (!stack.isEmpty()) {
			path.addVertex(stack.pop());
		}
		return path;
    }
    
    /**
     * Unweighted all pairs shortest path
     * @param parent parent map
     * @return distance map
     */
    public void unweightedAPSP() {
    	if (apspUnweightedDistance.size() == vertexSet().size())
    		return;
    	logger.info("APSP begins");
    	List<Result> results = vertexSet().parallelStream().map(vertex -> {
    		Map<V, V> p = new HashMap<>();
    		Map<V, Double> d = unweightedShortestPath(vertex, p);
    		return new Result(vertex, d, p);
    	}).collect(Collectors.toList());
    	apspUnweightedDistance = new HashMap<>();
    	apspUnweightedParent = new HashMap<>();
    	for (Result result: results) {
    		apspUnweightedDistance.put(result.vertex, result.distance);
    		apspUnweightedParent.put(result.vertex, result.parent);
    	}
    	logger.info("APSP finished");
    }
    
    /**
     * A class for holding result
     * @author yuhanlyu
     *
     */
    private final class Result {
    	public V vertex;
    	public Map<V, Double> distance;
    	public Map<V, V> parent;
    	
    	/**
    	 * Constructor
    	 * @param v vetex 
    	 * @param d distance mapping
    	 * @param p parent mapping
    	 */
    	public Result(V v, Map<V, Double> d, Map<V, V> p) {
    		vertex = v;
    		distance = d;
    		parent = p;
    	}
    }
    
    /**
     * All pairs shortest path
     * @param next vertex mapping
     * @return distance mapping
     */
    public Map<V, Map<V, Double>> allPairsSP(Map<V, Map<V, V>> next) {
    	Map<V, Map<V, Double>> distance = new HashMap<>();
    	logger.info("APSP begins");
    	for (V u : vertexSet()) {
    		Map<V, Double> distMap = new HashMap<>();
    		distance.put(u, distMap);
    		Map<V, V> nextMap = new HashMap<>();
    		next.put(u, nextMap);
    		for (V v : vertices) {
    			if (u.equals(v)) {
    				distMap.put(v, 0.0);
    				nextMap.put(v,  v);
    			} else if (isAdjacent(u, v)) {
    				distMap.put(v, getWeight(u, v));
    				nextMap.put(v,  v);
    			} else {
    				distMap.put(v, Double.POSITIVE_INFINITY);
    				nextMap.put(v,  null);
    			}
    		}
    	}
    	for (V k : vertexSet()) {
    		for (V i : vertexSet()) {
    			double distanceik = distance.get(i).get(k);
    			if (!Double.isFinite(distanceik))
    				continue;
    			for (V j : vertexSet()) {
    				double distancekj = distance.get(k).get(j);
    				if (!Double.isFinite(distancekj))
    					continue;
    				if (distanceik + distancekj < distance.get(i).get(j)) {
    					distance.get(i).put(j, distanceik + distancekj);
    					next.get(i).put(j, next.get(i).get(k));
    				}
    			}
    		}
    	}
    	logger.info("APSP finished");
    	return distance;
    }
    
    /**
	 * Reconstruct the path from next mapping
	 * @param source source point
	 * @param target target point
	 * @param next next mapping
	 * @return a shortest path from source to target
	 */
	public Path<V> buildPathForward(V source, V target, Map<V, Map<V, V>> next) {
		V current = source;
		Path<V> result = new Path<>();
		result.addVertex(source);
		while (!current.equals(target)) {
			current = next.get(current).get(target);
			result.addVertex(current);
		}
		return result;
	}
	
	/**
	 * Reconstruct the path from parent mapping
	 * @param source source point
	 * @param target target point
	 * @param parent parent mapping
	 * @return a shortest path from source to target
	 */
	public Path<V> buildPathBackward(V source, V target, Map<V, Map<V, V>> parent) {
		Deque<V> stack = new ArrayDeque<>();
		V current = target;
		while (current != null) {
			stack.push(current);
			current = parent.get(source).get(current);
		}
		Path<V> path = new Path<>();
		while (!stack.isEmpty()) {
			path.addVertex(stack.pop());
		}
		return path;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (V vertex: vertexSet()) {
			result.append(vertex.toString() + " ");
		}
		return result.toString();
	}
}
