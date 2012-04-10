package io;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import engine.parallel.WorkNotifier;
import engine.world.ModelGroup;
import engine.world.Polygon;

public class ModelWriter implements WorkNotifier {


	public void output(String file, HashMap<String,ModelGroup> groups)
	{
		try {
			handleModel(file, groups);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
	private void handleModel(String file, HashMap<String,ModelGroup> groups) throws Exception
	{

		ArrayList<ModelGroup> savedGroups = new ArrayList<ModelGroup>();
		Iterator<String> itr = groups.keySet().iterator();
		String[] ngroups = new String[groups.size()];
		int ctr = 0;
		while (itr.hasNext())
		{
			String curGroupName = itr.next();
			ngroups[ctr++] = curGroupName;
		}
		boolean outputtedVerts = false;
		for (int x = 0; x < ngroups.length; x++)
		{
			String xName = "g " + ngroups[x];
			ModelGroup curGroup = groups.get(ngroups[x]);
			if (ngroups[x].equals("default") && curGroup.polys.length == 0)
				continue;
			
			if (savedGroups.contains(curGroup))
				continue;
			
			for (int y = 0; y < ngroups.length; y++)
			{
				if (y == x)
					continue;
				if (curGroup == groups.get(ngroups[y]))
				{
					xName += " " + ngroups[y];
				}
			}
			savedGroups.add(curGroup);
			System.out.println("Applying group operations " + xName);
			long operationStartTime = System.currentTimeMillis();
			//curGroup.applyOpertation(this);
			long operationEndTime = System.currentTimeMillis();
			long operationRunTime = operationEndTime - operationStartTime;
			System.out.println("Time to perform matrix operations: " + operationRunTime + "ms on " + Polygon.verts.length + " vertices");
			
			try
			{
				FileWriter fstream = new FileWriter(file);

				BufferedWriter out = new BufferedWriter(fstream);
				out.write(xName + "\n");
				if (!outputtedVerts)
				{
					for (int z = 0; z < Polygon.transformedVerts.length; z++)
					{
						out.write("v " + Polygon.transformedVerts[z][0] + " " + Polygon.transformedVerts[z][1] + " " + Polygon.transformedVerts[z][2] + " " + Polygon.transformedVerts[z][3] + "\n");
					}
					outputtedVerts = true;
				}
				for (int z = 0; z < curGroup.polys.length; z++)
				{
					out.write("f");
					for (int j = 0; j < curGroup.polys[z].myVerts.length;j++)
					{
						out.write(" " + (curGroup.polys[z].myVerts[j]+1));
					}
					out.write("\n");
				}
				
				out.flush();
				out.close();
			}
			catch (Exception ex)
			{
				System.out.println("Failed to output model");
			}
		}
	}
	@Override
	public void workComplete() {
		// TODO Auto-generated method stub
		
	}

}

