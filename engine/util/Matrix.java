package engine.util;
public class Matrix {
	public double[][] values;
	public Matrix(int rows, int cols)
	{
		values = new double[rows][cols];
	}
	
	public boolean areTheSame(Matrix m)
	{
		//boolean ret;
		for (int x = 0; x < m.values.length;x++)
		{
			for (int y = 0; y < m.values[0].length;y++)
			{
				if (this.values[x][y] != m.values[x][y])
					return false;
			}
		}
		return true;
	}
	
	public Matrix(double x1, double y1, double z1, double w1,
			double x2, double y2, double z2, double w2,
			double x3, double y3, double z3, double w3,
			double x4, double y4, double z4, double w4)
	{
		values = new double[4][4];
		values[0][0] = x1;
		values[0][1] = y1;
		values[0][2] = z1;
		values[0][3] = w1;
		
		values[1][0] = x2;
		values[1][1] = y2;
		values[1][2] = z2;
		values[1][3] = w2;
		
		values[2][0] = x3;
		values[2][1] = y3;
		values[2][2] = z3;
		values[2][3] = w3;
		
		values[3][0] = x4;
		values[3][1] = y4;
		values[3][2] = z4;
		values[3][3] = w4;
	}
	
	public static Matrix identity()
	{
		Matrix m = new Matrix(4,4);
		m.values[0][0] = 1.0f;
		m.values[1][1] = 1.0f;
		m.values[2][2] = 1.0f;
		m.values[3][3] = 1.0f;
		return m;
	}
		
	public static Matrix transpose(Matrix original)
	{
		Matrix m = new Matrix(original.values[0].length,original.values.length);
		double[][] transpose = new double[original.values[0].length][original.values.length];
		for (int x = 0; x < original.values.length; x++)
		{
			for (int y = 0; y < original.values[0].length; y++)
			{
				transpose[x][y] = original.values[y][x];
			}
		}
		m.values = transpose;
		return m;
	}
	
	public static Matrix multiply(Matrix m1, Matrix m2)
	{
		Matrix m = new Matrix(m1.values.length,m1.values[0].length);
		double[][] result = new double[m1.values.length][m1.values[0].length];
		
		if (m1.values.length == 1)
		{
			result[0][0] = m1.values[0][0] * m2.values[0][0];
			m.values = result;
			return m;
		}
		
		for (int x = 0; x < m1.values.length; x++)
		{
			//System.out.println("X: " + x);
			for (int y = 0; y < m1.values.length; y++)
			{
				//System.out.println("Y: " + y);
				double sum = 0;
				for (int z = 0; z < m1.values.length; z++)
				{
					//System.out.println("Z: " + z);
					//System.out.println("Muliplying: " + m1.values[x][z] + " * " + m2.values[z][y]);
					sum += m1.values[x][z] * m2.values[z][y];
					//System.out.println("New Sum:" + sum);
				}
				
				result[x][y] = sum;
			}
		}
		
		m.values = result;	
		return m;
	}
	
	public static double[] crossProduct(double[] v1, double[] v2)
	{
		double[] result = new double[v1.length];
		
		result[0] = v1[1] * v2[2] - v1[2] * v2[1];
		result[1] = v1[2] * v2[0] - v1[0] * v2[2];
		result[2] = v1[0] * v2[1] - v1[1] * v2[0];
		result[3] = 1;
		return result;
	}
	
	public static double[] crossProduct3(double[] v1, double[] v2)
	{
		double[] result = new double[3];
		
		result[0] = v1[1] * v2[2] - v1[2] * v2[1];
		result[1] = v1[2] * v2[0] - v1[0] * v2[2];
		result[2] = v1[0] * v2[1] - v1[1] * v2[0];
		return result;
	}
	
	public static double[] multiply(double[] v1, double scalar)
	{
		
		double[] result = new double[v1.length];
		for (int x = 0; x < v1.length; x++)
			result[x] = v1[x] * scalar;

		//result[3] = v1[3];
		return result;
	}
	public static double[] add(double[] m1, double[] m2)
	{
		
		double[] result = new double[m2.length];
		
		
		for (int x = 0; x < m1.length; x++)
		{
			
				result[x] = m1[x] + m2[x];		

			
		}
		
			
		return result;
	}
	
	   // swap rows i and j
    private void swap(int i, int j) {
        double[] temp = values[i];
        values[i] = values[j];
        values[j] = temp;
    }
    
    private static void VectSwap(double[] v, int i, int j) {
        double temp = v[i];
        v[i] = v[j];
        v[j] = temp;
    }
	
    public static  double[] solve(Matrix lhs,double[] rhs) {
     
 
        // Gaussian elimination with partial pivoting
        for (int i = 0; i < lhs.values[0].length; i++) {

            // find pivot row and swap
            int max = i;
            for (int j = i + 1; j < lhs.values[0].length; j++)
                if (FastMath.abs(lhs.values[j][i]) > FastMath.abs(lhs.values[max][i]))
                    max = j;
            lhs.swap(i, max);
            Matrix.VectSwap(rhs,i, max);

            //// singular
            //if (lhs.values[i][i] == 0.0) throw new RuntimeException("Matrix is singular.");

            // pivot within b
            for (int j = i + 1; j < lhs.values[0].length; j++)
                rhs[j] -= rhs[i] * lhs.values[j][i] / lhs.values[i][i];

            // pivot within A
            for (int j = i + 1; j < lhs.values[0].length; j++) {
                double m = lhs.values[j][i] / lhs.values[i][i];
                for (int k = i+1; k < lhs.values[0].length; k++) {
                    lhs.values[j][k] -= lhs.values[i][k] * m;
                }
                lhs.values[j][i] = 0.0;
            }
        }

        // back substitution
        double[] x = new double[lhs.values[0].length];
        for (int j = lhs.values[0].length - 1; j >= 0; j--) {
            double t = 0.0;
            for (int k = j + 1; k < lhs.values[0].length; k++)
                t += lhs.values[j][k] * x[k];
            x[j] = (rhs[j] - t) / lhs.values[j][j];
        }
        return x;
    }
	
	
	public static double[] multiply(Matrix m1, double[] m2)
	{
		
		double[] result = new double[m2.length];
		
		
		for (int x = 0; x < m1.values.length; x++)
		{
			double sum = 0;
			for (int y = 0; y < m1.values.length; y++)
			{
					sum += m1.values[x][y] * m2[y];		
			}
			result[x] = sum;
		}
		
			
		return result;
	}
	
	public static Matrix translate(double x, double y, double z)
	{
		Matrix m = Matrix.identity();
		m.values[0][3] = x;
		m.values[1][3] = y;
		m.values[2][3] = z;
		m.values[3][3] = 1;
		return m;
	}
	private static Matrix rotation(double theta)
	{
		Matrix m = Matrix.identity();
		m.values[0][0] = (double) Math.cos(-theta);
		m.values[0][1] = (double) -Math.sin(-theta);
		m.values[1][0] = (double) Math.sin(-theta);
		m.values[1][1] = (double) Math.cos(-theta);
		return m;
	}
	public static double dotProduct4(double[] v1, double[] v2)
	{
		double sum = 0;
		for (int x = 0; x < v1.length-1; x++)
		{
			sum += v1[x] * v2[x];
		}
		return sum;
	}
	
	public static double dotProduct3(double[] v1, double[] v2)
	{
		double sum = 0;
		for (int x = 0; x < 3; x++)
		{
			sum += v1[x] * v2[x];
		}
		return sum;
	}
	
	public static Matrix rotate(double theta, double xAxis, double yAxis, double zAxis)
	{
		//System.out.println(theta);
		//System.out.println(xAxis);
		//System.out.println(yAxis);
		//System.out.println(zAxis);
		double mag = (double) Math.sqrt(Math.pow(xAxis, 2) + Math.pow(yAxis, 2) + Math.pow(zAxis, 2));
		
		double[] w = new double[4];
		w[0]= xAxis/mag;
		w[1] = yAxis/mag;
		w[2] = zAxis/mag;
		w[3] = 1;
		
		double minAxium = Math.min(FastMath.abs(w[2]), Math.min(FastMath.abs(w[0]), FastMath.abs(w[1])));
		
		
		double[] u = new double[4];
		u[0]= w[0];
		u[1] = w[1];
		u[2] = w[2];
		u[3] = 1;
		
		if (minAxium == FastMath.abs(u[0]))
		{
			u[0] = 1;
		}
		else if (minAxium == FastMath.abs(u[1]))
		{
			u[1] = 1;
		}
		else
		{
			u[2] = 1;
		}
		
		
		double normMag = (double) Math.sqrt(Math.pow(u[0], 2) + Math.pow(u[1], 2) + Math.pow(u[2], 2));
		
		u[0]= u[0]/normMag;
		u[1] = u[1]/normMag;
		u[2] = u[2]/normMag;
		u[3] = 1;
		
		//for (int x = 0; x < w.length; x++)
		//	System.out.println("w : " + w[x]);
		
		//for (int x = 0; x < u.length; x++)
		//	System.out.println("u: " + u[x]);
		
		double[] v = Matrix.crossProduct(w, u);
		//for (int x = 0; x < v.length; x++)
		//	System.out.println("v: " + v[x]);
		
		double vMag = (double) Math.sqrt(Math.pow(v[0], 2) + Math.pow(v[1], 2) + Math.pow(v[2], 2));
		
		//System.out.println("vMag: " + vMag);
		v[0]= v[0]/vMag;
		v[1] = v[1]/vMag;
		v[2] = v[2]/vMag;
		v[3] = 1;
		
		//for (int x = 0; x < v.length; x++)
		//	System.out.println("v: " + v[x]);
	
		double[] m = Matrix.crossProduct(w, v);
		
		//for (int x = 0; x < m.length; x++)
		//	System.out.println("m: " + m[x]);
		
		Matrix mat = Matrix.identity();
		mat.values[0][0] = m[0];
		mat.values[0][1] = m[1];
		mat.values[0][2] = m[2];
		mat.values[0][3] = 0;
		mat.values[1][0] = v[0];
		mat.values[1][1] = v[1];
		mat.values[1][2] = v[2];
		mat.values[1][3] = 0;
		mat.values[2][0] = w[0];
		mat.values[2][1] = w[1];
		mat.values[2][2] = w[2];
		mat.values[2][3] = 0;
		
		Matrix r = Matrix.multiply(Matrix.multiply(transpose(mat),rotation(theta)), mat);
		return r;
	}
	
	public static Matrix scale(double x, double y, double z)
	{
		Matrix m = Matrix.identity();
		m.values[0][0] = x;
		m.values[1][1] = y;
		m.values[2][2] = z;
		m.values[3][3] = 1;
		return m;
	}
	
	public String toString()
	{
		StringBuilder bld = new StringBuilder();
		bld.append("{");
		for (int x = 0; x < values.length; x++)
		{
			bld.append("{");
			for (int y = 0; y < values[0].length-1; y++)
			{
				bld.append(values[x][y] + ",");
			}
			bld.append(values[x][values[0].length-1] + "");
			bld.append("},");
		}
		bld.append("}");
		return bld.toString();
	}
}
