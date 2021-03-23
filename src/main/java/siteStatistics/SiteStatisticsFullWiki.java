package siteStatistics;

import me.tongfei.progressbar.ProgressBar;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SiteStatisticsFullWiki {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        Path stubFile = Paths.get("dewiki-20210220-stub-articles.xml");
        Path normdatenFile = Paths.get("normdaten_templates.tsv");

        Path outFile = Paths.get("dewiki-20210220-article-vectors.vec.txt");

        System.out.println("loading normdaten templates...");
        NormdatenTemplate normdatenTemplate = new NormdatenTemplate(normdatenFile);

        System.out.println("loading article names from dump file...");
        StubArticlesReader stubArticles = new StubArticlesReader(Files.newInputStream(stubFile, StandardOpenOption.READ), null);

        System.out.println("loading " + stubArticles.getPages().size() + " articles...");
        try (ProgressBar pb = new ProgressBar("WP-API", stubArticles.getPages().size());
             BufferedWriter writer = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            for (StubArticlesReader.Page page : stubArticles.getPages()) {
                pb.setExtraMessage(page.getTitle());

                PageStatistics statistics = new PageStatistics("de", page.getTitle(), normdatenTemplate);

                // ID
                writer.write(String.valueOf(statistics.getPageId()));
                writer.write("\t");

                // article vector data
                writer.write(String.valueOf(statistics.getSize()));
                writer.write(" ");
                writer.write(String.valueOf(statistics.getCategories().size()));
                writer.write(" ");
                writer.write(String.valueOf(statistics.getImages().size()));
                writer.write(" ");
                writer.write(String.valueOf(statistics.getNumberOfTables()));
                writer.write(" ");
                writer.write(String.valueOf(statistics.getExtlinks().size()));
                writer.write(" ");
                writer.write(String.valueOf(statistics.getLinks().size()));
                writer.write(" ");
                writer.write(String.valueOf(statistics.getSections().size()));
                writer.write(" ");
                writer.write(String.valueOf(statistics.getBreadthOfTOC()));
                writer.write(" ");
                writer.write(String.valueOf(statistics.getDepthOfTOC()));
                writer.write(" ");
                writer.write(String.valueOf(statistics.getNumberOfReferences()));
                writer.write(" ");
                writer.write(String.valueOf(statistics.getNumberOfNormdata()));
                writer.write(" ");

                // end of line
                writer.newLine();

                pb.step();
            }
        }

        System.out.println("done");
    }
}
