package org.opencv.samples.biomerieux.test;

/*
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Rect;
import org.opencv.samples.biomerieux.capsules.Capsules;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;
import org.opencv.samples.biomerieux.utils.Tuple;
*/
import junit.framework.TestCase;

public class CompleteDetectionTestCase extends TestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	/*
	public void test_get_best_center() throws BiomerieuxException{
		List<Integer> vX = Arrays.asList(new Integer[] {10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
		List<Integer> rX = Arrays.asList(new Integer[] {22, 56, 93});
		int i_gap = 10;
		List<Integer> obtained, expected; 
	
		expected = Arrays.asList(new Integer[] {10, 22, 32, 42, 52, 62, 72, 82, 93, 103});
		obtained = Detection.get_best_center(vX, rX, i_gap);
		
		for (int i = 0; i < expected.size(); i++) {
			assertTrue(obtained.get(i).equals(expected.get(i)));
		}
	}
	
	public void test_gap_estimation() throws BiomerieuxException{
		List<Integer> vX = Arrays.asList(new Integer[] {10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
		int obtained, expected = 10;
		obtained = Detection.gap_estimation(vX);
		assertTrue(obtained == expected);
		
		vX = Arrays.asList(new Integer[] {10, 18, 30, 38, 50, 58, 70, 78, 90, 98, 110});
		expected = 10;
		obtained = Detection.gap_estimation(vX);
		assertTrue(obtained == expected);
	}
	 
	public void test_regression_line() throws BiomerieuxException{
		Capsules objects;
		Rect [] aux = new Rect[10];
		int x, y;
		Tuple<Tuple<Double, Double>,Tuple<List<Integer>, List<Integer>>> expected, obtained;
		List<Integer> vX = new ArrayList<Integer>(); 
		List<Integer> vY = new ArrayList<Integer>();
		
		// Initialization
		for (int i = 0; i < 10; i ++){
			x = (i + 1) * 10;
			y = (i + 1) * 20 + 10;
			aux[i] = new Rect(x - 5, y - 5, 10, 10);
			vX.add(x); 
			vY.add(y);			
		}
		objects = new Capsules(aux);
		
		Tuple<Double, Double> coefs = new Tuple<Double, Double>(10.0, 2.0);
		Tuple<List<Integer>, List<Integer>> vars = new Tuple<List<Integer>, List<Integer>>(vX, vY);
		
		expected = new Tuple<Tuple<Double, Double>, Tuple<List<Integer>, List<Integer>>>(coefs, vars);
		obtained = Detection.regression_line(objects);
				
		assertTrue(Math.abs(expected.x.x - obtained.x.x) < 10e-4);
		assertTrue(Math.abs(expected.x.y - obtained.x.y) < 10e-4);
		for (int i = 0; i < expected.y.x.size(); i++) {
			assertTrue(obtained.y.x.get(i).equals(expected.y.x.get(i)));
			assertTrue(obtained.y.y.get(i).equals(expected.y.y.get(i)));
		}			
	}
	
	/*
	public static List<RgbColor> get_cap_color(SuperMat img, List<Integer> lsX, List<Integer> lsY)
	*/
	
	/*
	public static Tuple<List<Integer>, Integer> findLeftGroup(List<Integer> X, List<Integer> Y, SuperMat img,
	 */
	
	/*
	public static Tuple<List<Integer>, Integer> findRightGroup(List<Integer> X, List<Integer> Y, SuperMat img,
			 int leftX, ColorPos colorpos)  */
	 
	 /*
	 private static Tuple<Double, Double> polyfit (List<Integer> lsX, List<Integer> lsY)  
	 
	 public static Tuple<Mat, Mat> rotating_image(SuperMat img, double slope)  
	 
	 public static Tuple<Mat, Mat> rotating_coord(SuperMat rot_matrix, List<Integer> vX, List<Integer> vY) 

	 private static Tuple<List<Integer>, Integer> findGroup(List<Integer> X, List<Integer> grids, SuperMat img,
			 int l, List<Integer> y, ColorPos colorpos, int num, int limit, boolean left)  

	 private static List<Integer> map_index(List<Integer> ls, List<Integer> index) 
	 
	 private static List<Integer> map_mod(List<Integer> ls, int mod) 
	 
	 private static List<Integer> map_biggerThan(List<Integer> ls, double pivot)
	 
	 private static Mat list2Mat(List<Integer> ls, int type) 
	 
	 private static Mat vStack(Mat lsa, Mat lsb)  */

}
	

	
