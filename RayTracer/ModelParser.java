package RayTracer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;


public class ModelParser {

	public HashMap<String,ModelGroup> parse(String file)
	{
		try {
			return handleFile(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("An error was encountered with the model file: " + e.getMessage());

			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	private HashMap<String,ModelGroup> handleFile(String file) throws Exception
	{
		HashMap<String,ModelGroup> groups = new HashMap<String,ModelGroup>();
		try {
			int len = (int)(new File(file).length());
			FileInputStream fis =
				new FileInputStream(file);
			byte buf[] = new byte[len];
			fis.read(buf);
			fis.close();

			StringBuilder longCommandName;
			
			// Params
			boolean cmd = false;
			boolean comment = false;
			boolean group = false;
			boolean face = false;
			boolean vert = false;
			boolean material = false;
			//



			double[][] verts = new double[10000][4];
			int curVertIdx = -1;
			int curPolygonIdx = -1;
			ModelGroup curGroup = new ModelGroup();
			groups.put("default",curGroup);
			String[] curGroupNames = new String[30];
			Polygon[] curGroupPolygons = new Polygon[1024];
			int numNames = 0;
			int[] curPoly = null;
			StringBuilder bld = null;
			ModelMaterial curMaterial = new ModelMaterial("Default");
			curMaterial.setRGB(0.58, .2, 0);
			curMaterial.setReflectance(50);
			curMaterial.setSpecular(.48);
			
			for (int i = 0; i < len; i++) {
				switch ((char)buf[i])
				{
				case '#':
					//System.out.println("Comment");
					if (cmd)
						throw new Exception("Invalid model format: unexpected #");
					comment = true;
					cmd = true;
					break;
				case 'g':
					//System.out.println("Group");
					if (cmd)
						throw new Exception("Invalid model format: unexpected g");

					bld = new StringBuilder();
					if (curGroup != null)
					{
						curGroup.polys = ArrayUtil.redimArray(curGroupPolygons,curPolygonIdx+1);
					}
					curPolygonIdx = -1;
					numNames = 0;
					curGroupNames = new String[30];
					curGroupPolygons = new Polygon[1024];
					curGroup = new ModelGroup();
					group = true;
					cmd = true;
					break;
				case 'f':
					//System.out.println("Face");
					if (cmd)
						throw new Exception("Invalid model format: unexpected f");
					curPoly = new int[4];
					face = true;
					cmd = true;
					break;
				case 'u':
					//System.out.println("Use Material Face");
					longCommandName = new StringBuilder();
					longCommandName.append((char)buf[i]);
					while ((char)buf[++i] != ' ' && (char)buf[i] != '\t')
						longCommandName.append((char)buf[i]);
					--i;
					if (longCommandName.toString().equalsIgnoreCase("usemtl"))
					{
						if (cmd)
							throw new Exception("Invalid model format: unexpected usemtl");
						bld = new StringBuilder();
						material = true;
						cmd = true;
						break;
					}
					else
						throw new Exception("Invalid model format: unknown command");
				case 'v':
					//System.out.println("Vert");
					if (cmd)
						throw new Exception("Invalid model format: unexpected v");
					curVertIdx++;
					if (curVertIdx == verts.length)
						verts = ArrayUtil.increaseVerts(verts);
					vert = true;
					cmd = true;
					break;
				default:
					if (!cmd  && ((char)buf[i] == ' ' || (char)buf[i] == '\t' || (char)buf[i] == '\r' || (char)buf[i] == '\n'))
						continue;
					else if (!cmd)
						throw new Exception("Invalid model format: unexpected " + (char)buf[i]);
					while(((char)buf[i] == ' ' || (char)buf[i] == '\t'))
					{
						i++;
					}

					if (group)
					{
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							if ((char)buf[i] == ' ' || (char)buf[i] == '\t')
							{
								if (bld.length() == 0)
								{
									i++;
									continue;
								}
								else
								{
									curGroupNames[numNames++] = bld.toString();
									bld = new StringBuilder();
								}
							}
							bld.append((char)buf[i++]);
						}
						if (bld.length() > 0)
							curGroupNames[numNames++] = bld.toString();
						if (numNames > 0)
						{
							for (int j = 0; j < numNames; j++)
							{
								curGroup.name += curGroupNames[j];
								groups.put(curGroupNames[j], curGroup);
							}
						}
						else
						{
							curGroup.name += "default";
							groups.put("default", curGroup);
						}
						group = false;
						cmd = false;
					}
					if (material)
					{
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n' && (char)buf[i] != ' ' && (char)buf[i] != '\t')
						{
								bld.append((char)buf[i++]);
						}
						if (bld.length() == 0)
						{
							throw new Exception("No material name supplied");
						}
						else
						{
							if (ModelGroup.materials.containsKey(bld.toString()))
							{
								curMaterial = ModelGroup.materials.get(bld.toString());
							}
							else
							{
								curMaterial = new ModelMaterial(bld.toString());
								ModelGroup.materials.put(bld.toString(),curMaterial);
							}
							bld = new StringBuilder();
						}
						material = false;
						cmd = false;
					}
					if (vert)
					{
						bld = new StringBuilder();
						short curVert = 0;
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							bld.append((char)buf[i]);
							++i;
							if (i >= buf.length || (char)buf[i] == ' ' || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (bld.toString().trim().length() != 0)
								{
									//System.out.println(bld.toString());
									verts[curVertIdx][curVert++] = Double.parseDouble(bld.toString().trim());
									
									bld = new StringBuilder();
								}
							}
						}
						//System.out.println("Read in vert: " + Arrays.toString(verts[curVertIdx]));
						if (curVert > 4 || curVert <= 2)
							throw new Exception("Invalid object file: Invalid number of vertices: " + curVert);
						else if (curVert == 3)
							verts[curVertIdx][3] = 1.0f;
						vert = false;
						cmd = false;
					}
					else if (face)
					{
						bld = new StringBuilder();
						short curVert = 0;
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							bld.append((char)buf[i]);
							++i;
							if ((char)buf[i] == ' ' || i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (bld.toString().trim().length() != 0)
								{
									if (curVert == curPoly.length-1)
										curPoly = ArrayUtil.redimArray(curPoly,curPoly.length*2);
									curPoly[curVert++] = Integer.parseInt(bld.toString().trim())-1;
									bld = new StringBuilder();
								}
								if (i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n')
								{
									if (curPolygonIdx == curGroupPolygons.length-1)
										curGroupPolygons = ArrayUtil.redimArray(curGroupPolygons, curGroupPolygons.length*2);
									curGroupPolygons[++curPolygonIdx] = new Polygon(ArrayUtil.redimArray(curPoly,curVert),curMaterial);
								}
							}
						}
						if (curVert <= 2)
							throw new Exception("Invalid object file: Face has too few vertices");
						face = false;
						cmd = false;
					}
					else if (comment)
					{
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							i++;
						}
						cmd = false;
						comment = false;
					}

					break;
				}
			}
			if (curGroup != null)
			{
				curGroup.polys = ArrayUtil.redimArray(curGroupPolygons,curPolygonIdx+1);
				Polygon.verts = ArrayUtil.redimVerts(verts,curVertIdx+1);
			}
		}
		catch (IOException e) {
			System.out.println("Experienced an IO Exception while trying to read model");
			System.exit(1);
		}
		return groups;
	}

}

