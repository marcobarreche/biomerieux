package org.opencv.samples.biomerieux.test;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.biomerieux.capsules.CapsulesReader;
import org.opencv.samples.biomerieux.capsules.RgbColor;
import org.opencv.samples.biomerieux.utils.Tuple;

import junit.framework.TestCase;

public class CapsulesReaderTestCase extends TestCase {

	private CapsulesReader capsule;
	private Mat image;
	protected void setUp() throws Exception {
		super.setUp();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		String current = new java.io.File(".").getCanonicalPath() + '/';
		try {
			capsule = new CapsulesReader(new FileInputStream(current + "res/raw/color-capsules-api20e.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}		
		image = Highgui.imread(current + "local/1N-2imagelarge.png", Highgui.CV_LOAD_IMAGE_COLOR);
		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGRA2RGB);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		capsule = null;
	}

	public void testPredict() {
		// Get the expected Array
		ArrayList<Tuple<String, RgbColor>> expected = new ArrayList<Tuple<String, RgbColor>>();
		expected.add(new Tuple<String, RgbColor>("5P", new RgbColor(94, 85, 41)));
		expected.add(new Tuple<String, RgbColor>("3P", new RgbColor(31, 7, 178)));
		expected.add(new Tuple<String, RgbColor>("3N", new RgbColor(54, 123, 198)));
		expected.add(new Tuple<String, RgbColor>("1N", new RgbColor(165, 175, 183)));
		expected.add(new Tuple<String, RgbColor>("1P", new RgbColor(123, 196, 207)));

		final RgbColor color = RgbColor.averageColor(image);
		Collections.sort(expected, new Comparator<Tuple<String, RgbColor>>() {
			@Override
			public int compare(Tuple<String, RgbColor> t1, Tuple<String, RgbColor> t2) {
				return (int) (t1.y.dist2(color) - t2.y.dist2(color));
			}
		});

		// Get the got array
		String [] ch = new String [] {"3N", "1P", "5P", "3P", "1N"};
		ArrayList<Tuple<String, RgbColor>> got = capsule.predict(image, new HashSet<String>(Arrays.asList(ch)));
		
		// Compare
		for (int i = 0; i < got.size(); i++) {
			assertTrue(got.get(i).x.equals(expected.get(i).x));
			assertTrue(got.get(i).y.equals(expected.get(i).y));
		}
	}
	
	public void testPredictCapsule() {
		Tuple<String, Double> got;
		for (int i=1; i < 12; i++) {
			got = capsule.predictCapsule(image, i);
			assertTrue(got.x.equals("N"));
			assertTrue(got.y.equals(1.0));
		}
		for (int i=12; i < 21; i++) {
			got = capsule.predictCapsule(image, i);
			assertTrue(got.x.equals("P"));
			assertTrue(got.y.equals(1.0));
		}
		try {
			capsule.predictCapsule(image, 21);
			assertTrue(false);
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}
	
	public void testPredictEmptyImage() {
		Tuple<String, Double> got;

		got = capsule.predictCapsule(new Mat(), 1);
		assertTrue(got.x.equals("N"));
		assertTrue(got.y.equals(1.0));

		for (int i = 2; i < 12; i++) {
			got = capsule.predictCapsule(new Mat(), i);
			assertTrue(got.x.equals("P"));
			assertTrue(got.y.equals(1.0));
		}

		for (int i = 12; i < 21; i++) {
			got = capsule.predictCapsule(new Mat(), i);
			assertTrue(got.x.equals("N"));
			assertTrue(got.y.equals(1.0));
		}

		try {
			capsule.predictCapsule(new Mat(), 21);
			assertTrue(false);
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}
}
