package engine.forwardgraphics;
/*
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;


import engine.util.ArrayUtil;
import engine.util.ScanLineBound;
import engine.world.Camera;
import engine.world.ModelGroup;
import engine.world.Polygon;

public class RenderWorkerDynamic implements Runnable {
	Camera cam;
	WritableRaster imgData;
	ModelGroup group;
	int start;
	int end;
	double[][] zData;
	public RenderWorkerDynamic (Camera cam,WritableRaster imgData, double[][] zData, ModelGroup group, int start, int end)
	{
		this.group = group;
		//nextPoly = new AtomicInteger();
		this.cam = cam;
		this.imgData = imgData;
		this.zData = zData;
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
		int x1 = 0,x2 = 0;
		int y1 = 0,y2 = 0;
		int clippedx = 0;
		int clippedy = 0;
		double clippedz = 0;
		double clippedr = 0;
		double clippedg = 0;
		double clippgedb = 0;

		int tr, tg, tb;
		
		int tx, ty;
		int pflip;
		double flip,flip2;
		boolean reject = true;

		double dy = 0;
		double dx = 0;
		double dz = 0;
		double[] dColor = new double[3];

		int inc;
		double[] tColor = new double[3];
		double tZ = 0;

		double[] vert1Color = null;
		double[] vert2Color = null;
		double vert1Z = 0;
		double vert2Z = 0;

		int tyMin, tyMax;
		ScanLineBound boundA;
		ScanLineBound boundB;

		//Make this better?
		ArrayList<ScanLineBound> savedBoundries = new ArrayList<ScanLineBound>();

		int minXClipped;
		int maxXClipped;
		double[][] clippedMinXBound;
		double[][] clippedMaxXBound;
		double[] edge1;
		double[] edge2;
		double[][] edgeList;
		int edgeCount;
		for (int p = start; p < end; p++)
		{
			//System.out.println("Starting Polygon");
			curPoly = group.polys[p];
			curPoly.colorify(cam);
			vertLength = curPoly.myVerts.length;
			minXClipped = 0;
			maxXClipped = 0;
			clippedMinXBound = new double[2][5];
			clippedMaxXBound = new double[2][5];
			edgeList = new double[6][6];
			edgeCount = 0;
			savedBoundries.clear();
			//curPoly.colorify(cam);
			for (int v = 0; v < vertLength; v++)
			{
				
				
				mask1 = 0;
				mask2 = 0;
				tmask = 0;


				vert1 = Polygon.projectedVerts[curPoly.myVerts[v]];
				vert2 = Polygon.projectedVerts[curPoly.myVerts[((v + 1) % vertLength)]];

				vert1Color = curPoly.vertColors[v];
				vert2Color = curPoly.vertColors[((v +1) % vertLength)];

				vert1Z = Polygon.transformedVerts[curPoly.myVerts[v]][2];
				vert2Z = Polygon.transformedVerts[curPoly.myVerts[((v + 1) % vertLength)]][2];

				
		


				

				x1 = (vert1[0] > 0) ? (int)(vert1[0]+0.5) : (int)(vert1[0]-0.5);
				y1 = (vert1[1] > 0) ? (int)(vert1[1]+0.5) : (int)(vert1[1]-0.5);

				x2 = (vert2[0] > 0) ? (int)(vert2[0]+0.5) : (int)(vert2[0]-0.5);
				y2 = (vert2[1] > 0) ? (int)(vert2[1]+0.5) : (int)(vert2[1]-0.5);
				reject = false;
				/*
				if (x1 < cam.minX)
					mask1 |= 0x1;
				else if (x1 >= cam.maxX)
					mask1 |= 0x2;

				if (y1 <= cam.minY)
					mask1 |= 0x4;
				else if (y1 > cam.maxY)
					mask1 |= 0x8;


				if (x2 < cam.minX)
					mask2 |= 0x1;
				else if (x2 >= cam.maxX)
					mask2 |= 0x2;

				if (y2 <= cam.minY)
					mask2 |= 0x4;
				else if (y2 > cam.maxY)
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
							//System.out.println("Clipping Line: " + x1 + " " + y1 + " to " + x2 + " " + y2);
							clippedx = x1 + (x2 - x1) * (cam.maxY - y1) / (y2 - y1);
							clippedy = cam.maxY;
							//System.out.println("Clipped Top : " + clippedx + " " + clippedy);
						} else if ((tmask & 0x4) > 0) {
							//System.out.println("Clipping Line: " + x1 + " " + y1 + " to " + x2 + " " + y2);
							clippedx = x1 + (x2 - x1) * (cam.minY+1 - y1) / (y2 - y1);
							clippedy = cam.minY+1;
							//System.out.println("Clipped Bottom : " + clippedx + " " + clippedy);
						} else if ((tmask & 0x1) > 0) {
							//System.out.println("Clipping Line: " + x1 + " " + y1 + " to " + x2 + " " + y2);
							clippedx = cam.minX;
							clippedy = y1 + (y2 - y1) * (cam.minX - x1) / (x2 - x1);
							//System.out.println("Clipped Left : " + clippedx + " " + clippedy);
						} else if ((tmask & 0x2) > 0) {
							//System.out.println("Clipping Line: " + x1 + " " + y1 + " to " + x2 + " " + y2);
							clippedx = cam.maxX-1;
							clippedy = y1 + (y2 - y1) * (cam.maxX-1 - x1) / (x2 - x1);
							//System.out.println("Clipped Right : " + clippedx + " " + clippedy);
						}

						if (mask1 > 0) {
							mask1 =0;
							x1 = clippedx;
							y1 = clippedy;
							if (x1 < cam.minX)
								mask1 |= 0x1;
							else if (x1 >= cam.maxX)
								mask1 |= 0x2;

							if (y1 <= cam.minY)
								mask1 |= 0x4;
							else if (y1 > cam.maxY)
								mask1 |= 0x8;
						} else {
							mask2 = 0;
							x2 = clippedx;
							y2 = clippedy;
							if (x2 < cam.minX)
								mask2 |= 0x1;
							else if (x2 >= cam.maxX)
								mask2 |= 0x2;

							if (y2 <= cam.minY)
								mask2 |= 0x4;
							else if (y2 > cam.maxY)
								mask2 |= 0x8;
						}
					}
				}
				// END COMMENT
				
				
				

				//System.out.println(reject);
				if (!reject) {
					x1 = x1-cam.minX;
					x2 = x2-cam.minX;
					y1 = -(y1+cam.minY);
					y2 = -(y2+cam.minY);


					//System.out.println("Unrejected Line From " + x1 + "," + y1 + " to " + x2 + " " + y2);
					edgeList[edgeCount][0] = x1;
					edgeList[edgeCount][1] = y1;
					edgeList[edgeCount][2] = vert1Color[0];
					edgeList[edgeCount][3] = vert1Color[1];
					edgeList[edgeCount][4] = vert1Color[2];
					edgeList[edgeCount][5] = vert1Z;
					edgeCount++;
					edgeList[edgeCount][0] = x2;
					edgeList[edgeCount][1] = y2;
					edgeList[edgeCount][2] = vert2Color[0];
					edgeList[edgeCount][3] = vert2Color[1];
					edgeList[edgeCount][4] = vert2Color[2];
					edgeList[edgeCount][5] = vert2Z;
					edgeCount++;
				}
			}

			for (int edg = 0; edg < (edgeCount-1); edg+=2)
			{
				if (edgeList[edg+1][0] < edgeList[edg][0])
				{
					x2 = (int)edgeList[edg][0];
					y2 = (int)edgeList[edg][1];
					vert2Color[0] = edgeList[edg][2];
					vert2Color[1] = edgeList[edg][3];
					vert2Color[2] = edgeList[edg][4];
					vert2Z = edgeList[edg][5];

					x1 = (int)edgeList[edg+1][0];
					y1 = (int)edgeList[edg+1][1];
					vert1Color[0] = edgeList[edg+1][2];
					vert1Color[1] = edgeList[edg+1][3];
					vert1Color[2] = edgeList[edg+1][4];
					vert1Z = edgeList[edg+1][5];

				}
				else
				{
					x1 = (int)edgeList[edg][0];
					y1 = (int)edgeList[edg][1];
					vert1Color[0] = edgeList[edg][2];
					vert1Color[1] = edgeList[edg][3];
					vert1Color[2] = edgeList[edg][4];
					vert1Z = edgeList[edg][5];

					x2 = (int)edgeList[edg+1][0];
					y2 = (int)edgeList[edg+1][1];
					vert2Color[0] = edgeList[edg+1][2];
					vert2Color[1] = edgeList[edg+1][3];
					vert2Color[2] = edgeList[edg+1][4];
					vert2Z = edgeList[edg+1][5];
				}
				//System.out.println("Line from " + x1 + " " + y1 + " to " + x2 + " " + y2);
				//System.out.println("v1: " +  Arrays.toString(vert1Color) + " v2: " + Arrays.toString(vert2Color));

				if (y1 > y2)
				{
					tyMax = y1;
				}
				else
				{
					tyMax = y2;
				}

				dx = (x2 - x1);
				dy = (y2 - y1);
				dx = dx > 0 ? dx : -dx;
				dy = dy > 0 ? dy : -dy;




				if (x1 < x2)
					tx = 1;
				else
					tx = -1;

				if (y1 < y2)
					ty = 1;
				else
					ty = -1;

				flip = dx-dy;

				tColor[0] = vert1Color[0];
				tColor[1] = vert1Color[1];
				tColor[2] = vert1Color[2];
				tZ = vert1Z;

				if (dy == 1)
				{
					dz = (vert2Z - vert1Z) / (double)2;
					dColor[0] = (vert2Color[0] - vert1Color[0]) / (double)2;
					dColor[1] = (vert2Color[1] - vert1Color[1]) / (double)2;
					dColor[2] = (vert2Color[2] - vert1Color[2]) / (double)2;
				}
				else
				{
				dz = (vert2Z - vert1Z) / ((double)dy+1);
				dColor[0] = (vert2Color[0] - vert1Color[0]) / ((double)dy + 1);
				dColor[1] = (vert2Color[1] - vert1Color[1]) / ((double)dy + 1);
				dColor[2] = (vert2Color[2] - vert1Color[2]) / ((double)dy + 1);
				}

				if (dy == 0)
				{
					//savedBoundries.add(new ScanLineBound(x1,y1,new double[] {tColor[0],tColor[1],tColor[2]},tZ));
					//savedBoundries.add(new ScanLineBound(x1,y1,new double[] {tColor[0],tColor[1],tColor[2]},tZ));
				//	savedBoundries.add(new ScanLineBound(x2,y1,new double[] {vert2Color[0],vert2Color[1],vert2Color[2]},vert2Z));
					continue;
				}


				if (y1 != tyMax)
				{
					//if (y1 == 483)
					//		System.out.println("Adding boundryA: " + new ScanLineBound(x1,y1,new double[] {tColor[0],tColor[1],tColor[2]},tZ));
					if (y1 >= cam.imgHeight || y1 < 0);
					else
					savedBoundries.add(new ScanLineBound(x1,y1,new double[] {tColor[0],tColor[1],tColor[2]},tZ));
				}
				//System.out.println("Starting Bresenhams");
				while ((x1 != x2 || y1 != y2))
				{
					flip2 = flip*2;

				
					if (flip2 > -dy)
					{
						flip = flip - dy;
						x1 = x1 + tx;
					}
					if (flip2 < dx)
					{
						flip = flip + dx;
						tColor[0] = tColor[0] + dColor[0];
						tColor[1] = tColor[1] + dColor[1];
						tColor[2] = tColor[2] + dColor[2];
						tZ = tZ + dz;
						y1 = y1 + ty;
						if (y1 != tyMax)
						{
								//if (y1 == 483)
								//	System.out.println("ty:" + ty + "Adding boundryB: " + new ScanLineBound(x1,y1,new double[] {tColor[0],tColor[1],tColor[2]},tZ));
							if (y1 >= cam.imgHeight || y1 < 0)
								continue;
							savedBoundries.add(new ScanLineBound(x1,y1,new double[] {tColor[0],tColor[1],tColor[2]},tZ));
						}	
					}
				}
				//System.out.println("Ending Bresenhams");
			}

			if (savedBoundries.size() == 0)
				continue;
			ArrayUtil.quicksort(0, savedBoundries.size()-1,savedBoundries);

			int curY = Integer.MIN_VALUE;
			ArrayList<ScanLineBound> yBounds = new ArrayList<ScanLineBound>();
			if (savedBoundries.size() % 2 != 0)
			{
				System.out.println("Uneven number of boundries??!");
			}

			for (int x = 0; x < savedBoundries.size(); x++ )
			{

				boundA = savedBoundries.get(x);

				if (boundA.y != curY)
				{
					curY = boundA.y;
					
					if (yBounds.size() > 0)
					{
						if (yBounds.size() < 2)
						{
							System.out.println("Unmatched Bound");
							continue;
						}
						for (int b = 0; b < yBounds.size()-1; b+=2)
						{
							boundA = yBounds.get(b);
							boundB = yBounds.get(b+1);
							

							if (boundA.x > boundB.x)
							{
								boundA = yBounds.get(b+1);
								boundB = yBounds.get(b);
							}
							if (boundA.x == boundB.x)
							{
								continue;
							}
							if (boundA.x >= cam.imgWidth)
								continue;
							if (boundA.x < 0)
							{
								
								tx = boundA.x;
								ty = boundA.y;
								tZ = boundA.z;
								tColor[0] = boundA.rgb[0];
								tColor[1] = boundA.rgb[1];
								tColor[2] = boundA.rgb[2];

								dx = boundB.x - boundA.x;
								dx = (dx < 0) ? dx * -1: dx;
								dz = (boundB.z - boundA.z) / ((double)dx);
								inc = (boundA.x > boundB.x) ? -1: 1;
								dColor[0] = (boundB.rgb[0] - boundA.rgb[0]) / ((double)dx);
								dColor[1] = (boundB.rgb[1] - boundA.rgb[1]) / ((double)dx);
								dColor[2] = (boundB.rgb[2] - boundA.rgb[2]) / ((double)dx);
								
								boundA.rgb[0] = tColor[0] + (dColor[0] * (-(boundA.x -1)));
								boundA.rgb[1] = tColor[1] + (dColor[1] * (-(boundA.x -1)));
								boundA.rgb[2] = tColor[2] + (dColor[2] * (-(boundA.x -1)));
								boundA.z = tZ + (dz * (-(boundA.x -1)));
								boundA.x = 0;
								//CLIP X
							}
							if (boundB.x >= cam.imgWidth)
							{
								tx = boundA.x;
								ty = boundA.y;
								tZ = boundA.z;
								tColor[0] = boundA.rgb[0];
								tColor[1] = boundA.rgb[1];
								tColor[2] = boundA.rgb[2];

								dx = boundB.x - boundA.x;
								dx = (dx < 0) ? dx * -1: dx;
								dz = (boundB.z - boundA.z) / ((double)dx);
								inc = (boundA.x > boundB.x) ? -1: 1;
								dColor[0] = (boundB.rgb[0] - boundA.rgb[0]) / ((double)dx);
								dColor[1] = (boundB.rgb[1] - boundA.rgb[1]) / ((double)dx);
								dColor[2] = (boundB.rgb[2] - boundA.rgb[2]) / ((double)dx);
								
								boundB.rgb[0] = tColor[0] + (dColor[0] * (cam.imgWidth -1 - boundA.x));
								boundB.rgb[1]= tColor[1] + (dColor[1] * (cam.imgWidth -1 - boundA.x));
								boundB.rgb[2] = tColor[2] + (dColor[2] * (cam.imgWidth -1 - boundA.x));
								boundB.z = tZ + (dz * (cam.imgWidth -1 - boundA.x));
								boundB.x = cam.imgWidth-1;
								//ClIP X
							}
							//if (boundA.x > 550 && boundB.x < 620)
							//if (boundA.y > 480 && boundA.y < 485)
							//	System.out.println("Bound From: " + boundA + " to " + boundB);
							tx = boundA.x;
							ty = boundA.y;
							tZ = boundA.z;
							tColor[0] = boundA.rgb[0];
							tColor[1] = boundA.rgb[1];
							tColor[2] = boundA.rgb[2];

							dx = boundB.x - boundA.x;
							dx = (dx < 0) ? dx * -1: dx;
							dz = (boundB.z - boundA.z) / ((double)dx+1);
							inc = (boundA.x > boundB.x) ? -1: 1;
							dColor[0] = (boundB.rgb[0] - boundA.rgb[0]) / ((double)dx+1);
							dColor[1] = (boundB.rgb[1] - boundA.rgb[1]) / ((double)dx+1);
							dColor[2] = (boundB.rgb[2] - boundA.rgb[2]) / ((double)dx+1);
							if (tZ > zData[tx][ty])
							{
							//	System.out.println("Coloring: " + tx + " " + ty + " " + tColor[0] + " " + tColor[1] + " " + tColor[2]);
								tr = (tColor[0] <= 255) ? (int)tColor[0] : 255;
								tg = (tColor[1] <= 255) ? (int)tColor[1] : 255;
								tb = (tColor[2] <= 255) ? (int)tColor[2] : 255;
								imgData.setPixel(tx,ty,new double[] {tr,tg,tb}); 
								//imgData[tx][ty] = (((0)&0x0ff)<<16)|(((0)&0x0ff)<<8)|((255)&0x0ff);			
								zData[tx][ty] = tZ;
							}
							while (tx < boundB.x)
							{

								tx += inc;
								tZ += dz;
								tColor[0] += dColor[0];
								tColor[1] += dColor[1];
								tColor[2] += dColor[2];


								if (tZ> zData[tx][ty])
								{
								//	System.out.println("Coloring: " + tx + " " + ty + " " + tColor[0] + " " + tColor[1] + " " + tColor[2]);
									tr = (tColor[0] <= 255) ? (int)tColor[0] : 255;
									tg = (tColor[1] <= 255) ? (int)tColor[1] : 255;
									tb = (tColor[2] <= 255) ? (int)tColor[2] : 255;
									imgData.setPixel(tx,ty,new double[] {tr,tg,tb}); 
									zData[tx][ty] = tZ;
								}
							}
							//if (tZ >= zData[tx][ty])
							//{
							//imgData[tx][ty] = (((0)&0x0ff)<<16)|(((0)&0x0ff)<<8)|((255)&0x0ff);		
							//}
							
						}
					}
					yBounds.clear();
					x--;
				}
				else
				{
					yBounds.add(boundA);
					continue;
				}
			}
			
			if (yBounds.size() > 0)
			{
				if (yBounds.size() < 2)
				{
					System.out.println("Unmatched Bound");
					continue;
				}
				for (int b = 0; b < yBounds.size()-1; b+=2)
				{
					boundA = yBounds.get(b);
					boundB = yBounds.get(b+1);


					if (boundA.x > boundB.x)
					{
						boundA = yBounds.get(b+1);
						boundB = yBounds.get(b);
					}
					if (boundA.x == boundB.x)
					{
						continue;
					}
					if (boundA.x >= cam.imgWidth)
						continue;
					if (boundA.x < 0)
					{
						
						tx = boundA.x;
						ty = boundA.y;
						tZ = boundA.z;
						tColor[0] = boundA.rgb[0];
						tColor[1] = boundA.rgb[1];
						tColor[2] = boundA.rgb[2];

						dx = boundB.x - boundA.x;
						dx = (dx < 0) ? dx * -1: dx;
						dz = (boundB.z - boundA.z) / ((double)dx+1);
						inc = (boundA.x > boundB.x) ? -1: 1;
						dColor[0] = (boundB.rgb[0] - boundA.rgb[0]) / ((double)dx+1);
						dColor[1] = (boundB.rgb[1] - boundA.rgb[1]) / ((double)dx+1);
						dColor[2] = (boundB.rgb[2] - boundA.rgb[2]) / ((double)dx+1);
						
						boundA.rgb[0] = tColor[0] + (dColor[0] * -boundA.x);
						boundA.rgb[1]= tColor[1] + (dColor[1] * -boundA.x);
						boundA.rgb[2] = tColor[2] + (dColor[2] * -boundA.x);
						boundA.z = tZ + (dz * -boundA.x);
						boundA.x = 0;
						//CLIP X
					}
					if (boundB.x >= cam.imgWidth)
					{
						tx = boundA.x;
						ty = boundA.y;
						tZ = boundA.z;
						tColor[0] = boundA.rgb[0];
						tColor[1] = boundA.rgb[1];
						tColor[2] = boundA.rgb[2];

						dx = boundB.x - boundA.x;
						dx = (dx < 0) ? dx * -1: dx;
						dz = (boundB.z - boundA.z) / ((double)dx);
						inc = (boundA.x > boundB.x) ? -1: 1;
						dColor[0] = (boundB.rgb[0] - boundA.rgb[0]) / ((double)dx);
						dColor[1] = (boundB.rgb[1] - boundA.rgb[1]) / ((double)dx);
						dColor[2] = (boundB.rgb[2] - boundA.rgb[2]) / ((double)dx);
						
						boundB.rgb[0] = tColor[0] + (dColor[0] * (cam.imgWidth -1 - boundA.x));
						boundB.rgb[1]= tColor[1] + (dColor[1] * (cam.imgWidth -1 - boundA.x));
						boundB.rgb[2] = tColor[2] + (dColor[2] * (cam.imgWidth -1 - boundA.x));
						boundB.z = tZ + (dz * (cam.imgWidth -1 - boundA.x));
						boundB.x = cam.imgWidth-1;
						//ClIP X
					}
					
					
					
					//if (boundA.x > 550 && boundB.x < 620)
					//	if (boundA.y > 480 && boundA.y < 485)
					//		System.out.println("Bound From: " + boundA + " to " + boundB);

					tx = boundA.x;
					ty = boundA.y;
					tZ = boundA.z;
					tColor[0] = boundA.rgb[0];
					tColor[1] = boundA.rgb[1];
					tColor[2] = boundA.rgb[2];
					dx = boundB.x - boundA.x;
					dx = (dx < 0) ? dx * -1: dx;
					dz = (boundB.z - boundA.z) / ((double)dx+1);
					inc = (boundA.x > boundB.x) ? -1: 1;
					dColor[0] = (boundB.rgb[0] - boundA.rgb[0]) / ((double)dx+1);
					dColor[1] = (boundB.rgb[1] - boundA.rgb[1]) / ((double)dx+1);
					dColor[2] = (boundB.rgb[2] - boundA.rgb[2]) / ((double)dx+1);
					//if (tx != xMax)
					//{
					if (tZ > zData[tx][ty])
					{
					//	System.out.println("Coloring: " + tx + " " + ty + " " + tColor[0] + " " + tColor[1] + " " + tColor[2]);
						tr = (tColor[0] <= 255) ? (int)tColor[0] : 255;
						tg = (tColor[1] <= 255) ? (int)tColor[1] : 255;
						tb = (tColor[2] <= 255) ? (int)tColor[2] : 255;
						imgData.setPixel(tx,ty,new double[] {tr,tg,tb}); 
						//imgData[tx][ty] = (((0)&0x0ff)<<16)|(((0)&0x0ff)<<8)|((255)&0x0ff);			
						zData[tx][ty] = tZ;
					}
					while (tx < boundB.x)
					{

						tx += inc;
						tZ += dz;
						tColor[0] += dColor[0];
						tColor[1] += dColor[1];
						tColor[2] += dColor[2];


						if (tZ > zData[tx][ty] )
						{
						
						//	System.out.println("Coloring: " + tx + " " + ty + " " + tColor[0] + " " + tColor[1] + " " + tColor[2]);
							tr = (tColor[0] <= 255) ? (int)tColor[0] : 255;
							tg = (tColor[1] <= 255) ? (int)tColor[1] : 255;
							tb = (tColor[2] <= 255) ? (int)tColor[2] : 255;
							imgData.setPixel(tx,ty,new double[] {tr,tg,tb}); 
							zData[tx][ty] = tZ;
						}
					}
					//if (tZ >= zData[tx][ty])
					//{
					//imgData[tx][ty] = (((0)&0x0ff)<<16)|(((0)&0x0ff)<<8)|((255)&0x0ff);		
					//}					
				}
			}
				
		}
	}

}
*/
