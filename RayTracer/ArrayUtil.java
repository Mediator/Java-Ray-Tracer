package RayTracer;

import java.util.ArrayList;

public class ArrayUtil {
	public static double[][] increaseVerts(double[][] verts)
	{
		double retVerts[][] = new double[verts.length][4];
		for (int x = 0; x < verts.length; x++)
		{
			retVerts[x] = verts[x];
		}
		return retVerts;
	}
	public static int[] redimArray(int[] original, int size)
	{
		int[] retArray = new int[size];
		if (size <= original.length)
		{
			for (int x = 0; x < size; x++)
			{
				retArray[x] = original[x];
			}
		}
		else
		{
			for (int x = 0; x < original.length; x++)
			{
				retArray[x] = original[x];
			}
		}
		return retArray;
	}
	public static String[] redimArray(String[] original, int size)
	{
		String[] retArray = new String[size];
		if (size <= original.length)
		{
			for (int x = 0; x < size; x++)
			{
				retArray[x] = original[x];
			}
		}
		else
		{
			for (int x = 0; x < original.length; x++)
			{
				retArray[x] = original[x];
			}
		}
		return retArray;
	}
	public static ModelGroup[] redimArray(ModelGroup[] original, int size)
	{
		ModelGroup[] retArray = new ModelGroup[size];
		if (size <= original.length)
		{
			for (int x = 0; x < size; x++)
			{
				retArray[x] = original[x];
			}
		}
		else
		{
			for (int x = 0; x < original.length; x++)
			{
				retArray[x] = original[x];
			}
		}
		return retArray;
	}
	public static Camera[] redimArray(Camera[] original, int size)
	{
		Camera[] retArray = new Camera[size];
		if (size <= original.length)
		{
			for (int x = 0; x < size; x++)
			{
				retArray[x] = original[x];
			}
		}
		else
		{
			for (int x = 0; x < original.length; x++)
			{
				retArray[x] = original[x];
			}
		}
		return retArray;
	}

	public static Polygon[] redimArray(Polygon[] original, int size)
	{
		Polygon[] retArray = new Polygon[size];
		if (size <= original.length)
		{
			for (int x = 0; x < size; x++)
			{
				retArray[x] = original[x];
			}
		}
		else
		{
			for (int x = 0; x < original.length; x++)
			{
				retArray[x] = original[x];
			}
		}
		return retArray;
	}
	public static double[] redimArray(double[] original, int size)
	{
		double[] retArray = new double[size];
		if (size <= original.length)
		{
			for (int x = 0; x < size; x++)
			{
				retArray[x] = original[x];
			}
		}
		else
		{
			for (int x = 0; x < original.length; x++)
			{
				retArray[x] = original[x];
			}
		}
		return retArray;
	}
	public static double[][] redimVerts(double[][] original, int size)
	{
		double[][] retArray = new double[size][4];
		if (size <= original.length)
		{
			for (int x = 0; x < size; x++)
			{
				retArray[x] = original[x];
			}
		}
		else
		{
			for (int x = 0; x < original.length; x++)
			{
				retArray[x] = original[x];
			}
		}
		return retArray;
	}
	// Usage of http://www.vogella.de/articles/JavaAlgorithmsQuicksort/article.html
	public static void quicksort(int low, int high, ArrayList<ScanLineBound> arr) {
		int i = low, j = high;
		double pivot = arr.get(low + (high-low)/2).y;


		while (i <= j) {
			while (arr.get(i).y < pivot) {
				i++;
			}

			while (arr.get(j).y > pivot) {
				j--;
			}

			if (i <= j) {

				ScanLineBound temp = arr.get(i);
				arr.set(i,arr.get(j));
				arr.set(j,temp);
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j)
			quicksort(low, j, arr);
		if (i < high)
			quicksort(i, high, arr);
	}
	
	public static void quicksort(int low, int high, ArrayList<ScanLineBound> arr, boolean x) {
		int i = low, j = high;
		double pivot = arr.get(low + (high-low)/2).x;


		while (i <= j) {
			while (arr.get(i).x < pivot) {
				i++;
			}

			while (arr.get(j).x > pivot) {
				j--;
			}

			if (i <= j) {

				ScanLineBound temp = arr.get(i);
				arr.set(i,arr.get(j));
				arr.set(j,temp);
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j)
			quicksort(low, j, arr);
		if (i < high)
			quicksort(i, high, arr);
	}
	
	
	
	public static void quicksort(int low, int high, double[][] arr) {
		int i = low, j = high;
		double pivot = arr[low + (high-low)/2][0];


		while (i <= j) {
			while (arr[i][0] < pivot) {
				i++;
			}

			while (arr[j][0] > pivot) {
				j--;
			}

			if (i <= j) {

				double[] temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j)
			quicksort(low, j, arr);
		if (i < high)
			quicksort(i, high, arr);
	}
	
	public static void quicksort(int low, int high, int[][] lines, double[] curX, double[] slopes) {
		int i = low, j = high;
		int pivot = lines[low + (high-low)/2][1];


		while (i <= j) {
			while (lines[i][1] < pivot) {
				i++;
			}

			while (lines[j][1] > pivot) {
				j--;
			}

			if (i <= j) {

				double tempSlope = slopes[i];
				double tempCurX = curX[i];
				int[] temp = lines[i];
				slopes[i] = slopes[j];
				slopes[j] = tempSlope;
				curX[i] = curX[j];
				curX[j] = tempCurX;
				lines[i] = lines[j];
				lines[j] = temp;
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j)
			quicksort(low, j, lines, curX, slopes);
		if (i < high)
			quicksort(i, high, lines, curX, slopes);
	}


}
