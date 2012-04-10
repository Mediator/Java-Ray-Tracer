package RayTracer;
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
			Polygon.composedVerts[data[x]] = Matrix.multiply(transformation, Polygon.verts[data[x]]);
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
	}
}
