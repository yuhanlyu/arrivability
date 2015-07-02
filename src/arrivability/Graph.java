package arrivability;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Random;
import java.util.TreeSet;

/**
 * Undirected graph
 * @author yuhanlyu
 *
 * @param <V>
 */
public class Graph<V extends Comparable<V>> {
	
	private Collection<V> vertices = new HashSet<>();                    // all vertices
	private Map<V, Map<V, Double>> neighbors = new HashMap<>();         // adjacent lists
	
	// for all-pairs-shortest-paths
	private Map<V, Map<V, V>> apspNext = null;
	private Map<V, Map<V, Double>> apspDist = null;
	
	// for picking random vertex
	private V[] vertexArray;                   
	private Random random = new Random();
	
	
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
    public Iterable<V> getNeighbors(V v) {
    	return neighbors.get(v).keySet();
    }
    
    /**
     * Increase the weights of edges adjacent to v
     * @param v
     * @param weight
     */
    public void increaseWeight(V v, double weight) {
    	if (!vertices.contains(v))
    		throw new IllegalArgumentException("Vertex does not exist.");
    	for (V neighbor : getNeighbors(v)) {
    		double newWeight = getWeight(v, neighbor) + weight;
    		addEdge(v, neighbor, newWeight);
    	}
    }
    
    /**
     * Reset all edge weights to 1 and clear APSP information
     */
    public void reset() {
    	for (V v : vertices) {
    	    for (V neighbor : getNeighbors(v)) {
    	    	addEdge(v, neighbor, 1.0);
    	    }
    	}
    	apspNext = null;
    	apspDist = null;
    }
    
    /**
     * Reweight the edge between source and target
     * @param source
     * @param target
     * @param weight
     */
    public void reweight(V source, V target, double weight) {
    	if (!vertices.contains(source) || !vertices.contains(target))
    		throw new IllegalArgumentException("Insert an edge between non-existence vertices.");
    	if (!isAdjacent(source, target))
    		throw new IllegalArgumentException("Edge does not exist.");
    	addEdge(source, target, weight);
    	apspNext = null;
    	apspDist = null;
    }
    
    /**
     * Compute the shortest distance (number of edges) from all vertices to a set of vertices
     * @param vertex
     * @param vertices
     * @return
     */
    public Map<V, Integer> unweightedDistance(Iterable<V> vertexset) {
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
    	    
    	    if (distanceMap.get(node) == Integer.MAX_VALUE)
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
    	return distanceMap;
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
    	    
    	    if (node.equals(target) || distanceMap.get(node) == Integer.MAX_VALUE)
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
     * Compute the shortest distance from all vertices to a set of vertices
     * @param vertex
     * @param vertices
     * @return
     */
    public Map<V, Double> distance(Iterable<V> vertexset) {
    	for (V v : vertexset) {
    		if (!vertices.contains(v))
        		throw new IllegalArgumentException("Path does not exist.");
    	}
    	
    	Map<V, Double> distanceMap = new HashMap<>();
    	NavigableSet<V> queue = new TreeSet<>(new Comparator<V>() {
            public int compare(V o1, V o2) {
            	int result = Double.compare(distanceMap.get(o1), distanceMap.get(o2));
            	return result == 0 ? o1.compareTo(o2) : result;
            }});
    	
    	for (V v : vertices) {
    		distanceMap.put(v, Double.POSITIVE_INFINITY);
    	}
    	for (V v : vertexset) {
    		distanceMap.put(v, 0.0);
    		queue.add(v);
    	}
    	
    	while (!queue.isEmpty()) {
    	    V node = queue.pollFirst();
    	    
    	    if (distanceMap.get(node).isInfinite())
    	    	break;
    	    double distance = distanceMap.get(node);
            // Visit each edge exiting u
            for (V neighbor : getNeighbors(node)) {
                double newDistance = distance + getWeight(node, neighbor);
                
                if (newDistance < distanceMap.get(neighbor)) {
                	queue.remove(neighbor);
    		        distanceMap.put(neighbor, newDistance);
    		        queue.add(neighbor);
    		    }
            }
        }
    	return distanceMap;
    }
    
    /**
     * Return the minimum distance from a vertex set to a target 
     * @param target
     * @return
     */
    public double distance(Iterable<V> vertexset, V target) {
    	for (V v : vertexset) {
    		if (!vertices.contains(v))
        		throw new IllegalArgumentException("Path does not exist.");
    	}
    	
    	Map<V, Double> distanceMap = new HashMap<>();
    	NavigableSet<V> queue = new TreeSet<>(new Comparator<V>() {
            public int compare(V o1, V o2) {
            	int result = Double.compare(distanceMap.get(o1), distanceMap.get(o2));
            	return result == 0 ? o1.compareTo(o2) : result;
            }});
    	for (V v : vertices) {
    		distanceMap.put(v, Double.POSITIVE_INFINITY);
    	}
    	for (V v : vertexset) {
    		distanceMap.put(v, 0.0);
    		queue.add(v);
    	}
    	
    	while (!queue.isEmpty()) {
    	    V node = queue.pollFirst();
    	    
    	    if (target.equals(node) || distanceMap.get(node).isInfinite())
    	    	break;
    	    double distance = distanceMap.get(node);
    	    
            // Visit each edge exiting u
            for (V neighbor : getNeighbors(node)) {
                double newDistance = distance + getWeight(node, neighbor);
                
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
     * Find the shortest path from source to target
     * @param source
     * @param target
     * @return
     */
    public Path<V> shortestPath(V source, V target) {
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
    	    	return buildPath(target, parentMap);
            double distance = distanceMap.get(node);
            // Visit each edge exiting u
            for (V neighbor : getNeighbors(node)) {
                double newDistance = distance + getWeight(node, neighbor);
                
                if (newDistance < distanceMap.get(neighbor)) {
                	queue.remove(neighbor);
    		        parentMap.put(neighbor, node);
    		        distanceMap.put(neighbor, newDistance);
    		        queue.add(neighbor);
    		    }
            }
        }
    	throw new IllegalArgumentException("Path does not exist.");
    }
    
    /**
     * Build the shortest path from target to the source, whose parent is null
     * @param target
     * @param parentMap
     * @return
     */
    private Path<V> buildPath(V target, Map<V, V> parentMap) {
    	Deque<V> stack = new ArrayDeque<>();
    	for(V current = target; current != null; current = parentMap.get(current))
    		stack.push(current);
    	Path<V> path = new Path<>();
    	while(!stack.isEmpty()) {
    		path.addVertex(stack.pop());
    	}
    	return path;
    }
    
    /**
     * Generate k paths in the graph from source to target
     * @param source
     * @param target
     * @param k
     * @return
     */
    public Collection<Path<V>> generateKPaths(V source, V target, int k) {
    	if (!vertices.contains(source) || !vertices.contains(target))
    		throw new IllegalArgumentException("Source or target does not exist.");
		ArrayList<Path<V>> result = new ArrayList<>(k);
		for (int i = 0; i < k; ++i) {
			Path<V> path = shortestPath(source, target);
			result.add(path);
			for (V point : path) {
				if (point.equals(source) || point.equals(target))
					continue;
				increaseWeight(point, 2.0);
			}
		}
		result.trimToSize();
		return result;
	}
    
    /**
     * Use RRT to find a randoom path
     * @param source
     * @param target
     * @return
     */
    public Path<V> randomPath(V source, V target) {
    	if (!vertices.contains(source) || !vertices.contains(target))
    		throw new IllegalArgumentException("Source or target does not exist.");
    	if (apspNext == null)
    		computeAPSP();
    	
    	Map<V, V> treeParent = new HashMap<>();
    	treeParent.put(source, null);
    	Collection<V> treeVertices = new ArrayList<>();
    	while (true) {
    		V r;
    		// Pick a random non-tree vertex
    		do {
    			r = randomVertex();
    		} while (treeParent.keySet().contains(r));
    		
    		//System.out.println("Random vertex is " + r.toString());
    		// Pick a tree vertex that is closest to r
    		V node = closest(treeParent.keySet(), r);
    		//System.out.println("Closest vertex is " + node.toString());
    		
    		// Grow the tree
    		int step = 2;
    		grow(node, r, step, treeParent, treeVertices);
    		
    		if (treeParent.keySet().contains(target))
    			break;
    	}
    	return buildPath(target, treeParent);
    }
    

    /**
     * Grow RRT from node toward r with 'step' steps
     * @param node
     * @param r
     * @param step
     * @param treeParent
     * @param treeVertices
     */
	private void grow(V node, V r, int step, Map<V, V> treeParent, Collection<V> treeVertices) {
		V current = node;
		for (int i = 0; i < step; ++i) {
			V next = apspNext.get(current).get(r);
			if (treeParent.get(next) == null) {
				treeParent.put(next, current);
				treeVertices.add(next);
				//System.out.println("Add " + next.toString());
			}
			if (next.equals(r))
				break;
			current = next;
		}
	}

	/**
	 * Find a vertex on tree with minimum distance to r
	 * @param treeVertices
	 * @param r
	 * @return
	 */
	private V closest(Iterable<V> treeVertices, V r) {
		V result = null;
		double minDist = Double.POSITIVE_INFINITY;
		for (V v : treeVertices) {
			if (getDistance(v, r) < minDist) {
				result = v;
				minDist = getDistance(v, r);
			}
		}
		return result;
    }
    
	/**
	 * Choose a random vertex
	 * @return
	 */
    public V randomVertex() {
    	if (vertexArray == null)
    		vertexArray = vertices.toArray((V[])new Comparable[0]);
    	return vertexArray[random.nextInt(vertexArray.length)];
    }
    
    /**
     * Compute all-pairs-shortest-paths by Floyd-Warshall algorithm
     */
    private void computeAPSP() {
    	System.out.println("APSP initialization");
    	apspDist = new HashMap<>();
    	apspNext = new HashMap<>();
    	for (V u : vertices) {
    		Map<V, Double> distMap = new HashMap<>();
    		apspDist.put(u, distMap);
    		Map<V, V> nextMap = new HashMap<>();
    		apspNext.put(u, nextMap);
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
    	System.out.println("APSP begins");
    	for (V k : vertices) {
    		for (V i : vertices) {
    			if (!Double.isFinite(getDistance(i, k)))
    				continue;
    			for (V j : vertices) {
    				if (!Double.isFinite(getDistance(k, j)))
    					continue;
    				if (getDistance(i, k) + getDistance(k, j) < getDistance(i, j)) {
    					apspDist.get(i).put(j, getDistance(i, k) + getDistance(k, j));
    					apspNext.get(i).put(j, apspNext.get(i).get(k));
    				}
    			}
    		}
    	}
    	System.out.println("APSP finished");
    }
    
    /**
     * Retrieve the distance during the computation of all-pairs-shortest-path
     * @param source
     * @param target
     * @return
     */
    private double getDistance(V source, V target) {
    	return apspDist.get(source).get(target);
    }
    
    @Override
	public String toString() {
    	StringBuilder result = new StringBuilder("Vertices are: ");
    	for (V v : vertices) {
    		result.append(v.toString() + " ");
    	}
    	result.append("\nEdges are:\n");
    	for (V v: vertices) {
    		result.append(v.toString() + ": ");
    		for (V neighbor : getNeighbors(v))
    			result.append(neighbor.toString() + " ");
    		result.append('\n');
    	}
    	return result.toString();
    }
}
