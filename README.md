# html2pdfr - R wrapper for OpenHTMLtoPDF java library.

[![R-CMD-check](https://github.com/terminological/html2pdfr/workflows/R-CMD-check/badge.svg)](https://github.com/terminological/html2pdfr/actions)

[![DOI](https://zenodo.org/badge/459091655.svg)](https://zenodo.org/badge/latestdoi/459091655)

`html2pdfr` is designed to help get PDF or PNG rendering of HTML tables, so that the table can be included in a LaTeX page. This allows use of HTML table layout within LaTeX documents with page size determined by LaTeX, but with layout and control of styling determined by HTML and CSS. This creates alignment between web and printed content. Tabular content should be allowed to grow up to the page dimensions, but we don't want to force narrower tables to be a set width or height. Automatic calculation of the output size up to specified page size limits, is one of the differentiators between this and other options, such as `webshot` and `webshot2`. Although the focus is on tables, any basic static HTML content can be rendered including SVG and MathML, up to the level support of the underlying HTML rendering engine, the Java OpenHTML2PDF library (https://github.com/danfickle/openhtmltopdf). 

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

Stable releases are preferred.
On windows particularly build may fail if the multi-arch option is set

```R
devtools::install_github("terminological/html2pdfr@0.4.2", args = c("--no-multiarch"))
```

Submission to CRAN is planned shortly.

[Visit the docs](https://terminological.github.io/html2pdfr/) for usage info.
