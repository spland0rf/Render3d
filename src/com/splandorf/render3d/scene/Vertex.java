package com.splandorf.render3d.scene;

import com.splandorf.render3d.MemMgr;
import com.splandorf.render3d.math.Vec3f;
import java.util.ArrayList;

public class Vertex
{
    public Vec3f p;
    public Vec3f n;
    public Vec3f w_n;
    
    public ArrayList<Edge> elist;
    public ArrayList<Triangle> tlist;
    
    public int x;
    public int y;
    public int z;

    public int xfrac;
    public int yfrac;

    public int zbuf; // Z-buffer value.  Equals (10000.0/z), bounded to range [1, MAX24BIT]
    // We use a multiple of 1/z in z-buffer because 1/z interpolates
    // linearly in screen space (as we walk pixels).  (10000.0/z) should
    // be accurate roughly in the z range [0.001, 100], and some errors
    // will appear in distant objects (100<z<1000).  Everything beyond
    // z=1000 will be rife with error.  But that's okay.  We don't expect
    // to have scenes that go out past 1000 z.
    public float invz;
    public float invs;
    public float invt;

    public int s;
    public int t;
    
    public int r;
    public int g;
    public int b;
    
    public Vertex next;
    
    public Vertex()
    {
      elist = new ArrayList<Edge>(10);
      tlist = new ArrayList<Triangle>(10);
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

