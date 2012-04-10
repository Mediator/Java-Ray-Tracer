package RayTracer;
import java.awt.image.WritableRaster;
import java.util.concurrent.atomic.AtomicInteger;


public class Camera implements ThreadNotifier {

	public String name;
	protected double FL;
	protected double[] fp;
	protected double[] vpn;
	protected double[] lookat;
	protected double[] vup;
	
	protected double[] camX;
	protected double[] camY;
	
	public double[][] coloredVerts;
	
	public boolean raytrace = false;
	public boolean render = false;
	protected Object merrus = new Object();
	private WorkPool workPool;
	public Graphics graphics = null;
	public Matrix cameraMatrix = null;
	
	public Matrix cameraCoordsMatrix = null;
	public Matrix projectionMatrix = null;
	public Matrix cameraRotationMatrix = null;
	
	//int workersCount; //= new AtomicInteger();
	AtomicInteger workersCount = new AtomicInteger();
	WorkNotifier notifier;
	public Camera(String name)
	{
		this.name = name;
	}
	public void setWorkPool(WorkPool workPool)
	{
		this.workPool = workPool;
	}
	public boolean isWireframeInitialized()
	{
		
		return graphics != null;
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

	
	
	public int minX;
	public int minY;
	public int maxX;
	public int maxY;
	public int imgWidth;
	public int imgHeight;

	public void initializeGraphics(double minX, double minY, double maxX, double maxY)
	{
		this.maxX = (int) maxX;
		this.maxY = (int) maxY;
		this.minX = (int) minX;
		this.minY = (int) minY;
		imgWidth = (this.maxX - this.minX)+1;
		imgHeight = (this.maxY - this.minY)+1;
		coloredVerts = new double[imgWidth][imgHeight];
		
		//System.out.println("Width: " + imgWidth + " Height: " + imgHeight);


		this.graphics = new Graphics(minX, minY, maxX, maxY);
	}
	
	public void initializeGraphics(double minX, double minY, double maxX, double maxY, boolean render)
	{
		this.maxX = (int) maxX;
		this.maxY = (int) maxY;
		this.minX = (int) minX;
		this.minY = (int) minY;
		imgWidth = (this.maxX - this.minX);
		imgHeight = (this.maxY - this.minY);
		
		//System.out.println("Width: " + imgWidth + " Height: " + imgHeight);

		this.render = true;
		this.graphics = new Graphics(minX, minY, maxX, maxY);
	}
	
	public void initializeRaytraceGraphics(double minX, double minY, double maxX, double maxY)
	{
		this.maxX = (int) maxX;
		this.maxY = (int) maxY;
		this.minX = (int) minX;
		this.minY = (int) minY;
		imgWidth = (this.maxX - this.minX);
		imgHeight = (this.maxY - this.minY);
		
		//System.out.println("Width: " + imgWidth + " Height: " + imgHeight);

		this.raytrace = true;
		this.graphics = new Graphics(minX, minY, maxX, maxY);
	}

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


				vert1 = Polygon.composedVerts[curPoly.myVerts[v]];
				vert2 = Polygon.composedVerts[curPoly.myVerts[((v +1) % vertLength)]];

				
			


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
					
					//tx = y1;
					//ty = y2;
					
					//y1 = x1;
					//y2 = x2;
					//x1 = tx;
					//x2 = ty;
					
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
					
					/*
					// if x1 == x2 or y1 == y2, then it does not matter what we set here
				    int delta_x = x2 - x1;
				    delta_x = Math.abs(delta_x) << 1;
				 
				    int delta_y = y2 - y1;
					delta_y = Math.abs(delta_y) << 1;
				 
					if (x1 < x2)
						tx = 1;
					else
						tx = -1;

					if (y1 < y2)
						ty = 1;
					else
						ty = -1;
				    
				    
					imgData[imgWidth * (x1) + (y1)] = 16777215;
				 
				    if (delta_x >= delta_y)
				    {
				        // error may go below zero
				        int error = delta_y - (delta_x >> 1);
				 
				        while (x1 != x2)
				        {
				            if (error >= 0)
				            {
				                if (error  > 0 || (tx > 0))
				                {
				                    y1 += ty;
				                    error -= delta_x;
				                }
				                // else do nothing
				            }
				            // else do nothing
				 
				            x1 += tx;
				            error += delta_y;
				 
				            imgData[imgWidth * (x1) + (y1)] = 16777215;
				        }
				    }
				    else
				    {
				        // error may go below zero
				        int error = delta_x - (delta_y >> 1);
				 
				        while (y1 != y2)
				        {
				            if (error >= 0)
				            {
				                if (error > 0 || (ty > 0))
				                {
				                    x1 += tx;
				                    error -= delta_y;
				                }
				                // else do nothing
				            }
				            // else do nothing
				 
				            y1 += ty;
				            error += delta_x;
				 
				            imgData[imgWidth * (x1) + (y1)] = 16777215;
				        }
				    }/*
					
					/*
				     boolean steep = Math.abs(y2 - y1) > Math.abs(x2 - x1);
				    	     if (steep)
				    	     {
				    	    	 
				    	    	 tx = y1;
				    	    	 ty = y2;
				    	    	 y1 = x1;
				    	    	 y2 = x2;
				    	    	 x1 = tx;
				    	    	 x2 = ty;
				    	     }
				    	     if (x1 > x2)
				    	     {
				    	    	 tx = x1;
				    	    	 ty = y1;
				    	    	 x1 = x2;
				    	    	 y1 = y2;
				    	    	 x2 = tx;
				    	    	 y2 = ty;
				    	     }
				    	     int deltax = x2 - x1;
				    	     int deltay = Math.abs(y2 - y1);
				    	     int error = deltax / 2;
				    	     int ystep;
				    	     int y = y1;
				    	     if (y1 < y2)
				    	    	 ystep = 1;
				    	     else 
				    	    	 ystep = -1;
				    	     for (int x = x1; x<= x2;x++)
				    	     {
				    	         if (steep)
				    	         {
				    	        	 imgData[imgWidth * (y) + (x)] = 16777215;
				    	         }else
				    	        	 {
				    	        	 imgData[imgWidth * (x) + (y)] = 16777215;
				    	         }
				    	         error = error - deltay;
				    	         if (error < 0)
				    	         {
				    	             y = y + ystep;
				    	             error = error + deltax;
				    	         }
				    	     }
					*/
					
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


	
	public void renderShaded(ModelGroup group, ImageWrapper iw, WorkNotifier notifier)
	{
		System.out.println(group.toString());
		double[][] zData = new double[iw.pixels.length][iw.pixels[0].length];
		for (int x = 0; x < iw.pixels.length; x++)
		{
			for (int y = 0; y < iw.pixels[1].length; y++)
			{
				zData[x][y] = Integer.MIN_VALUE;
			}
		}

		System.out.println(workersCount.get());
		while (workersCount.get() > 0)
		{
			try {
				System.out.println("Waiting");
				synchronized (merrus)
				{
					merrus.wait();
				}
			} catch (InterruptedException e) {
				System.out.println("Done Waiting");

			}
		}

		

		System.out.println(workersCount.get());
		this.notifier = notifier;
		workersCount.set(WorkPool.cores);
		System.out.println(workersCount.get());
		//workersCount =WorkPool.cores;
		if (workPool == null)
			workPool = new WorkPool(WorkPool.cores, this);
		int tasksPerThread = (group.polys.length+(WorkPool.cores-1))/WorkPool.cores;
		int tstop;
		
		
		for (int p = 0; p < WorkPool.cores; p++)
		{
			
			tstop = (p+1)*tasksPerThread;
			if (tstop > group.polys.length)
				tstop = group.polys.length;
			RenderWorkerStaticBresenhams z = new RenderWorkerStaticBresenhams(this,iw,zData, group,p*tasksPerThread,tstop);
			workPool.addWork(z);
		}
	}
	
	public void renderShaded(ModelGroup group, WritableRaster raster, WorkNotifier notifier)
	{
		double[][] zData = new double[imgWidth][imgHeight];
		for (int x = 0; x < imgWidth; x++)
		{
			for (int y = 0; y < imgHeight; y++)
			{
				zData[x][y] = Integer.MIN_VALUE;
			}
		}
		int polyLength = group.polys.length;
		while (workersCount.get() > 0)
		{
			try {
				System.out.println("Waiting");
				synchronized (merrus)
				{
					merrus.wait();
				}
			} catch (InterruptedException e) {
				System.out.println("Done Waiting");

			}
		}
		this.notifier = notifier;
		workersCount.set(WorkPool.cores);
		if (workPool == null)
			workPool = new WorkPool(WorkPool.cores, this);
		int tasksPerThread = (group.polys.length+(WorkPool.cores-1))/WorkPool.cores;
		int tstop;
		
		
		
		
		for (int p = 0; p < WorkPool.cores; p++)
		{
			
			tstop = (p+1)*tasksPerThread;
			if (tstop > group.polys.length)
				tstop = group.polys.length;
			RenderWorkerDynamic z = new RenderWorkerDynamic(this,raster, zData, group,p*tasksPerThread,tstop);
			workPool.addWork(z);
		}

	}
	
	
	public void renderRaytraced(ModelGroup[] groups, ImageWrapper iw, WorkNotifier notifier)
	{
		double[][] zData = new double[imgWidth][imgHeight];
		for (int x = 0; x < imgWidth; x++)
		{
			for (int y = 0; y < imgHeight; y++)
			{
				zData[x][y] = Integer.MIN_VALUE;
			}
		}
		
		while (workersCount.get() > 0)
		{
			try {
				System.out.println("Waiting");
				synchronized (merrus)
				{
					merrus.wait();
				}
			} catch (InterruptedException e) {
				System.out.println("Done Waiting");

			}
		}
		this.notifier = notifier;
		workersCount.set(WorkPool.cores);
		if (workPool == null)
			workPool = new WorkPool(WorkPool.cores, this);
		
		System.out.println(this.imgHeight);
		int tasks = this.imgHeight-1;
		
		int tasksPerThread = (tasks+(WorkPool.cores-1))/WorkPool.cores;
		//int tasksPerThread = (group.polys.length+(WorkPool.cores-1))/WorkPool.cores;
		int tstop;
		
		
		
		
		for (int p = 0; p < WorkPool.cores; p++)
		{
			
			tstop = (p+1)*tasksPerThread;
			if (tstop > this.imgHeight-1)
				tstop = this.imgHeight-1;
			RaytraceWorkerStatic z = new RaytraceWorkerStatic(this,iw, zData, groups,p*tasksPerThread,tstop);
			workPool.addWork(z);
		}

	}
	
	
	public void renderWireFrame(ModelGroup group, ImageWrapper iw, WorkNotifier notifier)
	{
		if (render == true)
		{
			System.out.println("RenderingShaded");
			renderShaded(group,iw,notifier);
			return;
		}
	

		while (workersCount.get() > 0)
		{
			try {
				System.out.println("Waiting");
				synchronized (merrus)
				{
					merrus.wait();
				}
			} catch (InterruptedException e) {
				System.out.println("Done Waiting");

			}
		}
		this.notifier = notifier;
		workersCount.set(WorkPool.cores);
		//workersCount =WorkPool.cores;
		if (workPool == null)
			workPool = new WorkPool(WorkPool.cores, this);
		int tasksPerThread = (group.polys.length+(WorkPool.cores-1))/WorkPool.cores;
		int tstop;
		
		
		for (int p = 0; p < WorkPool.cores; p++)
		{
			
			tstop = (p+1)*tasksPerThread;
			if (tstop > group.polys.length)
				tstop = group.polys.length;
			WireFrameWorkerStatic z = new WireFrameWorkerStatic(this,iw, group,p*tasksPerThread,tstop);
			workPool.addWork(z);
		}
	}
	
	
	public void renderWireFrame(ModelGroup group, WritableRaster raster, WorkNotifier notifier)
	{
		
		if (render == true)
		{
			//System.out.println("RenderingShaded");
			renderShaded(group,raster,notifier);
			return;
		}
		int polyLength = group.polys.length;
		while (workersCount.get() > 0)
		{
			try {
				System.out.println("Waiting");
				synchronized (merrus)
				{
					merrus.wait();
				}
			} catch (InterruptedException e) {
				System.out.println("Done Waiting");

			}
		}
		this.notifier = notifier;
		workersCount.set(WorkPool.cores);
		if (workPool == null)
			workPool = new WorkPool(WorkPool.cores, this);
		int tasksPerThread = (group.polys.length+(WorkPool.cores-1))/WorkPool.cores;
		int tstop;
		
		
		
		
		for (int p = 0; p < WorkPool.cores; p++)
		{
			
			tstop = (p+1)*tasksPerThread;
			if (tstop > group.polys.length)
				tstop = group.polys.length;
			WireFrameWorkerDynamic z = new WireFrameWorkerDynamic(this,raster, group,p*tasksPerThread,tstop);
			workPool.addWork(z);
		}

	}
	

	public void render(ModelGroup group, WritableRaster raster)
	{
		
		int[] white = new int[] {255,255,255};
		
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


				vert1 = Polygon.composedVerts[curPoly.myVerts[v]];
				vert2 = Polygon.composedVerts[curPoly.myVerts[((v +1) % vertLength)]];

				
			


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
					
					//tx = y1;
					//ty = y2;
					
					//y1 = x1;
					//y2 = x2;
					//x1 = tx;
					//x2 = ty;
					
					x1 = x1-minX;
					x2 = x2-minX;
					y1 = -(y1+minY);
					y2 = -(y2+minY);
					
					
					
					//System.out.println("Clipped Line from (" + x1 + ", " + y1 + ") to (" + x2 + ", " + y2 + ")");
					if (x1 == x2 && y1 == y2)
					{
						raster.setPixel((y1),(x1),white);
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

					while (x1 != x2 || y1 != y2)
					{
						//System.out.println (imgWidth/2 + " " + imgHeight/2);
						//if (imgWidth * (x1) + (y1) > imgData.length)
						//	System.out.println(x1 + " " + y1);
						//imgData[imgWidth * (y1) + (x1)] = 16777215;
						raster.setPixel((y1),(x1),white);
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
					raster.setPixel((y1),(x1),white);
					
				}
			}
			//System.out.println("poly");
		}
		//System.out.println("returning");
	}

	@Override
	public void notified() {
		int cnt = workersCount.decrementAndGet();
		if (cnt == 0)
		{
			workPool.shutdownPool();
			workPool = new WorkPool(WorkPool.cores,this);
			

			synchronized (merrus)
			{
				merrus.notifyAll();
			}
			notifier.workComplete();
		}
	}
	public void dispose()
	{
		workPool.shutdownPool();
	}
}
