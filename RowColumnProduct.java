public class RowColumnProduct implements Runnable {
    private int row[];
    private int col[];
    private int product;
    public RowColumnProduct(int row[], int column[]) {
        super();
        this.row = row;
        this.col = column;
        
    }
   
    @Override
    public void run() {
        int sum = 0;
        for (int i = 0; i < this.row.length; i++){
            sum = sum + this.row[i] * this.col[i];
        }
        this.product = sum;
        
    }
    
    public int getProduct() {
        return product;
    }
    
    
}
