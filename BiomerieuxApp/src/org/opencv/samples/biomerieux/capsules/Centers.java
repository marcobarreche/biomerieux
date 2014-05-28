package org.opencv.samples.biomerieux.capsules;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;
import org.opencv.samples.biomerieux.utils.PolynomialFit;
import org.opencv.samples.biomerieux.utils.Sci;
import org.opencv.samples.biomerieux.utils.Tuple;


public class Centers {
	private List<Integer> x;
	private List<Integer> y;
	
	public Centers(List x, List y) {
		this.x = new ArrayList<Integer>();
		this.y = new ArrayList<Integer>();
		
		if (y.size() < x.size()){
			for (int i = y. size(); i < x.size(); i++){
				y.add(y.get(0));
			}
		}
		for (int i = 0; i < x.size(); i++){
			if (x.get(i).getClass().getName().equals("java.lang.Integer")){
				this.x.add(((Integer) x.get(i)).intValue());
			} else {
				this.x.add(((Double) x.get(i)).intValue());
			}
			if (y.get(i).getClass().getName().equals("java.lang.Integer")){
				this.y.add(((Integer) y.get(i)).intValue());
			} else {
				this.y.add(((Double) y.get(i)).intValue());
			}			
		}
	}
	
	public Centers(List<Integer> x, double y) {
		this.x = new ArrayList<Integer>(x);
		this.y = Sci.ones(this.x.size(), Double.valueOf(Math.round(y)).intValue());
	}
	
	public void set(List<Integer> x, List<Integer> y) {
		this.x = new ArrayList<Integer>(x);
		this.y = new ArrayList<Integer>(y);
	}
	
	public Centers gridCenters(int from, int x0, int y0, int gap){
		List<Integer> x = new ArrayList<Integer>();
		List<Integer> y = new ArrayList<Integer>();
		for (int i = 0; i < from; i ++ ){
			x.add(getX(i));
			y.add(getY(i));
		}
		for (int i = 0; i < 10; i ++ ){
			x.add(x0 + i * gap);
			y.add(y0);
		}
		return new Centers(x, y);  
	}
	
	public void add (Centers c) {
		this.x.addAll(c.getX());
		this.y.addAll(c.getY());
	}
	
	public List<Integer> getX() {
		return x;
	}

	public List<Integer> getY() {
		return y;
	}
	
	public int getX(int i) {
		return x.get(i);
	}

	public int getY(int i) {
		return y.get(i);
	}
	
	public double meanY () throws BiomerieuxException {
		return Sci.mean(y);
	}

	public int gap_estimation() throws BiomerieuxException {
		return Double.valueOf(Sci.median(Sci.diff(x))).intValue();
	}
	
	public String toString() {
		String res = "[";
		for (int i= 0; i< this.x.size(); i++) {
			res += "(" + this.x.get(i) + "," + this.y.get(i) + "), ";
		}
		return res + "]";
	}

	public Tuple<Double, Double> polyfit () {
		double [] xd = fromListInteger2arrayDouble(x);
		double [] yd = fromListInteger2arrayDouble(y);
		PolynomialFit p = new PolynomialFit(1);
		p.fit(xd, yd);
		double [] res = p.getCoef();
		return new Tuple<Double, Double>(res[0], res[1]);
	}
	
	public void rotating_coordOLD(Mat rot_matrix) throws BiomerieuxException {
		if (rot_matrix.rows() != 2 && rot_matrix.cols() != 3) {
			throw new BiomerieuxException("The rotation Matrix have to have 2 rows and 3 columns.");
		}
		
		double dot [] = new double [3];
		List<Integer> z = Sci.ones(x.size(), 1);

		double [] element_matrix = new double [1];
		for (int row = 0; row < rot_matrix.rows(); row ++) {
			for (int col = 0; col < x.size(); col++) {
				for (int deep = 0; deep < 3; deep++) {
					rot_matrix.get(row, deep, element_matrix);
					dot[deep] = getElement(x, y, z, deep) * element_matrix[0];
				}
				if (row == 0) {
					x.set(col, sumAllElements(dot));
				} else {
					y.set(col, sumAllElements(dot));
				}
			}
		}
	}

	public void rotating_coord(Mat rot_matrix) throws BiomerieuxException {
		if (rot_matrix.rows() != 2 && rot_matrix.cols() != 3) {
			throw new BiomerieuxException("The rotation Matrix have to have 2 rows and 3 columns.");
		}
		
		List<Integer> rX = new ArrayList<Integer>();
		List<Integer> rY = new ArrayList<Integer>();
		double resX;
		double resY;

		//double [] element_matrix = new double [1];
		
		for (int i = 0; i < getX().size(); i++) {
			resX = rot_matrix.get(0, 0)[0] * getX(i);
			resX += rot_matrix.get(0, 1)[0] * getY(i);
			resX += rot_matrix.get(0, 1)[0];
			rX.add((int) Math.round(resX));
			resY = rot_matrix.get(1, 0)[0] * getX(i);
			resY += rot_matrix.get(1, 1)[0] * getY(i);
			resY += rot_matrix.get(1, 1)[0];
			rY.add((int) Math.round(resY));
		}
		set(rX, rY);
	}
	
	
	private void correct_first(int gap, List<Integer> old_x, double max_error, int first_pos) throws BiomerieuxException{
		int k = first_pos;
		int actual_x = getX(k);
		while (!old_x.contains(actual_x) && (k < 9) ) {
			k++;
			actual_x = getX(k);
		}
		if (old_x.contains(actual_x)) {
			if (k > first_pos){
				for (int i = k - 1; i > first_pos - 1; i--) {
					actual_x -= gap;
					this.x.set(i, actual_x);
				}
			}
		}	
	}

	public void grid_centers_left(int x0, int gap, int number, int first_pos) throws BiomerieuxException{
		double max_error = 0.25 * gap;
		List <Integer> old_x = new ArrayList<Integer>();
		for (int i = 0; i < this.x.size(); i++) old_x.add(this.x.get(i));
		
		int sum = 0;
		int count = 0;
		for (int i = first_pos; i < getY().size(); i++) {
			sum += getY(i);
			count++;
		}
		int y = (int) sum / count;
		// Erase old data
		this.x.clear();  
		this.y.clear();  
		// Initialization
		for (int i = 0; i < first_pos;  i++) {
			this.x.add(old_x.get(i));
			this.y.add(y);
		}
		if (first_pos > 0) x0 = Math.max(x0, old_x.get(first_pos - 1));           // Correction of first value	
		
		int k;
		int actual_x = x0;
		for (count = 0; count < 10;  count++) {
			k = Sci.argmin(Sci.abs(Sci.sum(old_x, - actual_x)));
			double small = Double.valueOf(Math.abs(old_x.get(k) - actual_x));
			actual_x = (small > max_error)? actual_x : old_x.get(k);  
			this.x.add(actual_x);
			this.y.add(y);
			actual_x += gap;
		}
		correct_first(gap, old_x, max_error, first_pos);                           // Correct first centers's estimation
	}
	
	public void grid_centers_left(int x0, int gap, int number) throws BiomerieuxException{
		double max_error = 0.25 * gap;
		List <Integer> old_x = new ArrayList<Integer>();
		for (int i = 0; i < this.x.size(); i++) old_x.add(this.x.get(i));
		
				
		int sum = 0;
		int count = 0;
		for (int i = 10; i < getY().size(); i++) {
			sum += getY(i);
			count++;
		}
		int y = (int) sum / count;
		// Erase old data
		this.x.clear();  // = new ArrayList<Integer>();
		this.y.clear();  // = new ArrayList<Integer>();
		// Initialization
		for (int i = 0; i < 10;  i++) {
			this.x.add(old_x.get(i));
			this.y.add(y);
		}
		x0 = Math.max(x0, old_x.get(9));           // Correction of first value	
		
		int k;
		int actual_x = x0;
		for (count = 0; count < 10;  count++) {
			k = Sci.argmin(Sci.abs(Sci.sum(old_x, - actual_x)));
			double small = Double.valueOf(Math.abs(old_x.get(k) - actual_x));
			actual_x = (small > max_error)? actual_x : old_x.get(k);  
			this.x.add(actual_x);
			this.y.add(y);
			actual_x += gap;
		}
		correct_first(gap, old_x, max_error, 10);                           // Correct first centers's estimation
	}
	
	public void grid_centers(Centers pCters) throws BiomerieuxException {
		double max_error = 0.25 * pCters.gap_estimation();
		int dim = pCters.getX().size();
		int y = (int) Sci.mean(pCters.getY());
		int k;
		List <Integer> old_x = new ArrayList<Integer>();
		for (int i = 0; i < this.x.size(); i++) old_x.add(this.x.get(i));
		for (int i = 0; i < dim;  i++) {
			k = Sci.argmin(Sci.abs(Sci.sum(old_x, -pCters.getX(i))));
			double small = Double.valueOf(Math.abs(old_x.get(k) - pCters.getX(i)));
			if (i < this.x.size()) {
				this.x.set(i, (small > max_error)? pCters.getX(i) : old_x.get(k));
				this.y.set(i, y);
			} else {
				this.x.add((small > max_error)? pCters.getX(i) : old_x.get(k));
				this.y.add(i);
			}
		}
	}
	
	public Centers filterLT (int num) {
		List<Integer> lx = new ArrayList<Integer>();
		List<Integer> ly = new ArrayList<Integer>();
		
		for (int i = 0; i < x.size(); i++) {
			if (x.get(i) < num) {
				lx.add(i);
				ly.add(i);
			}
		}
		return new Centers(lx, ly);
	}
	
	public Centers filterGT (int num) {
		List<Integer> lx = new ArrayList<Integer>();
		List<Integer> ly = new ArrayList<Integer>();
		
		for (int i = 0; i < x.size(); i++) {
			if (x.get(i) > num) {
				lx.add(i);
				ly.add(i);
			}
		}
		return new Centers(lx, ly);
	}
	
	private static int sumAllElements(double [] elements) {
		int res = 0;
		for (int i = 0; i < elements.length; i++) {
			res += (int) elements[i];
		}
		return res;
	}
	
	private static int getElement(List<Integer> x, List<Integer> y, List<Integer> z, int i) {
		switch(i) {
		case 0:
			return x.get(i);
		case 1:
			return y.get(i);
		default:
			return z.get(i);
		}
	}

	private static double [] fromListInteger2arrayDouble(List<Integer> ls) {
		double [] res = new double[ls.size()];
		for (int i=0; i<ls.size(); i++) {
			res[i] = ls.get(i);
		}
		return res;
	}
}
