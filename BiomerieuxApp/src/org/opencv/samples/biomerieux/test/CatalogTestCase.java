package org.opencv.samples.biomerieux.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.MissingFormatWidthException;

import org.opencv.samples.biomerieux.catalog.Catalog;
import org.opencv.samples.biomerieux.catalog.CatalogEntryWithProb;
import org.opencv.samples.biomerieux.utils.Tuple;

import junit.framework.TestCase;

public class CatalogTestCase extends TestCase {
	private Catalog catalog;
	private Double errorMargin;
	protected void setUp() throws Exception {
		super.setUp();
		
		String currentFolder = new File(".").getCanonicalPath() + '/';
		InputStream is = new FileInputStream(currentFolder + "res/raw/api20e.csv");
		catalog = new Catalog(is, 5);
		errorMargin = Math.pow(10, -17);
	}
	
	public void testBinToCodeError() {
		try {
			Catalog.binToCode("11", 0);
			assertFalse(true);
		} catch(MissingFormatWidthException e) {
			assertTrue(true);
		}
		try {
			Catalog.binToCode("44", 0);
			assertFalse(true);
		} catch(NumberFormatException e) {
			assertTrue(true);
		}
		try {
			Catalog.binToCode("hello", 0);
			assertFalse(true);
		} catch(NumberFormatException e) {
			assertTrue(true);
		}
	}
	public void testBinToCode() {
		assertTrue(Catalog.binToCode("11", 1).equals("3"));
		assertTrue(Catalog.binToCode("101", 1).equals("5"));
		assertTrue(Catalog.binToCode("1111", 1).equals("71"));
		assertTrue(Catalog.binToCode("001111", 1).equals("47"));
		assertTrue(Catalog.binToCode("100111", 1).equals("17"));
		
		String binCode = "101001110";
		assertTrue(Catalog.binToCode(binCode, 4).equals("5430"));
		assertTrue(Catalog.binToCode(binCode, 5).equals("54300"));
		assertTrue(Catalog.binToCode(binCode, 6).equals("543000"));
		for (int i = 1; i < 4; i++)
			assertTrue(Catalog.binToCode(binCode, i).equals("543"));
	}
	
	public void testCodeToBinError() {
		try {
			System.out.println(Catalog.codeToBin("97", 3));
			assertFalse(true);
		} catch (NumberFormatException e) {
			assertTrue(true);
		}
		try {
			System.out.println(Catalog.codeToBin("hellpo", 3));
			assertFalse(true);
		} catch (NumberFormatException e) {
			assertTrue(true);
		}
	}

	public void testCodeToBin() {
		assertTrue(Catalog.codeToBin("1", 1).equals("1"));
		assertTrue(Catalog.codeToBin("11", 1).equals("1001"));
		assertTrue(Catalog.codeToBin("101", 1).equals("1000001"));
		assertTrue(Catalog.codeToBin("5", 1).equals("101"));
		assertTrue(Catalog.codeToBin("41", 1).equals("0011"));
		assertTrue(Catalog.codeToBin("25", 1).equals("010101"));
		
		String binCode = "33";
		assertTrue(Catalog.codeToBin(binCode, 6).equals("110110"));
		assertTrue(Catalog.codeToBin(binCode, 7).equals("1101100"));
		assertTrue(Catalog.codeToBin(binCode, 8).equals("11011000"));
		assertTrue(Catalog.codeToBin(binCode, 9).equals("110110000"));
		for (int i = 1; i < 6; i++)
			assertTrue(Catalog.codeToBin(binCode, i).equals("11011"));
	}
	
	public void testBestCandidates() {
		String [] bin = new String [] {"01001", "11011","11000", "00100", "11001"};
		ArrayList<Tuple<Double, String>> got = catalog.bestCandidates(
			new double [] {0.2, 0.6, 0.1, 0.3, 0.7},
			new HashSet<String>(Arrays.asList(bin))
		);
		
		ArrayList<Tuple<Double, String>> expected = new ArrayList<Tuple<Double, String>>();
		expected.add(new Tuple<Double, String>(0.8 * 0.6 * 0.9 * 0.7 * 0.7, "01001"));
		expected.add(new Tuple<Double, String>(0.2 * 0.6 * 0.9 * 0.7 * 0.7, "11001"));
		expected.add(new Tuple<Double, String>(0.2 * 0.6 * 0.9 * 0.7 * 0.3, "11000"));
		expected.add(new Tuple<Double, String>(0.2 * 0.6 * 0.9 * 0.3 * 0.7, "11011"));
		expected.add(new Tuple<Double, String>(0.8 * 0.4 * 0.1 * 0.7 * 0.3, "00100"));
		
		for (int i = 0; i<expected.size(); i++) {
			assertTrue(Math.abs(got.get(i).x - expected.get(i).x) < errorMargin);
			assertTrue(got.get(i).y.equals(expected.get(i).y));
		}
	}
	
	public void testSearchp() {
		CatalogEntryWithProb cat;
		cat = catalog.searchp(new double [] {0.2, 0.6, 0.1, 0.3, 0.7});
		assertTrue(cat.name.equals("Pseudomonas aeruginosa"));
		assertTrue(cat.code.equals("2206004"));
		assertTrue(cat.bin.equals("010010000011000000001"));
		assertTrue(cat.prob == 0.8 * 0.6 * 0.9 * 0.7 * 0.7);
		
		cat = catalog.searchp(new double [] {});
		assertTrue(cat.name.equals("Serratia marcescens"));
		assertTrue(cat.code.equals("5317761"));
		assertTrue(cat.bin.equals("1011101001111110111"));
		assertTrue(cat.prob == 1.0);
	}
	
	public void testSearchb() {
		CatalogEntryWithProb cat = catalog.searchb("01001");
		CatalogEntryWithProb expect = catalog.searchp(new double [] {0.1, 0.9, 0.1, 0.1, 0.9});
		
		assertTrue(cat.name.equals(expect.name));
		assertTrue(cat.code.equals(expect.code));
		assertTrue(cat.bin.equals(expect.bin));
		assertTrue(cat.prob == expect.prob);

		assertTrue(cat.name.equals("Pseudomonas aeruginosa"));
		assertTrue(cat.code.equals("2206004"));
		assertTrue(cat.bin.equals("010010000011000000001"));
		assertTrue(cat.prob == 0.5904900000000002);		
	}

	public void testSearchc() {
		CatalogEntryWithProb cat = catalog.searchc("22");
		CatalogEntryWithProb expect = catalog.searchb("01001");

		assertTrue(cat.name.equals(expect.name));
		assertTrue(cat.code.equals(expect.code));
		assertTrue(cat.bin.equals(expect.bin));
		assertTrue(cat.prob == expect.prob);

		assertTrue(cat.name.equals("Pseudomonas aeruginosa"));
		assertTrue(cat.code.equals("2206004"));
		assertTrue(cat.bin.equals("010010000011000000001"));
		assertTrue(cat.prob == 0.5904900000000002);
	}
	
	public void testSearchError() {
		try {
			System.out.println(catalog.searchb("1234567890123"));
			assertFalse(true);
		} catch (StringIndexOutOfBoundsException e) {
			assertTrue(true);
		}

		String [] code = new String [] {"8" , "9", "12345670123", "a"};
		for (int i = 0; i < code.length; i++) {
			try {
				System.out.println(catalog.searchc(code[i]));
				assertFalse(true);
			} catch (NumberFormatException e) {
				assertTrue(true);
			}
		}
	}
}
