package arrivability;

public class GridFailureGroup extends FailureGroup<Point> {

	/**
	 * Construct a grid environment
	 * @param n the number of rows
	 * @param m the number of columns
	 */
	public GridFailureGroup(int n, int m) {
		int numberOfRows = n;
		int numberOfColumns = m;
		
        for (int i = -1; i <= numberOfRows; ++i) {
			for (int j = -1; j <= numberOfColumns; ++j) {
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
					addForbidden(new Point(i, j), new Point(neighbor_row, neighbor_column));
					if (neighbor_row < 0 || neighbor_row == numberOfRows
					 || neighbor_column < 0 || neighbor_column == numberOfColumns)
						addForbidden(new Point(neighbor_row, neighbor_column), new Point(i, j));
				}
			}
		}
	}
	
	public static void main(String[] args) {
		GridFailureGroup g = new GridFailureGroup(3, 3);
		System.out.println(g.toString());
	}
}
