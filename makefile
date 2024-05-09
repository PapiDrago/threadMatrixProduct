all: Main.class

%.class: %.java
	javac $<

clean:
	rm -f *.class