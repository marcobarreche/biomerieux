package org.opencv.samples.biomerieux.utils;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.samples.biomerieux.capsules.RgbColor;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;

import android.util.Log;

public class SuperMat extends Mat {
	
	public SuperMat (Mat m) {
		super(m.rows(), m.cols(), m.type());
		m.copyTo(this);
	}
	
	public SuperMat (int rows, int cols, int type) {
		super(rows, cols, type);
	}

	public SuperMat crop (Rect rect) {
		if (rect.x < 0 || rect.y < 0) {
			Log.e("ERROR", "The value of rect cannot be less than 0: "+ rect);
		}
		int x = Math.max(0, rect.x);
		int y = Math.max(0, rect.y);
		return new SuperMat(
				this.submat(
					y, Math.min(y + rect.height, this.rows() -1), x, Math.min(x + rect.width, this.cols() - 1)));
	}
	
	public void paste(Mat img, int left, int top) {
		img.copyTo(this.submat(top,  Math.min(this.rows() - 1, img.rows() + top),  left,  Math.min(this.cols() - 1, img.cols() + left)));
	}
	
	public RgbColor median () throws BiomerieuxException {
		if (this.channels() < 3) {
			throw new BiomerieuxException("The matrix have to have at least 3 channels (r, g, b)");
		}

		List<Integer> r = new ArrayList<Integer>();
		List<Integer> g = new ArrayList<Integer>();
		List<Integer> b = new ArrayList<Integer>();

		for (int row = 0; row < this.height(); row++) {
			for (int col = 0; col < this.width(); col ++) {
				double color [] = this.get(row, col);
				r.add((int)color[0]);
				g.add((int)color[1]);
				b.add((int)color[2]);
			}
		}

		int mr = Double.valueOf(Sci.median(r)).intValue();
		int mg = Double.valueOf(Sci.median(g)).intValue();
		int mb = Double.valueOf(Sci.median(b)).intValue();
		return new RgbColor(mr, mg, mb);
	}

	public RgbColor mean () throws BiomerieuxException {
		if (this.channels() < 3) {
			throw new BiomerieuxException("The matrix have to have at least 3 channels (r, g, b)");
		}

		int r, g, b;
		r = 0;
		g = 0;
		b = 0;
		for (int row = 0; row < this.height(); row++) {
			for (int col = 0; col < this.width(); col ++) {
				double color [] = this.get(row, col);
				r += (int)color[0];
				g += (int)color[1];
				b += (int)color[2];
			}
		}

		int mr = r / (this.height() * this.width());
		int mg = g / (this.height() * this.width());
		int mb = b / (this.height() * this.width());
		return new RgbColor(mr, mg, mb);
	}
}
