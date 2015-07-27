package arrivability;

import java.awt.geom.Point2D;

/**
 * 2D grid point
 * @author yuhanlyu
 *
 */
public final class Point implements Comparable<Point> {
	
	private Point2D.Double point;
	
	/**
	 * Constructor
	 * @param x x-coordinate
	 * @param y y-coordinate
	 */
	public Point(double x, double y) {
		point = new Point2D.Double(x, y);
	}
	
	/**
	 * Return the y-coordinate
	 * @return y-coordinate
	 */
	public double getY() {
		return point.getY();
	}
	
	/**
	 * Return the x-coordinate
	 * @return x-coordinate
	 */
	public double getX() {
		return point.getX();
	}
	
	@Override
	public int compareTo(Point rhs) {
		int compareRow = Double.compare(getY(), rhs.getY());
		return compareRow == 0 ? Double.compare(getX(), rhs.getX()) : compareRow;
	}
	
	@Override
	public String toString() {
		return "(" + getX() + " " + getY() + ")";
	}
	
	@Override
	public int hashCode() {
		return point.hashCode();
	}
	
	@Override
	public boolean equals(Object rhs) {
		return point.equals(((Point)rhs).point);
	}
}