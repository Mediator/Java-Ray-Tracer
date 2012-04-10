package RayTracer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
/*
public class RenderWorkerStatic implements Runnable {
	Camera cam;
	int[][] imgData;
	double[][] zData;
	ModelGroup group;
	int start;
	int end;
	public RenderWorkerStatic (Camera cam,int[][] imgData,double [][] zData, ModelGroup group, int start, int end)
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
		int x1,x2;
		int y1,y2;
		int clippedx = 0;
		int clippedy = 0;

		boolean reject = true;

		double dy = 0;
		double dx = 0;
		
		double deltaY;
		double rInc;
		double gInc;
		double bInc;
		double zInc;




		double curR;
		double curG;
		double curB;
		double curZ;
		
		double xDist;

		for (int p = start; p < end; p++)
		{
			curPoly = group.polys[p];
			vertLength = curPoly.myVerts.length;

			int curLines = 0;
			int[][] lines = new int[3][12];
			double[][] vertZs = new double[3][2];
			double[] curX = new double[3];
			double[] curLineR = new double[3];
			double[] curLineG = new double[3];
			double[] curLineB = new double[3];
			double[] curLineZ = new double[3];
			double[][] curColorSlope = new double[3][3];
			double[] slopes = new double[3];
			int yMax = -1, yMin = cam.imgHeight;
			int[] vert1Color = null;
			int[] vert2Color = null;
			double vert1Z;
			double vert2Z;
			curPoly.colorify(cam);
			double curZSlope[] = new double[3];
			for (int v = 0; v < vertLength; v++)
			{

				mask1 = 0;
				mask2 = 0;
				tmask = 0;


				vert1 = Polygon.composedVerts[curPoly.myVerts[v]];
				vert2 = Polygon.composedVerts[curPoly.myVerts[((v +1) % vertLength)]];

				//vert1Color = curPoly.vertColors[v];
				//vert2Color = curPoly.vertColors[((v +1) % vertLength)];

				vert1Z = Polygon.vertsZBuffer[curPoly.myVerts[v]];
				vert2Z = Polygon.vertsZBuffer[curPoly.myVerts[((v +1) % vertLength)]];


				x1 = (vert1[0] > 0) ? (int)(vert1[0]+0.5) : (int)(vert1[0]-0.5);
				y1 = (vert1[1] > 0) ? (int)(vert1[1]+0.5) : (int)(vert1[1]-0.5);

				x2 = (vert2[0] > 0) ? (int)(vert2[0]+0.5) : (int)(vert2[0]-0.5);
				y2 = (vert2[1] > 0) ? (int)(vert2[1]+0.5) : (int)(vert2[1]-0.5);

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
							clippedx = x1 + (x2 - x1) * (cam.maxY - y1) / (y2 - y1);
							clippedy = cam.maxY;
						} else if ((tmask & 0x4) > 0) {
							clippedx = x1 + (x2 - x1) * (cam.minY+1 - y1) / (y2 - y1);
							clippedy = cam.minY+1;
						} else if ((tmask & 0x1) > 0) {
							clippedx = cam.minX;
							clippedy = y1 + (y2 - y1) * (cam.minX - x1) / (x2 - x1);
						} else if ((tmask & 0x2) > 0) {
							clippedx = cam.maxX-1;
							clippedy = y1 + (y2 - y1) * (cam.maxX-1 - x1) / (x2 - x1);
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

				//System.out.println(reject);
				if (!reject) {


					x1 = x1-cam.minX;
					x2 = x2-cam.minX;
					y1 = -(y1+cam.minY);
					y2 = -(y2+cam.minY);


					if (y1 < yMin)
						yMin = y1;
					if (y2 < yMin)
						yMin = y2;

					if (y1 > yMax)
						yMax = y1;
					if (y2 > yMax)
						yMax = y2;
					
					
					if (yMax >= cam.imgHeight)
						yMax = cam.imgHeight - 1;
					
					
					
					
					
					
					
					
					
					
					// why is this mere?
					//if (curLines == 0)
					//{
					if (y1 <= y2)
					{
						dx = (x2 - x1);
						dy = (y2 - y1);
						lines[curLines][0] = x1;
						lines[curLines][1] = y1;
						lines[curLines][2] = x2;
						lines[curLines][3] = y2;
						
						//Point 1 RGB
						lines[curLines][4] = vert1Color[0];
						lines[curLines][5] = vert1Color[1];
						lines[curLines][6] = vert1Color[2];
						
						//Point 2 RGB
						lines[curLines][7] = vert2Color[0];
						lines[curLines][8] = vert2Color[1];
						lines[curLines][9] = vert2Color[2];
						
						
						vertZs[curLines][0] = vert1Z;
						vertZs[curLines][1] = vert2Z;
						curX[curLines] = -1;
		
						slopes[curLines] = (double)1 / (dy/dx);
						//System.out.println("slope: " + slopes[curLines] + "dy: " + dy + " dx: " + dx);
						//System.out.println("Line From " + x1 + "," + y1 + " to " + x2 + " " + y2 + " Colors: " + lines[curLines][4] + " " + lines[curLines][5] + " " + lines[curLines][6] + " to " + lines[curLines][7] + " " + lines[curLines][8] + " " + lines[curLines][9]);
						//System.out.println("Z From " + vert1Z + " to " + vert2Z);
						curLines++;
					}
					else
					{

						dx = (x1 - x2);
						dy = (y1 - y2);
						lines[curLines][0] = x2;
						lines[curLines][1] = y2;
						lines[curLines][2] = x1;
						lines[curLines][3] = y1;
						
						
						//Point 2 RGB
						lines[curLines][4] = vert2Color[0];
						lines[curLines][5] = vert2Color[1];
						lines[curLines][6] = vert2Color[2];
						
						//Point 1 RGB
						lines[curLines][7] = vert1Color[0];
						lines[curLines][8] = vert1Color[1];
						lines[curLines][9] = vert1Color[2];
						
						
						vertZs[curLines][0] = vert2Z;
						vertZs[curLines][1] = vert1Z;
						
						curX[curLines] = -1;
						slopes[curLines] = (double)1 / (dy/dx);
						//System.out.println("slope: " + slopes[curLines] + "dy: " + dy + " dx: " + dx);
						//System.out.println("Line From " + x2 + "," + y2 + " to " + x1 + " " + y1 + " Colors: " + lines[curLines][4] + " " + lines[curLines][5] + " " + lines[curLines][6] + " to " + lines[curLines][7] + " " + lines[curLines][8] + " " + lines[curLines][9]);
						//System.out.println("Z From " + vert2Z + " to " + vert1Z);
						curLines++;
					}
					//}

				}

			}
			if (curLines > 0)
			{
				//ArrayUtil.quicksort(0, curLines-1,lines, curX, slopes);


				deltaY = yMax - yMin;
				for (int line = 0; line < curLines; line++)
				{
					//curColorSlope[line][0] = (lines[line][4] - lines[line][7]) / deltaY;						
				//	curColorSlope[line][1] = (lines[line][5] - lines[line][8]) / deltaY;			
					//curColorSlope[line][2] = (lines[line][6] - lines[line][9]) / deltaY;
					curZSlope[line] = (vertZs[line][1] - vertZs[line][0]) / deltaY;
					curColorSlope[line][0] = (lines[line][7] - lines[line][4]) / deltaY;						
					curColorSlope[line][1] = (lines[line][8] - lines[line][5]) / deltaY;			
					curColorSlope[line][2] = (lines[line][9] - lines[line][6]) / deltaY;
					
				}
				
				//System.out.println("cur lines:" + curLines);
				double[][] fillPoints = new double[cam.imgWidth][5];
				int filledPoints = 0;
				for (int y = yMin; y <= yMax; y++)
				{
					//System.out.println("--------------------SCAN LINE " + y + "-----------------------");
					filledPoints = 0;
					for (int line = 0; line < curLines; line++)
					{
						if (y == lines[line][1] && lines[line][1] == lines[line][3])
						{
							curLineZ[line] = vertZs[line][1];
							curX[line] = lines[line][2];
							curLineR[line] =  lines[line][7];
							curLineG[line] =  lines[line][8];
							curLineB[line] =  lines[line][9];
							fillPoints[filledPoints][0] = curX[line];
							fillPoints[filledPoints][1] = curLineR[line];
							fillPoints[filledPoints][2] = curLineG[line];
							fillPoints[filledPoints][3] = curLineB[line];
							fillPoints[filledPoints][4] = curLineZ[line];
							
						//	System.out.print("x " + curX[line]);
						//	System.out.print(" y " + y);
						//	System.out.println(" color: " + curLineR[line] + " " + curLineG[line] + " " + curLineB[line]);
							filledPoints++;
							continue;
						}

						if (y == lines[line][1])
						{
							//if (curX[line] == -1)
							curX[line] = lines[line][0];
							curLineZ[line] = vertZs[line][0];
							curLineR[line] =  lines[line][4];
							curLineG[line] =  lines[line][5];
							curLineB[line] =  lines[line][6];
							//fillPoints[filledPoints++] = (int)curX[line];
							continue;

						}
						if (y == lines[line][3])
						{
							curLineZ[line] = vertZs[line][1];
							curX[line] = lines[line][2];
							curLineR[line] =  lines[line][7];
							curLineG[line] =  lines[line][8];
							curLineB[line] =  lines[line][9];
							fillPoints[filledPoints][0] = curX[line];
							fillPoints[filledPoints][1] = curLineR[line];
							fillPoints[filledPoints][2] = curLineG[line];
							fillPoints[filledPoints][3] = curLineB[line];
							fillPoints[filledPoints][4] = curLineZ[line];
						//	System.out.print("x " + curX[line]);
						//	System.out.print(" y " + y);
						//	System.out.println(" color: " + curLineR[line] + " " + curLineG[line] + " " + curLineB[line]);
							filledPoints++;
							continue;
						}
						if (y > lines[line][1] && y < lines[line][3])
						{
							curLineZ[line] += curZSlope[line];
							curX[line] += slopes[line];
							curLineR[line] +=  curColorSlope[line][0];
							curLineG[line] +=  curColorSlope[line][1];
							curLineB[line] +=  curColorSlope[line][2];
							fillPoints[filledPoints][0] = curX[line];
							fillPoints[filledPoints][1] = curLineR[line];
							fillPoints[filledPoints][2] = curLineG[line];
							fillPoints[filledPoints][3] = curLineB[line];
							fillPoints[filledPoints][4] = curLineZ[line];
						//	System.out.print("x " + curX[line]);
						//	System.out.print(" y " + y);
						//	System.out.println(" color: " + curLineR[line] + " " + curLineG[line] + " " + curLineB[line]);
							filledPoints++;
						}	

					}
					//if (filledPoints > 0 && filledPoints < 2)
					//	System.out.println("WDF");
					if (filledPoints > 0)
					{
						//System.out.println("--------------------FILLING POINTS-----------------------");
						//Sort fill points in increasing order

						ArrayUtil.quicksort(0, filledPoints-1, fillPoints);
						
						//if (fillPoints[0][1] < 10 || fillPoints[filledPoints-1][1] <10)
						//System.out.println("Filling Points y " + y + " and x between " + fillPoints[0][0] + " and " + fillPoints[filledPoints-1][0] + " "
						//+ " Color from " + fillPoints[0][1] + " " + fillPoints[0][2] + fillPoints[0][3]
						//+ " to " + fillPoints[filledPoints-1][1] + " " + fillPoints[filledPoints-1][2] + fillPoints[filledPoints-1][3]);
						//System.out.println(Arrays.toString(fillPoints[0]) + " " + Arrays.toString(fillPoints[filledPoints-1]));
						xDist = (int)Math.ceil(fillPoints[filledPoints-1][0]) - (int)Math.ceil(fillPoints[0][0]);
						curR = fillPoints[0][1];
						curG = fillPoints[0][2];
						curB = fillPoints[0][3];
						curZ = fillPoints[0][4];
						
						if (xDist <= 0)
						{
							xDist = 0;
							rInc = 0;
							gInc = 0;
							bInc = 0;
							zInc = 0;
						}
						else
						{
							rInc = (fillPoints[filledPoints-1][1] - fillPoints[0][1]) / (double)xDist;
							gInc = (fillPoints[filledPoints-1][2] - fillPoints[0][2]) / (double)xDist;
							bInc = (fillPoints[filledPoints-1][3] - fillPoints[0][3]) / (double)xDist;
							zInc = (fillPoints[filledPoints-1][4] - fillPoints[0][4]) / (double)xDist;
						}
							//if (fillPoints[0][1] < 10 || fillPoints[filledPoints-1][1] <10)
							//	System.out.println("BALLS");
							//System.out.println("Interpolating R between: " + fillPoints[0][1] + " to " + fillPoints[filledPoints-1][1] + " over " + xDist + " per inc " + rInc);
						//System.out.println(Arrays.toString(fillPoints[0]) + " " + Arrays.toString(fillPoints[filledPoints-1]));
					//		rInc = (fillPoints[0][1] - fillPoints[filledPoints-1][1]) / (double)xDist;
					//		gInc = (fillPoints[0][2] - fillPoints[filledPoints-1][2]) / (double)xDist;
					//		bInc = (fillPoints[0][3] - fillPoints[filledPoints-1][3]) / (double)xDist;
							
						for (int x = (int)Math.ceil(fillPoints[0][0]) ; x < (int)Math.ceil(fillPoints[filledPoints-1][0]); x++)
						{
						//if (y == yMin+1)
							//if (curR < 10)
							//{
								//System.out.println(
								//		" Interpolating R between: " + fillPoints[0][1] + " to " + fillPoints[filledPoints-1][1] + " over " + xDist + " per inc " + rInc +
								//		" WDF x: " + x + " y: "  + y + " RINC " + rInc +  " R: " + curR + " G: " + curG + " B: " + curB
								//		+ Arrays.toString(fillPoints[0]) + " " + Arrays.toString(fillPoints[filledPoints-1]));
							//}
							try
							{
								
								//if (curR < 0)
								//	curR = 0;
								//if (curG < 0)
								//	curG = 0;
								//if (curB < 0)
								//	curB = 0;
								if (curZ > zData[x][y])
								{

									imgData[x][y] = ((((int)curR)&0x0ff)<<16)|((((int)curG)&0x0ff)<<8)|(((int)curB)&0x0ff); 
									zData[x][y] = curZ;
								}
								//if (curR < 10)
								//{
								//	System.out.println("Before " + curR + " RINC: " +  rInc);
								//}
								curR = curR + rInc;
								//if (curR < 10)
								//{
								//	System.out.println("After " + curR + " RINC: " +  rInc);
								//}
								curG += gInc;
								curB += bInc;
								curZ += zInc;
							}
							catch (Exception ex)
							{
								System.out.println("ERRRORORORAOFDASFAS x: " + x + " y: "  + y);
								ex.printStackTrace();

							}
						}
						//System.out.println("--------------------DONE FILLING POINTS-----------------------");
					}
					//System.out.println("--------------------DONE SCANLINE " + y + "-----------------------");
				}
				//System.out.println("done");
				curLines = 0;
			}
		}
	}

	
}*/

