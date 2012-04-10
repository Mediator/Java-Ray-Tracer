package RayTracer;

import java.util.Arrays;

public class ScanLineBound {
	public double[] rgb;
	public double z;
	public int x;
	public int y;
	public ScanLineBound(int x, int y, double[] rgb, double z)
	{
		this.x = x;
		this.y = y;
		this.rgb = rgb;
		this.z = z;
	}
	public String toString()
	{
		return "X: " + x + " Y: " + y + " " + z + " " + Arrays.toString(rgb);
	}
}
