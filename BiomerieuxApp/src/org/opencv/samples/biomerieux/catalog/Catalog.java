package org.opencv.samples.biomerieux.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.samples.biomerieux.capsules.SmartCapsules;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;
import org.opencv.samples.biomerieux.utils.CsvReader;
import org.opencv.samples.biomerieux.utils.Tuple;

public class Catalog {
	public static String FILENAMEPATH = "api20e.csv";
	private Map<String, String> entriesByCode;
	private Map<String, String> entriesByBin;
	int nBits;
	int nDigits;
	
	public Catalog(InputStream is, int nBits) throws IOException {
		this.nBits = nBits;
		this.nDigits = (int) Math.ceil(nBits / 3.0);
		entriesByCode = readCatalog(is, nBits);
		entriesByBin = new HashMap<String, String>();

		for (Map.Entry<String, String> entry: entriesByCode.entrySet()) {
			String bin = codeToBin(entry.getKey(), nBits);
			entriesByBin.put(bin, entry.getValue());
		}
	}
	
	/**
	 * Converts 543... into 101001110...
	 * 
	 * @param code
	 * @param nBits
	 * @return
	 */
	public static String codeToBin(String code, int nBits) {
		String rcode = new StringBuilder(code).reverse().toString();
	    int i = Integer.parseInt(rcode, 8);
	    String rbin = Integer.toBinaryString(i);
	    String bin = new StringBuilder(rbin).reverse().toString();
	    return String.format("%-" + nBits + "s", bin).replace(' ', '0');
	}
	
	/**
	 * Converts 101001110... into 543...
	 *  
	 * @param code
	 * @param nBits
	 * @return
	 */
	public static String binToCode(String bin, int nDigits) {
		String rbin = new StringBuilder(bin).reverse().toString();
		int i = Integer.parseInt(rbin, 2);
		String rcode = Integer.toOctalString(i);
		String code = new StringBuilder(rcode).reverse().toString();
	    return String.format("%-" + nDigits + "s", code).replace(' ', '0');
	}

    /**
     * 
     * @param probabilitiesOfOne
     * @param bins
     * @return an array of (probability, binary) sorted by the most likely candidate.
     */
    public ArrayList<Tuple<Double, String>> bestCandidates(double[] probabilitiesOfOne, Set<String> bins) {
		assert(probabilitiesOfOne.length == nBits);

		ArrayList<Tuple<Double, String>> catalogProbs = new ArrayList<Tuple<Double, String>>();

		for (String bin: bins) {
			catalogProbs.add(
				new Tuple<Double, String>(
					Double.valueOf(totalProb(probabilitiesOfOne, bin)),
					bin
					)
				);
		}
		Collections.sort(catalogProbs, new Comparator<Tuple<Double, String>>() {
			@Override
			public int compare(Tuple<Double, String> t1, Tuple<Double, String> t2) {
				return t1.x < t2.x ? 1 : (t1.x > t2.x ? -1 : 0);
			}
		});
	    return catalogProbs;
	}
    
	/**
	 * Searches the most likely entry given a list [0.1, 0.9, 0.94, 0.02, ...].
	 * 
	 * @param probabilitiesOfOne
	 * @return Bacteria(probability, bin code, oct code, bactery name) the most
	 * probable match given a list of probabilities of having each bit equal to 1.
	 */
    public CatalogEntryWithProb searchp(double[] probabilitiesOfOne) {
        ArrayList<Tuple<Double, String>> probBin = bestCandidates(probabilitiesOfOne, entriesByBin.keySet());
        Tuple<Double, String> mostProbableBin = probBin.get(0);
        String bin = mostProbableBin.y;
        double prob = mostProbableBin.x.doubleValue();
        String code = binToCode(bin, nDigits);
        return new CatalogEntryWithProb(prob, bin, code, entriesByBin.get(bin));
    }

    /**
     * Searches the most likely entry given a string "11001100101100110010".
     *
     * We randomly give a probability of 0.9 to each bit in the reading.
     * 
     * @param bin
     * @return Bacteria(probability, bin code, oct code, bactery name) the most
     * probable match given a candidate reading.
     */
    public CatalogEntryWithProb searchb(String bin) {
    	double[] probabilitiesOfOne = new double[bin.length()];
    	for (int i = 0; i < bin.length(); i++) {
    		char b = bin.charAt(i);
    		probabilitiesOfOne[i] = b == '1' ? 0.9 : 0.1;
    	}
        return searchp(probabilitiesOfOne);
    }
    
    /**
     * Searches the most likely entry given a list of Boolean [True, False, False, True, ...]
     *
     * We randomly give a probability of 0.9 to each bit in the reading.
     * 
     * @param bin
     * @return Bacteria(probability, bin code, oct code, bactery name) the most
     * probable match given a candidate reading.
     */
    public CatalogEntryWithProb searchb(List<Boolean> bin) {
    	double[] probabilitiesOfOne = new double[bin.size()];
    	for (int i = 0; i < bin.size(); i++) {
    		probabilitiesOfOne[i] = bin.get(i) ? 0.9 : 0.1;
    	}
        return searchp(probabilitiesOfOne);
    }

    /**
     * Searches the most likely entry given a string "673251".
     * 
     * We randomly give a probability of 0.9 to each bit in the reading.
     * 
     * @param code
     * @return Bacteria(probability, bin code, oct code, bactery name) the most
     * probable match given a candidate reading.
     */
    public CatalogEntryWithProb searchc(String code) {
        String bin = codeToBin(code, nBits);
        return searchb(bin);   
    }

    private static Map<String, String> readCatalog(InputStream is, int nBits) throws IOException {
		Map<String, String> entries = new HashMap<String, String>();
		ArrayList<String[]> lines = CsvReader.parse(is);
		for (String[] col: lines) {
			if (col.length != 2)
				continue;
			String code = col[0];
			String entryName = col[1];
			entries.put(code,  entryName);
		}
		return entries;
	}

    private static double totalProb(double[] probabilitiesOfOne, String catalogKey) {
    	double totalProduct = 1.0;

    	for (int i = 0; i < probabilitiesOfOne.length; i++) {
    		double p = probabilitiesOfOne[i];
    		char b = catalogKey.charAt(i);
    		totalProduct *= (b == '1') ? p : (1 - p);
    	}

    	return totalProduct;
    }
    
    public static String eval_to_binary(List<Boolean> evaluation) {
		String res = "";
		for (int i=0; i< evaluation.size(); i++) {
			res += (evaluation.get(i)) ? "1" : "0";
		}
		return res;
	}
	
	public static int bin_distance(String bin1, String bin2) {
		int res = 0;
		for (int i=0; i< bin1.length(); i++) {
			res += (bin1.charAt(i) == bin2.charAt(i)) ? 0: 1;
		}
		return res;
	}
	
	public static String bin_human_read(String bin_code) {
		int f = 0;
		int l = 3;

		String b_list = "";
		for (int i = f; i < l; i++) {
			b_list += (bin_code.charAt(i) == '1') ? "+" : "-"; 
		}
		
		while (l < SmartCapsules.MAX_NUM_CAPSULES) {
			f += 3;
			l = Math.min(f + 3, SmartCapsules.MAX_NUM_CAPSULES + 1);
			if (f < l) {
				b_list += " ";
			}
			for (int i = f; i < l; i++) {
				b_list += (bin_code.charAt(i) == '1') ? "+" : "-"; 
			}
		}
		return b_list;
	}
	
	public Map<Integer, List<String>> conclusion (String binary_code) {
		Map<Integer, List<String>> conclusion = new HashMap<Integer, List<String>>();
		for (String key: entriesByCode.keySet()) {
			int d = bin_distance(binary_code, codeToBin(key, key.length()));
			
			List<String> valueConclusion = conclusion.get(d);
			if (valueConclusion == null) {
				valueConclusion = new ArrayList<String>();
			}
			valueConclusion.add(key);
			conclusion.put(d, valueConclusion);
		}
		return conclusion;
	}
	
	public String conclusion2 (Map<Integer, List<String>> conc_dict) throws BiomerieuxException {
		if (conc_dict.containsKey(0)) {
			return this.entriesByBin.get(conc_dict.get(0).get(0));
		}
		return null;
	}
}