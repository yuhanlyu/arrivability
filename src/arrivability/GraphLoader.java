package arrivability;

import java.util.Random;

public class GraphLoader {
	private static final int ROW = MineField.ROW;
	private static final int COLUMN = MineField.COLUMN;
	private static final int RADIUS = 1;
	
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
		GridGraph g = new GridGraph(ROW, COLUMN, RADIUS);
		/*
		Random random = new Random();
		for (int j = 5; j < COLUMN; j += 5) {
			int gap1 = random.nextInt(ROW / 10) + 1;
			int gap2 = random.nextInt(ROW / 10) + 1;
			for (int i = 0; i < ROW; ++i) {
				if (i % gap1 != 0 && i % gap2 != 0) {
					Point p = new Point(i, j);
					g.removeVertex(p);
				}
			}	
		}*/
		return g;
	}
}
