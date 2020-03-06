package wiki;

import java.io.IOException;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class test {

	public static void main(String...args) throws JSONException, IOException{
		String title = "Honolulu";
		String url = "https://de.wikipedia.org/w/api.php?action=query&prop=pageprops&ppprop=wikibase_item&redirects=1&format=xml&titles="+title;
		Document doc = Jsoup.connect(url).ignoreContentType(true).execute().parse();
		String wikidataId = (doc.select("query > pages pageprops").get(0).attr("wikibase_item"));
		System.out.println(wikidataId);
	}
}
