package org.opencv.samples.biomerieux.test;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import junit.framework.TestCase;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;
import org.opencv.samples.biomerieux.capsules.Centers;


public class CentersTestCase extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private void assertEqualsCenters (Centers obtained, Centers expected){
		for(int i = 0; i < obtained.getX().size(); i++){
			assertTrue(obtained.getX(i) == expected.getX(i));
			assertTrue(obtained.getY(i) == expected.getY(i));
		}
	}
	
	public void testRotating_coord() throws BiomerieuxException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		int x, y;
		Centers expected;
		List<Integer> Xi = new ArrayList<Integer>();
		List<Integer> Xo = new ArrayList<Integer>();
		List<Integer> Yi = new ArrayList<Integer>();
		List<Integer> Yo = new ArrayList<Integer>();
		
		for (int i = 0; i < 10; i++){
			x = (i + 1) * 10;
			y = -x;
			Xi.add(x);
			Yi.add(y);
			Xo.add(x);
			Yo.add(-y);
		}
		System.out.println(Xi);
		System.out.println(Yi);
		
		expected = new Centers(Xo, Yo);
		Centers obtained = new Centers(Xi, Yi);
		
		//Mat A = Mat.ones(100, 100, CV_8U)*3;
		// Mat M = new Mat(2, 3, 6);
		
		Mat M = Imgproc.getRotationMatrix2D(new Point(0.0, 0.0), 90, 1); //Rota en el sentido horario porque las X y Y se intercambian 
		
		//System.out.println(M);
		obtained.rotating_coord(M);
		
		System.out.println(obtained.getX());
		System.out.println(obtained.getY());
		
		assertEqualsCenters(obtained, expected);
	}


}	
	
