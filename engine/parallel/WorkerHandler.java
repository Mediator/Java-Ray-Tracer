package engine.parallel;
public interface WorkerHandler {
	public void handle(Object result);
	public void handle();
}
