package arrivability;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
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
	public static Graph<Point> Hanover(String filename, int numberOfRows, int numberOfColumns) {
		Charset charset = Charset.forName("UTF-8");
		Path file = FileSystems.getDefault().getPath(".", filename);
		Map<Long, Point2D.Double> nodeMap = new HashMap<>();
		Set<List<Long>> buildings = new HashSet<>();
		Map<Long, Point2D.Double> resultNodeMap = new HashMap<>();
		try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
			System.setProperty("jdk.xml.entityExpansionLimit", "0");
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			XMLEventReader read = inputFactory.createXMLEventReader(reader);
			while(read.hasNext()) {
				XMLEvent e = read.nextEvent();
				if (e.isStartElement()) {
					StartElement element = e.asStartElement();
					if (element.getName().getLocalPart().equals("node")) {
						parseNode(nodeMap, element);
					} else if (element.getName().getLocalPart().equals("way")){
						parseBuilding(nodeMap, buildings, resultNodeMap, read);
					}
				}
			}
			return buildGraph(numberOfRows, numberOfColumns, resultNodeMap, buildings);
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Parse for a way
	 * @param nodeMap mapping between id and points
	 * @param buildings list of ways
	 * @param resultNodeMap nodes that are used in ways
	 * @param read current parsing position
	 * @throws XMLStreamException
	 */
	private static void parseBuilding(Map<Long, Point2D.Double> nodeMap, Set<List<Long>> buildings,
			Map<Long, Point2D.Double> resultNodeMap, XMLEventReader read) throws XMLStreamException {
		XMLEvent ee = read.nextEvent();
		List<Long> way = new ArrayList<>();
		boolean isBuilding = false;
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
								isBuilding = true;
					}
				}
				ee = read.nextEvent();
			}
		}
		if (isBuilding && way.size() > 0) {
			buildings.add(way);
		}
	}

	/**
	 * Parse for a node in a map
	 * @param nodeMap store information of nodes
	 * @param element parsing position
	 */
	private static void parseNode(Map<Long, Point2D.Double> nodeMap, StartElement element) {
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
		}
		if (id != 0 && MIN_LON <= lon && lon <= MAX_LON && MIN_LAT <= lat && lat <= MAX_LAT)
			nodeMap.put(id, new Point2D.Double(lon, lat));
	}
	
	/**
	 * 
	 * @param nodeMap
	 * @param buildings
	 * @param st
	 * @return
	 */
	public static Graph<Point> buildGraph(int numberOfRows, int numberOfColumns, Map<Long, Point2D.Double> nodeMap, Set<List<Long>> buildings) {
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
		double xFactor = numberOfColumns / (maxlon - minlon), yFactor = numberOfRows / (maxlat - minlat);
		//double xFactor = 10 / (maxlon - minlon), yFactor = 10 / (maxlat - minlat);
		Map<Long, Point> vertexMap = new HashMap<>();
		// Latitude ~ y ~ row
		// Longitude ~ x ~ column
		// Construct mapping between id and points
		for (Long id : nodeMap.keySet()) {
			Point2D.Double p = nodeMap.get(id);
			Point point = new Point((maxlat - p.getY()) * yFactor, (p.getX() - minlon) * xFactor);
			//Point point = new Point((p.getX() - minlon) * xFactor, (p.getY() - minlat) * yFactor);
			vertexMap.put(id, point);
		}
		// Construct all obstacles in terms of row and columns
		Area blocked = new Area();
		System.out.println(buildings.size());
		for (List<Long> building : buildings) {
			Path2D.Double obstacle = new Path2D.Double();
			//List<Point2D.Double> path = new ArrayList<>();
			obstacle.moveTo(vertexMap.get(building.get(0)).getX(), vertexMap.get(building.get(0)).getY());
			//path.add(new Point2D.Double(vertexMap.get(building.get(0)).getX(), vertexMap.get(building.get(0)).getY()));
			for (int i = 1; i < building.size(); ++i) {
				obstacle.lineTo(vertexMap.get(building.get(i)).getX(), vertexMap.get(building.get(i)).getY());
				//path.add(new Point2D.Double(vertexMap.get(building.get(i)).getX(), vertexMap.get(building.get(i)).getY()));
			}
			obstacle.closePath();
			blocked.add(new Area(obstacle));
			//System.out.println(path.size());
			//for (Point2D.Double p : path) {
			//    System.out.println(String.format("%f %f", p.getX() - 5, p.getY() - 5));
			//}
		}
		GridGraph g = new GridGraph(numberOfRows, numberOfColumns);
		for (int i = 0; i < numberOfRows; ++i) {
	    	for (int j = 0; j < numberOfColumns; ++j) {
	    		if (blocked.contains(i, j)) {
	    			g.removeVertex(new Point(i, j));
	    		}
	    	}
	    }
		return g;
	}
	
	public static void main(String[] args) {
		//GraphLoader.getGraph("files/map");
		GraphLoader.Hanover("files/Hanover.osm", 100, 100);
	}
}
