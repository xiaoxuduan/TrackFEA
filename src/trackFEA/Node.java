package trackFEA;

public class Node {
	private int number;
	private double x;
	private double y;
	private double u;
	private double v;
	private double theta;
	// Used to label dof for the convenience of forming system matrix;
	// Numbered from 1,...
	private int dofNumber1; // u
	private int dofNumber2; // v
	private int dofNumber3; // theta

	public Node(int number, double x, double y, int dofNumber1, int dofNumber2, double v, double theta) {
		this.number = number;
		this.x = x;
		this.y = y;
		this.dofNumber1=dofNumber1;
		this.dofNumber2=dofNumber2;
		this.v=v;
		this.theta=theta;
	}
	
	public Node(int number, double x, double y, int dofNumber1, int dofNumber2, int dofNumber3, double u, double v, double theta) {
		this.number = number;
		this.x = x;
		this.y = y;
		this.dofNumber1=dofNumber1;
		this.dofNumber2=dofNumber2;
		this.dofNumber3=dofNumber3;
		this.u=u;
		this.v=v;
		this.theta=theta;
	}

	public int getNumber() {
		return number;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getU(){
		return u;
	}
	public void setU(double u){
		this.u=u;
	}
	public void setV(double v) {
		this.v = v;
	}

	public double getV() {
		return v;
	}

	public void setTheta(double theta) {
		this.theta = theta;
	}

	public double getTheta() {
		return theta;
	}

	public int getDofNumber1() {
		return dofNumber1;
	}

	public int getDofNumber2() {
		return dofNumber2;
	}
	
	public int getDofNumber3(){
		return dofNumber3;
	}
}
