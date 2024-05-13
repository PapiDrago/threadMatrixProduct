all: Main.class

Main.class: RowColumnProduct.class

%.class: %.java
	javac $<

clean:
	rm -f *.class