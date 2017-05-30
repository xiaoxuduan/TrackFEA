package myFEA;
/**
 * 
 * @description Store elements' information; Ke, Me, Ce,
 * @reference
 * @author duan xiaoxu
 *
 */
public class Element {

	private int number; // Element number
	private double E;
	private double I;
	private double m;
	private double c;
	private double k;
	private Node nodeI; // Node i;
	private Node nodeJ; // Node j;
	private double Pt;
	// distance from Pt to Node i;
	private double xx;
	// element length;
	private double ln=Math.sqrt((nodeI.getX()-nodeJ.getX())^2+(nodeI.getY()-nodeJ.getY())^2);
	
	// element's local matrix
	private double[][] Me=new double[4][4];
	private double[][] Ce=new double[4][4];
	private double[][] Ke=new double[4][4];
	private double[][] Qe=new double[4][1];
	
	public Element(int number, double E, double I, double m, double c, double k, Node i, Node j, double Pt, double xx){
		this.number=number;
		this.E=E;
		this.I=I;
		this.m=m;
		this.c=c;
		this.k=k;
		this.nodeI=i;
		this.nodeJ=j;
		this.Pt=Pt;
		this.xx=xx;
		
		createMe();
		createCe();
		createKe();
		createQe();
		
		checkSymmetry(Me);
		checkSymmetry(Ce);
		checkSymmetry(Ke);
		
	}
	
	private void createMe(){
		Me[0][0]=(13*ln*m)/35;
		Me[0][1]=(11*Math.pow(ln, 2)*m)/210;
		Me[0][2]=(9*ln)/70;
		Me[0][3]=(-13*Math.pow(ln, 2)*m)/420;
		Me[1][1]=(Math.pow(ln, 3)*m)/105;
		Me[1][2]=(13*Math.pow(ln, 2)*m)/420;
		Me[1][3]=(-Math.pow(ln, 3)*m)/140;
		Me[2][2]=(13*ln*m)/35;
		Me[2][3]=(-11*Math.pow(ln, 2)*m)/210;
		Me[3][3]=(Math.pow(ln, 3)*m)/105;
		for(int i=1; i<4; i++){
			for(int j=0; j<i; j++){
				Me[i][j]=Me[j][i];
			}
		}
	}
	
	private void createCe(){
		Ce[0][0]=(13*c*ln)/35;
		Ce[0][1]=(11*c*ln*ln)/210;
		Ce[0][2]=(9*c*ln)/70;
		Ce[0][3]=(-13*c*ln*ln)/420;
		Ce[1][1]=(c*Math.pow(ln, 3))/105;
		Ce[1][2]=(13*c*ln*ln)/420;
		Ce[1][3]=(-c*Math.pow(ln, 3))/140;
		Ce[2][2]=(13*c*ln)/35;
		Ce[2][3]=(-11*c*ln*ln)/210;
		Ce[3][3]=(c*Math.pow(ln, 3))/105;
		for(int i=1; i<4; i++){
			for(int j=0; j<i; j++){
				Ce[i][j]=Me[j][i];
			}
		}
	}
	
	private void createKe(){
		Ke[0][0]=-(72*I*E)/Math.pow(ln,3)+(48*k*ln)/35-(-48*I*E+2*k*Math.pow(ln, 4))/Math.pow(ln, 3)+(36*I*E+k*Math.pow(ln, 4))/Math.pow(ln, 3);
		Ke[0][1]=(24*I*E)/(ln*ln)+(23*k*ln*ln)/105+(-84*I*E+k*Math.pow(ln, 4))/(2*ln*ln)-(-72*I*E+2*k*Math.pow(ln, 4))/(3*ln*ln);
		Ke[0][2]=(36*I*E)/Math.pow(ln, 3)-(61*k*ln)/70+(-48*I*E+k*Math.pow(ln, 4))/Math.pow(ln, 3);
		Ke[0][3]=-(18*I*E)/(ln*ln)+(127*k*ln*ln)/420-(-72*I*E+k*Math.pow(ln, 4))/(3*ln*ln);
		Ke[1][1]=-(8*I*E)/ln-(34*k*Math.pow(ln, 3))/105+(36*I*E+k*Math.pow(ln, 4))/(3*ln);
		Ke[1][2]=-(6*I*E)/(ln*ln)+13*k*ln*ln/420;
		Ke[1][3]=(2*I*E)/ln-k*Math.pow(ln, 3)/140;
		Ke[2][2]=(12*I*E)/Math.pow(ln, 3)+(13*k*ln)/35;
		Ke[2][3]=-(6*I*E)/(ln*ln)-11*k*ln*ln/210;
		Ke[3][3]=(4*I*E)/ln+k*Math.pow(ln, 3)/105;
		for(int i=1; i<4; i++){
			for(int j=0; j<i; j++){
				Ke[i][j]=Me[j][i];
			}
		}
	}
	
	private void createQe(){
		
	}
	
	public double[][] getMe(){
		return Me;
	}
	public double[][] getCe(){
		return Ce;
	}
	public double[][] getKe(){
		return Ke;
	}
	
	// check if matrix is symmetric;
	private void checkSymmetry(double[][] matrix){
		if(matrix.length==0) return;
		for(int i=0; i<matrix.length; i++){
			for(int j=0; j<matrix[0].length; j++){
				if((Double.valueOf(matrix[i][j])).compareTo(Double.valueOf(matrix[j][i]))!=0)
					try {
						throw new Exception("matrix is not symmetric;");
					} catch (Exception e) {
						System.out.println("matrix is not symmetric;");
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	}
}
