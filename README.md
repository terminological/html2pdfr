# html2pdfr

[![R-CMD-check](https://github.com/terminological/html2pdfr/workflows/R-CMD-check/badge.svg)](https://github.com/terminological/html2pdfr/actions)

[![DOI](https://zenodo.org/badge/459091655.svg)](https://zenodo.org/badge/latestdoi/459091655)

R wrapper for OpenHTMLtoPDF java library.

`html2pdfr` is a basic replacement for `webshot` which does not require any external dependencies, and would suit running on a headless server. Its aim is to support the minimally useful layout and conversion of static HTML to PDF. The general purpose is to generate a static version of HTML tables, created by libraries such as `huxtable`, that can be included in latex documents, or presentations. To facilitate this a focus of `html2pdfr` is automatically laying out and rendering content within pre-specified bounding boxes. `html2pdfr` can convert a range of simple HTML, including some SVG and MathML, you'll just have to try it to see whether it works. 

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
