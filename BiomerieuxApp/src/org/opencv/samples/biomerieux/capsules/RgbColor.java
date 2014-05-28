package org.opencv.samples.biomerieux.capsules;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;
import org.opencv.samples.biomerieux.utils.Sci;

public class RgbColor {
	public final int r;
	public final int g;
	public final int b;

	public RgbColor(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public static RgbColor averageColor(Mat im)	{
		double[] rgb = new double[3];
		float[] rgbCum = new float[3];
		for (int r = 0; r < im.rows(); r++) {
			for (int c = 0; c < im.cols(); c++) {
				rgb = im.get(r, c);
				rgbCum[0] += (int) rgb[0];
				rgbCum[1] += (int) rgb[1];
				rgbCum[2] += (int) rgb[2];
			}
		}

		RgbColor color = new RgbColor(
				(int) (rgbCum[0] / (im.rows() * im.cols())),
				(int) (rgbCum[1] / (im.rows() * im.cols())),
				(int) (rgbCum[2] / (im.rows() * im.cols())));
		return color;
	}
	
	public double distSet(List<RgbColor> setColor) throws BiomerieuxException{
		List<Double> distAll = new ArrayList<Double>();
		for (int i = 0; i < setColor.size(); i++){
			distAll.add(dist2(setColor.get(i)));
		}
		return Sci.min(distAll); 
	}
	
	public double dist2(RgbColor c) {
		return Math.sqrt((c.r - this.r) * (c.r - this.r) + (c.g - this.g) * (c.g - this.g) + (c.b - this.b) * (c.b - this.b)); 
	}
	
	public String toString() {
		return "(" + r + ", " + g + ", " + b + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + b;
		result = prime * result + g;
		result = prime * result + r;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RgbColor other = (RgbColor) obj;
		if (b != other.b)
			return false;
		if (g != other.g)
			return false;
		if (r != other.r)
			return false;
		return true;
	}
	
}
