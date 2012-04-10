package RayTracer;

public class ModelMaterial {
	public String name;
	public Matrix rgb;
	public double specular;
	public double reflect;
	public ModelMaterial(String name)
	{
		this.name = name;
	}
	public void setRGB(double R, double G, double B)
	{
		this.rgb = new Matrix(3,3);
		this.rgb.values[0][0] = R;
		this.rgb.values[1][1] = G;
		this.rgb.values[2][2] = B;
	}
	public void setSpecular(double spec)
	{
		this.specular = spec;
	}
	public void setReflectance(int reflect)
	{
		this.reflect = reflect;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Name " + name+ "\n");
		sb.append(rgb.toString());
		sb.append("Specular " + specular + " Reflection: "  + reflect + "\n");
		return sb.toString();
	}
	
}
