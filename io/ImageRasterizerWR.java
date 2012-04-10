package io;

import java.awt.image.WritableRaster;

import engine.util.ImageRasterizer;

public class ImageRasterizerWR implements ImageRasterizer {
	WritableRaster wr;
	public ImageRasterizerWR(WritableRaster wr) {
		this.wr = wr;
	}
	public void setPixel(int x, int y, int[] rgb)
	{
		wr.setPixel(x, y, rgb);
	}
	public void setPixel(int x, int y, int r, int g, int b)
	{
		wr.setPixel(x, y, new int[] {r,g,b});
	}
}
