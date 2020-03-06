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
import wiki.Wiki;
import wiki.WikipediaFetcher;

public class GraphBuilderNew {
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
				"krankheitGML",
				"theoremGML",
				"paintingGML",
				"holydayGML",
				"killerGML",
				"fussballligaGML",
				"komponistGML",
				"warGML"
		};

		String[]pids = new String[]{
				"P31",
				"P31",
				"P31",
				"P31",
				"P1399",
				"P31",
				"P106",
				"P279"
		};

		String[]qids = new String[]{
				"Q12136",
				"Q65943",
				"Q3305213",
				"Q1197685",
				"Q132821",
				"Q15991303",
				"Q36834",
				"Q350604",
		};

		for(int i = 0;i<outputPaths.length;i++){
			String pid = pids[i];
			String qid = qids[i];
			String outputPath = outputPaths[i];
			
			
			HashSet<String>entities = Wiki.getEntities(pid,qid);
			System.out.println(outputPath);
			System.out.println(entities.size());
			
			Path entityObjects = Paths.get("graphs","entities",outputPath+".txt");
			entityObjects.toFile().getParentFile().mkdirs();
			FileUtils.writeLines(entityObjects.toFile(), entities);
			
			
			Path fullgml = Paths.get("graphs","fullgml",outputPath);
			fullgml.toFile().mkdirs();

			
			Path gmlPath = Paths.get("graphs","gml",outputPath);
			gmlPath.toFile().mkdirs();

//			if(true)
//				continue;
			
			Wiki.getWikipediaLinksByLanguagesForWikidataHyponym(pid,qid).entrySet().forEach(
					entry->{
						String language = entry.getKey().replace("wiki", "");
						if(!consideredLanguages.contains(language))
							return;
						System.out.println(language);
						DefaultDirectedGraph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);		
						System.out.println(entry.getValue().size());
						current = 0;

						entry.getValue().forEach(
								link -> {
									System.out.println(current++ + "/" + entry.getValue().size());
									try {
										String wikidataId = link.wikidataId;
										if(wikidataId!=null){
											directedGraph.addVertex(wikidataId);
											List<String> wikidataLinks = fetcher.getWikiDataLinks(language, link.wikipediaLink);
											for (String string2 : wikidataLinks) {
												directedGraph.addVertex(string2);
												directedGraph.addEdge(wikidataId, string2);
											}
										}
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								);

						CachedGraph.saveGraph(directedGraph, Paths.get(fullgml.toString(), language + ".gml").toString());
						CachedGraph.simplifyGraph(directedGraph,entities);
						CachedGraph.saveGraph(directedGraph, Paths.get(gmlPath.toString(), language + ".gml").toString());
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
				hyponyms.addAll(Wiki.getWikidataSubclassOf(string));
				hyponymsTmpTmp.addAll(Wiki.getWikidataSubclassOf(string));
			}
			hyponymsTmp = hyponymsTmpTmp;
		}
		return hyponyms;
	}

}
