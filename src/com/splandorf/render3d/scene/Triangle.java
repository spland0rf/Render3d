package com.splandorf.render3d.scene;

import com.splandorf.render3d.math.*;

public class Triangle extends Object
{
	public Vertex v1;
	public Vertex v2;
	public Vertex v3;

	public Edge e1;
	public Edge e2;
	public Edge e3;

	public float s1;
	public float t1;
	public float s2;
	public float t2;
	public float s3;
	public float t3;

	public int bs1;
	public int bt1;
	public int bs2;
	public int bt2;
	public int bs3;
	public int bt3;

	public Vec3f c;
	public Vec3f n;

	public Material mat = null;
	public Obj obj;

	public Triangle next;

	public void setTexture( float s1, float t1, float s2, float t2, float s3, float t3)
	{
		this.s1 = s1;
		this.t1 = t1;
		this.s2 = s2;
		this.t2 = t2;
		this.s3 = s3;
		this.t3 = t3;
	}

	public void setBump( float s1, float t1, float s2, float t2, float s3, float t3)
	{
		this.bs1 = (int)(s1 * (float)128.0);
		this.bt1 = (int)(t1 * (float)128.0);
		this.bs2 = (int)(s2 * (float)128.0);
		this.bt2 = (int)(t2 * (float)128.0);
		this.bs3 = (int)(s3 * (float)128.0);
		this.bt3 = (int)(t3 * (float)128.0);
	}


}

