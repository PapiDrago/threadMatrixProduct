import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private static long threadPoolStartTime;
    private static long threadPoolEstimatedTime = 0;
    private static ExecutorService threadPool;
    public static void main(String args[]) throws InterruptedException {
        int matrices[][][] = initMatricesFromArguments(args);
        //printMatrixArray(matrices);
        @SuppressWarnings("unused")
        int productResults[][][] = product(matrices);
        System.out.println("Elenco matrici prodotto:");
        //printMatrixArray(productResults);
        while (areThreadsRunning.get() || areCoreThreadsRunning.get() || !threadPool.isShutdown()) {}
        //System.out.println("Elenco matrici prodotto thread:");
        //printMatrixArray(productResults);
        System.out.println("Durata comulativa del prodotto con un singolo thread [ns]: "+singleThreadEstimatedTime);
        System.out.println();
        System.out.println("Durata cumulativa totale del prodotto con multipli thread [ns]: "+multiThreadEstimatedTime);
        System.out.println("Durata cumulativa del prodotto con multipli thread senza contare inizializzazione [ns]: "+insideMultiThreadEstimatedTime);
        System.out.println("Durata cumulativa dell'inizializzazione dei multipli thread [ns]: "+initMultiThreadEstimatedTime);
        System.out.println();
        System.out.println("Durata cumulativa totale del prodotto con 4 thread [ns]: "+coreThreadEstimatedTime);
        System.out.println("Durata cumulativa del prodotto con 4 thread senza contare inizializzazione [ns]: "+insideCoreThreadEstimatedTime);
        System.out.println("Durata cumulativa dell'inizializzazione dei 4 thread [ns]: "+initCoreThreadEstimatedTime);
        System.out.println();
        System.out.println("Durata comulativa del prodotto con un gruppo di thread senza contare inizializzazione [ns]: "+threadPoolEstimatedTime);
    }

    public static int[][] initMatrix(int rows, int columns) {
        Random randomStream = new Random();
        int matrix[][] = new int[rows][columns];
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                matrix[i][j] = randomStream.nextInt(10);
            }
        }
        return matrix;
    }

    public static void printMatrix(int[][] matrix) {
        if (isMatrixNull(matrix)) {
            System.out.println("La matrice non è stata definita.");
        } else {
            for (int i = 0; i<matrix.length; i++) {
                for (int j = 0; j<matrix[0].length; j++) {
                    System.out.print(matrix[i][j] + " ");
                }
                System.out.println();
            }
        }
        System.out.println();
    }
    public static void createProducts(RowColumnProduct[] products,
                                        int[][] firstMatrix, int secondMatrix[][], int resultMatrix[][]){
        if (isMatrixNull(firstMatrix) || isMatrixNull(secondMatrix)){
            System.out.println("Impossibile eseguire il prodotto perche' almeno una delle matrici non è stata definita.");
        } else {
            int i = 0;
            for (int firstMatrixRow = 0; firstMatrixRow<firstMatrix.length; firstMatrixRow++) {
                for (int secondMatrixCol = 0; secondMatrixCol<secondMatrix[0].length; secondMatrixCol++){
                    products[i] = new RowColumnProduct(firstMatrix, secondMatrix, firstMatrixRow, secondMatrixCol, resultMatrix);
                    i++;
                }
            }
        }
    }


    public static void singleThreadMatrixProd(RowColumnProduct[] products){
        for (RowColumnProduct product : products) {
            product.executeRowColumnProduct();
        }
    }

    public static int[][][] product(int[][][] matrices) throws InterruptedException {
        int numProducts = (int) matrices.length / 2;
        int matrixResults[][][] = new int[numProducts][][];
        initThreadPool();
        for(int i=0; i+1<matrices.length; i +=2) {
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
            createProducts(products, firstMatrix, secondMatrix, matrixResults[i/2]);
            
            singleThreadStartTime = System.nanoTime();
            singleThreadMatrixProd(products);
            singleThreadEstimatedTime = singleThreadEstimatedTime + (System.nanoTime() - singleThreadStartTime);
            
            multiThreadStartTime = System.nanoTime();
            multiThreadMatrixProduct(firstMatrix, secondMatrix, matrixResults[i/2], products);
            multiThreadEstimatedTime = multiThreadEstimatedTime + (System.nanoTime() - multiThreadStartTime);
            
            coreThreadStartTime = System.nanoTime();
            coreThreadMatrixProduct(matrixResults[i/2], products);
            coreThreadEstimatedTime = coreThreadEstimatedTime + (System.nanoTime() - coreThreadStartTime);
            
            threadPoolStartTime = System.nanoTime();
            threadPoolMatrixProduct(products);
            threadPoolEstimatedTime = threadPoolEstimatedTime + (System.nanoTime() - threadPoolStartTime);
        }
        threadPool.shutdown();
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
        Thread[] threads = initThreads(getEntryCount(c), products);
        initMultiThreadEstimatedTime = initMultiThreadEstimatedTime + (System.nanoTime() - initMultiThreadStartTime);
        insideMultiThreadStartTime = System.nanoTime();
        startThreads(threads);
        areThreadsRunning.set(true);
        waitForThreadsToDie(threads);
        insideMultiThreadEstimatedTime = insideMultiThreadEstimatedTime + (System.nanoTime() - insideMultiThreadStartTime);
        areThreadsRunning.set(false);
    }
    public static void coreThreadMatrixProduct(int[][] c, RowColumnProduct[] products) throws InterruptedException {
        int coreCount = Runtime.getRuntime().availableProcessors();
        int entriesPerThread = (int) Math.ceil((double) getEntryCount(c)/(coreCount)); //mi permette di considerare
                                                                //tutte le entry nel caso esse non siano perfettamente distribuite su ogni thread: es 4 prodotti e 3 core, 1 prodotto per ciascun thread non mi genera
                                                                //completamente la matrice prodotto. Arrotondo all'intero successivo più piccolo per essere sicuro di
                                                                //poter fare i prodotti necessari. Tuttavia devo limitare il numero di prodotti
                                                                //allo stretto necessario perche' rischio di accedere a un elemento non definito.
        Runnable runnable = new Runnable() {
            private static AtomicInteger group = new AtomicInteger(-1); //sono sicuro che si potrebbe evitare di usarla.
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
                }
            }
        };
        initCoreThreadStartTime = System.nanoTime();
        Thread[] threads = initThreads(coreCount, products);
        for(int k = 0; k<threads.length; k++){
            threads[k] = new Thread(runnable);
        }
        initCoreThreadEstimatedTime = initCoreThreadEstimatedTime + (System.nanoTime() - initCoreThreadStartTime);
        insideCoreThreadStartTime = System.nanoTime();
        startThreads(threads);
        areCoreThreadsRunning.set(true);
        waitForThreadsToDie(threads);
        insideCoreThreadEstimatedTime = insideCoreThreadEstimatedTime + (System.nanoTime() - insideCoreThreadStartTime);
        areCoreThreadsRunning.set(false);
    }
    /**
     * @param firstMatrix
     * @param secondMatrix
     * @param c
     * @param products
     * @throws InterruptedException
     */
    public static void coreThreadMatrixProductV2(int[][] c, RowColumnProduct[] products) throws InterruptedException {
        int coreCount = Runtime.getRuntime().availableProcessors()-1;
        int entriesPerThread = (int) Math.ceil((double) getEntryCount(c)/(coreCount));
        
        initMultiThreadStartTime = System.nanoTime();
        Thread[] threads = new Thread[coreCount];
        for (int i = 0; i<threads.length; i++) {
            final int bound = i;
            threads[i] = new Thread(() -> {
                int start = bound * entriesPerThread;
                int end = Math.min(start + entriesPerThread, products.length);
                for(int k = start; k<end; k++) {
                    products[k].executeRowColumnProduct();
                }
            }
            );
            
        }
        initMultiThreadEstimatedTime = System.nanoTime() - initMultiThreadStartTime;
        insideMultiThreadStartTime = System.nanoTime();
        startThreads(threads);
        areThreadsRunning.set(true);
        waitForThreadsToDie(threads);
        insideMultiThreadEstimatedTime = System.nanoTime() - insideMultiThreadStartTime;
        areThreadsRunning.set(false);
    }
    public static Thread[] initThreads(int n, Runnable[] runnables) {
        Thread[] threads = new Thread[n];
        for(int k = 0; k<n; k++){
            threads[k] = new Thread(runnables[k]);
        }
        return threads;
    }
    public static void startThreads(Thread[] threads){
        for (int k=0; k<threads.length; k++) {
            threads[k].start();
        }
    }
    public static void waitForThreadsToDie(Thread[] threads) throws InterruptedException {
        for (int k=0; k<threads.length; k++) {
            threads[k].join();
        }
    }
    public static void initThreadPool() {
        if (threadPool == null){
            int coreCount = Runtime.getRuntime().availableProcessors();
            threadPool = Executors.newFixedThreadPool(coreCount);
        }
    }

    public static void threadPoolMatrixProduct(RowColumnProduct[] products) throws InterruptedException {
        for (RowColumnProduct product : products) {
            threadPool.execute(product);
        }  
    }
}
