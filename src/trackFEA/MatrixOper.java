package trackFEA;

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

	public static String matrixPrint(double[] a) {
		String s = "matrix " + String.valueOf(a) + ":\n";
		s += "a.length: " + a.length + "\n";
		for (int i = 0; i < a.length; i++) {
			String temp = String.format("%.4f", a[i]);
			s = s + temp + " ";
		}
		return s;
	}

	public static String matrixPrint(double[][] a) {
		String s = "matrix " + String.valueOf(a) + ":\n";
		s += "rows: " + a.length + "  columns: " + a[0].length + "\n";
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				String temp = String.format("%.4f", a[i][j]);
				s = s + temp + " ";
			}
			s += "\n";
		}
		return s;
	}

	public static String matrixPrint(int[][] a) {
		String s = "matrix " + String.valueOf(a) + ":\n";
		s += "rows: " + a.length + "  columns: " + a[0].length + "\n";
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				s = s + a[i][j] + " ";
			}
			s += "\n";
		}
		return s;
	}

	public static double[] copyArray(double[] a) {
		double[] b = new double[a.length];
		for (int i = 0; i < b.length; i++) {
			b[i] = a[i];
		}
		return b;
	}

	public static double[][] copyArray(double[][] a) {
		if (a.length == 0)
			return new double[0][0];
		double[][] b = new double[a.length][a[0].length];
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				b[i][j] = a[i][j];
			}
		}
		return b;
	}
}
