package trackFEA;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

/**
 * 
 * @description Solve track elements particularly; Don't need to transfer matrix
 *              since the local coordinate is the same as the global; No global
 *              external forces;
 * @reference
 * @author duan xiaoxu
 *
 */
public class Solver {

	private String analysisType; // static or dynamics;
	private int nNodes; // total number of nodes;
	private int nFreedoms; // single node's dof;
	private int nElements; // number of elements;
	private int nRestraints; // number of restrained nodes;
	// private int nExternalForces; // number of external forces;

	private double E;
	private double I;
	private double m;
	private double c;
	private double k;

	private double[] x = new double[nNodes]; // x[0:nNodes-1] coordinate of
												// nodes;
	private double[] y = new double[nNodes]; // y[0:nNodes-1] coordinate of
												// nodes;

	// elements[0][j]: node number of element i end;
	// elements[1][j]: node number of element j end;
	private int[][] elementsNode = new int[2][nElements];

	// forces applied in elements in arbitrary location;
	// elementsForce[0][j]: xx of P(t), distance from P to node i;
	// elementsForce[1][j]: P(t);
	private double[][] elementsForce = new double[2][nElements];

	// restraints[0][j]: node number;
	// restraints[1][j]: node restrained status
	// (10: v was restrained, 01: v' was restraints, 11: all was restrained;);
	private int[][] restraints = new int[2][nRestraints];

	// // ExternalForces[0][j]: node number where external forces loaded;
	// // ExternalForces[1][j]: F;
	// private double[][] externalForces = new double[2][nExternalForces];

	/*
	 * Above variables should be inputed from file;
	 */

	// element's global matrix
	private double[][] M = new double[nFreedoms][nFreedoms];
	private double[][] C = new double[nFreedoms][nFreedoms];
	private double[][] K = new double[nFreedoms][nFreedoms];
	private double[][] Q = new double[nFreedoms][1];
	// restrained matrix;
	private double[][] restrainedM = new double[nFreedoms][nFreedoms];
	private double[][] restrainedC = new double[nFreedoms][nFreedoms];
	private double[][] restrainedK = new double[nFreedoms][nFreedoms];
	private double[][] restrainedQ = new double[nFreedoms][1];

	private double[][] displacement = new double[nFreedoms][1];
	// restrained Dof's number; from 0;
	private List<Integer> restrainedDofNumber = new ArrayList<>();
	private Node[] nodesArray = new Node[nNodes];
	private Element[] elementsArray = new Element[nElements];

	public static void main(String[] args) {
		Solver solver = new Solver();
		solver.inputData();
		solver.createNodes();
		solver.createElements();
		solver.createM();
		solver.createC();
		solver.createK();
		solver.createQ();
		solver.createRestrainedDofNumber();
		// boundary conditions;
		solver.createRestrainedK();
		solver.createRestrainedQ();
		
		if (solver.analysisType.equals("static"))
			solver.staticSolve();
		else if (solver.analysisType.equals("dynamics"))
			solver.dynamicsSolve();
		else {
			System.out.println("analysis type is wrong;");
			try {
				throw new Exception("analysis type is wrong;");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		solver.outputData();
		System.out.println();
		System.out.println("Program is over.");
		System.out.println();
	}

	private void inputData() {
		System.out.println("Input file's name:");
		Scanner in = new Scanner(System.in);
		String fileName = in.next();

		try (Scanner inFile = new Scanner(Paths.get(fileName))) {
			analysisType = inFile.next();
			nNodes = inFile.nextInt();
			nFreedoms = inFile.nextInt();
			nElements = inFile.nextInt();
			nRestraints = inFile.nextInt();
			// nExternalForces=inFile.nextInt();
			E = inFile.nextDouble();
			I = inFile.nextDouble();
			m = inFile.nextDouble();
			c = inFile.nextDouble();
			k = inFile.nextDouble();

			// initial arrays use just applied variable;
			initialArray();

			for (int i = 0; i < x.length; i++) {
				x[i] = Double.valueOf(inFile.next());
			}

			for (int i = 0; i < y.length; i++) {
				y[i] = inFile.nextDouble();
			}
			for (int i = 0; i < elementsNode.length; i++) {
				for (int j = 0; j < elementsNode[0].length; j++) {
					elementsNode[i][j] = inFile.nextInt();
				}
			}
			for (int i = 0; i < elementsForce.length; i++) {
				for (int j = 0; j < elementsForce[0].length; j++) {
					elementsForce[i][j] = inFile.nextDouble();
				}
			}
			if (nRestraints > 0) {
				for (int i = 0; i < restraints.length; i++) {
					for (int j = 0; j < restraints[0].length; j++) {
						restraints[i][j] = inFile.nextInt();
					}
				}
			}
			// if(nExternalForces>0){
			// for(int i=0; i<externalForces.length; i++){
			// for(int j=0; j<externalForces[0].length; j++){
			// externalForces[i][j]=inFile.nextDouble();
			// }
			// }
			// }
			System.out.println();
			System.out.println("All data have been read from input file;");
		} catch (IOException e) {
			System.out.println(e.toString());
			System.out.println("Something is wrong with the IO of input file;");
		}

		checkInput();
	}

	private void initialArray() {
		x = new double[nNodes]; // x[0:nNodes-1] coordinate of nodes;
		y = new double[nNodes]; // y[0:nNodes-1] coordinate of nodes;
		elementsNode = new int[2][nElements];
		elementsForce = new double[2][nElements];
		restraints = new int[2][nRestraints];

		M = new double[nFreedoms][nFreedoms];
		C = new double[nFreedoms][nFreedoms];
		K = new double[nFreedoms][nFreedoms];
		Q = new double[nFreedoms][1];
		restrainedM = new double[nFreedoms][nFreedoms];
		restrainedC = new double[nFreedoms][nFreedoms];
		restrainedK = new double[nFreedoms][nFreedoms];
		restrainedQ = new double[nFreedoms][1];

		displacement = new double[nFreedoms][1];
		nodesArray = new Node[nNodes];
		elementsArray = new Element[nElements];

	}

	// check if input wrong;
	private void checkInput() {
		try (PrintWriter out = new PrintWriter("checkInput.txt")) {
			out.println(analysisType);
			out.println();
			out.println("nNodes: " + nNodes);
			out.println("NElements: " + nElements);
			out.println("nRestraints: " + nRestraints);
			out.println("nRestraints: " + nRestraints);
			out.println("E: " + E);
			out.println("I: " + I);
			out.println("m: " + m);
			out.println("c: " + c);
			out.println("k: " + k);
			out.println();
			out.println("x[]:");
			out.println(MatrixOper.matrixPrint(x));
			out.println();
			out.println("y[]:");
			out.println(MatrixOper.matrixPrint(y));
			out.println();
			out.println("elementsNode[][]:");
			out.println(MatrixOper.matrixPrint(elementsNode));
			out.println();
			out.println("elementsForce[][]:");
			out.println(MatrixOper.matrixPrint(elementsForce));
			out.println();
			out.println("restraints[][]:");
			out.println(MatrixOper.matrixPrint(restraints));
			out.println();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createNodes() {
		for (int i = 0; i < nNodes; i++) {
			nodesArray[i] = new Node(i + 1, x[i], y[i]);
		}
	}

	// Nodes number of element i are i and i+1;
	private void createElements() {
		for (int i = 0; i < nElements; i++) {
			elementsArray[i] = new Element(i + 1, E, I, m, c, k, nodesArray[i], nodesArray[i + 1], elementsForce[0][i],
					elementsForce[1][i]);
		}
	}

	private void createM() {
		if (M.length == 0)
			return;
		for (int i = 0; i < elementsArray.length; i++) {
			M[elementsArray[i].getAssembleArray()[0]][elementsArray[i].getAssembleArray()[0]] += elementsArray[i]
					.getMe()[0][0];
			M[elementsArray[i].getAssembleArray()[0]][elementsArray[i].getAssembleArray()[1]] += elementsArray[i]
					.getMe()[0][1];
			M[elementsArray[i].getAssembleArray()[0]][elementsArray[i].getAssembleArray()[2]] += elementsArray[i]
					.getMe()[0][2];
			M[elementsArray[i].getAssembleArray()[0]][elementsArray[i].getAssembleArray()[3]] += elementsArray[i]
					.getMe()[0][3];
			M[elementsArray[i].getAssembleArray()[1]][elementsArray[i].getAssembleArray()[0]] += elementsArray[i]
					.getMe()[1][0];
			M[elementsArray[i].getAssembleArray()[1]][elementsArray[i].getAssembleArray()[1]] += elementsArray[i]
					.getMe()[1][1];
			M[elementsArray[i].getAssembleArray()[1]][elementsArray[i].getAssembleArray()[2]] += elementsArray[i]
					.getMe()[1][2];
			M[elementsArray[i].getAssembleArray()[1]][elementsArray[i].getAssembleArray()[3]] += elementsArray[i]
					.getMe()[1][3];

			M[elementsArray[i].getAssembleArray()[2]][elementsArray[i].getAssembleArray()[0]] += elementsArray[i]
					.getMe()[2][0];
			M[elementsArray[i].getAssembleArray()[2]][elementsArray[i].getAssembleArray()[1]] += elementsArray[i]
					.getMe()[2][1];
			M[elementsArray[i].getAssembleArray()[2]][elementsArray[i].getAssembleArray()[2]] += elementsArray[i]
					.getMe()[2][2];
			M[elementsArray[i].getAssembleArray()[2]][elementsArray[i].getAssembleArray()[3]] += elementsArray[i]
					.getMe()[2][3];

			M[elementsArray[i].getAssembleArray()[3]][elementsArray[i].getAssembleArray()[0]] += elementsArray[i]
					.getMe()[3][0];
			M[elementsArray[i].getAssembleArray()[3]][elementsArray[i].getAssembleArray()[1]] += elementsArray[i]
					.getMe()[3][1];
			M[elementsArray[i].getAssembleArray()[3]][elementsArray[i].getAssembleArray()[2]] += elementsArray[i]
					.getMe()[3][2];
			M[elementsArray[i].getAssembleArray()[3]][elementsArray[i].getAssembleArray()[3]] += elementsArray[i]
					.getMe()[3][3];
		}
	}

	private void createC() {
		if (C.length == 0)
			return;
		for (int i = 0; i < elementsArray.length; i++) {
			C[elementsArray[i].getAssembleArray()[0]][elementsArray[i].getAssembleArray()[0]] += elementsArray[i]
					.getCe()[0][0];
			C[elementsArray[i].getAssembleArray()[0]][elementsArray[i].getAssembleArray()[1]] += elementsArray[i]
					.getCe()[0][1];
			C[elementsArray[i].getAssembleArray()[0]][elementsArray[i].getAssembleArray()[2]] += elementsArray[i]
					.getCe()[0][2];
			C[elementsArray[i].getAssembleArray()[0]][elementsArray[i].getAssembleArray()[3]] += elementsArray[i]
					.getCe()[0][3];

			C[elementsArray[i].getAssembleArray()[1]][elementsArray[i].getAssembleArray()[0]] += elementsArray[i]
					.getCe()[1][0];
			C[elementsArray[i].getAssembleArray()[1]][elementsArray[i].getAssembleArray()[1]] += elementsArray[i]
					.getCe()[1][1];
			C[elementsArray[i].getAssembleArray()[1]][elementsArray[i].getAssembleArray()[2]] += elementsArray[i]
					.getCe()[1][2];
			C[elementsArray[i].getAssembleArray()[1]][elementsArray[i].getAssembleArray()[3]] += elementsArray[i]
					.getCe()[1][3];

			C[elementsArray[i].getAssembleArray()[2]][elementsArray[i].getAssembleArray()[0]] += elementsArray[i]
					.getCe()[2][0];
			C[elementsArray[i].getAssembleArray()[2]][elementsArray[i].getAssembleArray()[1]] += elementsArray[i]
					.getCe()[2][1];
			C[elementsArray[i].getAssembleArray()[2]][elementsArray[i].getAssembleArray()[2]] += elementsArray[i]
					.getCe()[2][2];
			C[elementsArray[i].getAssembleArray()[2]][elementsArray[i].getAssembleArray()[3]] += elementsArray[i]
					.getCe()[2][3];

			C[elementsArray[i].getAssembleArray()[3]][elementsArray[i].getAssembleArray()[0]] += elementsArray[i]
					.getCe()[3][0];
			C[elementsArray[i].getAssembleArray()[3]][elementsArray[i].getAssembleArray()[1]] += elementsArray[i]
					.getCe()[3][1];
			C[elementsArray[i].getAssembleArray()[3]][elementsArray[i].getAssembleArray()[2]] += elementsArray[i]
					.getCe()[3][2];
			C[elementsArray[i].getAssembleArray()[3]][elementsArray[i].getAssembleArray()[3]] += elementsArray[i]
					.getCe()[3][3];
		}
	}

	private void createK() {
		if (K.length == 0)
			return;
		for (int i = 0; i < elementsArray.length; i++) {
			K[elementsArray[i].getAssembleArray()[0]][elementsArray[i].getAssembleArray()[0]] += elementsArray[i]
					.getKe()[0][0];
			K[elementsArray[i].getAssembleArray()[0]][elementsArray[i].getAssembleArray()[1]] += elementsArray[i]
					.getKe()[0][1];
			K[elementsArray[i].getAssembleArray()[0]][elementsArray[i].getAssembleArray()[2]] += elementsArray[i]
					.getKe()[0][2];
			K[elementsArray[i].getAssembleArray()[0]][elementsArray[i].getAssembleArray()[3]] += elementsArray[i]
					.getKe()[0][3];

			K[elementsArray[i].getAssembleArray()[1]][elementsArray[i].getAssembleArray()[0]] += elementsArray[i]
					.getKe()[1][0];
			K[elementsArray[i].getAssembleArray()[1]][elementsArray[i].getAssembleArray()[1]] += elementsArray[i]
					.getKe()[1][1];
			K[elementsArray[i].getAssembleArray()[1]][elementsArray[i].getAssembleArray()[2]] += elementsArray[i]
					.getKe()[1][2];
			K[elementsArray[i].getAssembleArray()[1]][elementsArray[i].getAssembleArray()[3]] += elementsArray[i]
					.getKe()[1][3];

			K[elementsArray[i].getAssembleArray()[2]][elementsArray[i].getAssembleArray()[0]] += elementsArray[i]
					.getKe()[2][0];
			K[elementsArray[i].getAssembleArray()[2]][elementsArray[i].getAssembleArray()[1]] += elementsArray[i]
					.getKe()[2][1];
			K[elementsArray[i].getAssembleArray()[2]][elementsArray[i].getAssembleArray()[2]] += elementsArray[i]
					.getKe()[2][2];
			K[elementsArray[i].getAssembleArray()[2]][elementsArray[i].getAssembleArray()[3]] += elementsArray[i]
					.getKe()[2][3];

			K[elementsArray[i].getAssembleArray()[3]][elementsArray[i].getAssembleArray()[0]] += elementsArray[i]
					.getKe()[3][0];
			K[elementsArray[i].getAssembleArray()[3]][elementsArray[i].getAssembleArray()[1]] += elementsArray[i]
					.getKe()[3][1];
			K[elementsArray[i].getAssembleArray()[3]][elementsArray[i].getAssembleArray()[2]] += elementsArray[i]
					.getKe()[3][2];
			K[elementsArray[i].getAssembleArray()[3]][elementsArray[i].getAssembleArray()[3]] += elementsArray[i]
					.getKe()[3][3];
		
		}

	}

	private void createQ() {
		if (Q.length == 0)
			return;
		for (int i = 0; i < elementsArray.length; i++) {
			Q[elementsArray[i].getAssembleArray()[0]][0] += elementsArray[i].getQe()[0][0];
			Q[elementsArray[i].getAssembleArray()[1]][0] += elementsArray[i].getQe()[1][0];
			Q[elementsArray[i].getAssembleArray()[2]][0] += elementsArray[i].getQe()[2][0];
			Q[elementsArray[i].getAssembleArray()[3]][0] += elementsArray[i].getQe()[3][0];
		}
	}

	private void createRestrainedDofNumber() {
		if (restraints.length == 0)
			return;
		for (int i = 0; i < restraints[0].length; i++) {
			Node tempNode = nodesArray[restraints[0][i] - 1];
			switch (restraints[1][i]) {
			case 10:
				restrainedDofNumber.add(tempNode.getNumber() * 2-1);
				break;
			case 01:
				restrainedDofNumber.add(tempNode.getNumber() * 2);
				break;
			case 11:
				restrainedDofNumber.add(tempNode.getNumber() * 2-1);
				restrainedDofNumber.add(tempNode.getNumber() * 2);
				break;
			default:
				try {
					throw new Exception("input restraints is wrong;");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// Put a big number;
	private void createRestrainedK() {
		restrainedK = MatrixOper.copyArray(K);
		for (int i = 0; i < restrainedDofNumber.size(); i++) {
//			System.out.println(restrainedDofNumber.get(i)-1);
			restrainedK[restrainedDofNumber.get(i)-1][restrainedDofNumber.get(i)-1] *= Math.pow(10, 10);
		}
	}

	// Put a big number;
	private void createRestrainedQ() {
		restrainedQ = MatrixOper.copyArray(Q);
		for (int i = 0; i < restrainedDofNumber.size(); i++) {
			restrainedQ[restrainedDofNumber.get(i)-1][0] *= Math.pow(10, 10);
		}
	}

	private void staticSolve() {
		displacement = EquationSet.gaussEliminate(restrainedK, restrainedQ);
		setNodeDisplacement();
		System.out.println();
		System.out.println("Static solve is completed;");
		System.out.println();
	}

	private void setNodeDisplacement() {
		for (int i = 0; i < nodesArray.length; i++) {
			nodesArray[i].setV(displacement[i * 2][0]);
			nodesArray[i].setTheta(displacement[i * 2 + 1][0]);
		}
	}

	private void dynamicsSolve() {

	}

	private void outputData() {
		System.out.println("Output file's name:");
		Scanner in = new Scanner(System.in);
		String fileName = in.next();

		try (PrintWriter out = new PrintWriter(fileName)) {
			out.println("Track FEA result file.\nPowered by Duan Xiaoxu.");
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			out.println(dateFormat.format(date));
			out.println();

			out.println("Nodes's vertical displacement:");
			out.println("node number   vertical displacement");
			for (int i = 0; i < nNodes; i++) {
				String temp=String.format("%.4f", nodesArray[i].getV());
				out.println((i + 1) + "         " + temp);
			}
			out.println();
			out.println("Global M matrix:");
			out.println(MatrixOper.matrixPrint(M));
			out.println();
			out.println("Global C matrix:");
			out.println(MatrixOper.matrixPrint(C));
			out.println();
			out.println("Global K matrix:");
			out.println(MatrixOper.matrixPrint(K));
			out.println();
			out.println("Global Q matrix:");
			out.println(MatrixOper.matrixPrint(Q));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
