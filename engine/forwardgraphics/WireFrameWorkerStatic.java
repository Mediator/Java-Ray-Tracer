package engine.forwardgraphics;
import engine.util.ImageRasterizer;
import engine.world.ModelGroup;
import engine.world.Polygon;
import engine.world.World;

public class WireFrameWorkerStatic implements Runnable {
	ImageRasterizer iw;
	ModelGroup group;
	World world;
	int start;
	int end;
	public WireFrameWorkerStatic (World world,ImageRasterizer tIW, ModelGroup group, int start, int end)
	{
		this.world = world;
		this.group = group;
		//nextPoly = new AtomicInteger();
		this.iw = tIW;
		this.start = start;
		this.end = end;
	}
	public void run()
	{	
		//System.out.println("rendering");
		int vertLength;
		Polygon curPoly;
		double[] vert1;
		double[] vert2;

		byte mask1 = 0;
		byte mask2 = 0;
		byte tmask = 0;
		int x1,x2;
		int y1,y2;
		int tx,ty;
		int clippedx = 0;
		int clippedy = 0;

		boolean reject = true;
		//boolean swapped = false;
		int dy = 0;
		int dx = 0;

		int flip = 0;
		int flip2 = 0;

		for (int p = start; p < end; p++)
		{
			curPoly = group.polys[p];
			vertLength = curPoly.myVerts.length;
			for (int v = 0; v < vertLength; v++)
			{

				mask1 = 0;
				mask2 = 0;
				tmask = 0;


				vert1 = Polygon.projectedVerts[curPoly.myVerts[v]];
				vert2 = Polygon.projectedVerts[curPoly.myVerts[((v +1) % vertLength)]];

				
			


				x1 = (vert1[0] > 0) ? (int)(vert1[0]+0.5) : (int)(vert1[0]-0.5);
				y1 = (vert1[1] > 0) ? (int)(vert1[1]+0.5) : (int)(vert1[1]-0.5);

				x2 = (vert2[0] > 0) ? (int)(vert2[0]+0.5) : (int)(vert2[0]-0.5);
				y2 = (vert2[1] > 0) ? (int)(vert2[1]+0.5) : (int)(vert2[1]-0.5);
				//x1 += (imgWidth/2);
				//y1 += (imgHeight/2);
				//x2 += (imgWidth/2);
				//y2+= (imgHeight/2);
				//System.out.println("Line from (" + x1 + ", " + y1 + ") to (" + x2 + ", " + y2 + ")");

				if (x1 < world.minX)
					mask1 |= 0x1;
				else if (x1 >= world.maxX)
					mask1 |= 0x2;

				if (y1 <= world.minY)
					mask1 |= 0x4;
				else if (y1 > world.maxY)
					mask1 |= 0x8;


				if (x2 < world.minX)
					mask2 |= 0x1;
				else if (x2 >= world.maxX)
					mask2 |= 0x2;

				if (y2 <= world.minY)
					mask2 |= 0x4;
				else if (y2 > world.maxY)
					mask2 |= 0x8;

				reject = true;


				// Based on http://en.wikipedia.org/wiki/Cohen%E2%80%93Sutherland_algorithm
				while (true) {
					if ((mask1 | mask2) <= 0) {
						reject = false;
						break;
					} else if ((mask1 & mask2) > 0) {
						break;
					} else {


						tmask = (mask1 > 0) ? mask1 : mask2;

						if ((tmask & 0x8) > 0) {
							clippedx = x1 + (x2 - x1) * (world.maxY - y1) / (y2 - y1);
							clippedy = world.maxY;
						} else if ((tmask & 0x4) > 0) {
							clippedx = x1 + (x2 - x1) * (world.minY+1 - y1) / (y2 - y1);
							clippedy = world.minY+1;
						} else if ((tmask & 0x1) > 0) {
							clippedx = world.minX;
							clippedy = y1 + (y2 - y1) * (world.minX - x1) / (x2 - x1);
						} else if ((tmask & 0x2) > 0) {
							clippedx = world.maxX-1;
							clippedy = y1 + (y2 - y1) * (world.maxX-1 - x1) / (x2 - x1);
						}

						if (mask1 > 0) {
							mask1 =0;
							x1 = clippedx;
							y1 = clippedy;
							if (x1 < world.minX)
								mask1 |= 0x1;
							else if (x1 >= world.maxX)
								mask1 |= 0x2;

							if (y1 <= world.minY)
								mask1 |= 0x4;
							else if (y1 > world.maxY)
								mask1 |= 0x8;
						} else {
							mask2 = 0;
							x2 = clippedx;
							y2 = clippedy;
							if (x2 < world.minX)
								mask2 |= 0x1;
							else if (x2 >= world.maxX)
								mask2 |= 0x2;

							if (y2 <= world.minY)
								mask2 |= 0x4;
							else if (y2 > world.maxY)
								mask2 |= 0x8;
						}
					}
				}
				
				//System.out.println(reject);
				if (!reject) {
					
					
					//System.out.println("Drawing clipped line from (" + x1 + ", " + y1 + ") to (" + x2 + ", " + y2 + ")");
					
					
					//System.out.println("Line from (" + x1 + ", " + y1 + ") to (" + x2 + ", " + y2 + ")");
					//tx = y1;
					//ty = y2;
					
					//y1 = x1;
					//y2 = x2;
					//x1 = tx;
					//x2 = ty;
					
					x1 = x1-world.minX;
					x2 = x2-world.minX;
					y1 = -(y1+world.minY);
					y2 = -(y2+world.minY);
					
					if (x1 == x2 && y1 == y2)
					{
						iw.setPixel(x1, y1, 255,255,255);
						//iw.pixels[x1][y1] = 16777215;
						//imgData[cam.imgHeight * x1 + y1] = 16777215;
						continue;
					}
					
					
					//Draw the line
	
					dx = (x2 - x1);
					dy = (y2 - y1);
					dx = dx > 0 ? dx : -dx;
					dy = dy > 0 ? dy : -dy;
					/*if (dx == 0)
					{
						if (y1 < y2)
							ty = 1;
						else
							ty = -1;
						while (y1 != y2)
						{
							//System.out.println(x1 + " " + y1);
							imgData[imgHeight * (y1) + (x1)] = 16777215;
							y1+=ty;
						}
						continue;
					}
					if (dy == 0)
					{
						if (x1 < x2)
							tx = 1;
						else
							tx = -1;
						while (x1 != x2)
						{
							imgData[imgHeight * (y1) + (x1)] = 16777215;
							x1+=tx;
						}
						continue;
					}*/
					//System.out.println(dx + " " + dy);
					if (x1 < x2)
						tx = 1;
					else
						tx = -1;

					if (y1 < y2)
						ty = 1;
					else
						ty = -1;

					flip = dx-dy;

					while ((x1 != x2 || y1 != y2))
					{
						//System.out.println (imgWidth/2 + " " + imgHeight/2);
						//if (imgWidth * (x1) + (y1) > imgData.length)
						//	System.out.println(x1 + " " + y1);
						//imgData[imgWidth * (y1) + (x1)] = 16777215;
						iw.setPixel(x1, y1, 255,255,255);
						//iw.pixels[x1][y1] = 16777215;
						//imgData[cam.imgHeight * x1 + y1] = 16777215;
						flip2 = flip*2;
						if (flip2 > -dy)
						{
							flip = flip - dy;
							x1 = x1 + tx;
						}
						if (flip2 < dx)
						{
							flip = flip + dx;
							y1 = y1 + ty;
						}
					}
					iw.setPixel(x1, y1, 255,255,255);
					//iw.pixels[x1][y1] = 16777215;
					//imgData[cam.imgHeight * x1 + y1] = 16777215;
					
				}
			}
		}
	
	}

}
