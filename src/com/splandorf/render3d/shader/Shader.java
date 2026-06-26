package com.splandorf.render3d.shader;

public class Shader extends Object {
    protected static int _width  = 0;
	protected static int _height = 0;
	protected static int [] _pix = null;
	protected static float [] _zbuf = null;

	public static void initShader( int width, int height, int [] pix, float [] zbuf)
	{
		_width  = width;
		_height = height;
		_pix    = pix;
		_zbuf   = zbuf;
	}
}