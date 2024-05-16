import java.util.Random;
//import java.util.regex.Pattern;

public class Main{
    public static void main(String args[]) throws InterruptedException {
        /*SynchronizedMatrix firstMatrix = new SynchronizedMatrix(2, 2);
        SynchronizedMatrix secondMatrix = new SynchronizedMatrix(2, 2);
        firstMatrix.printMatrix();
        secondMatrix.printMatrix();
        SynchronizedMatrix c = firstMatrix.matrixProd(secondMatrix.getMatrix());
        c.printMatrix();*/
        int matrices[][][] = initMatricesFromArguments(args);
        int productResults[][][] = product(matrices);
        printMatrixArray(productResults);
        /*int firstMatrix[][] = matrices[0];
        int secondMatrix[][] = matrices[1];
        printMatrix(firstMatrix);
        printMatrix(secondMatrix);
        int c[][] = new int[firstMatrix.length][secondMatrix[0].length];//
        int nCEntries = firstMatrix.length*secondMatrix[0].length;
        Thread[] threads = new Thread[nCEntries];
        RowColumnProduct products[] = new RowColumnProduct[nCEntries];
        initThreadProducts(products, firstMatrix, secondMatrix, c);
        for(int i = 0; i<threads.length; i++){
            
            threads[i] = new Thread(products[i]);
        }
        AtomicBoolean AreThreadsRunning = new AtomicBoolean(true);
        new Thread(()->{while(AreThreadsRunning.get()) {}
                            System.out.print("\nCurrent state: \n");
        /*synchronized(c){*///printMatrix(c);/*  try {products[0].getMatrix().wait();} catch(InterruptedException e){}*//*}*/}).start();
        /*for (int k=0; k<threads.length; k++) {
            threads[k].start();
        }
        
        for (int k=0; k<threads.length; k++) {
            threads[k].join();
        }
        AreThreadsRunning.set(false);*/
        //printMatrix(c);
        //System.out.println("Fine mainThread.");
        //System.out.println();
        //printMatrix(singleThreadMatrixProd(firstMatrix, secondMatrix));
    }

    public static int[][] initMatrix(int rows, int columns) {
        Random randomStream = new Random();
        int matrix[][] = new int[rows][columns];
        for (int i=0; i<rows; i++){
            for (int j=0; j<columns; j++){
                matrix[i][j] = randomStream.nextInt(10);
            }
        }
        return matrix;
    }

    public static void printMatrix(int[][] matrix){
        if (matrix == null) {
            System.out.println("La matrice non è stata definita.");
        } else {
            System.out.print("\n");
            for (int i = 0; i<matrix.length; i++){
                for (int j = 0; j<matrix[0].length; j++){ //itero sulla riga i-esima
                    System.out.print(matrix[i][j] + " ");
                }
                System.out.println();
            }
        }
    }
    public static void initThreadProducts(RowColumnProduct[] products,
                                        int[][] firstMatrix, int secondMatrix[][], int c[][]){
        if (firstMatrix == null || secondMatrix == null){
            System.out.println("Impossibile eseguire il prodotto perche' almeno una delle matrici non è stata definita.");
        } else {
            int i = 0;
            for (int firstMatrixRow = 0; firstMatrixRow<firstMatrix.length; firstMatrixRow++) {
                for (int secondMatrixCol = 0; secondMatrixCol<secondMatrix[0].length; secondMatrixCol++){
                    products[i] = new RowColumnProduct(firstMatrix, secondMatrix, firstMatrixRow, secondMatrixCol, c);
                    i++;
                }
            }
        }
    }


    public static int[][] singleThreadMatrixProd(int[][] firstMatrix, int[][] secondMatrix){
        if(firstMatrix[0].length != secondMatrix.length) {
            throw new IllegalArgumentException("Il numero di colonne di A "+
                                    "non e' uguale al numero di righe di B!");
        } 
        int c[][] = new int[firstMatrix.length][secondMatrix[0].length];
        for(int firstMatrixRow = 0; firstMatrixRow<firstMatrix.length; firstMatrixRow++){
            for(int secondMatrixCol = 0; secondMatrixCol<secondMatrix[0].length; secondMatrixCol++){
                int sum = 0;
                for (int firstMatrixCol = 0; firstMatrixCol < firstMatrix[0].length; firstMatrixCol++){
                    sum = sum + firstMatrix[firstMatrixRow][firstMatrixCol] * secondMatrix[firstMatrixCol][secondMatrixCol];
                }
                c[firstMatrixRow][secondMatrixCol] = sum;
            }
        }
        return c;
    }

    public static int[][][] product(int[][][] matrices) throws InterruptedException {
        int numProducts = (int) matrices.length / 2;
        int matrixResults[][][] = new int[numProducts][][];
        for(int i=0; i+1<matrices.length; i +=2){
            int[][] firstMatrix = matrices[i];
            int[][] secondMatrix = matrices[i+1];
            printMatrix(firstMatrix);
            printMatrix(secondMatrix);
            if(!RowColumnProduct.isWellDefined(firstMatrix, secondMatrix)){
                matrixResults[i/2] = null;
                continue;
            }
            matrixResults[i/2] = new int[firstMatrix.length][secondMatrix[0].length];
            int[][] c = matrixResults[i/2];
            System.out.println(getEntryCount(c));
            Thread[] threads = new Thread[getEntryCount(c)];
            RowColumnProduct products[] = new RowColumnProduct[getEntryCount(c)];
            initThreadProducts(products, firstMatrix, secondMatrix, c);
            for(int k = 0; k<threads.length; k++){
                threads[k] = new Thread(products[k]);
            }
            for (int k=0; k<threads.length; k++) {
                threads[k].start();
            }
            for (int k=0; k<threads.length; k++) {
                threads[k].join();
            }
            System.out.println(i);

        }
        return matrixResults;
    }

    public static int[][][] initMatricesFromArguments(String[] args){
        if(args.length == 0) {
            System.out.println("Inizialiazzazione di due matrici 2x2.");
            int matrices[][][] = new int[2][][];
            matrices[0] = initMatrix(2, 2);
            matrices[1] = initMatrix(2, 2);
            return matrices;
        }
        int matrices[][][] = new int[args.length][][];
        for (int i = 0; i<args.length; i++){
            try {
                String[] dimensions = args[i].split(",");
                if (dimensions.length != 2) {
                    throw new IllegalArgumentException("formato "+
                    "dell'input invalido, richiesto '[righe],[colonne]'.");
                }
                int rows = Integer.parseInt(dimensions[0]);
                int cols = Integer.parseInt(dimensions[1]);
                if (rows <= 0 || cols <= 0) {
                    throw new IllegalArgumentException("dimensioni "
                    +"della matrice non corrette, devono essere strettamente positive.");
                }
                matrices[i] = initMatrix(rows, cols);
            } catch (NumberFormatException e) {
                System.err.println("Errore nel parsing di '"+args[i]+"'."+
                " La stringa contiene un valore che non rappresenta un numero intero. "+ e.getMessage());
                matrices[i] = null;
                //System.exit(1);
            } catch (IllegalArgumentException e ) {
                System.err.println("Errore nella gestione di '"+args[i]+"':"+
                " "+ e.getMessage());
                matrices[i] = null;
                //System.exit(1);
            }
            
            /*if(Pattern.matches("[0-9]+,[0-9]+", args[i])){
                int rows = Integer.parseInt(args[i].split(",")[0]);
                int cols = Integer.parseInt(args[i].split(",")[1]);
                int matrix[][] = initMatrix(rows, cols);
                matrices[i] = matrix; //da gestire '0,0'

            } else {
                throw new IllegalArgumentException("Gli argomenti facoltativi "
                +"devono avere la seguente espressione [numero righe],[numero colonne]");
            }*/
            /*if (matrices[i] == null){
                System.exit(1);
            }*/
        }
        return matrices;
    }
    public static boolean IsMatrixNull(int[][] matrix){
        return matrix == null;
    }

    public static int getEntryCount(int[][] matrix) {
        return matrix.length*matrix[0].length;
    }

    public static void printMatrixArray(int[][][] matrixArray) {
        for (int[][] matrix : matrixArray) {
            printMatrix(matrix);
        }
    }
}
