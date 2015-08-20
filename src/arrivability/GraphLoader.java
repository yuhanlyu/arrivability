package arrivability;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;

public class GraphLoader {
	private static final int ROW = MineField.ROW;
	private static final int COLUMN = MineField.COLUMN;
	private static final int RADIUS = 1;
	private static final double MIN_LON = -72.2956;
	private static final double MAX_LON = -72.2827;
	private static final double MIN_LAT = 43.6994;
	private static final double MAX_LAT = 43.7100;
	/*
	private static final double MIN_LON = -72.29044;
	private static final double MAX_LON = -72.29017;
	private static final double MIN_LAT = 43.70311;
	private static final double MAX_LAT = 43.70335;
	*/
	
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
	
	/**
	 * Parse Hanover's street map
	 * @param filename a file storing osm
	 * @return a Graph
	 */
	public static Graph<Point> Hanover(String filename, Point[] st) {
		Charset charset = Charset.forName("UTF-8");
		Path file = FileSystems.getDefault().getPath(".", filename);
		double minlon = 0, maxlon = 0, minlat = 0, maxlat = 0;
		Map<Long, Point2D.Double> nodeMap = new HashMap<>();
		Set<List<Long>> ways = new HashSet<>();
		Map<Long, Point2D.Double> resultNodeMap = new HashMap<>();
		try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
			System.setProperty("jdk.xml.entityExpansionLimit", "0");
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			XMLEventReader read = inputFactory.createXMLEventReader(reader);
			while(read.hasNext()) {
				XMLEvent e = read.nextEvent();
				if (e.isStartElement()) {
					StartElement element = e.asStartElement();
					if (element.getName().getLocalPart().equals("bounds")) {
						Iterator it = element.getAttributes();
						while (it.hasNext()) {
							Attribute att = (Attribute) it.next();
							if (att.getName().toString().equals("minlon"))
								minlon = Double.parseDouble(att.getValue());
							else if (att.getName().toString().equals("maxlat"))
								maxlat = Double.parseDouble(att.getValue());
							else if (att.getName().toString().equals("minlat"))
								minlat = Double.parseDouble(att.getValue());
							else if (att.getName().toString().equals("maxlon"))
								maxlon = Double.parseDouble(att.getValue());
						}
					} else if (element.getName().getLocalPart().equals("node")) {
						Iterator<?> it = element.getAttributes();
						double lon = 0, lat = 0;
						long id = 0;
						while (it.hasNext()) {
							Attribute att = (Attribute) it.next();
							if (att.getName().toString().equals("lon"))
								lon = Double.parseDouble(att.getValue());
							else if (att.getName().toString().equals("lat"))
								lat = Double.parseDouble(att.getValue());
							else if (att.getName().toString().equals("id"))
								id = Long.parseLong(att.getValue());
							else if (att.getName().toString().equals("user"))
								//if (!att.getValue().equals("woodpeck_fixbot") &&
								//	!att.getValue().equals("lewis_pusey"))
								//	id = 0;
								;
						}
						if (id != 0 && MIN_LON <= lon && lon <= MAX_LON && MIN_LAT <= lat && lat <= MAX_LAT)
							nodeMap.put(id, new Point2D.Double(lon, lat));
					} else if (element.getName().getLocalPart().equals("way")){
						XMLEvent ee = read.nextEvent();
						List<Long> way = new ArrayList<>();
						boolean flag = true;
						while (read.hasNext()) {
							ee = read.nextEvent();
							if (ee.isEndElement())
								break;
							if (ee.isStartElement()) {
								StartElement elm2 = ee.asStartElement();
								if (elm2.getName().getLocalPart().equals("nd")) {
									Iterator<?> it = elm2.getAttributes();
									long id = 0;
									while (it.hasNext()) {
										Attribute att = (Attribute) it.next();
										if (att.getName().toString().equals("ref"))
											id = Long.parseLong(att.getValue());
									}
									if (nodeMap.containsKey(id)) {
										way.add(id);
										resultNodeMap.put(id, nodeMap.get(id));
									}
								} else if (elm2.getName().getLocalPart().equals("tag")) {
									Iterator<?> it = elm2.getAttributes();
									while (it.hasNext()) {
										Attribute att = (Attribute) it.next();
										if (att.getName().toString().equals("k"))
											if (att.getValue().equals("building"))
												flag = false;
												//flag = true;
									}
								}
								ee = read.nextEvent();
							}
						}
						if (flag)
							ways.add(way);
					}
				}
			}
			return buildGraph(resultNodeMap, ways, st);
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Graph<Point> buildGraph(Map<Long, Point2D.Double> nodeMap, Set<List<Long>> ways, Point[] st) {
		Graph<Point> g = new Graph<>();
		double minlon = Double.POSITIVE_INFINITY, maxlon = Double.NEGATIVE_INFINITY;
		double minlat = Double.POSITIVE_INFINITY, maxlat = Double.NEGATIVE_INFINITY;
		for (Point2D.Double p : nodeMap.values()) {
			if (p.getX() > maxlon)
				maxlon = p.getX();
			if (p.getX() < minlon)
				minlon = p.getX();
			if (p.getY() > maxlat)
				maxlat = p.getY();
			if (p.getY() < minlat)
				minlat = p.getY();
		}
		double xFactor = 100 / (maxlon - minlon), yFactor = 100 / (maxlat - minlat);
		Map<Long, Point> vertexMap = new HashMap<>();
		Map<Point, Long> inverse = new HashMap<>();
		for (Long id : nodeMap.keySet()) {
			Point2D.Double p = nodeMap.get(id);
			Point point = new Point((maxlat - p.getY()) * yFactor, (p.getX() - minlon) * xFactor);
			if (!inverse.containsKey(point)) {
				g.addVertex(point);
			}
			vertexMap.put(id, point);
			inverse.put(point,  id);
		}
		for (List<Long> way : ways) {
			for (int i = 0; i < way.size() - 1; ++i) {
				Point p1 = vertexMap.get(way.get(i));
				Point p2 = vertexMap.get(way.get(i + 1));
				g.addEdge(p1, p2);
				g.addEdge(p2, p1);
			}
		}
		for (Point p : new HashSet<Point>(g.vertexSet())) {
			if (g.getNeighbors(p).size() == 0)
				g.removeVertex(p);
		}
		for (Long id : nodeMap.keySet()) {
			System.out.println(id + " " + nodeMap.get(id));
		}
		st[0] = vertexMap.get(194783106L);
		st[1] = vertexMap.get(194703843L);
		return g;
	}
	
	public static void main(String[] args) {
		//GraphLoader.getGraph("files/map");
		GraphLoader.Hanover("files/Hanover.osm", new Point[2]);
	}
}
