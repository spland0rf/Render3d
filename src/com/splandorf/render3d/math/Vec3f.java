package com.splandorf.render3d.math;

public class Vec3f {
	public float x;
	public float y;
	public float z;
	public float w;

	public Vec3f( float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Vec3f()
	{
		this.x = (float)0.0;
		this.y = (float)0.0;
		this.z = (float)0.0;
	}

	public Vec3f next;
}
