package org.opencv.samples.biomerieux.detection;
import java.io.File;
import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.samples.biomerieux.capsules.Capsules;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;


public class Classifier {
	public static String FILENAMEPATH = "cascade.xml";
	private static final double SCALE_FACTOR = 1.3;
	private static final int MIN_NEIGHBOURS = 1;
	private static final int CLASSIFIER_FLAGS = 0;
	private static final int MIN_SIZE_OBJECT = 0;
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private static final int THICKNESS = 2;

    private CascadeClassifier detector;
    private String filename;
  
    public Classifier (File cascade_file) throws IOException {
    	filename = cascade_file.getAbsolutePath();
    	detector = new CascadeClassifier(filename);
        if (detector.empty()) {
            detector = null;
        }
    }
    
    public String getFilename () {
    	return filename;
    }
    
    public CascadeClassifier getClassifier() {
    	return detector;
    }

	public Capsules applyClassifier(Mat rgb){
		Mat gray = new Mat();
		Imgproc.cvtColor(rgb, gray, Imgproc.COLOR_RGB2GRAY);
		MatOfRect faces = new MatOfRect();
		detector.detectMultiScale(
			gray, faces, SCALE_FACTOR, MIN_NEIGHBOURS, CLASSIFIER_FLAGS, new Size(MIN_SIZE_OBJECT, MIN_SIZE_OBJECT), new Size()
		);
		Capsules capsules = new Capsules(faces.toArray());
		capsules.paint(rgb, FACE_RECT_COLOR, THICKNESS);
        return capsules;
    }
	
	public Capsules capsulesDetector(Mat rgb) throws BiomerieuxException{
		Mat gray = new Mat();
		Imgproc.cvtColor(rgb, gray, Imgproc.COLOR_RGB2GRAY);
		MatOfRect faces = new MatOfRect();
		detector.detectMultiScale(
			gray, faces, SCALE_FACTOR, MIN_NEIGHBOURS, CLASSIFIER_FLAGS, new Size(MIN_SIZE_OBJECT, MIN_SIZE_OBJECT), new Size()
		);
		Capsules res = new Capsules(faces.toArray());
		res.sort();
		return res;
    }
}
