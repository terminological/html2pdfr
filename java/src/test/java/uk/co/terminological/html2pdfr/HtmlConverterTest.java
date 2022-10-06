package uk.co.terminological.html2pdfr;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import uk.co.terminological.html2pdfr.AutoFont.CSSFont;
import uk.co.terminological.rjava.RConverter;
import uk.co.terminological.rjava.types.RCharacter;
import uk.co.terminological.rjava.types.RCharacterVector;
import uk.co.terminological.rjava.types.RNumeric;

class HtmlConverterTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	private String getContent(String s) {
		InputStream in = HtmlConverterTest.class.getResourceAsStream(s);
		BufferedReader in2 = new BufferedReader(new InputStreamReader(in));
		String html = in2.lines().collect(Collectors.joining());
		return(html);
	}
	
	private String getContent(int i) {
		return getContent("/test"+i+".html");
	}
	
	private String out(String name) {
		Path p = Paths.get(System.getProperty("user.home"),"tmp",name);
		try {
			Files.createDirectories(p.getParent());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return p.toString();
	}
	
	@Test
	final void testReader() {
		System.out.println(getContent(1));
	}

	@Test
	final void testPdfRender() throws IOException {
		String html = getContent(4);
		HtmlConverter cov = new HtmlConverter(
				new String[] {
						"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf", 
						"/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf"});
		cov.renderHtmlToFitA4(html, out("test4"),1);
	}
	
	@Test
	final void testPdfRender2() throws IOException {
		String html = getContent(6);
		HtmlConverter cov = new HtmlConverter(
				new String[] {
						"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf", 
						"/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf"});
		HtmlConverter.htmlFragmentToPdf(
			html, 
			out("test6"), 
			RNumeric.from(0),
			RNumeric.from(0),
			RNumeric.from(5.9),
			RNumeric.from(8.0),
			RCharacterVector.with("pdf","png"), 
			RNumeric.from(300),
			cov
		);
	}
	
	@Test
	final void testStringToPdf() throws IOException {
		String html = getContent(2);
		HtmlConverter cov = new HtmlConverter(
				new String[] {
						"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf", 
						"/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf"});
		HtmlConverter.htmlDocumentToPdf(
				html, out("test2"), 
				RCharacter.NA, 
				RCharacter.NA,
				RNumeric.NA,
				RNumeric.NA,
				RNumeric.NA,
				RNumeric.NA,
				RCharacterVector.with("pdf"), 
				RNumeric.from(300),
				cov
		);
	}
	
	@Test
	final void testNBSP() throws IOException {
		String html = getContent(7);
		HtmlConverter cov = new HtmlConverter(
				new String[] {
						"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf", 
						"/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf"});
		HtmlConverter.htmlFragmentToPdf(
				html, out("test7"), 
				RNumeric.NA,
				RNumeric.NA,
				RNumeric.from(4),
				RNumeric.from(8),
				RCharacterVector.with("pdf"), 
				RNumeric.from(300),
				cov
		);
	}
	
	@Test
	final void testSvgToPdf() throws IOException {
		String html = getContent("/svgTest.svg");
		HtmlConverter cov = new HtmlConverter(
				new String[] {
						"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf", 
						"/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf"});
		cov.renderHtmlToFitA4(html, out("svg"), 1);
	}
	
	@Test
	final void testUrlToPdf() throws IOException {
		String html = getContent("/testDocument.html");
		HtmlConverter cov = new HtmlConverter(
				new String[] {
						"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf", 
						"/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf"});
		assertThrows(IOException.class, () -> {
			HtmlConverter.htmlDocumentToPdf(
				html, 
				out("testUrl.pdf"), 
				RConverter.convert("http://www.w3.org/People/mimasa/test/xhtml/media-types/test.html"),
				RCharacter.NA,
				RNumeric.NA,
				RNumeric.NA,
				RNumeric.NA,
				RNumeric.NA,
				RCharacterVector.with("pdf"), 
				RNumeric.from(300),
				cov
			);
		});
	}
	
	@Test
	final void testUrlToPdf2() throws IOException {
		// String html = "http://www.w3.org/People/mimasa/test/xhtml/media-types/test.html";
		String html = "https://cran.r-project.org/banner.shtml";
		HtmlConverter cov = new HtmlConverter(
				new String[] {
						"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf", 
						"/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf"});
		HtmlConverter.urlToPdf(
			html, out("testUrl2.pdf"), 
			RCharacter.NA,
			RNumeric.NA,
			RNumeric.NA,
			RNumeric.NA,
			RNumeric.NA,
			RCharacterVector.with("pdf"), 
			RNumeric.from(300),
			cov
			
		);
	}
	
	@Test
	final void testUrlToPdf3() throws IOException {
		String html = "https://developer.mozilla.org/en-US/docs/Web/SVG/Tutorial/Getting_Started";
		HtmlConverter cov = new HtmlConverter(
				new String[] {
						"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf", 
						"/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf"});
		HtmlConverter.urlToPdf(html, out("testUrl3.pdf"), 
			RCharacter.from("#content"),
			RNumeric.NA,
			RNumeric.NA,
			RNumeric.NA,
			RNumeric.NA,
			RCharacterVector.with("pdf"), 
			RNumeric.from(300),
			cov	
		);
	}
	
	@Test
	final void testFonts() throws IOException {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = ge.getAllFonts();

        for (Font font : fonts) {
        	System.out.print(font.getFontName() + " : ");
            System.out.println(font.getFamily());
        }
        //sun.font.FontManagerFactory.getInstance()
        //String fontFilePath = FontManager.getFontPath( true ) + "/" + FontManager.getFileNameForFontName( fontName );
        Font font = Font.decode(Font.SANS_SERIF);
        System.out.print(font.getFontName() + " : ");
        System.out.println(font.getFamily());
        
//        Font font = Font.decode(Font.SANS_SERIF);
//        System.out.print(font.getFontName() + " : ");
//        System.out.println(font.getFamily());
        
        Path systemFontDirectory = Paths.get(System.getProperty("java.home"), "lib","fonts");
        List<CSSFont> autofonts = AutoFont.findFontsInDirectory(systemFontDirectory);
        autofonts.forEach(f -> System.out.println(f.family+": "+f.path));
        
        System.getProperties().forEach((x,y) -> System.out.println(x+":"+y));
        
	}
	
	public static void main(String[] args) throws IOException {
		HtmlConverterTest tmp = new HtmlConverterTest();
		tmp.testNBSP();
		
	}
	
	@Test
	final void testFontPaths() {
		String[] paths = new String[] {"/usr/share/fonts/X11/util",
				"/home/terminological/R/x86_64-pc-linux-gnu-library/3.6/showtextdb/fonts"};
		
		HtmlConverter conv = HtmlConverter.htmlConverter(RCharacterVector.with(paths), RConverter.convert(true));
		conv.fonts.stream().forEach(f -> System.out.println(f.family));
	}
}
