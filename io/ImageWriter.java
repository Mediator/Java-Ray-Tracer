package io;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.atomic.AtomicInteger;

import engine.parallel.WorkNotifier;
import engine.raytracer.optimization.Octree;
import engine.util.ImageWrapper;
import engine.world.ModelGroup;
import engine.world.Polygon;
import engine.world.World;

public class ImageWriter implements WorkNotifier {

	AtomicInteger matrixOperations = new AtomicInteger();
	boolean matrixOperationsComplete = false;
	AtomicInteger renderOperations = new AtomicInteger();
	boolean renderOperationsComplete = false;

	private World world = null;
	private ImageWrapper iw = new ImageWrapper();
	String file;
	
	long matrixOperationsStartTime;
	long renderOperationsStartTime;
	public void output(String file, World world)
	{
		try {
			this.world = world;
			handleModel(file);
		} 
		catch (IllegalArgumentException ex)
		{
			throw ex;
		}
		catch (Exception e) {
			System.out.println("Rasterization Error: " + e.getMessage());
			
			e.printStackTrace();
			System.exit(1);
		}
		return;
	}
	
	public void doMatrixOperations(ModelGroup curGroup)
	{		
		

		world.renderer.performMatrixOperations(world,curGroup,this);
	
	
	}
	
	public void doRenderOperations(ModelGroup curGroup)
	{
		

		
		world.renderer.render(world,iw,this, new Object[]{curGroup});
		

	}
	public void doRaytraceOperations(ModelGroup[] groups)
	{
		

	
		world.renderer.render(world,iw,this, groups);
		

	}
	public void writeImage()
	{
		try
		{
			FileWriter fstream = new FileWriter(file);

			BufferedWriter out = new BufferedWriter(fstream, iw.pixels.length*11+56);
			//if (workCam.imgHeight > workCam.imgWidth)
				out.write("P3 " + world.imgWidth + " " + world.imgHeight + " 256\n");
			//else
				//out.write("P3 " + workCam.imgHeight + " " + workCam.imgWidth + " 256\n");
				//System.out.println("Max Length" + imgData.length);
				int data = 0;

				for (int y = 0; y < world.imgHeight; y++)
				{
					for (int x = 0; x < world.imgWidth; x++)
					{


						//System.out.println("x: " + x + " y: " + y + " idx: " + (workCam.imgHeight * y + x));
						//data = imgData[workCam.imgHeight * x + y];
						data = iw.pixels[x][y];
						out.write(((data >> 16) & 0xFF) + " " + ((data >> 8) & 0xFF) + " " + (data  & 0xFF) + " \n");
					}
				}

				
			out.flush();
			out.close();
			System.out.println("Image Written");
		}
		catch (Exception ex)
		{
			System.out.println("Failed to output model");
			
			ex.printStackTrace();
		}
	}
	private void handleModel(String file) throws Exception
	{

		this.file = file;
		//System.out.println("Cameras: " + ModelGroup.cameras.length);
		iw.pixels = new int[world.imgHeight][world.imgWidth];
		

		matrixOperationsComplete = false;
		renderOperationsComplete = false;
		matrixOperations.set(world.modelGroups.length);
		
		
		System.out.println("Applying group operations");
		matrixOperationsStartTime = System.currentTimeMillis();
		World.prepOperations();
		for (ModelGroup curGroup : world.modelGroups)
		{
			//System.out.println("op");
			doMatrixOperations(curGroup);
		}

		
	}
	@Override
	public void workComplete() {

		if (!matrixOperationsComplete)
		{

			if (matrixOperations.decrementAndGet() == 0)
			{
				
				
				long operationEndTime = System.currentTimeMillis();
				long operationRunTime = operationEndTime - matrixOperationsStartTime;
				System.out.println("Time to perform matrix operations: " + operationRunTime + "ms on " + Polygon.verts.length + " vertices");
				
				System.out.println("Rendering Model - Group ");
				renderOperationsStartTime = System.currentTimeMillis();
				for (ModelGroup curGroup : world.modelGroups)
				{
						
					curGroup.recomputeNormals();
				}
				matrixOperationsComplete = true;
				if (!world.renderingRequiresAllGroups)
				{
					renderOperations.set(world.modelGroups.length);
					for (ModelGroup curGroup : world.modelGroups)
					{
							
							doRenderOperations(curGroup);
					}
				}
				else
				{
					renderOperations.set(1);
					world.octree = new Octree(world.modelGroups);
					doRaytraceOperations(world.modelGroups);
				}
				
			}
		}
		else if (!renderOperationsComplete)
		{
	
			
			if (renderOperations.decrementAndGet() == 0)
			{

				long operationEndTime = System.currentTimeMillis();
				long operationRunTime = operationEndTime - renderOperationsStartTime;
				System.out.println("Time to perform rendering: " + operationRunTime + "ms");
				//for (ModelGroup curGroup : savedGroups)
				//{
				//	curGroup.dispose();
				//}
				world.renderer.dispose();
				writeImage();
				renderOperationsComplete = true;
			}
		}
		
	}

}

