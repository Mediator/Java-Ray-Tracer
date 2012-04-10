package engine.world;

import engine.parallel.WorkNotifier;
import engine.util.ImageRasterizer;

public interface IRenderer {
	public void render(World world, ImageRasterizer raster, WorkNotifier workNotifier, Object[] data);
	public void performMatrixOperations(World world, ModelGroup group, WorkNotifier workNotifier);
	public void dispose();
}
