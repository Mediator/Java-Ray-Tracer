package engine.raytracer.renderers;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import engine.parallel.ThreadNotifier;
import engine.parallel.WorkNotifier;
import engine.parallel.WorkPool;
import engine.raytracer.GenericTransformationWorker;
import engine.raytracer.RaytraceWorkerStatic;
import engine.util.ImageRasterizer;
import engine.util.Matrix;
import engine.world.IRenderer;
import engine.world.ModelGroup;
import engine.world.World;

public class RaytracerRenderer implements ThreadNotifier, IRenderer {

	private Object locker = new Object();
	
	
	private WorkPool workPool;
	AtomicInteger workersCount;
	AtomicBoolean workComplete;
	public RaytracerRenderer()
	{
		System.out.println("Initializing Render");
		workersCount = new AtomicInteger();
		workComplete = new AtomicBoolean(true);
		
	}
	WorkNotifier notifier;
	
	public void render(World world, ImageRasterizer raster, WorkNotifier notifier, Object[] data)
	{
		//System.out.println("Rendering - Ray Traced");
		ModelGroup[] groups = (ModelGroup[])data;
		
		while (workComplete.get() == false)
		{
			//System.out.println(workersCount.get());
			try {
				//System.out.println("Waiting");
				synchronized (locker)
				{
					locker.wait();
				}
			} catch (InterruptedException e) {
				//System.out.println("Done Waiting");

			}
		}
		workComplete.set(false);
		//System.out.println("Starting Render");
		//if (this.notifier != null)
		//	throw new IllegalStateException("Work in progress");
		if (notifier == null)
			System.out.println("Notifier Null");
		this.notifier = notifier;
		workersCount.set(WorkPool.cores);
		if (workPool == null)
			workPool = new WorkPool(WorkPool.cores, this);
		

		int tasks = world.imgHeight-1;
		
		int tasksPerThread = (tasks+(WorkPool.cores-1))/WorkPool.cores;
		//int tasksPerThread = (group.polys.length+(WorkPool.cores-1))/WorkPool.cores;
		int tstop;
		
		
		
		
		for (int p = 0; p < WorkPool.cores; p++)
		{
			
			tstop = (p+1)*tasksPerThread;
			if (tstop > world.imgHeight-1)
				tstop = world.imgHeight-1;
			RaytraceWorkerStatic z = new RaytraceWorkerStatic(world, raster, groups,p*tasksPerThread,tstop);
			workPool.addWork(z);
		}
		//System.out.println("Started: " + WorkPool.cores + "  threads");

	}
	
	
	public void performMatrixOperations(World world, ModelGroup group, WorkNotifier notifier)
	{
		//System.out.println("Applying Operation: " + workersCount.get());
		while (workComplete.get() == false)
		{
			try {
				//System.out.println("Waiting to apply operation");
				synchronized (locker)
				{
					locker.wait();
				}
			} catch (InterruptedException e) {
				//System.out.println("Done Waiting to apply operation");

			}
		}
	//	System.out.println("Done Waiting to apply operation!");
		workComplete.set(false);
		//if (this.notifier != null)
		//	throw new IllegalStateException("Work in progress");
		
		/*
		double[] curSum;
		double curUsed;
		double[] fNormal;
		double e = 0;
		group.recomputeNormals();
		for (int vv = 0; vv < group.workVerts.length; vv++)
		{
			fNormal = null;
			curSum = new double[4];
			curUsed = 0;
			for (int pp = 0; pp < group.polys.length; pp++)
			{
				for (int pv = 0; pv < 3; pv++)
				{
					if (group.workVerts[vv] == group.polys[pp].myVerts[pv])
					{
						
						if (fNormal == null)
						{
							fNormal = new double[] {group.polys[pp].normal[0],group.polys[pp].normal[1],group.polys[pp].normal[2],group.polys[pp].normal[3]};
							curSum[0] = group.polys[pp].normal[0];
							curSum[1] = group.polys[pp].normal[1];
							curSum[2] = group.polys[pp].normal[2];
							curUsed++;
						}
						else
						{
							if (Matrix.dotProduct4(fNormal,group.polys[pp].normal) < 0)
							{
							//if (Math.abs(Matrix.dotProduct(fNormal,polys[pp].normal)) > e)
							//{
								//curSum[0] += polys[pp].normal[0];
								//curSum[1] += polys[pp].normal[1];
								//curSum[2] += polys[pp].normal[2];
								
								curSum[0] += group.polys[pp].normal[0]*-1;
								curSum[1] += group.polys[pp].normal[1]*-1;
								curSum[2] += group.polys[pp].normal[2]*-1;
								curUsed++;
							}
							else
							{
								//	System.out.println("Polynormal: " + pp + " " + Arrays.toString(polys[pp].normal));
								//curSum[0] += polys[pp].normal[0]*-1;
								//curSum[1] += polys[pp].normal[1]*-1;
								//curSum[2] += polys[pp].normal[2]*-1;
								///
								curSum[0] += group.polys[pp].normal[0];
								curSum[1] += group.polys[pp].normal[1];
								curSum[2] += group.polys[pp].normal[2];
								curUsed++;
							//curSum[3] += polys[pp].normal[3];
						
							}
						}
						
					}
				}
			}
			Polygon.composedVertNormals[group.workVerts[vv]] = new double[] {curSum[0]/curUsed,curSum[1]/curUsed, curSum[2]/curUsed, 1};
		   // System.out.println("Averages: " + Arrays.toString(Polygon.vertNormals[vv]));
			double vMag = (double) Math.sqrt(Math.pow(Polygon.composedVertNormals[group.workVerts[vv]][0], 2) + Math.pow(Polygon.composedVertNormals[group.workVerts[vv]][1],2) + Math.pow(Polygon.composedVertNormals[group.workVerts[vv]][2], 2));
			Polygon.composedVertNormals[group.workVerts[vv]][0] /= vMag;
			Polygon.composedVertNormals[group.workVerts[vv]][1] /= vMag;
			Polygon.composedVertNormals[group.workVerts[vv]][2] /= vMag;
			Polygon.composedVertNormals[group.workVerts[vv]][3] = 1;
		}
		
		
		*/
		
		
		this.notifier = notifier;
		Matrix workingMatrix;
		//System.out.println(workingMatrix);
		
		workingMatrix = group.composedMatrix;

		

		workersCount.set(WorkPool.cores);
		if (workPool == null)
			workPool = new WorkPool(WorkPool.cores, this);
		
		int tasksPerThread = (group.numWorkVerts+(WorkPool.cores-1))/WorkPool.cores;
		int tstop;
		
		//int pos = 0;
		for (int x = 0; x < WorkPool.cores; x++)
		{
			
			tstop = (x+1)*tasksPerThread;
			if (tstop > group.numWorkVerts)
				tstop = group.numWorkVerts;
			
			GenericTransformationWorker z = new GenericTransformationWorker(workingMatrix, group.nonPreservingMatrix,group.getUsedVectors(),x*tasksPerThread,tstop);
			workPool.addWork(z);

		}
	}
	
	
	@Override
	public void notified() {
		//System.out.println("Notified Me");
		int cnt = workersCount.decrementAndGet();
		
		if (cnt== 0)
		{
			//System.out.println("Work Complete: " + (cnt-1));
			workPool.shutdownPool();
			workPool = null;//new WorkPool(WorkPool.cores,this);
			
			Runnable rn = new Runnable() {
				public void run()
				{
					notifier.workComplete();
				}
			};
			Thread notificationThread = new Thread(rn);
			notificationThread.start();
			
			//workersCount.decrementAndGet();
			//System.out.println("Poolsize:" + workersCount.get());
			workComplete.set(true);
			synchronized (locker)
			{
				//System.out.println("Notifying");
				locker.notify();
				//System.out.println("Notified");
			}
			
			return;

		}
	}
	public void dispose()
	{
		//workPool.shutdownPool();
	}

	
}
