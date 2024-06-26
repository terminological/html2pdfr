% Generated by r6-generator-maven-plugin: do not edit by hand

\name{html_document_to_pdf}
\alias{html_document_to_pdf}
\title{Convert HTML document from a string to a PDF document. }
\usage{html_document_to_pdf(
	html,
	outFile,
	baseUri,
	cssSelector,
	xMarginInches,
	yMarginInches,
	maxWidthInches,
	maxHeightInches,
	formats,
	pngDpi,
	converter
)
}
\arguments{
	\if{html}{\out{<div class="arguments">}}
	\item{html}{html the html document as a string - (java expects a RCharacter)}
	\item{outFile}{outFile the full path of the output file - (defaulting to `tempfile('html2pdfr_')`) - (java expects a RFile)}
	\item{baseUri}{baseUri the URI from which to interpret relative links in the html content. - (defaulting to `NA_character_`) - (java expects a RCharacter)}
	\item{cssSelector}{cssSelector the part of the page you want to convert to PDF. - (defaulting to `NA_character_`) - (java expects a RCharacter)}
	\item{xMarginInches}{xMarginInches page width margins - (defaulting to `NA_real_`) - (java expects a RNumeric)}
	\item{yMarginInches}{yMarginInches page height margins - (defaulting to `NA_real_`) - (java expects a RNumeric)}
	\item{maxWidthInches}{maxWidthInches what is the maximum allowable width? - (defaulting to `NA_real_`) - (java expects a RNumeric)}
	\item{maxHeightInches}{maxHeightInches what is the maximum allowable height? (if the content is larger than this then it will overflow to another page) - (defaulting to `NA_real_`) - (java expects a RNumeric)}
	\item{formats}{formats If the outFile does not specify a file extension then you can do so here as "png" or "pdf" or both. - (defaulting to `c('pdf')`) - (java expects a RCharacterVector)}
	\item{pngDpi}{pngDpi the dots per inch for png outputs if requested - (defaulting to `300`) - (java expects a RNumeric)}
	\item{converter}{converter (optional) a configured HTML converter, only needed if manually specifying fonts. - (defaulting to `html2pdfr::html_converter()`) - (java expects a HtmlConverter)}
	\if{html}{\out{</div>}}
}
\value{
RCharacterVector: the filename written to (with extension '.pdf' or '.png' if outFile did not have an extension).

}
\description{
The HTML in `html` is assumed to be a complete document. Relative references are resolved
with reference to `baseUri` if it is given (which could be a `file://` URI).
The resulting PDF size will be controlled by page media directives within the HTML, 
unless explicitly given here in `maxWidthInches` and `maxHeightInches`. 
If the `cssSelector` parameter is given the HTML fragment at that selector will be used.
In this case it is will be resized to fit within the given dimensions and shrink wrapped
so that the content is smaller. If no dimensions are present this will default to A4.
}
\examples{
library(testthat)
library(readr)
html = read_file('https://fred-wang.github.io/MathFonts/mozilla_mathml_test/')
html_document_to_pdf(html, baseUri = 'https://fred-wang.github.io/MathFonts/mozilla_mathml_test/')
}
\keyword{java api}


