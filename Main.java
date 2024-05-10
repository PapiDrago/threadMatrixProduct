public class Main{
    public static void main(String args[]){
        int k, n; //k=colonne, n=righe
        int a[][] = initMatrix(2, 3);
        int b[][] = initMatrix(2, 2);
        printMatrix(a);
        printMatrix(b);
        printMatrix(matrixProd(a, b));
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

    public static int[][] matrixProd(int[][] a, int[][] b){
        if(a[0].length != b.length) {
            throw new IllegalArgumentException("Il numero di colonne di A "+
                                    "deve essere uguale al numero di righe di B!");
        } 
        int c[][] = new int[a.length][b[0].length];
        for(int rowA = 0; rowA<a.length; rowA++){
            for(int colB = 0; colB<b[0].length; colB++){
                for (int colA = 0; colA < a[0].length; colA++){
                    c[rowA][colB] = c[rowA][colB] + a[rowA][colA] * b[colA][colB];
                }
            }
        }
        return c;
    }
}
