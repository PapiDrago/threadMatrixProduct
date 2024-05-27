# Thread Matricial product execution time comparator

This Java program executes one or more matricial products by using different approaches offered by the Java platform.
Then for each of those is registered the execution time.

## Features

- Single-threaded matrix multiplication
- Multi-threaded matrix multiplication using "raw" threads, thread pool and threads waiting at a cyclic barrier
- Comprehensive JavaDoc documentation
- Simple example of use of a makefile file

## Installation

1. Get a Java Development Kit (JDK). This program has been developed using openjdk 17.0.10, but earlier version, from Java 8 should work.
2.  Clone this repository:
```sh
   git clone https://github.com/PapiDrago/threadMatrixProduct.git
```

## Usage

1. Generating a particular number 'N' of matrix ready to be multiplied: 
```sh
   java Main [N]
```
2. Generating as many matrix as the command-line arguments with particular values for rows and columns:
```sh
   java Main [rows],[columns]
```
For example:
```sh
   java Main 2,4 4,2
```
There will be generated two matrices. The first with dimension '2x4', the second one with order '4x2'.

## License
[MIT](LICENSE)