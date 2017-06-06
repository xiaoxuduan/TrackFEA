package trackFEA;

/**
 * 
 * @description Nodes' information;
 * @reference
 * @author duan xiaoxu
 *
 */
public class NodeOld {
	private int number;
	private double x;
	private double y;
	private double v;
	private double theta;
	// Used to label dof for the convenience of forming system matrix;
	// Numbered from 1,...
	private int dofNumber1; // v
	private int dofNumber2; // theta

	public NodeOld(int number, double x, double y, int dofNumber1, int dofNumber2, double v, double theta) {
		this.number = number;
		this.x = x;
		this.y = y;
		this.dofNumber1=dofNumber1;
		this.dofNumber2=dofNumber2;
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
}
