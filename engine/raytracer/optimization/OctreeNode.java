package engine.raytracer.optimization;

import java.util.ArrayList;

import engine.raytracer.Ray;
import engine.util.FastMath;
import engine.util.Matrix;
import engine.world.Polygon;

public class OctreeNode {
	private final short MAX_DEPTH = 7;
	private OctreeNode[] nodes = new OctreeNode[8];
	int numNodes = 0;
	public BoundingBox bounds;
	public boolean hasChildNodes = false;
	private ArrayList<Polygon> polys;
	public OctreeNode(BoundingBox bounds)
	{
		this.bounds = bounds;
	}
	public static boolean cleanTree(OctreeNode n)
	{
		boolean hasChildren = false;
		if (n.polys != null && n.polys.size() > 0)
		{
			hasChildren = true;
		}
		if (n.nodes == null)
			return hasChildren;
		for (int x = 0; x < n.nodes.length; x++)
		{
			if (n.nodes[x] != null)
			{
				if (cleanTree(n.nodes[x]))
					hasChildren = true;
				else
				{
					n.numNodes--;
					n.nodes[x] = null;
				}
			}
		}
		return hasChildren;
	}
	//public final double epsilonPositive = 1;//0.9999999;
	//public final double epsilonNegative = 0;//0.0000001;
	public final double epsilonPositive = 1.000001;//0.9999999;
	public final double epsilonNegative = -0.9999999;//0.0000001;
	public boolean intersection(Ray ray, Intersection in, int depth, double tMax, Polygon ignore)
	{
		/*for (int x =0; x < 8;x++)
		{
			if (nodes[x] != null)
			System.out.println("Child" + this.nodes[x].bounds);
			else
				System.out.println("Depth " + depth + " Child: " + x +  "is null");
		}
		System.out.println();
		System.out.println("{" + this.bounds + "} " + "Node has no polygons");*/
		if (this.polys != null)
		{
			//System.out.println("{" + this.bounds + "} " + polys.size());
			Polygon poly;
			/*for (int p = 0; p < this.polys.size(); p++)
			{
				poly = polys.get(p);
				if (in.sPoly != null && (poly == in.sPoly || poly.hashCode() == in.sPoly.hashCode()))
					continue;
				double[] LminusA = new double[3];
				LminusA[0] = ray.startX - Polygon.transformedVerts[poly.myVerts[0]][0];
				LminusA[1] = ray.startY - Polygon.transformedVerts[poly.myVerts[0]][1];
				LminusA[2] = ray.startZ - Polygon.transformedVerts[poly.myVerts[0]][2];




				Matrix tri = new Matrix(3,3);
				tri.values[0][0] = Polygon.transformedVerts[poly.myVerts[1]][0] - Polygon.transformedVerts[poly.myVerts[0]][0];
				tri.values[0][1] = Polygon.transformedVerts[poly.myVerts[2]][0] - Polygon.transformedVerts[poly.myVerts[0]][0];
				tri.values[0][2] = -ray.dirX;


				tri.values[1][0] = Polygon.transformedVerts[poly.myVerts[1]][1] - Polygon.transformedVerts[poly.myVerts[0]][1];
				tri.values[1][1] = Polygon.transformedVerts[poly.myVerts[2]][1] - Polygon.transformedVerts[poly.myVerts[0]][1];
				tri.values[1][2] = -ray.dirY;


				tri.values[2][0] = Polygon.transformedVerts[poly.myVerts[1]][2] - Polygon.transformedVerts[poly.myVerts[0]][2];
				tri.values[2][1] = Polygon.transformedVerts[poly.myVerts[2]][2] - Polygon.transformedVerts[poly.myVerts[0]][2];
				tri.values[2][2] = -ray.dirZ;

				double[] sol = Matrix.solve(tri, LminusA);

				if (sol[0] >= -0.01 && sol[1] >= -0.01 && (sol[0] + sol[1]) <= 1.01)
				{				
					//System.out.println("Intersection: " + Arrays.toString(sol) + " [] " + poly + "{" + bounds + "}");
					//System.out.println("INTERSECTION");
					if (sol[2] > -0.01  && sol[2] < in.sMin)
					{
						in.sBeta = sol[0];
						in.sGamma = sol[1];
						in.sMin = sol[2];
						in.sPoly = poly;
					}
				}
			}
			*/
			// Based on http://softsurfer.com/Archive/algorithm_0105/algorithm_0105.htm
			// Copyright 2001, softSurfer (www.softsurfer.com)
			// This code may be freely used and modified for any purpose
			// providing that this copyright notice is included with it.
			// SoftSurfer makes no warranty for this code, and cannot be held
			// liable for any real or imagined damage resulting from its use.
			// Users of this code must verify correctness for their application.
			
			double[]    u = new double[3];
			double[]	v = new double[3];
			double[]    w0 = new double[3];
			double[]	w = new double[3];        
			double     a,b,c;             
			double I[] = new double[3];
			double    uu, uv, vv, wu, wv, D;
			double s, t;
			for (int p = 0; p < this.polys.size(); p++)
			{
				poly = polys.get(p);
				if (in.sPoly != null && (poly == in.sPoly || poly.hashCode() == in.sPoly.hashCode()))
					continue;
				
				if (ignore != null && poly == ignore)
					continue;
					u[0] = Polygon.transformedVerts[poly.myVerts[1]][0] - Polygon.transformedVerts[poly.myVerts[0]][0];
					u[1] = Polygon.transformedVerts[poly.myVerts[1]][1] - Polygon.transformedVerts[poly.myVerts[0]][1];
					u[2] = Polygon.transformedVerts[poly.myVerts[1]][2] - Polygon.transformedVerts[poly.myVerts[0]][2];
					
					v[0] = Polygon.transformedVerts[poly.myVerts[2]][0] - Polygon.transformedVerts[poly.myVerts[0]][0];
					v[1] = Polygon.transformedVerts[poly.myVerts[2]][1] - Polygon.transformedVerts[poly.myVerts[0]][1];
					v[2] = Polygon.transformedVerts[poly.myVerts[2]][2] - Polygon.transformedVerts[poly.myVerts[0]][2];

				
					w0[0] = ray.startX - Polygon.transformedVerts[poly.myVerts[0]][0];
					w0[1] = ray.startY - Polygon.transformedVerts[poly.myVerts[0]][1];
					w0[2] = ray.startZ - Polygon.transformedVerts[poly.myVerts[0]][2];
		    
					
					a = -Matrix.dotProduct3(poly.normal,w0);
					b = Matrix.dotProduct3(poly.normal,ray.directionVector);
					if (FastMath.abs(b) < 0) {
					//	System.out.println("Rejecting b: " + b);

						continue;
					}

					
					c = a / b;
					if (c < 0.000001)                  
					{
					//	System.out.println("Rejecting C: " + c);
						continue;          
					}



					I[0] = ray.startX + c * ray.dirX;
					I[1] = ray.startY + c * ray.dirY;
					I[2] = ray.startZ + c * ray.dirZ;



					uu = Matrix.dotProduct3(u,u);
					uv = Matrix.dotProduct3(u,v);
					vv = Matrix.dotProduct3(v,v);
					w[0] = I[0] - Polygon.transformedVerts[poly.myVerts[0]][0];
					w[1] = I[1] - Polygon.transformedVerts[poly.myVerts[0]][1];
					w[2] = I[2] - Polygon.transformedVerts[poly.myVerts[0]][2];
					wu = Matrix.dotProduct3(w,u);
					wv = Matrix.dotProduct3(w,v);
					D = uv * uv - uu * vv;


				
					s = (uv * wv - vv * wu) / D;
					if (s < 0 || s > 1)
					{
					//	System.out.println("Rejecting S: " + s);

						continue;
					}
					t = (uv * wu - uu * wv) / D;
					if (t < 0 || (s + t) > 1)
					{
					//	System.out.println("Rejecting t: " + t + " " + s + " " + (t+s));

						continue;
					}
					/*System.out.println("Intersection: ");
					System.out.println("\t Poly: " + poly);
					System.out.println("\t Ray: " + ray);
					System.out.println("\t I: " + Arrays.toString(I));
					System.out.println("\tC: " + c + " T: " + t + " S: " + s);
					System.out.println("\tA: " + a + " B: " + b);
					System.out.println("\r\n");*/
					if (c < in.sMin)
					{
						in.sBeta = s;
						in.sGamma = t;
						in.sMin = c;
						in.sPoly = poly;
						in.sPoint = I;
					}
				}
		}
		
		if (in.sPoly != null && in.sMin < tMax && numNodes == 0)
			return true;

		double[] minVals = new double[8];
		double[] maxVals = new double[8];
		double minVal = Double.POSITIVE_INFINITY;
		int minIndex = 0;
		for (int n = 0; n < nodes.length; n++)
		{
			if (nodes[n] != null)
			{

				if (nodes[n].bounds.intersects(ray, in))
				{
					
					//System.out.println("\r\n\r\nIntersection ("+depth+"): " + ray + "{" + nodes[n].bounds + "}" );
					minVals[n] = in.iMin;
					maxVals[n] = in.iMax;
					if (in.iMin < minVal)
					{
						minVal = in.iMin;
						minIndex = n;
					}
				}
				else
				{
					minVals[n]= Double.POSITIVE_INFINITY;
					maxVals[n] = Double.POSITIVE_INFINITY;
				}
			}
		}
		int tIdx;
		for (int n = 0; n < nodes.length; n++)
		{
			tIdx = n ^ minIndex;
			if (nodes[tIdx] != null && Double.compare(minVals[tIdx],Double.POSITIVE_INFINITY) != 0)
			{
				if (nodes[tIdx].intersection(ray, in, depth+1, maxVals[tIdx],ignore))
				{
					if (in.sPoly != null && in.sMin < maxVals[tIdx])
						return true;
				}
			}
		}
		return false;
	}
	
	
	public boolean intersectionExists(Ray ray, Polygon ignore)
	{
		
		if (this.polys != null)
		{
			
			Polygon poly;
			double[]    u = new double[3];
			double[]	v = new double[3];
			double[]    w0 = new double[3];
			double[]	w = new double[3];          
			double     a, b,c;            
			double I[] = new double[3];
			double    uu, uv, vv, wu, wv, D;
			double s, t;
			for (int p = 0; p < this.polys.size(); p++)
			{
				poly = polys.get(p);
				if (ignore != null && ignore == poly)
					continue;
					u[0] = Polygon.transformedVerts[poly.myVerts[1]][0] - Polygon.transformedVerts[poly.myVerts[0]][0];
					u[1] = Polygon.transformedVerts[poly.myVerts[1]][1] - Polygon.transformedVerts[poly.myVerts[0]][1];
					u[2] = Polygon.transformedVerts[poly.myVerts[1]][2] - Polygon.transformedVerts[poly.myVerts[0]][2];
					
					v[0] = Polygon.transformedVerts[poly.myVerts[2]][0] - Polygon.transformedVerts[poly.myVerts[0]][0];
					v[1] = Polygon.transformedVerts[poly.myVerts[2]][1] - Polygon.transformedVerts[poly.myVerts[0]][1];
					v[2] = Polygon.transformedVerts[poly.myVerts[2]][2] - Polygon.transformedVerts[poly.myVerts[0]][2];

										
					w0[0] = ray.startX - Polygon.transformedVerts[poly.myVerts[0]][0];
					w0[1] = ray.startY - Polygon.transformedVerts[poly.myVerts[0]][1];
					w0[2] = ray.startZ - Polygon.transformedVerts[poly.myVerts[0]][2];
		    
					
					a = -Matrix.dotProduct3(poly.normal,w0);
					b = Matrix.dotProduct3(poly.normal,ray.directionVector);
					if (FastMath.abs(b) < epsilonNegative) {
						continue;
					}

					
					c = a / b;
					if (c < 0.00001)                  
					{
					//	System.out.println("Rejecting C: " + c);
						continue;           
					}
						


					I[0] = ray.startX + c * ray.dirX;
					I[1] = ray.startY + c * ray.dirY;
					I[2] = ray.startZ + c * ray.dirZ;

				

					uu = Matrix.dotProduct3(u,u);
					uv = Matrix.dotProduct3(u,v);
					vv = Matrix.dotProduct3(v,v);
					w[0] = I[0] - Polygon.transformedVerts[poly.myVerts[0]][0];
					w[1] = I[1] - Polygon.transformedVerts[poly.myVerts[0]][1];
					w[2] = I[2] - Polygon.transformedVerts[poly.myVerts[0]][2];
					wu = Matrix.dotProduct3(w,u);
					wv = Matrix.dotProduct3(w,v);
					D = uv * uv - uu * vv;

					
				
					s = (uv * wv - vv * wu) / D;
					if (s < 0 || s > 1)
					{
					//	System.out.println("Rejecting S: " + s);

						continue;
					}
					t = (uv * wu - uu * wv) / D;
					if (t < 0 || (s + t) > 1)
					{
					//	System.out.println("Rejecting t: " + t + " " + s + " " + (t+s));

						continue;
					}
					return true;
				}
		}
		
		if (numNodes == 0)
			return false;
		Intersection in = new Intersection();
		double[] minVals = new double[8];
		double[] maxVals = new double[8];
		double minVal = Double.POSITIVE_INFINITY;
		int minIndex = 0;
		for (int n = 0; n < nodes.length; n++)
		{
			if (nodes[n] != null)
			{

				if (nodes[n].bounds.intersects(ray, in))
				{
					
					//System.out.println("\r\n\r\nIntersection ("+depth+"): " + ray + "{" + nodes[n].bounds + "}" );
					minVals[n] = in.iMin;
					maxVals[n] = in.iMax;
					if (in.iMin < minVal)
					{
						minVal = in.iMin;
						minIndex = n;
					}
				}
				else
				{
					minVals[n]= Double.POSITIVE_INFINITY;
					maxVals[n] = Double.POSITIVE_INFINITY;
				}
			}
		}
		int tIdx;
		for (int n = 0; n < nodes.length; n++)
		{
			tIdx = n ^ minIndex;
			if (nodes[tIdx] != null && Double.compare(minVals[tIdx],Double.POSITIVE_INFINITY) != 0)
			{
				if (nodes[tIdx].intersectionExists(ray,ignore))
				{
						return true;
				}
			}
		}
		return false;
	}
	
	
	public void insertTriangle(Polygon poly, BoundingBox tBounds, int depth)
	{

		if (depth == MAX_DEPTH)
		{
			//System.out.println("non-root add: " + this.bounds + " " + depth + " " + poly.toString() + "\r\n\r\n");
			if (this.polys == null)
			{
				this.polys = new ArrayList<Polygon>();
			}
			this.polys.add(poly);
			return;
		}
		createChildren();
		int added = 0;
		for (OctreeNode tNode : nodes)
		{
			if(tBounds.intersects(tNode.bounds))
			{
				added++;
				
			}
		}
		if (added == 8)
		{
			if (this.polys == null)
			{
				this.polys = new ArrayList<Polygon>();
			}
			this.polys.add(poly);
			return;
		}
		for (OctreeNode tNode : nodes)
		{
			//if (depth < 2)
			//{
			//	System.out.println("Poly: " + poly + "{" + tNode.bounds + "} " + tBounds.intersects(tNode.bounds));
			//}
			
			if(tBounds.intersects(tNode.bounds))
			{
				//if (depth < 2)
				//{
				//System.out.println("Inserting triangle into child + " + depth);
				//}
					tNode.insertTriangle(poly,tBounds, depth+1);
			}
		}
		
	}

	


	public void createChildren()
	{
		if (hasChildNodes)
			return;
		hasChildNodes = true;
		numNodes = 8;
		double[] center = this.bounds.getCenter();
		
		//left-bottom-far
		BoundingBox b = new BoundingBox(this.bounds.getXMin(), this.bounds.getYMin(), this.bounds.getZMin(),
				center[0],center[1], center[2]);

		OctreeNode node = new OctreeNode(b);
		nodes[0] = node;

		//right-bottom-far
		b = new BoundingBox(center[0], this.bounds.getYMin(), this.bounds.getZMin(),
				this.bounds.getXMax(),center[1], center[2]);
		node = new OctreeNode(b);
		nodes[1] = node;

		//left-top-far
		b = new BoundingBox(this.bounds.getXMin(), center[1], this.bounds.getZMin(),
				center[0],this.bounds.getYMax(), center[2]);
		node = new OctreeNode(b);
		nodes[2] = node;

		//right-top-far
		b = new BoundingBox(center[0], center[1], this.bounds.getZMin(),
				this.bounds.getXMax(),this.bounds.getYMax(), center[2]);
		node = new OctreeNode(b);
		nodes[3] = node;

		//left-bottom-near
		b = new BoundingBox(this.bounds.getXMin(), this.bounds.getYMin(), center[2],
				center[0],center[1], this.bounds.getZMax());
		node = new OctreeNode(b);
		nodes[4] = node;

		//right-bottom-near
		b = new BoundingBox(center[0], this.bounds.getYMin(), center[2],
				this.bounds.getXMax(),center[1], this.bounds.getZMax());
		node = new OctreeNode(b);
		nodes[5] = node;

		//left-top-near
		b = new BoundingBox(this.bounds.getXMin(), center[1], center[2],
				center[0],this.bounds.getYMax(), this.bounds.getZMax());
		node = new OctreeNode(b);
		nodes[6] = node;

		//right-top-near
		b = new BoundingBox(center[0], center[1], center[2],
				this.bounds.getXMax(),this.bounds.getYMax(), this.bounds.getZMax());
		node = new OctreeNode(b);
		nodes[7] = node;
	}
}
