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
 * a fixed number of threads, with a thread pool and with a cyclic barrier.
 * 
 * @author Claudio Guarrasi
 */
public class Main{
    private static AtomicBoolean areThreadsRunning = new AtomicBoolean(false);
    private static AtomicBoolean areCoreThreadsRunning = new AtomicBoolean(false);

    private static final int DIM_BOUND = 10;
    private static final int ENTRY_BOUND = 100;

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
    private static long initThreadPoolStartTime;
    private static long initThreadPoolEstimatedTime = 0;
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
        System.out.println("Durata dell'inizializzazione del gruppo di thread [ns]: " + NumberFormat.getInstance(Locale.ITALIAN).format(initThreadPoolEstimatedTime));
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
     * @return a matrix dimensioned by the paramaters passed
     */
    public static int[][] initMatrix(int rows, int columns) {
        Random randomStream = new Random();
        int matrix[][] = new int[rows][columns];
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                matrix[i][j] = randomStream.nextInt(ENTRY_BOUND);
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
     * a fixed number of threads, with a thread pool and with a cyclic barrier.
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
        int productCount = (int) matrices.length / 2;
        int matrixResults[][][] = new int[productCount][][];
        initThreadPoolStartTime = System.nanoTime();
        initThreadPool();
        initThreadPoolEstimatedTime = initThreadPoolEstimatedTime + (System.nanoTime() - initThreadPoolStartTime);
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
     * If there is just one argument representing an int number n,
     * then n matrices are initialized with dimensions equals to a
     * "random" value, but, in order to ensure a well defined matricial
     * product, the subsequent matrix in a couple has always a number
     * of rows equal to the previous one.
     * 
     * If there are k arguments with the following format '[rows],[cols]',
     * then k matrix are generated using the dimensions provided.
     * 
     * @param args
     * @return a list of matrices
     */
    public static int[][][] initMatricesFromArguments(String[] args){
        int matrixCount = args.length;
        if(args.length == 0) {
            System.out.println("Inizialiazzazione di due matrici 2x2.");
            int matrices[][][] = new int[2][][];
            /*matrices[0] = new int[][]{{1,9},{7,9},{6,1},{4,4},{4,6},{8,8},{7,7},{3,1},{0,5}};
            matrices[1] = new int[][]{{4,7,3}, {6,9,2}};*/
            matrices[0] = initMatrix(2, 2);
            matrices[1] = initMatrix(2, 2);
            return matrices;
        } else if (args.length == 1 && args[0].matches("[0-9]+")) {
                matrixCount = Integer.parseInt(args[0]);
                try {
                    if (matrixCount < 2) {
                        throw new IllegalArgumentException("numero di matrici da creare per fare almeno un prodotto non signifificativo,"
                        +"\ndeve essere maggiore di 1 se si desidera un preciso numero di matrici da inizializzare delle quali fare il prodotto.");
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println(("Errore nella gestione di '"+args[0]+"':"+
                    " "+ e.getMessage()));
                }
                int matrices[][][] = new int[matrixCount][][];
                for (int i = 0; i+1<matrixCount; i+=2) {
                    matrices[i] = getMatrixFromArg(null);
                    matrices[i+1] = initMatrix(matrices[i][0].length, (1 + new Random().nextInt(DIM_BOUND)));
                }
                return matrices;
        } else {
            int matrices[][][] = new int[args.length][][];
            for (int i = 0; i<args.length; i++) {
                matrices[i] = getMatrixFromArg(args[i]);
            }
            return matrices;
        }
    }
    
    /**
     * This method cast to int dimensions extrapolated
     * from command-line arguments.
     * 
     * @param arg
     * @return a matrix with proper dimensions
     */
    private static int[][] getMatrixFromArg(String arg) {
        try {
            String[] dimensions = getStringDimensionsFromArg(arg);
            int rows = Integer.parseInt(dimensions[0]);
            int cols = Integer.parseInt(dimensions[1]);
            if (!AreDimensionsMeaningful(rows, cols)) {
                 throw new IllegalArgumentException("dimensioni "
                +"della matrice non corrette, devono essere strettamente positive.");
            }
            return initMatrix(rows, cols);
        } catch (NumberFormatException e) {
            System.err.println("Errore nel parsing di '"+arg+"'."+
            " La stringa contiene un valore che non rappresenta un numero intero. "+ e.getMessage());
            return null;
            //System.exit(1);
        } catch (IllegalArgumentException e ) {
            System.err.println("Errore nella gestione di '"+arg+"':"+
            " "+ e.getMessage());
            return null;
            //System.exit(1);
        }
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
     * of (sub)strings according to a format.
     * 
     * If a null pointer is passed, it returns
     * a couple of dimensions "randomly" generated.
     * 
     * @param arg
     * @return
     * @throws IllegalArgumentException
     */
    public static String[] getStringDimensionsFromArg(String arg) throws IllegalArgumentException {
        String[] dimensions;
        if (arg == null) {
            Random randomStream = new Random();
            int rows = 1 + randomStream.nextInt(DIM_BOUND);
            int cols = 1 + randomStream.nextInt(DIM_BOUND);
            dimensions = new String[2];
            dimensions[0] = String.valueOf(rows);
            dimensions[1] = String.valueOf(cols);
            return dimensions;
        }
        dimensions = arg.split(",");
        if (dimensions.length != 2) {
                throw new IllegalArgumentException("formato "+
                "dell'input invalido, richiesto '[righe],[colonne]' in caso si desideri inserire almeno una matrice di dimensioni scelte.");
            }
        return dimensions;
    }

    /**
     * Cast to int the string values of the matrix
     * dimensions.
     * 
     * @param stringDimensions it is an array of strings containing at index 0 the
     * the string value of the rows, and at index 1 the string value of the columns.
     * @return
     */
    public static int[] getDimensionsFromString (String[] stringDimensions){
        int dimensions[] = new int[2];
        dimensions[0] = Integer.parseInt(stringDimensions[0]);
        dimensions[1] = Integer.parseInt(stringDimensions[1]);
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

        /**
         * The ceiling operation let me consider all the entries in the case, these are not equally
         * distributed among the threads beacuse I will consider the minimum greater integer.
         * For example if I have 4 products and three cores, 1 product for
         * each thread does not generate the product matrix entirely.
         */
        int entriesPerThread = (int) Math.ceil((double) getEntryCount(c)/(coreCount));
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
                 * an ArrayIndexOutOfBoundException would be thrown!
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
        Thread[] threads = new Thread[coreCount];
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

    /**
     * This method executes the product using a cyclic barrier.
     * The columns of the first matrix are divided among the threads.
     * In general a single row-column product cannot be completed
     * by just one thread because a thread does not have the access
     * to a whole raw. A raw is divided among the threads.
     * A thread multiply the accessible entries on its row with
     * the corresponding entries on the column of the second matrix.
     * Before passing to next column of the second matrix, it saves
     * its entry products in the addends array and then waits for
     * the other threads to finish saving the remaining entry
     * products for that row-column products.
     * So the thread waits for the others one at the barrier.
     * When all the threads come in front of the barrier, the
     * barrier action is executed and the result matrix c is written.
     * 
     * @param firstMatrix
     * @param secondMatrix
     * @param c
     * @throws InterruptedException
     */
    public static void barrierThreadMatrixProduct(int[][] firstMatrix, int[][] secondMatrix, int[][] c) throws InterruptedException{
        final int coreCount = Runtime.getRuntime().availableProcessors();
        int addends[] = new int[firstMatrix[0].length];
        Runnable barrierAction = new Runnable() {
            int rowC = 0;
            int colC = 0;
            @Override
            public void run() {
                int sum = 0;
                for (int addend : addends) {
                    sum = sum + addend;
                }
                c[rowC][colC] = sum;
                rowC = (rowC + 1) % firstMatrix.length;
                if (rowC == 0){
                    colC = (colC + 1) % secondMatrix[0].length;
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
