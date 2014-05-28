package org.opencv.samples.biomerieux.capsules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;
import org.opencv.samples.biomerieux.utils.Sci;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ColorPos {
	private Map<Integer, Map<String, List<RgbColor>>> posEvalColors;
	
	public ColorPos(File isColor) 
			throws IOException, ParserConfigurationException, SAXException {
		this.posEvalColors = parse_pos_eva_col_xml(isColor);
	}
	
	public ColorPos(String fnColor) throws IOException, ParserConfigurationException, SAXException {
		this.posEvalColors = parse_pos_eva_col_xml(new File(fnColor));
	}
	
	public static Document getXmlObject (File f) throws IOException, ParserConfigurationException, SAXException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		return dBuilder.parse(f);
	}
	
	private static Map<Integer, Map<String, List<RgbColor>>> parse_pos_eva_col_xml(File is) 
			throws IOException, ParserConfigurationException, SAXException {
		
		Map<Integer, Map<String, List<RgbColor>>> pos_eval_colors = new HashMap<Integer, Map<String, List<RgbColor>>>();
		int position;
		String nature;
		Map<String, List<RgbColor>> eval_colors;
		List<RgbColor> colors;
		
		NodeList nL_caps = ColorPos.getXmlObject(is).getElementsByTagName("capsule");
				
		for (int i_cap = 0; i_cap < nL_caps.getLength(); i_cap++) {
			Node nN_cap = nL_caps.item(i_cap);
			Element eE_cap = (Element) nN_cap;

			position = Integer.parseInt(eE_cap.getAttribute("id"));

			NodeList nL_nature = nN_cap.getChildNodes();
			eval_colors = new HashMap<String, List<RgbColor>>();
			for (int i_eval = 0; i_eval < nL_nature.getLength(); i_eval ++) {
				Node nN_nature = nL_nature.item(i_eval);
				if (! nN_nature.getNodeName().equals("positive") && !nN_nature.getNodeName().equals("negative")) continue;
				
				nature = nN_nature.getNodeName().equals("positive")? "P" : "N";
				NodeList nL_colors = nN_nature.getChildNodes();
				colors = new ArrayList<RgbColor>();
				for (int i_color = 0; i_color < nL_colors.getLength(); i_color++){
					Node nN_color = nL_colors.item(i_color);
					if (! nN_color.getNodeName().equals("color")) continue;
					
					Element eE_color = (Element) nN_color;
					int r = (int) Double.parseDouble(eE_color.getAttribute("r"));
					int g = (int) Double.parseDouble(eE_color.getAttribute("g"));
					int b = (int) Double.parseDouble(eE_color.getAttribute("b"));
					colors.add(new RgbColor(r, g, b));
				}
				eval_colors.put(nature, colors);
			}
			pos_eval_colors.put(position, eval_colors);
		}
		return pos_eval_colors;
	}
	
	public String toString (){
		String output = "";
		Map<String, List<RgbColor>> eval_colors;
		for (int i_pos = 1; i_pos <= posEvalColors.size(); i_pos++){
			output += "Position " + i_pos + "\n";
			eval_colors = posEvalColors.get(i_pos);
			for (String str_nature : new ArrayList<String>(Arrays.asList("P","N"))){
				output += "\t" + (str_nature.equals("P")? "Positive": "Negative") + " colors\n";
				for (int i_color = 0; i_color < eval_colors.get(str_nature).size(); i_color++){
					output += "\t\t" + eval_colors.get(str_nature).get(i_color) + "\n";
				}
			}
		}
		return output;
	}
		
	public List<Double> class_color(RgbColor color) {
		List<Double> res = new ArrayList<Double>();
		for (int i = 1; i < posEvalColors.size() + 1; i ++) {
			List<RgbColor> setColors = new ArrayList<RgbColor>(posEvalColors.get(i).get("P"));
			setColors.addAll(posEvalColors.get(i).get("N"));
			double min = Long.MAX_VALUE;
			for (int j=0; j < setColors.size(); j++) {
				double norm = setColors.get(j).dist2(color);
				if (norm < min) {
					min = norm;
				}
			}
			res.add(min);
		}
		return res;
	}
	
	public List<Integer> pos_color (List<RgbColor> lsColor, List<Integer> lsPos) throws BiomerieuxException {
		List<Integer> res = new ArrayList<Integer>();
		int len = Math.min(lsColor.size(), lsPos.size());
		for (int i= 0; i < len; i++) {
			RgbColor color = lsColor.get(i);
			int position = lsPos.get(i);

			List<Double> lsDist = class_color(color);
			double min = Sci.min(lsDist);
			double max = Sci.max(lsDist);
			int element = (lsDist.get(position - 1) < min + max * 0.2) ? 1: 0;
			res.add(element);
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	public List<Boolean> get_positives (List<RgbColor> lsColor, List<Integer> lsPos) throws BiomerieuxException {
		if (lsPos == null) {
			lsPos = Sci.range(1, 21, Integer.class);
		}
		List<Boolean> res = new ArrayList<Boolean>();
		int min = Math.min(lsColor.size(), lsPos.size());
		for (int i= 0; i < min; i++) {
			RgbColor color = lsColor.get(i);
			int position = lsPos.get(i);
			List<RgbColor> positive = posEvalColors.get(position).get("P");
			List<RgbColor> negative = posEvalColors.get(position).get("N");
			if (color.distSet(negative) < color.distSet(positive)) {
				res.add(false);
			} else {
				res.add(true);
			}
		}
		return res;
	}

	public int eval_color_vs_pos(List<RgbColor> lsColor, List<Integer> lsPos) throws BiomerieuxException {
		List<Integer> color = pos_color(lsColor, lsPos);
		int res = 0;
		for (int i= 0; i < lsPos.size(); i++) {
			res += color.get(i);
		}
		return res;
	}
}
