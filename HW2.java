

import io.CommandParser;
import io.ImageWriter;
import io.ModelParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import engine.world.ModelGroup;
import engine.world.Polygon;
import engine.world.World;

public class HW2 {
	public static void main(String[] args)
	{
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
		//System.out.println("Groups: " + groups.values().size());

		//String s;
		//Iterator<String> i = groups.keySet().iterator();
		int[] usedVerts = new int[Polygon.verts.length];
		int curVert = -1;
		boolean firstGroup = true;
		ArrayList<ModelGroup> coveredGroups = new ArrayList<ModelGroup>();
		for (ModelGroup tmp : groups.values())
		{
			//s = i.next();

			//ModelGroup tmp = groups.get(s);

			if (coveredGroups.contains(tmp))
				continue;
			//System.out.println(tmp.composedMatrix);
			if (!tmp.validate())
			{
				System.out.println("Model format specifies invalid polygon");
				System.exit(1);
			}
			coveredGroups.add(tmp);
			if (tmp.hasPolys())
			{
				int[] modelUsedVerts = tmp.getUsedVectors();
				for (int x = 0; x < usedVerts.length;x++)
				{
					boolean found = false;
					for (int y = 0; y <= curVert; y++)
					{
						if (modelUsedVerts[x] == usedVerts[y])
						{
							found = true;
							if (!firstGroup)
							{
								System.out.println("Two groups reference the same vertex");
								System.exit(1);
							}
						}
					}
					if (!found)
						usedVerts[++curVert] = modelUsedVerts[x];
				}
				firstGroup = false;
			}
		}

		

		
		
		if (world.cameras == null || world.cameras.length == 0)
		{
			System.out.println("No camera's specified in model");
			System.exit(1);
		}		
		//ModelWriter mwriter = new ModelWriter();
		//mwriter.output(outObjFile, groups);

		ImageWriter iwriter = new ImageWriter();
		try
		{
			iwriter.output(outObjFile, world);
		}
		catch (Exception ex)
		{
			System.out.println("Rendering Error: " + ex.getMessage());
			System.exit(1);
		}
		
		
		
		/*i = groups.keySet().iterator();
		while (i.hasNext())
		{
			s = i.next();
			System.out.println(s);
			groups.get(s).print();
		}*/
	}
}
