package engine.forwardgraphics;

import engine.util.Matrix;
import engine.world.Camera;
import engine.world.Polygon;

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
						
			if (x >= data.length)
				break;
			Polygon.composedVertNormals[data[x]] = Matrix.multiply(Matrix.multiply(cam.cameraRotationMatrix,nonPreservingMatrix), Polygon.vertNormals[data[x]]);
			
			
			
			tData = Matrix.multiply(transformation, Polygon.verts[data[x]]);
			
			
	
			
			Polygon.transformedVerts[data[x]] = tData;
			
			
			
			
			
			
			
			
			Polygon.projectedVerts[data[x]] = Matrix.multiply(projection,tData);
			if (Polygon.projectedVerts[data[x]][3] == 0)
			{
				Polygon.projectedVerts[data[x]][0] = 0;
			    Polygon.projectedVerts[data[x]][1] = 0;
			}
			else
			{
				Polygon.projectedVerts[data[x]][0] = Polygon.projectedVerts[data[x]][0]/ Polygon.projectedVerts[data[x]][3];
				Polygon.projectedVerts[data[x]][1] = Polygon.projectedVerts[data[x]][1]/ Polygon.projectedVerts[data[x]][3];
			}

		}
	}
}
