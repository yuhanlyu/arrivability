package arrivability;

import java.util.List;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Benchmark extends ConsoleHandler {
	
	private static int ROW = 50;
	private static int COLUMN = 50;
	private static int NUMBER_OF_BLOCKERS = 10;
	private static double FAILURE_PROBABILITY = (double)NUMBER_OF_BLOCKERS / (ROW * COLUMN);
	private static int MINE_RADIUS = 1;
	private static int NUMBER_OF_ROBOTS = 3;
	private static int NUMBER_OF_REQUEST = 1;
	private static int NUMBER_OF_GENERATE = 100;
	private static int NUMBER_OF_ITERATIONS = 3;
	
	private static boolean[] modeForSelection = new boolean[PathSelection.NUMBER_OF_MODE];
	private static boolean[] modeForGeneration = new boolean[PathGeneration.NUMBER_OF_MODE];
	
	private static double time[][][] = new double[PathGeneration.NUMBER_OF_MODE][PathSelection.NUMBER_OF_MODE][6];
	private static double quality[][][] = new double[PathGeneration.NUMBER_OF_MODE][PathSelection.NUMBER_OF_MODE][3];
	
	private static int generationTime;
	private static int initialSolutionTime;
	private static int localImprovementTime;
	private static int pathSelectionTime;
	private static int pathImprovementTime;
	private static int totalTime;
	private static double preLocalQuality;
	private static double aftLocalQuality;
	
	@Override
	public void publish(LogRecord logRecord) {
		try (Scanner scanner = new Scanner(logRecord.getMessage())) {
			if (logRecord.getMessage().startsWith("Path generation takes ")) {
				scanner.skip("Path generation takes ");
				generationTime = scanner.nextInt();
			} else if (logRecord.getMessage().startsWith("Initial solution takes ")) {
				scanner.skip("Initial solution takes ");
				initialSolutionTime = scanner.nextInt();
			} else if (logRecord.getMessage().startsWith("Local improvementn takes ")) {
				scanner.skip("Local improvementn takes ");
				localImprovementTime = scanner.nextInt();
			} else if (logRecord.getMessage().startsWith("Path selection takes ")) {
				scanner.skip("Path selection takes ");
				pathSelectionTime = scanner.nextInt();
			} else if (logRecord.getMessage().startsWith("Path improvement takes ")) {
				scanner.skip("Path improvement takes ");
				pathImprovementTime = scanner.nextInt();
			} else if (logRecord.getMessage().startsWith("Search completed in ")) {
				scanner.skip("Search completed in ");
				totalTime = scanner.nextInt();
			} else if (logRecord.getMessage().startsWith("Arrivability before path selection ")) {
				scanner.skip("Arrivability before path selection ");
				preLocalQuality = scanner.nextDouble();
			} else if (logRecord.getMessage().startsWith("Arrivability after local improvement ")) {
				scanner.skip("Arrivability after local improvement ");
				aftLocalQuality = scanner.nextDouble();
			}
		}
    }
	
	public void getResult() {
		System.out.println("Time = " + generationTime + " " + initialSolutionTime + " " + localImprovementTime
				           + " " + pathSelectionTime + " " + pathImprovementTime + " " + totalTime);
	}
	
	public static void demo_RandomRadius() {
	    // variables specific to this demo
		//int ROW = 30;
		//int COLUMN = 30;
		//int NUMBER_OF_BLOCKERS = 2;
		int NUMBER_OF_ROBOTS = 2;
		double FAILURE_PROBABILITY = (double)0.1 / (ROW * COLUMN);
		int MINE_RADIUS = 5;
		
		Benchmark bm = new Benchmark();
	    Logger.getLogger("").addHandler(bm);
	    GridFailureGroup fg = new GridFailureGroup(ROW, COLUMN, MINE_RADIUS);
		Graph<Point> g = GraphLoader.getGraph();
		FailureRate model = new RandomRadius(fg, g, FAILURE_PROBABILITY, MINE_RADIUS);
		MaximizeArrivability ma = new MaximizeArrivability(g, model, NUMBER_OF_ROBOTS, NUMBER_OF_REQUEST, NUMBER_OF_GENERATE, 0, 0, NUMBER_OF_ITERATIONS);
		Point source = new Point(ROW / 2, 0);
		Point target = new Point(ROW / 2, COLUMN - 1);
		
		List<Path<Point>> paths = ma.getSolution(source, target);
		bm.getResult();
		String FILENAME = "demos/demo_RandomRadius";
		ResultWriter writer = new ResultWriter((GridGraph)g, model);
		writer.write(ROW, COLUMN, source, target, paths, FILENAME);
	}
	
	public static void demo_FixedRadius() {
	    // variables specific to this demo
		//int ROW = 30;
		//int COLUMN = 30;
		//int NUMBER_OF_BLOCKERS = 2;
		int NUMBER_OF_ROBOTS = 2;
		double FAILURE_PROBABILITY = (double)0.1 / (ROW * COLUMN);
		int MINE_RADIUS = 5;
		
		Benchmark bm = new Benchmark();
	    Logger.getLogger("").addHandler(bm);
	    GridFailureGroup fg = new GridFailureGroup(ROW, COLUMN, MINE_RADIUS);
		Graph<Point> g = GraphLoader.getGraph();
		FailureRate model = new FixedRadius(fg, g, FAILURE_PROBABILITY);
		MaximizeArrivability ma = new MaximizeArrivability(g, model, NUMBER_OF_ROBOTS, NUMBER_OF_REQUEST, NUMBER_OF_GENERATE, 0, 0, NUMBER_OF_ITERATIONS);
		Point source = new Point(ROW / 2, 0);
		Point target = new Point(ROW / 2, COLUMN - 1);
		
		List<Path<Point>> paths = ma.getSolution(source, target);
		bm.getResult();
		String FILENAME = "demos/demo_FixedRadius";
		ResultWriter writer = new ResultWriter((GridGraph)g, model);
		writer.write(ROW, COLUMN, source, target, paths, FILENAME);
	}
	
	public static void demo_MakeBlockGraph() {
	    // variables specific to this demo
		int NUMBER_OF_ROBOTS = 8;
		int MINE_RADIUS = 2;
		
		Benchmark bm = new Benchmark();
	    Logger.getLogger("").addHandler(bm);
	    GridFailureGroup fg = new GridFailureGroup(ROW, COLUMN, MINE_RADIUS);
		Graph<Point> g = GraphLoader.getGraph("files/map");
		FailureRate model = new FixedRadius(fg, g, FAILURE_PROBABILITY);
		MaximizeArrivability ma = new MaximizeArrivability(g, model, NUMBER_OF_ROBOTS, NUMBER_OF_REQUEST, NUMBER_OF_GENERATE, 0, 0, NUMBER_OF_ITERATIONS);
		Point source = new Point(10, 10);
		Point target = new Point(35, 35);
		
		List<Path<Point>> paths = ma.getSolution(source, target);
		bm.getResult();
		String FILENAME = "demos/demo_MakeBlockGraph";
		ResultWriter writer = new ResultWriter((GridGraph)g, model);
		writer.write(ROW, COLUMN, source, target, paths, FILENAME);
	}
	
	public static void demo_kArrivability() {
		// variables specific to this demo
		int NUMBER_OF_ROBOTS = 4;
		int NUMBER_OF_BLOCKERS = 10;
		double FAILURE_PROBABILITY = (double)NUMBER_OF_BLOCKERS / (ROW * COLUMN);
		for (int NUMBER_OF_REQUEST = 1; NUMBER_OF_REQUEST <= NUMBER_OF_ROBOTS; NUMBER_OF_REQUEST++) {	
			Benchmark bm = new Benchmark();
			Logger.getLogger("").addHandler(bm);
			GridFailureGroup fg = new GridFailureGroup(ROW, COLUMN, MINE_RADIUS);
			Graph<Point> g = GraphLoader.getGraph("files/random_map");
			FailureRate model = new FixedRadius(fg, g, FAILURE_PROBABILITY);
			MaximizeArrivability ma = new MaximizeArrivability(g, model, NUMBER_OF_ROBOTS, NUMBER_OF_REQUEST, NUMBER_OF_GENERATE, 0, 0, NUMBER_OF_ITERATIONS);
			Point source = new Point(ROW / 2, 0);
			Point target = new Point(ROW / 2, COLUMN - 1);
			//Point source = new Point(10, 10);
			//Point target = new Point(35, 35);
		
			List<Path<Point>> paths = ma.getSolution(source, target);
			bm.getResult();
			String FILENAME = "demos/demo_"+NUMBER_OF_REQUEST+"-Arrivability_FixedRadius";
			ResultWriter writer = new ResultWriter((GridGraph)g, model);
			writer.write(ROW, COLUMN,source, target, paths, FILENAME);
		}
	}
	
	public static void demo_RandomRadius_kArrivability() {
		// variables specific to this demo
		int ROW = 20;
		int COLUMN = 20;
		for (int NUMBER_OF_REQUEST = 1; NUMBER_OF_REQUEST <= NUMBER_OF_ROBOTS; NUMBER_OF_REQUEST++) {			
			Benchmark bm = new Benchmark();
			Logger.getLogger("").addHandler(bm);
			GridFailureGroup fg = new GridFailureGroup(ROW, COLUMN, MINE_RADIUS);
			Graph<Point> g = GraphLoader.getGraph();
			FailureRate model = new RandomRadius(fg, g, FAILURE_PROBABILITY, MINE_RADIUS + 1);
			MaximizeArrivability ma = new MaximizeArrivability(g, model, NUMBER_OF_ROBOTS, NUMBER_OF_REQUEST, NUMBER_OF_GENERATE, 0, 0, NUMBER_OF_ITERATIONS);
			Point source = new Point(ROW / 2, 0);
			Point target = new Point(ROW / 2, COLUMN - 1);
		
			ma.getSolution(source, target);
			bm.getResult();
			String FILENAME = "demo_"+NUMBER_OF_REQUEST+" -Arrivability_RandomRadius";
		}
	}
	
	public static void demo_NumofBlockers() {
		// variables specific to this demo
		int NUMBER_OF_ROBOTS = 2;
		for (int NUMBER_OF_BLOCKERS = 1; NUMBER_OF_BLOCKERS <=COLUMN; NUMBER_OF_BLOCKERS+=10) {	
			double FAILURE_PROBABILITY = (double)NUMBER_OF_BLOCKERS / (100*ROW * COLUMN);
			Benchmark bm = new Benchmark();
			Logger.getLogger("").addHandler(bm);
			GridFailureGroup fg = new GridFailureGroup(ROW, COLUMN, MINE_RADIUS);
			Graph<Point> g = GraphLoader.getGraph("files/random_map");
//			Graph<Point> g = GraphLoader.Hanover("files/Hanover.osm", ROW, COLUMN);;
			FailureRate model = new FixedRadius(fg, g, FAILURE_PROBABILITY);
			MaximizeArrivability ma = new MaximizeArrivability(g, model, NUMBER_OF_ROBOTS, NUMBER_OF_REQUEST, NUMBER_OF_GENERATE, 0, 0, NUMBER_OF_ITERATIONS);
			//Point source = new Point(17, 10);
			//Point target = new Point(35, 35);
//			Point source = new Point(28, 68);
//			Point target = new Point(65, 40);
			Point source = new Point(ROW / 2, 0);
			Point target = new Point(ROW / 2, COLUMN - 1);
		
			List<Path<Point>> paths = ma.getSolution(source, target);
			bm.getResult();
			String FILENAME = "demos/ranMap_"+NUMBER_OF_BLOCKERS+"Blkrs";
			ResultWriter writer = new ResultWriter((GridGraph)g, model);
			writer.write(ROW, COLUMN, source, target, paths, FILENAME);
		}
	}
	
	public static void main( String[] args ) {
		//demo_RandomRadius();
		//demo_FixedRadius();
		//demo_kArrivability();
		//demo_RandomRadius_kArrivability();
		demo_NumofBlockers();
		//demo_MakeBlockGraph();
		
		/*
		for (int i = 0; i < PathSelection.NUMBER_OF_MODE; i++) modeForSelection[i]=true;
		for (int i = 0; i < PathGeneration.NUMBER_OF_MODE; i++) modeForGeneration[i]=true;
		for (int i = 0; i < PathGeneration.NUMBER_OF_MODE; i++)
			if (modeForGeneration[i]==true)
				for (int j = 0; j < PathSelection.NUMBER_OF_MODE; j++)
					if (modeForSelection[j]==true) {
						Benchmark bm = new Benchmark();
						Logger.getLogger("").addHandler(bm);
						GridFailureGroup fg = new GridFailureGroup(ROW, COLUMN, MINE_RADIUS);
						Graph<Point> g = GraphLoader.getGraph("files/random_map");
						FailureRate model = new FixedRadius(fg, g, FAILURE_PROBABILITY);
						MaximizeArrivability ma = new MaximizeArrivability(g, model, NUMBER_OF_ROBOTS, NUMBER_OF_REQUEST, NUMBER_OF_GENERATE, i, j, NUMBER_OF_ITERATIONS);
						Point source = new Point(ROW / 2, 0);
						Point target = new Point(ROW / 2, COLUMN - 1);
					
						List<Path<Point>> solution=ma.getSolution(source, target);
						bm.getResult();
						time[i][j][0]=generationTime;
						time[i][j][1]=initialSolutionTime;
						time[i][j][2]=localImprovementTime;
						time[i][j][3]=pathSelectionTime;
						time[i][j][4]=pathImprovementTime;
						time[i][j][5]=totalTime;
						quality[i][j][0]=preLocalQuality;
						quality[i][j][1]=aftLocalQuality;
						quality[i][j][2]=model.arrivability(solution, NUMBER_OF_REQUEST);
					}
		
		// Output table
		for (int i = 0; i < PathGeneration.NUMBER_OF_MODE; i++)
			if (modeForGeneration[i]==true)
				for (int j = 0; j < PathSelection.NUMBER_OF_MODE; j++)
					if (modeForSelection[j]==true) {
						System.out.println("Generation Method: "+i+" Selection Method: "+j);
						System.out.println("Time = "+time[i][j][0]+" "+time[i][j][1]+" "+time[i][j][2]+" "+time[i][j][3]+" "+time[i][j][4]+" "+time[i][j][5]);
						System.out.println("Arrivability = "+quality[i][j][0]+" "+quality[i][j][1]+" "+quality[i][j][2]);
					}*/
	}
}
