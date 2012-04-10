package RayTracer;
import java.util.HashMap;


public class Graphics {
	public double minX;
	public double minY;
	public double maxX;
	public double maxY;
	public Graphics(double minX, double minY, double maxX, double maxY)
	{
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}
	
	
	public void clipModelGroups(HashMap<String,ModelGroup> groups)
	{
		// We only need a nibble of data for each vert, so we can store
		// two nibbles per byte (or two points)
		byte mask1 = 0;
		byte mask2 = 0;
		double[] vert1;
		double[] vert2;
		int[] curPoly;
		for (ModelGroup g : groups.values())
		{
			for (int p = 0; p < g.polys.length; p++)
			{
				curPoly = g.polys[p].myVerts;
				for (int x = 0; x < curPoly.length ;x+=2)
				{
					vert1 = Polygon.verts[curPoly[x]];
					vert2 = Polygon.verts[curPoly[x+1]];
					
					if (vert1[0] < minX)
						mask1 |= 0x1;
					else if (vert1[0] > maxX)
						mask1 |= 0x2;
					
					if (vert1[1] < minY)
						mask1 |= 0x4;
					else if (vert1[1] > maxY)
						mask1 |= 0x8;
					

					if (vert2[0] < minX)
						mask2 |= 0x1;
					else if (vert2[0] > maxX)
						mask2 |= 0x2;
					
					if (vert2[1] < minY)
						mask2 |= 0x4;
					else if (vert2[1] > maxY)
						mask2 |= 0x8;
			
					if ((mask1 & mask2) == 0)
					{
					//reject
					}
					
				}
			
			}
		}
		
	}
}
