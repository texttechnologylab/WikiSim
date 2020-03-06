package wikipediaGraphExtractor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import graph.BF2JGraphT;

public class GraphBuilderMitDDC {

	public static void main(String[] args) throws IOException {
		HashSet<String>consideredLanguages = new HashSet<>();
		consideredLanguages.add("ceb");
		consideredLanguages.add("war");
		consideredLanguages.add("sv");
		consideredLanguages.add("vi");
		consideredLanguages.add("nl");
		consideredLanguages.add("fr");
		consideredLanguages.add("pl");
		consideredLanguages.add("en");
		consideredLanguages.add("it");
		consideredLanguages.add("de");
		consideredLanguages.add("ru");
		consideredLanguages.add("es");
		consideredLanguages.add("ja");
		
		for (File category: new File("statistics").listFiles()) {
			HashMap<String, HashMap<String,String[]>>spracheVector = new HashMap<>();
			List<String> lines = FileUtils.readLines(category);
			for (String string : lines) {
				String[] split = string.split("\t");
				String language = split[6];
				String wikidataId = split[5];
				if(!spracheVector.containsKey(language))
					spracheVector.put(language, new HashMap<>());
				spracheVector.get(language).put(wikidataId, split);
			}

			for (String languageG : consideredLanguages) {
				if(!(spracheVector.containsKey(languageG)))
					continue;

				Graph<String,DefaultEdge> de = BF2JGraphT.readBFGraph(new File("graphs/gml/"+category.getName()+"/" + languageG + ".gml.bf"));

				HashSet<String>difference = new HashSet<>(spracheVector.get(languageG).keySet());
				difference.removeAll(de.vertexSet());

				de.removeAllVertices(difference);

				StringBuilder sb = new StringBuilder();
				sb.append("directed").append(System.lineSeparator());
				sb.append("SimilarityGraph").append(System.lineSeparator());
				sb.append("Vertex Attributes:");
				sb.append(formatProp("PageSize", "String",false)).append(";");
				sb.append(formatProp("CategorySize", "Integer",false)).append(";");
				sb.append(formatProp("SectionsSize", "Integer",false)).append(";");
				sb.append(formatProp("ExternalLinksCount", "Integer",false)).append(";");
				sb.append(formatProp("InternalLinksCount", "Integer",false)).append(";");
				for (int i = 0; i < 1000; i=i+10) {
					sb.append(formatProp("DDC"+i, "Double",false)).append(";");

				}
				sb.append(System.lineSeparator());
				sb.append("Edge Attributes:").append(System.lineSeparator());
				sb.append("Vertices:").append(System.lineSeparator());

				HashSet<String>verticies = new HashSet<>();
				for (Entry<String, String[]> string : spracheVector.get(languageG).entrySet()) {
					if(de.vertexSet().contains(string.getKey()) && string.getValue().length > 10){
						sb.append(string.getKey()+"¤"+getAttributes(string.getValue())).append(System.lineSeparator());
						verticies.add(string.getKey());
					}
				}
				sb.append("Edges:").append(System.lineSeparator());
				for (String sourceVertex : verticies) {
					for (DefaultEdge outgoing: de.outgoingEdgesOf(sourceVertex)) {
						if(verticies.contains(de.getEdgeTarget(outgoing))){
							sb.append(sourceVertex).append("¤").append(de.getEdgeTarget(outgoing)).append("¤").append("1.0").append("¤").append(System.lineSeparator());
						}
					}
				}

				FileUtils.writeStringToFile(new File("/resources/public/hemati/WikipediaGraphs/contentGraphs/"+category.getName()+"/" + languageG + ".gml.bf"), sb.toString());
			}
		}
	}


	public static String getAttributes(String[]split){
		StringBuilder sb = new StringBuilder();
		sb.append(formatProp("PageSize", split[0],true)).append("¤");
		sb.append(formatProp("CategorySize", split[1],true)).append("¤");
		sb.append(formatProp("SectionsSize", split[2],true)).append("¤");
		sb.append(formatProp("ExternalLinksCount", split[3],true)).append("¤");
		sb.append(formatProp("InternalLinksCount", split[4],true)).append("¤");

		for (int i = 9; i < split.length; i++) {
			sb.append(formatProp("DDC"+((i-9)*10), split[i],true)).append("¤");
		}

		return sb.toString();
	}

	public static String formatProp(String name, String value,boolean addSuffixZeichen){
		StringBuilder sb = new StringBuilder().append("[").append(name).append("¤").append(value);
		if(addSuffixZeichen)
			sb.append("¤");
		sb.append("]");
		return sb.toString();

	}

}
