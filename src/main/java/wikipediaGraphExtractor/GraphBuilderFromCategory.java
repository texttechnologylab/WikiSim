package wikipediaGraphExtractor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.json.JSONException;

import graph.CachedGraph;
import wiki.WikidataFetcher;
import wiki.WikipediaFetcher;
import wiki.WikipeidaWikidata;

public class GraphBuilderFromCategory {
	static int current = 0;

	public static void main(String[] args) throws JSONException, IOException, SQLException {
		WikipediaFetcher fetcher = new WikipediaFetcher();

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

		String[]outputPaths=new String[]{
				"mathematicsGML",
				"astronomyGML",
				"biologyGML",
				"chemistryGML",
				"geologyGML",
				"humanitiesGML",
				"militaryScienceGML",
				"musicologyGML",
				"physicsGML",
				"psychologyGML",
				"technologyGML",
				"economicsGML"
		};

		String[]qids = new String[]{
				"Q395",
				"Q333",
				"Q420",
				"Q2329",
				"Q1069",
				"Q80083",
				"Q192386",
				"Q164204",
				"Q413",
				"Q9418",
				"Q11016",
				"Q8134"
		};

		for(int i = 0;i<outputPaths.length;i++){
			String qid = qids[i];
			String outputPath = outputPaths[i];

			HashMap<String, List<WikipeidaWikidata>> languageWikidataMap=  WikidataFetcher.getWikipediaLinksByLanguagesForWikidataScienceCategory(qid);


			HashSet<String>entities = new HashSet<>();
			for (Entry<String, List<WikipeidaWikidata>> entry : languageWikidataMap.entrySet()) {
				for (WikipeidaWikidata item : entry.getValue()) {
					entities.add(item.wikidataId);
				}
			}

			System.out.println(outputPath);
			System.out.println(entities.size());
			System.out.println();
			if(true)
				continue;

			Path fullgml = Paths.get("/resources/public/hemati/WikipediaGraphs/graphsCategory","fullgml",outputPath);
			fullgml.toFile().mkdirs();

			Path gmlPath = Paths.get("/resources/public/hemati/WikipediaGraphs/graphsCategory","gml",outputPath);
			gmlPath.toFile().mkdirs();

			languageWikidataMap.entrySet().forEach(
					entry->{
						String language = entry.getKey().replace("wiki", "");
						//						if(!consideredLanguages.contains(language))
						//							return;
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
