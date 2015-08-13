package arrivability;

import java.util.ArrayList;
import java.util.List;

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
		List<int[]> direction = getDirections(1);
		for (int i = 0; i < numberOfRows; ++i) {
			for (int j = 0; j < numberOfColumns; ++j) {
				for (int k = 0; k < direction.size(); ++k) {
					int neighbor_row = i + direction.get(k)[0];
					int neighbor_column = j + direction.get(k)[1];
					if (neighbor_row >= numberOfRows || neighbor_row < 0)
						continue;
					if (neighbor_column >= numberOfColumns || neighbor_column < 0)
						continue;
					addEdge(new Point(i, j), new Point(neighbor_row, neighbor_column));
				}
			}
		}
	}
	
	/**
	 * Compute the directions
	 * @param radius radius of the bomb
	 * @return all directions
	 */
	private static List<int[]> getDirections(int radius) {
		List<int[]> result = new ArrayList<>();
		for (int x = -radius; x <= radius; ++x) {
			for (int y = -radius; y <= radius; ++y) {
				if (0 < x * x + y * y && x * x + y * y <= radius)
					result.add(new int[]{x, y});
			}
		}
		return result;
	}
	
	/**
	 * Get the number of rows
	 * @return the number of rows
	 */
	public int getNumberOfRows() {
		return numberOfRows;
	}
	
	/**
	 * Get the number of Columns
	 * @return the number of columns
	 */
	public int getNumberOfColumns() {
		return numberOfColumns;
	}
}
