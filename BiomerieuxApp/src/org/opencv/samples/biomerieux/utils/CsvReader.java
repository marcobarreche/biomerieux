package org.opencv.samples.biomerieux.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class CsvReader {
	public static ArrayList<String[]> parse(InputStream is) throws IOException {
		return parse(is, ",");
	}
	
	public static Document getXmlObject (InputStream is) throws IOException, ParserConfigurationException, SAXException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		return dBuilder.parse(is);
	}

	public static ArrayList<String[]> parse(InputStream is, String splitBy) throws IOException {
		ArrayList<String[]> lines = new ArrayList<String[]>();
		BufferedReader br = null;
		String line = "";

		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				String[] columns = line.split(splitBy);
				lines.add(columns);
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}

		return lines;
	}

}
