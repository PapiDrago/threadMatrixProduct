/**
 * This class models the concept of a row-column product.
 * A row-column product is an entry of the matrix resulting
 * from the matricial product between two matrices.
 * Given C as the resulting matrix, the its Cij entry is defined
 * as the summation of the products between the entries of the first
 * matrix on the i-th row and the corresponding entries of the
 * second matrix on the j-th column.
 * 
 * @author Claudio Guarrasi
 */
public class RowColumnProduct implements Runnable {
    
    /**
     * In order to obtain an entry for the resulting matrix
     * it is necessary to multiply the entries from 
     * a row of the first matrix with the matching entries
     * from a column of the second matrix.
     * That is possible only if the number of rows
     * of the first matrix is equals to number of columns
     * of the second matrix.
     * 
     * @param firstMatrix
     * @param secondMatrix
     * @return true if the number of rows
     * of the first matrix is equals to number of columns
     * of the second matrix, false otherwise
     */
    public static boolean isWellDefined(int[][] firstMatrix, int[][] secondMatrix) {
        if(firstMatrix[0].length != secondMatrix.length){
            System.out.println("Il prodotto tra le due matrici precedentemente stampate non e' ben definito.");
            return false;
        }
        return true;
    }
    private int nRow;
    private int nCol;
    private int product;
    private int firstMatrix[][];
    private int secondMatrix[][];
    
    private /*volatile*/ int matrix[][];
   
    /**
     * Constructor method. It initializes what is needed to
     * compute a single row-column product
     * 
     * @param firstMatrix
     * @param secondMatrix
     * @param nRow
     * @param nCol
     * @param matrix
     */
    public RowColumnProduct(int firstMatrix[][], int secondMatrix[][], int nRow,
                        int nCol, int[][] matrix) {
        super();
        this.firstMatrix = firstMatrix;
        this.secondMatrix = secondMatrix;
        this.nRow = nRow;
        this.nCol = nCol;
        this.matrix = matrix;
        
    }
    /**
     * When an istance of RowColumnProduct class is passed to
     * a thread, this one executes a single row-column product.
     */
    @Override
    public void run() {
        executeRowColumnProduct();
        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {}*/
        //System.out.println(matrix);
        //System.out.println(Thread.currentThread().getName());
        
           
    }

    /**
     * @return the result matrix containing this row-column product
     */
    public int[][] getMatrix() {
        return matrix;
    }
    
    /**
     * @return the summation of the products
     */
    public int getProduct() {
        return product;
    }
    
    /**
     * This mehod executes the row-column product in the following fashion:
     * Cij = summation of Aik*Bkj, for k from 0 to the number of the
     * columns of the first matrix 'A' (or to the number of the rows
     * of the second maytrix 'B').
     */
    public void executeRowColumnProduct() {
        int sum = 0;
        for (int k = 0; k < this.firstMatrix[0].length; k++){
            sum = sum + this.firstMatrix[nRow][k] * this.secondMatrix[k][nCol];
        }
        //synchronized(this.matrix){
            this.matrix[nRow][nCol] = sum;
            //System.out.println(Thread.currentThread().getName());
            //this.matrix.notify();
       // }
    }
    /**
     * @return what is essential to know to identify a row-column
     * product
     */
    @Override
    public String toString() {
        String string = "riga di A: " + this.nRow + " colonna di B: " + this.nCol;
        return string;
    }
    
    
}
