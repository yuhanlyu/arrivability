package arrivability;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class FailureRateTest {

	@Test
	public void test() {
		double failureProbability = 0.01, successProbaiblity = 1 - failureProbability;
		FixedRadius fr = new FixedRadius(new GridFailureGroup(5, 5, 1), new GridGraph(5, 5), failureProbability);
        Path<Point> path = new Path<>();
        path.addVertex(new Point(2, 0));
        path.addVertex(new Point(2, 1));
        path.addVertex(new Point(2, 2));
        path.addVertex(new Point(2, 3));
        path.addVertex(new Point(2, 4));
        assertEquals(15, fr.forbiddenArea(path).size());
        assertTrue(Math.abs(fr.arrivability(path) - fr.arrivability(Arrays.asList(path, path), 1)) < 1e-7);
        
        Path<Point> path2 = new Path<>();
        path2.addVertex(new Point(2, 0));
        path2.addVertex(new Point(3, 0));
        path2.addVertex(new Point(3, 1));
        path2.addVertex(new Point(3, 2));
        path2.addVertex(new Point(3, 3));
        path2.addVertex(new Point(3, 4));
        path2.addVertex(new Point(2, 4));
        assertEquals(17, fr.forbiddenArea(path2).size());
        assertEquals(20, fr.forbiddenArea(Arrays.asList(path, path2)).size());
        
        double arrivability = Math.pow(successProbaiblity, fr.forbiddenArea(path).size());
        arrivability += Math.pow(successProbaiblity, fr.forbiddenArea(path2).size());
        arrivability -= Math.pow(successProbaiblity, fr.forbiddenArea(Arrays.asList(path, path2)).size());
        assertTrue(Math.abs(fr.arrivability(Arrays.asList(path, path2), 1) - arrivability) < 1e-7);
	}
}
