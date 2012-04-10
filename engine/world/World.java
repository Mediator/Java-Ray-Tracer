package engine.world;

import java.util.ArrayList;
import java.util.HashMap;

import engine.raytracer.optimization.Octree;

public class World {
	public int recursiveDepth = 5;
	public double superSamplingStart = -0.5;
	public double superSamplingEnd = 0.5;
	public double superSamplingIncrement = 0.5;
	public boolean superSample = false;
	public ModelGroup[] modelGroups;
	public Camera[] cameras;
	public Camera primaryCamera;
	public static HashMap<String, ModelMaterial> materials = new HashMap<String,ModelMaterial>();
	public static ArrayList<LightSource> lights = new ArrayList<LightSource>();
	public IRenderer renderer;
	public Octree octree;
	public boolean renderingRequiresAllGroups = false;
	
	public int minX;
	public int minY;
	public int maxX;
	public int maxY;
	public int imgWidth;
	public int imgHeight;

	public void initializeGraphics(String camera, double minX, double minY, double maxX, double maxY)
	{
		if (camera.toString().trim().equals(""))
			throw new IllegalArgumentException("No camera and or wireframe supplied");
		this.maxX = (int) maxX;
		this.maxY = (int) maxY;
		this.minX = (int) minX;
		this.minY = (int) minY;
		imgWidth = (this.maxX - this.minX)+1;
		imgHeight = (this.maxY - this.minY)+1;
		for (int x = 0; x < cameras.length;x++)
		{
			if (cameras[x].name.equals(camera))
			{
				this.primaryCamera = cameras[x];
				return;
			}
		}
		if (this.primaryCamera == null)
		{
			throw new IllegalArgumentException("No camera and or wireframe supplied");
		}
		
	}
	
	
	
	public static void prepOperations()
	{
		Polygon.composedVertNormals = new double[Polygon.verts.length][4];
		Polygon.projectedVerts = new double[Polygon.verts.length][4];
		Polygon.transformedVerts = new double[Polygon.verts.length][4];
	}
}
