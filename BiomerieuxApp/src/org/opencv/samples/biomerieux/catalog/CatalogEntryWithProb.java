package org.opencv.samples.biomerieux.catalog;

public class CatalogEntryWithProb {
	public double prob;
	public String bin;
	public String code;
	public String name;

    public CatalogEntryWithProb(double prob, String bin, String code, String name) {
    	this.prob = prob;
    	this.bin = bin;
    	this.code = code;
    	this.name = name;
    }
    
    public String toString() {
    	return "(prob: " + this.prob + ", bin: " + this.bin + ", code: " + 
    			this.code + ", name: " + this.name + ")";
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bin == null) ? 0 : bin.hashCode());
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		long temp;
		temp = Double.doubleToLongBits(prob);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		CatalogEntryWithProb other = (CatalogEntryWithProb) obj;
		if (bin == null) {
			if (other.bin != null)
				return false;
		} else if (!bin.equals(other.bin))
			return false;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(prob) != Double
				.doubleToLongBits(other.prob))
			return false;
		return true;
	}
    
    
}
