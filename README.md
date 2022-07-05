# html2pdfr - R wrapper for OpenHTMLtoPDF java library.

[![R-CMD-check](https://github.com/terminological/html2pdfr/workflows/R-CMD-check/badge.svg)](https://github.com/terminological/html2pdfr/actions)

[![DOI](https://zenodo.org/badge/459091655.svg)](https://zenodo.org/badge/latestdoi/459091655)

`html2pdfr` is designed to help get PDF or PNG formats of HTML tables, so that the table can be included in a LaTeX page size, but with layout and control of styling determined by HTML and CSS. This allows use of HTML table layout within LaTeX documents, and for alignment between web and printed content. For HTML layout tabular content should be allowed to grow up to the page dimensions, but we don't want to force narrower tables to be a set width or height either. Automatic calculation of the output size up to set page size limits, is one of the differentiators between this and other options, such as `webshot` and `webshot2`. Although the focus is on tables, any basic static HTML content can be rendered including SVG and MathML, up to the support of the underlying HTML rendering engine, the Java OpenHTML2PDF library (https://github.com/danfickle/openhtmltopdf). 

## Installation instructions

`html2pdfr` is based on a java library and must have a working version of `Java` and `rJava` installed. The following commands can ensure that your `rJava` installation is working.

```R
install.packages("rJava")
rJava::.jinit()
rJava::J("java.lang.System")$getProperty("java.version")
```

`html2pdfr` is not on cran yet. Installation from this repo can be done as follows:

```R
devtools::install_github("terminological/html2pdfr")
```

Submission to CRAN is planned shortly.

[Visit the docs](https://terminological.github.io/html2pdfr/) for usage info.
