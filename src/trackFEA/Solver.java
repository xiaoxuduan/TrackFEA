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
 * @description Solve beam elements particularly; Don't need to transfer matrix
 *              since the local coordinate system is the same as the global; No global
 *              external forces; Preprocess displacement boundary conditions;
 * @reference
 * @author duan xiaoxu
 *
 */
public class Solver {

	private String analysisType; // static or dynamics;
	private int nNodes; // total number of nodes;
	private int nNodeFreedoms; // single node's freedoms;
	private int nFreedoms; // total number of nodes' dof;
	private int nElements; // number of elements;
	private int nRestraints; // number of restrained nodes;
	private int nRestrainedDof; // number of restrained dof;
	// private int nExternalForces; // number of external forces;
	private String elementType; // element type, now available: trackElement, BeamElement;
	private double A;
	private double E;
	private double I;
	private double m;
	private double c;
	private double k;

	// Arrays are initialed in initialArray();
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
	// (110: u and v are restrained, theta is not restrained);
	private int[][] restraints = new int[2][nRestraints];

	// // ExternalForces[0][j]: node number where external forces loaded;
	// // ExternalForces[1][j]: F;
	// private double[][] externalForces = new double[2][nExternalForces];

	/*
	 * Above variables above should be inputed from file;
	 */

	// element's global matrix
	private double[][] M;
	private double[][] C;
	private double[][] K;
	private double[][] Q;

	private double[][] displacement;
	private Node[] nodesArray;
	private Object[] elementsArray;

	public static void main(String[] args) {
		Solver solver = new Solver();
		solver.inputData();
		solver.createNodes();
		solver.createElements();
		// Preprocess displacement boundary conditions; 
		// Only add the local matrix's elements (whose related dof's displacement is not restrained) to global matrix; 
		solver.createM();
		solver.createC();
		solver.createK();
		solver.createQ();

		if (solver.analysisType.equals("static"))
			solver.staticSolve();
		else if (solver.analysisType.equals("dynamics"))
			solver.dynamicsSolve();
		else {
			System.out.println("analysis type is wrong;");
			try {
				throw new Exception("analysis type is wrong;");
			} catch (Exception e) {
				// TODO Auto-generated catch blockC
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
			nNodeFreedoms=inFile.nextInt();
			nFreedoms = inFile.nextInt();
			nElements = inFile.nextInt();
			nRestraints = inFile.nextInt();
			nRestrainedDof=inFile.nextInt();
			// nExternalForces=inFile.nextInt();
			elementType=inFile.next();
			if(elementType.equals("BeamElement3"))
				A=inFile.nextDouble();
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

		M = new double[nFreedoms-nRestrainedDof][nFreedoms-nRestrainedDof];
		C = new double[nFreedoms-nRestrainedDof][nFreedoms-nRestrainedDof];
		K = new double[nFreedoms-nRestrainedDof][nFreedoms-nRestrainedDof];
		Q = new double[nFreedoms-nRestrainedDof][1];

		displacement = new double[nFreedoms-nRestrainedDof][1];
		nodesArray = new Node[nNodes];
		if(elementType.equals("TrackElement")){
			elementsArray = new TrackElement[nElements];
		}
		else if(elementType.equals("BeamElement")){
			elementsArray = new BeamElement[nElements];
		}
		else if(elementType.equals("BeamElement3")){
			elementsArray = new BeamElement3[nElements];
		}
		else{
			try {
				throw new Exception("Element type is not exist.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// check if input wrong;
	private void checkInput() {
		try (PrintWriter out = new PrintWriter("checkInput.txt")) {
			out.println(analysisType);
			out.println();
			out.println("nNodes: " + nNodes);
			out.println("NElements: " + nElements);
			out.println("nRestraints: " + nRestraints);
			out.println("nNodeFreedoms: "+nNodeFreedoms);
			out.println("nFreedoms: " + nFreedoms);
			out.println("nRestrainedDof: " + nRestrainedDof);
			out.println("elementType: " + elementType);
			out.println("A: "+A);
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

	private void createNodes(){
		if(elementType.equals("TrackElement")||elementType.equals("BeamElement"))
			createNodes1();
		else if(elementType.equals("BeamElement3"))
			createNodes2();
	}
	
	// set restrained dof's dofNumber to 0, which is stored in Node class;
	// other dofs numbered are created by adding 1 to totalDofNumber;
	private void createNodes1() {
		int totalDofNumber=0;
		int dofNumber1=0;
		int dofNumber2=0;
		for (int i = 0; i < nNodes; i++) { // i=node number - 1;
			// set default displacement to 0;
			double v=0;
			double theta=0;
			// Mark if node i+1 is restrained.
			boolean flag=false;
			// set restrained dof's dofNumber to 0, which is stored in Node class;
			for (int j = 0; j < restraints[0].length; j++) {  // for each column of restraints;
				if(i+1==restraints[0][j]){
					// Mark if node i+1 is restrained. If it's restrained, if will be processed in switch below. 
					flag=true; 
					switch (restraints[1][j]) {
					case 10:
						dofNumber1=0;
						dofNumber2=totalDofNumber+1;
						totalDofNumber++;
						break;
					case 01:
						dofNumber1=totalDofNumber+1;
						totalDofNumber++;
						dofNumber2=0;
						break;
					case 11:
						dofNumber1=0;
						dofNumber2=0;
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
			// if node i+1 is not restrained, deal with it here;
			if(flag==false){
				dofNumber1=totalDofNumber+1;
				totalDofNumber++;
				dofNumber2=totalDofNumber+1;
				totalDofNumber++;
			}
			nodesArray[i] = new Node(i + 1, x[i], y[i], dofNumber1, dofNumber2, v, theta);
		}
	}
	
	// set restrained dof's dofNumber to 0, which is stored in Node class;
	// other dofs numbered are created by adding 1 to totalDofNumber;
	private void createNodes2() {
		int totalDofNumber=0;
		int dofNumber1=0;
		int dofNumber2=0;
		int dofNumber3=0;
		for (int i = 0; i < nNodes; i++) { // i=node number - 1;
			// set default displacement to 0;
			double u=0;
			double v=0;
			double theta=0;
			// Mark if node i+1 is restrained.
			boolean flag=false;
			// set restrained dof's dofNumber to 0, which is stored in Node2 class;
			for (int j = 0; j < restraints[0].length; j++) {  // for each column of restraints;
				if(i+1==restraints[0][j]){
					// Mark if node i+1 is restrained. If it's restrained, if will be processed in switch below. 
					flag=true; 
					switch (restraints[1][j]) {
					case 100:
						dofNumber1=0;
						dofNumber2=totalDofNumber+1;
						totalDofNumber++;
						dofNumber3=totalDofNumber+1;
						totalDofNumber++;
						break;
					case 110:
						dofNumber1=0;
						dofNumber1=0;
						dofNumber3=totalDofNumber+1;
						totalDofNumber++;
						break;
					case 111:
						dofNumber1=0;
						dofNumber2=0;
						dofNumber3=0;
						break;
					case 10:  // 010
						dofNumber1=++totalDofNumber;
						dofNumber2=0;
						dofNumber3=++totalDofNumber;
						break;
					case 011:
						dofNumber1=++totalDofNumber;
						dofNumber2=0;
						dofNumber3=0;
						break;
					case 001:
						dofNumber1=++totalDofNumber;
						dofNumber2=++totalDofNumber;
						dofNumber3=0;
						break;
					case 101:
						dofNumber1=0;
						dofNumber2=++totalDofNumber;
						dofNumber3=0;
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
			// if node i+1 is not restrained, deal with it here;
			if(flag==false){
				dofNumber1=totalDofNumber+1;
				totalDofNumber++;
				dofNumber2=totalDofNumber+1;
				totalDofNumber++;
				dofNumber3=++totalDofNumber;
			}
			nodesArray[i] = new Node(i + 1, x[i], y[i], dofNumber1, dofNumber2, dofNumber3, u, v, theta);
		}
		
			// check dof number
			for(int i=0; i<nodesArray.length; i++){
				System.out.println(nodesArray[i].getDofNumber1());
				System.out.println(nodesArray[i].getDofNumber2());
				System.out.println(nodesArray[i].getDofNumber3());
			}
		
	}

	// Nodes number of element i are i and i+1;
	private void createElements() {
		for (int i = 0; i < nElements; i++) {
			if(elementType.equals("TrackElement"))
				elementsArray[i] = new TrackElement(i + 1, E, I, m, c, k, nodesArray[i], nodesArray[i + 1], elementsForce[0][i], elementsForce[1][i]);
			else if(elementType.equals("BeamElement"))
				elementsArray[i] = new BeamElement(i + 1, E, I, m, c, k, nodesArray[i], nodesArray[i + 1], elementsForce[0][i], elementsForce[1][i]);
			else if(elementType.equals("BeamElement3"))
				elementsArray[i] = new BeamElement3(i + 1, E, I, m, c, k, nodesArray[i], nodesArray[i + 1], elementsForce[0][i], elementsForce[1][i], A);
		}
	}

	private void createM() {
		if (M.length == 0)
			return;
		for (int i = 0; i < elementsArray.length; i++) {
			for(int mm=0; mm<nNodeFreedoms*2; mm++){
				for(int nn=0; nn<nNodeFreedoms*2; nn++){
					if(elementType.equals("TrackElement")){
						TrackElement tempElement=(TrackElement)elementsArray[i];
						// The reason why it's -1 rather than 0, see Element.createAssembleArray();
						if(tempElement.getAssembleArray()[mm]!=-1 && tempElement.getAssembleArray()[nn]!=-1){
							M[tempElement.getAssembleArray()[mm]][tempElement.getAssembleArray()[nn]] += tempElement.getMe()[mm][nn];
						}
					}
					else if(elementType.equals("BeamElement")){
						BeamElement tempElement=(BeamElement)elementsArray[i];
						// The reason why it's -1 rather than 0, see Element.createAssembleArray();
						if(tempElement.getAssembleArray()[mm]!=-1 && tempElement.getAssembleArray()[nn]!=-1){
							M[tempElement.getAssembleArray()[mm]][tempElement.getAssembleArray()[nn]] += tempElement.getMe()[mm][nn];
						}
					}
					else if(elementType.equals("BeamElement3")){
						BeamElement3 tempElement=(BeamElement3)elementsArray[i];
						// The reason why it's -1 rather than 0, see Element.createAssembleArray();
						if(tempElement.getAssembleArray()[mm]!=-1 && tempElement.getAssembleArray()[nn]!=-1){
							M[tempElement.getAssembleArray()[mm]][tempElement.getAssembleArray()[nn]] += tempElement.getMe()[mm][nn];
						}
					}
				}
			}
		}
	}
	
	private void createC() {
		if (C.length == 0)
			return;
		for (int i = 0; i < elementsArray.length; i++) {
			for(int mm=0; mm<nNodeFreedoms*2; mm++){
				for(int nn=0; nn<nNodeFreedoms*2; nn++){
					if(elementType.equals("TrackElement")){
						TrackElement tempElement=(TrackElement)elementsArray[i];
						// The reason why it's -1 rather than 0, see Element.createAssembleArray();
						if(tempElement.getAssembleArray()[mm]!=-1 && tempElement.getAssembleArray()[nn]!=-1){
							C[tempElement.getAssembleArray()[mm]][tempElement.getAssembleArray()[nn]] += tempElement.getCe()[mm][nn];
						}
					}
					else if(elementType.equals("BeamElement")){
						BeamElement tempElement=(BeamElement)elementsArray[i];
						// The reason why it's -1 rather than 0, see Element.createAssembleArray();
						if(tempElement.getAssembleArray()[mm]!=-1 && tempElement.getAssembleArray()[nn]!=-1){
							C[tempElement.getAssembleArray()[mm]][tempElement.getAssembleArray()[nn]] += tempElement.getCe()[mm][nn];
						}
					}
					else if(elementType.equals("BeamElement3")){
						BeamElement3 tempElement=(BeamElement3)elementsArray[i];
						// The reason why it's -1 rather than 0, see Element.createAssembleArray();
						if(tempElement.getAssembleArray()[mm]!=-1 && tempElement.getAssembleArray()[nn]!=-1){
							C[tempElement.getAssembleArray()[mm]][tempElement.getAssembleArray()[nn]] += tempElement.getCe()[mm][nn];
						}
					}
				}
			}
		}
	}
	
	private void createK() {
		if (K.length == 0)
			return;
		for (int i = 0; i < elementsArray.length; i++) {
			for(int mm=0; mm<nNodeFreedoms*2; mm++){
				for(int nn=0; nn<nNodeFreedoms*2; nn++){
					if(elementType.equals("TrackElement")){
						TrackElement tempElement=(TrackElement)elementsArray[i];
						// The reason why it's -1 rather than 0, see Element.createAssembleArray();
						if(tempElement.getAssembleArray()[mm]!=-1 && tempElement.getAssembleArray()[nn]!=-1){
							K[tempElement.getAssembleArray()[mm]][tempElement.getAssembleArray()[nn]] += tempElement.getKe()[mm][nn];
						}
					}
					else if(elementType.equals("BeamElement")){
						BeamElement tempElement=(BeamElement)elementsArray[i];
						// The reason why it's -1 rather than 0, see Element.createAssembleArray();
						if(tempElement.getAssembleArray()[mm]!=-1 && tempElement.getAssembleArray()[nn]!=-1){
							K[tempElement.getAssembleArray()[mm]][tempElement.getAssembleArray()[nn]] += tempElement.getKe()[mm][nn];
						}
					}
					else if(elementType.equals("BeamElement3")){
						BeamElement3 tempElement=(BeamElement3)elementsArray[i];
						// The reason why it's -1 rather than 0, see Element.createAssembleArray();
						if(tempElement.getAssembleArray()[mm]!=-1 && tempElement.getAssembleArray()[nn]!=-1){
							K[tempElement.getAssembleArray()[mm]][tempElement.getAssembleArray()[nn]] += tempElement.getKe()[mm][nn];
						}
					}
				}
			}
		}
	}
	
	private void createQ() {
		if (Q.length == 0)
			return;
		for (int i = 0; i < elementsArray.length; i++) {
			for(int mm=0; mm<nNodeFreedoms*2; mm++){
				if(elementType.equals("TrackElement")){
					TrackElement tempElement=(TrackElement)elementsArray[i];
					// The reason why it's -1 rather than 0, see Element.createAssembleArray();
					if(tempElement.getAssembleArray()[mm]!=-1){
						Q[tempElement.getAssembleArray()[mm]][0] += tempElement.getQe()[mm][0];
					}
				}
				else if(elementType.equals("BeamElement")){
					BeamElement tempElement=(BeamElement)elementsArray[i];
					// The reason why it's -1 rather than 0, see Element.createAssembleArray();
					if(tempElement.getAssembleArray()[mm]!=-1){
						Q[tempElement.getAssembleArray()[mm]][0] += tempElement.getQe()[mm][0];
					}
				}
				else if(elementType.equals("BeamElement3")){
					BeamElement3 tempElement=(BeamElement3)elementsArray[i];
					// The reason why it's -1 rather than 0, see Element.createAssembleArray();
					if(tempElement.getAssembleArray()[mm]!=-1){
						Q[tempElement.getAssembleArray()[mm]][0] += tempElement.getQe()[mm][0];
					}
				}
			}
		}
	}

	private void staticSolve() {
		double[][] tempK=MatrixOper.copyArray(K);
		double[][] tempQ=MatrixOper.copyArray(Q);
		displacement = EquationSet.gaussEliminate(tempK, tempQ);
		setNodeDisplacement();
		System.out.println();
		System.out.println("Static solve is completed;");
		System.out.println();
	}

	private void setNodeDisplacement() {
		int totalDofNumber=0;
		if(elementType.equals("TrackElement")||elementType.equals("BeamElement")){
			for (int i = 0; i < nodesArray.length; i++) {
				if(nodesArray[i].getDofNumber1()!=0){
					nodesArray[i].setV(displacement[totalDofNumber][0]);
					totalDofNumber++;
				}
				if(nodesArray[i].getDofNumber2()!=0){
					nodesArray[i].setTheta(displacement[totalDofNumber][0]);
					totalDofNumber++;
				}
			}
		}
		else if(elementType.equals("BeamElement3")){
			for (int i = 0; i < nodesArray.length; i++) {
				if(nodesArray[i].getDofNumber1()!=0){
					nodesArray[i].setU(displacement[totalDofNumber][0]);
					totalDofNumber++;
				}
				if(nodesArray[i].getDofNumber2()!=0){
					nodesArray[i].setV(displacement[totalDofNumber][0]);
					totalDofNumber++;
				}
				if(nodesArray[i].getDofNumber3()!=0){
					nodesArray[i].setTheta(displacement[totalDofNumber][0]);
					totalDofNumber++;
				}
			}
		}
		
	}

	private void dynamicsSolve() {

	}

	private void outputData() {
		System.out.println("Output file's name:");
		Scanner in = new Scanner(System.in);
		String fileName = in.next();

		try (PrintWriter out = new PrintWriter(fileName)) {
			out.println("TrackFEA result file.\nPowered by Duan Xiaoxu.");
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			out.println(dateFormat.format(date));
			out.println();

			out.println("Nodes' vertical displacement:");
			out.println("Node number   Vertical displacement");
			for (int i = 0; i < nNodes; i++) {
				String temp = String.format("%.4f", nodesArray[i].getV());
				out.println(nodesArray[i].getNumber() + "             " + temp);
			}
			out.println();
			out.println("Global restrained M matrix:");
			out.println(MatrixOper.matrixPrint(M));
			out.println();
			out.println("Global restrained C matrix:");
			out.println(MatrixOper.matrixPrint(C));
			out.println();
			out.println("Global restrained K matrix:");
			out.println(MatrixOper.matrixPrint(K));
			out.println();
			out.println("Global restrained Q matrix:");
			out.println(MatrixOper.matrixPrint(Q));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
