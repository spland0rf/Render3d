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

    int xfrac;
    int yfrac;

    int zbuf; // Z-buffer value.  Equals (10000.0/z), bounded to range [1, MAX24BIT]
    // We use a multiple of 1/z in z-buffer because 1/z interpolates
    // linearly in screen space (as we walk pixels).  (10000.0/z) should
    // be accurate roughly in the z range [0.001, 100], and some errors
    // will appear in distant objects (100<z<1000).  Everything beyond
    // z=1000 will be rife with error.  But that's okay.
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

