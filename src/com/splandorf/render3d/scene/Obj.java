package com.splandorf.render3d;

class Obj extends Object
{
	public Ctm ctm;
	public Material mat;

	public Vector  vlist;
	public Vector  elist;
	public Vector  tlist;

	public Obj()
	{
		vlist = new Vector(50);
		elist = new Vector(50);
		tlist = new Vector(50);
		ctm = new Ctm();
	}

	public Ctm ctm()
	{
		return ctm;
	}

	public Vertex v( int i)
	{
		return (Vertex)vlist.elementAt(i);
	}

	public Edge e( int i)
	{
		return (Edge)elist.elementAt(i);
	}

	public Triangle t( int i)
	{
		return (Triangle)tlist.elementAt(i);
	}

	public void addVertex( Vertex v)
	{
		vlist.addElement(v);	
	}

	public void addEdge( Edge e, int v1, int v2)
	{
		try {
			elist.addElement(e);
			Vertex vrtx1 = (Vertex)vlist.elementAt(v1);
			Vertex vrtx2 = (Vertex)vlist.elementAt(v2);
			e.v1 = vrtx1;
			e.v2 = vrtx2;
			vrtx1.elist.addElement(e);
			vrtx2.elist.addElement(e);

		} catch (Exception ex) {
			System.err.println("addEdge: oops. v1: " + v1 + "  v2: " + v2);
		}
	}

	public void addTriangle( Triangle t, int v1, int v2, int v3, int e1, int e2, int e3)
	{
		try {

			tlist.addElement( t);

			Vertex vrtx1 = (Vertex)vlist.elementAt(v1);
			Vertex vrtx2 = (Vertex)vlist.elementAt(v2);
			Vertex vrtx3 = (Vertex)vlist.elementAt(v3);
			Edge   edge1 = (Edge)  elist.elementAt(e1);
			Edge   edge2 = (Edge)  elist.elementAt(e2);
			Edge   edge3 = (Edge)  elist.elementAt(e3);
			
			t.v1 = vrtx1;
			t.v2 = vrtx2;
			t.v3 = vrtx3;
			t.e1 = edge1;
			t.e2 = edge2;
			t.e3 = edge3;

			vrtx1.tlist.addElement(t);
			vrtx2.tlist.addElement(t);
			vrtx3.tlist.addElement(t);
			if (edge1.t1 == null) {
				edge1.t1 = t;
			} else {
				edge1.t2 = t;
			}
			if (edge2.t1 == null) {
				edge2.t1 = t;
			} else {
				edge2.t2 = t;
			}
			if (edge3.t1 == null) {
				edge3.t1 = t;
			} else {
				edge3.t2 = t;
			}

			// Calculate normal.
			Vec3f one    = MemMgr.Vec3f();
			Vec3f two    = MemMgr.Vec3f();
			Vec3f normal = MemMgr.Vec3f();
			Alg.sub( vrtx2.p, vrtx1.p, one);
			Alg.sub( vrtx3.p, vrtx2.p, two);
			Alg.normalize( one);
			Alg.normalize( two);
			Alg.cross( one, two, normal);
			Alg.normalize( normal);
			t.n = normal;
			MemMgr.done( one);
			MemMgr.done( two);

			// Calculate centroid
			t.c = MemMgr.Vec3f();
			t.c.x = (t.v1.p.x + t.v2.p.x + t.v3.p.x) / (float)3.0;
			t.c.y = (t.v1.p.y + t.v2.p.y + t.v3.p.y) / (float)3.0;
			t.c.z = (t.v1.p.z + t.v2.p.z + t.v3.p.z) / (float)3.0;

			t.obj = this;

		} catch (Exception e) {
			System.err.println("addTriangle: oops.");
		}
	}

	public void calcVertexNormals()
	{
		Vec3f    vert_n = null;
		Vertex   vert   = null;
		Triangle tri    = null;

		for (int i=0; i<vlist.size(); i++) {

			vert = (Vertex)vlist.elementAt(i);
			vert_n = MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0);
			for (int j=0; j<vert.tlist.size(); j++) {
				tri = (Triangle)vert.tlist.elementAt(j);
				vert_n.x += tri.n.x;
				vert_n.y += tri.n.y;
				vert_n.z += tri.n.z;
			}
			vert_n.x /= (float)vert.tlist.size();
			vert_n.y /= (float)vert.tlist.size();
			vert_n.z /= (float)vert.tlist.size();
			Alg.normalize(vert_n);
			vert.n = vert_n;
			vert.w_n = MemMgr.Vec3f();
		}
	}

	Obj next;
}
