package graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class CreateMatrix {

	public static void main(String[] args) throws IOException {
		List<String> lines = FileUtils.readLines(new File("similaritites_simple.txt"));
		HashSet<String>languages = new HashSet<>();
		for (String string : lines) {
			String language1 = string.split(" - ")[0];
			String language2 = string.split(" - ")[1].split(" : ")[0];
			languages.add(language2);
			languages.add(language1);
		}
		
		double[][]similarities = new double[languages.size()][languages.size()];
		
		ArrayList<String> languagesArray = new ArrayList<>(languages);
		for (String string : lines) {
			String language1 = string.split(" - ")[0];
			String language2 = string.split(" - ")[1].split(" : ")[0];
			Double value = Double.valueOf(string.split(" - ")[1].split(" : ")[1]);
			if(value.isNaN())
				value = 0d;
			
			similarities[languagesArray.indexOf(language1)][languagesArray.indexOf(language2)] = value;
			similarities[languagesArray.indexOf(language2)][languagesArray.indexOf(language1)] = value;
		}
		
		String output = "0  1  2  3  4  0  0  5  6  0  0  0  7  0  8  0  9  0 10 11  0  0 12  0  0 13  0 14 15  0 16 17  0  0 0 18  0 19  0  0 20  0  0  0 21 22  0  0  0 23 24  0  0 25 26 27  0  0 28  0  0  0 29 30 31 32  0  0 33  0  0 34  0 35  0  0 36  0 37 38  0  0 39 40  0  0 41  0 42 43 44 45  0 46 47  0 48 49 50 51 52  0 53 54  0  0 55 56  0  0 57 58 59  0 60  0 61 62 63  0  0  0 64  0  0 65  0  0  0  0  0  0 66 67  0";
		String[] split = output.split("\\s+");

		for (int i = 0; i < split.length; i++) {
			System.out.println(split[i]+ ", " +languagesArray.get(i));
		}
		
		System.out.println(languagesArray);
		
		//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < languagesArray.size(); i++) {
//			sb.append(languagesArray.get(i).replace("_test.gml", ""));
//			if(i < languagesArray.size()-1)
//				sb.append("\t");
//		}
//		sb.append(System.lineSeparator());
//		for (int i = 0; i < similarities.length; i++) {
//			sb.append(languagesArray.get(i).replace("_test.gml", "")).append("\t");
//			for (int j = 0; j < similarities.length; j++) {
//				sb.append(similarities[i][j]);
//				if(j < similarities.length-1)
//					sb.append("\t");
//			}
//			sb.append(System.lineSeparator());
//		}
//		System.out.println(sb.toString());
//		FileUtils.writeStringToFile(new File("similarity_simple.csv"), sb.toString());
	}

}
