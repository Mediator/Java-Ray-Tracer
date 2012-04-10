package engine.raytracer.optimization;

import engine.world.Polygon;

public class Intersection {
	public double[] sPoint = new double[3];
	public double sMin = Double.MAX_VALUE;
	public double sBeta = 0;
	public double sGamma = 0;
	public Polygon sPoly = null;
	public double iMax = Double.POSITIVE_INFINITY;
	public double iMin = Double.NEGATIVE_INFINITY;
}
