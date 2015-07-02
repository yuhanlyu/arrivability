package arrivability;

/**
 * 2D grid point
 * @author yuhanlyu
 *
 */
public final class Point implements Comparable<Point>{
	
	private int row, column;
	
	/**
	 * Constructor
	 * @param r
	 * @param c
	 */
	public Point(int r, int c) {
		row = r;
		column = c;
	}
	
	/**
	 * Return the row
	 * @return
	 */
	public int getRow() {
		return row;
	}
	
	/**
	 * Return the column
	 * @return
	 */
	public int getColumn() {
		return column;
	}
	
	@Override
	public int compareTo(Point rhs) {
		int compareRow = Integer.compare(row, rhs.getRow());
		return compareRow == 0 ? Integer.compare(column, rhs.getColumn()) : compareRow;
	}
	
	@Override
	public String toString() {
		return "(" + row + " " + column + ")";
	}
	
	@Override
	public int hashCode() {
		return row * (1 << 16) + column;
	}
	
	@Override
	public boolean equals(Object rhs) {
		Point point = (Point)rhs;
		return row == point.getRow() && column == point.getColumn();
	}
}