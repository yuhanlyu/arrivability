package arrivability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class MineField extends Application {
	public static final boolean PARALLEL = true;
	
	// For application
	private static final int WIDTH = 1000;
	private static final int HEIGHT = 1000;
	private Group root = new Group();
    private Group lines = new Group();
	
	// For drawing circles
	public static final int ROW = 50;
	public static final int COLUMN = 50;
	private static final int RADIUS = 3;
	private static final int SHIFT = 20;
	private static final int SEPARATION = 3;

	// For arrivability model
	public static final int MINE_RADIUS = 1;
	private static final int NUMBER_OF_BLOCKERS = 1;
	private static final double FAILURE_PROBABILITY = (double)NUMBER_OF_BLOCKERS / (ROW * COLUMN);
	
	private static final int NUMBER_OF_ROBOTS = 5;
	private static final int NUMBER_OF_REQUEST = 1;
	private static final int NUMBER_OF_GENERATE = 100;
	private static final int NUMBER_OF_ITERATIONS = 100;
	private static final int genMode = 0;
	private static final int selMode = 0;
	
	
	//private Arrivability model = new FixedRadiusHardDisk(ROW, COLUMN, NUMBER_OF_BLOCKERS, MINE_RADIUS);
	//private Arrivability model = new FixedRadiusPoissonHardDisk(ROW, COLUMN, NUMBER_OF_BLOCKERS, MINE_RADIUS);
	
	// For minimizing failure rate
	private GridFailureGroup fg = new GridFailureGroup(ROW, COLUMN, MINE_RADIUS);
	private Graph<Point> g = GraphLoader.getGraph();
	private FailureRate model = new FixedRadius(fg, g, FAILURE_PROBABILITY);
	//private FailureRate model = new RandomRadius(fg, g, FAILURE_PROBABILITY, MINE_RADIUS + 1);
	private Map<Point, Circle> pointToCircle = new LinkedHashMap<>();
	private Map<Circle, Point> circleToPoint = new LinkedHashMap<>();
	private Point source = new Point(ROW / 2, 0);
	private Point target = new Point(ROW / 2, COLUMN - 1);
	
	
	// For maximizing arrivability
	private MaximizeArrivability ma = new MaximizeArrivability(g, model, NUMBER_OF_ROBOTS, NUMBER_OF_REQUEST, NUMBER_OF_GENERATE, genMode, selMode, NUMBER_OF_ITERATIONS);
	
	// For auxiliary information
	private Text info = new Text(10, 10, "#Rows is " + ROW + " #Columns is " + COLUMN);
	
	@Override
    public void start(Stage primaryStage) {
		// Initialize the scene

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setTitle("Mine Field");
        
        // Initialize the nodes
        Group circles = new Group();
        Map<Point, Circle> circleMap = new HashMap<>();
		for (Point p : model.vertexSet()) {
            Circle circle = new Circle(SHIFT + p.getY() * RADIUS * SEPARATION, SHIFT + p.getX() * RADIUS * SEPARATION, RADIUS, Color.web("black", 0.05));
            circleMap.put(p, circle);
            circle.setStrokeType(StrokeType.CENTERED);
            circle.setStroke(Color.web("black", 0.5));
            circle.setStrokeWidth(2);
            if (p.equals(source)) {
            	circle.setStroke(Color.web("red", 0.5));
                circle.setStrokeWidth(5);	
            }
            if (p.equals(target)) {
            	circle.setStroke(Color.web("blue", 0.5));
                circle.setStrokeWidth(5);
            }
            circles.getChildren().add(circle);
            pointToCircle.put(p, circle);
            circleToPoint.put(circle, p);
		}
		
		Map<Point, List<Circle>> groupMap = new HashMap<>();
		// Initialize failure groups
		for (Point p : model.vertexSet()) {
			List<Circle> fg = new ArrayList<>();
			groupMap.put(p, fg);
			for (Point forbidden : model.getForbiddenArea(p)) {
				if (model.vertexSet().contains(forbidden))
					fg.add(circleMap.get(forbidden));
			}
			circleMap.get(p).setOnMouseEntered(new EventHandler<MouseEvent>() {
	        	 
	            @Override
	            public void handle(MouseEvent mouseEvent) {
	            	for (Circle forbidden : fg) {
	            		forbidden.setStrokeWidth(5.0);
	            	}
	            }
	         
	        });
			
			circleMap.get(p).setOnMouseExited(new EventHandler<MouseEvent>() {
	        	 
	            @Override
	            public void handle(MouseEvent mouseEvent) {
	            	for (Circle forbidden : fg) {
	            		forbidden.setStrokeWidth(2.0);
	            	}
	            }
	         
	        });
		}
		
        root.getChildren().add(circles);
        root.getChildren().add(lines);
        
        // Set up listeners
        root.getChildren().add(info);
        scene.setOnMouseMoved(new EventHandler<MouseEvent>() {
        	 
            @Override
            public void handle(MouseEvent mouseEvent) {
            	info.setText("#Rows is " + ROW + " #Columns is " + COLUMN + " Current position is (" + mouseEvent.getSceneX() + ", " + mouseEvent.getSceneY() + ")");
            }
         
        });
        
        scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
       	 
            @Override
            public void handle(final MouseEvent mouseEvent) {
        		// Compute and draw path
            	//computePaths();
            }
        });
        
        scene.setOnKeyTyped(new EventHandler<KeyEvent>() {

            @Override
            public void handle(final KeyEvent keyEvent) {
        		// Drawing path
            	switch (keyEvent.getCharacter().charAt(0)) {
            	    case 'r':
            	    	randomPath();
            	    	break;
            	    case 'c':
            	    	computePaths();
            	    	break;
				    default:
					    break;
            	}

            }
        });
        
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
	
	/**
	 * Compute a random path
	 */
	protected void randomPath() {
		root.getChildren().remove(lines);
		lines.getChildren().clear();
		new Thread(new Task<Void>() {

			@Override
			public Void call() {
				try {
					drawPaths(ma.getSolution(source, target));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}}).start();
	}
	
	

	/**
	 * Compute the paths and draw them
	 */
	private void computePaths() {
		root.getChildren().remove(lines);
		lines.getChildren().clear();
		new Thread(new Task<Void>() {

			@Override
			public Void call() throws Exception {
				try {
					System.out.println("Computing");
					//MaximizeArrivability maximizer = new MaximizeArrivability(model, 2, 1, source, target);
					//drawPaths(maximizer.getSolution(MAX_UNION));
					FailureMinimizer minimizer = new FailureMinimizer((FixedRadius)model, 2, 1, source, target);
					drawPaths(minimizer.getSolution());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return null;
			}}).start();
	}

	/**
	 * Draw paths, can be invoked by non-main thread
	 * @param paths
	 */
	private void drawPaths(Iterable<Path<Point>> paths) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (Path<Point> path : paths) {
					Point previous = null;
					for (Point p : path) {
						if (previous != null) {
							Circle start = pointToCircle.get(previous);
							Circle end = pointToCircle.get(p);
							Line line = new Line(start.getCenterX(), start.getCenterY(), end.getCenterX(), end.getCenterY());
							lines.getChildren().add(line);
						}
						previous = p;
					}
				}
				root.getChildren().add(lines);
			}
		});
	}
	
	/**
	 * Draw one path
	 * @param path
	 */
	private void drawPath(Path<Point> path) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Point previous = null;
				for (Point p : path) {
					if (previous != null) {
						Circle start = pointToCircle.get(previous);
						Circle end = pointToCircle.get(p);
						Line line = new Line(start.getCenterX(), start.getCenterY(), end.getCenterX(), end.getCenterY());
						lines.getChildren().add(line);
					}
					previous = p;
				}
				root.getChildren().add(lines);
			}
		});
	}
	
    public static void main(String[] args) {
        launch(args);
    }
    
    // For 5 * 5, Best arrivability is: 0.6653926026058428 time is: 6175 iterations is 19099 Maximum union bound is 30 actual union size is 24
    // For 6 * 6, Best arrivability is: 0.7420112883549137 time is: 169866 iterations is 515043 Maximum union bound is 30 actual union size is 30
    // For 7 * 7, Best arrivability is: 0.7722741152276762 time is: 400337 iterations is 678638 Maximum union bound is 35 actual union size is 35
    // Best arrivability is: 0.7722741152276762 time is: 1020738 iterations is 1746975 Maximum union bound is 37 actual union size is 35
    // 0.5665413355469848 time is: 15346
    // 0.5665413355469848 time is: 6388
    // 0.6652720919613734 time is: 347942
    // 0.6652720919613734 time is: 78884
    // For 7 * 7 0.7356914732051443 time is: 1788986
}
