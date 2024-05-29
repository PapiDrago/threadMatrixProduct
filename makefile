all: Main.class

Main.class: RowColumnProduct.class

%.class: %.java
	javac $<

jar: prodottoMatriciThread.jar

doc: doc/index.html

prodottoMatriciThread.jar: Main.java RowColumnProduct.java makefile
	jar -cfm prodottoMatriciThread.jar manifest.mf Main*.class Main.java RowColumnProduct.class RowColumnProduct.java makefile LICENSE README.md

doc/index.html: *.java
	javadoc -nodeprecated -nohelp -d doc Main.java RowColumnProduct.java

clean:
	rm -f *.class