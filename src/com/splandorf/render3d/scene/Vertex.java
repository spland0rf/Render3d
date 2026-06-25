package com.splandorf.render3d;

public class Vertex	extends Object
{
	Vec3f p;
	Vec3f n;
	Vec3f w_n;

	Vector elist;
	Vector tlist;

	int x;
	int y;
	int z;

	float invz;
	float invs;
	float invt;

	int s;
	int t;

	int r;
	int g;
	int b;

	Vertex next;

	public Vertex()
	{
		elist = new Vector(10);
		tlist = new Vector(10);
	}

	public Vertex( float x, float y, float z)
	{
		this();
		p = MemMgr.Vec3f();
		p.x = x;
		p.y = y;
		p.z = z;
	}
}

