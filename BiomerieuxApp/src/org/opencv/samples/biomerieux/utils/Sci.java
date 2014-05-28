package org.opencv.samples.biomerieux.utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;

public class Sci<T> {
	
	@SuppressWarnings("rawtypes")
	private static List getInitialList(List ls) throws BiomerieuxException {
		List res;
		if (ls.isEmpty()) {
			throw new BiomerieuxException("It has not been possible to return an initialized list.");
		}
		Class c = ls.get(0).getClass();
		if (c.equals(Double.class)) {
			res = new ArrayList<Double>();
		} else {
			res = new ArrayList<Integer>();
		} 
		return res;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List abs (List ls) throws BiomerieuxException {		
		List res = getInitialList(ls);
		Class c = ls.get(0).getClass();
		
		if (c.equals(Double.class)) {
			for (int i = 0; i < ls.size(); i++)
				res.add(Math.abs(((Double) ls.get(i))));
		} else {
			for (int i = 0; i < ls.size(); i++)
				res.add(Math.abs(((Integer) ls.get(i))));
		}
		return res;
	}

	@SuppressWarnings("rawtypes")
	public static double mean (List ls) throws BiomerieuxException {
		if (ls.isEmpty()) {
			throw new BiomerieuxException("It was not possible to calculate the mean value in a empty list");
		}
		double res = 0;
		if (ls.get(0).getClass().equals(Double.class)) {
			for (int i=0; i < ls.size(); i++) {	
				res += (Double)ls.get(i);
			}
		} else {
			for (int i=0; i < ls.size(); i++) {	
				res += (Integer)ls.get(i);
			}
		}
		return res / ls.size();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List mult (List ls, double num) throws BiomerieuxException {
		List res = getInitialList(ls);
		if (ls.get(0).getClass().equals(Double.class)) {
			for (int i = 0; i < ls.size(); i++) {
				res.add((Double)ls.get(i) * num);
			}
		} else {
			for (int i = 0; i < ls.size(); i++) {
				res.add((Integer)ls.get(i) * num);
			}
		}
		return res;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List sum (List ls, double num) throws BiomerieuxException {
		List res = getInitialList(ls);
		if (ls.get(0).getClass().equals(Double.class)) {
			for (int i = 0; i < ls.size(); i++) {
				res.add((Double)ls.get(i) + num);
			}
		} else {
			for (int i = 0; i < ls.size(); i++) {
				res.add((Integer)ls.get(i) + num);
			}
		}
		return res;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List sum (List ls, int num) throws BiomerieuxException {
		List res = getInitialList(ls);
		if (ls.get(0).getClass().equals(Double.class)) {
			for (int i = 0; i < ls.size(); i++) {
				res.add((Double)ls.get(i) + num);
			}
		} else {
			for (int i = 0; i < ls.size(); i++) {
				res.add((Integer)ls.get(i) + num);
			}
		}
		return res;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List div (List ls, double num) throws BiomerieuxException {
		List res = getInitialList(ls);
		if (ls.get(0).getClass().equals(Double.class)) {
			for (int i = 0; i < ls.size(); i++) {
				res.add((Double)ls.get(i) / num);
			}
		} else {
			for (int i = 0; i < ls.size(); i++) {
				res.add((int)((Integer)ls.get(i) / num));
			}
		}
		return res;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List sumLists (List lsA, List lsB) throws BiomerieuxException {
		List res = getInitialList(lsA);
		int length = Math.min(lsA.size(), lsB.size());
		if (lsA.get(0).getClass().equals(Double.class)) {
			for (int i = 0; i < length; i++)
				res.add((Double)lsA.get(i) + (Double)lsB.get(i));
		} else {
			for (int i = 0; i < length; i++)
				res.add((Integer)lsA.get(i) + (Integer)lsB.get(i));
		}
		
		return res;
	}
		
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List subLists (List lsA, List lsB) throws BiomerieuxException {
		List res = getInitialList(lsA);
		int length = Math.min(lsA.size(), lsB.size());
		List<Double> dlsB;
		List<Integer> ilsB;
	
		if (lsB.get(0).getClass().equals(Integer.class)) {
			dlsB = int2Double(lsB);
			ilsB = lsB;
		} else {
			ilsB = double2Int(lsB);
			dlsB = lsB;
		}
		if (lsA.get(0).getClass().equals(Double.class)) {
			for (int i = 0; i < length; i++)
				res.add(Double.valueOf((Double)lsA.get(i)) - Double.valueOf((Double)dlsB.get(i)));
		} else {
			for (int i = 0; i < length; i++)
				res.add((Integer)lsA.get(i) - (Integer)ilsB.get(i));
		}
		
		return res;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List range (int start, int end, Class class_exit) {
		double length = (end - start);
		List res;
		if (class_exit.equals(Double.class)) {
			res = new ArrayList<Double>();
			for (int i = 0; i < length; i++)
				 res.add((Double)((i + start) * 1.0));
		} else {
			res = new ArrayList<Integer>();
			for (int i = 0; i < length; i++)
				 res.add((Integer)(i + start));
		}
		return res;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static double median (List ls) throws BiomerieuxException {
		if (ls.isEmpty()) {
			throw new BiomerieuxException("It was not possible to calculate the median value in a empty list");
		}
		double res, pivot_half, pivot_half_prev;
		int pivot = ls.size() / 2;
		List cls;
		if (ls.get(0).getClass().equals(Double.class)) {
			cls = new ArrayList<Double>(ls);
			Collections.sort(cls);
			pivot_half = (Double)cls.get(pivot);
			pivot_half_prev = (Double)cls.get(pivot - 1);
		} else {
			cls = new ArrayList<Integer>(ls);
			Collections.sort(cls);
			pivot_half = (Integer)cls.get(pivot);
			pivot_half_prev = (Integer)cls.get(pivot - 1);
		}		
		if (ls.size() % 2 == 0) {
			res = (pivot_half + pivot_half_prev) / 2;
		} else {
			res = pivot_half;
		}
		return res;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List diff (List ls) throws BiomerieuxException {
		List res = getInitialList(ls);
		if (ls.get(0).getClass().equals(Double.class)) {
			for (int i = 1; i < ls.size(); i++) {
				res.add((Double)ls.get(i) - (Double)ls.get(i - 1));
			}
		} else {
			for (int i = 1; i < ls.size(); i++) {
				res.add((Integer)ls.get(i) - (Integer)ls.get(i - 1));
			}
		}
		return res;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static double min (List ls) throws BiomerieuxException {
		if (ls.isEmpty()) {
			throw new BiomerieuxException("It was not possible to calculate the min value in a empty list");
		}
		if (ls.get(0).getClass().equals(Double.class)) {
			return (Double)ls.get(argmin(ls));
		} else {
			return (Integer)ls.get(argmin(ls));
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static int argmin (List ls) throws BiomerieuxException {
		if (ls.isEmpty()) {
			throw new BiomerieuxException("It was not possible to calculate the min value in a empty list");
		}
		double min = Double.MAX_VALUE;
		int k = -1;
		 
		if (ls.get(0).getClass().equals(Double.class)) {
			for (int i = 0; i < ls.size(); i++) {
				double element = (Double)ls.get(i);
				if (element < min) {
					min = element;
					k = i;
				}
			}
		 } else {
			for (int i = 0; i < ls.size(); i++) {
				int element = (Integer)ls.get(i);
				if (element < min) {
					min = element;
					k = i;
				}
			}
		}
		return k;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static double max (List ls) throws BiomerieuxException {
		if (ls.isEmpty()) {
			throw new BiomerieuxException("It was not possible to calculate the max value in a empty list");
		}
		if (ls.get(0).getClass().equals(Double.class)) {
			return (Double)ls.get(argmax(ls));
		} else {
			return (Integer)ls.get(argmax(ls));
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static int argmax (List ls) throws BiomerieuxException {
		if (ls.isEmpty()) {
			throw new BiomerieuxException("It was not possible to calculate the max value in a empty list");
		}
		 double max = Double.MIN_VALUE;
		 int k = -1;
		 
		 if (ls.get(0).getClass().equals(Double.class)) {
			 for (int i = 0; i < ls.size(); i++) {
				 double element = (Double)ls.get(i);
				 if (element > max) {
					 max = element;
					 k = i;
				 }
			 }
		 } else {
			 for (int i = 0; i < ls.size(); i++) {
				 int element = (Integer)ls.get(i);
				 if (element > max) {
					 max = element;
					 k = i;
				 }
			 }
		 }
		 return k;
	}
	
	public static List<Double> ones (int lenght, double num) {
		List<Double> res = new ArrayList<Double>();
		for (int i = 0; i < lenght; i++) {
			res.add(num);
		}
		return res;
	}
	
	public static List<Integer> ones (int length, int num) {
		List<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < length; i++) {
			res.add(num);
		}
		return res;
	}
	
	public static List<Integer> double2Int(List<Double> ls) {
		List<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < ls.size(); i++) {
			res.add(ls.get(i).intValue());
		}
		return res;
	}
	
	public static List<Double> int2Double(List<Integer> ls) {
		List<Double> res = new ArrayList<Double>();
		for (int i = 0; i < ls.size(); i++) {
			res.add((double)ls.get(i));
		}
		return res;
	}
	
	public static List<Boolean> parseJsonEvaluation (String json) throws JSONException {
		JSONArray array = new JSONArray(json);
		List<Boolean> res = new ArrayList<Boolean>();
		
		for (int i=0; i < array.length(); i++) {
			res.add((Boolean) array.get(i));
		}
	
		return res;
	}
	
	public static String createJsonEvaluation(List<Boolean> ls) {
		JSONArray array = new JSONArray();
		for (int i=0; i < ls.size(); i++) {
			array.put(ls.get(i));
		}
		return array.toString();
	}
	
}
