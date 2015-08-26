package arrivability;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ResultWriter {
	private GridGraph graph;
	private FailureRate fr;
	
	/**
	 * Constructor
	 * @param arg_graph a graph
	 * @param arg_fr failure rate
	 */
	public ResultWriter(GridGraph arg_graph, FailureRate arg_fr) {
		graph = arg_graph;
		fr = arg_fr;
	}
	
	/**
	 * Write to a file
	 * @param source source point
	 * @param target target point
	 * @param paths paths between source and target
	 * @param filename filename to be outputted
	 */
	public void write(int ROW, int COLUMN, Point source, Point target, List<arrivability.Path<Point>> paths, String filename) {
		Path file = FileSystems.getDefault().getPath(".", filename);
		try (PrintStream writer = new PrintStream(Files.newOutputStream(file, StandardOpenOption.CREATE))) {
			writer.println(ROW + " " + COLUMN);
			writer.println(graph.vertexSet().size());
			writer.println(graph.toString());
			writer.println(source.toString());
			writer.println(target.toString());
			writer.println(paths.size());
			for (arrivability.Path<Point> p : paths) {
				writer.println(p.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
