package trackFEA;

/**
 * 
 * @description Nodes' information;
 * @reference
 * @author duan xiaoxu
 *
 */
public class Node {
	private int number;
	private double x;
	private double y;
	private double v;
	private double theta;
	// Used to label dof for the convenience of forming system matrix;
	private int dofNumber1;
	private int dofNumber2;

	public Node(int number, double x, double y) {
		this.number = number;
		this.x = x;
		this.y = y;

		dofNumber1 = number * 2 - 1;
		dofNumber2 = number * 2;
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
