package org.opencv.samples.biomerieux.capsules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;
import org.opencv.samples.biomerieux.utils.Sci;
import org.opencv.samples.biomerieux.utils.Tuple;


public class Capsules {
	public final static int LEFT_COMPONENT = 0;
	public final static int TOP_COMPONENT = 1;
	public final static int WIDTH_COMPONENT = 2;
	public final static int HEIGHT_COMPONENT = 3;

	public final static double THRESHOLD_SIZE = 0.3;
	public final static double THRESHOLD_ALIGN = 0.5;
	public final static double MAX_ERROR = 0.25;

	private Rect [] capsules;

	public Capsules(Rect [] capsules) {
		this.capsules = capsules;
	}

	private Rect[] buildCapsules(Centers C, int w, int h){
		int dim = C.getX().size();
		Rect capsules [] = new Rect[dim];
		for (int i = 0; i < dim; i++){
			capsules[i] = new Rect(C.getX(i)- w / 2, C.getY(i) - h / 2, w, h);
		}
		return capsules;
	}
	
	public void add(Capsules cap) throws BiomerieuxException {
		Rect res [] = new Rect[cap.getCapsules().length + capsules.length];
		for (int i=0; i < capsules.length; i++) {
			res[i] = capsules[i];
		}
		int j = 0;
		for (int i=capsules.length; i < res.length; i++) {
			res[i] = cap.getCapsules()[j];
			j++;
		}
		this.capsules = res;
		this.sort();
	}
	
	public Capsules(Centers C, int w){
		this.capsules = buildCapsules(C, w, w);
	}
	
	public Capsules(Centers C, int w, int h){
		this.capsules = buildCapsules(C, w, h);
	}

	public Rect [] getCapsules () {
		return capsules;
	}

	public Tuple<Double, Double> regression_line () throws BiomerieuxException {
		return this.getCenters().polyfit();
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getCentersX () throws BiomerieuxException {
		List<Integer> lefts = getComponentObject(LEFT_COMPONENT);
		List<Integer> width = getComponentObject(WIDTH_COMPONENT);
		return Sci.sumLists(lefts, Sci.div(width, 2));
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getCentersY () throws BiomerieuxException {
		List<Integer> tops = getComponentObject(TOP_COMPONENT);
		List<Integer> height = getComponentObject(HEIGHT_COMPONENT);
		return Sci.sumLists(tops, Sci.div(height, 2));	
	}

	public Centers getCenters () throws BiomerieuxException {
		return new Centers(getCentersX (), getCentersY ());
	}
	
	public void sort() throws BiomerieuxException {
		List<Rect> res = Arrays.asList(getCapsules());
		Collections.sort(res, new Comparator<Rect>() {
			@Override
			public int compare(Rect r1, Rect r2) {
				return r1.x - r2.x;
			}
		});
		capsules =  (Rect[]) res.toArray();
	}

	public List<Rect> getCapsulesList () {
		return new ArrayList<Rect>(Arrays.asList(capsules));
	}

	public void setCapsules(Rect [] capsules) {
		this.capsules = capsules;
	}

	public List<Integer> getComponentObject (int component) {
		List<Integer> ls = new ArrayList<Integer>();
		for (int i=0; i < capsules.length; i++) {
			ls.add(getComponent(capsules[i], component));
		}
		return ls;
	}

	@SuppressWarnings("unchecked")
	public void filter_capsules_same_info() throws BiomerieuxException {
		List<Integer> vXdiff = Sci.diff(getCentersX());
		List<Integer> vYdiff = Sci.diff(getCentersY());
		int x, y;
		List<Double> gaps = new ArrayList<Double>();
		for (int i = 0; i < vXdiff.size(); i++){
			x = vXdiff.get(i);
			y = vYdiff.get(i);
			gaps.add(Math.sqrt(x * x + y * y));
		}
		double gap = Sci.median(gaps);

		List<Rect> filtered_Caps = new ArrayList<Rect>();
		filtered_Caps.add(capsules[0].clone());
		for (int i = 1; i < capsules.length; i++) {
			if (gaps.get(i - 1) > gap / 2){
				filtered_Caps.add(capsules[i].clone());
			}
		}
		this.setCapsules(filtered_Caps.toArray(new Rect[filtered_Caps.size()]));
	}
	
	public Rect getSelection(int i) {
		Rect aux = this.getCapsules()[i];
		return new Rect(aux.x, aux.y, aux.width, 2*aux.height);
	}

	private double medianWidth () throws BiomerieuxException {
		List<Double> widths = Sci.int2Double(getComponentObject(WIDTH_COMPONENT));
		return Sci.median(widths);
	}

	private double meanWidth () throws BiomerieuxException {
		List<Double> widths = Sci.int2Double(getComponentObject(WIDTH_COMPONENT));
		return Sci.mean(widths);
	}

	@SuppressWarnings("unchecked")
	public void filter_capsules_by_alignment() throws BiomerieuxException {
		List<Integer> vX = Sci.sumLists(
				getComponentObject(LEFT_COMPONENT),
				Sci.div(getComponentObject(WIDTH_COMPONENT), 2));
		List<Integer> vY = Sci.sumLists(
				getComponentObject(TOP_COMPONENT),
				Sci.div(getComponentObject(HEIGHT_COMPONENT), 2));
		
		Tuple<Double, Double> coef = this.getCenters().polyfit();
		List<Rect> myCapsules = this.getCapsulesList();
		Centers Filter_centers = new Centers(vX, vY);
		int max_error = new Centers(vX, vY).gap_estimation();

 		double big = Sci.max(Sci.abs(Sci.subLists(Sci.sum(Sci.mult(vX, coef.y), coef.x), vY)));
		while(big > THRESHOLD_ALIGN * max_error){  //Sci.mean(Sci.abs(Sci.subLists(Sci.sum(Sci.mult(vX, coef.y), coef.x), vY)))) {
			int i = Sci.argmax(Sci.abs(Sci.subLists(Sci.sum(Sci.mult(vX, coef.y), coef.x), vY)));
			myCapsules.remove(i);
			vX.remove(i);
			vY.remove(i);
			Filter_centers = new Centers(vX, vY);
			coef = Filter_centers.polyfit();
			big = Sci.max(Sci.abs(Sci.subLists(Sci.sum(Sci.mult(vX, coef.y), coef.x), vY)));
		}
		this.setCapsules(myCapsules.toArray(new Rect[myCapsules.size()]));
	}

	private static int getComponent(Rect object, int component) {
		int res = 0;
		switch(component) {
		case LEFT_COMPONENT: res = object.x;
		break;
		case TOP_COMPONENT: res = object.y;
		break;
		case WIDTH_COMPONENT: res = object.width;
		break;
		case HEIGHT_COMPONENT: res = object.height;
		break;
		}
		return res;
	}

	public void filter_by_size() throws BiomerieuxException {
		double median = medianWidth();
		double mean = meanWidth();

		List<Rect> res = new ArrayList<Rect>();
		for (int i=0; i < capsules.length; i++) {
			Rect element = capsules[i];

			double less = Math.abs(element.width - median);
			double big = THRESHOLD_SIZE * mean;
			if (less < big) {
				res.add(element.clone());
			}
		}
		this.setCapsules(res.toArray(new Rect[res.size()]));
	}
	
	public String toString() {
		String res = "[";
		for (Rect element: capsules) {
			res += element.toString() + ", ";
		}
		return res + "]";
	}
	
	public String createJsonString() throws JSONException {
		JSONArray res = new JSONArray();
		for (Rect rect : capsules) {
			JSONObject element = new JSONObject();
			element.put("left", rect.x);
			element.put("top", rect.y);
			element.put("width", rect.width);
			element.put("height", rect.height);
			res.put(element);
		}
		return res.toString();
	}
	
	public static Capsules parseJsonCapsules(String json) throws JSONException {
		JSONArray array = new JSONArray(json);
		Rect [] res = new Rect [array.length()];
		
		for (int i=0; i < array.length(); i++) {
			JSONObject obj = (JSONObject) array.get(i);
			res[i] = new Rect(obj.optInt("left"), obj.optInt("top"), obj.optInt("width"), obj.optInt("height"));
		}
	
		return new Capsules(res);
	}

	public void paint (Mat rgba, Scalar color, int tickness) {
		for (int i = 0; i < capsules.length; i++)
	        Core.rectangle(
	        	rgba,
	        	new Point(capsules[i].x - tickness, capsules[i].y - tickness),
	        	new Point(capsules[i].x + capsules[i].width + 2 * tickness, capsules[i].y + capsules[i].height + tickness),
	        	color, tickness);
		}
}
