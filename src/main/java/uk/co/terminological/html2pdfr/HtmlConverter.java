package uk.co.terminological.html2pdfr;

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
import java.util.logging.Level;
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
import uk.co.terminological.rjava.RConverter;
import uk.co.terminological.rjava.RDefault;
import uk.co.terminological.rjava.RFinalize;
import uk.co.terminological.rjava.RMethod;
import uk.co.terminological.rjava.types.RCharacter;
import uk.co.terminological.rjava.types.RCharacterVector;

@RClass(imports = {"extrafont"},suggests= {"here"})
public class HtmlConverter {

	List<CSSFont> fonts = null;
	SVGDrawer svg = new BatikSVGDrawer();
	SVGDrawer mathMl = new MathMLDrawer();
	W3CDom w3cDom = new W3CDom();

	/**
	 * Create a new HtmlConverter for creating PDF and PNG files from HTML.
	 * 
	 * @param fontfiles - a character vector of font files that will be imported into the converter.
	 * @throws IOException  -If the font files cannot be opened
	 */
	@RMethod
	public HtmlConverter(@RDefault(rCode = "extrafont::fonttable()$fontfile") RCharacterVector fontfiles) throws IOException {
		this(fontfiles.rPrimitive());
		XRLog.setLoggerImpl(new Slf4jLogger());
		XRLog.setLoggingEnabled(false);
		XRLog.setLevel(XRLog.CSS_PARSE, Level.SEVERE);
	}
	
	public HtmlConverter(String[] fontfiles) throws IOException {

		Path systemFontDirectory = Paths.get(System.getProperty("java.home"), "lib","fonts");
				
		fonts = AutoFont.findFontsInDirectory(systemFontDirectory);

		Arrays.asList(fontfiles).stream()
			.flatMap(ff -> AutoFont.fromFontFile(ff).map(o -> Stream.of(o)).orElse(Stream.empty()))
			.forEach(fonts::add);

		// Path fontDirectory = Paths.get("/usr/share/fonts/truetype");
		// fonts.addAll(AutoFont.findFontsInDirectory(fontDirectory));
		// fonts = AutoFont.defineFallbacks(fonts);

	}
	
	@RFinalize
	public void close() throws IOException {
		svg.close();
		mathMl.close();
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
	// This will basically shirink to content unless that gets bigger than outer box.
	// The result is guaranteed to be no larger than maxWidth and no one page longer than maxHeight.
	// Content will overflow to fill multiple pages.
	private Document shrinkWrap(Document doc, URI htmlBaseUrl, int maxWidth, int maxHeight, int padding) throws IOException {
		// figure out dimensions on full page.
		Document doc2 = resizeHtml(doc, maxWidth, maxHeight, padding);
		PdfRendererBuilder builder = configuredBuilder();
		Element shrinkWrap = doc2.body().appendElement("div").attr("style","display: inline-block;");
		shrinkWrap.siblingElements().forEach(el -> el.appendTo(shrinkWrap));
		builder.withHtmlContent(doc2.outerHtml(), htmlBaseUrl == null ? null : htmlBaseUrl.toString());
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
	 * Convert HTML from a URL to a PDF file. PDF size will be controlled by page media directives within the html.
	 * @param htmlUrl the URL 
	 * @param outFile the full path of the output file 
	 * @return the filename written to (with extension '.pdf' if outFile did not have an extension).
	 * @throws IOException if the output file cannot be written
	 */
	@RMethod
	public RCharacter urlToPdf(String htmlUrl, String outFile) throws IOException {
		Scanner s = new Scanner(new URL(htmlUrl).openStream(), "UTF-8");
		String html = s.useDelimiter("\\A").next();
		s.close();
		List<String> saved = renderHtml(html, URI.create(htmlUrl), outFile, new String[] {"pdf"}, false, 0D, 0D, 0D, false, 300D);
		return RConverter.convert(saved.get(0));
	}
	
	/**
	 * Convert HTML from a URL to a PDF file. PDF size will be controlled by page media directives within the html.
	 * @param htmlUrl the URL 
	 * @param outFile the full path of the output file
	 * @param cssSelector the part of the page you want to convert to PDF. 
	 * @return the filename written to (with extension '.pdf' if outFile did not have an extension).
	 * @throws IOException if the output file cannot be written
	 */
	@RMethod
	public RCharacter urlComponentToPdf(String htmlUrl, String outFile, String cssSelector) throws IOException {
		Scanner s = new Scanner(new URL(htmlUrl).openStream(), "UTF-8");
		String html = s.useDelimiter("\\A").next();
		s.close();
		List<String> saved = renderHtmlWithSelector(html, URI.create(htmlUrl), outFile, new String[] {"pdf"}, false, 0D, 0D, 0D, false, 300D, cssSelector);
		return RConverter.convert(saved.get(0));
	}
	
	/**
	 * Convert HTML from a local file to a PDF file. PDF size will be controlled by page media directives within the html.
	 * @param inFile the full path to an input HTML file
	 * @param outFile the full path to the output pdf file. (N.B. this function can also output PNG if specified in the filename extension)
	 * @return the filename written to (with extension '.pdf' if outFile did not have an extension).
	 * @throws IOException if the output file cannot be written
	 */
	@RMethod
	public RCharacter fileToPdf(String inFile, String outFile) throws IOException {
		Scanner s = new Scanner(new FileInputStream(new File(inFile)), "UTF-8");
		String html = s.useDelimiter("\\A").next();
		URI directory = Paths.get(inFile).getParent().toUri();
		s.close();
		List<String> saved = renderHtml(html, directory, outFile, new String[] {"pdf"}, false, 0D, 0D, 0D, false, 300D);
		return RConverter.convert(saved.get(0));
	}
	
	//
	/**
	 * @param html the HTML string
	 * @param outFile the full path to the output pdf file (N.B. this function can also output PNG if specified in the filename extension)
	 * @param baseUri optionally the base URI of the HTML string for resolving relative URLs in the HTML (e.g. CSS files).
	 * @return the filename written to (with extension '.pdf' if outFile did not have an extension).
	 * @throws IOException if the output file cannot be written
	 */
	@RMethod
	public RCharacter stringToPdf(String html, String outFile, @RDefault(rCode = "NA_character_") RCharacter baseUri) throws IOException {
		URI directory;
		if (baseUri.isNa()) {
			directory = null;
		} else {
			directory = URI.create(baseUri.get()); 
		}
		List<String> saved = renderHtml(html, directory, outFile, new String[] {"pdf"}, false, 0D, 0D, 0D, false, 300D);
		return RConverter.convert(saved.get(0));
	}
	
	/**
	 * Render HTML string to fit into a page,
	 * 
	 * @param htmlFragment a HTML fragment, e.g. the table element. It is usually expected there will not be any page media directives in the HTML 
	 * @param outFile the full path to the output pdf file (N.B. this function can also output PNG if specified in the filename extension)
	 * @param maxWidthInches what is the maximum allowable width?
	 * @param maxHeightInches what is the maximium allowable height? (if the content is larger than this then it will overflow to another page)
	 * @param formats If the outFile does not specify a file extension then you can do so here as "png" or "pdf" or both.
	 * @param pngDpi if outputting a PNG the dpi will determine the dimensions of the image.
	 * @return the filename(s) written to (with extension '.pdf' and '.png' if outFile did not have an extension).
	 * @throws IOException if the output file cannot be written
	 */
	@RMethod
	public RCharacterVector fitIntoPage(
			String htmlFragment, 
			String outFile, 
			@RDefault(rCode = "6.25") double maxWidthInches, 
			@RDefault(rCode = "9.75") double maxHeightInches,
			@RDefault(rCode = "c('pdf','png')") RCharacterVector formats,
			@RDefault(rCode = "300") double pngDpi
	) throws IOException {
		List<String> saved = renderHtml(htmlFragment, null, outFile, formats.rPrimitive(), true, maxWidthInches, maxHeightInches, 1.0/16, true, pngDpi);
		return saved.stream().collect(RConverter.stringCollector());
	}
	
	
	/**
	 * Render HTML string to fit into an A4 page,
	 * 
	 * @param htmlFragment a HTML fragment, e.g. the table element. It is usually expected there will not be any page media directives in the HTML 
	 * @param outFile the full path to the output pdf file (N.B. this function can also output PNG if specified in the filename extension)
	 * @param xMarginInInches page margins
	 * @param yMarginInInches page margins
	 * @param formats If the outFile does not specify a file extension then you can do so here as "png" or "pdf" or both.
	 * @return the filename(s) written to (with extension '.pdf' and '.png' if outFile did not have an extension).
	 * @throws IOException if the output file cannot be written
	 */
	@RMethod
	public RCharacterVector fitIntoA4(
			String htmlFragment, 
			String outFile, 
			@RDefault(rCode = "1.0") double xMarginInInches, 
			@RDefault(rCode = "1.0") double yMarginInInches,
			@RDefault(rCode = "c('pdf','png')") RCharacterVector formats
	) throws IOException {
		List<String> saved = renderHtml(htmlFragment, null, outFile, formats.rPrimitive(), true, A4_INCH_WIDTH-2*xMarginInInches, A4_INCH_HEIGHT-2*yMarginInInches, 1.0/16, true, 300D);
		return saved.stream().collect(RConverter.stringCollector());
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
	
	public List<String> renderHtml(String html, URI htmlBaseUrl, String outFile, String[] formats, boolean resize,  double widthInches, double heightInches, double paddingInches, boolean shrinkWrap, double pngDotsPerInch) throws IOException {
		return renderHtmlWithSelector(html, htmlBaseUrl, outFile, formats, resize, widthInches, heightInches, paddingInches, shrinkWrap, pngDotsPerInch, null);
	}
	// main function 
	public List<String> renderHtmlWithSelector(String html, URI htmlBaseUrl, String outFile, String[] formats, boolean resize,  double widthInches, double heightInches, double paddingInches, boolean shrinkWrap, double pngDotsPerInch, String cssSelector) throws IOException {
		
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
				doc2 = shrinkWrap(doc, htmlBaseUrl, (int) (widthInches * PIXELS_PER_INCH), (int) (heightInches * PIXELS_PER_INCH), (int) (paddingInches*PIXELS_PER_INCH));
			} else {
				doc2 = resizeHtml(doc, (int) (widthInches * PIXELS_PER_INCH), (int) (heightInches * PIXELS_PER_INCH), (int) (paddingInches*PIXELS_PER_INCH));
			}
		} else {
			// assume size supplied as @page css (or default to A4) 
			doc2 = doc;
		}
		doc2.outputSettings(new OutputSettings().syntax(Syntax.xml));
		
		// System.out.println(doc2.outerHtml());
		
		// decide on formats
		String extn = FilenameUtils.getExtension(outFile);
		if(extn != "") formats = new String[] {extn};
		outFile = FilenameUtils.removeExtension(outFile);
		
		Files.createDirectories(Paths.get(outFile).getParent());
		
		PdfRendererBuilder builder = configuredBuilder();
		
		builder.withW3cDocument(w3cDom.fromJsoup(doc2), htmlBaseUrl == null ? null : htmlBaseUrl.toString());
		// builder.withHtmlContent(doc2.outerHtml(), htmlBaseUrl == null ? null : htmlBaseUrl.toString());
		
		PdfBoxRenderer pdfrender = builder.buildPdfRenderer();
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
