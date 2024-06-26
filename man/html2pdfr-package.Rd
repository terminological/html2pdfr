% Generated by r6-generator-maven-plugin: do not edit by hand
\docType{package}
\name{html2pdfr-package}
\alias{html2pdfr}
\alias{html2pdfr-package}
\title{html2pdfr: R Wrapper For Openhtmltopdf Java Library}
\description{
% \if{html}{\figure{logo.png}{options: align='right'}}
HTML and CSS do a good job at automatically laying out and styling content particularly in tables, however it is not natively designed for pagination. This library converts HTML content into PDF and PNG formats for embedding into LaTeX documents, within the constraints of page sizes. It allows use of HTML table layout from HTML first libraries such as 'gt' and 'huxtable' within latex documents. It allows HTML content to grow in width up to the page dimensions, but preventing it from overflowing, and without forcing table layout to be wider than it would normally be. This heurisitic calculation of the output size up to fit within set limits is one of the differentiators between this and other HTML to PDF converters. Although the focus is on tables, any basic HTML content can be rendered, including simple SVG and MathML, up to the support of the underlying HTML rendering engine (https://github.com/danfickle/openhtmltopdf).

Version: 0.4.5

Classes:
\itemize{ 
	\item \code{JavaApi}
	\item \code{HtmlConverter}
}
}

\author{
\itemize{ 
   \item Rob Challen rob.challen@bristol.ac.uk 0000-0002-5504-7768
}
}

\keyword{java api}