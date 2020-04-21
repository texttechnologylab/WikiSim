package siteStatistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import graph.BF2JGraphT;
import wiki.WikipediaFetcher;


public class SiteStatistics {
	static HashMap<String, HashMap<String,double[]>>ddcs = new HashMap<>();
	static ArrayList<String>ddcLanguageOrder = new ArrayList<>();

	public static void main(String[] args) throws IOException {
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

		long start = System.currentTimeMillis();
		for (String language : consideredLanguages) {
			for (String path : new String[]{"graphs/oecdTopics","graphs/oecd"}) {
				for (File category: Paths.get(path,"gml").toFile().listFiles()) {
					for (File graphFile : category.listFiles()) {
						if(Paths.get(path, "content",category.getName(),graphFile.getName()).toFile().exists())
							continue;
						//			File graphFile = new File("graphs/gml/warGML/en.gml.bf");
						if(graphFile.getName().endsWith(".gml.bf")){
							if(!graphFile.getName().replace(".gml.bf", "").equals(language))
								continue;
							System.out.println(graphFile.getPath());

							Graph<String,DefaultEdge> graph = BF2JGraphT.readBFGraph(graphFile);
							HashMap<String, String>wikipediaTitles = WikipediaFetcher.getWikipediaFromWikidataList(new ArrayList<String>(graph.vertexSet()), language);
							List<String> lines = FileUtils.readLines(graphFile);					

							List<String> output = new ArrayList<>();
							boolean inVertices = false;
							for (String string : lines) {
								if(string.trim().equals("Vertex Attributes:")){
									output.add("Vertex Attributes:[SV1¤IntegerDistribution];[SV2¤IntegerDistribution];");
									continue;
								}
								if(string.trim().equals("Edges:"))
									inVertices = false;
								if(inVertices){
									String vertex = string.split("¤")[0];
									if(wikipediaTitles.containsKey(vertex)){
										PageStatistics currentPage = new PageStatistics(language,wikipediaTitles.get(vertex));
										if(currentPage.getPageId() > 0){
											StringBuilder sb = new StringBuilder();
											sb.append("[SV1¤");
											sb.append("1").append("¶").append(currentPage.getSize()).append("¤");
											sb.append("2").append("¶").append(currentPage.getCategories().size()).append("¤");
											sb.append("3").append("¶").append(currentPage.getImages().size()).append("¤");
											sb.append("4").append("¶").append(currentPage.getNumberOfTables()).append("¤");
											sb.append("5").append("¶").append(currentPage.getExtlinks().size()).append("¤");
											sb.append("6").append("¶").append(currentPage.getLinks().size()).append("¤");
											sb.append("7").append("¶").append(currentPage.getSections().size()).append("¤");
											sb.append("8").append("¶").append(currentPage.getBreadthOfTOC()).append("¤");
											sb.append("9").append("¶").append(currentPage.getDepthOfTOC()).append("¤");
											sb.append("]¤");

											//							sb.append(formatProp("number of characters", ""+ currentPage.getSize(),true)).append("¤");
											//							sb.append(formatProp("number of categories", ""+ currentPage.getCategories().size(),true)).append("¤");
											//							sb.append(formatProp("number of pictures", ""+ currentPage.getImages().size(),true)).append("¤");
											//							sb.append(formatProp("number of tables", ""+ currentPage.getNumberOfTables(),true)).append("¤");
											//							sb.append(formatProp("links to pages outside Wikipedia", ""+currentPage.getExtlinks().size(),true)).append("¤");
											//							sb.append(formatProp("links to pages inside Wikipedia", ""+currentPage.getLinks().size(),true)).append("¤");
											//							sb.append(formatProp("number of sections", ""+ currentPage.getCategories().size(),true)).append("¤");
											//							sb.append(formatProp("breadth of table of content", ""+currentPage.getBreadthOfTOC(),true)).append("¤");
											//							sb.append(formatProp("depth of table of content", ""+currentPage.getDepthOfTOC(),true)).append("¤");

											sb.append(formatProp("SV2",formatDDC(getDDC(language, ""+currentPage.getPageId())),false)).append("¤");
											System.out.println(sb.toString());
											string+=sb.toString();
											FileUtils.writeStringToFile(Paths.get(path,"html",category.getName(),language,vertex+"_"+wikipediaTitles.get(vertex)+".xml").toFile(),currentPage.apiOutput,"UTF-8");
										}

									}
								}
								if(string.trim().equals("Vertices:"))
									inVertices = true;

								output.add(string);
							}
							FileUtils.writeLines(Paths.get(path,"content",category.getName(),graphFile.getName()).toFile(), output);
							//										FileUtils.writeLines(new File("test.bf"), output);
						}
					}
				}
			}
			ddcs = new HashMap<>();
		}

		System.out.println("time spend:" + (System.currentTimeMillis() - start));
	}


	public static String formatProp(String name, String value,boolean addSuffixZeichen){
		StringBuilder sb = new StringBuilder().append("[").append(name).append("¤").append(value);
		if(addSuffixZeichen)
			sb.append("¤");
		sb.append("]");
		return sb.toString();
	}

	public static String formatDDC(double[]ddc){
		if(ddc == null)
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ddc.length; i++) {
			sb.append(i*10).append("¶").append(ddc[i]).append("¤");
		}
		return sb.toString();
	}


	public synchronized static double[] getDDC(String language, String pageName){
		System.out.println(language+"\t"+pageName);
		if(!ddcs.containsKey(language)){
			//			ddcLanguageOrder.add(language);
			//			if(ddcLanguageOrder.size()>3)
			//			{
			//				ddcs.remove(ddcLanguageOrder.get(0));
			//				ddcLanguageOrder.remove(0);
			//			}
			try (BufferedReader br = Files.newBufferedReader(Paths.get("/mnt/hydra/vol/public/baumartz/wikipedia.v8/wiki_archive/"+language+"wiki/"+language+"wiki.token.export_for_tagging."+language+"wiki.token_gnd_ddc.v5.with_categories-lr.0.2-lrUR150-minC5-dim300-ep1000-vec-cc."+language+".vec.best_epoch.bin.predicted.20191001.txt"))) {
				try (BufferedReader brNames = Files.newBufferedReader(Paths.get("/mnt/hydra/vol/public/baumartz/wikipedia.v8/wiki_archive/"+language+"wiki/"+language+"wiki.token"))) {
					// read line by line
					String line;
					String page;
					int lineCount = 0;
					while ((line = br.readLine()) != null) {
						double[]ddc = new double[100];
						lineCount++;
						page = brNames.readLine().split("\t")[0];
						ArrayList<String>ddcList = new ArrayList<>(Arrays.asList(line.split("__label_ddc__")));
						for (String string : ddcList) {
							if(string.trim().length()==0)
								continue;

							String[]split = (string.trim().split(" "));
							ddc[Integer.parseInt(split[0].trim())/10] = Double.parseDouble(split[1].trim());
						}

						if(!ddcs.containsKey(language))
							ddcs.put(language, new HashMap<>());
						ddcs.get(language).put(page, ddc);

						if(lineCount%100000==0)
							System.out.println(lineCount);
					}
				}

			} catch (IOException e) {
				System.err.format("IOException: %s%n", e);
			}
		}
		return ddcs.get(language).get(pageName);
	}


}
