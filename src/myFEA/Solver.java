package myFEA;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import zte.ConstraintEdgeWeightedDiGraph;
/**
 * 
 * @description Solve track elements particularly; Don't need transfer matrix since the local coordinate is the same as the global;
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
	private int nExternalForces; // number of external forces;

	private double E;
	private double I;
	private double m;
	private double c;
	private double k;
	
	private double[] x=new double[nNodes]; // x[0:nNodes-1] coordinate of nodes;
	private double[] y=new double[nNodes]; // y[0:nNodes-1] coordinate of nodes;

	// elements[0][j]: node number of element i end;
	// elements[1][j]: node number of element j end;
	private int[][] elements = new int[2][nElements];

	// forces applied in elements in arbitrary location;
	// elementsForce[0][j]: xx of P(t), distance from P to node i;
	// elementsForce[1][j]: P(t);
	private double[][] elementsForce=new double[2][nElements];
	
	// restraints[0][j]: node number;
	// restraints[1][j]: node restrained status
	// 	(00: all were not restrained, 10: v was restrained, 01: v' was restraints, 11: all was restrained;);
	private double[][] restraints = new double[2][nRestraints]; 

	// ExternalForces[0][j]: node number where external forces loaded;
	// ExternalForces[1][j]: F;
	private double[][] externalForces = new double[2][nExternalForces];
	
	/*Above variables should be inputed from file;*/
	
	// element's global matrix
	private double[][] M=new double[nFreedoms][nFreedoms];
	private double[][] C=new double[nFreedoms][nFreedoms];
	private double[][] K=new double[nFreedoms][nFreedoms];
	private double[][] Q=new double[nFreedoms][1];
	
	private double[][] displacement=new double[nFreedoms][1];
	
	private List<Node> nodeList=new ArrayList<>();
	private List<Element> elementList=new ArrayList<>();
	
	public static void main(String[] args){
		inputData();
		createNodes();
		createElements();
		createM();
		createC();
		createK();
		createQ();
		if(analysisType.equalTo("static"))
			staticSolve();
		else if(analysisType.equalTo("dynamics"))
			dynamicsSolve();
		else{
			System.out.println("analysis type is wrong;");
			throw new Exception("analysis type is wrong;");
		}
		outputData();
	}
	
	private void inputData(){
		System.out.println("input file name:");
		Scanner in=new Scanner(System.in);
		String fileName=in.next();
		
		try (Scanner inFile=new Scanner(Paths.get(fileName))) {
			analysisType=inFile.next();
			nNodes=inFile.nextInt();
			nFreedoms=inFile.nextInt();
			nElements=inFile.nextInt();
			nRestraints=inFile.nextInt();
			nExternalForces=inFile.nextInt();
			E=inFile.nextDouble();
			I=inFile.nextDouble();
			m=inFile.nextDouble();
			c=inFile.nextDouble();
			k=inFile.nextDouble();
			for(int i=0; i<x.length; i++){
				x[i]=inFile.nextDouble();
			}
			for(int i=0; i<y.length; i++){
				y[i]=inFile.nextDouble();
			}
			for(int i=0; i<elements.length; i++){
				for(int j=0; j<elements[0].length; j++){
					elements[i][j]=inFile.nextInt();
				}
			}
			for(int i=0; i<elementsForce.length; i++){
				for(int j=0; j<elementsForce[0].length; j++){
					elementsForce[i][j]=inFile.nextDouble();
				}
			}
			if(nRestraints>0){
				for(int i=0; i<restraints.length; i++){
					for(int j=0; j<restraints[0].length; j++){
						restraints[i][j]=inFile.nextDouble();
					}
				}
			}
			if(nExternalForces>0){
				for(int i=0; i<externalForces.length; i++){
					for(int j=0; j<externalForces[0].length; j++){
						externalForces[i][j]=inFile.nextDouble();
					}
				}
			}
			
			
			
		} catch (IOException e) {
			System.out.println(e.toString());
			System.out.println("Something is wrong with the IO of input.txt");
		}
	}
}
