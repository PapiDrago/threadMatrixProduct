import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * This class provides an example of logic to compare the performances
 * (i.e. execution times) of different ways to do a matricial
 * product pertaining the use of threads.
 * These different approaches are: with a single thread, with multiple threads, with
 * a fixed number of threads, and with a thread pool.
 * 
 * @author Claudio Guarrasi
 */
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
    private static long barrierStartTime = 0;
    private static long barrierEstimatedTime;
    
    private static ExecutorService threadPool;
    private static CyclicBarrier barrier;
    
    /**
     * The entry point of the program.
     * 
     * @param args
     * @throws InterruptedException
     */
    public static void main(String args[]) throws InterruptedException {
        int matrices[][][] = initMatricesFromArguments(args);
        printMatrixArray(matrices);
        @SuppressWarnings("unused")
        int productResults[][][] = product(matrices);
        System.out.println("Elenco matrici prodotto:");
        printMatrixArray(productResults);
        while (areThreadsRunning.get() || areCoreThreadsRunning.get() || !threadPool.isShutdown()) {}
        System.out.println("Elenco matrici prodotto thread:");
        printMatrixArray(productResults);
        
        System.out.println("Durata comulativa del prodotto con un singolo thread [ns]: "+NumberFormat.getInstance(Locale.ITALIAN).format(singleThreadEstimatedTime));
        System.out.println();
        System.out.println("Durata cumulativa totale del prodotto con multipli thread [ns]: "+NumberFormat.getInstance(Locale.ITALIAN).format(multiThreadEstimatedTime));
        System.out.println("Durata cumulativa del prodotto con multipli thread senza contare inizializzazione [ns]: "+NumberFormat.getInstance(Locale.ITALIAN).format(insideMultiThreadEstimatedTime));
        System.out.println("Durata cumulativa dell'inizializzazione dei multipli thread [ns]: "+NumberFormat.getInstance(Locale.ITALIAN).format(initMultiThreadEstimatedTime));
        System.out.println();
        System.out.println("Durata cumulativa totale del prodotto con 4 thread [ns]: "+NumberFormat.getInstance(Locale.ITALIAN).format(coreThreadEstimatedTime));
        System.out.println("Durata cumulativa del prodotto con 4 thread senza contare inizializzazione [ns]: "+NumberFormat.getInstance(Locale.ITALIAN).format(insideCoreThreadEstimatedTime));
        System.out.println("Durata cumulativa dell'inizializzazione dei 4 thread [ns]: "+ NumberFormat.getInstance(Locale.ITALIAN).format(initCoreThreadEstimatedTime));
        System.out.println();
        System.out.println("Durata comulativa del prodotto con un gruppo di thread senza contare inizializzazione [ns]: "+ NumberFormat.getInstance(Locale.ITALIAN).format(threadPoolEstimatedTime));
        System.out.println();
        System.out.println("Durata comulativa del prodotto con 4 thread e una barriera [ns]: "+ NumberFormat.getInstance(Locale.ITALIAN).format(barrierEstimatedTime));
    }

    /**
     * Returns a matrix defined by the dimensions passed as
     * arguments and formed by entries, whose values are
     * pseudo-random generated. 
     * 
     * @param rows
     * @param columns
     * @return
     */
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

    /**
     * Prints the entries of the matrix passed as argument.
     * 
     * @param matrix
     */
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
    
    /**
     * Creates the elements of a RowColumnProductArray.
     * Each array corresponds to a matricial product.
     * Each element of the array corresponds to an entry of the
     * matrix obtained from the product.
     * To obtain the entry Cij of the resulting matrix,
     * are needed the i-th row of the first matrix
     * and the j-th column of the second matrix.
     * 
     * @param products
     * @param firstMatrix
     * @param secondMatrix
     * @param resultMatrix
     */
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


    /**
     * As the name says, a matricial product is carried out, entirely,
     * by a single thread.
     * 
     * @param products
     */
    public static void singleThreadMatrixProd(RowColumnProduct[] products){
        for (RowColumnProduct product : products) {
            product.executeRowColumnProduct();
        }
    }

    /**
     * This method executes all the products available.
     * For each couple of matrices in 'matrices' 3D array there
     * is a product. That product is then carried out in different
     * ways: with a single thread, with multiple threads, with
     * a fixed number of threads, and with a thread pool.
     * That is done to compare the execution times of the
     * different approaches.
     * If the number of matrices in 'matrices' 3D array is uneven,
     * the matrix contained in the last array position is not used.
     * 
     * @param matrices
     * @return the array containing the matrices generated from the products
     * @throws InterruptedException
     */
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

            barrierStartTime = System.nanoTime();
            barrierThreadMatrixProduct(firstMatrix, secondMatrix, matrixResults[i/2]);
            barrierEstimatedTime = barrierEstimatedTime + (System.nanoTime() - barrierStartTime);
        }
        threadPool.shutdown();
        return matrixResults;
    }

    /**
     * Initializes matrices according to command-line arguments.
     * Returns an array of matrices.
     * @param args
     * @return matrices
     */
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
    
    /**
     * Check if a matrix has been initialized.
     * 
     * @param matrix
     * @return
     */
    public static boolean isMatrixNull(int[][] matrix){
        return matrix == null;
    }

    /**
     * Returns the entries number of a matrix.
     * It is equal to the product between the number of the rows
     * and the number of the columns.
     * 
     * @param matrix
     * @return
     */
    public static int getEntryCount(int[][] matrix) {
        return matrix.length*matrix[0].length;
    }

    /**
     * Prints the matrices contained in the array of matrices passed as argument.
     * @param matrixArray
     */
    public static void printMatrixArray(int[][][] matrixArray) {
        for (int i = 0; i<matrixArray.length; i++) {
            System.out.println("Matrice n"+i);
            printMatrix(matrixArray[i]);
        }
    }

    /**
     * Extrapolates from the 'arg' string a couple
     * of (sub)strings according a format.
     * If
     * 
     * @param arg
     * @return
     * @throws IllegalArgumentException
     */
    public static String[] getDimensionsFromArg(String arg) throws IllegalArgumentException {
        String[] dimensions = arg.split(",");
        if (dimensions.length != 2) {
                throw new IllegalArgumentException("formato "+
                "dell'input invalido, richiesto '[righe],[colonne]'.");
            }
        return dimensions;
    }
    
    /**
     * Checks if the number provided are valid dimension values.
     * 
     * @param rows
     * @param cols
     * @return
     */
    public static boolean AreDimensionsMeaningful(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            return false;
        }
        return true;
    }
    
    /**
     * Executes the matricial product by generating a thread for each
     * of the row column products needed to get the resulting matrix c.
     * 
     * @param firstMatrix
     * @param secondMatrix
     * @param c
     * @param products
     * @throws InterruptedException
     */
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
    
    /**
     * Multiplicates two matrices using a number of threads equals
     * to the number of cores.
     * 
     * @param c
     * @param products
     * @throws InterruptedException
     */
    public static void coreThreadMatrixProduct(int[][] c, RowColumnProduct[] products) throws InterruptedException {
        int coreCount = Runtime.getRuntime().availableProcessors();
        int entriesPerThread = (int) Math.ceil((double) getEntryCount(c)/(coreCount)); //mi permette di considerare
                                                                //tutte le entry nel caso esse non siano perfettamente distribuite su ogni thread: es 4 prodotti e 3 core, 1 prodotto per ciascun thread non mi genera
                                                                //completamente la matrice prodotto. Arrotondo all'intero successivo più piccolo per essere sicuro di
                                                                //poter fare i prodotti necessari. Tuttavia devo limitare il numero di prodotti
                                                                //allo stretto necessario perche' rischio di accedere a un elemento non definito.
        Runnable runnable = new Runnable() {
            
            /*
             * At each thread is assigned a group of products.
             * Each group of product count entriesPerThread products.
             * In practice, the group variable will be enveloped in
             * a ThreadLocal object. It starts from -1 in order to
             * let the running threads reading in a synchronized way
             * only matching non-negative value starting from 0.
             */
            private static AtomicInteger group = new AtomicInteger(-1);

            /*
             * Each running thread gets atomically a particular value of
             * the static group variable at the instantiation of a ThreadLocal object.
             */
            ThreadLocal<Integer> localGroup = new ThreadLocal<>() {
                    @Override protected Integer initialValue() {
                        return group.incrementAndGet();    
                }
            };
            @Override
            public void run() {
                int bound = localGroup.get();
                int start = bound * entriesPerThread;
                /*
                 * The value assigned to 'end' is the minimum because
                 * if the sum of all the products to execute by each thread
                 * is greater than the actual number of products, then
                 * an ArrayIndexOutOfBoundException woul be thrown!
                 * In that case products.length value is assigned instead.
                 */
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
     * Multiplicates two matrices using a number of threads equals
     * to the number of cores. In this method the thread local
     * variables are not used.
     * 
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
            
            /*
             * Lets the inner for loop to start from the right
             * position in order to avoid that a running threads
             * write at the same memory address of other ones.
             * It is declared final because the compiler needs
             * to capture the variable in order to use it in
             * the lambda expression of the Runnable object.
             */
            final int bound = i;
            threads[i] = new Thread(() -> {
                int start = bound * entriesPerThread;

                /*
                 * The value assigned to 'end' is the minimum because
                 * if the sum of all the products to execute by each thread
                 * is greater than the actual number of products, then
                 * an ArrayIndexOutOfBoundException woul be thrown!
                 * In that case products.length value is assigned instead.
                 */
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
    
    /**
     * A general method to initialize threads in a vector, given
     * an array of Runnable objects.
     * 
     * @param n
     * @param runnables
     * @return
     */
    public static Thread[] initThreads(int n, Runnable[] runnables) {
        Thread[] threads = new Thread[n];
        for(int k = 0; k<n; k++){
            threads[k] = new Thread(runnables[k]);
        }
        return threads;
    }
    
    /**
     * A general method to start an array of threads.
     * 
     * @param threads
     */
    public static void startThreads(Thread[] threads){
        for (int k=0; k<threads.length; k++) {
            threads[k].start();
        }
    }
    
    /**
     * Joins an array of running threads.
     * 
     * @param threads
     * @throws InterruptedException
     */
    public static void waitForThreadsToDie(Thread[] threads) throws InterruptedException {
        for (int k=0; k<threads.length; k++) {
            threads[k].join();
        }
    }
    
    /**
     * Initialize a thread pool formed by a number of threads
     * equals to the number of cores.
     * 
     */
    public static void initThreadPool() {
        if (threadPool == null){
            int coreCount = Runtime.getRuntime().availableProcessors();
            threadPool = Executors.newFixedThreadPool(coreCount);
        }
    }

    /**
     * Executes a matricial product using a thread pool
     * and an array of RowColumnProduct objects.
     * 
     * @param products
     * @throws InterruptedException
     */
    public static void threadPoolMatrixProduct(RowColumnProduct[] products) throws InterruptedException {
        for (RowColumnProduct product : products) {
            threadPool.execute(product);
        }  
    }

    public static void barrierThreadMatrixProduct(int[][] firstMatrix, int[][] secondMatrix, int[][] c) throws InterruptedException{
        final int coreCount = Runtime.getRuntime().availableProcessors();
        int addends[] = new int[firstMatrix[0].length];
        Runnable barrierAction = new Runnable() {
            int i = 0;
            int j = 0;
            @Override
            public void run() {
                int sum = 0;
                for (int addend : addends) {
                    sum = sum + addend;
                }
                c[i][j] = sum;
                i = (i + 1) % firstMatrix.length;
                if (i == 0){
                    j = (j + 1) % secondMatrix[0].length;
                }
            } 
        };
        barrier = new CyclicBarrier(coreCount, barrierAction);
        Thread threads[] = new Thread[coreCount];
        int colsPerThread = (int) Math.ceil((double) firstMatrix.length / coreCount);
        for (int i = 0; i<threads.length; i++) {
            final int bound = i;
            threads[i] = new Thread(() -> {
                int startCol = bound * colsPerThread;
                int endCol = Math.min(startCol + colsPerThread, firstMatrix[0].length);
                for(int colB = 0; colB<secondMatrix[0].length; colB++){
                    for(int rowA = 0; rowA<firstMatrix.length; rowA++){
                        for(int colA = startCol; colA<endCol; colA++){
                            addends[colA] = firstMatrix[rowA][colA]*secondMatrix[colA][colB];
                        }
                        try {
                            barrier.await();
                        } catch (InterruptedException e) {
                            return;
                        } catch (BrokenBarrierException e) {
                            return;
                        }  
                    }
                } 
            });
        }
        startThreads(threads);
        areThreadsRunning.set(true);
        waitForThreadsToDie(threads);
        areThreadsRunning.set(false);
    }
}
