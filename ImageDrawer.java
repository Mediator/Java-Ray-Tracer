
import io.CommandParser;
import io.DrawingPane;
import io.ImageRasterizerWR;
import io.ModelParser;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;

import animation.ModelAnimation;
import animation.RotationAnimation;
import animation.ScaleAnimation;
import engine.parallel.WorkNotifier;
import engine.util.Matrix;
import engine.world.Camera;
import engine.world.ModelGroup;
import engine.world.Polygon;
import engine.world.World;



public class ImageDrawer implements WorkNotifier {


	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {




					if (args.length < 3)
					{
						System.out.println("Missing Arguements");
						System.out.println("Format: HW2 [objfile] [commandfile] [outputobj]");
						System.exit(1);
					}
					String objFile = args[0];
					String cmdFile = args[1];
					String outObjFile = args[2];
					ModelParser parser = new ModelParser();
					long modelParserStartTime = System.currentTimeMillis();
					HashMap<String, ModelGroup> groups = parser.parse(objFile);
					long modelParserEndTime = System.currentTimeMillis();
					long modelParserRunTime = modelParserEndTime - modelParserStartTime;
					System.out.println("Time to parse Model File: " +  modelParserRunTime + "ms");
					CommandParser cparser = new CommandParser();
					long commandParserStartTime = System.currentTimeMillis();
					
					ArrayList<ModelGroup> savedGroups = new ArrayList<ModelGroup>();
					for (ModelGroup curGroup : groups.values())
					{
						if (curGroup.polys.length == 0)
							continue;
						
						if (savedGroups.contains(curGroup))
							continue;
						
						savedGroups.add(curGroup);
					}
					
					World world = new World();
					world.modelGroups =savedGroups.toArray(new ModelGroup[]{});
					
					
					cparser.parse(cmdFile, world, groups);
					long commandParserEndTime = System.currentTimeMillis();
					long commandParserRunTime = commandParserEndTime - commandParserStartTime;
					System.out.println("Time to parse command file and build matrix: " + commandParserRunTime + "ms");
					System.out.println("Groups: " + groups.values().size());

					String s;
					Iterator<String> i = groups.keySet().iterator();
					int[] usedVerts = new int[Polygon.verts.length];
					int curVert = -1;
					boolean firstGroup = true;
					ArrayList<ModelGroup> coveredGroups = new ArrayList<ModelGroup>();
					while (i.hasNext())
					{
						s = i.next();

						ModelGroup tmp = groups.get(s);


						if (!tmp.validate())
						{
							System.out.println("Model format specifies invalid polygon");
							System.exit(1);
						}
						
					}



					ImageDrawer window = new ImageDrawer();
					window.world = world;
					boolean exists = false;
window.savedGroups = savedGroups;

					System.out.println(world.imgWidth);
					System.out.println(world.imgHeight);


					window.start();
	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	/**
	 * Create the application.
	 */
	public ImageDrawer() {
		initialize();
	}
	public double inc = 300;
	public double inc2 = 100;
	public World world = null;
	ArrayList<ModelGroup> savedGroups = new ArrayList<ModelGroup>();
	public HashMap<String, ModelGroup> groups;
	boolean increase = true;
	boolean increase2 = true;

	DrawingPane panel;
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		//frame = new JFrame();
		//frame.setBounds(100, 100, 1024, 1024);
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new DrawingPane(1024,1024);
		panel.setBounds(100, 100, 1024, 1024);
		panel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel.setVisible(true);
		//frame.getContentPane().add(panel, BorderLayout.CENTER);
		//ScaleAnimation animation = new ScaleAnimation(200,700, true);
		ScaleAnimation sanimation = new ScaleAnimation(500, 800,true);
		RotationAnimation animation = new RotationAnimation(0,6.3,0,1,0, false);
		animations.add(sanimation);
		animations.add(animation);
		panel.initializeGraphics();

	}
	public void start()
	{
		this.run();
	}
	public ArrayList<ModelAnimation> animations = new ArrayList<ModelAnimation>();
	AtomicInteger matrixOperations = new AtomicInteger();
	boolean matrixOperationsComplete = false;
	AtomicInteger renderOperations = new AtomicInteger();
	boolean renderOperationsComplete = false;
	public void run()
	{
		//System.out.println("rendering");
		long time = System.currentTimeMillis();
		matrixOperations.set( savedGroups.size());
		renderOperations.set( savedGroups.size());
		matrixOperationsComplete = false;
		renderOperationsComplete = false;
		World.prepOperations();
		for (int x = 0; x < savedGroups.size(); x++)
		{
			ModelGroup curGroup = savedGroups.get(x);
			
			//Matrix product = curGroup.composedMatrix;
			Matrix product = Matrix.identity();

			for (ModelAnimation animation : animations)
			{
				product = Matrix.multiply(product, animation.getFrame(time));
			}
			//product = Matrix.multiply(product,Matrix.translate(0, 10, 0));

			curGroup.composedMatrix = product;
			//long operationStartTime = System.currentTimeMillis();
			world.renderer.performMatrixOperations(world, curGroup, this);
			//curGroup.applyOpertation(workCam, this, true);
			//long operationEndTime = System.currentTimeMillis();
			//logng operationRunTime = operationEndTime - operationStartTime;
			//System.out.println("Time to pejavarform matrix operations: " + operationRunTime + "ms on " + Polygon.verts.length + " vertices");
		}

	}
	BufferedImage rasterizer;
	@Override
	public void workComplete() {
		if (!matrixOperationsComplete)
		{
		//	System.out.println(matrixOperations.get());
			if (matrixOperations.decrementAndGet() == 0)
			{
				matrixOperationsComplete = true;
				rasterizer = panel.getRasterizer();
				
				for (int x = 0; x < savedGroups.size(); x++)
				{
					ModelGroup curGroup = savedGroups.get(x);
					if (curGroup.polys.length == 0)
						continue;
					
					ImageRasterizerWR wr = new ImageRasterizerWR(rasterizer.getRaster());
					world.renderer.render(world, wr, this, new Object[]{curGroup});
					
				}
			}
		}
		else if (!renderOperationsComplete)
		{
			//System.out.println(renderOperations.get());
			if (renderOperations.decrementAndGet() == 0)
			{
				panel.render(rasterizer);
			//	panel.render(rasterizer);
			//	panel.render(rasterizer);
			//    panel.render(rasterizer);
				this.run();
				return;
			}
			
		}

	}

}
