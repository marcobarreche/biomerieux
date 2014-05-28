package org.opencv.samples.biomerieux.test;

import java.io.File;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import junit.framework.TestCase;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;
import org.opencv.samples.biomerieux.utils.SuperMat;
import org.opencv.samples.biomerieux.utils.Tuple;
import org.opencv.samples.biomerieux.capsules.Capsules;
import org.opencv.samples.biomerieux.capsules.RgbColor;
import org.opencv.samples.biomerieux.capsules.SmartCapsules;


public class SmartCapsulesTestCase extends TestCase {
	
	private Rect [] capsules = new Rect[10];
	
	protected void setUp() throws Exception {
		
		super.setUp();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		int x, y, w, h;
		
		for (int i = 0; i < 10; i++){
			x = (i + 1) * 10;
			y = 10;
			w = 10;
			h = 10;
			capsules[i] = new Rect(x, y, w, h); 
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGet_color() throws BiomerieuxException {
		SmartCapsules SC = new SmartCapsules(capsules);
		List<RgbColor> vColor;
		
		SuperMat image = new SuperMat(Highgui.imread(new File("local/red.png").getAbsolutePath(), Highgui.CV_LOAD_IMAGE_COLOR));
		Imgproc.cvtColor((Mat) image, (Mat) image, Imgproc.COLOR_BGRA2RGB);
		vColor = SC.get_color(image);
		
		System.out.println(vColor);
	}
	
	public void testProp_newCaps () throws BiomerieuxException {
		Rect [] o_capsules = new Rect[20];
		int x, y, w;
		
		for (int i = 0; i < 10; i++){
			x = 5 + i * 10;
			y = 50;
			w = 10;
			o_capsules[i] = new Rect(x, y, w, w); 
		}
		for (int i = 10; i < 20; i++){
			x = 13 + i * 10;
			y = 10;
			w = 10;
			o_capsules[i] = new Rect(x, y, w, w); 
		}
		SmartCapsules SC = new SmartCapsules(o_capsules);
		Tuple<Capsules, int[]> obtained = SC.drop_Cap(10);
		//Tuple<Capsules, int[]> expected = new Tuple<Capsules, int[]>(new Capsules(new Rect[]{new Rect(105, 50, 10, 10)}), new int[]{10});
		System.out.println(obtained.x.getCapsules()[0]);
		System.out.println(obtained.y[0]);
		//System.out.println(expected.y[0]);
	}
}	
	
