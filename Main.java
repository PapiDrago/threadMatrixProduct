import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class Main{
    public static void main(String args[]) throws InterruptedException {
        /*SynchronizedMatrix a = new SynchronizedMatrix(2, 2);
        SynchronizedMatrix b = new SynchronizedMatrix(2, 2);
        a.printMatrix();
        b.printMatrix();
        SynchronizedMatrix c = a.matrixProd(b.getMatrix());
        c.printMatrix();*/
        int matrices[][][] = initMatricesFromArguments(args);
        int a[][] = matrices[0];
        int b[][] = matrices[1];
        printMatrix(a);
        printMatrix(b);
        int c[][] = new int[a.length][b[0].length];//
        int nCEntries = a.length*b[0].length;
        Thread[] threads = new Thread[nCEntries];
        RowColumnProduct products[] = new RowColumnProduct[nCEntries];
        initThreadProducts(products, a, b, c);
        for(int i = 0; i<threads.length; i++){
            
            threads[i] = new Thread(products[i]);
        }
        AtomicBoolean AreThreadsRunning = new AtomicBoolean(true);
        new Thread(()->{while(AreThreadsRunning.get())
                            {System.out.print("\nCurrent state: \n");
        synchronized(c){printMatrix(products[0].getMatrix());/*  try {products[0].getMatrix().wait();} catch(InterruptedException e){}*/}}}).start();
        for (int k=0; k<threads.length; k++) {
            threads[k].start();
        }
        
        for (int k=0; k<threads.length; k++) {
            threads[k].join();
        }
        AreThreadsRunning.set(false);
        printMatrix(c);
        System.out.println("Fine mainThread.");
        //System.out.println();
        //printMatrix(matrixProd(a, b));
    }

    public static int[][] initMatrix(int rows, int columns) {
        int matrix[][] = new int[rows][columns];
        for (int i=0; i<rows; i++){
            for (int j=0; j<columns; j++){
                matrix[i][j] = (int) (Math.random() * 10);
            }
        }
        return matrix;
    }

    public static void printMatrix(int[][] matrix){
        System.out.print("\n");
        for (int i = 0; i<matrix.length; i++){
            for (int j = 0; j<matrix[0].length; j++){ //itero sulla riga i-esima
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
    public static void initThreadProducts(RowColumnProduct[] products,
                                        int[][] a, int b[][], int c[][]){
        int i = 0;
        for (int rowA = 0; rowA<a.length; rowA++) {
            for (int colB = 0; colB<b[0].length; colB++){
                products[i] = new RowColumnProduct(a, b, rowA, colB, c);
                i++;
            }
        }
    }


    public static int[][] matrixProd(int[][] a, int[][] b){
        if(a[0].length != b.length) {
            throw new IllegalArgumentException("Il numero di colonne di A "+
                                    "non e' uguale al numero di righe di B!");
        } 
        int c[][] = new int[a.length][b[0].length];
        for(int rowA = 0; rowA<a.length; rowA++){
            for(int colB = 0; colB<b[0].length; colB++){
                int sum = 0;
                for (int colA = 0; colA < a[0].length; colA++){
                    sum = sum + a[rowA][colA] * b[colA][colB];
                }
                c[rowA][colB] = sum;
            }
        }
        return c;
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
            System.out.println(args[i]);
            if(Pattern.matches("[0-9]+,[0-9]+", args[i])){
                int rows = Integer.parseInt(args[i].split(",")[0]);
                int cols = Integer.parseInt(args[i].split(",")[1]);
                int matrix[][] = initMatrix(rows, cols);
                matrices[i] = matrix; //da gestire '0,0'

            } else {
                throw new IllegalArgumentException("Gli argomenti facoltativi "
                +"devono avere la seguente espressione [numero righe],[numero colonne]");
            }
        }
        return matrices;
    }
}
