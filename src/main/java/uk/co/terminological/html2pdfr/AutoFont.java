package uk.co.terminological.html2pdfr;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;

/**
 * A tool for listing all the fonts in a directory.
 */
public class AutoFont {

	static Logger log = LoggerFactory.getLogger(AutoFont.class);
	
    private AutoFont() { }

    
   
    
//    static String[] SANS = {"Arial","Verdana","Tahoma","Trebuchet MS","Impact","Lucida Sans","SansSerif.plain"}; 
//    static String[] SERIF = {"Times New Roman","Didot","Georgia","American Typewriter","Lucida Bright","Serif.plain"};
//    static String[] MONO = {"Andale Mono","Courier","Lucida Console","Monaco","Lucida Sans Typewriter","Monospaced.plain"};
//    static String[] CURSIVE = {"Bradley Hand","Brush Script MT","Comic Sans MS","Lucida Sans"};
//    static String[] FANTASY = {"Luminari","Lucida Sans"};
//    
//    public static List<CSSFont> defineFallbacks(List<CSSFont> fonts) {
//    	Map<String,CSSFont> tmp = new HashMap<>();
//    	fonts.stream().filter(f->f.weight==400).forEach(o -> tmp.putIfAbsent(o.family, o));
//    	
//    	Arrays.asList(SANS).stream().map(f -> tmp.get(f)).filter(o -> o != null)
//    		.findFirst().map(cs -> new CSSFont(cs.path,"sans-serif",cs.weight,cs.style))
//    		.ifPresent(fonts::add);
//    	
//    	Arrays.asList(SERIF).stream().map(f -> tmp.get(f)).filter(o -> o != null)
//			.findFirst().map(cs -> new CSSFont(cs.path,"serif",cs.weight,cs.style))
//			.ifPresent(fonts::add);
//    	
//    	Arrays.asList(MONO).stream().map(f -> tmp.get(f)).filter(o -> o != null)
//			.findFirst().map(cs -> new CSSFont(cs.path,"monospace",cs.weight,cs.style))
//			.ifPresent(fonts::add);
//    	
//    	Arrays.asList(CURSIVE).stream().map(f -> tmp.get(f)).filter(o -> o != null)
//			.findFirst().map(cs -> new CSSFont(cs.path,"cursive",cs.weight,cs.style))
//			.ifPresent(fonts::add);
//    	
//    	Arrays.asList(FANTASY).stream().map(f -> tmp.get(f)).filter(o -> o != null)
//			.findFirst().map(cs -> new CSSFont(cs.path,"fantasy",cs.weight,cs.style))
//			.ifPresent(fonts::add);
//    	
//    	return fonts;
//    }
    
    /**
     * Returns a list of fonts in a given directory.
     * NOTE: Should not be used repeatedly as each font found is parsed to get the family name.
     * @param directory the directory to browse
     * @param validFileExtensions list of file extensions that are fonts - usually Collections.singletonList("ttf")
     * @param recurse whether to look in sub-directories recursively
     * @param followLinks whether to follow symbolic links in the file system
     * @return a list of fonts.
     * @throws IOException - If any files cannot be read.
     */
    public static List<CSSFont> findFontsInDirectory(
        Path directory, List<String> validFileExtensions, boolean recurse, boolean followLinks) throws IOException {

        FontFileProcessor processor = new FontFileProcessor(validFileExtensions);

        int maxDepth = recurse ? Integer.MAX_VALUE : 1;
        Set<FileVisitOption> options = followLinks ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet.noneOf(FileVisitOption.class);

        Files.walkFileTree(directory, options, maxDepth, processor);

        return processor.getFontsAdded();
    }

    /**
     * Returns a list of fonts in a given directory. Recursively searches directory and
     * sub-directories for .ttf files. Follows symbolic links.
     * NOTE: Should not be used repeatedly as each font found is parsed to get the family name.
     * @param directory the directory to browse
     * @return a list of fonts.
     * @throws IOException - If any files cannot be read.
     */
    public static List<CSSFont> findFontsInDirectory(Path directory) throws IOException {
        return findFontsInDirectory(directory, Collections.singletonList("ttf"), true, true);
    }

//    private static String toCSSEscapedFontFamily(List<CSSFont> fontsList) {
//        return fontsList.stream()
//           .map(fnt -> '\'' + fnt.familyCssEscaped() + '\'')
//           .distinct()
//           .collect(Collectors.joining(", "));
//    }

    protected static void toBuilder(BaseRendererBuilder<?,?> builder, List<CSSFont> fonts) {
        for (CSSFont font : fonts) {
            builder.useFont(font.path.toFile(), font.family, font.weight, font.style, true);
        }
    }
    
    public static Optional<CSSFont> fromFontFile(String ttfFile) {
    	try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, Paths.get(ttfFile).toFile());
            
            String family = f.getFamily();
            // Short of parsing the font ourselves there doesn't seem to be a way
            // of getting the font properties, so we use heuristics based on font name.
            String name = f.getFontName(Locale.US).toLowerCase(Locale.US);
            int weight = name.contains("bold") ? 700 : 400;
            FontStyle style = name.contains("italic") ? FontStyle.ITALIC : FontStyle.NORMAL;

            CSSFont fnt = new CSSFont(Paths.get(ttfFile), family, weight, style);

            return Optional.of(fnt);
            
        } catch (FontFormatException | IOException ffe) {
            return Optional.empty();
        }
    }

    public static class CSSFont {
        public final Path path;
        public final String family;

        /**
         * WARNING: Heuristics are used to determine if a font is bold (700) or normal (400) weight.
         */
        public final int weight;

        /**
         * WARNING: Heuristics are used to determine if a font is italic or normal style.
         */
        public final FontStyle style;

        public CSSFont(Path path, String family, int weight, FontStyle style) {
            this.path = path;
            this.family = family;
            this.weight = weight;
            this.style = style;
        }

        /**
         * WARNING: Basic escaping, may not be robust to attack.
         */
        public String familyCssEscaped() {
            return this.family.replace("'", "\\'");
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, family, weight, style);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            
            if (other == null ||
                other.getClass() != this.getClass()) {
                return false;
            }

            CSSFont b = (CSSFont) other;

            return Objects.equals(this.path, b.path) &&
                   Objects.equals(this.family, b.family) &&
                   this.weight == b.weight &&
                   this.style == b.style;
        }
    }

    

    public static class FontFileProcessor extends SimpleFileVisitor<Path> {
        private final List<String> validFileExtensions;
        private final List<CSSFont> fontsAdded = new ArrayList<>();

        public FontFileProcessor(List<String> validFileExtensions) {
            this.validFileExtensions = new ArrayList<>(validFileExtensions);
        }

        public List<CSSFont> getFontsAdded() {
            return this.fontsAdded;
        }

        @Override
        public FileVisitResult visitFile(Path font, BasicFileAttributes attrs) throws IOException {
            if (attrs.isRegularFile() && isValidFont(font)) {
            	AutoFont.fromFontFile(font.toString()).ifPresent(fontsAdded::add);
            }
            return FileVisitResult.CONTINUE;
        }

        protected void onValidFont(CSSFont font) {
            log.info("Adding font with path = '%s', name = '%s', weight = %d, style = %s%n", font.path, font.family, font.weight, font.style.name());
        }

        protected void onInvalidFont(Path font, FontFormatException ffe) {
            log.warn("Ignoring font file with invalid font format: " + font);
            log.debug("Exception details: ", ffe);
        }
  
        protected boolean isValidFont(Path font) {
            return this.validFileExtensions.stream()
                     .anyMatch(ext -> font.toString().endsWith(ext));
        }
    }
}
