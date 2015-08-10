package arrivability;

import java.util.ArrayList;
import java.util.List;

public class GridFailureGroup extends FailureGroup<Point> {

	/**
	 * Construct a grid environment
	 * @param n the number of rows
	 * @param m the number of columns
	 */
	public GridFailureGroup(int n, int m, int radius) {
		int numberOfRows = n;
		int numberOfColumns = m;
		
        for (int i = -radius; i < numberOfRows + radius; ++i) {
			for (int j = -radius; j < numberOfColumns + radius; ++j) {
				addVertex(new Point(i, j));
			}
		}
		
		// Initialize edges
		List<int[]> direction = getDirections(radius);
		for (int i = 0; i < numberOfRows; ++i) {
			for (int j = 0; j < numberOfColumns; ++j) {
				for (int k = 0; k < direction.size(); ++k) {
					int neighbor_row = i + direction.get(k)[0];
					int neighbor_column = j + direction.get(k)[1];
					addForbidden(new Point(i, j), new Point(neighbor_row, neighbor_column));
					if (neighbor_row < 0 || neighbor_row == numberOfRows
					 || neighbor_column < 0 || neighbor_column == numberOfColumns)
						addForbidden(new Point(neighbor_row, neighbor_column), new Point(i, j));
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
	
	public static void main(String[] args) {
		GridFailureGroup g = new GridFailureGroup(3, 3, 1);
		System.out.println(g.toString());
	}
}
