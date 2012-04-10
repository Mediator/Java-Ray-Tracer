package engine.raytracer;

public class Ray {
	public double startX;
	public double startY;
	public double startZ;
	public double dirX;
	public double dirY;
	public double dirZ;
	public double[] directionVector;
	public double[] startVector;
	public double[] invDirection;
	public int[] sign;
	public Ray(double startX, double startY, double startZ, double endX, double endY, double endZ)
	{
		this.startX = startX;
		this.startY = startY;
		this.startZ = startZ;
		startVector = new double[] {startX, startY,startZ};
		directionVector = new double[]{endX, endY, endZ};
		this.dirX = endX;
		this.dirY = endY;
		this.dirZ = endZ;
		invDirection = new double[3];
		sign = new int[3];
		invDirection[0] = 1/dirX;
		invDirection[1] = 1/dirY;
		invDirection[2] = 1/dirZ;
		sign[0] = (invDirection[0] < 0) ? 1 : 0;
		sign[1] = (invDirection[1] < 0) ? 1 : 0;
		sign[2] = (invDirection[2] < 0) ? 1 : 0;
		
	}
	public String toString()
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append("Start: " + startX + " " + startY + " " + startZ);
		bldr.append(" Direction: " + dirX + " " + dirY + " " + dirZ);
		return bldr.toString();
	}
}
