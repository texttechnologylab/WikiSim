package wikipediaGraphExtractor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.json.JSONException;

import graph.CachedGraph;
import wiki.WikidataFetcher;
import wiki.WikipediaFetcher;
import wiki.WikipeidaWikidata;

public class GraphBuilderTopPages {
	static int current = 0;

	public static void main(String[] args) throws JSONException, IOException, SQLException {
		WikipediaFetcher fetcher = new WikipediaFetcher();

		HashSet<String>consideredLanguages = new HashSet<>();
//				consideredLanguages.add("ceb");
//				consideredLanguages.add("war");
				consideredLanguages.add("zh");
//		consideredLanguages.add("sv");
//		consideredLanguages.add("vi");
//		consideredLanguages.add("nl");
//		consideredLanguages.add("fr");
//		consideredLanguages.add("pl");
//		consideredLanguages.add("en");
//		consideredLanguages.add("it");
//		consideredLanguages.add("de");
//		consideredLanguages.add("ru");
//		consideredLanguages.add("es");
//		consideredLanguages.add("ja");

		String[]wikidataIds=new String[]{"Q361", "Q362", "Q148", "Q145", "Q22686", "Q8646", "Q23781155", "Q17", "Q30", "Q52", "Q866"};


		String outputPath = "topVisited";

		Path fullgml = Paths.get("graphs","fullgml",outputPath);
		fullgml.toFile().mkdirs();

		Path gmlPath = Paths.get("graphs","gml",outputPath);
		gmlPath.toFile().mkdirs();

		for (String language : consideredLanguages) {
			System.out.println(language);
			DefaultDirectedGraph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
			for (String wikidataId : wikidataIds) {
				String wikipedialink = (fetcher.getWikipediaFromWikidata(language, wikidataId));
				directedGraph.addVertex(wikidataId);
				List<String> wikidataLinks = fetcher.getWikiDataLinks(language, wikipedialink);
				for (String string2 : wikidataLinks) {
					directedGraph.addVertex(string2);
					directedGraph.addEdge(wikidataId, string2);
				}
			}
			CachedGraph.saveGraph(directedGraph, Paths.get(fullgml.toString(), language + ".gml").toString(),true);
		}
		
		
//		Wiki.getWikipediaLinksByLanguagesForWikidataHyponym(pid,qid).entrySet().forEach(
//				entry->{
//					String language = entry.getKey().replace("wiki", "");
//					if(!consideredLanguages.contains(language))
//						return;
//					System.out.println(language);
//					DefaultDirectedGraph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);		
//					System.out.println(entry.getValue().size());
//					current = 0;
//
//					entry.getValue().forEach(
//							link -> {
//								System.out.println(current++ + "/" + entry.getValue().size());
//
//								try {
//									String wikidataId = link.wikidataId;
//									if(wikidataId!=null){
//										directedGraph.addVertex(wikidataId);
//										List<String> wikidataLinks = fetcher.getWikiDataLinks(language, link.wikipediaLink);
//										for (String string2 : wikidataLinks) {
//											directedGraph.addVertex(string2);
//											directedGraph.addEdge(wikidataId, string2);
//										}
//									}
//								} catch (IOException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//
//							}
//							);
//
//					CachedGraph.saveGraph(directedGraph, Paths.get(fullgml.toString(), language + ".gml").toString());
//
//					CachedGraph.simplifyGraph(directedGraph,entities);
//
//					CachedGraph.saveGraph(directedGraph, Paths.get(gmlPath.toString(), language + ".gml").toString());
//
//
//				});
//	}
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
