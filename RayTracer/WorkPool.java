package RayTracer;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class WorkPool {
	public final static int cores = Runtime.getRuntime().availableProcessors();
	private boolean shutdown;
	final ArrayBlockingQueue<Runnable> queue;
	public ThreadNotifier notify;
	Thread[] threadPool;
	public WorkPool(int poolSize, ThreadNotifier notify)
	{
		//System.out.println("Creating a work pool of " + poolSize);
		this.notify = notify;
		queue = new ArrayBlockingQueue<Runnable>(poolSize,true);
		threadPool = new Thread[cores];
		for (int x = 0; x < poolSize;x++)
		{
			threadPool[x] = new Thread(new workExecutor(queue));
			threadPool[x].start();
		}

	}
	public void addWork(Runnable worker)
	{
		queue.add(worker);
	}
	
	public void shutdownPool()
	{
		this.shutdown = true;
		for (int x = 0; x < cores;x++)
		{
			//System.out.println("INTERRUPTING");
			threadPool[x].interrupt();
		}
	}
	public class workExecutor implements Runnable
	{
		ArrayBlockingQueue<Runnable> queue;
		public workExecutor(ArrayBlockingQueue<Runnable> queue)
		{
			this.queue = queue;
		}
		@Override
		public void run() {
			
			while (!shutdown)
			{
				try {
					Runnable r = queue.take();
					r.run();
					notify.notified();
				} catch (InterruptedException e) {
					return;
				}
			}
			
		}
		
	}
	
}
