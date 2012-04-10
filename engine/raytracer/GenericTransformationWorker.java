package engine.raytracer;

import engine.util.Matrix;
import engine.world.Polygon;

public class GenericTransformationWorker implements Runnable {
	int[] data;
	int start;
	int end;
	Matrix transformation;
	Matrix nonPreservingMatrix;
	double[] tData = new double[4];
	public GenericTransformationWorker (Matrix transformation, Matrix nonPreservingMatrix, int[] data, int idx, int cnt)
	{
		this.data = data;
		this.start = idx;
		this.end = cnt;
		this.nonPreservingMatrix = nonPreservingMatrix;
		this.transformation = transformation;

	}
	

	
	public void run()
	{	
		//double mag;
		for (int x = start; x < end; x++)
		{

				Polygon.composedVertNormals[data[x]] = Matrix.multiply(nonPreservingMatrix, Polygon.vertNormals[data[x]]);
				
				/*mag = Math.sqrt(Polygon.composedVertNormals[data[x]][0] * Polygon.composedVertNormals[data[x]][0] + Polygon.composedVertNormals[data[x]][1] * Polygon.composedVertNormals[data[x]][1] + Polygon.composedVertNormals[data[x]][2] * Polygon.composedVertNormals[data[x]][2]);
				Polygon.composedVertNormals[data[x]][0] /= mag;
			    Polygon.composedVertNormals[data[x]][1] /= mag;
			    Polygon.composedVertNormals[data[x]][2] /= mag;
			    Polygon.composedVertNormals[data[x]][3] = 1;*/
				Polygon.transformedVerts[data[x]] = Matrix.multiply(transformation, Polygon.verts[data[x]]);
		}
	}
}
