public class Main{
    public static void main(String args[]){
        int k, n; //k=colonne, n=righe
        int a[][] = initMatrix(2, 2);
        printMatrix(a);
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
        for (int i = 0; i<matrix.length; i++){
            for (int j = 0; j<matrix[0].length; j++){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}
