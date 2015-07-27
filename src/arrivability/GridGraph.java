package arrivability;

public class GridGraph extends Graph<Point> {
	
	private int numberOfRows;
	private int numberOfColumns;
	
	/**
	 * Initialize a gridgraph with n rows and m columns
	 * @param n
	 * @param m
	 */
	public GridGraph(int n, int m) {
		numberOfRows = n;
		numberOfColumns = m;
		
		// Initialize vertices
		for (int i = 0; i < numberOfRows; ++i) {
			for (int j = 0; j < numberOfColumns; ++j) {
				addVertex(new Point(i, j));
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
					addEdge(new Point(i, j), new Point(neighbor_row, neighbor_column));
				}
			}
		}
	}
	
	public int getNumberOfRows() {
		return numberOfRows;
	}
	
	public int getNumberOfColumns() {
		return numberOfColumns;
	}
}
