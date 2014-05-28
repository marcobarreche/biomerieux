package org.opencv.samples.biomerieux.test;

import org.opencv.core.Rect;

import junit.framework.TestCase;

import org.opencv.samples.biomerieux.exception.BiomerieuxException;
import org.opencv.samples.biomerieux.utils.Tuple;
import org.opencv.samples.biomerieux.capsules.Capsules;


public class CapsulesTestCase extends TestCase {
	
	private Rect [] capsules = new Rect[10];
	private Capsules testCapsules;
	
	protected void setUp() throws Exception {
		super.setUp();
		int x, y, w, h;
		
		for (int i = 0; i < 10; i++){
			x = (i + 1) * 10;
			y = 10;
			w = 10;
			h = 10;
			capsules[i] = new Rect(x, y, w, h); 
		}
		testCapsules = new Capsules(capsules);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private void assertEqualsCapsules (Capsules obtained, Capsules expected){
		for(int i = 0; i < obtained.getCapsulesList().size(); i++){
			assertTrue(obtained.getCapsulesList().get(i).equals(
					expected.getCapsulesList().get(i)));
		}
	}
	
	public void testFilter_capsules_same_info() throws BiomerieuxException {
		Capsules expected, obtained;
		expected = testCapsules;
		obtained = new Capsules(capsules);
		obtained.filter_capsules_same_info();
		
		assertEqualsCapsules(obtained, expected);
		
		Rect [] othercapsules = new Rect[15];
		int x;
		for (int i = 0; i < 5; i++){
			x = (i + 1) * 10;
			othercapsules[2 * i] = new Rect(x, 10, 10, 10);
			x += 2;
			othercapsules[2 * i + 1] = new Rect(x, 10, 10, 10);
		}
		for (int i = 10; i < 15; i++){
			x = (i - 4) * 10;
			othercapsules[i] = new Rect(x, 10, 10, 10);
		}
		
		obtained = new Capsules(othercapsules);
		obtained.filter_capsules_same_info();
		assertEqualsCapsules(obtained, expected);
	
	}

	public void testFilter_by_size() throws BiomerieuxException {
		Capsules expected, obtained;
		expected = testCapsules;
		
		Rect [] othercapsules = new Rect[20];
		int x;
		for (int i = 0; i < 10; i++){
			x = (i + 1) * 10;
			othercapsules[i] = new Rect(x, 10, 10, 10);
		}
		for (int i = 10; i < 15; i++){
			x = (i + 1) * 10;
			othercapsules[i] = new Rect(x, 10, 20, 20);
		}
		for (int i = 15; i < 20; i++){
			x = (i + 1) * 10;
			othercapsules[i] = new Rect(x, 10, 6, 6);
		}

		obtained = new Capsules(othercapsules);
		obtained.filter_by_size();
		assertEqualsCapsules(obtained, expected);
	}
		
	public void testRegression_line() throws BiomerieuxException{
		Capsules objects;
		Rect [] aux = new Rect[10];
		int x, y;
		Tuple<Double, Double> expected, obtained;
		
		// Initialization
		for (int i = 0; i < 10; i ++){
			x = (i + 1) * 10;
			y = (i + 1) * 20 + 10;
			aux[i] = new Rect(x - 5, y - 5, 10, 10);		
		}
		objects = new Capsules(aux);
		
		expected = new Tuple<Double, Double>(10.0, 2.0);
		obtained = objects.regression_line();
				
		assertTrue(Math.abs(expected.x - obtained.x) < 10e-4);
		assertTrue(Math.abs(expected.y - obtained.y) < 10e-4);
	}

	public void testSort() throws BiomerieuxException{
		Capsules expected, obtained;
		expected = testCapsules;
		obtained = new Capsules(capsules);
		
		Rect [] othercapsules = new Rect[10];
		int x;
		for (int i = 5; i < 10; i++){
			x = (i + 1) * 10;
			othercapsules[i] = new Rect(x, 10, 10, 10);
		}
		for (int i = 0; i < 5; i++){
			x = (i + 1) * 10;
			othercapsules[i] = new Rect(x, 10, 10, 10);
		}
		
		obtained = new Capsules(othercapsules);
		assertEqualsCapsules(obtained, expected);
	}

	public void testFilter_capsules_by_alignment() throws BiomerieuxException{
		Capsules obtained;
		Rect [] Eaux = new Rect[10];
		Rect [] Oaux = new Rect[15];
		int x, y;
		//Tuple<Double, Double> expected, obtained;

		// Initialization
		for (int i = 0; i < 10; i ++){
			x = (i + 1) * 10;
			y = (i + 1) * 20 + 10;
			Oaux[i] = new Rect(x - 5, y - 5, 10, 10);
			Eaux[i] = new Rect(x - 5, y - 5, 10, 10);
		}
		
		for (int i = 0; i < 5; i ++){
			x = (i + 1) * 20;
			y = (i + 1) * 40 + 30;
			Oaux[10 + i] = new Rect(x - 5, y - 5, 10, 10);		
		}
		obtained = new Capsules(Oaux);
		Capsules expected = new Capsules(Eaux); 
		obtained.filter_capsules_by_alignment();
		
		assertEqualsCapsules(obtained, expected);
	}


}	
	
