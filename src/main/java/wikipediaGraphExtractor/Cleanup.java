package wikipediaGraphExtractor;


import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class Cleanup {

	public static void main(String[] args) throws Exception {
		File[]createdFiles = new File("graphsReduced/content").listFiles();
		for (File category : createdFiles) {
			File[]languages = category.listFiles();
			for (File file : languages) {
				System.out.println(file);

				List<String> created  = FileUtils.readLines(file);
				List<String> original= FileUtils.readLines(new File(file.getPath().replace("content", "gml")));
				
				HashSet<String>defect = new HashSet<>();
				
				if(created.size() != original.size())
					throw new Exception("error");

				boolean inVertices = false;
				for (int i = 0; i < created.size(); i++) {
					if(created.get(i).trim().equals("Edges:"))
						inVertices = false;

					if(inVertices){
						if(!created.get(i).split("¤")[0].equals(original.get(i).split("¤")[0]))
							throw new Exception("error");

						Pattern pattern = Pattern.compile("\\[(.*?)\\]");
						Matcher matcher = pattern.matcher(created.get(i));
						boolean foundSV1 = false;
						boolean foundSV2 = false;
						while (matcher.find())
						{
							String[] match = matcher.group(1).split("¤");
							if(match[0].equals("SV1")){
								if( match.length==10)
									foundSV1 = true;
							}
							else if(match[0].equals("SV2")){
								if(101 == match.length)
									foundSV2 = true;
							}
						}
						if(!foundSV1 )
							defect.add(original.get(i).split("¤")[0]);
						if(!foundSV2){
							defect.add(original.get(i).split("¤")[0]);
//							throw new Exception("error");
						}
					}
					else{
						if(i != 2)
							if(!created.get(i).equals(original.get(i)))
								throw new Exception("error");
					}

					if(created.get(i).trim().equals("Vertices:"))
						inVertices = true;
				}
				
				cleanUp(created,defect,new File(file.getPath().replace("graphsReduced", "graphsReducedCleaned")));
				cleanUp(original,defect,new File(file.getPath().replace("content", "gml").replace("graphsReduced", "graphsReducedCleaned")));
			}
		}
	}
	
	public static void cleanUp(List<String> lines,HashSet<String> toDelete,File outputFile) throws IOException{
		List<String>cleaned = new ArrayList<>();
		for (String line : lines) {
			boolean skipLine = false;
			for (String delete : toDelete) {
				if(line.contains(delete)){
					skipLine = true;
					continue;
				}
			}
			if(!skipLine)
				cleaned.add(line);
		}
		FileUtils.writeLines(outputFile, cleaned);
	}

}
