package uk.co.terminological.html2pdfr;

import static uk.co.terminological.rjava.RConverter.stringCollector;
import static uk.co.terminological.rjava.RConverter.using;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
//import org.htmlcleaner.CleanerProperties;
//import org.htmlcleaner.HtmlCleaner;
//import org.htmlcleaner.SimpleXmlSerializer;
//import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;

import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.extend.SVGDrawer;
import com.openhtmltopdf.latexsupport.LaTeXDOMMutator;
import com.openhtmltopdf.mathmlsupport.MathMLDrawer;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfBoxRenderer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.render.Box;
import com.openhtmltopdf.slf4j.Slf4jLogger;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import com.openhtmltopdf.util.XRLog;

import uk.co.terminological.html2pdfr.AutoFont.CSSFont;
import uk.co.terminological.rjava.RClass;
import uk.co.terminological.rjava.RDefault;
import uk.co.terminological.rjava.RFinalize;
import uk.co.terminological.rjava.RMethod;
import uk.co.terminological.rjava.types.RCharacter;
import uk.co.terminological.rjava.types.RCharacterVector;
import uk.co.terminological.rjava.types.RLogical;
import uk.co.terminological.rjava.types.RNumeric;

@RClass(imports = {"systemfonts"},suggests= {"here","huxtable","dplyr","readr"})
public class HtmlConverter {

	protected List<CSSFont> fonts = new ArrayList<>();
	SVGDrawer svg = new BatikSVGDrawer();
	SVGDrawer mathMl = new MathMLDrawer();
	W3CDom w3cDom = new W3CDom();

	private static HtmlConverter instance = null;


	/**
	 * Create a new HtmlConverter 
	 * 
	 * for creating PDF and PNG files from HTML. In general this will be created automatically.
	 * but if you have specific fonts you want to use then you may need to pass them to this
	 * function and specify the result in the `converter` parameter of the main functions.
	 * 
	 * @param fontfiles - a character vector of font files that will be imported into the converter.
	 */
	@RMethod(examples= {
			"conv = html2pdfr::html_converter()"
	}) 
	public static HtmlConverter htmlConverter(
			@RDefault(rCode = "systemfonts::system_fonts()$path") RCharacterVector fontfiles,
			@RDefault(rCode = "FALSE") RLogical update
			) {
		if (instance == null || update.get().booleanValue()) {
			XRLog.setLoggerImpl(new Slf4jLogger());
			XRLog.setLoggingEnabled(false);
			instance = new HtmlConverter(fontfiles.rPrimitive());
		}
		return instance;
	}

	public HtmlConverter(String[] fontfiles) {
		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
		Arrays.asList(fontfiles).stream()
		.flatMap(ff -> AutoFont.fromFontPath(ff))
		.forEach(fonts::add);
	}

	@RFinalize
	public void close() throws IOException {
		svg.close();
		mathMl.close();
	}

	private static String resolvePath(String outFile) throws IOException {
		Path tmp = Paths.get(outFile);
		Files.createDirectories(tmp.getParent());
		return tmp.getParent().toRealPath().resolve(tmp.getFileName()).toString();
	}

	// An internal number, should stay the same.
	private static final int PDF_DOTS_PER_PIXEL = 20;
	private static final int PIXELS_PER_INCH = 96;

	private static final double A4_INCH_WIDTH = 8.25D; //96 DPI
	private static final double A4_INCH_HEIGHT = 11.75;

	//	private static final int A4_PIXEL_WIDTH = 793; //96 DPI
	//	private static final int A4_PIXEL_HEIGHT = 1122;
	//	
	//	private static final float PDF_POINT_WIDTH_A4 = 595f; // 72 DPI (pdf pt per inch)
	//	private static final float PDF_POINT_HEIGHT_A4 = 841.88f;

	private Document resizeHtml(Document doc, int width, int height, int padding) {
		// padding implemented as margin on body, with page size as width and height.
		Document doc2 = doc.clone();
		Elements styles = doc2.head().getElementsByTag("style");
		Element style;
		if (styles.isEmpty()) {
			style = doc2.head().appendElement("style");
		} else {
			style = styles.first();
		}
		String styleSpec = style.text();
		styleSpec = styleSpec.replaceAll("@page\\s+\\{[^}]+\\}", "").replaceAll("body\\s+\\{[^}]+\\}", "");
		String newStyleSpec = "@page { size: "+width+"px "+height+"px; margin: "+padding+"px; } body { margin: 0px; } "+styleSpec;
		style.text(newStyleSpec);

		return doc2;
	}

	// Shrinks HTML content so that it is smaller that maxWidth, and shorter than (or equal to) maxWidth by paging when it gets too long.
	// This will basically shrink to content unless that gets bigger than outer box.
	// The result is guaranteed to be no larger than maxWidth with no single page longer than maxHeight.
	// Content will overflow to fill multiple pages.
	private Document shrinkWrap(Document doc, URI htmlBaseUrl, int maxWidth, int maxHeight, int padding) throws IOException {
		// figure out dimensions on full page.
		Document doc2 = resizeHtml(doc, maxWidth, maxHeight, padding);
		PdfRendererBuilder builder = configuredBuilder();
		Element shrinkWrap = doc2.body().appendElement("div").attr("style","display: inline-block;");
		shrinkWrap.siblingElements().forEach(el -> el.appendTo(shrinkWrap));
		doc2.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		doc2.outputSettings().escapeMode(EscapeMode.xhtml);
		String tmp = doc2.toString(); 
		builder.withHtmlContent(tmp, htmlBaseUrl == null ? null : htmlBaseUrl.toString());
		PdfBoxRenderer renderer = builder.buildPdfRenderer();
		renderer.layout();
		// The root box is <html>, the first child is <body>, then shrinkWrap <div>.
		Box box = renderer.getRootBox().getChild(0).getChild(0);
		int boxWidth = (int) Math.ceil(1.0*box.getBoxDimensions().getContentWidth()/PDF_DOTS_PER_PIXEL);
		int boxHeight = (int) Math.ceil(1.0*box.getHeight()/PDF_DOTS_PER_PIXEL);
		int newWidth = Math.min(boxWidth+2*padding+1,maxWidth);
		int newHeight = Math.min(boxHeight+2*padding+1,maxHeight);
		// doing this to try and make sure resources are closed.
		renderer.createPDFWithoutClosing();
		PDDocument pdfdoc = renderer.getPdfDocument();
		pdfdoc.close();
		renderer.close();
		// shrink image to fit rendered size (plus padding)
		return resizeHtml(doc,newWidth,newHeight,padding); 
	}

	// for testing
	public List<String> renderHtmlToFitA4(String html, String outFile, double marginInInches) throws IOException {
		return renderHtml(html, null, outFile, new String[] {"pdf","png"}, true, A4_INCH_WIDTH-2*marginInInches, A4_INCH_HEIGHT-2*marginInInches, 1.0/16, true, 300D);
	}

	/**
	 * Convert HTML document from a URL to a PDF document. 
	 * 
	 * The URL is assumed to be a complete document. 
	 * The resulting PDF size will be controlled by page media directives within the HTML, 
	 * unless explicitly given here in `maxWidthInches` and `maxHeightInches`. 
	 * If the `cssSelector` parameter is given the HTML fragment at that selector will be used.
	 * In this case it is will be resized to fit within the given dimensions and shrink wrapped
	 * so that the content is smaller. If no dimensions are present this will default to A4.
	 *  
	 * @param htmlUrl the URL 
	 * @param outFile the full path of the output file
	 * @param cssSelector the part of the page you want to convert to PDF.	 
	 * @param xMarginInches page width margins
	 * @param yMarginInches page height margins
	 * @param maxWidthInches what is the maximum allowable width?
	 * @param maxHeightInches what is the maximum allowable height? (if the content is larger than this then it will overflow to another page)
	 * @param formats If the outFile does not specify a file extension then you can do so here as "png" or "pdf" or both.
	 * @param pngDpi the dots per inch for png outputs if requested
	 * @param converter (optional) a configured HTML converter, only needed if manually specifying fonts.
	 * @return the filename(s) written to (with extension '.pdf' or '.png' if outFile did not have an extension).
	 * @throws IOException if the output file cannot be written
	 */
	@RMethod(tests = {
			"url_to_pdf('https://cran.r-project.org/banner.shtml')"
	})
	public static RCharacterVector urlToPdf(
			RCharacter htmlUrl, 
			@RDefault(rCode = "tempfile('html2pdfr_')") RCharacter outFile,
			@RDefault(rCode = "NA_character_") RCharacter cssSelector,
			@RDefault(rCode = "NA_real_") RNumeric xMarginInches, 
			@RDefault(rCode = "NA_real_") RNumeric yMarginInches,
			@RDefault(rCode = "NA_real_") RNumeric maxWidthInches, 
			@RDefault(rCode = "NA_real_") RNumeric maxHeightInches,
			@RDefault(rCode = "c('pdf')") RCharacterVector formats,
			@RDefault(rCode = "300") RNumeric pngDpi,
			@RDefault(rCode = "html2pdfr::html_converter()") HtmlConverter converter
			) throws IOException {
		Scanner s = new Scanner(new URL(htmlUrl.get()).openStream(), "UTF-8");
		String html = s.useDelimiter("\\A").next();
		s.close();
		URI uri = URI.create(htmlUrl.get());
		Double x = maxWidthInches.opt().map(w -> w-2*xMarginInches.opt().orElse(0D)).orElse(null);
		Double y = maxHeightInches.opt().map(w -> w-2*yMarginInches.opt().orElse(0D)).orElse(null);
		boolean resize = !(maxWidthInches.isNa() || maxHeightInches.isNa());
		boolean shrink = !cssSelector.isNa();
		List<String> saved = converter.renderHtmlWithSelector(html, uri, 
				outFile.get(), formats.rPrimitive(), 
				resize, x, y, 
				1.0/16, 
				shrink, 
				pngDpi.opt().orElse(300D),
				cssSelector.get());
		return using(stringCollector()).convert(saved);
	}


	/**
	 * Convert HTML document from a local file to a PDF document. 
	 * 
	 * The HTML in `inFile` is assumed to be a complete document. Relative references are resolved
	 * with reference to the HTML file on the file system, so correctly located images etc whould be 
	 * picked up without requiring a server.
	 * The resulting PDF size will be controlled by page media directives within the HTML, 
	 * unless explicitly given here in `maxWidthInches` and `maxHeightInches`. 
	 * If the `cssSelector` parameter is given the HTML fragment at that selector will be used.
	 * In this case it is will be resized to fit within the given dimensions and shrink wrapped
	 * so that the content is smaller. If no dimensions are present this will default to A4.
	 *  
	 * @param inFile the full path the the HTML file 
	 * @param outFile the full path of the output file
	 * @param cssSelector the part of the page you want to convert to PDF.	 
	 * @param xMarginInches page width margins
	 * @param yMarginInches page height margins
	 * @param maxWidthInches what is the maximum allowable width?
	 * @param maxHeightInches what is the maximum allowable height? (if the content is larger than this then it will overflow to another page)
	 * @param formats If the outFile does not specify a file extension then you can do so here as "png" or "pdf" or both.
	 * @param pngDpi the dots per inch for png outputs if requested
	 * @param converter (optional) a configured HTML converter, only needed if manually specifying fonts.
	 * @return the filename written to (with extension '.pdf' or '.png' if outFile did not have an extension).
	 * @throws IOException if the output file cannot be written
	 */	 
	@RMethod(tests = {
			"dest = tempfile(fileext='.html')",
			"download.file('https://cran.r-project.org/banner.shtml', destfile = dest)",
			"file_to_pdf(dest)"
	})
	public static RCharacterVector fileToPdf(
			RCharacter inFile, 
			@RDefault(rCode = "tempfile('html2pdfr_')") RCharacter outFile,
			@RDefault(rCode = "NA_character_") RCharacter cssSelector,
			@RDefault(rCode = "NA_real_") RNumeric xMarginInches, 
			@RDefault(rCode = "NA_real_") RNumeric yMarginInches,
			@RDefault(rCode = "NA_real_") RNumeric maxWidthInches, 
			@RDefault(rCode = "NA_real_") RNumeric maxHeightInches,
			@RDefault(rCode = "c('pdf')") RCharacterVector formats,
			@RDefault(rCode = "300") RNumeric pngDpi,
			@RDefault(rCode = "html2pdfr::html_converter()") HtmlConverter converter
			) throws IOException {
		Scanner s = new Scanner(new FileInputStream(new File(inFile.get())), "UTF-8");
		String html = s.useDelimiter("\\A").next();
		URI uri = Paths.get(inFile.get()).getParent().toUri();
		s.close();
		Double x = maxWidthInches.opt().map(w -> w-2*xMarginInches.opt().orElse(0D)).orElse(null);
		Double y = maxHeightInches.opt().map(w -> w-2*yMarginInches.opt().orElse(0D)).orElse(null);
		boolean resize = !(maxWidthInches.isNa() || maxHeightInches.isNa());
		boolean shrink = !cssSelector.isNa();
		List<String> saved = converter.renderHtmlWithSelector(html, uri, 
				outFile.get(), formats.rPrimitive(), 
				resize, x, y, 
				1.0/16, 
				shrink, 
				pngDpi.opt().orElse(300D),
				cssSelector.get());
		return using(stringCollector()).convert(saved);
	}

	/**
	 * Convert HTML document from a string to a PDF document. 
	 * 
	 * The HTML in `html` is assumed to be a complete document. Relative references are resolved
	 * with reference to `baseUri` if it is given (which could be a `file://` URI).
	 * The resulting PDF size will be controlled by page media directives within the HTML, 
	 * unless explicitly given here in `maxWidthInches` and `maxHeightInches`. 
	 * If the `cssSelector` parameter is given the HTML fragment at that selector will be used.
	 * In this case it is will be resized to fit within the given dimensions and shrink wrapped
	 * so that the content is smaller. If no dimensions are present this will default to A4.
	 * 
	 * @param html the html document as a string 
	 * @param outFile the full path of the output file
	 * @param baseUri the URI from which to interpret relative links in the html content.
	 * @param cssSelector the part of the page you want to convert to PDF.	 
	 * @param xMarginInches page width margins
	 * @param yMarginInches page height margins
	 * @param maxWidthInches what is the maximum allowable width?	 
	 * @param maxHeightInches what is the maximum allowable height? (if the content is larger than this then it will overflow to another page)
	 * @param formats If the outFile does not specify a file extension then you can do so here as "png" or "pdf" or both.
	 * @param pngDpi the dots per inch for png outputs if requested
	 * @param converter (optional) a configured HTML converter, only needed if manually specifying fonts.
	 * @return the filename written to (with extension '.pdf' or '.png' if outFile did not have an extension).
	 * @throws IOException if the output file cannot be written
	 */
	@RMethod(tests = {
			"library(readr)", 
			"html = read_file('https://fred-wang.github.io/MathFonts/mozilla_mathml_test/')",
			"html_document_to_pdf(html, baseUri = 'https://fred-wang.github.io/MathFonts/mozilla_mathml_test/')"
	})
	public static RCharacterVector htmlDocumentToPdf(
			RCharacter html, 
			@RDefault(rCode = "tempfile('html2pdfr_')") RCharacter outFile, 
			@RDefault(rCode = "NA_character_") RCharacter baseUri,
			@RDefault(rCode = "NA_character_") RCharacter cssSelector,
			@RDefault(rCode = "NA_real_") RNumeric xMarginInches, 
			@RDefault(rCode = "NA_real_") RNumeric yMarginInches,
			@RDefault(rCode = "NA_real_") RNumeric maxWidthInches, 
			@RDefault(rCode = "NA_real_") RNumeric maxHeightInches,
			@RDefault(rCode = "c('pdf')") RCharacterVector formats,
			@RDefault(rCode = "300") RNumeric pngDpi,
			@RDefault(rCode = "html2pdfr::html_converter()") HtmlConverter converter
			) throws IOException {
		URI uri;
		if (baseUri.isNa()) {
			uri = null;
		} else {
			uri = URI.create(baseUri.get()); 
		}
		Double x = maxWidthInches.opt().map(w -> w-2*xMarginInches.opt().orElse(0D)).orElse(null);
		Double y = maxHeightInches.opt().map(w -> w-2*yMarginInches.opt().orElse(0D)).orElse(null);
		boolean resize = !(maxWidthInches.isNa() || maxHeightInches.isNa());
		boolean shrink = !cssSelector.isNa();
		List<String> saved = converter.renderHtmlWithSelector(html.get(), uri, 
				outFile.get(), formats.rPrimitive(), 
				resize, x, y, 
				1.0/16, 
				shrink, 
				pngDpi.opt().orElse(300D),
				cssSelector.get());
		return using(stringCollector()).convert(saved);
	}

	/**
	 * Render HTML fragment from a string to a PDF image.
	 * 
	 * This is the simple rendering function that will output a PDF file (potentially many pages) and a set of PNG files from HTML content. 
	 * This is primarily used to render HTML content (e.g. a table) that is being included in a larger document.
	 * In this case the HTML fragment will not specify page dimensions which need to be provided (defaults to A4 size with 1 inch margins). 
	 * The result can be embedded into an existing page using latex's includegraphics directive exactly the same way as a graphical figure might be used. 
	 * The sizing of the output will always be smaller than the dimensions of a page, but will shrink to fit the content.
	 * 
	 * @param htmlFragment a HTML fragment, e.g. usually the table element, but may be the whole page.
	 * @param outFile the full path with or without extension (if no extension specified then `formats` parameter will apply)
	 * @param xMarginInches page width margins
	 * @param yMarginInches page height margins
	 * @param maxWidthInches what is the maximum allowable width? (default is A4)
	 * @param maxHeightInches what is the maximum allowable height? (if the content is larger than this then it will overflow to another page)
	 * @param formats If the outFile does not specify a file extension then you can do so here as "png" or "pdf" or both.
	 * @param pngDpi the dots per inch for png outputs if requested.
	 * @param converter (optional) a configured HTML converter, only needed if manually specifying fonts.
	 * @return the filename(s) written to (with extension '.pdf' or '.png' if outFile did not have an extension).
	 * @throws IOException if the output file cannot be written
	 */
	@RMethod(tests = {
			"library(dplyr)", 
			"html = iris %>% group_by(Species) %>% ",
			"  summarise(across(everything(), mean)) %>% ",
			"  huxtable::as_hux() %>% ",
			"  huxtable::theme_article() %>% ",
			"  huxtable::to_html()",
			"html_fragment_to_pdf(html)"
	})
	public static RCharacterVector htmlFragmentToPdf(
			RCharacter htmlFragment, 
			@RDefault(rCode = "tempfile('html2pdfr_')") RCharacter outFile, 
			@RDefault(rCode = "1.0") RNumeric xMarginInches, 
			@RDefault(rCode = "1.0") RNumeric yMarginInches,
			@RDefault(rCode = "8.27") RNumeric maxWidthInches, 
			@RDefault(rCode = "11.69") RNumeric maxHeightInches,
			@RDefault(rCode = "c('pdf','png')") RCharacterVector formats,
			@RDefault(rCode = "300") RNumeric pngDpi,
			@RDefault(rCode = "html2pdfr::html_converter()") HtmlConverter converter
			) throws IOException {
		Double x = maxWidthInches.opt().map(w -> w-2*xMarginInches.opt().orElse(0D)).orElse(null);
		Double y = maxHeightInches.opt().map(w -> w-2*yMarginInches.opt().orElse(0D)).orElse(null);
		List<String> saved = converter.renderHtml(
				htmlFragment.get(), null, 
				outFile.get(), formats.rPrimitive(), 
				true, x, y, 
				1.0/16, true, pngDpi.opt().orElse(300D));
		return using(stringCollector()).convert(saved);
	}

	private PdfRendererBuilder configuredBuilder() {
		PdfRendererBuilder builder = new PdfRendererBuilder();
		AutoFont.toBuilder(builder, fonts);
		builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
		builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
		builder.defaultTextDirection(BaseRendererBuilder.TextDirection.LTR);
		builder.useSVGDrawer(svg);
		builder.useMathMLDrawer(mathMl);
		builder.addDOMMutator(LaTeXDOMMutator.INSTANCE);
		return builder;
	}

	public List<String> renderHtml(
			String html, URI htmlBaseUrl, 
			String outFile, String[] formats, boolean resize,  
			Double widthInches, Double heightInches, Double paddingInches, 
			boolean shrinkWrap, double pngDotsPerInch
			) throws IOException {
		return renderHtmlWithSelector(html, htmlBaseUrl, outFile, formats, 
				resize, widthInches, heightInches, paddingInches, shrinkWrap, 
				pngDotsPerInch, null);
	}
	// main function 
	public List<String> renderHtmlWithSelector(
			String html, URI htmlBaseUrl, 
			String outFile, String[] formats, boolean resize,  
			Double widthInches, Double heightInches, Double paddingInches, 
			boolean shrinkWrap, double pngDotsPerInch, String cssSelector
			) throws IOException {

		// resolve symbolic links.
		outFile = resolvePath(outFile);

		// clean up html
		Document doc = Jsoup.parse(html);
		doc.outputSettings(new OutputSettings().syntax(Syntax.xml));
		if (cssSelector != null) {
			Elements els = doc.select(cssSelector);
			Document tmp = Document.createShell(htmlBaseUrl.toString());
			doc.head().children().forEach(el -> el.clone().appendTo(tmp.head()));
			els.forEach(el -> el.clone().appendTo(tmp.body()));
			doc = tmp;
		}

		List<String> saved = new ArrayList<String>();


		Document doc2;
		if (resize) {
			// resize page to content 
			if (shrinkWrap) {
				doc2 = shrinkWrap(doc, htmlBaseUrl, 
						(int) (widthInches * PIXELS_PER_INCH), 
						(int) (heightInches * PIXELS_PER_INCH), 
						(int) (paddingInches*PIXELS_PER_INCH));
			} else {
				doc2 = resizeHtml(doc, (int) (widthInches * PIXELS_PER_INCH), 
						(int) (heightInches * PIXELS_PER_INCH), 
						(int) (paddingInches*PIXELS_PER_INCH));
			}
		} else {
			// assume size supplied as @page css (or default to A4) 
			doc2 = doc;
		}
		doc2.outputSettings(new OutputSettings().syntax(Syntax.xml));
		doc2.outputSettings().escapeMode(EscapeMode.xhtml);

		// System.out.println(doc2.outerHtml());

		// decide on formats
		String extn = FilenameUtils.getExtension(outFile);
		if(extn != "") formats = new String[] {extn};
		outFile = FilenameUtils.removeExtension(outFile);

		PdfRendererBuilder builder = configuredBuilder();

		builder.withW3cDocument(w3cDom.fromJsoup(doc2), htmlBaseUrl == null ? null : htmlBaseUrl.toString());
		// builder.withHtmlContent(doc2.outerHtml(), htmlBaseUrl == null ? null : htmlBaseUrl.toString());

		PdfBoxRenderer pdfrender = builder.buildPdfRenderer();
		//pdfrender.getRootBox(). get box model layout as another function
		pdfrender.layout();
		try {
			pdfrender.createPDFWithoutClosing();
		} catch (Exception e) {
			throw new IOException("HTML renderer did not complete normally. This usually means the HTML contains unsupported features, (e.g. some types of form fields.)",e);
		}



		PDDocument pdfdoc = pdfrender.getPdfDocument();
		boolean write = false;

		if(Arrays.asList(formats).contains("pdf")) {

			File pdfFile = new File(outFile+".pdf"); 
			pdfdoc.save(pdfFile);
			write = true;
			saved.add(pdfFile.toString());
		} else {
			// Dummy save to trigger allow png save to execute correctly
			pdfdoc.save(new NullOutputStream());
		}


		if(Arrays.asList(formats).contains("png")) {
			if (pdfdoc.getNumberOfPages() == 1) {

				BufferedImage buffGraph = new PDFRenderer(pdfdoc).renderImage(0,((float) pngDotsPerInch)/72.0F);
				File pngFile = new File(outFile+".png");
				saveImage(buffGraph, pngFile, pngDotsPerInch);
				saved.add(pngFile.toString());

			} else {
				for (int i=0; i<pdfdoc.getNumberOfPages(); i++) {

					BufferedImage buffGraph = new PDFRenderer(pdfdoc).renderImage(i,((float) pngDotsPerInch)/72.0F);
					File pngFile = new File(outFile+"_"+(i+1)+".png");
					saveImage(buffGraph, pngFile, pngDotsPerInch);
					saved.add(pngFile.toString());
				}
			}
			write = true;
		} 

		pdfdoc.close();
		pdfrender.close();

		if (!write) {
			throw new IOException("Formats not supported: "+Stream.of(formats).collect(Collectors.joining(", ")));
		}


		return saved;
	}

	private  static final float INCH_2_CM = 2.54F;

	private static void saveImage(BufferedImage image, File output, double dotsPerInch) throws IOException {
		output.delete();

		final String formatName = "png";

		for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
			ImageWriter writer = iw.next();
			ImageWriteParam writeParam = writer.getDefaultWriteParam();
			ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
			IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
			if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
				continue;
			}

			setDPI(dotsPerInch, metadata);

			final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
			try {
				writer.setOutput(stream);
				writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
			} finally {
				stream.close();
			}
			break;
		}
	}

	private static void setDPI(double dotsPerInch, IIOMetadata metadata) throws IIOInvalidTreeException {

		// for PNG, it's dots per millimeter
		double dotsPerMilli = 1.0 * dotsPerInch / 10 / INCH_2_CM;

		IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
		horiz.setAttribute("value", Double.toString(dotsPerMilli));

		IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
		vert.setAttribute("value", Double.toString(dotsPerMilli));

		IIOMetadataNode dim = new IIOMetadataNode("Dimension");
		dim.appendChild(horiz);
		dim.appendChild(vert);

		IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
		root.appendChild(dim);

		metadata.mergeTree("javax_imageio_1.0", root);
	}

}
