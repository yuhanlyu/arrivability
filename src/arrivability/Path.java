package arrivability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class for path.
 * @author yuhanlyu
 *
 * @param <V>
 */
public class Path<V> implements Iterable<V> {

	private Set<V> path = new HashSet<>();
	private List<V> points = new ArrayList<>();
	
	public Path() {}
	
	/**
	 * Constructor
	 * @param points all points in the path in order
	 */
	public Path(V[] points) {
		for (V point : points)
			this.addVertex(point);
	}
	
	/**
	 * Constructor
	 * @param arg_path
	 */
	public Path(Collection<V> arg_path) {
		for (V point : arg_path)
			this.addVertex(point);
	}
	
	/**
	 * Append a vertex to the end
	 * @param vertex a vertex
	 */
	public void addVertex(V vertex) {
		path.add(vertex);
		points.add(vertex);
	}
	
	/**
	 * Test whether path contains a vertex 
	 * @param vertex a vertex
	 * @return true if path contains vertex, false otherwise
	 */
	public boolean contains(V vertex) {
		return path.contains(vertex);
	}
	
	/**
	 * Test whether two paths overlap
	 * @param arg_path another path
	 * @return true if two paths are overlapped, false otherwise
	 */
	public boolean contains(Path<V> arg_path) {
		for (V point : arg_path)
			if (path.contains(point))
				return true;
		return false;
	}
	
	/**
	 * Return a collection containing all elements in path
	 * @return a collection containing all elements in path
	 */
	public Collection<V> toCollection() {
		return path;
	}
	
	/**
	 * Duplicate the current path
	 * @return a duplicated path
	 */
	public Path<V> duplicate() {
		return new Path<>(path);
	}
	
	/**
	 * Clear all information
	 */
	public void clear() {
		path.clear();
	}
	
	/**
	 * Return the length of the path
	 * @return the length
	 */
	public int size() {
		return points.size();
	}
	
	/**
	 * Get the index-th point
	 * @param index the index
	 * @return the index-th point
	 */
	public V get(int index) {
		return points.get(index);
	}
	
	/**
	 * Get a sub path [begin, end)
	 * @param begin the index of begin point
	 * @param end index
	 * @return a subpath
	 */
	public Path<V> slice(int begin, int end) {
		int i = 0;
		Path<V> result = new Path<V>();
		for (V point : points) {
			if (begin <= i && i < end) {
				result.addVertex(point);
			}
			++i;
		}
		return result;
	}
	
	/**
	 * Concatenate a path in the end
	 * @param path
	 */
	public void concate(Path<V> path) {
		for (V point: path)
			addVertex(point);
	}
	
	
	@Override
	public Iterator<V> iterator() {
		return points.iterator();
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (V node : path) {
			result.append(node.toString() + " ");
		}
		return result.toString();
	}
}
