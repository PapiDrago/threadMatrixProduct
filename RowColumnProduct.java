public class RowColumnProduct implements Runnable {
    private int nRow;
    private int nCol;
    private int product;
    private int a[][];
    private int b[][];
    private int matrix[][];
    
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
        for (int i = 0; i < this.a.length; i++){
            sum = sum + this.a[nRow][i] * this.b[i][nCol];
        }
        this.matrix[nRow][nCol] = sum;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
           
    }
    
    public int getProduct() {
        return product;
    }
    
    
}
