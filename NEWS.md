# html2pdfr 0.4.0

* Feature complete initial CRAN candidate
* converts simple html to pdf and png files from a url, from a local file, or from a HTML string
* identical pdf and png output for huxtable tables. 
* very close to browser rendering if size contraints matched
* ttf fonts need to be supplied from R (done automatically).

# html2pdfr 0.4.1

* remove dependency on extrafont (for sysfont)
* Bug: cannot generate png files without pdfs

# html2pdfr 0.4.2

* Fix bug: cannot generate png files without pdfs
* Reduce verbosity of logging.
* Updated to use r6-generator:0.5.5 

# html2pdfr unstable

* TODO: check whether java's font detection can be used.
* possibly including Google fonts.