package engine.raytracer.optimization;

import engine.raytracer.Ray;
import engine.world.ModelGroup;
import engine.world.Polygon;

public class Octree {
	private OctreeNode root;
	
	public Octree(ModelGroup[] modelGroups)
	{
		double xMin = Double.POSITIVE_INFINITY;
		double yMin = Double.POSITIVE_INFINITY;
		double zMin = Double.POSITIVE_INFINITY;
		double xMax = Double.NEGATIVE_INFINITY;
		double yMax = Double.NEGATIVE_INFINITY;
		double zMax = Double.NEGATIVE_INFINITY;
		for (int v = 0; v < Polygon.transformedVerts.length; v++)
		{
			if (Polygon.transformedVerts[v][0] < xMin)
				xMin = Polygon.transformedVerts[v][0];
			if (Polygon.transformedVerts[v][1] < yMin)
				yMin = Polygon.transformedVerts[v][1];
			if (Polygon.transformedVerts[v][2] < zMin)
				zMin = Polygon.transformedVerts[v][2];
			
			if (Polygon.transformedVerts[v][0] > xMax)
				xMax = Polygon.transformedVerts[v][0];
			if (Polygon.transformedVerts[v][1] > yMax)
				yMax = Polygon.transformedVerts[v][1];
			if (Polygon.transformedVerts[v][2] > zMax)
				zMax = Polygon.transformedVerts[v][2];
			
		}
		this.root = new OctreeNode(new BoundingBox(xMin,yMin,zMin,xMax,yMax,zMax));
		BoundingBox tBounds;
		int totalTriangles = 0;
		for (int g = 0; g < modelGroups.length; g++)
		{
			for (int p = 0; p < modelGroups[g].polys.length;p++)
			{
				tBounds = BoundingBox.fromTriangle(modelGroups[g].polys[p]);
				//System.out.println(tBounds + " " + modelGroups[g].polys[p]);
				root.insertTriangle(modelGroups[g].polys[p], tBounds, 0);
				totalTriangles++;
			}
		}
		System.out.println("Inserted " + totalTriangles + " triangles into octree");
		OctreeNode.cleanTree(root);
		//this.root = rootNode;
	}
	
	public Intersection intersect(Ray ray, Polygon ignore)
	{
		Intersection in = new Intersection();
		if (root.bounds.intersects(ray, in))
		{
			in.iMax = Double.POSITIVE_INFINITY;
			in.iMin = Double.POSITIVE_INFINITY;
			root.intersection(ray, in, 0, Double.NEGATIVE_INFINITY, ignore);
		}
		//System.out.println("\r\n");
		
		//else
		//{
		//	System.out.println("No root intersection");
		//}
		return in;
	}
	public boolean intersectionExists(Ray ray, Polygon ignore)
	{
		Intersection in = new Intersection();
		if (root.bounds.intersects(ray, in))
		{
			in.iMax = Double.POSITIVE_INFINITY;
			in.iMin = Double.POSITIVE_INFINITY;
			return root.intersectionExists(ray, ignore);
		}
		//System.out.println("\r\n");
		
		//else
		//{
		//	System.out.println("No root intersection");
		//}
		return false;
	}
}
