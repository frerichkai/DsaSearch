package de.vierheldenundeinschelm.dsasearch;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class SearchEngine {

    @Value("${dsa.search.lucene-path}")
    private String lucenePath;

    private Directory memoryIndex;
    private StandardAnalyzer analyzer;
    private IndexWriterConfig indexWriterConfig;

    @PostConstruct
    public void init() throws Exception {
        memoryIndex = FSDirectory.open(Path.of(lucenePath));
        analyzer = new StandardAnalyzer();


    }

    public void addPage(DsaPage page) throws IOException {
        indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(memoryIndex, indexWriterConfig);
        Document document = new Document();
        document.add(new TextField("path", page.getPdfPath(), Field.Store.YES));
        document.add(new TextField("text", page.getText(), Field.Store.YES));
        document.add(new StoredField("number", page.getNumber()));
        writer.addDocument(document);
        writer.close();
    }

    public List<DsaPage> search(String searchTerm ) throws Exception {
        Query query = new WildcardQuery(new Term("text",searchTerm.toLowerCase()));

        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, 1000);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcher.doc(scoreDoc.doc));
        }

        return Stream.of(topDocs.scoreDocs)
            .map( scoreDoc -> {
                try {
                    return searcher.doc(scoreDoc.doc);
                } catch (IOException e) {
                    return null;
                }
            }).map(doc -> new DsaPage(
                doc.getField("path").stringValue(),
                doc.getField("number").numericValue().intValue(),
                doc.getField("text").stringValue())).toList();
    }

}
