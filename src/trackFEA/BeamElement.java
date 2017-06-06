package trackFEA;
/**
 * 
 * @description Beam element with 2 nodes; Each node has 2 dofs; The spring is not used;
 * @reference
 * @author duan xiaoxu
 *
 */
public class BeamElement{

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
	private double ln;

	// assemble array used to map local matrix location to global matrix;
	private int[] assembleArray = new int[4];
	// element's local matrix
	private double[][] Me = new double[4][4];
	private double[][] Ce = new double[4][4];
	private double[][] Ke = new double[4][4];
	private double[][] Qe = new double[4][1];

	public BeamElement(){
		
	}
	public BeamElement(int number, double E, double I, double m, double c, double k, Node i, Node j, double xx,
			double Pt) {
		this.number = number;
		this.E = E;
		this.I = I;
		this.m = m;
		this.c = c;
		this.k = k;
		this.nodeI = i;
		this.nodeJ = j;
		this.Pt = Pt;
		this.xx = xx;

		ln = Math.sqrt(Math.pow((nodeI.getX() - nodeJ.getX()), 2) + Math.pow((nodeI.getY() - nodeJ.getY()), 2));

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
		Me[0][0] = (13 * ln * m) / 35;
		Me[0][1] = (11 * Math.pow(ln, 2) * m) / 210;
		Me[0][2] = (9 * ln*m) / 70;
		Me[0][3] = (-13 * Math.pow(ln, 2) * m) / 420;
		Me[1][1] = (Math.pow(ln, 3) * m) / 105;
		Me[1][2] = (13 * Math.pow(ln, 2) * m) / 420;
		Me[1][3] = (-Math.pow(ln, 3) * m) / 140;
		Me[2][2] = (13 * ln * m) / 35;
		Me[2][3] = (-11 * Math.pow(ln, 2) * m) / 210;
		Me[3][3] = (Math.pow(ln, 3) * m) / 105;
		for (int i = 1; i < 4; i++) {
			for (int j = 0; j < i; j++) {
				Me[i][j] = Me[j][i];
			}
		}
	}

	private void createCe() {
		Ce[0][0] = (13 * c * ln) / 35;
		Ce[0][1] = (11 * c * ln * ln) / 210;
		Ce[0][2] = (9 * c * ln) / 70;
		Ce[0][3] = (-13 * c * ln * ln) / 420;
		Ce[1][1] = (c * Math.pow(ln, 3)) / 105;
		Ce[1][2] = (13 * c * ln * ln) / 420;
		Ce[1][3] = (-c * Math.pow(ln, 3)) / 140;
		Ce[2][2] = (13 * c * ln) / 35;
		Ce[2][3] = (-11 * c * ln * ln) / 210;
		Ce[3][3] = (c * Math.pow(ln, 3)) / 105;
		for (int i = 1; i < 4; i++) {
			for (int j = 0; j < i; j++) {
				Ce[i][j] = Ce[j][i];
			}
		}
	}

	private void createKe() {
		Ke[0][0] = (12 * I * E) / Math.pow(ln, 3);
		Ke[0][1] = (6 * I * E) / (ln * ln);
		Ke[0][2] = (-12 * I * E) / Math.pow(ln, 3);
		Ke[0][3] = (6 * I * E) / (ln * ln);
		Ke[1][1] = (4 * I * E) / ln;
		Ke[1][2] = -(6 * I * E) / (ln * ln);
		Ke[1][3] = (2 * I * E) / ln;
		Ke[2][2] = (12 * I * E) / Math.pow(ln, 3);
		Ke[2][3] = -(6 * I * E) / (ln * ln);
		Ke[3][3] = (4 * I * E) / ln;
		for (int i = 1; i < 4; i++) {
			for (int j = 0; j < i; j++) {
				Ke[i][j] = Ke[j][i];
			}
		}
	}

	private void createQe() {
		Qe[0][0] = (1 - (3 * xx * xx) / (ln * ln) + (2 * Math.pow(xx, 3)) / Math.pow(ln, 3)) * Pt;
		Qe[1][0] = (xx - (2 * xx * xx) / ln + Math.pow(xx, 3) / (ln * ln)) * Pt;
		Qe[2][0] = ((3 * xx * xx) / (ln * ln) - (2 * xx * xx * xx) / (ln * ln * ln)) * Pt;
		Qe[3][0] = (-(xx * xx) / ln + Math.pow(xx, 3) / (ln * ln)) * Pt;
	}

	private void createAssembleArray() {
		// minus 1 so that 0 will turn to -1;
		assembleArray[0] = nodeI.getDofNumber1() - 1;
		assembleArray[1] = nodeI.getDofNumber2() - 1;
		assembleArray[2] = nodeJ.getDofNumber1() - 1;
		assembleArray[3] = nodeJ.getDofNumber2() - 1;
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
//						System.out.println("matrix is not symmetric;");
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	}
}
