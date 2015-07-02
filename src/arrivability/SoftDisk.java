package arrivability;

import java.util.Collection;
import java.util.List;

public class SoftDisk extends Arrivability {

	public SoftDisk(int n, int m) {
		super(n, m);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double arrivability(Iterable<Point> vertexset) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Collection<Point> forbiddenArea(Iterable<Point> vertexset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double heuristic(List<Path<Point>> vertexset, Point target) {
		// TODO Auto-generated method stub
		return 0;
	}

}
