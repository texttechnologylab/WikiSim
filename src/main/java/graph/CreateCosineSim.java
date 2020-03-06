package graph;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import similarityMeasure.CosineSimiliarity;
import similarityMeasure.EuclideanDist;

public class CreateCosineSim {

	public static void main(String[] args) throws IOException {
		List<String> lines = FileUtils.readLines(new File("biggest40_dist_ged.csv"));
		String[]names = lines.get(0).split("\t");
		double[][]similarities = new double[names.length][names.length];

		for (int i = 1; i < lines.size(); i++) {
			for (int j = i; j < lines.size(); j++) {
				System.out.println(lines.get(i).split("\t")[0] + " - " + lines.get(j).split("\t")[0] + " : ");
				double value = (EuclideanDist.distance(lines.get(i).replaceFirst(".*?\t", ""), lines.get(j).replaceFirst(".*?\t", "")));

				similarities[i-1][j-1] = value;
				similarities[j-1][i-1] = value;
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(lines.get(0));
		sb.append(System.lineSeparator());
		for (int i = 0; i < similarities.length; i++) {
			sb.append(names[i].replace("_test.gml", "")).append("\t");
			for (int j = 0; j < similarities.length; j++) {
				sb.append(similarities[i][j]);
				if(j < similarities.length-1)
					sb.append("\t");
			}
			sb.append(System.lineSeparator());
		}
		System.out.println(sb.toString());
		FileUtils.writeStringToFile(new File("biggest40_dist_ged.euclid.csv"), sb.toString());
	}

}
