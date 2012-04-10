package animation;

import engine.util.Matrix;

public class RotationAnimation extends ModelAnimation {
	double original;
	double end;
	double deltaRotation;
	double deltaStep;
	boolean forward = true;
	long lastFrame = 0;
	double rotateY,rotateX, rotateZ;
	boolean reverse;
	public RotationAnimation(double original, double end, double rotateX, double rotateY, double rotateZ, boolean reverse)
	{
		this.reverse = reverse;
		this.original = original;
		this.end = end;
		this.rotateY = rotateY;
		this.rotateX = rotateX;
		this.rotateZ = rotateZ;
		deltaRotation = end - original;
		forward = true;
	}
	
	public RotationAnimation(Matrix translation, double original, double end, boolean reverse)
	{
		this.original = original;
		this.end = end;
		deltaRotation = end - original;
		forward = true;
	}
	
	public Matrix getFrame(long time)
	{
		if (lastFrame == 0)
		{
			lastFrame = time;
			return Matrix.rotate(original, rotateX, rotateX, rotateZ);
		}
		else
		{
			double deltaTime = time - lastFrame;
			if (reverse)
			{
			if (deltaTime > 3000)
				lastFrame = time;
			}
			double fract = deltaTime / 3000;
			if (reverse)
			{
			fract = Math.min(1.0, fract);
			fract = 1 - Math.abs(1 - (2 * fract));
		}
			double rotation = original + (fract * (end - original));
			//System.out.println(Matrix.rotate(rotation, rotateX, rotateY, rotateZ));
			return Matrix.rotate(rotation, rotateX, rotateY, rotateZ);
		}
		
		 
		
		
	}
	
}
