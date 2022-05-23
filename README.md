# html2pdfr

![R-CMD-check](https://github.com/terminological/htm2pdfr/actions/workflows/R-CMD-check/badge.svg)

R wrapper for OpenHTMLtoPDF java library.

Html2pdfr is a basic replacement for webshot which does not require any external dependencies, and would suit running on a headless server. Its aim is to support the minimally useful layout and conversion of static HTML to PDF. The general purpose is to generate a static version of HTML tables, created by libraries such as huxtable, that can be included in latex documents, or presentations. It can convert other simple HTML, you'll just have to try it to see whether it works. 

## Installation instructions

html2pdfr is not on cran yet. Installation from this repo can be done as follows:

```R
devtools::install_github("terminological/html2pdfr")
```

[Visit the docs](https://terminological.github.io/html2pdfr/) for more info.
