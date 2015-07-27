package arrivability;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Class for path.
 * @author yuhanlyu
 *
 * @param <V>
 */
public class Path<V> implements Iterable<V> {

	private Set<V> path = new LinkedHashSet<>();
	private V last;
	
	public Path() {}
	
	/**
	 * Constructor
	 * @param arg_path
	 */
	public Path(Collection<V> arg_path) {
		path.addAll(arg_path);
	}
	
	/**
	 * Append a vertex to the end
	 * @param vertex a vertex
	 */
	public void addVertex(V vertex) {
		path.add(vertex);
		last = vertex;
	}
	
	/**
	 * One time undo
	 */
	public void undo() {
		if (last != null) {
			path.remove(last);
			last = null;
		}
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
		last = null;
	}
	
	@Override
	public Iterator<V> iterator() {
		return path.iterator();
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
