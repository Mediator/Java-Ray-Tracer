package RayTracer;
public class ProjectionTransformationWorker implements Runnable {
	int[] data;
	int start;
	int end;
	Matrix transformation;
	Matrix nonPreservingMatrix;
	Matrix projection;
	Camera cam = null;
	double[] tData = new double[4];
	public ProjectionTransformationWorker (Camera cam, Matrix transformation, Matrix projection, Matrix nonPreservingMatrix, int[] data, int idx, int cnt)
	{
		this.data = data;
		this.start = idx;
		this.end = cnt;
		this.transformation = transformation;
		this.projection = projection;
		this.cam = cam;
		this.nonPreservingMatrix = nonPreservingMatrix;
	}
	

	
	public void run()
	{	
		
		for (int x = start; x < end; x++)
		{
			if (!cam.raytrace)
			{
				
			
			if (x >= data.length)
				break;
			Polygon.composedVertNormals[data[x]] = Matrix.multiply(cam.cameraRotationMatrix, Polygon.vertNormals[data[x]]);
			
			//double vMag = (double) Math.sqrt(Math.pow(Polygon.vertNormals[data[x]][0], 2) + Math.pow(Polygon.vertNormals[data[x]][1],2) + Math.pow(Polygon.vertNormals[data[x]][2], 2));
			//Polygon.vertNormals[data[x]][0] /= vMag;
			//Polygon.vertNormals[data[x]][1] /= vMag;
			//Polygon.vertNormals[data[x]][2] /= vMag;
			//Polygon.vertNormals[data[x]][3] = 1;
			
			
			tData = Matrix.multiply(transformation, Polygon.verts[data[x]]);
			
			
		/*	
			//Compute Vert Light
			intR = 0;
			intG = 0;
			intB = 0;
			
			
			for (LightSource ls : ModelGroup.lights)
			{
				
				
				
				intR += (k.values[0][0] * ls.r * (Matrix.dotProduct(Polygon.vertNormals[data[x]],L)) + Ks * ls.r * Math.pow(Matrix.dotProduct(V),phongRefl));
				intG +=
				intB +=
			}
			
			
			
			*/
			
			/*if (tData[2] < Polygon.maxDepth)
			{
				Polygon.maxDepth = tData[2];	
			}*/
			
			Polygon.transformedVerts[data[x]] = tData;
			
			
			
			
			
			
			
			
			Polygon.composedVerts[data[x]] = Matrix.multiply(projection,tData);
			if (Polygon.composedVerts[data[x]][3] == 0)
			{
				Polygon.composedVerts[data[x]][0] = 0;
			    Polygon.composedVerts[data[x]][1] = 0;
			}
			else
			{
				Polygon.composedVerts[data[x]][0] = Polygon.composedVerts[data[x]][0]/ Polygon.composedVerts[data[x]][3];
				Polygon.composedVerts[data[x]][1] = Polygon.composedVerts[data[x]][1]/ Polygon.composedVerts[data[x]][3];
			}
			//System.out.println(Polygon.composedVerts[data[x]][0] + " " + Polygon.composedVerts[data[x]][1] + " w: " + Polygon.composedVerts[data[x]][3]);
			//Polygon.composedVerts[data[x]][2] = Polygon.composedVerts[data[x]][2]/ Polygon.composedVerts[data[x]][3];
			}
			else
			{
				//Polygon.vertNormals[data[x]] = Matrix.multiply(nonPreservingMatrix, Polygon.vertNormals[data[x]]);
				Polygon.composedVerts[data[x]] = Matrix.multiply(transformation, Polygon.verts[data[x]]);
			}
		}
	}
}
