package engine.raytracer;
import java.util.Arrays;

import engine.raytracer.optimization.Intersection;
import engine.raytracer.optimization.Octree;
import engine.util.FastMath;
import engine.util.ImageRasterizer;
import engine.util.Matrix;
import engine.world.LightSource;
import engine.world.ModelGroup;
import engine.world.Polygon;
import engine.world.World;

public class RaytraceWorkerStatic implements Runnable {
	World world;
	ImageRasterizer iw;
	ModelGroup[] groups;
	int start;
	int end;
	public RaytraceWorkerStatic (World world,ImageRasterizer tIW, ModelGroup[] groups, int start, int end)
	{
		this.groups = groups;
		//nextPoly = new AtomicInteger();
		this.world = world;
		this.iw = tIW;

		this.start = start;
		this.end = end;
	}

	public double[] rayCast(double x, double y)
	{
		//System.out.println("Casting for Pixel: " + x + " " + y);
		
		double[] rgb;
		double tx  = (x) + world.minX;
		double ty  = -(world.minY+(y));
		//System.out.println("tx: " + -tx + " ty: " + -ty);
		//System.out.println(x + " " + y);
		double[] vpnFL = Matrix.multiply(world.primaryCamera.vpn,world.primaryCamera.FL);
		double[] Ux = Matrix.multiply(world.primaryCamera.camX,-tx);
		double[] Vy = Matrix.multiply(world.primaryCamera.camY,-ty);
		vpnFL[3] = 1;
		Ux[3] = 1;
		Vy[3] = 1;

		double[] pt = Matrix.add(Matrix.add(vpnFL, Ux),Vy);
		double px = world.primaryCamera.fp[0];
		double py = world.primaryCamera.fp[1];
		double pz = world.primaryCamera.fp[2];
		double ex = -pt[0];
		double ey = -pt[1];
		double ez = -pt[2];



		//System.out.println("Pixel: " + x + " " + y);
		Ray r = new Ray(px,py,pz,ex,ey,ez);
		//System.out.println("Ray: " + r);
		rgb = rayReflect(world.octree,r, 0, null);
		//System.out.println("Got Color: " + Arrays.toString(rgb));
		//System.out.println("\r\n\r\n");
		//System.out.println("Color: " + x + " " + y + " " + Arrays.toString(rgb));
		return rgb;
	}

	public void run()
	{	
		//System.out.println("Doing work for: " + start + " to " + end);
		for (double y = start; y < end; y+=1)
		{


			for (double x = 0; x < world.imgWidth; x+=1)
			{


				double[] rgb = new double[3];
				int cnt = 0;
				
				/*if (world.superSample)
				{
					for (double yi = world.superSamplingStart; yi <= world.superSamplingEnd; yi += world.superSamplingIncrement)
					{
						for (double xi = world.superSamplingStart; xi <= world.superSamplingEnd; xi += world.superSamplingIncrement)
						{
							double[] trgb = rayCast(x+xi,y+yi);
							rgb[0] += trgb[0];
							rgb[1] += trgb[1]; 
							rgb[2] += trgb[2];
							cnt++;
						}
					}
				}
				else
				{
					cnt += 1;	
					rgb = rayCast(x,y);
				}*/
				double[] trgb;
				/*
				double delta = world.superSamplingIncrement;
				trgb = rayCast(x+delta,y+delta);
				rgb[0] += trgb[0];
				rgb[1] += trgb[1]; 
				rgb[2] += trgb[2];
				cnt++;
				
				trgb = rayCast(x+delta,y-delta);
				rgb[0] += trgb[0];
				rgb[1] += trgb[1]; 
				rgb[2] += trgb[2];
				cnt++;
				
				trgb = rayCast(x-delta,y+delta);
				rgb[0] += trgb[0];
				rgb[1] += trgb[1]; 
				rgb[2] += trgb[2];
				cnt++;
				
								
				trgb = rayCast(x-delta,y-delta);
				rgb[0] += trgb[0];
				rgb[1] += trgb[1]; 
				rgb[2] += trgb[2];
				cnt++;
				*/
				trgb = rayCast(x,y);
				rgb[0] += trgb[0];
				rgb[1] += trgb[1]; 
				rgb[2] += trgb[2];
				cnt++;
				
				//

				rgb[0] = rgb[0]/cnt;
				rgb[0] = (rgb[0] < 255) ? rgb[0] : 255;
				rgb[1] = rgb[1]/cnt;
				rgb[1] = (rgb[1] < 255) ? rgb[1] : 255;
				rgb[2] = rgb[2]/cnt;
				rgb[2] = (rgb[2] < 255) ? rgb[2] : 255;

				/*double[] trgb = rayCast(x,y);
			rgb[0] += trgb[0];
			rgb[1] += trgb[1]; 
			rgb[2] += trgb[2];
			rgb[0] = (rgb[0] < 255) ? rgb[0] : 255;
			rgb[1] = (rgb[1] < 255) ? rgb[1] : 255;
			rgb[2] = (rgb[2] < 255) ? rgb[2] : 255;*/
				iw.setPixel((int)x, (int)y, (int)rgb[0],(int)rgb[1],(int)rgb[2]);
				//iw.pixels[(int)x][(int)y] = ((((int)rgb[0])&0x0ff)<<16)|((((int)rgb[1])&0x0ff)<<8)|(((int)rgb[2])&0x0ff); 

			}
		}
	}
	public double[] rayReflect(Octree oct, Ray r, int depth, Polygon ignore)
	{
	//	System.out.println("Casting Ray: " + r);
		if (depth >= world.recursiveDepth)
			return new double[] {0,0,0};
/*
		double sMin = Double.MAX_VALUE;
		double sBeta = 0, sGamma = 0;
		Polygon sPoly = null;
		ModelGroup group;

		for (int g = 0; g < groups.length; g++)
		{
			group = groups[g];
			for (int p = 0; p < group.polys.length;p++)
			{
				Polygon poly = group.polys[p];
				//System.out.println("Poly: " + poly);

				double[] LminusA = new double[3];
				LminusA[0] = r.startX - Polygon.transformedVerts[poly.myVerts[0]][0];
				LminusA[1] = r.startY - Polygon.transformedVerts[poly.myVerts[0]][1];
				LminusA[2] = r.startZ - Polygon.transformedVerts[poly.myVerts[0]][2];




				Matrix tri = new Matrix(3,3);
				tri.values[0][0] = Polygon.transformedVerts[poly.myVerts[1]][0] - Polygon.transformedVerts[poly.myVerts[0]][0];
				tri.values[0][1] = Polygon.transformedVerts[poly.myVerts[2]][0] - Polygon.transformedVerts[poly.myVerts[0]][0];
				tri.values[0][2] = -r.dirX;


				tri.values[1][0] = Polygon.transformedVerts[poly.myVerts[1]][1] - Polygon.transformedVerts[poly.myVerts[0]][1];
				tri.values[1][1] = Polygon.transformedVerts[poly.myVerts[2]][1] - Polygon.transformedVerts[poly.myVerts[0]][1];
				tri.values[1][2] = -r.dirY;


				tri.values[2][0] = Polygon.transformedVerts[poly.myVerts[1]][2] - Polygon.transformedVerts[poly.myVerts[0]][2];
				tri.values[2][1] = Polygon.transformedVerts[poly.myVerts[2]][2] - Polygon.transformedVerts[poly.myVerts[0]][2];
				tri.values[2][2] = -r.dirZ;

				double[] sol = Matrix.solve(tri, LminusA);

				if (sol[0] >= 0 && sol[1] >= 0 && (sol[0] + sol[1]) <= 1)
				{				
					System.out.println("Intersection: " + Arrays.toString(sol) + " " + poly);
					//System.out.println("INTERSECTION");
					if (sol[2] > 0  && sol[2] < sMin)
					{
						sBeta = sol[0];
						sGamma = sol[1];
						sMin = sol[2];
						sPoly = poly;
					}
				}
				else
					continue;




			}

		}
		/*
		Intersection in = oct.intersect(r);
		if (in.sPoly == null)
		{
			//System.out.println("Null poly for: " + r);
			return new double[] {0,0,0};
		}
		
		double sMin = in.sMin;
		double sBeta = in.sBeta;
		double sGamma = in.sGamma;
		Polygon sPoly = in.sPoly;
		return pointReflect(sMin, r,sPoly,sBeta,sGamma, depth);
		
		Intersection in = new Intersection();
		ModelGroup group;

		for (int g = 0; g < groups.length; g++)
		{
			group = groups[g];
			for (int p = 0; p < group.polys.length;p++)
			{
				Polygon poly = group.polys[p];
				double[]    u = new double[3];
				double[]	v = new double[3];// triangle vectors
				double[]    w0 = new double[3];
				double[]	w = new double[3];          // ray vectors
				double     a, b,c;             // params to calc ray-plane intersect

				u[0] = Polygon.transformedVerts[poly.myVerts[1]][0] - Polygon.transformedVerts[poly.myVerts[0]][0];
				u[1] = Polygon.transformedVerts[poly.myVerts[1]][1] - Polygon.transformedVerts[poly.myVerts[0]][1];
				u[2] = Polygon.transformedVerts[poly.myVerts[1]][2] - Polygon.transformedVerts[poly.myVerts[0]][2];
				
				v[0] = Polygon.transformedVerts[poly.myVerts[2]][0] - Polygon.transformedVerts[poly.myVerts[0]][0];
				v[1] = Polygon.transformedVerts[poly.myVerts[2]][1] - Polygon.transformedVerts[poly.myVerts[0]][1];
				v[2] = Polygon.transformedVerts[poly.myVerts[2]][2] - Polygon.transformedVerts[poly.myVerts[0]][2];

				 double[] n = Matrix.crossProduct3(u, v);             // cross product
				    if (n[0] == 0.0 && n[1] == 0.0 && n[2] == 0.0)            // triangle is degenerate
				        throw new IllegalArgumentException("eg: " + poly);                 // do not deal with this case
				
				w0[0] = r.startX - Polygon.transformedVerts[poly.myVerts[0]][0];
				w0[1] = r.startY - Polygon.transformedVerts[poly.myVerts[0]][1];
				w0[2] = r.startZ - Polygon.transformedVerts[poly.myVerts[0]][2];
	    
				
				a = -Matrix.dotProduct3(n,w0);
				b = Matrix.dotProduct3(n,new double[] {r.dirX, r.dirY, r.dirZ});
				if (Math.abs(b) < 0.00001) {
					continue;
				}

				// get intersect point of ray with triangle plane
				c = a / b;
				if (c < .000001)                   // ray goes away from triangle
					continue;                // => no intersect
					// for a segment, also test if (r > 1.0) => no intersect

				double I[] = new double[3];
				I[0] = r.startX + c * r.dirX;
				I[1] = r.startY + c * r.dirY;
				I[2] = r.startZ + c * r.dirZ;

				// is I inside T?
				double    uu, uv, vv, wu, wv, D;
				uu = Matrix.dotProduct3(u,u);
				uv = Matrix.dotProduct3(u,v);
				vv = Matrix.dotProduct3(v,v);
				w[0] = I[0] - Polygon.transformedVerts[poly.myVerts[0]][0];
				w[1] = I[1] - Polygon.transformedVerts[poly.myVerts[0]][1];
				w[2] = I[2] - Polygon.transformedVerts[poly.myVerts[0]][2];
				wu = Matrix.dotProduct3(w,u);
				wv = Matrix.dotProduct3(w,v);
				D = uv * uv - uu * vv;

				// get and test parametric coords
				double s, t;
				s = (uv * wv - vv * wu) / D;
				if (s < 0.000001 || s > .999998)        // I is outside T
					continue;
				t = (uv * wu - uu * wv) / D;
				if (t < 0.000001 || (s + t) > .99998)  // I is outside T
					continue;

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
		*/
		Intersection in = oct.intersect(r,ignore);
		if (in.sPoly == null)
		{
			//System.out.println("Null poly for: " + r);
			return new double[] {0,0,0};
		}
		else
		{
			return pointReflect(in,r, depth);
		}
		
	}
	
	public boolean rayReflect2(Ray r)
	{
		//System.out.println("Casting Ray: " + r);
		

		//Intersection in = new Intersection();
		ModelGroup group;
		double[]    u = new double[3];
		double[]	v = new double[3];// triangle vectors
		double[]    w0 = new double[3];
		double[]	w = new double[3];          // ray vectors
		double     a, b,c;       
		double I[] = new double[3];
		double    uu, uv, vv, wu, wv, D;
		double s, t;
		for (int g = 0; g < groups.length; g++)
		{
			group = groups[g];
			for (int p = 0; p < group.polys.length;p++)
			{
				Polygon poly = group.polys[p];
				      // params to calc ray-plane intersect

				u[0] = Polygon.transformedVerts[poly.myVerts[1]][0] - Polygon.transformedVerts[poly.myVerts[0]][0];
				u[1] = Polygon.transformedVerts[poly.myVerts[1]][1] - Polygon.transformedVerts[poly.myVerts[0]][1];
				u[2] = Polygon.transformedVerts[poly.myVerts[1]][2] - Polygon.transformedVerts[poly.myVerts[0]][2];
				
				v[0] = Polygon.transformedVerts[poly.myVerts[2]][0] - Polygon.transformedVerts[poly.myVerts[0]][0];
				v[1] = Polygon.transformedVerts[poly.myVerts[2]][1] - Polygon.transformedVerts[poly.myVerts[0]][1];
				v[2] = Polygon.transformedVerts[poly.myVerts[2]][2] - Polygon.transformedVerts[poly.myVerts[0]][2];

				// double[] n = Matrix.crossProduct3(u, v);             // cross product
				//    if (n[0] == 0.0 && n[1] == 0.0 && n[2] == 0.0)            // triangle is degenerate
				//        throw new IllegalArgumentException("eg: " + poly);                 // do not deal with this case
				
				w0[0] = r.startX - Polygon.transformedVerts[poly.myVerts[0]][0];
				w0[1] = r.startY - Polygon.transformedVerts[poly.myVerts[0]][1];
				w0[2] = r.startZ - Polygon.transformedVerts[poly.myVerts[0]][2];
	    
				
				a = -Matrix.dotProduct3(poly.normal,w0);
				b = Matrix.dotProduct3(poly.normal,r.directionVector);
				if (FastMath.abs(b) < 0.00001) {
					continue;
				}

				// get intersect point of ray with triangle plane
				c = a / b;
				if (c < 0.00001)                   // ray goes away from triangle
					continue;                // => no intersect
					// for a segment, also test if (r > 1.0) => no intersect

				
				I[0] = r.startX + c * r.dirX;
				I[1] = r.startY + c * r.dirY;
				I[2] = r.startZ + c * r.dirZ;

				// is I inside T?
				
				uu = Matrix.dotProduct3(u,u);
				uv = Matrix.dotProduct3(u,v);
				vv = Matrix.dotProduct3(v,v);
				w[0] = I[0] - Polygon.transformedVerts[poly.myVerts[0]][0];
				w[1] = I[1] - Polygon.transformedVerts[poly.myVerts[0]][1];
				w[2] = I[2] - Polygon.transformedVerts[poly.myVerts[0]][2];
				wu = Matrix.dotProduct3(w,u);
				wv = Matrix.dotProduct3(w,v);
				D = uv * uv - uu * vv;

				// get and test parametric coords

				s = (uv * wv - vv * wu) / D;
				if (s < 0.00001 || s > .99998)        // I is outside T
					continue;
				t = (uv * wu - uu * wv) / D;
				if (t < 0.00001 || (s + t) > .99998)  // I is outside T
					continue;
				//System.out.println("Intersection: " + Arrays.toString(I) + " " + poly + "\r\n\t" + c + " " + t + " " + s + "----" + a + " " + b);
				return true;
		
			}
		}
		
		return false;
		
	}
	
	public boolean checkShadows(double[] pt, double[] l, Polygon ignore)
	{
		Ray tRay = new Ray(pt[0],pt[1],pt[2],l[0],l[1],l[2]);
		//System.out.println("Checking Shadows: " + tRay);
		// External
		
		
		//Intersection in = world.octree.intersect(tRay);
		//if (in.sPoly != null)
		//{
			//Ray tRay = new Ray(l[0],l[1],l[2],-(pt[0]-l[0]),-(pt[1]-l[1]),-(pt[2]-l[2]));
			//Ray tRay = new Ray(l[0]+pt[0], l[1]+pt[1],l[2]+pt[2],l[0],l[1],l[2]);
			//boolean check = rayReflect2(tRay);//world.octree.intersect(tRay);
			boolean check = world.octree.intersectionExists(tRay, ignore);
			//Intersection in = world.octree.intersect(tRay);
			//System.out.println("Done Checking: " + check);
			return check;
			/*System.out.println("Done Shadows: " + check + in.sPoly);
			if (in.sPoly != null)
			{
				return true;
				//System.out.println("GOT DIFFERENT RESULTS");
			}
			//return true;
		//}
		
		return false;*/
	}
	/*
	 public double[] getTextureColor(double px, double py, double pz, Polygon p)
	{

		 double tmp1x = 1;
	     double tmp1y = 0;
	     double tmp2x = 0;
	     double tmp2y = 0;
	     double tmp3x = 1;
	     double tmp3y = 1;
		 
	        double dx = p.texture.getWidth();//imgBitmap.Width;
	        double dy = p.texture.getHeight();

	        double[] U = new double[3]; //Ux = 0, Uy = 0, Uz = 0;
	        double[] V = new double[3]; //Vx = 0, Vy = 0, Vz = 0;
	        double[] v2 = new double[3];
	        double distp = 0;

	        U[0] = Polygon.transformedVerts[p.myVerts[2]][0] - Polygon.transformedVerts[p.myVerts[1]][0];
	        U[1] = Polygon.transformedVerts[p.myVerts[2]][1] - Polygon.transformedVerts[p.myVerts[1]][1];
	        U[2] = Polygon.transformedVerts[p.myVerts[2]][2] - Polygon.transformedVerts[p.myVerts[1]][2];

	        v2[0] = px - Polygon.transformedVerts[p.myVerts[1]][0];
	        v2[1] = py - Polygon.transformedVerts[p.myVerts[1]][1];
	        v2[2] = pz - Polygon.transformedVerts[p.myVerts[1]][2];

	        double v2Mag = Math.sqrt(v2[0] * v2[0] + v2[1] * v2[1] + v2[2] * v2[2]);
	        
	        v2[0] /= v2Mag;
	        v2[1] /= v2Mag;
	        v2[2] /= v2Mag;
	        double jX = 0;
	        double jY = 0;
	        double jZ = 0;
	        jX = Polygon.transformedVerts[p.myVerts[1]][0] - px;
	        jY = Polygon.transformedVerts[p.myVerts[1]][1] - py;
	        jZ = Polygon.transformedVerts[p.myVerts[1]][2] - pz;
	        
	        distp = Math.sqrt(jX * jX + jY * jY + jZ*jZ);
	        

	        V[0] = Polygon.transformedVerts[p.myVerts[0]][0] - Polygon.transformedVerts[p.myVerts[1]][0];
	        V[1] = Polygon.transformedVerts[p.myVerts[0]][1] - Polygon.transformedVerts[p.myVerts[1]][1];
	        V[2] = Polygon.transformedVerts[p.myVerts[0]][2] - Polygon.transformedVerts[p.myVerts[1]][2];

	        double dU = Math.sqrt(U[0] * U[0] + U[1] * U[1] + U[2] * U[2]);
	        double dV = Math.sqrt(V[0] * V[0] + V[1] * V[1] + V[2] * V[2]);

	        U[0] /= dU;
	        U[1] /= dU;
	        U[2] /= dU;
	        
	        V[0] /= dV;
	        V[1] /= dV;
	        V[2] /= dV;
	        
	        double cost = Matrix.dotProduct(U, v2);
	        double t = Math.acos(cost);

	        double distY=0, distX=0;
	        distY = dU - distp * Math.cos(t);
	        distX = dV - distp * Math.sin(t);

	        double x1 = 0;
	        double y1 = 0;
	        y1 = GetCoord(0, dU, tmp3y * dy, tmp2y * dy, distY);
            x1 = GetCoord(0, dV, tmp1x * dx, tmp2x * dx, distX);
	        //y1 = tAlgebra.GetCoord(0, dU, tmp3y * dy, tmp2y * dy, distY);
	     //   System.out.println(dy + " " + dU + " " + distY);
	    //    y1 = ((distY) / (dU)) * (0 - dy) + dy; //tAlgebra.GetCoord(0, dU, tmp3y * dy, tmp2y * dy, distY);
	      //  x1 = ((distX) / (dV)) * (dx);//tAlgebra.GetCoord(0, dV, tmp1x * dx, tmp2x * dx, distX);
	        double[] rgb = new double[3];
	        int i1 = (int)x1, j1 = (int)y1;
	       //System.out.println(i1 + " " + j1);
	        if (i1 >= 0 && j1 >= 0 && i1 < dx &&
	               j1 < dy)
	            {
	        		int trgb = p.texture.getRGB(i1, j1);
	        		rgb[0] = (trgb & 0x00ff0000) >> 16;
	        		rgb[1] = (trgb & 0x0000ff00) >> 8;
	        		rgb[2] = trgb & 0x000000ff;
	        		//if (rgb[0] != 0.0)
	        		//	System.out.println("COLOR: " + Arrays.toString(rgb));
	        		/*rgb[0] = clr.R;
	        		rgb[1] = clr.G;
	        		rgb[2] = clr.B;
	            }
	            return rgb;
 }
	 public static double GetCoord(double i1, double i2, double w1,
             double w2, double p)
{
return ((p - i1) / (i2 - i1)) * (w2 - w1) + w1;
}*/

	 public double[] getTextureColor(Ray r, double s, double px, double py, double pz, Polygon p)
		{

		 double[] pt = new double[3];
			pt[0] = r.startX + s * r.dirX;
			pt[1] = r.startY + s * r.dirY;
			pt[2] = r.startZ + s * r.dirZ;
		 
		  // vector form triangle a to b
        double[] u = new double[3];
        double[] v = new double[3];
        u[0] = Polygon.transformedVerts[p.myVerts[1]][0] - Polygon.transformedVerts[p.myVerts[0]][0];
        u[1] = Polygon.transformedVerts[p.myVerts[1]][1] - Polygon.transformedVerts[p.myVerts[0]][1];
        u[2] = Polygon.transformedVerts[p.myVerts[1]][2] - Polygon.transformedVerts[p.myVerts[0]][2];
        
        v[0] = Polygon.transformedVerts[p.myVerts[2]][0] - Polygon.transformedVerts[p.myVerts[0]][0];
        v[1] = Polygon.transformedVerts[p.myVerts[2]][1] - Polygon.transformedVerts[p.myVerts[0]][1];
        v[2] = Polygon.transformedVerts[p.myVerts[2]][2] - Polygon.transformedVerts[p.myVerts[0]][2];
        

       
         //origin = new Vector3d(x,y,z);

         //Used for uv mapping
         double dpa = dist(Polygon.transformedVerts[p.myVerts[0]], Polygon.transformedVerts[p.myVerts[1]], Polygon.transformedVerts[p.myVerts[2]]);
         double dpb = dist(Polygon.transformedVerts[p.myVerts[1]], Polygon.transformedVerts[p.myVerts[0]], Polygon.transformedVerts[p.myVerts[2]]);
         double dpc = dist(Polygon.transformedVerts[p.myVerts[2]], Polygon.transformedVerts[p.myVerts[0]], Polygon.transformedVerts[p.myVerts[1]]);
        
         double[] uv = new double[2];
         double rad = dist(pt, Polygon.transformedVerts[p.myVerts[1]], Polygon.transformedVerts[p.myVerts[2]]);
         double rbd = dist(pt, Polygon.transformedVerts[p.myVerts[0]], Polygon.transformedVerts[p.myVerts[2]]);
         double rcd = dist(pt, Polygon.transformedVerts[p.myVerts[0]], Polygon.transformedVerts[p.myVerts[1]]);

         uv[0] = 0 * (rad/dpa) + 1 * (rbd/dpb) + 1 * (rcd/dpc);
         uv[1] = 0 * (rad/dpa) + 0 * (rbd/dpb) + 1 * (rcd/dpc);
         System.out.println(Arrays.toString(uv));
		double[] rgb = new double[3];
		uv[0] *= p.texture.getWidth();
		uv[1] *= p.texture.getHeight();
		uv[0] = uv[0] >= p.texture.getWidth() ? p.texture.getWidth()-1 : uv[0];
		uv[1] = uv[1] >= p.texture.getHeight() ? p.texture.getHeight()-1 : uv[1];
         int trgb = p.texture.getRGB((int)uv[0],(int)uv[1]);
 		rgb[0] = (trgb & 0x00ff0000) >> 16;
 		rgb[1] = (trgb & 0x0000ff00) >> 8;
 		rgb[2] = trgb & 0x000000ff;
 		return rgb;
         
		}

	 double dist(double[] r, double[] p0, double[] p1) 
	 {
		 double[] p1p0 = new double[4];
		 p1p0[0] = p1[0] - p0[0];
		 p1p0[1] = p1[1] - p0[1];
		 p1p0[2] = p1[2] - p0[2];
		 
		 double[] p0r = new double[4];
		 p0r[0] = p0[0] - r[0];
		 p0r[1] = p0[1] - r[1];
		 p0r[2] = p0[2] - r[2];


         double[] cross = Matrix.crossProduct(p1p0, p0r);
         double crossMag = Math.sqrt(cross[0] * cross[0] + cross[1] * cross[1] + cross[2] * cross[2]);
         double p1p0Mag = Math.sqrt(p1p0[0] * p1p0[0] + p1p0[1] * p1p0[1] + p1p0[2] * p1p0[2]);
         return crossMag / p1p0Mag;
	 }
	 
	 
	public double[] pointReflect(Intersection in, Ray r, int depth)
	{
		double[] rgb = new double[3];
		
		LightSource ls;
		double[] il = new double[3];
		//	il = getTextureColor(r, s,pt[0],pt[1],pt[2], p);
			//System.out.println("Ret Color: " + Arrays.toString(rgb));
	

		double[] L = new double[4];
		double[] LOriginal = new double[4];
		double[] V = new double[4];
		double[] NL = new double[4];
		//double[] S = new double[4];
		double[] R = new double[4];



		double[] rgb2;
		//int vertIndex;


		double NdotL;
		double RdotV;


		double lMag;

		double[] pointNormal = getNormal(in.sPoint[0], in.sPoint[1], in.sPoint[2], in.sBeta,in.sGamma,in.sPoly);

		for (int x = 0; x < World.lights.size(); x++)
		{
			ls = World.lights.get(x);

			LOriginal[0] = ls.location[0] - in.sPoint[0];
			LOriginal[1] = ls.location[1] - in.sPoint[1];
			LOriginal[2] = ls.location[2] - in.sPoint[2];

			LOriginal[3] = 1;
			
			
			lMag = Math.sqrt((LOriginal[0] * LOriginal[0]) + (LOriginal[1] *  LOriginal[1]) + (LOriginal[2] * LOriginal[2]));
			if (lMag != 0)
			{
				L[0] = LOriginal[0]/lMag;
				L[1] = LOriginal[1]/lMag;
				L[2] = LOriginal[2]/lMag;
				L[3] = 1;
			}
			
			
			

			rgb = Matrix.multiply(in.sPoly.material.rgb,ls.rgb);

			//p.normal[0] *= -1;
			//p.normal[1] *= -1;
			//p.normal[2] *= -1;


			




		


			V[0] = in.sPoint[0];
			V[1] = in.sPoint[1];
			V[2] = in.sPoint[2];

			V[3] = 1;


			lMag = Math.sqrt((V[0] * V[0]) + (V[1] *  V[1]) + (V[2] * V[2]));
			if (lMag != 0)
			{
				V[0] = V[0]/lMag;
				V[1] = V[1]/lMag;
				V[2] = V[2]/lMag;
				V[3] = 1;
			}



			if (Matrix.dotProduct4(pointNormal, V) < .1)
			{
				V[0] *= -1;
				V[1] *= -1;
				V[2] *= -1;
			}


			NdotL = Matrix.dotProduct4(pointNormal,L);
			//NdotL = Math.abs(NdotL);

			NL = Matrix.multiply(pointNormal,NdotL);



			R[0] = 2 * NL[0] - L[0];
			R[1] = 2 * NL[1] - L[1];
			R[2] = 2 * NL[2] - L[2];
			R[3] = 1;


			RdotV = Matrix.dotProduct4(R,V);

			if (RdotV >= 0)
			{


				if (NdotL < 0)
				{

					rgb[0] = 0;
					rgb[1] = 0;
					rgb[2] = 0;
				}
				else
				{
					rgb = Matrix.multiply(rgb, NdotL);
				}

				rgb2 = Matrix.multiply(ls.rgb,in.sPoly.material.specular);

				rgb2 = Matrix.multiply(rgb2,Math.pow(RdotV,in.sPoly.material.reflect));
				
				double[] rgbRef = new double[3];
				rgbRef = rayReflect(world.octree, new Ray(in.sPoint[0],in.sPoint[1],in.sPoint[2],R[0],R[1],R[2]),depth+1, in.sPoly);
				if (rgbRef[0] > 0.0 || rgbRef[1] > 0.0 || rgbRef[2] > 0.0)
				{
					rgbRef = Matrix.multiply(rgbRef,in.sPoly.material.specular);
					//rgbRef = Matrix.multiply(rgbRef,Math.pow(RdotV,in.sPoly.material.reflect));
					rgb2[0] += (rgbRef[0]);
					rgb2[1] += (rgbRef[1]);
					rgb2[2] += (rgbRef[2]);
				//rgb2[0] /= 3;
				//rgb2[1] /= 3;
				//rgb2[2] /= 3;
				}
				if (in.sPoly.material.transparency > 0)
				{
					rgbRef = new double[3];
					rgbRef = rayReflect(world.octree, new Ray(in.sPoint[0],in.sPoint[1],in.sPoint[2],r.dirX,r.dirY,r.dirZ),depth+1, in.sPoly);
					if (rgbRef[0] > 0.0 || rgbRef[1] > 0.0 || rgbRef[2] > 0.0)
					{
						rgbRef = Matrix.multiply(rgbRef,in.sPoly.material.transparency);
						//rgbRef = Matrix.multiply(rgbRef,Math.pow(RdotV,in.sPoly.material.reflect));
						rgb2[0] += (rgbRef[0]);
						rgb2[1] += (rgbRef[1]);
						rgb2[2] += (rgbRef[2]);
					//rgb2[0] /= 3;
					//rgb2[1] /= 3;
					//rgb2[2] /= 3;
					}
				}
			//	rgbRef[0] += ls.rgb[0];
			//	rgbRef[1] += ls.rgb[1];
			//	rgbRef[2] += ls.rgb[2];
			
				rgb[0] = rgb[0] + rgb2[0];
				rgb[1] = rgb[1] + rgb2[1];
				rgb[2] = rgb[2] + rgb2[2];


			}
			else
			{

				if (NdotL < 0)
				{
					rgb[0] = 0;
					rgb[1] = 0;
					rgb[2] = 0;

				}
				else
				{
					rgb = Matrix.multiply(rgb, NdotL);

				}
				rgb2 = new double[3];
				double[] rgbRef = new double[3];
				if (in.sPoly.material.transparency > 0)
				{
					rgbRef = new double[3];
					rgbRef = rayReflect(world.octree, new Ray(in.sPoint[0],in.sPoint[1],in.sPoint[2],r.dirX,r.dirY,r.dirZ),depth+1, in.sPoly);
					if (rgbRef[0] > 0.0 || rgbRef[1] > 0.0 || rgbRef[2] > 0.0)
					{
						rgbRef = Matrix.multiply(rgbRef,in.sPoly.material.transparency);
						//rgbRef = Matrix.multiply(rgbRef,Math.pow(RdotV,in.sPoly.material.reflect));
						rgb2[0] += (rgbRef[0]);
						rgb2[1] += (rgbRef[1]);
						rgb2[2] += (rgbRef[2]);
					//rgb2[0] /= 3;
					//rgb2[1] /= 3;
					//rgb2[2] /= 3;
					}
				}
			//	rgbRef[0] += ls.rgb[0];
			//	rgbRef[1] += ls.rgb[1];
			//	rgbRef[2] += ls.rgb[2];
			
				rgb[0] = rgb[0] + rgb2[0];
				rgb[1] = rgb[1] + rgb2[1];
				rgb[2] = rgb[2] + rgb2[2];
				
				
			}
			if (rgb[0] > 0 || rgb[1] > 0 || rgb[2] > 0)
			{
			/*	if (checkShadows(in.sPoint, LOriginal, in.sPoly))
				{	
					//System.out.println("Shadowed");
					rgb[0] *= .55;
					rgb[1] *= .55;
					rgb[2] *= .55;
					//continue;
				}*/
			}
			il[0] += rgb[0];
			il[1] += rgb[1];
			il[2] += rgb[2];
		}




		return il;

	}


	public double[] getNormal(double x, double y, double z, double beta, double gamma, Polygon p)
	{


		double[] aNormal = Polygon.composedVertNormals[p.myVerts[0]];
		double[] bNormal = Polygon.composedVertNormals[p.myVerts[1]];
		double[] cNormal = Polygon.composedVertNormals[p.myVerts[2]];
		double[] pNormal = new double[4];

		pNormal[0] = aNormal[0] + beta * (bNormal[0] - aNormal[0]) + gamma * (cNormal[0] - aNormal[0]);
		pNormal[1] = aNormal[1] + beta * (bNormal[1] - aNormal[1]) + gamma * (cNormal[1] - aNormal[1]);
		pNormal[2] = aNormal[2] + beta * (bNormal[2] - aNormal[2]) + gamma * (cNormal[2] - aNormal[2]);

		double pNorm = Math.sqrt(pNormal[0] * pNormal[0] + pNormal[1] * pNormal[1] + pNormal[2] * pNormal[2]);

		pNormal[0] = pNormal[0] / pNorm;
		pNormal[1] = pNormal[1] / pNorm;
		pNormal[2] = pNormal[2] / pNorm;
		pNormal[3] = 1;
		return pNormal;






	}

}

