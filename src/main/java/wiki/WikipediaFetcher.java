package wiki;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikipediaFetcher {


//	static Connection con;
//	static Statement stmt;

	HashMap<String, String> wikidataIds = new HashMap<>();


	public WikipediaFetcher() throws SQLException{
//		con =  DriverManager.getConnection("jdbc:mysql://141.2.89.28:3306/wikipedia?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "ahemati","tXD3b%ld");
//		stmt = con.createStatement();
	}

	public List<String> getWikiDataLinks(String language, String pageId) throws IOException{
		List<String> links = new ArrayList<>();
		String url = String.format("https://%s.wikipedia.org/w/api.php?action=query&generator=links&format=xml&redirects=1&titles=%s&prop=pageprops&gpllimit=500&ppprop=wikibase_item",language,pageId);
		Document doc  = Jsoup.connect(url).header("Accept-Encoding", "gzip, deflate").get();
		Elements p =  doc.select("query > pages > page > pageprops");
		for (Element element : p) {
			links.add(element.attr("wikibase_item"));
		}
		
		String cont = null;
		if(!doc.getElementsByTag("continue").isEmpty()){
			cont = doc.getElementsByTag("continue").first().attr("gplcontinue");
		}
		while(cont != null){
			url = String.format("https://%s.wikipedia.org/w/api.php?action=query&generator=links&format=xml&redirects=1&titles=%s&prop=pageprops&gpllimit=500&ppprop=wikibase_item&gplcontinue="+cont,language,pageId);
			System.out.println(url);
			doc  = Jsoup.connect(url).header("Accept-Encoding", "gzip, deflate").get();
			p =  doc.select("query > pages > page > pageprops");
			for (Element element : p) {
				links.add(element.attr("wikibase_item"));
			}
			
			cont = null;
			if(!doc.getElementsByTag("continue").isEmpty()){
				cont = doc.getElementsByTag("continue").first().attr("gplcontinue");
			}	
		}
		return links;

	}

	public List<String> getWikiLinks(String language, String pageId) throws IOException{
		List<String> links = new ArrayList<>();
		String url = String.format("https://%s.wikipedia.org/wiki/%s",language,pageId);
		Document doc  = Jsoup.connect(url).header("Accept-Encoding", "gzip, deflate").get();
		Elements p =  doc.select("div#bodyContent a");
		for (Element element : p) {
			links.add(element.attr("href"));
		}
		return links;
	}
	
	public String getWikipediaFromWikidata(String language, String wikidataId) throws IOException{
		String url = String.format("https://www.wikidata.org/w/api.php?action=wbgetentities&format=xml&props=sitelinks&ids=%s&sitefilter=%swiki",wikidataId, language);
		Document doc  = Jsoup.connect(url).header("Accept-Encoding", "gzip, deflate").get();
		Elements p =  doc.select("sitelink");
		for (Element element : p) {
			return element.attr("title");
		}
		return null;
	}

	public String getWikidataId(String language, String wikipediaLink) throws IOException{
		if(wikidataIds.containsKey(language+"__"+wikipediaLink))
			return wikidataIds.get(language+"__"+wikipediaLink);
		else{
			String url = "https://" + language + ".wikipedia.org/w/api.php?action=query&format=xml&prop=pageprops&ppprop=wikibase_item&redirects=1&titles="+wikipediaLink;
			try{
				String wikidataId = Jsoup.connect(url).get().select("pageprops").first().attr("wikibase_item");
				wikidataIds.put(language+"__"+wikipediaLink, wikidataId);
				return wikidataId;
			}catch(NullPointerException e){
				wikidataIds.put(language+"__"+wikipediaLink, null);
				return null;
			}catch(HttpStatusException e){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return getWikidataId(language,wikipediaLink);			
			}
		}
	}
}
