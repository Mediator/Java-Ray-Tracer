package engine.util;


public class ImageWrapper implements ImageRasterizer {
	public int[][] pixels;

	@Override
	public void setPixel(int x, int y, int[] rgb) {
		pixels[x][y] = (((rgb[0])&0x0ff)<<16)|(((rgb[1])&0x0ff)<<8)|((rgb[2])&0x0ff); 
	}

	@Override
	public void setPixel(int x, int y, int r, int g, int b) {
		pixels[x][y] = (((r)&0x0ff)<<16)|(((g)&0x0ff)<<8)|((b)&0x0ff); 
		
	}
	
}
