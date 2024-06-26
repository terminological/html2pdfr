% Generated by r6-generator-maven-plugin: do not edit by hand

\name{JavaApi}

\alias{JavaApi}

\title{R Wrapper For Openhtmltopdf Java Library}

\section{Usage}{
	\if{html}{\out{<div class="r">}}
	\preformatted{
 J = html2pdfr::JavaApi$get(logLevel)
 	}
  \if{html}{\out{</div>}}
}

\arguments{
	\if{html}{\out{<div class="arguments">}}
	\item{logLevel}{optional - the slf4j log level as a string - one of OFF (most specific, no logging), 
	FATAL (most specific, little data), ERROR, WARN, INFO, DEBUG, 
	TRACE (least specific, a lot of data), ALL (least specific, all data)}
	\if{html}{\out{</div>}}
}

\description{
HTML and CSS do a good job at automatically laying out and styling content particularly in tables, however it is not natively designed for pagination. This library converts HTML content into PDF and PNG formats for embedding into LaTeX documents, within the constraints of page sizes. It allows use of HTML table layout from HTML first libraries such as 'gt' and 'huxtable' within latex documents. It allows HTML content to grow in width up to the page dimensions, but preventing it from overflowing, and without forcing table layout to be wider than it would normally be. This heurisitic calculation of the output size up to fit within set limits is one of the differentiators between this and other HTML to PDF converters. Although the focus is on tables, any basic HTML content can be rendered, including simple SVG and MathML, up to the support of the underlying HTML rendering engine (https://github.com/danfickle/openhtmltopdf).

Version: 0.4.5

Generated: 2024-04-24T15:58:01.201546101
}

\author{\email{rob.challen@bristol.ac.uk}}

\examples{
## -----------------------------------
## Check library dependencies for html2pdfr
## -----------------------------------
html2pdfr::JavaApi$installDependencies()

## -----------------------------------
## Construct a html2pdfr Java API instance
## -----------------------------------

J = html2pdfr::JavaApi$get()
# or a more verbose configuration
# J = html2pdfr::JavaApi$get("DEBUG")


## -----------------------------------
## Method `J$HtmlConverter$new(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$HtmlConverter$new(fontfiles)
# or alternatively:
html2pdfr::new(fontfiles)
}

## -----------------------------------
## Method `J$HtmlConverter$htmlConverter(...)`
## Aliased as `html2pdfr::html_converter(...)`
## -----------------------------------
conv = html2pdfr::html_converter()

## -----------------------------------
## Method `J$HtmlConverter$urlToPdf(...)`
## Aliased as `html2pdfr::url_to_pdf(...)`
## -----------------------------------
library(testthat)
url_to_pdf('https://cran.r-project.org/banner.shtml')

## -----------------------------------
## Method `J$HtmlConverter$fileToPdf(...)`
## Aliased as `html2pdfr::file_to_pdf(...)`
## -----------------------------------
library(testthat)
dest = tempfile(fileext='.html')
download.file('https://cran.r-project.org/banner.shtml',
 destfile = dest)
file_to_pdf(dest)

## -----------------------------------
## Method `J$HtmlConverter$htmlDocumentToPdf(...)`
## Aliased as `html2pdfr::html_document_to_pdf(...)`
## -----------------------------------
library(testthat)
library(readr)
html = read_file('https://fred-wang.github.io/MathFonts/mozilla_mathml_test/')
html_document_to_pdf(html, baseUri = 'https://fred-wang.github.io/MathFonts/mozilla_mathml_test/')

## -----------------------------------
## Method `J$HtmlConverter$htmlFragmentToPdf(...)`
## Aliased as `html2pdfr::html_fragment_to_pdf(...)`
## -----------------------------------
library(testthat)
library(dplyr)
html = iris \%>\% group_by(Species) \%>\%
summarise(across(everything(), mean)) \%>\%
huxtable::as_hux() \%>\%
huxtable::theme_article() \%>\%
huxtable::to_html()
html_fragment_to_pdf(html)
}

\keyword{java api}

\section{Package initialisation and control}{
	\itemize{
		\item \href{#method-api-installDependencies}{\code{JavaApi$installDependencies()}}
		\item \href{#method-api-rebuildDependencies}{\code{JavaApi$rebuildDependencies()}}
		\item \href{#method-api-versionInformation}{\code{JavaApi$versionInformation()}}
		\item \href{#method-api-get}{\code{J = JavaApi$get(logLevel)}}
		\item \href{#method-api-changeLogLevel}{\code{J$changeLogLevel(logLevel)}}
		\item \href{#method-api-reconfigureLog}{\code{J$reconfigureLog(log4jproperties)}}
		\item \href{#method-api-printMessages}{\code{J$printMessages()}}
	}
}

\section{Package classes and static methods}{
	\if{html}{\out{<hr>}}
	\itemize{
		\item \href{#method-HtmlConverter-new}{\code{J$HtmlConverter$new(fontfiles)}}
		\item \href{#method-HtmlConverter-htmlConverter}{\code{J$HtmlConverter$htmlConverter(fontfiles, update)}}
		\item \href{#method-HtmlConverter-urlToPdf}{\code{J$HtmlConverter$urlToPdf(htmlUrl, outFile, cssSelector, xMarginInches, yMarginInches, maxWidthInches, maxHeightInches, formats, pngDpi, converter)}}
		\item \href{#method-HtmlConverter-fileToPdf}{\code{J$HtmlConverter$fileToPdf(inFile, outFile, cssSelector, xMarginInches, yMarginInches, maxWidthInches, maxHeightInches, formats, pngDpi, converter)}}
		\item \href{#method-HtmlConverter-htmlDocumentToPdf}{\code{J$HtmlConverter$htmlDocumentToPdf(html, outFile, baseUri, cssSelector, xMarginInches, yMarginInches, maxWidthInches, maxHeightInches, formats, pngDpi, converter)}}
		\item \href{#method-HtmlConverter-htmlFragmentToPdf}{\code{J$HtmlConverter$htmlFragmentToPdf(htmlFragment, outFile, xMarginInches, yMarginInches, maxWidthInches, maxHeightInches, formats, pngDpi, converter)}}
	}
}

\section{Package initialisation and control}{
	\if{html}{\out{<a id="method-api-installDependencies"></a>}}
	\subsection{Package method \code{JavaApi$installDependencies()}}{
		This package level method checks for, and installs any dependencies needed for the running of the package.
		It is called automatically on first package load and so in general does not need to be used directly.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
html2pdfr::JavaApi$installDependencies()
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{none}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
nothing. called for side effects.
		}
	}
	
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-api-rebuildDependencies"></a>}}
	\subsection{Package method \code{JavaApi$rebuildDependencies()}}{
		This package level method removes existing dependencies and re-installs dependencies needed for the running of the package.
		It is called automatically on first package load and so in general does not need to be called.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
html2pdfr::JavaApi$rebuildDependencies()
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{none}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
nothing. called for side effects.
		}
	}
	
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-api-versionInformation"></a>}}
	\subsection{Package method \code{JavaApi$versionInformation()}}{
		This package level method returns debugging version information for the package
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
html2pdfr::JavaApi$versionInformation()
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{none}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
A list containing a set of versioning information about this package.
		}
	}
	
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-api-get"></a>}}
	\subsection{Package method \code{JavaApi$get()}}{
		This is the main entry point for the package and the root of the Java API in this package. All classes defined in the package
		are made available as items under this root. The JavaApi object manages the communication between R and Java.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = html2pdfr::JavaApi$get()
# package classes and functions are nested under the `J` api object.
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{logLevel}{ The desired verbosity of the package. One of "OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL".}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
A R6 html2pdfr::JavaApi object containing the access point to the objects and functions defined in this package 
		}
	}
	
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-api-changeLogLevel"></a>}}
	\subsection{Api method \code{J$changeLogLevel(logLevel)}}{
		Once the package is initialised the log level can be changed to increase the level of messages from the api.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = html2pdfr::JavaApi$get()
J$changeLogLevel("DEBUG")
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{logLevel}{ The desired verbosity of the package. One of "OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL".}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
nothing. used for side effects.
		}
	}
	
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-api-reconfigreLog"></a>}}
	\subsection{Api method \code{J$reconfigureLog(log4jproperties)}}{
		Experimental / Advanced use: Once the package is initialised the log configureation can be changed to log to 
		an external file for example.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = html2pdfr::JavaApi$get()
prp = fs::path(getwd(),"log4j.properties")
if (fs::file_exists(prp)) {
	J$changeLogLevel(prp)
}
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{log4jproperties}{ a full path to a log4jproperies file}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
nothing. used for side effects.
		}
	}
	
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-api-printMessages"></a>}}
	\subsection{Api method \code{J$printMessages()}}{
		Experimental / Internal use: Messages from Java to R are queued and
		printed after each function call. It is unlikely that any will be not printed
		so in normal circumstances this function should do nothing.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = html2pdfr::JavaApi$get()
J$printMessages()
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{none}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
nothing. used for side effects.
		}
	}
	
}


\section{Static methods and constructors}{
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-HtmlConverter-new"></a>}}
	
	\subsection{Method \code{HtmlConverter$new()}}{
		the default no-args constructor
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = html2pdfr::JavaApi$get()
J$HtmlConverter$new(fontfiles)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{fontfiles}{ - (java expects a String)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 HtmlConverter object: 
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-HtmlConverter-htmlConverter"></a>}}
	
	\subsection{Method \code{HtmlConverter$htmlConverter()}}{
		Create a new HtmlConverter 

for creating PDF and PNG files from HTML. In general this will be created automatically.
but if you have specific fonts you want to use then you may need to pass them to this
function and specify the result in the `converter` parameter of the main functions.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = html2pdfr::JavaApi$get()
J$HtmlConverter$htmlConverter(fontfiles, update)
# this method is also exposed as a package function:
html2pdfr::html_converter(fontfiles, update)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{fontfiles - a character vector of font files that will be imported into the converter. - (defaulting to `systemfonts::system_fonts()$path`)}{ - (java expects a RCharacterVector)}
				\item{update - (defaulting to `FALSE`)}{ - (java expects a RLogical)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 HtmlConverter object: 
		}
	}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
conv = html2pdfr::html_converter()
			}
			\if{html}{\out{</div>}}
		}

	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-HtmlConverter-urlToPdf"></a>}}
	
	\subsection{Method \code{HtmlConverter$urlToPdf()}}{
		Convert HTML document from a URL to a PDF document. 

The URL is assumed to be a complete document. 
The resulting PDF size will be controlled by page media directives within the HTML, 
unless explicitly given here in `maxWidthInches` and `maxHeightInches`. 
If the `cssSelector` parameter is given the HTML fragment at that selector will be used.
In this case it is will be resized to fit within the given dimensions and shrink wrapped
so that the content is smaller. If no dimensions are present this will default to A4.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = html2pdfr::JavaApi$get()
J$HtmlConverter$urlToPdf(htmlUrl, outFile, cssSelector, xMarginInches, yMarginInches, maxWidthInches, maxHeightInches, formats, pngDpi, converter)
# this method is also exposed as a package function:
html2pdfr::url_to_pdf(htmlUrl, outFile, cssSelector, xMarginInches, yMarginInches, maxWidthInches, maxHeightInches, formats, pngDpi, converter)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{htmlUrl the URL}{ - (java expects a RCharacter)}
				\item{outFile the full path of the output file - (defaulting to `tempfile('html2pdfr_')`)}{ - (java expects a RFile)}
				\item{cssSelector the part of the page you want to convert to PDF. - (defaulting to `NA_character_`)}{ - (java expects a RCharacter)}
				\item{xMarginInches page width margins - (defaulting to `NA_real_`)}{ - (java expects a RNumeric)}
				\item{yMarginInches page height margins - (defaulting to `NA_real_`)}{ - (java expects a RNumeric)}
				\item{maxWidthInches what is the maximum allowable width? - (defaulting to `NA_real_`)}{ - (java expects a RNumeric)}
				\item{maxHeightInches what is the maximum allowable height? (if the content is larger than this then it will overflow to another page) - (defaulting to `NA_real_`)}{ - (java expects a RNumeric)}
				\item{formats If the outFile does not specify a file extension then you can do so here as "png" or "pdf" or both. - (defaulting to `c('pdf')`)}{ - (java expects a RCharacterVector)}
				\item{pngDpi the dots per inch for png outputs if requested - (defaulting to `300`)}{ - (java expects a RNumeric)}
				\item{converter (optional) a configured HTML converter, only needed if manually specifying fonts. - (defaulting to `html2pdfr::html_converter()`)}{ - (java expects a HtmlConverter)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			RCharacterVector: the filename(s) written to (with extension '.pdf' or '.png' if outFile did not have an extension).
		}
	}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
library(testthat)
url_to_pdf('https://cran.r-project.org/banner.shtml')
			}
			\if{html}{\out{</div>}}
		}

	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-HtmlConverter-fileToPdf"></a>}}
	
	\subsection{Method \code{HtmlConverter$fileToPdf()}}{
		Convert HTML document from a local file to a PDF document. 

The HTML in `inFile` is assumed to be a complete document. Relative references are resolved
with reference to the HTML file on the file system, so correctly located images etc whould be 
picked up without requiring a server.
The resulting PDF size will be controlled by page media directives within the HTML, 
unless explicitly given here in `maxWidthInches` and `maxHeightInches`. 
If the `cssSelector` parameter is given the HTML fragment at that selector will be used.
In this case it is will be resized to fit within the given dimensions and shrink wrapped
so that the content is smaller. If no dimensions are present this will default to A4.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = html2pdfr::JavaApi$get()
J$HtmlConverter$fileToPdf(inFile, outFile, cssSelector, xMarginInches, yMarginInches, maxWidthInches, maxHeightInches, formats, pngDpi, converter)
# this method is also exposed as a package function:
html2pdfr::file_to_pdf(inFile, outFile, cssSelector, xMarginInches, yMarginInches, maxWidthInches, maxHeightInches, formats, pngDpi, converter)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{inFile the full path the the HTML file}{ - (java expects a RCharacter)}
				\item{outFile the full path of the output file - (defaulting to `tempfile('html2pdfr_')`)}{ - (java expects a RFile)}
				\item{cssSelector the part of the page you want to convert to PDF. - (defaulting to `NA_character_`)}{ - (java expects a RCharacter)}
				\item{xMarginInches page width margins - (defaulting to `NA_real_`)}{ - (java expects a RNumeric)}
				\item{yMarginInches page height margins - (defaulting to `NA_real_`)}{ - (java expects a RNumeric)}
				\item{maxWidthInches what is the maximum allowable width? - (defaulting to `NA_real_`)}{ - (java expects a RNumeric)}
				\item{maxHeightInches what is the maximum allowable height? (if the content is larger than this then it will overflow to another page) - (defaulting to `NA_real_`)}{ - (java expects a RNumeric)}
				\item{formats If the outFile does not specify a file extension then you can do so here as "png" or "pdf" or both. - (defaulting to `c('pdf')`)}{ - (java expects a RCharacterVector)}
				\item{pngDpi the dots per inch for png outputs if requested - (defaulting to `300`)}{ - (java expects a RNumeric)}
				\item{converter (optional) a configured HTML converter, only needed if manually specifying fonts. - (defaulting to `html2pdfr::html_converter()`)}{ - (java expects a HtmlConverter)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			RCharacterVector: the filename written to (with extension '.pdf' or '.png' if outFile did not have an extension).
		}
	}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
library(testthat)
dest = tempfile(fileext='.html')
download.file('https://cran.r-project.org/banner.shtml',
 destfile = dest)
file_to_pdf(dest)
			}
			\if{html}{\out{</div>}}
		}

	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-HtmlConverter-htmlDocumentToPdf"></a>}}
	
	\subsection{Method \code{HtmlConverter$htmlDocumentToPdf()}}{
		Convert HTML document from a string to a PDF document. 

The HTML in `html` is assumed to be a complete document. Relative references are resolved
with reference to `baseUri` if it is given (which could be a `file://` URI).
The resulting PDF size will be controlled by page media directives within the HTML, 
unless explicitly given here in `maxWidthInches` and `maxHeightInches`. 
If the `cssSelector` parameter is given the HTML fragment at that selector will be used.
In this case it is will be resized to fit within the given dimensions and shrink wrapped
so that the content is smaller. If no dimensions are present this will default to A4.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = html2pdfr::JavaApi$get()
J$HtmlConverter$htmlDocumentToPdf(html, outFile, baseUri, cssSelector, xMarginInches, yMarginInches, maxWidthInches, maxHeightInches, formats, pngDpi, converter)
# this method is also exposed as a package function:
html2pdfr::html_document_to_pdf(html, outFile, baseUri, cssSelector, xMarginInches, yMarginInches, maxWidthInches, maxHeightInches, formats, pngDpi, converter)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{html the html document as a string}{ - (java expects a RCharacter)}
				\item{outFile the full path of the output file - (defaulting to `tempfile('html2pdfr_')`)}{ - (java expects a RFile)}
				\item{baseUri the URI from which to interpret relative links in the html content. - (defaulting to `NA_character_`)}{ - (java expects a RCharacter)}
				\item{cssSelector the part of the page you want to convert to PDF. - (defaulting to `NA_character_`)}{ - (java expects a RCharacter)}
				\item{xMarginInches page width margins - (defaulting to `NA_real_`)}{ - (java expects a RNumeric)}
				\item{yMarginInches page height margins - (defaulting to `NA_real_`)}{ - (java expects a RNumeric)}
				\item{maxWidthInches what is the maximum allowable width? - (defaulting to `NA_real_`)}{ - (java expects a RNumeric)}
				\item{maxHeightInches what is the maximum allowable height? (if the content is larger than this then it will overflow to another page) - (defaulting to `NA_real_`)}{ - (java expects a RNumeric)}
				\item{formats If the outFile does not specify a file extension then you can do so here as "png" or "pdf" or both. - (defaulting to `c('pdf')`)}{ - (java expects a RCharacterVector)}
				\item{pngDpi the dots per inch for png outputs if requested - (defaulting to `300`)}{ - (java expects a RNumeric)}
				\item{converter (optional) a configured HTML converter, only needed if manually specifying fonts. - (defaulting to `html2pdfr::html_converter()`)}{ - (java expects a HtmlConverter)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			RCharacterVector: the filename written to (with extension '.pdf' or '.png' if outFile did not have an extension).
		}
	}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
library(testthat)
library(readr)
html = read_file('https://fred-wang.github.io/MathFonts/mozilla_mathml_test/')
html_document_to_pdf(html, baseUri = 'https://fred-wang.github.io/MathFonts/mozilla_mathml_test/')
			}
			\if{html}{\out{</div>}}
		}

	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-HtmlConverter-htmlFragmentToPdf"></a>}}
	
	\subsection{Method \code{HtmlConverter$htmlFragmentToPdf()}}{
		Render HTML fragment from a string to a PDF image.

This is the simple rendering function that will output a PDF file (potentially many pages) and a set of PNG files from HTML content. 
This is primarily used to render HTML content (e.g. a table) that is being included in a larger document.
In this case the HTML fragment will not specify page dimensions which need to be provided (defaults to A4 size with 1 inch margins). 
The result can be embedded into an existing page using latex's includegraphics directive exactly the same way as a graphical figure might be used. 
The sizing of the output will always be smaller than the dimensions of a page, but will shrink to fit the content.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = html2pdfr::JavaApi$get()
J$HtmlConverter$htmlFragmentToPdf(htmlFragment, outFile, xMarginInches, yMarginInches, maxWidthInches, maxHeightInches, formats, pngDpi, converter)
# this method is also exposed as a package function:
html2pdfr::html_fragment_to_pdf(htmlFragment, outFile, xMarginInches, yMarginInches, maxWidthInches, maxHeightInches, formats, pngDpi, converter)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{htmlFragment a HTML fragment, e.g. usually the table element, but may be the whole page.}{ - (java expects a RCharacter)}
				\item{outFile the full path with or without extension (if no extension specified then `formats` parameter will apply) - (defaulting to `tempfile('html2pdfr_')`)}{ - (java expects a RFile)}
				\item{xMarginInches page width margins - (defaulting to `1.0`)}{ - (java expects a RNumeric)}
				\item{yMarginInches page height margins - (defaulting to `1.0`)}{ - (java expects a RNumeric)}
				\item{maxWidthInches what is the maximum allowable width? (default is A4) - (defaulting to `8.27`)}{ - (java expects a RNumeric)}
				\item{maxHeightInches what is the maximum allowable height? (if the content is larger than this then it will overflow to another page) - (defaulting to `11.69`)}{ - (java expects a RNumeric)}
				\item{formats If the outFile does not specify a file extension then you can do so here as "png" or "pdf" or both. - (defaulting to `c('pdf','png')`)}{ - (java expects a RCharacterVector)}
				\item{pngDpi the dots per inch for png outputs if requested. - (defaulting to `300`)}{ - (java expects a RNumeric)}
				\item{converter (optional) a configured HTML converter, only needed if manually specifying fonts. - (defaulting to `html2pdfr::html_converter()`)}{ - (java expects a HtmlConverter)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			RCharacterVector: the filename(s) written to (with extension '.pdf' or '.png' if outFile did not have an extension).
		}
	}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
library(testthat)
library(dplyr)
html = iris \%>\% group_by(Species) \%>\%
summarise(across(everything(), mean)) \%>\%
huxtable::as_hux() \%>\%
huxtable::theme_article() \%>\%
huxtable::to_html()
html_fragment_to_pdf(html)
			}
			\if{html}{\out{</div>}}
		}


}