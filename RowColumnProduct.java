public class RowColumnProduct implements Runnable {
    private int nRow;
    private int nCol;
    private int product;
    private int firstMatrix[][];
    private int secondMatrix[][];
    private /*volatile*/ int matrix[][];
    
    public RowColumnProduct(int firstMatrix[][], int secondMatrix[][], int nRow,
                        int nCol, int[][] matrix) {
        super();
        this.firstMatrix = firstMatrix;
        this.secondMatrix = secondMatrix;
        this.nRow = nRow;
        this.nCol = nCol;
        this.matrix = matrix;
        
    }
   
    @Override
    public void run() {
        int sum = 0;
        for (int i = 0; i < this.firstMatrix[0].length; i++){
            sum = sum + this.firstMatrix[nRow][i] * this.secondMatrix[i][nCol];
        }
        synchronized(this.matrix){
            this.matrix[nRow][nCol] = sum;
            //System.out.println(Thread.currentThread().getName());
            //this.matrix.notify();
        }
        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {}*/
        //System.out.println(matrix);
        //System.out.println(Thread.currentThread().getName());
        
           
    }
    
    
    public int[][] getMatrix() {
        return matrix;
    }

    public int getProduct() {
        return product;
    }
    
    public static boolean isWellDefined(int[][] firstMatrix, int[][] secondMatrix) {
        if(firstMatrix[0].length != secondMatrix.length){
            System.out.println("Il prodotto tra le due matrici precedentemente stampate non e' ben definito.");
            return false;
        }
        return true;
    }
    
}
