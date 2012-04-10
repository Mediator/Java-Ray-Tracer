package RayTracer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RaytraceWorkerStatic implements Runnable {
	Camera cam;
	ImageWrapper iw;
	double[][] zData;
	ModelGroup[] groups;
	int start;
	int end;
	public RaytraceWorkerStatic (Camera cam,ImageWrapper tIW,double [][] zData, ModelGroup[] groups, int start, int end)
	{
		this.groups = groups;
		//nextPoly = new AtomicInteger();
		this.cam = cam;
		this.iw = tIW;
		this.zData = zData;
		this.start = start;
		this.end = end;
	}

	public double[] rayCast(double x, double y)
	{
		double[] rgb;
		double tx  = (x) + cam.minX;
		double ty  = -(cam.minY+(y));

		//System.out.println(x + " " + y);
		double[] vpnFL = Matrix.multiply(cam.vpn,cam.FL);
		double[] Ux = Matrix.multiply(cam.camX,-tx);
		double[] Vy = Matrix.multiply(cam.camY,-ty);
		vpnFL[3] = 1;
		Ux[3] = 1;
		Vy[3] = 1;

		double[] pt = Matrix.add(Matrix.add(vpnFL, Ux),Vy);
		double px = cam.fp[0];
		double py = cam.fp[1];
		double pz = cam.fp[2];
		double ex = -pt[0];
		double ey = -pt[1];
		double ez = -pt[2];



		//System.out.println("Pixel: " + x + " " + y);
		Ray r = new Ray(px,py,pz,ex,ey,ez);
		//System.out.println("Ray: " + r);
		rgb = rayReflect(r);
		//System.out.println("Color: " + x + " " + y + " " + Arrays.toString(rgb));
		return rgb;
	}

	public void run()
	{	

		for (double y = start; y < end; y+=1)
		{


			for (double x = 0; x < cam.imgWidth; x+=1)
			{


				double[] rgb = new double[3];
				int cnt = 0;
				/*
			for (double xi = 0; xi < 1; xi +=.2)
			{
				for (double yi = 0; yi < 1; yi +=.2)
				{
					double[] trgb = rayCast(x+xi,y+yi);
					rgb[0] += trgb[0];
					rgb[1] += trgb[1]; 
					rgb[2] += trgb[2];
					cnt++;
				}
			}*/
				cnt = 1;	
				rgb = rayCast(x,y);

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
				iw.pixels[(int)x][(int)y] = ((((int)rgb[0])&0x0ff)<<16)|((((int)rgb[1])&0x0ff)<<8)|(((int)rgb[2])&0x0ff); 

			}
		}
	}
	public double[] rayReflect(Ray r)
	{
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
				LminusA[0] = r.startX - Polygon.composedVerts[poly.myVerts[0]][0];
				LminusA[1] = r.startY - Polygon.composedVerts[poly.myVerts[0]][1];
				LminusA[2] = r.startZ - Polygon.composedVerts[poly.myVerts[0]][2];




				Matrix tri = new Matrix(3,3);
				tri.values[0][0] = Polygon.composedVerts[poly.myVerts[1]][0] - Polygon.composedVerts[poly.myVerts[0]][0];
				tri.values[0][1] = Polygon.composedVerts[poly.myVerts[2]][0] - Polygon.composedVerts[poly.myVerts[0]][0];
				tri.values[0][2] = -r.endX;


				tri.values[1][0] = Polygon.composedVerts[poly.myVerts[1]][1] - Polygon.composedVerts[poly.myVerts[0]][1];
				tri.values[1][1] = Polygon.composedVerts[poly.myVerts[2]][1] - Polygon.composedVerts[poly.myVerts[0]][1];
				tri.values[1][2] = -r.endY;


				tri.values[2][0] = Polygon.composedVerts[poly.myVerts[1]][2] - Polygon.composedVerts[poly.myVerts[0]][2];
				tri.values[2][1] = Polygon.composedVerts[poly.myVerts[2]][2] - Polygon.composedVerts[poly.myVerts[0]][2];
				tri.values[2][2] = -r.endZ;

				double[] sol = Matrix.solve(tri, LminusA);

				if (sol[0] >= 0 && sol[1] >= 0 && (sol[0] + sol[1]) <= 1)
				{				
					//System.out.println("Intersection: " + Arrays.toString(sol));
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
		if (sPoly != null)
		{
			return pointReflect(sMin, r, sPoly, sBeta, sGamma);
		}
		else
			return new double[] {0,0,0};
	}
	public double[] pointReflect(double s, Ray r, Polygon p, double beta, double gamma)
	{
		double[] rgb = new double[3];
		double[] pt = new double[3];
		pt[0] = r.startX + s * r.endX;
		pt[1] = r.startY + s * r.endY;
		pt[2] = r.startZ + s * r.endZ;
		LightSource ls;



		double[] L = new double[4];
		double[] V = new double[4];
		double[] NL = new double[4];
		double[] S = new double[4];
		double[] R = new double[4];
		double[] il = new double[3];


		double[] rgb2;
		int vertIndex;


		double NdotL;
		double RdotV;


		double lMag;

		double[] pointNormal = getNormal(pt[0], pt[1], pt[2], beta,gamma,p);

		for (int x = 0; x < ModelGroup.lights.size(); x++)
		{
			ls = ModelGroup.lights.get(x);


			rgb = Matrix.multiply(p.material.rgb,ls.rgb);

			//p.normal[0] *= -1;
			//p.normal[1] *= -1;
			//p.normal[2] *= -1;


			L[0] = ls.location[0] - pt[0];
			L[1] = ls.location[1] - pt[1];
			L[2] = ls.location[2] - pt[2];

			L[3] = 1;




			lMag = Math.sqrt((L[0] * L[0]) + (L[1] *  L[1]) + (L[2] * L[2]));
			if (lMag != 0)
			{
				L[0] = L[0]/lMag;
				L[1] = L[1]/lMag;
				L[2] = L[2]/lMag;
				L[3] = 1;
			}


			V[0] = pt[0];
			V[1] = pt[1];
			V[2] = pt[2];

			V[3] = 1;


			lMag = Math.sqrt((V[0] * V[0]) + (V[1] *  V[1]) + (V[2] * V[2]));
			if (lMag != 0)
			{
				V[0] = V[0]/lMag;
				V[1] = V[1]/lMag;
				V[2] = V[2]/lMag;
				V[3] = 1;
			}



			if (Matrix.dotProduct(pointNormal, V) < .1)
			{
				V[0] *= -1;
				V[1] *= -1;
				V[2] *= -1;
			}


			NdotL = Matrix.dotProduct(pointNormal,L);
			//NdotL = Math.abs(NdotL);

			NL = Matrix.multiply(pointNormal,NdotL);



			R[0] = 2 * NL[0] - L[0];
			R[1] = 2 * NL[1] - L[1];
			R[2] = 2 * NL[2] - L[2];
			R[3] = 1;


			RdotV = Matrix.dotProduct(R,V);

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

				rgb2 = Matrix.multiply(ls.rgb,p.material.specular);

				rgb2 = Matrix.multiply(rgb2,Math.pow(RdotV,p.material.reflect));

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
			}
			il[0] += rgb[0];
			il[1] += rgb[1];
			il[2] += rgb[2];
		}




		return il;

	}


	public double[] getNormal(double x, double y, double z, double beta, double gamma, Polygon p)
	{


		double[] aNormal = Polygon.vertNormals[p.myVerts[0]];
		double[] bNormal = Polygon.vertNormals[p.myVerts[1]];
		double[] cNormal = Polygon.vertNormals[p.myVerts[2]];
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

