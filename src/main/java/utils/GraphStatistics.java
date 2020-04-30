package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.persistence.criteria.CompoundSelection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import graph.BF2JGraphT;

public class GraphStatistics {

	public static void getGraphVerteilung() throws IOException{
		HashSet<String>consideredLanguages = new HashSet<>();
		consideredLanguages.add("arz");
		consideredLanguages.add("bs");
		consideredLanguages.add("ceb");
		consideredLanguages.add("ckb");
		consideredLanguages.add("da");
		consideredLanguages.add("de");
		consideredLanguages.add("el");
		consideredLanguages.add("en");
		consideredLanguages.add("es");
		consideredLanguages.add("fr");
		consideredLanguages.add("he");
		consideredLanguages.add("hi");
		consideredLanguages.add("hu");
		consideredLanguages.add("id");
		consideredLanguages.add("it");
		consideredLanguages.add("ja");
		consideredLanguages.add("ko");
		consideredLanguages.add("lv");
		consideredLanguages.add("mk");
		consideredLanguages.add("ml");
		consideredLanguages.add("mr");
		consideredLanguages.add("nl");
		consideredLanguages.add("pl");
		consideredLanguages.add("pt");
		consideredLanguages.add("ro");
		consideredLanguages.add("ru");
		consideredLanguages.add("sh");
		consideredLanguages.add("si");
		consideredLanguages.add("simple");
		consideredLanguages.add("sr");
		consideredLanguages.add("sv");
		consideredLanguages.add("te");
		//		consideredLanguages.add("tr");
		consideredLanguages.add("vi");
		consideredLanguages.add("war");
		consideredLanguages.add("zh");

		String modus = "Nodes";

		//String graphsFolder = "/home/staff_homes/ahemati/projects/WikiSim/graphs/oecd/gml";

		String graphsFolder = "/home/staff_homes/ahemati/projects/WikiSim/graphsReducedCleaned/gml";
		String outputPath = "/home/staff_homes/ahemati/projects/WikiSim/texOutputs/topicsStatistics/topicsStatistics_min10.tex";

		File[]topics = new File(graphsFolder).listFiles();
		ArrayList<String>topicsString = new ArrayList<>();
		HashSet<String>alllanguages = new HashSet<>();
		for (File topic : topics) {
			topicsString.add(topic.getName());
			File[]languages = topic.listFiles();
			for (File language : languages) {
				if(language.getName().endsWith(".bf") && consideredLanguages.contains(language.getName().replace(".gml.bf", ""))){
					alllanguages.add(language.getName().replace(".gml.bf", ""));
				}
			}
		}

		List<String> languages = new ArrayList<>(alllanguages);
		Collections.sort(languages);
		Collections.sort(topicsString);

		HashMap<String, ArrayList<Integer>> knotenCountsPerTopic = new HashMap<>();

		for (String topic : topicsString) {
			ArrayList<Integer>knotenAnzahl = new ArrayList<>();
			for (String language : languages) {
				File graphFile = Paths.get(graphsFolder,topic,language+".gml.bf").toFile();
				if(graphFile.exists()){
					if(modus.equals("Edges"))
						knotenAnzahl.add(BF2JGraphT.readBFGraph(graphFile).edgeSet().size());
					else
						knotenAnzahl.add(BF2JGraphT.readBFGraph(graphFile).vertexSet().size());
				}
				else
					knotenAnzahl.add(0);
			}
			knotenCountsPerTopic.put(topic, knotenAnzahl);
		}
//
//		for (File file : new File("graphs/gml").listFiles()) {
//			ArrayList<Integer>knotenAnzahl = new ArrayList<>();
//			for (String language : languages) {
//				File graphFile = Paths.get(file.getAbsolutePath(),language+".gml.bf").toFile();
//				if(graphFile.exists()){
//					if(modus.equals("Edges"))
//						knotenAnzahl.add(BF2JGraphT.readBFGraph(graphFile).edgeSet().size());
//					else
//						knotenAnzahl.add(BF2JGraphT.readBFGraph(graphFile).vertexSet().size());
//				}
//				//				else
//				//					knotenAnzahl.add(0);
//			}
//			knotenCountsPerTopic.put(file.getName(), knotenAnzahl);
//		}

		knotenCountsPerTopic.entrySet().stream().forEach(System.out::println);
		ArrayList<Entry<String, ArrayList<Integer>>> entries = new ArrayList<>(knotenCountsPerTopic.entrySet());
		Collections.sort(entries, new Comparator<Entry<String, ArrayList<Integer>>>() {

			@Override
			public int compare(Entry<String, ArrayList<Integer>> o1, Entry<String, ArrayList<Integer>> o2) {


				Median median = new Median();

				double[]o1Array = new double[o1.getValue().size()];
				for (int i = 0; i < o1.getValue().size(); i++) {
					o1Array[i] = o1.getValue().get(i);
				}

				double[]o2Array = new double[o2.getValue().size()];
				for (int i = 0; i < o2.getValue().size(); i++) {
					o2Array[i] = o2.getValue().get(i);
				}

				double medianO1 = median.evaluate(o1Array);
				double medianO2 = median.evaluate(o2Array);

				return (int)(medianO2-medianO1);
			}

		});

		StringBuilder boxplots = new StringBuilder();
		StringBuilder labels = new StringBuilder();
		StringBuilder tick = new StringBuilder();

		int tickCount = 1;
		for (Entry<String, ArrayList<Integer>> entry : entries) {
			String name = entry.getKey().replace(" ", "-");
			FileUtils.writeLines(new File("texOutputs/boxplots/data"+modus+"/"+name+".txt"), entry.getValue());
			boxplots.append("\\addplot+[").append(name.contains("GML")?"SeminarRot":"SeminarBlau").append(", thick, boxplot]  table[y index=0]{data"+modus+"/"+name+".txt};\n");
			if(labels.length() > 0)
				labels.append(",");
			labels.append(name).append("(").append(entry.getValue().stream().filter(x-> x>0).collect(Collectors.toList()).size()).append(")");

			if(tick.length() > 0)
				tick.append(",");
			tick.append(tickCount++);


		}
		System.out.println(boxplots.toString());
		System.out.println(labels.toString());
		System.out.println(tick);

		FileUtils.writeStringToFile(new File("texOutputs/boxplots/boxplots"+modus+".tex"), 
				FileUtils.readFileToString(new File("texOutputs/boxplots/boxplot.tex"))
				.replace("xxboxplotsxx", boxplots.toString())
				.replace("xxticksxx", tick.toString())
				.replace("xxlabelsxx", labels.toString())
				.replace("xxxmaxxx", ""+(entries.size()+1)));
	}

	public static void getStatistics() throws IOException{
		HashSet<String>consideredLanguages = new HashSet<>();
		consideredLanguages.add("arz");
		consideredLanguages.add("bs");
		consideredLanguages.add("ceb");
		consideredLanguages.add("ckb");
		consideredLanguages.add("da");
		consideredLanguages.add("de");
		consideredLanguages.add("el");
		consideredLanguages.add("en");
		consideredLanguages.add("es");
		consideredLanguages.add("fr");
		consideredLanguages.add("he");
		consideredLanguages.add("hi");
		consideredLanguages.add("hu");
		consideredLanguages.add("id");
		consideredLanguages.add("it");
		consideredLanguages.add("ja");
		consideredLanguages.add("ko");
		consideredLanguages.add("lv");
		consideredLanguages.add("mk");
		consideredLanguages.add("ml");
		consideredLanguages.add("mr");
		consideredLanguages.add("nl");
		consideredLanguages.add("pl");
		consideredLanguages.add("pt");
		consideredLanguages.add("ro");
		consideredLanguages.add("ru");
		consideredLanguages.add("sh");
		consideredLanguages.add("si");
		consideredLanguages.add("simple");
		consideredLanguages.add("sr");
		consideredLanguages.add("sv");
		consideredLanguages.add("te");
		//		consideredLanguages.add("tr");
		consideredLanguages.add("vi");
		consideredLanguages.add("war");
		consideredLanguages.add("zh");

		//String graphsFolder = "/home/staff_homes/ahemati/projects/WikiSim/graphs/oecd/gml";

		String graphsFolder = "/home/staff_homes/ahemati/projects/WikiSim/graphsCleaned/oecdTopics/gml";
		String outputPath = "/home/staff_homes/ahemati/projects/WikiSim/texOutputs/topicsStatistics/topicsStatistics_min10.tex";

		File[]topics = new File(graphsFolder).listFiles();
		ArrayList<String>topicsString = new ArrayList<>();
		HashSet<String>alllanguages = new HashSet<>();
		for (File topic : topics) {
			topicsString.add(topic.getName());
			File[]languages = topic.listFiles();
			for (File language : languages) {
				if(language.getName().endsWith(".bf") && consideredLanguages.contains(language.getName().replace(".gml.bf", ""))){
					System.out.println(topic.getName() + "\t" + language.getName());
					alllanguages.add(language.getName().replace(".gml.bf", ""));
				}
			}
		}



		List<String> languages = new ArrayList<>(alllanguages);
		Collections.sort(languages);
		Collections.sort(topicsString);


		String columns = "X|"+String.join("|", alllanguages.stream().map(x -> "l").collect(Collectors.toList()))+"|l";
		String languagesTex = "\\textbf{Field} & " +String.join(" & ",languages.stream().map(x->"\\textbf{"+x+"}").collect(Collectors.toList())) + "\\\\";

		System.out.println(columns);
		System.out.println(languagesTex);
		System.out.println(alllanguages.size());
		System.out.println(consideredLanguages.size());

		StringBuilder sb = new StringBuilder();

		HashMap<String, Integer> rangAbfolge = new HashMap<>();
		for (String topic : topicsString) {
			ArrayList<Integer>knotenAnzahl = new ArrayList<>();
			for (String language : languages) {
				File graphFile = Paths.get(graphsFolder,topic,language+".gml.bf").toFile();
				if(graphFile.exists())
					knotenAnzahl.add(BF2JGraphT.readBFGraph(graphFile).vertexSet().size());
				else
					knotenAnzahl.add(0);
			}
			if(knotenAnzahl.stream().max(Integer::compare).get() >= 10){
				String topicTex = topic +" & " + String.join(" & ", knotenAnzahl.stream().map(x-> "\\numprint{"+x+"}").collect(Collectors.toList())) +"\\\\";
				sb.append(topicTex).append("\n");
			}
			rangAbfolge.put(topic, knotenAnzahl.stream().filter(x-> x>=10).collect(Collectors.toList()).size());
		}

		String tex = FileUtils.readFileToString(new File("/home/staff_homes/ahemati/projects/WikiSim/texOutputs/topicsStatistics/topicsStatisticsPlain.tex"));
		System.out.println(tex.replace("xtablespansx", columns).replace("xlanguagesx", languagesTex).replace("xstatisticsx", sb.toString()));

		FileUtils.writeStringToFile(new File(outputPath), tex.replace("xtablespansx", columns).replace("xlanguagesx", languagesTex).replace("xstatisticsx", sb.toString()));

		//		for (Entry<String, Integer> string : sortByValue(rangAbfolge).entrySet()) {
		//			System.out.println(string);
		//		} ;
	}

	public static void main(String[] args) throws IOException {
		getGraphVerteilung();
	}


	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Entry.comparingByValue());

		Map<K, V> result = new LinkedHashMap<>();
		for (Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}
}
