package com.splandorf.render3d.shader;

public class Shader {
    protected static int _width  = 0;
	protected static int _height = 0;
	protected static int [] _pix = null;
	protected static int [] _zbuf = null;

	public static void initShader( int width, int height, int [] pix, int [] zbuf)
	{
		_width  = width;
		_height = height;
		_pix    = pix;
		_zbuf   = zbuf;
	}
}