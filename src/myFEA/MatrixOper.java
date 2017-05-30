package myFEA;

import java.util.Arrays;

/**
 * @description Matrix multiply, plus, transpose, print
 * @author duan xiaoxu
 *
 */
public class MatrixOper {
	public double[][] matrixPlus(double[][] a, double[][] b) throws Exception {
		if (a.length != b.length || a[0].length != b[0].length) {
			System.out.println("matrixPlus error: size does not fit;");
			System.out.println("inputed a: \n" + Arrays.toString(a));
			System.out.println("inputed b: \n" + Arrays.toString(b));
			throw new Exception("matrixPlus error: size does not fit;");
		}
		double[][] c = new double[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				c[i][j] = a[i][j] + b[i][j];
			}
		}
		return c;
	}

	public double[][] matrixMultiply(double[][] a, double[][] b) throws Exception {
		if (a[0].length != b.length) {
			System.out.println("matrixMultiply error: size does not fit;");
			System.out.println("inputed a: \n" + Arrays.toString(a));
			System.out.println("inputed b: \n" + Arrays.toString(b));
			throw new Exception("matrixMultiply error: size does not fit;");
		}
		double[][] c = new double[a.length][b[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				for (int k = 0; k < a[0].length; k++) {
					c[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return c;
	}

	public double[][] matrixTranspose(double[][] a) {
		double[][] b = new double[a[0].length][a.length];
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				b[i][j] = a[j][i];
			}
		}
		return b;
	}

	public String matrixPrint(double[][] a) {
		String s = "matrix " + a + ":\n";
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				s = s + a[i][j] + " ";
			}
			s += "\n";
		}
		return s;
	}
}
