package RayTracer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageWriter implements WorkNotifier {

	AtomicInteger matrixOperations = new AtomicInteger();
	boolean matrixOperationsComplete = false;
	AtomicInteger renderOperations = new AtomicInteger();
	boolean renderOperationsComplete = false;
	ArrayList<ModelGroup> savedGroups = new ArrayList<ModelGroup>();
	private Camera workCam = null;
	private ImageWrapper iw = new ImageWrapper();
	String file;
	
	long matrixOperationsStartTime;
	long renderOperationsStartTime;
	public void output(String file, HashMap<String,ModelGroup> groups)
	{
		try {
			handleModel(file, groups);
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
		

		curGroup.applyOpertation(workCam, this, true);
	
	
	}
	
	public void doRenderOperations(ModelGroup curGroup)
	{
		

		
		workCam.renderWireFrame(curGroup,iw,this);
		

	}
	public void doRaytraceOperations(ModelGroup[] groups)
	{
		

		
		workCam.renderRaytraced(groups,iw,this);
		

	}
	public void writeImage()
	{
		try
		{
			FileWriter fstream = new FileWriter(file);

			BufferedWriter out = new BufferedWriter(fstream, iw.pixels.length*11+56);
			//if (workCam.imgHeight > workCam.imgWidth)
				out.write("P3 " + workCam.imgWidth + " " + workCam.imgHeight + " 256\n");
			//else
				//out.write("P3 " + workCam.imgHeight + " " + workCam.imgWidth + " 256\n");
				//System.out.println("Max Length" + imgData.length);
				int data = 0;

				for (int y = 0; y < workCam.imgHeight; y++)
				{
					for (int x = 0; x < workCam.imgWidth; x++)
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
	private void handleModel(String file, HashMap<String,ModelGroup> groups) throws Exception
	{

		this.file = file;
		//System.out.println("Cameras: " + ModelGroup.cameras.length);
		for (int cc = 0; cc < ModelGroup.cameras.length; cc++)
		{
			if (ModelGroup.cameras[cc].graphics != null)
			{
				workCam = ModelGroup.cameras[cc];
			}
		}
		if (workCam == null || !workCam.isCameraInitialized())
		{
			throw new IllegalArgumentException("No camera and or wireframe supplied");
		}
		if (!workCam.isWireframeInitialized())
		{
			throw new IllegalArgumentException("Found no valid wireframe for specified camera");
		}
		iw.pixels = new int[workCam.imgHeight][workCam.imgWidth];
		workCam.setWorkPool(new WorkPool(WorkPool.cores, workCam));
		for (ModelGroup curGroup : groups.values())
		{
			if (curGroup.polys.length == 0)
				continue;
			
			if (savedGroups.contains(curGroup))
				continue;
			
			savedGroups.add(curGroup);
		}
		matrixOperationsComplete = false;
		renderOperationsComplete = false;
		matrixOperations.set(savedGroups.size());
		
		
		System.out.println("Applying group operations");
		matrixOperationsStartTime = System.currentTimeMillis();
		ModelGroup.prepOperations();
		for (ModelGroup curGroup : savedGroups)
		{

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
				ModelGroup[] tgroups = new ModelGroup[savedGroups.size()];
				matrixOperationsComplete = true;
				int i=0;
				if (!workCam.raytrace)
				{
					renderOperations.set(savedGroups.size());

				}
				else
				{
					renderOperations.set(1);
				}
				for (ModelGroup curGroup : savedGroups)
				{
					
					if (!workCam.raytrace)
					{
						doRenderOperations(curGroup);
					}
					else
					{
						tgroups[i++] = curGroup;
					}
				}
				if (workCam.raytrace)
				{
					doRaytraceOperations(tgroups);
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
				for (ModelGroup curGroup : savedGroups)
				{
					curGroup.dispose();
				}
				workCam.dispose();
				writeImage();
				renderOperationsComplete = true;
			}
		}
		
	}

}

