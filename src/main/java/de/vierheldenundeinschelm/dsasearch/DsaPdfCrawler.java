package de.vierheldenundeinschelm.dsasearch;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@Service
@Log
@RequiredArgsConstructor
public class DsaPdfCrawler {

    private final SearchEngine searchEngine;

    @Value("${dsa.search.drivethru-path}")
    private String drivethruPath;

    @Value("${dsa.search.lucene-path}")
    private String tempPath;

    public void crawl() throws Exception {
        crawl(drivethruPath);
    }

    private void crawl( String baseDirectory ) throws Exception {
        Files.walk(Path.of(baseDirectory))
            .filter(path -> path.toString().toLowerCase().endsWith(".pdf") )
            .forEach( this::parse );
    }

    private void parse( Path pdfPath ) {
        try (PDDocument pdfDocument = PDDocument.load(pdfPath.toFile())) {
            for( int pageNumber=0; pageNumber<pdfDocument.getNumberOfPages(); pageNumber++)
                parsePage(pdfPath, pdfDocument, pageNumber);
            System.out.println(pdfPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parsePage(Path pdfPath, PDDocument pdfDocument, int pageNumber) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageNumber);
        stripper.setEndPage(pageNumber);
        String text = stripper.getText(pdfDocument);
        Path relativePath = Path.of(drivethruPath).relativize(pdfPath);
        searchEngine.addPage(new DsaPage(relativePath.toString(), pageNumber, text));

        if( pageNumber==0 )
            erzeugeVorschaubild(pdfDocument, pageNumber, relativePath);
    }

    private void erzeugeVorschaubild(PDDocument pdfDocument, int pageNumber, Path relativePath) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
        BufferedImage image = pdfRenderer.renderImageWithDPI(pageNumber, 100, ImageType.RGB);
        File imageFile = Path.of(tempPath, "preview",
            relativePath.toString() + "." + pageNumber + ".jpg").toFile();
        imageFile.mkdirs();
        ImageIO.write(image, "JPG", imageFile);
    }

}
