package wikipediaGraphExtractor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.json.JSONException;

import graph.CachedGraph;
import wiki.WikidataFetcher;
import wiki.WikipediaFetcher;

public class GraphBuilderNew {
	static int current = 0;

	public static void main(String[] args) throws JSONException, IOException, SQLException {
		WikipediaFetcher fetcher = new WikipediaFetcher();

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

		String[]outputPaths=new String[]{
				"krankheitGML",
				"theoremGML",
				"paintingGML",
				"holydayGML",
				"fussballligaGML",
				"komponistGML",
		};

		String[]pids = new String[]{
				"P31",
				"P31",
				"P31",
				"P31",
				"P31",
				"P106",
		};

		String[]qids = new String[]{
				"Q12136",
				"Q65943",
				"Q3305213",
				"Q1197685",
				"Q15991303",
				"Q36834",
		};

		for(int i = 0;i<outputPaths.length;i++){
			String pid = pids[i];
			String qid = qids[i];
			String outputPath = outputPaths[i];


			HashSet<String>entities = WikidataFetcher.getEntities(pid,qid);
			System.out.println(outputPath);
			System.out.println(entities.size());

			Path entityObjects = Paths.get("graphsReduced","entities",outputPath+".txt");
			entityObjects.toFile().getParentFile().mkdirs();
			FileUtils.writeLines(entityObjects.toFile(), entities);


			Path fullgml = Paths.get("graphsReduced","fullgml",outputPath);
			fullgml.toFile().mkdirs();


			Path gmlPath = Paths.get("graphsReduced","gml",outputPath);
			gmlPath.toFile().mkdirs();

			WikidataFetcher.getWikipediaLinksByLanguagesForWikidataHyponym(pid,qid,-1).entrySet().forEach(
					entry->{
						String language = entry.getKey().replace("wiki", "");
						if(!consideredLanguages.contains(language) || Paths.get(fullgml.toString(), language + ".gml").toFile().exists())
							return;
						System.out.println(language);
						DefaultDirectedGraph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);		
						System.out.println(entry.getValue().size());
						current = 0;

						entry.getValue().forEach(
								link -> {
									System.out.println(current++ + "/" + entry.getValue().size());
									String wikidataId = link.wikidataId;
									if(wikidataId!=null){
										directedGraph.addVertex(wikidataId);
										List<String> wikidataLinks = fetcher.getWikiDataLinks(language, link.wikipediaLink);
										for (String string2 : wikidataLinks) {
											directedGraph.addVertex(string2);
											directedGraph.addEdge(wikidataId, string2);
										}
									}
								}
								);

						CachedGraph.saveGraph(directedGraph, Paths.get(fullgml.toString(), language + ".gml").toString(),true);
						CachedGraph.simplifyGraph(directedGraph,entities);
						CachedGraph.saveGraph(directedGraph, Paths.get(gmlPath.toString(), language + ".gml").toString(),true);
					});
		}
	}



	public static List<String>hyponyms (String root, int level) throws JSONException, IOException{
		List<String> hyponyms = new ArrayList<>();
		List<String> hyponymsTmp = new ArrayList<>();
		hyponymsTmp.add(root);
		hyponyms.add(root);
		for (int i = 0; i < level; i++) {
			List<String> hyponymsTmpTmp = new ArrayList<>();
			for (String string : hyponymsTmp) {
				hyponyms.addAll(WikidataFetcher.getWikidataSubclassOf(string));
				hyponymsTmpTmp.addAll(WikidataFetcher.getWikidataSubclassOf(string));
			}
			hyponymsTmp = hyponymsTmpTmp;
		}
		return hyponyms;
	}

}
