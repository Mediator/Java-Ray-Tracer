package animation;

import engine.util.Matrix;

public class ScaleAnimation extends ModelAnimation {
	double original;
	double end;
	double deltaScale;
	double deltaStep;
	boolean forward = true;
	long lastFrame = 0;
	boolean reverse;
	public ScaleAnimation(double original, double end, boolean reverse)
	{
		this.reverse = reverse;
		this.original = original;
		this.end = end;
		deltaScale = end - original;
		forward = true;
	}
	
	public Matrix getFrame(long time)
	{
		if (lastFrame == 0)
		{
			lastFrame = time;
			return Matrix.scale(original, original, original);
		}
		else
		{
			
			double deltaTime = time - lastFrame;
			if (reverse)
			{
				if (deltaTime > 2000)
				{
					lastFrame = time;
				}
			}
			double fract = deltaTime / 2000;
			
			if ( reverse)
			{
				fract = Math.min(1.0, fract);
				fract = 1 - Math.abs(1 - (2 * fract));
			}
			double scale = original + (fract * (end - original));
			return Matrix.scale(scale,scale,scale);
		}
		
		 
		
		
	}
	
}
