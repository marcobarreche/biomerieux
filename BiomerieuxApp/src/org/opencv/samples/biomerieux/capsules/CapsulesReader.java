package org.opencv.samples.biomerieux.capsules;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.opencv.core.Mat;
import org.opencv.samples.biomerieux.utils.CsvReader;
import org.opencv.samples.biomerieux.utils.Tuple;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class CapsulesReader {
	private Map<String, RgbColor> colorCapsules;

	public CapsulesReader(InputStream is) throws IOException, ParserConfigurationException, SAXException {
		NodeList nList = CsvReader.getXmlObject(is).getElementsByTagName("capsule");
		colorCapsules = new HashMap<String, RgbColor>();
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String position = eElement.getAttribute("position");
				String result = eElement.getAttribute("result");
				int r = (int) Double.parseDouble(eElement.getAttribute("r"));
				int g = (int) Double.parseDouble(eElement.getAttribute("g"));
				int b = (int) Double.parseDouble(eElement.getAttribute("b"));
				colorCapsules.put(position + result, new RgbColor(r, g, b));
			}
		}
	}
	
	/**
	 * Returns the result and probability of the test assuming this capsule is in pos <pos> (P or N).
	 * @param im
	 * @param pos
	 * @return Tuple("P", 0.9)
	 */
	public Tuple<String, Double> predictCapsule(Mat im, int pos) {
		RgbColor color = RgbColor.averageColor(im);
		double dp = colorCapsules.get(pos + "P").dist2(color);
		double dn = colorCapsules.get(pos + "N").dist2(color);
		if (dp < dn) {
			return new Tuple<String, Double>("P", Double.valueOf(1.0 - (dp / (dp + dn))));
		}
		else {
			return new Tuple<String, Double>("N", Double.valueOf(1.0 - (dn / (dp + dn))));
		}
	}
	
	/*
	 * box is (left, upper, right, bottom)-tuple.
	 * Returns a list of ("category name", <distance to the category>). The largest the distance, the lowest the
	 * probability of this box to belong to this category.
	 * The list is sorted by distance, being the first entry in the list the most likely category.
	**/
	public ArrayList<Tuple<String, RgbColor>> predict(Mat imToDetect, Set<String> choices) {
		ArrayList<Tuple<String, RgbColor>> colorChoices = new ArrayList<Tuple<String, RgbColor>>();
		for (Map.Entry<String, RgbColor> entry: colorCapsules.entrySet()) {
			if (choices == null || choices.contains(entry.getKey())) {
				colorChoices.add(new Tuple<String, RgbColor>(entry.getKey(), entry.getValue()));
			}
		}
		final RgbColor color = RgbColor.averageColor(imToDetect);
		Collections.sort(colorChoices, new Comparator<Tuple<String, RgbColor>>() {
			@Override
			public int compare(Tuple<String, RgbColor> t1, Tuple<String, RgbColor> t2) {
				return (int) (t1.y.dist2(color) - t2.y.dist2(color));
			}
		});
		return colorChoices;
	}
	
	public String toString() {
		String res = "";
		for (Map.Entry<String, RgbColor> color: colorCapsules.entrySet()) {
			res += ">> (" + color.getKey() + ", " + color.getValue() + ")\n";
		}
		return res;
	}
}
