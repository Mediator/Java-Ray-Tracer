package engine.util;

public interface ImageRasterizer {
	public void setPixel(int x, int y, int[] rgb);
	public void setPixel(int x, int y, int r, int g, int b);
}
