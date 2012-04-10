package engine.world;
import java.util.Arrays;

import engine.parallel.WorkNotifier;
import engine.util.ArrayUtil;
import engine.util.FastMath;
import engine.util.Matrix;

public class ModelGroup {
	public Polygon[] polys;
	public Matrix composedMatrix = Matrix.identity();
	public Matrix nonPreservingMatrix = Matrix.identity();
	
	public int[] workVerts;
	public String name = "";
	public int numWorkVerts = 0;
	private boolean validated = false;

	protected Object merrus = new Object();
	
	public boolean hasPolys()
	{
		return polys.length > 0;
	}
	
	public void recomputeNormals()
	{
		if (this.nonPreservingMatrix.areTheSame(Matrix.identity()))
			return;
		for (int x = 0; x < polys.length;x++)
		{

			
			double[] a,b,c;
			a = Polygon.transformedVerts[polys[x].myVerts[0]];
			b = Polygon.transformedVerts[polys[x].myVerts[1]];
			c = Polygon.transformedVerts[polys[x].myVerts[2]];
		
			
			double[] v1 = new double[4];
			v1[0] = b[0] - a[0];
			v1[1] = b[1] - a[1];
			v1[2] = b[2] - a[2];
			v1[3] = 1;

		
			double[] v2 = new double[4];
			v2[0] = c[0] - b[0];
			v2[1] = c[1] - b[1];
			v2[2] = c[2] - b[2];
			v2[3] = 1;
			
			double[] v = Matrix.crossProduct(v1, v2);

			double[] normalV = new double[4];

			double vMag = (double) Math.sqrt(Math.pow(v[0], 2) + Math.pow(v[1], 2) + Math.pow(v[2], 2));

			normalV[0]= v[0]/vMag;
			normalV[1] = v[1]/vMag;
			normalV[2] = v[2]/vMag;
			normalV[3] = 1;
		
			
			polys[x].normal = new double[] {normalV[0],normalV[1],normalV[2],1};
		}
		
		double[] curSum;
		double curUsed;
		double[] fNormal;
		//double e = 0;
		for (int vv = 0; vv < workVerts.length; vv++)
		{
			fNormal = null;
			curSum = new double[4];
			curUsed = 0;
			for (int pp = 0; pp < polys.length; pp++)
			{
				for (int pv = 0; pv < 3; pv++)
				{
					if (workVerts[vv] == polys[pp].myVerts[pv])
					{
						
						if (fNormal == null)
						{
							fNormal = new double[] {polys[pp].normal[0],polys[pp].normal[1],polys[pp].normal[2],polys[pp].normal[3]};
							curSum[0] = polys[pp].normal[0];
							curSum[1] = polys[pp].normal[1];
							curSum[2] = polys[pp].normal[2];
							curUsed++;
						}
						else
						{
							if (Matrix.dotProduct4(fNormal,polys[pp].normal) < 0)
							{
							//if (Math.abs(Matrix.dotProduct(fNormal,polys[pp].normal)) > e)
							//{
								//curSum[0] += polys[pp].normal[0];
								//curSum[1] += polys[pp].normal[1];
								//curSum[2] += polys[pp].normal[2];
								
								curSum[0] += polys[pp].normal[0]*-1;
								curSum[1] += polys[pp].normal[1]*-1;
								curSum[2] += polys[pp].normal[2]*-1;
								curUsed++;
							}
							else
							{
								//	System.out.println("Polynormal: " + pp + " " + Arrays.toString(polys[pp].normal));
								//curSum[0] += polys[pp].normal[0]*-1;
								//curSum[1] += polys[pp].normal[1]*-1;
								//curSum[2] += polys[pp].normal[2]*-1;
								///
								curSum[0] += polys[pp].normal[0];
								curSum[1] += polys[pp].normal[1];
								curSum[2] += polys[pp].normal[2];
								curUsed++;
							//curSum[3] += polys[pp].normal[3];
						
							}
						}
						
					}
				}
			}
			Polygon.composedVertNormals[workVerts[vv]] = new double[] {curSum[0]/curUsed,curSum[1]/curUsed, curSum[2]/curUsed, 1};
		   // System.out.println("Averages: " + Arrays.toString(Polygon.vertNormals[vv]));
			double vMag = (double) Math.sqrt(Math.pow(Polygon.composedVertNormals[workVerts[vv]][0], 2) + Math.pow(Polygon.composedVertNormals[workVerts[vv]][1],2) + Math.pow(Polygon.composedVertNormals[workVerts[vv]][2], 2));
			Polygon.composedVertNormals[workVerts[vv]][0] /= vMag;
			Polygon.composedVertNormals[workVerts[vv]][1] /= vMag;
			Polygon.composedVertNormals[workVerts[vv]][2] /= vMag;
			Polygon.composedVertNormals[workVerts[vv]][3] = 1;
		}
		
		
	}
	
	public boolean validate()
	{
		
		int totalPolys = polys.length;
		int totalVerts = Polygon.verts.length;
		if (validated)
			return true;
		for (int x = 0; x < polys.length;x++)
		{
			if (polys[x].myVerts.length > 3)
			{
				totalPolys += polys[x].myVerts.length-1;
				totalVerts += 1;
			}
			double[] a,b,c;
			a = Polygon.verts[polys[x].myVerts[0]];
			b = Polygon.verts[polys[x].myVerts[1]];
			c = Polygon.verts[polys[x].myVerts[2]];
		
			
			double[] v1 = new double[4];
			v1[0] = b[0] - a[0];
			v1[1] = b[1] - a[1];
			v1[2] = b[2] - a[2];
			v1[3] = 1;

		
			double[] v2 = new double[4];
			v2[0] = c[0] - b[0];
			v2[1] = c[1] - b[1];
			v2[2] = c[2] - b[2];
			v2[3] = 1;
			
			double[] v = Matrix.crossProduct(v1, v2);
			

			if (v[0] == 0 && v[1] == 0 && v[2] == 0)
			{
				if (polys[x].myVerts.length < 4)
				{
					System.out.println("Too few verticies: " + Arrays.toString(polys[x].myVerts));
					return false;
				}
				else
				{
					int idx = 3;
					while (v[0] == 0 && v[1] == 0 && v[2] == 0 && idx < polys[x].myVerts.length)
					{
						v1[0] = v2[0];
						v1[1] = v2[1];
						v1[2] = v2[2];
						v1[3] = v2[3];
						
						v2 = new double[4];
						v2[0] = Polygon.verts[polys[x].myVerts[idx]][0] - Polygon.verts[polys[x].myVerts[idx-1]][0];
						v2[1] = Polygon.verts[polys[x].myVerts[idx]][1] - Polygon.verts[polys[x].myVerts[idx-1]][1];
						v2[2] = Polygon.verts[polys[x].myVerts[idx]][2] - Polygon.verts[polys[x].myVerts[idx-1]][2];
						v2[3] = 1;
						idx++;
						
						v = Matrix.crossProduct(v1, v2);
						
					}
					if (v[0] == 0 && v[1] == 0 && v[2] == 0)
					{
						System.out.println("Single Point Face");
						return false;
					}
				}
			}

			double[] normalV = new double[4];

			double vMag = (double) Math.sqrt(Math.pow(v[0], 2) + Math.pow(v[1], 2) + Math.pow(v[2], 2));

			normalV[0]= v[0]/vMag;
			normalV[1] = v[1]/vMag;
			normalV[2] = v[2]/vMag;
			normalV[3] = 1;
		
			
			polys[x].normal = new double[] {normalV[0],normalV[1],normalV[2],1};

			for (int y = 3; y < polys[x].myVerts.length;y++)
			{

				double[] v3 = new double[4];
				v3[0] = Polygon.verts[polys[x].myVerts[y]][0] - Polygon.verts[polys[x].myVerts[y-1]][0];
				v3[1] = Polygon.verts[polys[x].myVerts[y]][1] - Polygon.verts[polys[x].myVerts[y-1]][1];
				v3[2] = Polygon.verts[polys[x].myVerts[y]][2] - Polygon.verts[polys[x].myVerts[y-1]][2];
				v3[3] = 1;
				
				double[] vCross = new double[4];
				vCross = Matrix.crossProduct(v2, v3);
				
				
				v2 = v3;
				
				double vCrossMag = (double) Math.sqrt(Math.pow(vCross[0], 2) + Math.pow(vCross[1], 2) + Math.pow(vCross[2], 2));
				double[] normal2 = new double[4];
				normal2[0]= vCross[0]/vCrossMag;
				normal2[1] = vCross[1]/vCrossMag;
				normal2[2] = vCross[2]/vCrossMag;
				normal2[3] = 1;
				
				
				double ret = Matrix.dotProduct4(normalV,v3);
				//System.out.println("Ret: " + ret);
				if (FastMath.abs(ret) > 0.01)
				{
					System.out.println("Coplanarity failure");
					return false;
				}
				
				double ret2 = Matrix.dotProduct4(normalV, normal2);
				//System.out.print(ret2);
				if (ret2 < -0.01)
				{
					System.out.println("Convexity failure");
					return false;
				}
			}
			
			double[] v3 = new double[4];
			v3[0] = Polygon.verts[polys[x].myVerts[0]][0] - Polygon.verts[polys[x].myVerts[polys[x].myVerts.length-1]][0];
			v3[1] = Polygon.verts[polys[x].myVerts[0]][1] - Polygon.verts[polys[x].myVerts[polys[x].myVerts.length-1]][1];
			v3[2] = Polygon.verts[polys[x].myVerts[0]][2] - Polygon.verts[polys[x].myVerts[polys[x].myVerts.length-1]][2];
			v3[3] = 1;
			


			double[] vCross = new double[4];
			vCross = Matrix.crossProduct(v2, v3);

			
			v2 = v3;
			
			double vCrossMag = (double) Math.sqrt(Math.pow(vCross[0], 2) + Math.pow(vCross[1], 2) + Math.pow(vCross[2], 2));
			double[] normal2 = new double[4];
			normal2[0]= vCross[0]/vCrossMag;
			normal2[1] = vCross[1]/vCrossMag;
			normal2[2] = vCross[2]/vCrossMag;
			normal2[3] = 1;

			

			
			double ret = Matrix.dotProduct4(normalV,v3);
			//System.out.println("Ret: " + ret);
			if (FastMath.abs(ret) > 0.01)
			{
				System.out.println("Coplanarity failure2");
				return false;
			}
			
			double ret2 = Matrix.dotProduct4(normalV, normal2);
			//System.out.print(ret2);
			if (ret2 < -0.01)
			{
				System.out.println("Convexity failure2");
				return false;
			}
			
		}
		
		if (this.polys.length == 0)
		{
			validated = true;
			return true;
		}
/*
		int[] owners = new int[9999];
		int numOwners;
		for (int x = 0; x < Polygon.verts.length; x++)
		{
			numOwners = 0;
			for (int pp = 0; pp < this.polys.length; pp++)
			{
				for (int pv = 0; pv < 3; pv++)
				{
					if (this.polys[pp].myVerts[pv] == x)
					{
						owners[numOwners++] = pp;
					}
				}
			}
			int[] tOwners = new int[numOwners];
			for (int z = 0; z < numOwners; z++)
			{
				tOwners[z] = owners[z];
			}
			Polygon.vertOwners[x] = tOwners;
		}
		*/
		
		
		
		for (int x = 0; x < this.polys.length; x++)
		{
			
			//System.out.println("Doting:" + Arrays.toString(fpNormal) + " " + Arrays.toString(this.polys[x].normal) + " " + Matrix.dotProduct(fpNormal, this.polys[x].normal));
			/*if (Matrix.dotProduct(fpNormal, polys[x].normal) < 0)
			{
				polys[x].normal = Matrix.multiply(polys[x].normal,-1);
			}
			System.out.println("Doting:" + Arrays.toString(fpNormal) + " " + Arrays.toString(this.polys[x].normal) + " " + Matrix.dotProduct(fpNormal, this.polys[x].normal));
			*/
		}
		
		
		
		
		if (this.polys.length != totalPolys)
		{
			//System.out.println("Polygons: " + (totalPolys));
			int originalPolyLength = this.polys.length;
			int polyPos = this.polys.length;
			this.polys = ArrayUtil.redimArray(this.polys, totalPolys);
			
			int originalVertLength = Polygon.verts.length;
			Polygon.verts = ArrayUtil.redimVerts(Polygon.verts, totalVerts);
			//System.out.println("Total Verts: " + Polygon.verts.length);
			Polygon[] rPolys;
			for (int pp = 0; pp < originalPolyLength; pp++)
			{
				
				rPolys = polys[pp].trianglify(originalVertLength);
				for (int np = 0; np < rPolys.length; np++)
				{
					polys[polyPos++] = rPolys[np];
				}
				
				originalVertLength++;
			}
		}
		

		
		workVerts = new int[Polygon.verts.length];
		boolean vertExists = false;
		for (int w = 0; w < polys.length; w++)
		{
			for (int h = 0; h < polys[w].myVerts.length; h++)
			{
				vertExists = false;
				for (int i = 0; i < numWorkVerts; i++)
				{
					if (workVerts[i] == polys[w].myVerts[h])
					{
						vertExists = true;
						break;
					}
				}
				if (!vertExists)
				{
					//System.out.println("Adding Vert: " + polys[w].myVerts[h]);
					workVerts[numWorkVerts++] = polys[w].myVerts[h];
				}
			}
		}
		if (workVerts.length != numWorkVerts)
			workVerts  = ArrayUtil.redimArray(workVerts, numWorkVerts);
		
		
		
		if (Polygon.vertNormals == null || Polygon.vertNormals.length == 0)
			Polygon.vertNormals = new double[Polygon.verts.length][4];
		else if (Polygon.vertNormals.length != Polygon.verts.length)
		{
			Polygon.vertNormals = ArrayUtil.redimVerts(Polygon.vertNormals, Polygon.verts.length);
		}
		double[] curSum;
		double curUsed;
		double[] fNormal;
		//double e = 0;
		for (int vv = 0; vv < workVerts.length; vv++)
		{
			fNormal = null;
			curSum = new double[4];
			curUsed = 0;
			for (int pp = 0; pp < polys.length; pp++)
			{
				for (int pv = 0; pv < 3; pv++)
				{
					if (workVerts[vv] == polys[pp].myVerts[pv])
					{
						
						if (fNormal == null)
						{
							fNormal = new double[] {polys[pp].normal[0],polys[pp].normal[1],polys[pp].normal[2],polys[pp].normal[3]};
							curSum[0] = polys[pp].normal[0];
							curSum[1] = polys[pp].normal[1];
							curSum[2] = polys[pp].normal[2];
							curUsed++;
						}
						else
						{
							if (Matrix.dotProduct4(fNormal,polys[pp].normal) < 0)
							{
							//if (Math.abs(Matrix.dotProduct(fNormal,polys[pp].normal)) > e)
							//{
								//curSum[0] += polys[pp].normal[0];
								//curSum[1] += polys[pp].normal[1];
								//curSum[2] += polys[pp].normal[2];
								
								curSum[0] += polys[pp].normal[0]*-1;
								curSum[1] += polys[pp].normal[1]*-1;
								curSum[2] += polys[pp].normal[2]*-1;
								curUsed++;
							}
							else
							{
								//	System.out.println("Polynormal: " + pp + " " + Arrays.toString(polys[pp].normal));
								//curSum[0] += polys[pp].normal[0]*-1;
								//curSum[1] += polys[pp].normal[1]*-1;
								//curSum[2] += polys[pp].normal[2]*-1;
								///
								curSum[0] += polys[pp].normal[0];
								curSum[1] += polys[pp].normal[1];
								curSum[2] += polys[pp].normal[2];
								curUsed++;
							//curSum[3] += polys[pp].normal[3];
						
							}
						}
						
					}
				}
			}
			Polygon.vertNormals[workVerts[vv]] = new double[] {curSum[0]/curUsed,curSum[1]/curUsed, curSum[2]/curUsed, 1};
		   // System.out.println("Averages: " + Arrays.toString(Polygon.vertNormals[vv]));
			double vMag = (double) Math.sqrt(Math.pow(Polygon.vertNormals[workVerts[vv]][0], 2) + Math.pow(Polygon.vertNormals[workVerts[vv]][1],2) + Math.pow(Polygon.vertNormals[workVerts[vv]][2], 2));
			Polygon.vertNormals[workVerts[vv]][0] /= vMag;
			Polygon.vertNormals[workVerts[vv]][1] /= vMag;
			Polygon.vertNormals[workVerts[vv]][2] /= vMag;
			Polygon.vertNormals[workVerts[vv]][3] = 1;
		}
		
		
		/*
		for (int pp = 0; pp < polys.length; pp++)
		{
			polys[pp].colorify(ModelGroup.cameras[0]);
		}*/
		

		
		validated = true;
		return true;
	}
	public int[] getUsedVectors()
	{
		return workVerts;
	}

	

	
	public void applyOpertation(Camera cam, WorkNotifier notifier, boolean zbuffer)
	{
		
		
		

	}
	
/*
	public void applyOpertation(WorkNotifier notifier)
	{
		this.notifier = notifier;
		
		workPool = new WorkPool(WorkPool.cores, this);
		workersCount.set(WorkPool.cores);
		int tasksPerThread = (numWorkVerts+(WorkPool.cores-1))/WorkPool.cores;
		int tstop;
		
		int pos = 0;
		for (int x = 0; x < WorkPool.cores; x++)
		{
			
			tstop = (x+1)*tasksPerThread;
			if (tstop > numWorkVerts)
				tstop = numWorkVerts;
			
			TransformationWorker z = new TransformationWorker(this.composedMatrix,workVerts,x*tasksPerThread,tstop);
			workPool.addWork(z);

		}

	}
	*/
	public void print()
	{
		System.out.println();
		System.out.println("Vertices(s):");
		for (int x = 0; x < Polygon.verts.length; x++)
		{
			System.out.println("vert[" + (x+1) + "] ("  + Polygon.verts[x][0] + ", " + Polygon.verts[x][1] + ", " + Polygon.verts[x][2] + ", " + Polygon.verts[x][3] + ")");
		}
		System.out.println();

		System.out.println("Polygons(s):");
		for (int x = 0; x < polys.length; x++)
		{
			System.out.print("poly:");
			for (int y = 0; y < polys[x].myVerts.length; y++)
			{
				System.out.print(" " + polys[x].myVerts[y]);
			}
			System.out.println();
		}
		System.out.println();


		System.out.println(composedMatrix);
	}



	public String toString()
	{
		return this.name;
	}


	
}
