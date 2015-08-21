package arrivability;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

public class FailureGroup <V extends Comparable<V>> {
	
	private static final Logger logger = Logger.getLogger(FailureGroup.class.getName());
	
	private Collection<V> vertices = new HashSet<>();           // all vertices
	private Map<V, Collection<V>> neighbors = new HashMap<>();  // for each vertex, store all forbidden vertices
	
	/**
	 * Default constructor
	 */
	public FailureGroup() { }
	
	public FailureGroup(Graph<V> g, double radius) {
		for (V p : g.vertexSet())
			addVertex(p);
		for (V p : g.vertexSet()) {
			for (V q : g.getNeighbors(p)) {
				addForbidden(p, q);
			}
		}
	}
	
	/**
     * Return the vertexset
     * @return the veretx set
     */
    public Collection<V> vertexSet() {
    	return vertices;
    }
    
    /**
     * Add a vertex
     * @param vertex a new vertex
     */
    public void addVertex(V vertex) {
    	if (vertices.contains(vertex)) {
    		logger.warning("Insert a duplicate vertex");
    		throw new IllegalArgumentException("Insert a duplicate vertex");
    	}
    	vertices.add(vertex);
    	neighbors.put(vertex, new HashSet<>());
    	logger.fine("Add a new vertex " + vertex);
    }
    
    /**
     * Add require vertex to the forbidden set of source
     * @param source source point
     * @param require require point
     */
    public void addForbidden(V source, V require) {
    	if (!vertices.contains(source) || !vertices.contains(require)) {
    		logger.warning("Create forbidden requirement between non-existing vertices");
    		throw new IllegalArgumentException("Create forbidden requirement between non-existing vertices");
    	}
    	neighbors.get(source).add(require);
    	logger.fine("Add a new requirement: " + source + " requires " + require);
    }
    
    /**
     * Return the forbidden area of the vertex
     * @param vertex a vertex
     * @return forbidden area of the vertex
     */
    public Collection<V> getForbiddenArea(V vertex) {
    	if (!vertices.contains(vertex)) {
    		logger.warning("Try to find neighbors of a non-existing vertex");
    		throw new IllegalArgumentException("Try to find neighbors of a non-existing vertex");
    	}
    	return neighbors.get(vertex);
    }
    
    /**
     * Return the minimum distance (number of edges) from a vertex set to a target 
     * @return the mapping of the distance
     */
    public Map<V, Integer> unweightedDistance(Iterable<V> vertexset) {
    	for (V v : vertexset) {
    		if (!vertexSet().contains(v))
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
    	    
    	    int newDistance = distanceMap.get(node) + 1;
            // Visit each edge exiting u
            for (V neighbor : getForbiddenArea(node)) {
                
                if (distanceMap.get(neighbor) == Integer.MAX_VALUE) {
    		        distanceMap.put(neighbor, newDistance);
    		        queue.add(neighbor);
    		    }
            }
        }
    	return distanceMap;
    }
    
    @Override
	public String toString() {
    	StringBuilder result = new StringBuilder("Vertices are: ");
    	for (V v : vertices) {
    		result.append(v.toString() + " ");
    	}
    	result.append("\nForbidden areas are:\n");
    	for (V v: vertices) {
    		result.append(v.toString() + ": ");
    		for (V neighbor : getForbiddenArea(v))
    			result.append(neighbor.toString() + " ");
    		result.append('\n');
    	}
    	return result.toString();
    }
    
    /**
     * Test
     * @param args
     */
    public static void main(String[] args) {
        FailureGroup<Point> g = new FailureGroup<Point>();
        int numberOfRows = 3;
		int numberOfColumns = 3;
		
        for (int i = 0; i < numberOfRows; ++i) {
			for (int j = 0; j < numberOfColumns; ++j) {
				g.addVertex(new Point(i, j));
			}
		}
		
		// Initialize edges
		int[][] direction = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
		for (int i = 0; i < numberOfRows; ++i) {
			for (int j = 0; j < numberOfColumns; ++j) {
				for (int k = 0; k < direction.length; ++k) {
					int neighbor_row = i + direction[k][0];
					int neighbor_column = j + direction[k][1];
					if (neighbor_row >= numberOfRows || neighbor_row < 0)
						continue;
					if (neighbor_column >= numberOfColumns || neighbor_column < 0)
						continue;
					g.addForbidden(new Point(i, j), new Point(neighbor_row, neighbor_column));
				}
			}
		}
		System.out.println(g.toString());
    }
}
