package myFEA;
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
	public Node(int number, double x, double y){
		this.number=number;
		this.x=x;
		this.y=y;
	}
	public int getNumber(){
		return number;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public void setV(double v){
		this.v=v;
	}
	public double getV(){
		return v;
	}
	public void setTheta(double theta){
		this.theta=theta;
	}
	public double getTheta(){
		return theta;
	}
}
