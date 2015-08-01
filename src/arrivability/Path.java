package arrivability;

import java.util.ArrayList;
import java.util.Arrays;
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
		return new Path<>(points);
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
		Path<V> result = new Path<>();
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
	 * @param p another path
	 */
	public void concate(Path<V> p) {
		for (V point: p)
			addVertex(point);
	}
	
	/**
	 * Clone a list of paths
	 * @param paths a list of paths to be cloned
	 * @return a duplication of the input paths
	 */
	public static List<Path<Point>> clonePaths(List<Path<Point>> paths) {
		Path<Point>[] duplicates = new Path[paths.size()];
		for (int i = 0; i < paths.size(); ++i)
			duplicates[i] = paths.get(i).duplicate();
		return Arrays.asList(duplicates);
	}
	
	@Override
	public boolean equals(Object obj) {
		Path<V> rhs = (Path<V>)obj;
		if (rhs.size() != size())
			return false;
		for (int i = 0; i < size(); ++i)
			if (!get(i).equals(rhs.get(i)))
				return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = 0;
		for (V point : points) {
			result <<= 1;
			result ^= point.hashCode();
		}
		return result;
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
