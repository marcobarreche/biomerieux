package org.opencv.samples.biomerieux.test;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.samples.biomerieux.capsules.RgbColor;

import junit.framework.TestCase;

public class RgbColorTestCase extends TestCase {
	private Mat image;
	private RgbColor color;

	protected void setUp() throws Exception {
		super.setUp();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		image = Mat.ones(2, 2, CvType.CV_16UC3);
		image.put(0, 0, new short [] {50, 30, 40});
		image.put(0, 1, new short [] {150, 60, 200});
		image.put(1, 0, new short [] {200, 90, 5});
		image.put(1, 1, new short [] {250, 120, 10});		
		color = new RgbColor(20, 100, 150);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		image.release();
		color = null;
	}
	
	public void testAverageColor() {
		RgbColor expected = new RgbColor((int)(650 / 4), (int)(300 / 4), (int)(255 / 4));
		RgbColor got = RgbColor.averageColor(image);
		assertTrue(got.equals(expected));
	}
	
	public void testAverageColorEmptyImage() {
		RgbColor expected = new RgbColor(0, 0, 0);
		RgbColor got = RgbColor.averageColor(new Mat());
		assertTrue(got.equals(expected));
	}
	
	public void testDist2() {
		double got = color.dist2(new RgbColor(90, 114, 67));
		double expected = Math.pow(90 - 20, 2) + Math.pow(100 - 114, 2) + Math.pow(150 - 67, 2); 
		assertTrue(got == expected);
	}

}
