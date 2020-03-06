package wiki;
public class WikipeidaWikidata{
	public String wikipediaLink;
	public String wikipediaLanguage;
	public String wikidataId;
	public WikipeidaWikidata(String wikipediaLink, String wikipediaLanguage, String wikidataId){
		this.wikidataId = wikidataId;
		this.wikipediaLink = wikipediaLink;
		this.wikipediaLanguage = wikipediaLanguage;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return wikipediaLanguage + ":"+wikipediaLink;
	}
}