public class Main{
    public static void main(String args[]){
        int k, n; //k=colonne, n=righe
        int a[][] = initMatrix(k, n);
        System.out.println("Hello World");
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
}
