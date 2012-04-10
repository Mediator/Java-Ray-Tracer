package engine.forwardgraphics;

import engine.util.Matrix;
import engine.world.Polygon;

public class TransformationWorker implements Runnable {
	int[] data;
	int start;
	int end;
	Matrix transformation;
	public TransformationWorker (Matrix transformation, int[] data, int idx, int cnt)
	{
		this.data = data;
		this.start = idx;
		this.end = cnt;
		this.transformation = transformation;
	}
	public void run()
	{	
		for (int x = start; x < end; x++)
		{
			if (x >= data.length)
				break;
			Polygon.projectedVerts[data[x]] = Matrix.multiply(transformation, Polygon.verts[data[x]]);
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
			//System.out.println(Polygon.projectedVerts[data[x]][0] + " " + Polygon.projectedVerts[data[x]][1] + " w: " + Polygon.projectedVerts[data[x]][3]);
			//Polygon.projectedVerts[data[x]][2] = Polygon.projectedVerts[data[x]][2]/ Polygon.projectedVerts[data[x]][3];
		}
	}
}
