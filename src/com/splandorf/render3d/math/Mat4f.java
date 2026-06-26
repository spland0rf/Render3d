package com.splandorf.render3d.math;

public class Mat4f {
	public float m00;
	public float m01;
	public float m02;
	public float m03;
	public float m10;
	public float m11;
	public float m12;
	public float m13;
	public float m20;
	public float m21;
	public float m22;
	public float m23;
	public float m30;
	public float m31;
	public float m32;
	public float m33;

	public Mat4f()
	{
		m00 = (float)1.0;
		m11 = (float)1.0;
		m22 = (float)1.0;
		m33 = (float)1.0;
		m01 = (float)0.0;
		m02 = (float)0.0;
		m03 = (float)0.0;
		m10 = (float)0.0;
		m12 = (float)0.0;
		m13 = (float)0.0;
		m20 = (float)0.0;
		m21 = (float)0.0;
		m23 = (float)0.0;
		m30 = (float)0.0;
		m31 = (float)0.0;
		m32 = (float)0.0;
	}

	public Mat4f next;
}
