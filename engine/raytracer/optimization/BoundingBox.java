package engine.raytracer.optimization;

import engine.raytracer.Ray;
import engine.world.Polygon;

public class BoundingBox {
	private double xMin;
	private double yMin;
	private double zMin;
	private double xMax;
	private double yMax;
	private double zMax;
	private double[] center;
	public BoundingBox(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax)
	{
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.zMin = zMin;
		this.zMax = zMax;
		this.center = new double[3];
		center[0] = (xMax + xMin) / 2;
		center[1] = (yMax + yMin) / 2;
		center[2] = (zMax + zMin) / 2;
	}
	


   public boolean intersects(BoundingBox b) {
       return ((b != null)&&
                       (xMin <= b.xMax) &&
                       (xMax >= b.xMin) &&
                       (yMin <= b.yMax) &&
                       (yMax >= b.yMin) &&
                       (zMin <= b.zMax) &&
                       (zMax >= b.zMin));
   }
	
	private BoundingBox()
	{
		xMin = Double.POSITIVE_INFINITY;
		yMin = Double.POSITIVE_INFINITY;
		zMin = Double.POSITIVE_INFINITY;
		xMax = Double.NEGATIVE_INFINITY;
		yMax = Double.NEGATIVE_INFINITY;
		zMax = Double.NEGATIVE_INFINITY;
	}
	public double[] getCenter()
	{
		return center;
		
	}
	public double getXMin()
	{
		return xMin;
	}
	public double getYMin()
	{
		return yMin;
	}
	public double getZMin()
	{
		return zMin;
	}
	public double getXMax()
	{
		return xMax;
	}
	public double getYMax()
	{
		return yMax;
	}
	public double getZMax()
	{
		return zMax;
	}
	public static BoundingBox fromTriangle(Polygon poly)
	{
		BoundingBox ret = new BoundingBox();
          for (int i = 0; i < 3; i++) {
        	  double[] vert = Polygon.transformedVerts[poly.myVerts[i]];
        	//  System.out.println("N " + poly + " " + vert[0] + " " +  ret.xMax);
              if (vert[0] > ret.xMax)
              {
            	//  System.out.println("Setting " + poly + " " +  ret.xMax);
            	  ret.xMax = vert[0];
              }
              if (vert[0] < ret.xMin)
              {
            	 
            	  ret.xMin = vert[0];
            	  
            	  
              }
              
              if (vert[1] > ret.yMax)
            	  ret.yMax = vert[1];
              if (vert[1] < ret.yMin)
            	  ret.yMin = vert[1];
              
              if (vert[2] > ret.zMax)
            	  ret.zMax = vert[2];
              if (vert[2] < ret.zMin)
            	  ret.zMin = vert[2];

          }
  		ret.center = new double[3];
  		ret.center[0] = (ret.xMax + ret.xMin) / 2;
  		ret.center[1] = (ret.yMax + ret.yMin) / 2;
  		ret.center[2] = (ret.zMax + ret.zMin) / 2;
          return ret;
	}
	
	
	//Based in part on http://people.csail.mit.edu/amy/papers/box-jgt.ps
	// An Efficient and Robust Ray–Box Intersection Algorithm
	 public boolean intersects(Ray ray, Intersection in) {
		 double tnear = Double.NEGATIVE_INFINITY;
		 double tfar = Double.POSITIVE_INFINITY;
		 in.iMax = Double.POSITIVE_INFINITY;
		 in.iMin = Double.POSITIVE_INFINITY;
		 double tmin, tmax, tymin, tymax, tzmin, tzmax;
		 if (ray.sign[0] == 0)
		 {
			 tmin = (xMin - ray.startX) * ray.invDirection[0];
			 tmax = (xMax - ray.startX) * ray.invDirection[0];
		 }
		 else
		 {
			 tmin = (xMax - ray.startX) * ray.invDirection[0];
			 tmax = (xMin - ray.startX) * ray.invDirection[0];
		 }
		 
		 if (ray.sign[1] == 0)
		 {
			 tymin = (yMin - ray.startY) * ray.invDirection[1];
			 tymax = (yMax - ray.startY) * ray.invDirection[1];
		 }
		 else
		 {
			 tymin = (yMax - ray.startY) * ray.invDirection[1];
			 tymax = (yMin - ray.startY) * ray.invDirection[1];
		 }
		 if ( (tmin > tymax) || (tymin > tmax) )
			 return false;
		 if (tymin > tmin)
			 tmin = tymin;
		 if (tymax < tmax)
			 tmax = tymax;
		 if (ray.sign[2] == 0)
		 {
			 tzmin = (zMin - ray.startZ) * ray.invDirection[2];
			 tzmax = (zMax - ray.startZ) * ray.invDirection[2];
		 }
		 else
		 {
			 tzmin = (zMax - ray.startZ) * ray.invDirection[2];
			 tzmax = (zMin - ray.startZ) * ray.invDirection[2];
		 }
		 if ( (tmin > tzmax) || (tzmin > tmax) )
			 return false;
		 if (tzmin > tmin)
			 tmin = tzmin;
		 if (tzmax < tmax)
			 tmax = tzmax;
		 //System.out.print(tmin + " ");
		 //System.out.print(tmax + " ");
		 //System.out.println(((tmin < tfar) && (tmax > tnear)));
		 in.iMax = tmax;
		 in.iMin = tmin;
		 return ((tmin < tfar) && (tmax > tnear));
	 }
	 public String toString()
	 {
		 return this.xMin + "," + this.yMin + "," + this.zMin +" " + this.xMax + ","+ this.yMax + "," + this.zMax;
	 }
 }
