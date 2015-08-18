package arrivability;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
		GridGraph g = new GridGraph(ROW, COLUMN);
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
	
	/**
	 * Get a graph from a file
	 * @param filename name of the file
	 * @return a graph
	 */
	public static Graph<Point> getGraph(String filename) {
		Charset charset = Charset.forName("US-ASCII");
		Path file = FileSystems.getDefault().getPath(".", filename);
		try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
		    String[] line = reader.readLine().split(" ");
		    int row = Integer.parseInt(line[0]), column = Integer.parseInt(line[1]);
		    GridGraph g = new GridGraph(row, column);
		    int numberOfObstacles = Integer.parseInt(reader.readLine());
		    Polygon[] obstacles = new Polygon[numberOfObstacles];
		    for (int i = 0; i < numberOfObstacles; ++i) {
		    	int size = Integer.parseInt(reader.readLine());
		    	int[] x = new int[size], y = new int[size];
		    	for (int j = 0; j < size; ++j) {
		    		line = reader.readLine().split(" ");
		    		x[j] = Integer.parseInt(line[0]);
		    		y[j] = Integer.parseInt(line[1]);
		    	}
		    	obstacles[i] = new Polygon(x, y, size);
		    }
		    for (int i = 0; i < row; ++i) {
		    	for (int j = 0; j < column; ++j) {
		    		Point p = new Point(i, j);
		    		boolean isBlocked = false;
		    		for (int k = 0; k < obstacles.length; ++k) {
		    			if (obstacles[k].contains(p.getX(), p.getY())) {
		    				isBlocked = true;
		    				break;
		    			}
		    		}
		    		if (isBlocked) {
		    			g.removeVertex(p);
		    		}
		    	}
		    }
		    return g;
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
		return null;
	}
	
	public static void main(String[] args) {
		GraphLoader.getGraph("files/map");
	}
}
