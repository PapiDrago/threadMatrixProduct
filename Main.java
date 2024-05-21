import java.util.Random;
//import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main{
    private static AtomicBoolean areThreadsRunning = new AtomicBoolean(false);
    private static AtomicBoolean areCoreThreadsRunning = new AtomicBoolean(false);
    private static long singleThreadStartTime;
    private static long singleThreadEstimatedTime = 0;
    private static long multiThreadStartTime;
    private static long multiThreadEstimatedTime = 0;
    private static long insideMultiThreadStartTime;
    private static long insideMultiThreadEstimatedTime = 0;
    private static long initMultiThreadStartTime;
    private static long initMultiThreadEstimatedTime = 0;
    private static long coreThreadStartTime;
    private static long coreThreadEstimatedTime = 0;
    private static long initCoreThreadStartTime;
    private static long initCoreThreadEstimatedTime = 0;
    private static long insideCoreThreadStartTime;
    private static long insideCoreThreadEstimatedTime = 0;
    public static void main(String args[]) throws InterruptedException {
        /*SynchronizedMatrix firstMatrix = new SynchronizedMatrix(2, 2);
        SynchronizedMatrix secondMatrix = new SynchronizedMatrix(2, 2);
        firstMatrix.printMatrix();
        secondMatrix.printMatrix();
        SynchronizedMatrix c = firstMatrix.matrixProd(secondMatrix.getMatrix());
        c.printMatrix();*/
        int matrices[][][] = initMatricesFromArguments(args);
        //printMatrixArray(matrices);
        int productResults[][][] = product(matrices);
        System.out.println("Elenco matrici prodotto:");
        //printMatrixArray(productResults);
        while (areThreadsRunning.get() || areCoreThreadsRunning.get()) {}
        System.out.println("Elenco matrici prodotto thread:");
        //printMatrixArray(productResults);
        System.out.println("Durata comulativo del prodotto con un singolo thread singolo [ns]: "+singleThreadEstimatedTime);
        System.out.println();
        System.out.println("Durata cumulativa totale del prodotto con multipli thread [ns]: "+multiThreadEstimatedTime);
        System.out.println("Durata cumulativa del prodotto con multipli thread senza contare inizializzazione [ns]: "+insideMultiThreadEstimatedTime);
        System.out.println("Durata cumulativa dell'inizializzazione dei multipli thread [ns]: "+initMultiThreadEstimatedTime);
        System.out.println();
        System.out.println("Durata cumulativa totale del prodotto con 4 thread [ns]: "+coreThreadEstimatedTime);
        System.out.println("Durata cumulativa del prodotto con 4 thread senza contare inizializzazione [ns]: "+insideCoreThreadEstimatedTime);
        System.out.println("Durata cumulativa dell'inizializzazione dei 4 thread [ns]: "+initCoreThreadEstimatedTime);
        System.out.println();
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
        if (isMatrixNull(matrix)) {
            System.out.println("La matrice non è stata definita.");
        } else {
            //System.out.print("\n");
            for (int i = 0; i<matrix.length; i++){
                for (int j = 0; j<matrix[0].length; j++){ //itero sulla riga i-esima
                    System.out.print(matrix[i][j] + " ");
                }
                System.out.println();
            }
        }
        System.out.println();
    }
    public static void initProducts(RowColumnProduct[] products,
                                        int[][] firstMatrix, int secondMatrix[][], int c[][]){
        if (isMatrixNull(firstMatrix) || isMatrixNull(secondMatrix)){
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


    public static int[][] singleThreadMatrixProd(int[][] firstMatrix, int[][] secondMatrix, int[][] c, RowColumnProduct[] products){
        for (RowColumnProduct product : products) {
            product.executeRowColumnProduct();
        }
        /*for(int firstMatrixRow = 0; firstMatrixRow<firstMatrix.length; firstMatrixRow++){
            for(int secondMatrixCol = 0; secondMatrixCol<secondMatrix[0].length; secondMatrixCol++){
                int sum = 0;
                //product.execute();
                /*for (int firstMatrixCol = 0; firstMatrixCol < firstMatrix[0].length; firstMatrixCol++){
                    sum = sum + firstMatrix[firstMatrixRow][firstMatrixCol] * secondMatrix[firstMatrixCol][secondMatrixCol];
                }
                c[firstMatrixRow][secondMatrixCol] = sum;
            }
        }*/
        return c;
    }

    public static int[][][] product(int[][][] matrices) throws InterruptedException {
        int numProducts = (int) matrices.length / 2;
        int matrixResults[][][] = new int[numProducts][][];
        for(int i=0; i+1<matrices.length; i +=2){
            int[][] firstMatrix = matrices[i];
            int[][] secondMatrix = matrices[i+1];
            //printMatrix(firstMatrix);
            //printMatrix(secondMatrix);
            if(isMatrixNull(firstMatrix) || isMatrixNull(secondMatrix) || !RowColumnProduct.isWellDefined(firstMatrix, secondMatrix)){
                matrixResults[i/2] = null;
                continue;
            }
            matrixResults[i/2] = new int[firstMatrix.length][secondMatrix[0].length];
            RowColumnProduct products[] = new RowColumnProduct[getEntryCount(matrixResults[i/2])];
            initProducts(products, firstMatrix, secondMatrix, matrixResults[i/2]);
            
            singleThreadStartTime = System.nanoTime();
            singleThreadMatrixProd(firstMatrix, secondMatrix, matrixResults[i/2], products);
            singleThreadEstimatedTime = singleThreadEstimatedTime + (System.nanoTime() - singleThreadStartTime);
            
            multiThreadStartTime = System.nanoTime();
            multiThreadMatrixProduct(firstMatrix, secondMatrix, matrixResults[i/2], products);
            multiThreadEstimatedTime = multiThreadEstimatedTime + (System.nanoTime() - multiThreadStartTime);
            
            coreThreadStartTime = System.nanoTime();
            coreThreadMatrixProduct(firstMatrix, secondMatrix, matrixResults[i/2], products);
            coreThreadEstimatedTime = coreThreadEstimatedTime + (System.nanoTime() - coreThreadStartTime);
            /*System.out.println("Durata del prodotto "+i/2+"-esimo "+"con un thread singolo [ns]: "+singleThreadEstimatedTime);
            System.out.println("Durata del prodotto "+i/2+"-esimo "+"con multipli thread [ns]: "+multiThreadEstimatedTime);
            System.out.println("Durata del prodotto "+i/2+"-esimo "+"con multipli thread senza contare inizializzazione [ns]: "+insideMultiThreadEstimatedTime);
            System.out.println("Durata dell'inizializzazione dei thread per il prodotto "+i/2+"-esimo [ns]: "+initMultiThreadEstimatedTime);
            /*Thread[] threads = new Thread[getEntryCount(c)];0
            RowColumnProduct products[] = new RowColumnProduct[getEntryCount(c)];
            initProducts(products, firstMatrix, secondMatrix, c);
            for(int k = 0; k<threads.length; k++){
                threads[k] = new Thread(products[k]);
            }
            
            for (int k=0; k<threads.length; k++) {
                threads[k].start();
            }
            for (int k=0; k<threads.length; k++) {
                threads[k].join();
            }
            System.out.println(i);*/

        }
        return matrixResults;
    }

    public static int[][][] initMatricesFromArguments(String[] args){
        if(args.length == 0) {
            System.out.println("Inizialiazzazione di due matrici 2x2.");
            int matrices[][][] = new int[2][][];
            /*matrices[0] = new int[][]{{1,9},{7,9},{6,1},{4,4},{4,6},{8,8},{7,7},{3,1},{0,5}};
            matrices[1] = new int[][]{{4,7,3}, {6,9,2}};*/
            matrices[0] = initMatrix(2, 2);
            matrices[1] = initMatrix(2, 2);
            return matrices;
        }
        int matrices[][][] = new int[args.length][][];
        for (int i = 0; i<args.length; i++){
            try {
                String[] dimensions = getDimensionsFromArg(args[i]);
                int rows = Integer.parseInt(dimensions[0]);
                int cols = Integer.parseInt(dimensions[1]);
                if (!AreDimensionsMeaningful(rows, cols)) {
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
    public static boolean isMatrixNull(int[][] matrix){
        return matrix == null;
    }

    public static int getEntryCount(int[][] matrix) {
        return matrix.length*matrix[0].length;
    }

    public static void printMatrixArray(int[][][] matrixArray) {

        for (int i = 0; i<matrixArray.length; i++) {
            System.out.println("Matrice n"+i);
            printMatrix(matrixArray[i]);
        }
    }

    public static String[] getDimensionsFromArg(String arg) throws IllegalArgumentException {
        String[] dimensions = arg.split(",");
        if (dimensions.length != 2) {
                throw new IllegalArgumentException("formato "+
                "dell'input invalido, richiesto '[righe],[colonne]'.");
            }
        return dimensions;
    }
    public static boolean AreDimensionsMeaningful(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            return false;
        }
        return true;
    }
    public static void multiThreadMatrixProduct(int[][] firstMatrix,
                                int[][] secondMatrix, int[][] c, RowColumnProduct[] products) throws InterruptedException {
        initMultiThreadStartTime = System.nanoTime();
        Thread[] threads = new Thread[getEntryCount(c)];
        for(int k = 0; k<threads.length; k++){
            threads[k] = new Thread(products[k]);
        }
        initMultiThreadEstimatedTime = initMultiThreadEstimatedTime + (System.nanoTime() - initMultiThreadStartTime);
        insideMultiThreadStartTime = System.nanoTime();
        for (int k=0; k<threads.length; k++) {
            threads[k].start();
        }
        areThreadsRunning.set(true);
        for (int k=0; k<threads.length; k++) {
            threads[k].join();
        }
        insideMultiThreadEstimatedTime = insideMultiThreadEstimatedTime + (System.nanoTime() - insideMultiThreadStartTime);
        areThreadsRunning.set(false);
    }
    public static void coreThreadMatrixProduct(int[][] firstMatrix,
                                int[][] secondMatrix, int[][] c, RowColumnProduct[] products) throws InterruptedException {
        int coreCount = Runtime.getRuntime().availableProcessors();
        int entriesPerThread = (int) Math.ceil((double) getEntryCount(c)/(coreCount)); //mi permette di considerare
                                                                //tutte le entry nel caso esse non siano perfettamente distribuite su ogni thread: es 4 prodotti e 3 core, 1 prodotto per ciascun thread non mi porta
                                                                //completamente la matrice prodotto. Arrotondo all'intero successivo più piccolo per essere sicuro di
                                                                //poter fare i prodotti necessari. Tuttavia devo limitare il poi il numero di prodotti
                                                                //allo stretto necessario perche' rischio di accedere un elemento non definito.
        //System.out.println("groups ="+entriesPerThread+" getEntryCount(c)="+getEntryCount(c)+" core="+(coreCount));
        Runnable runnable = new Runnable() {
            private static AtomicInteger group = new AtomicInteger(-1);
            ThreadLocal<Integer> localGroup = new ThreadLocal<>() {
                    @Override protected Integer initialValue() {
                        return group.incrementAndGet();    
                }
            };
            @Override
            public void run() {
                int bound = localGroup.get();
                int start = bound * entriesPerThread;
                int end = Math.min(start + entriesPerThread, products.length);
                //System.out.println("Gruppo locale: "+bound);
                for(int i = start; i<end; i++) {
                    products[i].executeRowColumnProduct();
                    //System.out.println(Thread.currentThread().getName() +products[i].toString());
                }



                /*for (int rowA = 0; rowA < firstMatrix.length; rowA++){
                    for (int colB = 0; colB < secondMatrix[0].length; colB++) {
                        synchronized(c){
                            if(c[rowA][colB] == 0){
                                continue;
                            } else {
                                int sum = 0;
                                for (int colA = 0; colA < firstMatrix[0].length; colA++){
                                    sum = sum + firstMatrix[rowA][colA] * secondMatrix[colA][colB];
                                }
                                c[rowA][colB] = sum; 
                            }
                        }
                    }
                }*/

            }
        };
        initCoreThreadStartTime = System.nanoTime();
        Thread[] threads = new Thread[coreCount];
        for(int k = 0; k<threads.length; k++){
            threads[k] = new Thread(runnable);
        }
        initCoreThreadEstimatedTime = initCoreThreadEstimatedTime + (System.nanoTime() - initCoreThreadStartTime);
        insideCoreThreadStartTime = System.nanoTime();
        for (int k=0; k<threads.length; k++) {
            threads[k].start();
        }
        areCoreThreadsRunning.set(true);
        for (int k=0; k<threads.length; k++) {
            threads[k].join();
        }
        insideCoreThreadEstimatedTime = insideCoreThreadEstimatedTime + (System.nanoTime() - insideCoreThreadStartTime);
        areCoreThreadsRunning.set(false);
    }
    /*public static void coreMinusThreadMatrixProduct(int[][] firstMatrix,
                                int[][] secondMatrix, int[][] c, RowColumnProduct[] products) throws InterruptedException {
        int coreCount = Runtime.getRuntime().availableProcessors()-1;
        int entriesPerThread = (int) Math.ceil((double) getEntryCount(c)/(coreCount)); //mi permette di considerare
                                                                //tutte le entry nel caso esse non siano perfettamente distribuite su ogni thread: es 4 prodotti e 3 core, 1 prodotto per ciascun thread non mi porta
                                                                //completamente la matrice prodotto. Arrotondo all'intero successivo più piccolo per essere sicuro di
                                                                //poter fare i prodotti necessari. Tuttavia devo limitare il poi il numero di prodotti
                                                                //allo stretto necessario perche' rischio di accedere un elemento non definito.
        System.out.println("groups ="+entriesPerThread+" getEntryCount(c)="+getEntryCount(c)+" core="+(coreCount));
        Runnable runnable = new Runnable() {
            private static AtomicInteger group = new AtomicInteger(-1);
            ThreadLocal<Integer> localGroup = new ThreadLocal<>() {
                    @Override protected Integer initialValue() {
                        return group.incrementAndGet();    
                }
            };
            @Override
            public void run() {
                int bound = localGroup.get();
                int start = bound * entriesPerThread;
                int end = Math.min(start + entriesPerThread, products.length);
                System.out.println("Gruppo locale: "+bound);
                for(int i = start; i<end; i++) {
                    products[i].executeRowColumnProduct();
                    System.out.println(Thread.currentThread().getName() +products[i].toString());
                }
            }
        };
        initMultiThreadStartTime = System.nanoTime();
        Thread[] threads = new Thread[coreCount];
        for(int k = 0; k<threads.length; k++){
            threads[k] = new Thread(runnable);
        }
        initMultiThreadEstimatedTime = System.nanoTime() - initMultiThreadStartTime;
        insideMultiThreadStartTime = System.nanoTime();
        for (int k=0; k<threads.length; k++) {
            threads[k].start();
        }
        areThreadsRunning.set(true);
        for (int k=0; k<threads.length; k++) {
            threads[k].join();
        }
        insideMultiThreadEstimatedTime = System.nanoTime() - insideMultiThreadStartTime;
        areThreadsRunning.set(false);
    }*/

}
