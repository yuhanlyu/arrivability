package arrivability;

import java.util.LinkedHashMap;
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
	// For application
	private static final int WIDTH = 1000;
	private static final int HEIGHT = 1000;
	private Group root = new Group();
    private Group lines = new Group();
	
	// For drawing circles
	private static final int ROW = 7;
	private static final int COLUMN = 7;
	private static final int RADIUS = 3;
	private static final int SHIFT = 50;
	private static final int SEPARATION = 7;

	// For arrivability model
	private static final int NUMBER_OF_BLOCKERS = 1;
	private static final int MINE_RADIUS = 1;
	//private Arrivability model = new FixedRadiusHardDisk(ROW, COLUMN, NUMBER_OF_BLOCKERS, MINE_RADIUS);
	private Arrivability model = new FixedRadiusPoissonHardDisk(ROW, COLUMN, NUMBER_OF_BLOCKERS, MINE_RADIUS);
	private Map<Point, Circle> pointToCircle = new LinkedHashMap<>();
	private Map<Circle, Point> circleToPoint = new LinkedHashMap<>();
	private Point source = new Point(ROW / 2, 0);
	private Point target = new Point(ROW / 2, COLUMN - 1);
	private int numberOfPaths = 10;
	
	// For auxiliary information
	private Text info = new Text(10, 10, "#Rows is " + ROW + " #Columns is " + COLUMN);
	
	@Override
    public void start(Stage primaryStage) {
		// Initialize the scene

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setTitle("Mine Field");
        
        // Initialize the nodes
        Group circles = new Group();
		for (Point p : model.vertexSet()) {
            Circle circle = new Circle(SHIFT + p.getColumn() * RADIUS * SEPARATION, SHIFT + p.getRow() * RADIUS * SEPARATION, RADIUS, Color.web("black", 0.05));
            circle.setStrokeType(StrokeType.CENTERED);
            circle.setStroke(Color.web("black", 0.5));
            circle.setStrokeWidth(2);
            circles.getChildren().add(circle);
            pointToCircle.put(p, circle);
            circleToPoint.put(circle, p);
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
            	computePaths();
            }
        });
        
        scene.setOnKeyTyped(new EventHandler<KeyEvent>() {

            @Override
            public void handle(final KeyEvent keyEvent) {
        		// Drawing path
            	switch (keyEvent.getCharacter().charAt(0)) {
            	    case '+':
            		    ++numberOfPaths;
                    	computePaths();
            		    break;
            	    case '-':
            		    --numberOfPaths;
                    	computePaths();
            		    break;
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
					drawPath(model.randomPath(source, target));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}}).start();
	}
	
	

	/**
	 * Compute the paths and draw them 0.7420112883549137
	 */
	private void computePaths() {
		root.getChildren().remove(lines);
		lines.getChildren().clear();
		new Thread(new Task<Void>() {

			@Override
			public Void call() throws Exception {
				try {
					System.out.println("Computing");
					MaximizeArrivability maximizer = new MaximizeArrivability(model, 2, 1, source, target);
					drawPaths(maximizer.getSolution());
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
}
