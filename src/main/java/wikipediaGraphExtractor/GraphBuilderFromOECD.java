package wikipediaGraphExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.net.UrlEscapers;

import graph.CachedGraph;
import wiki.WikidataFetcher;
import wiki.WikipediaFetcher;
import wiki.WikipeidaWikidata;

public class GraphBuilderFromOECD {
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
		consideredLanguages.add("tr");
		consideredLanguages.add("vi");
		consideredLanguages.add("war");
		consideredLanguages.add("zh");
		
		System.out.println(consideredLanguages.size());


		JSONArray json = new JSONArray(FileUtils.readFileToString(new File("wissensbereicheRecursiveThemen.json")));
		for (Object object : json) {
			JSONObject thema = (JSONObject) object;
			
			
			HashSet<JSONObject>studies = new HashSet<>();
			if(thema.has("studies"))
				for (Object study : thema.getJSONArray("studies")) {
					JSONObject studyJson = (JSONObject) study;
					studies.add(studyJson);
				}
			if(thema.has("subclasses"))
				thema.getJSONArray("subclasses").forEach(x -> ((JSONObject)x).getJSONArray("studies").forEach(y -> studies.add(((JSONObject)y))));

			for (JSONObject jsonObject : studies) {
				Path fullgml = Paths.get("graphs/oecdTopics/","fullgml",jsonObject.getString("name"));
				fullgml.toFile().mkdirs();

				Path gmlPath = Paths.get("graphs/oecdTopics/","gml",jsonObject.getString("name"));
				gmlPath.toFile().mkdirs();
				HashSet<String> study = new HashSet<>();
				study.add(jsonObject.getString("qid"));
				
				HashMap<String, List<WikipeidaWikidata>> languageWikidataMap=  WikidataFetcher.getWikipediaLinksByLanguagesForWikidataHyponym(study,false, 20000);
				HashSet<String>entities = new HashSet<>();
				for (Entry<String, List<WikipeidaWikidata>> entry : languageWikidataMap.entrySet()) {
					for (WikipeidaWikidata item : entry.getValue()) {
						entities.add(item.wikidataId);
					}
				}

				FileUtils.writeLines(Paths.get("graphs/oecdTopics/","entities",jsonObject.getString("name")).toFile(), entities);
				languageWikidataMap.entrySet().forEach(
						entry->{
							String language = entry.getKey().replace("wiki", "");
							
							if(Paths.get(fullgml.toString(), language + ".gml").toFile().exists() && Paths.get(gmlPath.toString(), language + ".gml").toFile().exists())
								return;
							if(!consideredLanguages.contains(language))
								return;
							DefaultDirectedGraph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);		
							current = 0;

							entry.getValue().forEach(
									link -> {
										System.out.println(language + "\t"+jsonObject.getString("name")+"\t"+current++ + "/" + entry.getValue().size());
										String wikidataId = link.wikidataId;
										//										if(wikidataId!=null){
										directedGraph.addVertex(wikidataId);
										List<String> wikidataLinks = fetcher.getWikiDataLinks(language, UrlEscapers.urlPathSegmentEscaper().escape(link.wikipediaLink).replace("+", "%2B").replace("&", "%26"));
										for (String string2 : wikidataLinks) {
											directedGraph.addVertex(string2);
											directedGraph.addEdge(wikidataId, string2);
										}
									}
									);

							CachedGraph.saveGraph(directedGraph, Paths.get(fullgml.toString(), language + ".gml").toString(),true);
							CachedGraph.simplifyGraph(directedGraph,entities);
							CachedGraph.saveGraph(directedGraph, Paths.get(gmlPath.toString(), language + ".gml").toString(),true);
						});
			}
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
