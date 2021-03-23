package siteStatistics;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Normdaten Template in den verschiedenen Sprachversionen
 */
public class NormdatenTemplate {
    private final Map<String, String> langNormdatenTemplateMap;

    public NormdatenTemplate(Path filename) throws IOException {
        langNormdatenTemplateMap = new HashMap<>();

        Map<String, Map<String, String>> articleMap = new HashMap<>();

        List<String> lines = FileUtils.readLines(filename.toFile(), StandardCharsets.UTF_8);
        boolean skippedFirstLine = false;
        for (String line : lines) {
            if (!skippedFirstLine) {
                skippedFirstLine = true;
                continue;
            }

            // article \t property \t value
            String[] fields = line.split("\t", 3);
            String article = fields[0].trim();
            String property = fields[1].trim();
            String value = fields[2].trim();

            if (!articleMap.containsKey(article)) {
                articleMap.put(article, new HashMap<>());
            }
            articleMap.get(article).put(property, value);
        }

        // reformat to lang -> name
        for (Map.Entry<String, Map<String, String>> entry : articleMap.entrySet()) {
            String lang = entry.getValue().get("http://schema.org/isPartOf");
            String template = entry.getValue().get("http://schema.org/name");
            langNormdatenTemplateMap.put(lang, template);
        }
    }

    public String getNormdatenTemplate(String lang) {
        String url = String.format("https://%s.wikipedia.org/", lang);
        String template = langNormdatenTemplateMap.get(url);
        if (template != null && !template.isEmpty()) {
            template = template.split(":", 2)[1].trim();
        }
        return template;
    }
}
