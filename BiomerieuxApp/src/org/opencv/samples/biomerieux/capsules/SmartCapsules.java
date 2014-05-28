package org.opencv.samples.biomerieux.capsules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.opencv.core.Core;
import org.opencv.core.Rect;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.biomerieux.capsules.Capsules;
import org.opencv.samples.biomerieux.capsules.ColorPos;
import org.opencv.samples.biomerieux.capsules.RgbColor;
import org.opencv.samples.biomerieux.detection.Classifier;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;
import org.opencv.samples.biomerieux.utils.Sci;
import org.opencv.samples.biomerieux.utils.SuperMat;
import org.opencv.samples.biomerieux.utils.Tuple;
import org.xml.sax.SAXException;


public class SmartCapsules extends Capsules {
	private static final Scalar COLOR_GOT_EVALUATION = new Scalar(200, 200, 0);
	private static final Scalar COLOR_EXP_EVALUATION = new Scalar(200, 0, 0);
	private static final Scalar BLACK_SIGN_COLOR = new Scalar(0, 0, 0);

	public static final int MAX_NUM_CAPSULES = 20;
	public static final String COLOR_XML_FILEPATH = "res/raw/capsule_color.xml";
	public static final String POSITION_XML_FILEPATH = "res/raw/capsule_positions.xml";
	public static int width_capsule, height_capsule;

	List<Boolean> lsCorrect;
	public SmartCapsules(Capsules capsules) {
		super(capsules.getCapsules());
		lsCorrect = initializeBooleanList(MAX_NUM_CAPSULES);
	}

	public SmartCapsules(Rect[] capsules) {
		super(capsules);
		lsCorrect = initializeBooleanList(MAX_NUM_CAPSULES);
	}

	public void removeCapsule (int index) {
		this.lsCorrect.remove(index);
		this.getCapsulesList().remove(index);
	}

	public static Tuple<SmartCapsules,List<Boolean>> api20e_reader(SuperMat image, Classifier detector, 
			ColorPos colorpos) throws IOException, BiomerieuxException, ParserConfigurationException, SAXException {
		SmartCapsules capsules;
		SuperMat rotated_image;
		List<Boolean> positive_caps;

		capsules = detection(image, detector);
		if (capsules.getCapsules().length <8) {
			throw new BiomerieuxException("Unable to detect enough capsules in the first detection: " + capsules.getCapsules().length + " " +capsules);
		}

		rotated_image = capsules.main_method_1(image);
		SuperMat rot_image = new SuperMat(rotated_image);
		capsules = detection(rot_image, detector);
		if (capsules.getCapsules().length <8) {
			throw new BiomerieuxException("Unable to detect enough capsules in the second detection: " + capsules.getCapsules().length + " " +capsules);
		}
		
		// XXX: REMOVE THIS!!
		/*
		capsules = new SmartCapsules(new Rect[] {
		        new Rect( 78,  98,  24,  24), new Rect( 97,  96,  27,  27), 
				new Rect(119,  99,  24,  24), new Rect(142,  95,  26,  26), new Rect(164,  98,  24,  24), 
				new Rect(210,  98,  26,  26), new Rect(235,  98,  24,  24), new Rect(292,  92,  31,  31), 
				new Rect(320, 101,  24,  24), new Rect(342, 102,  24,  24), new Rect(364, 100,  24,  24), 
				new Rect(385, 101,  24,  24), new Rect(407, 103,  24,  24), new Rect(428, 101,  26,  26), 
				new Rect(471, 102,  25,  25), new Rect(493, 103,  24,  24)});
		 */
		capsules.main_method_2(rot_image, colorpos);
		
		positive_caps = capsules.get_positives(rotated_image, colorpos);
		capsules.draw_eval(rotated_image, positive_caps);
		return new Tuple<SmartCapsules,List<Boolean>>(capsules, positive_caps);
	}

	public static SmartCapsules detection(SuperMat image, Classifier detector) throws BiomerieuxException, IOException{
		SmartCapsules capsules = new SmartCapsules(detector.capsulesDetector(image));
		capsules.sort();
		return capsules;
	}

	public SuperMat main_method_1(SuperMat image) throws BiomerieuxException, IOException, ParserConfigurationException, SAXException {
		SuperMat image_fd = new SuperMat(image.clone());  
		draw(image_fd, new Scalar(255, 0, 0));
		this.filter_by_size();
		this.filter_capsules_by_alignment();
		this.filter_capsules_same_info();
		draw(image_fd, new Scalar(0, 0, 255));
		Tuple<Double, Double> tuple = regression_line();
		double line_slope = tuple.y;
		Tuple<Mat, Mat> tupleMat = rotating_image(image, line_slope);
		return new SuperMat(tupleMat.x);
	}

	public void main_method_2(SuperMat image, ColorPos colorpos) throws BiomerieuxException, IOException,
	ParserConfigurationException, SAXException {	
		filter_by_size();
		filter_capsules_by_alignment();
		filter_capsules_same_info();
		// Get left Group
		findLeftGroup(new SuperMat(image), colorpos);
		filter_capsules_same_info();
		// Get right Group
		findRightGroup(image, colorpos);
	}

	public void draw(SuperMat image, Scalar color){
		//Scalar green = new Scalar(0, 255, 0);
		for (int i= 0; i < getCapsules().length; i++) {
			draw(image, color, i);
		}	
	}

	public void draw(SuperMat image, Scalar color, int i) {
		Rect R = getCapsules()[i];
		Point P1 = new Point(R.x, R.y);
		Point P2 = new Point(R.x + R.width, R.y + R.height);

		Core.rectangle(image, P1, P2, color);
	}

	public void drawSelection(SuperMat image, Scalar color, int i, int thickness) throws BiomerieuxException {
		Rect R = this.getCapsules()[i];
		Point P1 = new Point(R.x + 1, R.y + 1);
		Point P2 = new Point(R.x + R.width - 1, R.y + R.height - 1);
		Core.rectangle(image, P1, P2, color, thickness, 1, 0);
	}

	public void draw_eval(Mat img, List<Boolean> vE) throws BiomerieuxException {
		int gap = getCenters().gap_estimation();
		width_capsule = gap / 2;
		height_capsule = gap / 2;

		for (int i= 0; i < getCentersX().size(); i++) {
			draw_eval(img, vE, i, gap);
		}
	}

	public void draw_eval(Mat img, final List<Boolean> vE, int i, int gap) throws BiomerieuxException {
		if (gap < 0) {
			gap = getCenters().gap_estimation();
		}
		int x = getCentersX().get(i);
		int y = getCentersY().get(i) + gap;

		Scalar color = (!lsCorrect.get(i)) ? COLOR_GOT_EVALUATION : COLOR_EXP_EVALUATION; 
		Core.circle(img, new Point(x, y), (int) gap/4, BLACK_SIGN_COLOR, (int) gap/4);
		Core.circle(img, new Point(x, y), (int) gap/5, color, (int) gap/5);
		Core.line(img, new Point(x - (int) gap /5, y), new Point(x+(int) gap /5, y), BLACK_SIGN_COLOR, (int) gap/10);
		if (vE.get(i)) {
			Core.line(img, new Point(x, y - (int) gap /5), new Point(x, y + (int) gap /5), BLACK_SIGN_COLOR, (int) gap/10);
		}
	}

	public void changeAndPaintEvaluation(Mat image, List<Boolean> ls, int indexCapsule) throws BiomerieuxException {
		lsCorrect.set(indexCapsule, !lsCorrect.get(indexCapsule));
		ls.set(indexCapsule, !ls.get(indexCapsule));
		draw_eval(image, ls, indexCapsule, -1);
	}

	public int getCapsuleAprox(int x, int y) throws BiomerieuxException {
		for (int i = 0; i < this.getCapsules().length; i++) {
			Rect cap = this.getCapsules()[i];
			int left = cap.x;
			int right = left + cap.width;
			int top = cap.y;
			int bottom = 2 * cap.height + top;

			if (x >= left && x <= right && y >= top && y <= bottom) {
				return i;
			}
		}
		return -1;
	}

	public List<RgbColor> get_color(SuperMat img) throws BiomerieuxException {
		Centers centers = getCenters();
		RgbColor  auxColor;
		int i_gap = (int)Math.round(centers.gap_estimation() / 3.0);

		List<RgbColor> res = new ArrayList<RgbColor>();
		int length = Math.min(centers.getX().size(), centers.getY().size());
		for (int i =0; i < length; i ++) {
			double x = centers.getX(i);
			double y = centers.getY(i);
			int box [] = new int [] {(int)(x - i_gap), (int)(y - i_gap), (int) i_gap, (int) i_gap};
			SuperMat cropped_img = img.crop(new Rect(box[0], box[1], box[2], box[3]));
			auxColor = cropped_img.median();
			res.add(auxColor);
		}
		return res;
	}
	
	public Tuple<Capsules, int[]> drop_Cap(int index) {
		Tuple<Capsules, int[]> output;

		switch (index) {
		case 0: output = prop_rect(1, 9, 10, 19);
		break;
		case 9: output = prop_rect(0, 8, 10, 19);
		break;
		case 10:output = prop_rect(0, 9, 11, 19);
		break;
		case 19:output = prop_rect(0, 9, 10, 18);
		break;
		default:return null;
		}
		return output;    
	}

	public static int [] getCapsules2Remove() {
		return new int []{0, 9, 10, 19};
	}
	
	public List<Boolean> get_positives(SuperMat image, ColorPos colorpos) throws BiomerieuxException, IOException, ParserConfigurationException, SAXException {
		return colorpos.get_positives(get_color(image), null);
	}

	public Tuple<Mat, Mat> rotating_image(Mat img, double slope) {
		int cols = img.width();
		int rows = img.height();
		Point center = new Point(cols/2, rows/2);
		Mat M = Imgproc.getRotationMatrix2D(center, Math.atan(slope) * 90, 1);		
		Mat dest = new Mat();
		Imgproc.warpAffine(img, dest, M, new Size(cols, rows));
		return new Tuple<Mat, Mat>(dest, M);
	}

	@SuppressWarnings("unchecked")
	private void findLeftGroup(SuperMat image, ColorPos colorpos) throws BiomerieuxException, IOException, ParserConfigurationException, SAXException {
		double numb_gaps = 19.5;
		int gap = getCenters().gap_estimation();                                      //
		List<Integer> grids = map_mod(getCentersX(), gap);                            // possible grids
		SmartCapsules tempCaps;                                                       // temporal capsules 
		Collections.sort(grids);													  //
		grids = map_index(grids, map_biggerThan(Sci.diff(grids), gap * 0.01)); 
		Rect[] orig_R = new Rect [getCentersX().size()];
		Centers original_centers = new Centers(getCentersX(), getCentersY());
		for (int i = 0; i < getCentersX().size(); i++) orig_R[i] = this.getCapsules()[i];
		int y = (int) Math.round(Sci.mean(getCentersY()));
		List<Integer> lsY = new ArrayList<Integer>();
		for (int i = 0; i < MAX_NUM_CAPSULES; i++) lsY.add(y);

		Centers vCe = getCenters();
		Centers best_confg = null;
		int best_value = -1;
		for (int g : grids) {                                                         // Taking every possible grid
			int x0 = (g > gap / 3) ? g: g + gap;                                        // Taking first point in grid                                                               // If left it must be moved
			int last_x = (int) Math.round(x0 + numb_gaps * gap);                           // Last point in group
			while (last_x < image.width()) {												// While last point is inside the image
				tempCaps = new SmartCapsules(new Capsules(orig_R));                         // Recover the original capsules
				vCe = tempCaps.getCenters();
				vCe.grid_centers_left(x0, gap, 10, 0);													// Complete centers
				tempCaps = new SmartCapsules(new Capsules(vCe, gap));
				List<RgbColor> vCo = tempCaps.get_color(image);                                 // Taken colors
				int value = colorpos.eval_color_vs_pos(vCo, Sci.range(1, 11, Integer.class));  // Evaluates colors versus positions
				if (value > best_value) {                                                   // Taking the biggest evaluation  
					best_value = value;
					best_confg = vCe;
				}
				x0 += gap;
				last_x = (int) Math.round(x0 + numb_gaps * gap);
			}
		}
		int f_i = 0;
		while (original_centers.getX(f_i) < best_confg.getX(9)) f_i++;
		Rect LRect [] = new Rect[original_centers.getX().size() - f_i + 10];
		for (int i = 0; i < 10; i++) LRect[i] = new Rect(best_confg.getX(i) - (int) (gap / 2), best_confg.getY(i) - (int) (gap / 2), gap, gap);
		for (int i = f_i, j = 10; i < original_centers.getX().size(); i++, j++) LRect[j] = orig_R[i];
		setCapsules( LRect);
	}

	@SuppressWarnings({ "unchecked" })
	private void findRightGroup(SuperMat image, ColorPos colorpos) throws BiomerieuxException, IOException, ParserConfigurationException, SAXException {

		Rect[] orig_R = new Rect [getCentersX().size()];
		Centers original_centers = new Centers(getCentersX(), getCentersY());
		for (int i = 0; i < getCentersX().size(); i++) orig_R[i] = this.getCapsules()[i];

		SmartCapsules tempCaps; 
		tempCaps = new SmartCapsules(new Capsules(orig_R));

		Rect[] left_R = new Rect [10];
		for (int i = 0; i < 10; i++) left_R[i] = getCapsules()[i];

		Centers vCe = getCenters();
		Centers best_confg = null;
		int best_value = -1;
		double numb_gaps = 9;

		List<Integer> right_centers_x = new ArrayList<Integer>();
		List<Integer> right_centers_y = new ArrayList<Integer>();
		for (int i = 10; i < original_centers.getX().size(); i++){
			right_centers_x.add(original_centers.getX(i));
			right_centers_y.add(original_centers.getY(i));
		}

		int gap = getCenters().gap_estimation();
		int limit = original_centers.getX(9);
		List<Integer> grids = map_mod(Sci.sum(right_centers_x, -limit), gap);         // possible grids 
		Collections.sort(grids);													  //
		grids = map_index(grids, map_biggerThan(Sci.diff(grids), gap * 0.01));        // Drops out the similar grids

		int y = (int) Math.round(Sci.mean(getCentersY()));
		List<Integer> lsY = new ArrayList<Integer>();
		for (int i = 0; i < MAX_NUM_CAPSULES; i++) lsY.add(y);		

		for (int g : grids) {                                                               // Taking every possible grid
			int x0 = (g > gap / 3) ? g: g + gap;                                            // Taking first point in grid                                                               // If left it must be moved
			x0 += limit;
			int last_x = (int) Math.round(x0 + numb_gaps * gap);                            // Last point in group
			while (last_x < image.width()) {												// While last point is inside the image
				tempCaps = new SmartCapsules(new Capsules(orig_R));                         // Recover the original capsules
				vCe = tempCaps.getCenters();
				vCe.grid_centers_left(x0, gap, 10);
				tempCaps = new SmartCapsules(new Capsules(vCe, gap));
				List<RgbColor> vCo = tempCaps.get_color(image);                                 // Taken colors
				int value = colorpos.eval_color_vs_pos(vCo, Sci.range(1, 21, Integer.class));  // Evaluates colors versus positions
				if (value > best_value) {                                                   // Taking the biggest evaluation  
					best_value = value;
					best_confg = vCe;
				}
				x0 += gap;
				last_x = (int) Math.round(x0 + numb_gaps * gap);
			}
		}		
		Rect goodRect [] = new Rect[MAX_NUM_CAPSULES];
		for (int i = 0; i < MAX_NUM_CAPSULES; i++) goodRect[i] = new Rect(best_confg.getX(i) - (int) (gap / 2), y - (int) (gap / 2), gap, gap);
		setCapsules(goodRect);
	}

	private List<Integer> map_index(List<Integer> ls, List<Integer> index) {
		List<Integer> res = new ArrayList<Integer>();
		int length = Math.min(ls.size(), index.size());
		for (int i=0; i < length; i++) {
			if (index.get(i) > 0) {
				res.add(ls.get(i));
			}
		}
		return res;
	}

	private List<Integer> map_mod(List<Integer> ls, int mod) {
		List<Integer> res = new ArrayList<Integer>();
		for (int i=0; i < ls.size(); i++) {
			res.add(ls.get(i) % mod);
		}
		return res;
	}

	private List<Integer> map_biggerThan(List<Integer> ls, double pivot) {
		for (int i=0; i < ls.size(); i++) {
			ls.set(i, ls.get(i) > pivot ? 1 : 0);
		}
		return ls;
	}

	private static List<Boolean> initializeBooleanList(int length) {
		List<Boolean> lsCorrect = new ArrayList<Boolean>();
		for (int i=0; i < length; i++) {
			lsCorrect.add(false);
		}
		return lsCorrect;
	}

	private  Tuple<Capsules, int[]> prop_rect(int f_l, int l_l, int f_r, int l_r) {
		Rect auxRect;
		int out_ind = -1;
		int ind = -1;
		int lgap = (int) (this.getCapsules()[l_l].x - this.getCapsules()[f_l].x) / (l_l - f_l);
		int rgap = (int) (this.getCapsules()[l_r].x - this.getCapsules()[f_r].x) / (l_r - f_r);

		int g = (l_l - f_l < 9)? lgap: rgap;
		int delta = ((f_l > 0) | f_r > 10)? 1: -1; 

		if (l_l - f_l < 9){
			ind = (delta < 0)? f_l: l_l; 
		} else {
			ind = (delta < 0)? f_r: l_r;
		}
		auxRect = new Rect(this.getCapsules()[ind].x + delta * lgap, this.getCapsules()[ind].y, g, g);

		int gap = (int) (lgap + rgap) / 2;
		if (this.getCapsules()[10].x - this.getCapsules()[9].x < 2.5 * gap) {
			out_ind = (delta < 0)? 9: 10;
		}
		return new Tuple<Capsules, int[]>(new Capsules(new Rect[]{auxRect}), new int[]{out_ind});
	}
}
