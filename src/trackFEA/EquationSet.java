package trackFEA;

import java.util.List;

/**
 * 
 * @description Solve equation set; Gauss elimination method(particularly positive definite symmetric matrix)
 * @reference
 * @author duan xiaoxu
 *
 */
public class EquationSet {
	// ax=b,
	public static double[][] gaussEliminate(double[][] a, double[][] b){
		// att: 1 column , not 0 column;
		double[][] res=new double[a.length][1];
		if(a.length==0) return res;
		if(a.length!=a[0].length){
			System.out.println("matrix is not square;");
			try {
				throw new Exception("matrix is not square;");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int n=a.length;
		// elimination to transfer a to upper triangular matrix;
		for(int i=0; i<n; i++){  // for each column
			for(int j=i+1; j<n; j++){  // for each row below row i
				// att: position;
				double temp=a[j][i]/a[i][i];
				for(int k=0; k<n; k++){  // for each element in row j
//					System.out.println("temp: "+temp);
					a[j][k]-=temp*a[i][k];
				}
				// att: position;
				b[j][0]-=temp*b[i][0];
			}
		}
//		System.out.println("a: ");
//		System.out.println(MatrixOper.matrixPrint(a));
//		System.out.println();
//		System.out.println("b:");
//		System.out.println(MatrixOper.matrixPrint(b));
		// calculate x from bottom;
		res[n-1][0]=b[n-1][0]/a[n-1][n-1];
		for(int i=n-2; i>=0; i--){
			double tempSum=0;
			for(int k=i+1; k<n; k++){
				tempSum+=res[k][0]*a[i][k];
			}
			res[i][0]=(b[i][0]-tempSum)/a[i][i];
		}
		return res;
	}
}
