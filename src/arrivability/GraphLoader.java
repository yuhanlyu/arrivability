package arrivability;

public class GraphLoader {
	private static final int ROW = MineField.ROW;
	private static final int COLUMN = MineField.COLUMN;
	
	/**
	 * Constructor
	 */
	public GraphLoader() {
		
	}
	
	/**
	 * Get a graph
	 * @return a graph
	 */
	public static Graph<Point> getGraph() {
		GridGraph g = new GridGraph(ROW, COLUMN);
		
		for (int i = 0; i < ROW; ++i) {
			if (i % 3 != 0 && i % 3 != 1) {
				Point p = new Point(i, 5);
				g.removeVertex(p);
			}
		}
		
		for (int i = 0; i < ROW; ++i) {
			if (i % 5 != 0 && i % 5 != 1 && i % 5 != 2) {
				Point p = new Point(i, 10);
				g.removeVertex(p);
			}
		}
		
		
		
		for (int i = 0; i < ROW; ++i) {
			if (i % 7 != 0) {
				Point p = new Point(i, 15);
				g.removeVertex(p);
			}
		}
		return g;
	}
}
