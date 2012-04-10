package RayTracer;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Polygon {
	public static double[][] verts;
	public static double[][] transformedVerts;
	//public static int[][] vertOwners;
	
	
	public double[][] vertColors;
	public static double[][] vertNormals;
	public static double[][] composedVertNormals;
	public static double[][] composedVerts;
	public int[] myVerts;
	public double[] normal = new double[4];
	public ModelMaterial material;
	private boolean colored = false;
	//public static volatile double maxDepth = 0;
	//public static double zShift;
	public Polygon(int[] iverts)
	{
		this.material = new ModelMaterial("Default");
		material.setRGB(.1, .1, .1);
		material.setReflectance(50);
		material.setSpecular(0.5);
		this.vertColors = new double[3][3];
		this.myVerts = iverts;
	}
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(Arrays.toString(verts[myVerts[0]]) + ", " + Arrays.toString(verts[myVerts[0]]) + " " + Arrays.toString(verts[myVerts[2]]));
		return sb.toString();
	}
	public Polygon(int[] iverts, ModelMaterial material)
	{
		this.material = material;
		this.myVerts = iverts;
		this.vertColors = new double[3][3];
	}
/*	public Polygon trianglify(int start)
	{
		if (myVerts.length == 3)
			return null;
		else if (start == myVerts.length-2)
		{
			myVerts = new int[] {myVerts[0],myVerts[start],myVerts[start+1]};
			return null;
		}
		Polygon nPoly = new Polygon(new int[] {myVerts[0],myVerts[start],myVerts[start+1]});
		nPoly.normal = this.normal;
		nPoly.material = this.material;
		return nPoly;
	}*/
	
	
	public Polygon[] trianglify(int nVertStart)
	{
		double nVert[] = new double[4];
		for (int x = 0; x < myVerts.length; x++)
		{
			nVert[0] += Polygon.verts[myVerts[x]][0];
			nVert[1] += Polygon.verts[myVerts[x]][1];
			nVert[2] += Polygon.verts[myVerts[x]][2];
			nVert[3] += Polygon.verts[myVerts[x]][3];
		}
		nVert[0] /= myVerts.length;
		nVert[1] /= myVerts.length;
		nVert[2] /= myVerts.length;
		nVert[3] /= myVerts.length;
		
		Polygon.verts[nVertStart] = nVert;
		
		Polygon[] retPolys = new Polygon[myVerts.length-1];
		
		for (int x = 1; x <= retPolys.length; x++)
		{
			retPolys[x-1] = new Polygon(new int[] {myVerts[x],nVertStart,myVerts[(x+1) % myVerts.length]});
			retPolys[x-1].normal = this.normal;
			retPolys[x-1].material = this.material;
		}
		myVerts = new int[] {myVerts[0], nVertStart,myVerts[1]};
		
		return retPolys;
	}

	

	public void colorify(Camera cam)
	{
		double[] L = new double[4];
		double[] V = new double[4];
		double[] NL = new double[4];
		double[] S = new double[4];
		double[] R = new double[4];
		double[] il = new double[3];

		double[] rgb = new double[3];
		double[] rgb2;
		int vertIndex;

		LightSource ls;

		double NdotL;
		double RdotV;


		double lMag;
		//System.out.println("Polygon Normals: " + Arrays.toString(this.normal));
		for (int vx = 0; vx < 3; vx++)
		{
			vertIndex = myVerts[vx];
			
			
			
			//System.out.println("Vert Normal: " + Arrays.toString(Polygon.transformedVerts[vertIndex]) + " " + Arrays.toString(composedVertNormals[vertIndex]));
			il = new double[3];
			//System.out.println("Coloring Vert: " + vertIndex);
			for (int x = 0; x < ModelGroup.lights.size(); x++)
			{
				ls = ModelGroup.lights.get(x);
				
				//System.out.println("Light Source");
				//System.out.println(ls);
				//System.out.println("Material");
				//System.out.println(this.material);
				rgb = Matrix.multiply(this.material.rgb,ls.rgb);

				
				//System.out.println("RGB == (materialRGB * lightRGB): \n" + Arrays.toString(rgb));
				
				//L[0] = verts[vertIndex][0] - ls.location[0];
				//L[1] = verts[vertIndex][1] - ls.location[1];
				//L[2] = verts[vertIndex][2] - ls.location[2];
				
				
				L[0] = ls.composedLocation[0] - transformedVerts[vertIndex][0];
				L[1] = ls.composedLocation[1] - transformedVerts[vertIndex][1];
				L[2] = ls.composedLocation[2] - transformedVerts[vertIndex][2];
				
				L[3] = 1;

				
				
				//System.out.println("L == (vert.location - light.location): \n" + Arrays.toString(L));
				
				lMag = Math.sqrt((L[0] * L[0]) + (L[1] *  L[1]) + (L[2] * L[2]));
				if (lMag != 0)
				{
					L[0] = L[0]/lMag;
					L[1] = L[1]/lMag;
					L[2] = L[2]/lMag;
					L[3] = 1;
				}
				//System.out.println("L HAT: \n" + Arrays.toString(L));
				
				
				//V[0] = cam.fp[0] - transformedVerts[vertIndex][0];
				//V[1] = cam.fp[1] - transformedVerts[vertIndex][1];
				//V[2] = cam.fp[2] - transformedVerts[vertIndex][2];
				
				V[0] = transformedVerts[vertIndex][0];
				V[1] = transformedVerts[vertIndex][1];
				V[2] = transformedVerts[vertIndex][2];
				
				V[3] = 1;
				
				
				//V[0] = verts[vertIndex][0] - cam.fp[0];
				//V[1] = verts[vertIndex][1] - cam.fp[1];
				//V[2] = verts[vertIndex][2] - cam.fp[2];
				//V[3] = 1;
				
				//System.out.println("V == (vert.location - cam.location): \n" + Arrays.toString(V));

				lMag = Math.sqrt((V[0] * V[0]) + (V[1] *  V[1]) + (V[2] * V[2]));
				if (lMag != 0)
				{
					V[0] = V[0]/lMag;
					V[1] = V[1]/lMag;
					V[2] = V[2]/lMag;
					V[3] = 1;
				}
				
				//System.out.println("V HAT: \n" + Arrays.toString(V));
				
				if (Matrix.dotProduct(composedVertNormals[vertIndex], V) < .1)
				{
					V[0] *= -1;
					V[1] *= -1;
					V[2] *= -1;
				}
			
				
				NdotL = Matrix.dotProduct(composedVertNormals[vertIndex],L);
				//NdotL = Math.abs(NdotL);
								
				NL = Matrix.multiply(composedVertNormals[vertIndex],NdotL);
				
				
				//System.out.println("Nl == (Vert.normal * (N dot L)): \n" + Arrays.toString(NL));
				
			//	S[0] = NL[0] - L[0]; 
				//S[1] = NL[1] - L[1]; 
				//S[2] = NL[2] - L[2]; 
				//S[3] = 1;
				
				//System.out.println("S == (Nl - L): \n" + Arrays.toString(S));
				
			//	R[0] = L[0] + (S[0] * 2);
			//	R[1] = L[1] + (S[1] * 2);
			//	R[2] = L[2] + (S[2] * 2);
			//	R[3] = 1;
				
				R[0] = 2 * NL[0] - L[0];
				R[1] = 2 * NL[1] - L[1];
				R[2] = 2 * NL[2] - L[2];
				R[3] = 1;
				
				//System.out.println("R == L + (S * 2): \n" + Arrays.toString(R));
				
				RdotV = Matrix.dotProduct(R,V);
				//RdotV = Math.abs(RdotV);
				if (RdotV >= 0)
				{
				//System.out.println("R dot V: \n" + RdotV);
					
					if (NdotL < 0)
					{
						//System.out.println("BLACK1");
						rgb[0] = 0;
						rgb[1] = 0;
						rgb[2] = 0;
					}
					else
					{
						rgb = Matrix.multiply(rgb, NdotL);
					}
			//System.out.println("RGB A == (RGB * N dot L): \n" + Arrays.toString(rgb));
				rgb2 = Matrix.multiply(ls.rgb,material.specular);
				//System.out.println("RGB B == (light.rgb * material.ks): \n" + Arrays.toString(rgb2));
				//System.out.println("Spec: (Rdot)^material.alpha " + material.reflect + " " + Math.pow(RdotV,material.reflect));
				rgb2 = Matrix.multiply(rgb2,Math.pow(RdotV,material.reflect));
				//System.out.println("RGB B  == ((LightsourceRGB * Material.S) * (R dot V)^Material.Alpha): \n" + Arrays.toString(rgb2));
				rgb[0] = rgb[0] + rgb2[0];
				rgb[1] = rgb[1] + rgb2[1];
				rgb[2] = rgb[2] + rgb2[2];
				
				//rgb[0] = rgb[0];// + rgb2[0];
				//rgb[1] = rgb[1];// + rgb2[1];
				//rgb[2] = rgb[2];// + rgb2[2];
				
				//System.out.println("RGB final == RGB A + RGB B: \n" + Arrays.toString(rgb));
				}
				else
				{

					if (NdotL < 0)
					{
						rgb[0] = 0;
						rgb[1] = 0;
						rgb[2] = 0;
						//System.out.println("BLACK1");
						//System.out.println("BLACK2");
					}
					else
					{
						rgb = Matrix.multiply(rgb, NdotL);
						//System.out.println("BLACK2");
					}
				}
				il[0] += rgb[0];
				il[1] += rgb[1];
				il[2] += rgb[2];
			}
		
			
			/*if (il[0] < 5 && il[1] < 5 && il[2] < 5)
			{
				il[0] = 168;
				il[1] = 50;
				il[2] = 122;
			}*/
			
			this.vertColors[vx][0] = il[0];
			this.vertColors[vx][1] = il[1];
			this.vertColors[vx][2] = il[2];
			
			if (this.vertColors[vx][0] < 0)
			{
				throw new IllegalArgumentException("CREAM");
			}
			//	this.vertColors[vx][0] = 0;
			if (this.vertColors[vx][1] < 0)
				throw new IllegalArgumentException("CREAM2");
				//this.vertColors[vx][1] = 0;
			if (this.vertColors[vx][2] < 0)
				throw new IllegalArgumentException("CREAM3");
				//this.vertColors[vx][2] = 0;
			/*if (this.vertColors[vx][0] > 255)
				this.vertColors[vx][0] = 255;
			if (this.vertColors[vx][1]> 255)
				this.vertColors[vx][1] = 255;
			if (this.vertColors[vx][2]> 255)
				this.vertColors[vx][2] = 255;
			*/
			
			//System.out.println("Vertex Colors: " + Arrays.toString(verts[vertIndex]) + " rgb" + Arrays.toString(this.vertColors[vx])); 
		}

	}

}
