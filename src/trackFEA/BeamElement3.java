package trackFEA;

/**
 * 
 * @description Beam element with 2 nodes; Each node has 3 dofs;
 * @reference
 * @author duan xiaoxu
 *
 */
public class BeamElement3 {

	private int number; // Element number
	private double A;
	private double E;
	private double I;
	private double m;
	private double c;
	private double k;
	private Node nodeI; // Node i;
	private Node nodeJ; // Node j;
	private double Pt;
	// distance from Pt to Node i;
	private double d;
	// element length;
	private double l;

	// assemble array used to map local matrix location to global matrix;
	private int[] assembleArray = new int[6];
	// element's local matrix
	private double[][] Me = new double[6][6];
	private double[][] Ce = new double[6][6];
	private double[][] Ke = new double[6][6];
	private double[][] Qe = new double[6][1];

	public BeamElement3() {

	}

	public BeamElement3(int number, double E, double I, double m, double c, double k, Node i, Node j, double d,
			double Pt, double A) {
		this.number = number;
		this.E = E;
		this.I = I;
		this.m = m;
		this.c = c;
		this.k = k;
		this.nodeI = i;
		this.nodeJ = j;
		this.Pt = Pt;
		this.d = d;
		this.A = A;

		l = Math.sqrt(Math.pow((nodeI.getX() - nodeJ.getX()), 2) + Math.pow((nodeI.getY() - nodeJ.getY()), 2));

		createMe();
		createCe();
		createKe();
		createQe();

		checkSymmetry(Me);
		checkSymmetry(Ce);
		checkSymmetry(Ke);

		createAssembleArray();
	}

	private void createMe() {
		Me[0][0] = (m*l/420)*140;
		Me[0][1] = (m*l/420)*0;
		Me[0][2] = (m*l/420)*0;
		Me[0][3] = (m*l/420)*70;
		Me[0][4] = (m*l/420)*0;
		Me[0][5] = (m*l/420)*0;
		Me[1][1] = (m*l/420)*156;
		Me[1][2] = (m*l/420)*22*l;
		Me[1][3] = (m*l/420)*0;
		Me[1][4] = (m*l/420)*54;
		Me[1][5] = (m*l/420)*(-13)*l;
		Me[2][2] = (m*l/420)*4*l*l;
		Me[2][3] = (m*l/420)*0;
		Me[2][4] = (m*l/420)*13*l;
		Me[2][5] = (m*l/420)*(-3*l*l);
		Me[3][3] = (m*l/420)*140;
		Me[3][4] = (m*l/420)*0;
		Me[3][5] = (m*l/420)*0;
		Me[4][4] = (m*l/420)*156;
		Me[4][5] = (m*l/420)*(-22*l);
		Me[5][5] = (m*l/420)*4*l*l;
		for (int i = 1; i < Me.length; i++) {
			for (int j = 0; j < i; j++) {
				Me[i][j] = Me[j][i];
			}
		}
	}

	private void createCe() {
		for(int i=0; i<Ce.length; i++){
			for(int j=0; j<Ce[0].length; j++){
				Ce[i][j]=Me[i][j]/m*c;
			}
		}
//		Ce[0][0] = 
//		Ce[0][1] = 
//		Ce[0][2] = 
//		Ce[0][3] = 
//		Ce[0][4] = 
//		Ce[0][5] = 
//		Ce[1][1] = 
//		Ce[1][2] = 
//		Ce[1][3] = 
//		Ce[1][4] = 
//		Ce[1][5] = 
//		Ce[2][2] = 
//		Ce[2][3] = 
//		Ce[2][4] = 
//		Ce[2][5] = 
//		Ce[3][3] = 
//		Ce[3][4] = 
//		Ce[3][5] = 
//		Ce[4][4] = 
//		Ce[4][5] = 
//		Ce[5][5] = 
//		for (int i = 1; i < Ce.length; i++) {
//			for (int j = 0; j < i; j++) {
//				Ce[i][j] = Ce[j][i];
//			}
//		}
	}

	private void createKe() {
		Ke[0][0] = (A * E) / l;
		Ke[0][1] = 0;
		Ke[0][2] = 0;
		Ke[0][3] = -(A * E) / l;
		Ke[0][4] = 0;
		Ke[0][5] = 0;
		Ke[1][1] = (12 * I * E) / Math.pow(l, 3);
		Ke[1][2] = (6 * I * E) / (l * l);
		Ke[1][3] = 0;
		Ke[1][4] = -(12 * I * E) / Math.pow(l, 3);
		Ke[1][5] = (6 * I * E) / (l * l);
		Ke[2][2] = (4 * I * E) / l;
		Ke[2][3] = 0;
		Ke[2][4] = -(6 * I * E) / (l * l);
		Ke[2][5] = (2 * E * I) / l;
		Ke[3][3] = (A * E) / l;
		Ke[3][4] = 0;
		Ke[3][5] = 0;
		Ke[4][4] = (12 * E * I) / Math.pow(l, 3);
		Ke[4][5] = -(6 * E * I) / (l * l);
		Ke[5][5] = 4 * I * E / l;
		for (int i = 1; i < Ke.length; i++) {
			for (int j = 0; j < i; j++) {
				Ke[i][j] = Ke[j][i];
			}
		}
	}

	private void createQe() {
		Qe[0][0] = 0;
		Qe[1][0] = (-3*c/(l*l)+2*d*d*d/Math.pow(l, 3)+1)*Pt;
		Qe[2][0] = Math.pow((-d/l+1),2)*Pt*d;
		Qe[3][0] = 0;
		Qe[4][0]=(-2*d*d*d/(l*l*l)+3*d*d/(l*l))*Pt;
		Qe[5][0]=((d/l-1)*Pt*d*d)/l;
	}

	private void createAssembleArray() {
		// minus 1 so that 0 will turn to -1;
		assembleArray[0] = nodeI.getDofNumber1() - 1;
		assembleArray[1] = nodeI.getDofNumber2() - 1;
		assembleArray[2] = nodeI.getDofNumber3() - 1;
		assembleArray[3] = nodeJ.getDofNumber1() - 1;
		assembleArray[4] = nodeJ.getDofNumber2() - 1;
		assembleArray[5] = nodeJ.getDofNumber3() - 1;
	}

	public int[] getAssembleArray() {
		return assembleArray;
	}

	public double[][] getMe() {
		return Me;
	}

	public double[][] getCe() {
		return Ce;
	}

	public double[][] getKe() {
		return Ke;
	}

	public double[][] getQe() {
		return Qe;
	}

	public Node getNodeI() {
		return nodeI;
	}

	public Node getNodeJ() {
		return nodeJ;
	}

	// check if matrix is symmetric;
	private void checkSymmetry(double[][] matrix) {
		if (matrix.length == 0)
			return;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if ((Double.valueOf(matrix[i][j])).compareTo(Double.valueOf(matrix[j][i])) != 0)
					try {
						throw new Exception("MaStrix is not symmetric;");
					} catch (Exception e) {
						// System.out.println("matrix is not symmetric;");
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	}
}
