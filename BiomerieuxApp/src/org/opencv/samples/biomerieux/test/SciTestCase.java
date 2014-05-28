package org.opencv.samples.biomerieux.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;

import org.opencv.samples.biomerieux.exception.BiomerieuxException;
import org.opencv.samples.biomerieux.utils.Sci;

import junit.framework.TestCase;

public class SciTestCase extends TestCase {

	List<Integer> lsi, LSi = new ArrayList<Integer>();
	List<Double> lsd, Lsd;
	
	protected void setUp() throws Exception {
		super.setUp();

		lsi = Arrays.asList(new Integer[] {5, 1, -4, 90, -23, 43});
		lsd = Arrays.asList(new Double[] {5.2, 1.7, -4.6, 90.1, -23.3, 43.43});
		
		Collections.addAll(LSi,10, 10, 10, 11, 11, 11);
	}

	@SuppressWarnings("unchecked")
	public void testDiff() throws BiomerieuxException {
		List<Integer> got_i = Sci.diff(lsi);
		List<Integer> expected_i = Arrays.asList(new Integer[] {-4, -5, 94, -113, 66});
		assertTrue(got_i.containsAll(expected_i));
		
		List<Double> got_d = Sci.diff(lsd);
		List<Double> expected_d = Arrays.asList(new Double[] {-3.5, -6.3, 94.69999999999999, -113.39999999999999, 66.73});
		assertTrue(got_d.containsAll(expected_d));
		
		// Array with one element
		got_i = Sci.diff(Arrays.asList(new Integer[] {3}));
		expected_i = Arrays.asList(new Integer[] {});
		assertTrue(got_i.containsAll(expected_i));
				
		got_d = Sci.diff(Arrays.asList(new Double[] {3.5}));
		expected_d = Arrays.asList(new Double[] {});
		assertTrue(got_d.containsAll(expected_d));
	}
	
	@SuppressWarnings("unchecked")
	public void testDiff_error() {
		// Empty array
		List<Integer> got_i, expected_i;
		List<Double> got_d, expected_d;
		try {
			got_i = Sci.diff(new ArrayList<Integer>());
			expected_i = Arrays.asList(new Integer[] {});
			assertTrue(got_i.containsAll(expected_i));
			assertTrue(false);
		} catch (BiomerieuxException e) {
			assertTrue(true);
		}
		
		try {
			got_d = Sci.diff(new ArrayList<Double>());
			expected_d = Arrays.asList(new Double[] {});
			assertTrue(got_d.containsAll(expected_d));
			assertTrue(false);
		} catch (BiomerieuxException e) {
			assertTrue(true);
		}
	}

	public void testMax() throws BiomerieuxException {
		assertTrue(Double.valueOf(Sci.max(lsd)) == 90.1);
		assertTrue(Double.valueOf(Sci.max(lsi)).intValue() == 90);
	}
	
	public void testMax_error() {
		try {
			assertTrue(Double.valueOf(Sci.max(new ArrayList<Double>())) == 90.1);
			assertTrue(false);
		} catch (BiomerieuxException e) {
			assertTrue(true);
		}

		try {
			assertTrue(Double.valueOf(Sci.max(new ArrayList<Integer>())).intValue() == 90);
			assertTrue(false);
		} catch (BiomerieuxException e) {
			assertTrue(true);
		}
	}
	
	public void testMin() throws BiomerieuxException {
		assertTrue(Double.valueOf(Sci.min(lsd)) == -23.3);
		assertTrue(Double.valueOf(Sci.min(lsi)).intValue() == -23);
	}
	
	public void testMin_error() {
		try {
			assertTrue(Double.valueOf(Sci.max(new ArrayList<Double>())) == 90.1);
			assertTrue(false);
		} catch (BiomerieuxException e) {
			assertTrue(true);
		}

		try {
			assertTrue(Double.valueOf(Sci.min(new ArrayList<Integer>())).intValue() == 90);
			assertTrue(false);
		} catch (BiomerieuxException e) {
			assertTrue(true);
		}
	}
	
	public void testMedian() throws BiomerieuxException {
		double obtained = Sci.median(LSi);
		assertTrue(obtained == 10.5);
		LSi.add( Integer.valueOf(0));
		LSi.add(17);
		assertTrue(obtained == 10.5);
		
		List<Double> LSd = Arrays.asList(new Double[] {25.0, 23.0, 23.0, 22.0, 26.0, 22.0, 25.0, 26.0, 88.0, 23.0, 24.0, 23.0, 23.0, 26.0, 4.0, 23.0, 21.0, 22.0});
		double expected = 23.0;
		obtained = Sci.median(LSd);
		assertTrue(obtained == expected);
	}
	
	public void testMean() throws BiomerieuxException {
		double obtained = Sci.mean(LSi);
		assertTrue(obtained == 10.5);
		LSi.add(0);
		LSi.add(17);
		obtained = Sci.mean(LSi);
		assertTrue(obtained == 10);
	}
	
}
