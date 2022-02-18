package uk.co.terminological.html2pdfr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
//import org.htmlcleaner.CleanerProperties;
//import org.htmlcleaner.HtmlCleaner;
//import org.htmlcleaner.SimpleXmlSerializer;
//import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
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
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;

import uk.co.terminological.html2pdfr.AutoFont.CSSFont;
import uk.co.terminological.rjava.RClass;
import uk.co.terminological.rjava.RConverter;
import uk.co.terminological.rjava.RDefault;
import uk.co.terminological.rjava.RFinalize;
import uk.co.terminological.rjava.RMethod;
import uk.co.terminological.rjava.types.RCharacter;
import uk.co.terminological.rjava.types.RCharacterVector;

@RClass(imports = {"extrafont"})
public class HtmlConverter {

	List<CSSFont> fonts = null;
	SVGDrawer svg = new BatikSVGDrawer();
	SVGDrawer mathMl = new MathMLDrawer();

	@RMethod
	public HtmlConverter(@RDefault(rCode = "extrafont::fonttable()$fontfile") RCharacterVector fontfiles) throws IOException {
		this(fontfiles.rPrimitive());		
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
	
//	private Document shrinkWrap(Document doc, URI htmlBaseUrl, int maxWidth, int maxHeight, int padding) {
//		// figure out dimensions on full page.
//		Document doc2 = resizeHtml(doc, maxWidth, maxHeight, padding);
//		PdfRendererBuilder builder = configuredBuilder();
//		Element shrinkWrap = doc2.body().appendElement("div").attr("style","width: "+maxWidth+"px;");
//		shrinkWrap.siblingElements().forEach(el -> el.appendTo(shrinkWrap));
//		builder.withHtmlContent(doc.outerHtml(), htmlBaseUrl == null ? null : htmlBaseUrl.toString());
//		PdfBoxRenderer renderer = builder.buildPdfRenderer();
//		renderer.layout();
//		// The root box is <html>, the first child is <body>, then shrinkWrap <div>.
//		if (renderer.getPdfDocument().getNumberOfPages() == 1) {
//			newHeight = maxHeight-(int) Math.floor(renderer.getLastContentBottom()/PDF_DOTS_PER_PIXEL);
//		}
//		Box box = renderer.getRootBox().getChild(0).getChild(0);
//		int boxWidth = (int) Math.ceil(1.0*box.getBoxDimensions().getContentWidth()/PDF_DOTS_PER_PIXEL);
//		int boxHeight = (int) Math.ceil(1.0*box.getBoxDimensions().getHeight()/PDF_DOTS_PER_PIXEL);
//		int newWidth = Math.min(boxWidth+2*padding+1,maxWidth);
//		int newHeight = Math.min(boxHeight+2*padding+1,maxHeight);
//		renderer.close();
//		// shrink image to fit rendered size (plus padding)
//		return resizeHtml(doc,newWidth,newHeight,padding); 
//	}
	
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
	
	@RMethod
	public RCharacter urlToPdf(String htmlUrl, String outFile) throws IOException {
		Scanner s = new Scanner(new URL(htmlUrl).openStream(), "UTF-8");
		String html = s.useDelimiter("\\A").next();
		s.close();
		List<String> saved = renderHtml(html, URI.create(htmlUrl), outFile, new String[] {"pdf"}, false, 0D, 0D, 0D, false, 300D);
		return RConverter.convert(saved.get(0));
	}
	
	@RMethod
	public RCharacter fileToPdf(String inFile, String outFile) throws IOException {
		Scanner s = new Scanner(new FileInputStream(new File(inFile)), "UTF-8");
		String html = s.useDelimiter("\\A").next();
		URI directory = Paths.get(inFile).getParent().toUri();
		s.close();
		List<String> saved = renderHtml(html, directory, outFile, new String[] {"pdf"}, false, 0D, 0D, 0D, false, 300D);
		return RConverter.convert(saved.get(0));
	}
	
	//N.b. can output png by specifying this in filename extension
	@RMethod
	public RCharacter stringToPdf(String html, String outFile, @RDefault(rCode = "NA_character_") RCharacter baseUri) throws IOException {
		URI directory;
		if (baseUri.isNa()) {
			directory = null;
		} else {
			directory = Paths.get(baseUri.get()).getParent().toUri(); 
		}
		List<String> saved = renderHtml(html, directory, outFile, new String[] {"pdf"}, false, 0D, 0D, 0D, false, 300D);
		return RConverter.convert(saved.get(0));
	}
	
	@RMethod
	public RCharacterVector fitIntoPage(
			String htmlFragment, 
			String outFile, 
			@RDefault(rCode = "6.25") double maxWidthInches, 
			@RDefault(rCode = "9.75") double maxHeightInches,
			@RDefault(rCode = "c('pdf','png')") String[] formats,
			@RDefault(rCode = "300") double pngDpi
	) throws IOException {
		List<String> saved = renderHtml(htmlFragment, null, outFile, formats, true, maxWidthInches, maxHeightInches, 1.0/16, true, pngDpi);
		return saved.stream().collect(RConverter.stringCollector());
	}
	
	@RMethod
	public RCharacterVector fitIntoA4(
			String htmlFragment, 
			String outFile, 
			@RDefault(rCode = "1.0") double xMarginInInches, 
			@RDefault(rCode = "1.0") double yMarginInInches
	) throws IOException {
		List<String> saved = renderHtml(htmlFragment, null, outFile, new String[] {"pdf","png"}, true, A4_INCH_WIDTH-2*xMarginInInches, A4_INCH_HEIGHT-2*yMarginInInches, 1.0/16, true, 300D);
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
	
	
	// main function
	public List<String> renderHtml(String html, URI htmlBaseUrl, String outFile, String[] formats, boolean resize,  double widthInches, double heightInches, double paddingInches, boolean shrinkWrap, double pngDotsPerInch) throws IOException {
		
		// clean up html
		Document doc = Jsoup.parse(html);
		doc.outputSettings(new OutputSettings().syntax(Syntax.xml));
		
//		TODO: 
//		com.openhtmltopdf.util.XRRuntimeException: Can't load the XML resource (using TRaX transformer). org.xml.sax.SAXParseException; lineNumber: 19; columnNumber: 55; The entity name must immediately follow the '&' in the entity reference.
//		HtmlCleaner cleaner = new HtmlCleaner();
//		CleanerProperties props = cleaner.getProperties();
//		props.setNamespacesAware(true);
//		TagNode node;
//		try {
//			node = cleaner.clean(html);
//			String xml = new SimpleXmlSerializer(props).getAsString(node);
//			
//			log.debug("Cleaning complete");
//			return dom;
//		} catch (IOException e) {
//			throw new XmlException("HtmlCleaner could not process HTML input stream",e);
//		}
		
		// System.out.println(doc.outerHtml());
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
		
		PdfRendererBuilder builder = configuredBuilder();
		builder.withHtmlContent(doc2.outerHtml(), htmlBaseUrl == null ? null : htmlBaseUrl.toString());
		
		PdfBoxRenderer pdfrender = builder.buildPdfRenderer();
		pdfrender.layout();
		pdfrender.createPDFWithoutClosing();
		
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
