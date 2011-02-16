Sendetary lets you drag and drop content from one computer to another.

THIS IS A PROTOTYPE OF A PROTOTYPE OF A PROTOTYPE.  I only put it on
Github so that I could test it by letting friends download it.

Building
--------

Because I haven't set up any build tools yet:

    mkdir build
    scalac -d build -cp lib/smack.jar sendetary.scala
    scala -cp build:lib/smack.jar Sendetary noah tim
