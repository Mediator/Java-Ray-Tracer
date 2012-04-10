package RayTracer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;


public class CommandParser {

	public void parse(String file, HashMap<String,ModelGroup> groups)
	{
		try {
			handleFile(file, groups);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Invalid command file: " + e.getMessage());
			
			e.printStackTrace();
			System.exit(1);
		}
		return;
	}
	private void handleFile(String file, HashMap<String,ModelGroup> groups) throws Exception
	{
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
			boolean rotate = false;
			boolean translate = false;
			boolean scale = false;
			boolean arbitrary = false;
			boolean camera = false;
			boolean lcamera = false;
			boolean wireframe = false;
			boolean light = false;
			boolean material = false;
			boolean render = false;
			boolean raytrace = false;
			//
			
			
			StringBuilder bld = null;
			
			String curGroupName = "";
			
			for (int i = 0; i < len; i++) {
				switch ((char)buf[i])
				{
				case '#':
					//System.out.println("Comment");
					if (cmd)
						throw new Exception("Invalid command format: unexpected #");
					comment = true;
					cmd = true;
					break;
				case 'c':
					//System.out.println("Camera");
					if (cmd)
						throw new Exception("Invalid command format: unexpected c");

					bld = new StringBuilder();
					curGroupName = "";
					camera = true;
					cmd = true;
					break;
				case 'l':
					longCommandName = new StringBuilder();
					longCommandName.append((char)buf[i]);
					while ((char)buf[++i] != ' ' && (char)buf[i] != '\t')
						longCommandName.append((char)buf[i]);
					--i;
					if (longCommandName.toString().equalsIgnoreCase("lookat"))
					{
						//System.out.println("Lookat Camera");
						if (cmd)
							throw new Exception("Invalid command format: unexpected lookat");

						bld = new StringBuilder();
						curGroupName = "";
						lcamera = true;
						cmd = true;
					}
					else if (longCommandName.toString().equalsIgnoreCase("l"))
					{
						//System.out.println("Light");
						if (cmd)
							throw new Exception("Invalid command format: unexpected l");

						bld = new StringBuilder();
						curGroupName = "";
						light = true;
						cmd = true;
					}
					else
					{
						throw new Exception("Invalid command format: unknown command");
					}
					
					break;
				
				case 'w':
					//System.out.println("Wireframe");
					if (cmd)
						throw new Exception("Invalid command format: unexpected w");

					bld = new StringBuilder();
					curGroupName = "";
					wireframe = true;
					cmd = true;
					break;
				case 'g':
					//System.out.println("Render");
					if (cmd)
						throw new Exception("Invalid command format: unexpected g");

					bld = new StringBuilder();
					curGroupName = "";
					render = true;
					cmd = true;
					break;
				case 'm':
					//System.out.println("Material");
					if (cmd)
						throw new Exception("Invalid command format: unexpected m");

					bld = new StringBuilder();
					curGroupName = "";
					material = true;
					cmd = true;
					break;
				case 'r':
					
					longCommandName = new StringBuilder();
					longCommandName.append((char)buf[i]);
					while ((char)buf[++i] != ' ' && (char)buf[i] != '\t')
						longCommandName.append((char)buf[i]);
					--i;
					if (longCommandName.toString().equalsIgnoreCase("rt"))
					{
						//System.out.println("Lookat Camera");
						if (cmd)
							throw new Exception("Invalid command format: unexpected raytrace");

						bld = new StringBuilder();
						curGroupName = "";
						raytrace = true;
						cmd = true;
					}
					else if (longCommandName.toString().equalsIgnoreCase("r"))
					{
						//System.out.println("Rotate");
						if (cmd)
							throw new Exception("Invalid command format: unexpected r");

						bld = new StringBuilder();
						curGroupName = "";
						rotate = true;
						cmd = true;
						break;
					}
					else
					{
						throw new Exception("Invalid command format: unknown command");
					}
					
					break;
					
					
				case 't':
					//System.out.println("Translate");
					if (cmd)
						throw new Exception("Invalid command format: unexpected t");

					bld = new StringBuilder();
					curGroupName = "";
					translate = true;
					cmd = true;
					break;
				case 's':
					//System.out.println("Scale");
					if (cmd)
						throw new Exception("Invalid command format: unexpected s");

					bld = new StringBuilder();
					curGroupName = "";
					scale = true;
					cmd = true;
					break;
				case 'a':
					//System.out.println("Arbitrary");
					if (cmd)
						throw new Exception("Invalid command format: unexpected a");

					bld = new StringBuilder();
					curGroupName = "";
					arbitrary = true;
					cmd = true;
					break;
				default:
					if (!cmd  && ((char)buf[i] == ' ' || (char)buf[i] == '\t' || (char)buf[i] == '\r' || (char)buf[i] == '\n'))
						continue;
					else if (!cmd)
						throw new Exception("Invalid command format: unexpected " + (char)buf[i]);
					while ((char)buf[i] == ' ' || (char)buf[i] == '\t')
						i++;
					
					if (rotate)
					{
						boolean needsName = true,needsAmount = true, needsX = true, needsY = true, needsZ= true;
						double ramount = 0,rx = 0,ry = 0,rz = 0;
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							if ((char)buf[i] == ' ')
							{
								i++;
								continue;
							}
							bld.append((char)buf[i]);
							++i;
							if (i >= buf.length || (char)buf[i] == ' ' || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (needsName == true)
								{
									curGroupName = bld.toString();
									
									bld = new StringBuilder();
									needsName = false;
								}
								else if (needsAmount == true)
								{
									ramount = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsAmount = false;
								}
								else if (needsX == true)
								{
									rx = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsX = false;
								}
								else if (needsY == true)
								{
									ry = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsY = false;
								}
								else if (needsZ == true)
								{
									rz = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsZ = false;
								}
								if (i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n')
								{
									if (needsZ == true)
										throw new Exception("Invalid command format: rotate missing arguement");
								}
							}
						}
						ModelGroup mg = groups.get(curGroupName);
						mg.nonPreservingMatrix = Matrix.multiply(Matrix.rotate(ramount, rx, ry, rz),mg.nonPreservingMatrix);
						mg.composedMatrix = Matrix.multiply(Matrix.rotate(ramount, rx, ry, rz),mg.composedMatrix);
						//System.out.println(mg.composedMatrix);
						rotate = false;
						cmd = false;
					}
					else if (translate)
					{
						boolean needsName = true, needsX = true, needsY = true, needsZ= true;
						double rx = 0,ry = 0,rz = 0;
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							if ((char)buf[i] == ' ')
							{
								i++;
								continue;
							}
							bld.append((char)buf[i]);
							++i;
							if (i >= buf.length || (char)buf[i] == ' ' || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (needsName == true)
								{
									curGroupName = bld.toString();
									
									bld = new StringBuilder();
									needsName = false;
								}
								else if (needsX == true)
								{
									rx = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsX = false;
								}
								else if (needsY == true)
								{
									ry = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsY = false;
								}
								else if (needsZ == true)
								{
									rz = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsZ = false;
								}
								if (i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n')
								{
									if (needsZ == true)
										throw new Exception("Invalid command format: translate missing arguement");
								}
							}
						}
						ModelGroup mg = groups.get(curGroupName);
						mg.composedMatrix = Matrix.multiply(Matrix.translate(rx, ry, rz),mg.composedMatrix);
						//System.out.println(mg.composedMatrix);
						translate = false;
						cmd = false;
					}
					else if (scale)
					{
						boolean needsName = true, needsX = true, needsY = true, needsZ= true;
						double rx = 0,ry = 0,rz = 0;
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							if ((char)buf[i] == ' ')
							{
								i++;
								continue;
							}
							bld.append((char)buf[i]);
							++i;
							if (i >= buf.length || (char)buf[i] == ' ' || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (needsName == true)
								{
									curGroupName = bld.toString();
									
									bld = new StringBuilder();
									needsName = false;
								}
								else if (needsX == true)
								{
									rx = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsX = false;
								}
								else if (needsY == true)
								{
									ry = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsY = false;
								}
								else if (needsZ == true)
								{
									rz = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsZ = false;
								}
								if (i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n')
								{
									if (needsZ == true)
										throw new Exception("Invalid command format: scale missing arguement");
								}
							}
						}
						ModelGroup mg = groups.get(curGroupName);
						mg.composedMatrix = Matrix.multiply(Matrix.scale(rx, ry, rz),mg.composedMatrix);
						if (rx != ry || rx != rz || ry != rz)
						{
							mg.nonPreservingMatrix = Matrix.multiply(Matrix.scale(rx, ry, rz),mg.nonPreservingMatrix);
						}
						//System.out.println(mg.composedMatrix);
						scale = false;
						cmd = false;
					}
					else if (arbitrary)
					{
						boolean needsName = true;
						int curVec = 0;
						double[] vects = new double[16];
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							if ((char)buf[i] == ' ')
							{
								i++;
								continue;
							}
							bld.append((char)buf[i]);
							++i;
							if (i >= buf.length || (char)buf[i] == ' ' || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (needsName == true)
								{
									curGroupName = bld.toString();
									
									bld = new StringBuilder();
									needsName = false;
								}
								else if (curVec <= 15)
								{
									vects[curVec++] = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
								}
								if (i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n')
								{
									
									if (curVec <= 15)
										throw new Exception("Invalid command format: arbitrary missing arguement");
								}
							}
						}
						ModelGroup mg = groups.get(curGroupName);
						Matrix zz = new Matrix(vects[0],vects[1],vects[2],vects[3],
								vects[4],vects[5],vects[6],vects[7],
								vects[8],vects[9],vects[10],vects[11],
								vects[12],vects[13],vects[14],vects[15]);
						mg.nonPreservingMatrix = Matrix.multiply(zz, mg.nonPreservingMatrix);
						mg.composedMatrix = Matrix.multiply(zz,mg.composedMatrix);
						//System.out.println(mg.composedMatrix);
						arbitrary = false;
						cmd = false;
					}
					else if (camera)
					{
						boolean needsName = true, needsFL = true, needsFPX = true, needsFPY= true, needsFPZ = true;
						boolean needsVPNX = true, needsVPNY = true, needsVPNZ = true;
						boolean needsVUPX = true, needsVUPY = true, needsVUPZ = true;
						double fl = 0;
						double fpX = 0, fpY = 0, fpZ = 0;
						double vpnX = 0, vpnY = 0, vpnZ = 0;
						double vupX = 0, vupY = 0, vupZ = 0;
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							if ((char)buf[i] == ' ')
							{
								i++;
								continue;
							}
							bld.append((char)buf[i]);
							++i;
							if (i >= buf.length || (char)buf[i] == ' ' || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (needsName == true)
								{
									curGroupName = bld.toString();
									
									bld = new StringBuilder();
									needsName = false;
								}
								else if (needsFL == true)
								{
									fl = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsFL = false;
								}
								else if (needsFPX == true)
								{
									fpX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsFPX = false;
								}
								else if (needsFPY == true)
								{
									fpY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsFPY = false;
								}
								else if (needsFPZ == true)
								{
									fpZ = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsFPZ = false;
								}
								else if (needsVPNX == true)
								{
									vpnX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsVPNX = false;
								}
								else if (needsVPNY == true)
								{
									vpnY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsVPNY = false;
								}
								else if (needsVPNZ == true)
								{
									vpnZ = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsVPNZ = false;
								}
								else if (needsVUPX == true)
								{
									vupX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsVUPX = false;
								}
								else if (needsVUPY == true)
								{
									vupY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsVUPY = false;
								}
								else if (needsVUPZ == true)
								{
									vupZ = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsVUPZ = false;
								}
								if (i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n')
								{
									if (needsVUPZ == true)
										throw new Exception("Invalid command format: camera missing arguement");
								}
							}
						}
						if (ModelGroup.cameras == null || ModelGroup.cameras.length == 0)
							ModelGroup.cameras = new Camera[1];
						Camera cam = null;
						for (int x = 0; x < ModelGroup.cameras.length; x++)
						{
							if (ModelGroup.cameras[x] == null)
								break;
							if (ModelGroup.cameras[x].name.equals(curGroupName))
								cam = ModelGroup.cameras[x];
						}
						if (cam == null)
						{
							cam = new Camera(curGroupName);
							if (ModelGroup.cameras.length > 1)
								ModelGroup.cameras = ArrayUtil.redimArray(ModelGroup.cameras, ModelGroup.cameras.length+1);

							ModelGroup.cameras[ModelGroup.cameras.length-1] = cam;
						}
						cam.initializeCamera(fl, fpX, fpY, fpZ, vpnX, vpnY, vpnZ, vupX, vupY, vupZ);
						//System.out.println("Camera: " + cam.cameraMatrix);
						
						camera = false;
						cmd = false;
					}
					else if (lcamera)
					{
						boolean needsName = true, needsFL = true, needsFPX = true, needsFPY= true, needsFPZ = true;
						boolean needsVPNX = true, needsVPNY = true, needsVPNZ = true;
						boolean needsVUPX = true, needsVUPY = true, needsVUPZ = true;
						double fl = 0;
						double fpX = 0, fpY = 0, fpZ = 0;
						double vpnX = 0, vpnY = 0, vpnZ = 0;
						double vupX = 0, vupY = 0, vupZ = 0;
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							if ((char)buf[i] == ' ')
							{
								i++;
								continue;
							}
							bld.append((char)buf[i]);
							++i;
							if (i >= buf.length || (char)buf[i] == ' ' || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (needsName == true)
								{
									curGroupName = bld.toString();
									
									bld = new StringBuilder();
									needsName = false;
								}
								else if (needsFL == true)
								{
									fl = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsFL = false;
								}
								else if (needsFPX == true)
								{
									fpX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsFPX = false;
								}
								else if (needsFPY == true)
								{
									fpY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsFPY = false;
								}
								else if (needsFPZ == true)
								{
									fpZ = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsFPZ = false;
								}
								else if (needsVPNX == true)
								{
									vpnX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsVPNX = false;
								}
								else if (needsVPNY == true)
								{
									vpnY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsVPNY = false;
								}
								else if (needsVPNZ == true)
								{
									vpnZ = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsVPNZ = false;
								}
								else if (needsVUPX == true)
								{
									vupX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsVUPX = false;
								}
								else if (needsVUPY == true)
								{
									vupY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsVUPY = false;
								}
								else if (needsVUPZ == true)
								{
									vupZ = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsVUPZ = false;
								}
								if (i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n')
								{
									if (needsVUPZ == true)
										throw new Exception("Invalid command format: camera missing arguement");
								}
							}
						}
						if (ModelGroup.cameras == null || ModelGroup.cameras.length == 0)
							ModelGroup.cameras = new Camera[1];
						Camera cam = null;
						for (int x = 0; x < ModelGroup.cameras.length; x++)
						{
							if (ModelGroup.cameras[x] == null)
								break;
							if (ModelGroup.cameras[x].name.equals(curGroupName))
								cam = ModelGroup.cameras[x];
						}
						if (cam == null)
						{
							cam = new Camera(curGroupName);
							if (ModelGroup.cameras.length > 1)
								ModelGroup.cameras = ArrayUtil.redimArray(ModelGroup.cameras, ModelGroup.cameras.length+1);

							ModelGroup.cameras[ModelGroup.cameras.length-1] = cam;
						}
						cam.initializeLookAtCamera(fl, fpX, fpY, fpZ, vpnX, vpnY, vpnZ, vupX, vupY, vupZ);
						//System.out.println("Camera: " + cam.cameraMatrix);
						
						lcamera = false;
						cmd = false;
					}
					else if (wireframe)
					{
						boolean needsName = true;
						boolean needsMinX = true, needsMinY = true;
						boolean needsMaxX = true, needsMaxY = true;
						double minX = 0,minY = 0;
						double maxX = 0, maxY = 0;
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							if ((char)buf[i] == ' ')
							{
								i++;
								continue;
							}
							bld.append((char)buf[i]);
							++i;
							if (i >= buf.length || (char)buf[i] == ' ' || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (needsName == true)
								{
									curGroupName = bld.toString();
									
									bld = new StringBuilder();
									needsName = false;
								}
								else if (needsMinX == true)
								{
									minX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsMinX = false;
								}
								else if (needsMinY == true)
								{
									minY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsMinY = false;
								}
								else if (needsMaxX == true)
								{
									maxX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsMaxX = false;
								}
								else if (needsMaxY == true)
								{
									maxY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsMaxY = false;
								}
								if (i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n')
								{
									if (needsMaxY == true)
										throw new Exception("Invalid command format: wireframe missing arguement");
								}
							}
						}
						Camera wCam = null;
						if (ModelGroup.cameras == null || ModelGroup.cameras.length == 0)
							ModelGroup.cameras = new Camera[1];
						for (int x = 0; x < ModelGroup.cameras.length; x++)
						{
							if (ModelGroup.cameras[x] == null)
								break;
							if (ModelGroup.cameras[x].name.equals(curGroupName))
								wCam = ModelGroup.cameras[x];
						}
						if (wCam == null)
						{
							wCam = new Camera(curGroupName);
							if (ModelGroup.cameras.length > 1)
								ModelGroup.cameras = ArrayUtil.redimArray(ModelGroup.cameras, ModelGroup.cameras.length+1);
							ModelGroup.cameras[ModelGroup.cameras.length-1] = wCam;
						}
						wCam.initializeGraphics(minX, minY, maxX, maxY);
						wireframe = false;
						cmd = false;
					}
					else if (material)
					{
						boolean needsName = true;
						boolean needsR = true, needsG = true, needsB = true;
						boolean needsS = true, needsA = true;
						double colorR = 0,colorG = 0, colorB = 0;
						double matSpec = 0;
						int matReflect = 0;
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							if ((char)buf[i] == ' ')
							{
								i++;
								continue;
							}
							bld.append((char)buf[i]);
							++i;
							if (i >= buf.length || (char)buf[i] == ' ' || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (needsName == true)
								{
									curGroupName = bld.toString();
									
									bld = new StringBuilder();
									needsName = false;
								}
								else if (needsR == true)
								{
									colorR = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsR = false;
								}
								else if (needsG == true)
								{
									colorG = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsG = false;
								}
								else if (needsB == true)
								{
									colorB = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsB = false;
								}
								else if (needsS == true)
								{
									matSpec = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsS = false;
								}
								else if (needsA == true)
								{
									matReflect = Integer.parseInt(bld.toString());
									bld = new StringBuilder();
									needsA = false;
								}
								if (i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n')
								{
									if (needsA == true)
										throw new Exception("Invalid command format: material missing arguement");
								}
							}
						}
						ModelMaterial m = ModelGroup.materials.get(curGroupName);
						m.setRGB(colorR, colorG, colorB);
						m.setSpecular(matSpec);
						m.setReflectance(matReflect);
							
						material = false;
						cmd = false;
					}
					
					else if (light)
					{
						boolean needsName = true;
						boolean needsX = true, needsY = true, needsZ = true, needsW = true;
						boolean needsR = true, needsG = true, needsB = true;
						double lightX = 0, lightY = 0, lightZ = 0, lightW = 0;;
						int lightR = 0, lightG = 0, lightB = 0;
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							if ((char)buf[i] == ' ')
							{
								i++;
								continue;
							}
							bld.append((char)buf[i]);
							++i;
							if (i >= buf.length || (char)buf[i] == ' ' || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (needsX == true)
								{
									lightX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsX = false;
								}
								else if (needsY == true)
								{
									lightY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsY = false;
								}
								else if (needsZ == true)
								{
									lightZ = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsZ = false;
								}
								else if (needsW == true)
								{
									lightW = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsW = false;
								}
								else if (needsR == true)
								{
									lightR = Integer.parseInt(bld.toString());
									bld = new StringBuilder();
									needsR = false;
								}
								else if (needsG == true)
								{
									lightG = Integer.parseInt(bld.toString());
									bld = new StringBuilder();
									needsG = false;
								}
								else if (needsB == true)
								{
									lightB = Integer.parseInt(bld.toString());
									bld = new StringBuilder();
									needsB = false;
								}
								if (i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n')
								{
									if (needsB == true)
										throw new Exception("Invalid command format: light missing arguement");
								}
							}
						}
						ModelGroup.lights.add(new LightSource(lightX, lightY, lightZ, lightW, lightR, lightG, lightB));
						light = false;
						cmd = false;
					}
					else if (render)
					{
						boolean needsName = true;
						boolean needsMinX = true, needsMinY = true;
						boolean needsMaxX = true, needsMaxY = true;
						double minX = 0,minY = 0;
						double maxX = 0, maxY = 0;
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							if ((char)buf[i] == ' ')
							{
								i++;
								continue;
							}
							bld.append((char)buf[i]);
							++i;
							if (i >= buf.length || (char)buf[i] == ' ' || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (needsName == true)
								{
									curGroupName = bld.toString();
									
									bld = new StringBuilder();
									needsName = false;
								}
								else if (needsMinX == true)
								{
									minX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsMinX = false;
								}
								else if (needsMinY == true)
								{
									minY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsMinY = false;
								}
								else if (needsMaxX == true)
								{
									maxX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsMaxX = false;
								}
								else if (needsMaxY == true)
								{
									maxY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsMaxY = false;
								}
								if (i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n')
								{
									if (needsMaxY == true)
										throw new Exception("Invalid command format: render missing arguement");
								}
							}
						}
						Camera wCam = null;
						if (ModelGroup.cameras == null || ModelGroup.cameras.length == 0)
							ModelGroup.cameras = new Camera[1];
						for (int x = 0; x < ModelGroup.cameras.length; x++)
						{
							if (ModelGroup.cameras[x] == null)
								break;
							if (ModelGroup.cameras[x].name.equals(curGroupName))
								wCam = ModelGroup.cameras[x];
						}
						if (wCam == null)
						{
							wCam = new Camera(curGroupName);
							if (ModelGroup.cameras.length > 1)
								ModelGroup.cameras = ArrayUtil.redimArray(ModelGroup.cameras, ModelGroup.cameras.length+1);
							ModelGroup.cameras[ModelGroup.cameras.length-1] = wCam;
						}
						wCam.initializeGraphics(minX, minY, maxX, maxY, true);
						render = false;
						cmd = false;
					}
					else if (raytrace)
					{
						boolean needsName = true;
						boolean needsMinX = true, needsMinY = true;
						boolean needsMaxX = true, needsMaxY = true;
						double minX = 0,minY = 0;
						double maxX = 0, maxY = 0;
						while (i < buf.length && (char)buf[i] != '\r' && (char)buf[i] != '\n')
						{
							if ((char)buf[i] == ' ')
							{
								i++;
								continue;
							}
							bld.append((char)buf[i]);
							++i;
							if (i >= buf.length || (char)buf[i] == ' ' || (char)buf[i] == '\r' || (char)buf[i] == '\n' || (char)buf[i] == '\t')
							{
								if (needsName == true)
								{
									curGroupName = bld.toString();
									
									bld = new StringBuilder();
									needsName = false;
								}
								else if (needsMinX == true)
								{
									minX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsMinX = false;
								}
								else if (needsMinY == true)
								{
									minY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsMinY = false;
								}
								else if (needsMaxX == true)
								{
									maxX = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsMaxX = false;
								}
								else if (needsMaxY == true)
								{
									maxY = Double.parseDouble(bld.toString());
									bld = new StringBuilder();
									needsMaxY = false;
								}
								if (i >= buf.length || (char)buf[i] == '\r' || (char)buf[i] == '\n')
								{
									if (needsMaxY == true)
										throw new Exception("Invalid command format: raytrace missing arguement");
								}
							}
						}
						Camera wCam = null;
						if (ModelGroup.cameras == null || ModelGroup.cameras.length == 0)
							ModelGroup.cameras = new Camera[1];
						for (int x = 0; x < ModelGroup.cameras.length; x++)
						{
							if (ModelGroup.cameras[x] == null)
								break;
							if (ModelGroup.cameras[x].name.equals(curGroupName))
								wCam = ModelGroup.cameras[x];
						}
						if (wCam == null)
						{
							wCam = new Camera(curGroupName);
							if (ModelGroup.cameras.length > 1)
								ModelGroup.cameras = ArrayUtil.redimArray(ModelGroup.cameras, ModelGroup.cameras.length+1);
							ModelGroup.cameras[ModelGroup.cameras.length-1] = wCam;
						}
						wCam.initializeRaytraceGraphics(minX, minY, maxX, maxY);
						raytrace = false;
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
		}
		catch (IOException e) {
			System.out.println("Experienced and IO Exception while attempting reading command file");
		}
	}
	
}

