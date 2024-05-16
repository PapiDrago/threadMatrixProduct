public class RowColumnProduct implements Runnable {
    private int nRow;
    private int nCol;
    private int product;
    private int a[][];
    private int b[][];
    private /*volatile*/ int matrix[][];
    
    public RowColumnProduct(int a[][], int b[][], int nRow,
                        int nCol, int[][] matrix) {
        super();
        this.a = a;
        this.b = b;
        this.nRow = nRow;
        this.nCol = nCol;
        this.matrix = matrix;
        
    }
   
    @Override
    public void run() {
        int sum = 0;
        for (int i = 0; i < this.a[0].length; i++){
            sum = sum + this.a[nRow][i] * this.b[i][nCol];
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
    
    public static boolean isWellDefined(int[][] a, int[][] b) {
        if(a[0].length != b.length){
            System.out.println("Il prodotto tra le due matrici precedentemente stampate non e' ben definito.");
            return false;
        }
        return true;
    }
    
}
