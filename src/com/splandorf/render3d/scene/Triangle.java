package com.splandorf.render3d;

public class Triangle extends Object
{
	Vertex v1;
	Vertex v2;
	Vertex v3;

	Edge e1;
	Edge e2;
	Edge e3;

	float s1;
	float t1;
	float s2;
	float t2;
	float s3;
	float t3;

	int bs1;
	int bt1;
	int bs2;
	int bt2;
	int bs3;
	int bt3;

	Vec3f c;
	Vec3f n;

	Material mat = null;
	Obj obj;

	Triangle next;

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

