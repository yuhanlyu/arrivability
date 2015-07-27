package arrivability;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class SharedRiskNodeGroup extends Arrivability {

	private Collection<Collection<Point>> groups = new LinkedHashSet<>();
	
	/**
	 * Add one vertex
	 * @param vertex
	 */
	public void addVertex(Point vertex) {
		g.addVertex(vertex);
	}
	
	/**
	 * Add one edge
	 * @param source
	 * @param target
	 */
	public void addEdge(Point source, Point target) {
		g.addEdge(source, target);
	}
	
	/**
	 * Add one group
	 * @param group
	 */
	public void addGroup(Collection<Point> group) {
		groups.add(group);
	}

	@Override
	protected double arrivability(Iterable<Point> vertexset) {
		// TODO Auto-generated method stub
		return 0;
	}
}
