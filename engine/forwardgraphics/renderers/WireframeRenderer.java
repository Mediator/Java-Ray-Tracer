package engine.forwardgraphics.renderers;

import java.util.concurrent.atomic.AtomicInteger;

import engine.forwardgraphics.ProjectionTransformationWorker;
import engine.forwardgraphics.WireFrameWorkerStatic;
import engine.parallel.ThreadNotifier;
import engine.parallel.WorkNotifier;
import engine.parallel.WorkPool;
import engine.util.ImageRasterizer;
import engine.util.Matrix;
import engine.world.IRenderer;
import engine.world.ModelGroup;
import engine.world.World;

public class WireframeRenderer implements ThreadNotifier, IRenderer {

	private Object locker = new Object();
	
	
	private WorkPool workPool;
	AtomicInteger workersCount = new AtomicInteger();
	WorkNotifier notifier;
	
	

	
	public void render(World world, ImageRasterizer raster, WorkNotifier notifier, Object[] data)
	{
		
		ModelGroup group = (ModelGroup)data[0];
		//int polyLength = group.polys.length;
		while (workersCount.get() > 0)
		{
			try {
				System.out.println("Waiting");
				synchronized (locker)
				{
					locker.wait();
				}
			} catch (InterruptedException e) {
				System.out.println("Done Waiting");

			}
		}
		if (this.notifier != null)
			throw new IllegalStateException("Work in progress");
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
			WireFrameWorkerStatic z = new WireFrameWorkerStatic(world,raster, group,p*tasksPerThread,tstop);
			workPool.addWork(z);
		}

	}
	
	public void performMatrixOperations(World world, ModelGroup group, WorkNotifier notifier)
	{
		
		
		while (workersCount.get() > 0)
		{
			try {
				System.out.println("Waiting to apply operation");
				synchronized (locker)
				{
					locker.wait();
				}
			} catch (InterruptedException e) {
				System.out.println("Done Waiting to apply operation");

			}
		}
		if (this.notifier != null)
			throw new IllegalStateException("Work in progress");
		System.out.println("Applying Operation");
		
		this.notifier = notifier;
		Matrix workingMatrix;
		//System.out.println(workingMatrix);
		
		workingMatrix = Matrix.multiply(world.primaryCamera.cameraCoordsMatrix, group.composedMatrix);

		

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
			
			ProjectionTransformationWorker z = new ProjectionTransformationWorker(world.primaryCamera, workingMatrix,world.primaryCamera.projectionMatrix, group.nonPreservingMatrix,group.getUsedVectors(),x*tasksPerThread,tstop);
			workPool.addWork(z);

		}
	}
	
	@Override
	public void notified() {
		int cnt = workersCount.get();
		
		if (cnt-1 == 0)
		{
			System.out.println("Work Complete: " + (cnt-1));
			workPool.shutdownPool();
			workPool = new WorkPool(WorkPool.cores,this);
			
			Runnable rn = new Runnable() {
				public void run()
				{
					notifier.workComplete();
				}
			};
			Thread notificationThread = new Thread(rn);
			notificationThread.start();
			
			workersCount.decrementAndGet();
			System.out.println("Poolsize:" + workersCount.get());
			synchronized (locker)
			{
				locker.notifyAll();
			}
			return;

		}
		workersCount.decrementAndGet();
	}
	public void dispose()
	{
		workPool.shutdownPool();
	}

	
	
}
