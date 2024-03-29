# Generated by r6-generator-maven-plugin: remove this line and make manual changes and stop them from being overwritten
# Contact: rob.challen@bristol.ac.uk
# Automated tests for html2pdfr::HtmlConverter

# R6 HtmlConverter instance methods ----


# HtmlConverter static methods ----

# ├ html_converter() package method ----
# ├ url_to_pdf() package method ----
test_that("html2pdfr::url_to_pdf() static method", {
	url_to_pdf('https://cran.r-project.org/banner.shtml')
	expect_message(message("test case complete"))
})
# ├ file_to_pdf() package method ----
test_that("html2pdfr::file_to_pdf() static method", {
	dest = tempfile(fileext='.html')
	download.file('https://cran.r-project.org/banner.shtml', destfile = dest)
	file_to_pdf(dest)
	expect_message(message("test case complete"))
})
# ├ html_document_to_pdf() package method ----
test_that("html2pdfr::html_document_to_pdf() static method", {
	library(readr)
	html = read_file('https://fred-wang.github.io/MathFonts/mozilla_mathml_test/')
	html_document_to_pdf(html, baseUri = 'https://fred-wang.github.io/MathFonts/mozilla_mathml_test/')
	expect_message(message("test case complete"))
})
# ├ html_fragment_to_pdf() package method ----
test_that("html2pdfr::html_fragment_to_pdf() static method", {
	library(dplyr)
	html = iris %>% group_by(Species) %>%
	summarise(across(everything(), mean)) %>%
	huxtable::as_hux() %>%
	huxtable::theme_article() %>%
	huxtable::to_html()
	html_fragment_to_pdf(html)
	expect_message(message("test case complete"))
})
