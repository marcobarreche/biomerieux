package org.opencv.samples.biomerieux.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.opencv.samples.biomerieux.utils.CsvReader;

import junit.framework.TestCase;

public class CsvReaderTestCase extends TestCase {

	private ArrayList<String[]> csvReader;
	protected void setUp() throws Exception {
		super.setUp();
		
		String currentFolder = new File(".").getCanonicalPath() + '/';
		InputStream is = new FileInputStream(currentFolder + "res/raw/api20e.csv");
		try {
			csvReader = CsvReader.parse(is, ",");
		} catch(IOException e) {
			assertFalse(true);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testParse() {
		ArrayList<String[]> exp = new ArrayList<String[]>();
		exp.add(new String [] {"0104140", "Shigella flexneri"});
		exp.add(new String [] {"0104452", "Salmonella paratyphi A"});
		exp.add(new String [] {"0104504", "Pasteurella multocida"});
		exp.add(new String [] {"0104521", "Yersinia enterocolitica"});
		exp.add(new String [] {"0140004", "Pasteurella multocida"});
		exp.add(new String [] {"0144042", "Shigella boydii"});
		exp.add(new String [] {"0206042", "Acinetobacter calcoaceticus"});
		exp.add(new String [] {"0210004", "Achromobacter sp."});
		exp.add(new String [] {"0210004", "Alcaligenes faecalis"});
		exp.add(new String [] {"0210004", "Alcaligenes sp."});
		exp.add(new String [] {"0314000", "Proteus mirabilis"});
		exp.add(new String [] {"0504512", "Salmonella paratyphi A"});
		exp.add(new String [] {"0676001", "Proteus vulgaris"});
		exp.add(new String [] {"0776000", "Proteus mirabilis"});
		exp.add(new String [] {"1000004", "Pasteurella sp."});
		exp.add(new String [] {"1014743", "Klebsiella ozaenae"});
		exp.add(new String [] {"1104112", "Shigella sonnei"});
		exp.add(new String [] {"1214012", "Yersinia pseudotuberculosis"});
		exp.add(new String [] {"1214773", "Klebsiella pneumoniae"});
		exp.add(new String [] {"2206004", "Pseudomonas aeruginosa"});
		exp.add(new String [] {"2504752", "Salmonella spp."});
		exp.add(new String [] {"3005573", "Enterobacter cloacae"});
		exp.add(new String [] {"3246527", "Aeromonas hydrophilia"});
		exp.add(new String [] {"4004550", "Salmonella cholerasuis"});
		exp.add(new String [] {"4004550", "Salmonella typhi"});
		exp.add(new String [] {"4104100", "Salmonella gallinarun"});
		exp.add(new String [] {"4357106", "Vibrio parahemolyticus"});
		exp.add(new String [] {"4404112", "Salmonella gallinarum"});
		exp.add(new String [] {"4404510", "Salmonella cholerasuis"});
		exp.add(new String [] {"4404540", "Salmonella tiphy"});
		exp.add(new String [] {"4504010", "Salmonella pullorum"});
		exp.add(new String [] {"4604552", "Salmonella spp."});
		exp.add(new String [] {"4704500", "Salmonella cholerasuis"});
		exp.add(new String [] {"5004024", "Pseudomonas cepacea"});
		exp.add(new String [] {"5044124", "Vibrio cholerae"});
		exp.add(new String [] {"5046753", "Serratia odorifera"});
		exp.add(new String [] {"5100100", "Yersinia ruckeri"});
		exp.add(new String [] {"5104111", "Hafnia alvei"});
		exp.add(new String [] {"5104542", "Salmonella arizonae"});
		exp.add(new String [] {"5144572", "Escherichia coli"});
		exp.add(new String [] {"5207323", "Serratia rubidae"});
		exp.add(new String [] {"5255573", "Klebsiella oxytoca"});
		exp.add(new String [] {"5346761", "Serratia marcescens"});
		exp.add(new String [] {"5314173", "Enterobacter gergoviae"});
		exp.add(new String [] {"5317761", "Serratia marcescens"});
		exp.add(new String [] {"5704412", "Salmonella arizonae"});
		exp.add(new String [] {"5704552", "Salmonella arizonae"});
		exp.add(new String [] {"6004540", "Salmonella typhi"});
		exp.add(new String [] {"6144204", "Plesiomonas shigelloides"});
		exp.add(new String [] {"6404112", "Salmonella spp."});
		exp.add(new String [] {"6504750", "Salmonella spp."});
		exp.add(new String [] {"6604512", "Salmonella spp."});
		exp.add(new String [] {"6704342", "Salmonella spp."});
		exp.add(new String [] {"6704532", "Salmonella spp"});
		exp.add(new String [] {"6704752", "Salmonella gallinarum"});
		exp.add(new String [] {"7244126", "Aeromonas hydrophila"});
		exp.add(new String [] {"7305773", "Enterobacter cloacae"});
		exp.add(new String [] {"7704752", "Salmonella arizonae"});
		exp.add(new String [] {"7704752", "Salmonella spp."});

		for (int i=0; i < csvReader.size(); i++) {
			assertTrue(csvReader.get(i)[0].equals(exp.get(i)[0]));
			assertTrue(csvReader.get(i)[1].equals(exp.get(i)[1]));
		}
	}

}
