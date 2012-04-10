package engine.world;

import engine.util.Matrix;

public class ModelMaterial {
	public String name;
	public Matrix rgb = Matrix.identity();
	public double specular = 0;
	public double reflect = 1;
	public double transparency = 0;
	public ModelMaterial(String name)
	{
		this.name = name;
	}
	public void setRGB(double R, double G, double B)
	{
		if (R < 0 || G < 0 || B < 0)
			throw new IllegalArgumentException("Invalid material color");
		this.rgb = new Matrix(3,3);
		this.rgb.values[0][0] = R;
		this.rgb.values[1][1] = G;
		this.rgb.values[2][2] = B;
		validateProperties();
	}
	public void setTransparency(double trans)
	{

		this.transparency = trans;
		validateProperties();
		
	}
	public void setSpecular(double spec)
	{
		this.specular = spec;
		validateProperties();
	}
	public void setReflectance(int reflect)
	{
		this.reflect = reflect;
		validateProperties();
	}
	public void validateProperties()
	{
		if ((transparency + specular + this.rgb.values[0][0]) > 1)
			throw new IllegalArgumentException("Invalid Material Properties1 " + (transparency + specular + this.rgb.values[0][0]) + " " + transparency + " " + specular + " " + this.rgb.values[0][0]);
		
		if ((transparency + specular + this.rgb.values[1][1]) > 1)
			throw new IllegalArgumentException("Invalid Material Properties2");
		
		if ((transparency + specular + this.rgb.values[2][2]) > 1)
			throw new IllegalArgumentException("Invalid Material Properties3");
		
		if (reflect < 1 || reflect > 200)
			throw new IllegalArgumentException("Invalid Material Properties4");
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Name " + name+ "\n");
		sb.append(rgb.toString());
		sb.append("Specular " + specular + " Reflection: "  + reflect + " Transparency: " + transparency +  "\n");
		return sb.toString();
	}
	
}
