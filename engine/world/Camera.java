package engine.world;
import java.util.Arrays;

import engine.util.Matrix;


public class Camera {

	public String name;
	public double FL;
	public double[] fp;
	public double[] vpn;
	public double[] lookat;
	public double[] vup;
	
	public double[] camX;
	public double[] camY;
	
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("FL: " + FL);
		sb.append("\n");
		sb.append("FP: " + Arrays.toString(fp));
		sb.append("\n");
		sb.append("VPN: " + Arrays.toString(vpn));
		sb.append("\n");
		sb.append("VUP: " + Arrays.toString(vup));
		sb.append("\n");
		sb.append(FL + " " + fp[0] + " " + fp[1] + " " + fp[2] + " " + vpn[0] + " " + vpn[1] + " " + vpn[2] + " " + vup[0] + " " + vup[1] + " " + vup[2]);
		
		
		
		return sb.toString();
	}
	
	public Matrix cameraMatrix = null;
	
	public Matrix cameraCoordsMatrix = null;
	public Matrix projectionMatrix = null;
	public Matrix cameraRotationMatrix = null;
	
	//int workersCount; //= new AtomicInteger();
	
	public Camera(String name)
	{
		this.name = name;
	}


	
	public boolean isCameraInitialized()
	{
		
		return cameraMatrix != null;
	}

	public void initializeCamera(double FL,
			double fpX, double fpY, double fpZ,
			double vpnX, double vpnY, double vpnZ,
			double vupX, double vupY, double vupZ)
	{


		//this.FL = FL;
		this.FL = (FL < 0) ? FL * -1 : FL;
		this.fp = new double[] {fpX, fpY, fpZ, 1};
		this.vpn = new double[] {vpnX, vpnY, vpnZ, 1};

		//for (int tt = 0; tt < fp.length; tt++)
		//	System.out.println("FP: " + fp[tt]);

		//for (int tt = 0; tt < vpn.length; tt++)
		//	System.out.println("VPN: " + vpn[tt]);

		double vpnMag = Math.sqrt(Math.pow(this.vpn[0],2) + Math.pow(this.vpn[1],2) + Math.pow(this.vpn[2],2));

		this.vpn[0] = this.vpn[0]/vpnMag;
		this.vpn[1] = this.vpn[1]/vpnMag;
		this.vpn[2] = this.vpn[2]/vpnMag;

		//for (int tt = 0; tt < vpn.length; tt++)
		//	System.out.println("VPN HAT: " + vpn[tt]);

		this.vup = new double[] {vupX, vupY, vupZ, 1};

		//for (int tt = 0; tt < vup.length; tt++)
		//	System.out.println("VUP: " + vup[tt]);



		//for (int tt = 0; tt < vup.length; tt++)
		//		System.out.println("VUP HAT: " + vup[tt]);

		//if (Math.abs(Matrix.dotProduct(vup, vpn)) > 0.1)
		//{
		//	throw new IllegalArgumentException("Invalid VUP");
		//}

		double[] u = Matrix.crossProduct(this.vup, this.vpn);

		double uMag = Math.sqrt(Math.pow(u[0],2) + Math.pow(u[1],2) + Math.pow(u[2],2));

		u[0] = u[0]/uMag;
		u[1] = u[1]/uMag;
		u[2] = u[2]/uMag;

		this.camX = u;

		//for (int tt = 0; tt < u.length; tt++)
		//	System.out.println("u: " + u[tt]);

		double[] v = Matrix.crossProduct(this.vpn,u);

		
		this.camY = v;
		//for (int tt = 0; tt < u.length; tt++)
		//	System.out.println("v: " + v[tt]);

		Matrix trans = Matrix.translate(this.fp[0]*-1, this.fp[1]*-1, this.fp[2]*-1);

		//System.out.println("Translation:\n" + trans);

		Matrix rotate = Matrix.identity();

		// U
		rotate.values[0][0] = u[0];
		rotate.values[0][1] = u[1];
		rotate.values[0][2] = u[2];
		rotate.values[0][3] = 0;
		// V
		rotate.values[1][0] = v[0];
		rotate.values[1][1] = v[1];
		rotate.values[1][2] = v[2];
		rotate.values[1][3] = 0;
		// N
		rotate.values[2][0] = this.vpn[0];
		rotate.values[2][1] = this.vpn[1];
		rotate.values[2][2] = this.vpn[2];
		rotate.values[2][3] = 0;
		//System.out.println("Rotation:\n" + rotate);

		Matrix projection = Matrix.identity();
		projection.values[3][2] = -1/this.FL;
		projection.values[3][3] = 0;

		
		
		
		//System.out.println("Projection:\n" + projection);

		//System.out.println(Matrix.multiply(projection, rotate));
		cameraRotationMatrix = rotate;
		cameraCoordsMatrix = Matrix.multiply(rotate, trans);
		projectionMatrix = projection;
		cameraMatrix = Matrix.multiply(projectionMatrix, cameraCoordsMatrix);
		//System.out.println(cameraMatrix);
	}

	
	
	public void initializeLookAtCamera(double FL,
			double fpX, double fpY, double fpZ,
			double lookAtX, double lookAtY, double lookAtZ,
			double vupX, double vupY, double vupZ)
	{

		this.FL = (FL < 0 ) ? FL * -1 : FL;
		//this.FL = (FL > 0) ? FL : FL*-1;
		this.fp = new double[] {fpX, fpY, fpZ, 1};
		this.lookat = new double[] {lookAtX, lookAtY, lookAtZ, 1};
		this.vpn = new double[4];
		//for (int tt = 0; tt < fp.length; tt++)
		//	System.out.println("FP: " + fp[tt]);

		//for (int tt = 0; tt < vpn.length; tt++)
		//	System.out.println("VPN: " + vpn[tt]);

		//double lookatMag = Math.sqrt(Math.pow(lookat[0],2) + Math.pow(lookat[1],2) + Math.pow(lookat[2],2));

		//lookat[0] = lookat[0]/lookatMag;
		//lookat[1] = lookat[1]/lookatMag;
		//lookat[2] = lookat[2]/lookatMag;
		
		this.vpn[0] = -(lookat[0] - (fpX));
		this.vpn[1] = -(lookat[1] - (fpY));
		this.vpn[2] = -(lookat[2] - (fpZ));
		this.vpn[3] = 1;
		
		double vpnMag = Math.sqrt(Math.pow(this.vpn[0],2) + Math.pow(this.vpn[1],2) + Math.pow(this.vpn[2],2));

		this.vpn[0] = this.vpn[0]/vpnMag;
		this.vpn[1] = this.vpn[1]/vpnMag;
		this.vpn[2] = this.vpn[2]/vpnMag;

		

		//for (int tt = 0; tt < this.vpn.length; tt++)
		//	System.out.println("VPN HAT: " + this.vpn[tt]);

		this.vup = new double[] {vupX, vupY, vupZ, 1};

		//for (int tt = 0; tt < this.vup.length; tt++)
		//	System.out.println("VUP: " + this.vup[tt]);



		//for (int tt = 0; tt < vup.length; tt++)
		//		System.out.println("VUP HAT: " + vup[tt]);

		//if (Math.abs(Matrix.dotProduct(vup, vpn)) > 0.1)
		//{
		//	throw new IllegalArgumentException("Invalid VUP");
		//}

		double[] u = Matrix.crossProduct(this.vup, this.vpn);

		double uMag = Math.sqrt(Math.pow(u[0],2) + Math.pow(u[1],2) + Math.pow(u[2],2));

		u[0] = u[0]/uMag;
		u[1] = u[1]/uMag;
		u[2] = u[2]/uMag;

		this.camX = u;
		//for (int tt = 0; tt < u.length; tt++)
		//	System.out.println("u: " + u[tt]);

		double[] v = Matrix.crossProduct(this.vpn,u);

		
		this.camY = v;
		//for (int tt = 0; tt < u.length; tt++)
		//	System.out.println("v: " + v[tt]);

		Matrix trans = Matrix.translate(fpX*-1, fpY*-1, fpZ*-1);

		//System.out.println("Translation:\n" + trans);

		Matrix rotate = Matrix.identity();

		// U
		rotate.values[0][0] = u[0];
		rotate.values[0][1] = u[1];
		rotate.values[0][2] = u[2];
		rotate.values[0][3] = 0;
		// V
		rotate.values[1][0] = v[0];
		rotate.values[1][1] = v[1];
		rotate.values[1][2] = v[2];
		rotate.values[1][3] = 0;
		// N
		rotate.values[2][0] = this.vpn[0];
		rotate.values[2][1] = this.vpn[1];
		rotate.values[2][2] = this.vpn[2];
		rotate.values[2][3] = 0;
		//System.out.println("Rotation:\n" + rotate);

		Matrix projection = Matrix.identity();
		projection.values[3][2] = -1/this.FL;
		projection.values[3][3] = 0;

		
		
		
		//System.out.println("Projection:\n" + projection);

		//System.out.println(Matrix.multiply(projection, rotate));
		
		
		cameraRotationMatrix = rotate;
		cameraCoordsMatrix = Matrix.multiply(rotate, trans);
		projectionMatrix = projection;
		cameraMatrix = Matrix.multiply(projectionMatrix, cameraCoordsMatrix);
		//System.out.println(cameraMatrix);
	}

	
	
/*
	public void render(ModelGroup group, ImageWrapper iw)
	{
		//System.out.println("Image Size!: " + imgData.length);
		int polyLength = group.polys.length;
		int vertLength;

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
		boolean swapped = false;
		int dy = 0;
		int dx = 0;

		int flip = 0;
		int flip2 = 0;

		Polygon curPoly;
		for (int p = 0; p < polyLength; p++)
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

				if (x1 < minX)
					mask1 |= 0x1;
				else if (x1 >= maxX)
					mask1 |= 0x2;

				if (y1 <= minY)
					mask1 |= 0x4;
				else if (y1 > maxY)
					mask1 |= 0x8;


				if (x2 < minX)
					mask2 |= 0x1;
				else if (x2 >= maxX)
					mask2 |= 0x2;

				if (y2 <= minY)
					mask2 |= 0x4;
				else if (y2 > maxY)
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
							clippedx = x1 + (x2 - x1) * (maxY - y1) / (y2 - y1);
							clippedy = maxY;
						} else if ((tmask & 0x4) > 0) {
							clippedx = x1 + (x2 - x1) * (minY+1 - y1) / (y2 - y1);
							clippedy = minY+1;
						} else if ((tmask & 0x1) > 0) {
							clippedx = minX;
							clippedy = y1 + (y2 - y1) * (minX - x1) / (x2 - x1);
						} else if ((tmask & 0x2) > 0) {
							clippedx = maxX-1;
							clippedy = y1 + (y2 - y1) * (maxX-1 - x1) / (x2 - x1);
						}

						if (mask1 > 0) {
							mask1 =0;
							x1 = clippedx;
							y1 = clippedy;
							if (x1 < minX)
								mask1 |= 0x1;
							else if (x1 >= maxX)
								mask1 |= 0x2;

							if (y1 <= minY)
								mask1 |= 0x4;
							else if (y1 > maxY)
								mask1 |= 0x8;
						} else {
							mask2 = 0;
							x2 = clippedx;
							y2 = clippedy;
							if (x2 < minX)
								mask2 |= 0x1;
							else if (x2 >= maxX)
								mask2 |= 0x2;

							if (y2 <= minY)
								mask2 |= 0x4;
							else if (y2 > maxY)
								mask2 |= 0x8;
						}
					}
				}
				
				//System.out.println(reject);
				if (!reject) {
					
	
					
					x1 = x1-minX;
					x2 = x2-minX;
					y1 = -(y1+minY);
					y2 = -(y2+minY);
					
					
					
					//System.out.println("Drawing Clipped Line from (" + x1 + ", " + y1 + ") to (" + x2 + ", " + y2 + ")");
					if (x1 == x2 && y1 == y2)
					{
						iw.pixels[x1][y1] = 16777215;
						//imgData[imgHeight * x1+ y1] = 16777215;
						continue;
					}
					

					if (x1 < x2)
						tx = 1;
					else
						tx = -1;

					if (y1 < y2)
						ty = 1;
					else
						ty = -1;

					flip = dx-dy;

					while (x1 != x2 || y1 != y2)
					{
						//System.out.println (imgWidth/2 + " " + imgHeight/2);
						//if (imgWidth * (x1) + (y1) > imgData.length)
						//	System.out.println(x1 + " " + y1);
						//imgData[imgWidth * (y1) + (x1)] = 16777215;
						iw.pixels[x1][y1] = 16777215;
						//imgData[imgHeight * x1+ y1] = 16777215;
						flip2 = flip*2;
						if (flip2 > -dy)
						{
							flip -= dy;
							x1 += tx;
						}
						if (flip2 < dx)
						{
							flip += dx;
							y1 += ty;
						}
					}
					iw.pixels[x1][y1] = 16777215;
					//imgData[imgHeight * x1+ y1] = 16777215;
					
				}
			}
		}
	}

*/
	
	
	
	
	
	
	

	
}
