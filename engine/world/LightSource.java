package engine.world;

public class LightSource {
	public double[] location;
	public double[] composedLocation;
	public double[] rgb;
	
	public LightSource(double X, double Y, double Z, double W, int R, int G, int B)
	{
		location = new double[] {X,Y,Z,W};
		rgb = new double[] {R,G,B};

	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Location {" + location[0] + "," + location[1] + "," + location[2] + "," + location[3] + "}\n");
		//sb.append("Composed Location {" + composedLocation[0] + "," + composedLocation[1] + "," + composedLocation[2] + "," + composedLocation[3] + "}\n");
		sb.append("Color {" + rgb[0] + "," + rgb[1] + "," + rgb[2] + "}\n");
		return sb.toString();
	}
}
