package engine.forwardgraphics.renderers;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import engine.forwardgraphics.ProjectionTransformationWorker;
import engine.forwardgraphics.RenderWorkerStaticBresenhams;
import engine.parallel.ThreadNotifier;
import engine.parallel.WorkNotifier;
import engine.parallel.WorkPool;
import engine.util.ImageRasterizer;
import engine.util.Matrix;
import engine.world.IRenderer;
import engine.world.ModelGroup;
import engine.world.World;

public class GouraudShadingRenderer implements ThreadNotifier, IRenderer {

	private Object locker = new Object();
	
	private AtomicBoolean workComplete;
	private WorkPool workPool;
	private AtomicInteger workersCount;
	WorkNotifier notifier;
	
	public GouraudShadingRenderer()
	{
		System.out.println("Initialized");
		workComplete = new AtomicBoolean(true);
		workersCount = new AtomicInteger();
		workersCount.set(0);
	}
	public void render(World world, ImageRasterizer raster, WorkNotifier notifier, Object[] data)
	{
	//	System.out.println(workersCount.get());
		while (workComplete.get() == false)
		{
		//	System.out.println("Attempting Lock");
			try {
				
				synchronized (locker)
				{
			//		System.out.println("Waiting");
					locker.wait();

				}
			} catch (InterruptedException e) {
				//System.out.println("Done Waiting");

			}
		}
		workComplete.set(false);
		workersCount.set(WorkPool.cores);
		ModelGroup group = (ModelGroup)data[0];
		System.out.println(group.toString());
		double[][] zData = new double[world.imgWidth][world.imgHeight];
		for (int x = 0; x < world.imgWidth; x++)
		{
			for (int y = 0; y < world.imgHeight; y++)
			{
				zData[x][y] = Integer.MIN_VALUE;
			}
		}

		

		

		//System.out.println(workersCount.get());
		this.notifier = notifier;

		//System.out.println(workersCount.get());
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
			RenderWorkerStaticBresenhams z = new RenderWorkerStaticBresenhams(world,raster,zData, group,p*tasksPerThread,tstop);
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


	@Override
	public void performMatrixOperations(World world, ModelGroup group, WorkNotifier notifier)
	{
		//System.out.println("Applying Operation: " + workersCount.get());
		while (workComplete.get() == false)
		{
			try {
			//	System.out.println("Attempting Lock");
				synchronized (locker)
				{
				//	System.out.println("z");
					//System.out.println("Waiting to apply operation " + workersCount.get());
					locker.wait();
					//System.out.println("d");
				}
			} catch (InterruptedException e) {
				//System.out.println("Done Waiting to apply operation");

			}
		}
		workComplete.set(false);
		//System.out.println("Calculating: " + workersCount.get());
		

		workersCount.set(WorkPool.cores);
		for (int x = 0; x < World.lights.size();x++)
		{
			World.lights.get(x).composedLocation = Matrix.multiply(world.primaryCamera.cameraCoordsMatrix, World.lights.get(x).location);                            
			// lights.get(x).composedLocation = Matrix.multiply(lights.get(x).composedLocation, -1);
		}

		
	
		
		
		
		this.notifier = notifier;
		Matrix workingMatrix;
		//System.out.println(workingMatrix);
		
		workingMatrix = Matrix.multiply(world.primaryCamera.cameraCoordsMatrix, group.composedMatrix);


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
			
			ProjectionTransformationWorker z = new ProjectionTransformationWorker(world.primaryCamera, workingMatrix,world.primaryCamera.projectionMatrix, group.nonPreservingMatrix,group.getUsedVectors(),x*tasksPerThread,tstop);
			workPool.addWork(z);

		}
	}
	
}
