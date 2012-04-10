package RayTracer;

public class Ray {
	double startX;
	double startY;
	double startZ;
	double endX;
	double endY;
	double endZ;
	public Ray(double startX, double startY, double startZ, double endX, double endY, double endZ)
	{
		this.startX = startX;
		this.startY = startY;
		this.startZ = startZ;
		this.endX = endX;
		this.endY = endY;
		this.endZ = endZ;
	}
	public String toString()
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append("Start: " + startX + " " + startY + " " + startZ);
		bldr.append(" End: " + endX + " " + endY + " " + endZ);
		return bldr.toString();
	}
}
