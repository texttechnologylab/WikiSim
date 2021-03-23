package siteStatistics;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StubArticlesReader extends DefaultHandler {
    class Page {
        private int ns;
        private long id;
        private long revId;
        private String title;

        public Page(int ns, long id, long revId, String title) {
            this.ns = ns;
            this.id = id;
            this.revId = revId;
            this.title = title;
        }

        public int getNs() {
            return ns;
        }

        public long getId() {
            return id;
        }

        public long getRevId() {
            return revId;
        }

        public String getTitle() {
            return title;
        }
    }

    private Path outFile = null;

    private List<Page> pages;
    private StringBuilder characters;

    private boolean inPage;
    private boolean inPageId;
    private boolean inTitle;
    private boolean inRevision;
    private boolean inRevisionId;
    private boolean inNs;

    private int startTitle;
    private int startPageId;
    private int startRevisionId;
    private int startNs;

    private String pageTitle;
    private String pageId;
    private String pageRevId;
    private String pageNs;

    public List<Page> getPages() {
        return pages;
    }

    @Override
    public void startDocument() throws SAXException {
        pages = new ArrayList<>();
        characters = new StringBuilder();

        inPage = false;
        inPageId = false;
        inTitle = false;
        inRevision = false;
        inRevisionId = false;
        inNs = false;

        startTitle = 0;
        startPageId = 0;
        startRevisionId = 0;
        startNs = 0;

        pageTitle = "";
        pageId = "";
        pageRevId = "";
        pageNs = "";
    }

    @Override
    public void endDocument() throws SAXException {
        if (outFile != null) {
            try (BufferedWriter writer = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
                for (Page p : pages) {
                    writer.write(p.getNs() + "\t" + p.getId() + "\t" + p.getRevId() + "\t" + p.getTitle() + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case "page": {
                inPage = true;
                break;
            }
            case "title": {
                if (inPage && !inTitle) {
                    inTitle = true;
                    startTitle = characters.length();
                }
                break;
            }
            case "ns": {
                if (inPage && !inNs) {
                    inNs = true;
                    startNs = characters.length();
                }
                break;
            }
            case "revision": {
                if (inPage && !inRevision) {
                    inRevision = true;
                }
                break;
            }
            case "id": {
                if (inRevision && !inRevisionId && pageRevId.isEmpty()) {
                    inRevisionId = true;
                    startRevisionId = characters.length();
                }
                else if (inPage && !inPageId && pageId.isEmpty()) {
                    inPageId = true;
                    startPageId = characters.length();
                }
                break;
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "page": {
                int pNs = Integer.parseInt(pageNs);
                long pId = Long.parseLong(pageId);
                long rId = Long.parseLong(pageRevId);

                Page page = new Page(pNs, pId, rId, pageTitle);
                pages.add(page);

                inPage = false;

                startTitle = 0;
                startPageId = 0;
                startRevisionId = 0;
                startNs = 0;

                pageTitle = "";
                pageId = "";
                pageRevId = "";
                pageNs = "";

                characters.setLength(0);

                System.out.println("#pages: " + pages.size());

                break;
            }
            case "title": {
                if (inTitle) {
                    inTitle = false;

                    pageTitle = characters.substring(startTitle);
                    characters.setLength(startTitle);
                }
                break;
            }
            case "ns": {
                if (inNs) {
                    inNs = false;

                    pageNs = characters.substring(startNs);
                    characters.setLength(startNs);
                }
                break;
            }
            case "revision": {
                if (inPage) {
                    inRevision = false;
                }
                break;
            }
            case "id": {
                if (inRevisionId) {
                    inRevisionId = false;

                    pageRevId = characters.substring(startRevisionId);
                    characters.setLength(startRevisionId);
                }
                else if (inPageId) {
                    inPageId = false;

                    pageId = characters.substring(startPageId);
                    characters.setLength(startPageId);
                }
                break;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        characters.append(ch, start, length);
    }

    public StubArticlesReader(InputStream content, Path outFile) throws ParserConfigurationException, SAXException, IOException {
        this.outFile = outFile;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(content, this);

        System.out.println("done.");
    }
}
