package com.splandorf.render3d.scene;

import com.splandorf.render3d.math.*;
import com.splandorf.render3d.shader.*;
import com.splandorf.render3d.*;
import java.util.ArrayList;

public class Obj extends Node
{
	public Material mat;
	public ArrayList<Vertex> vlist;
	public ArrayList<Edge> elist;
	public ArrayList<Triangle> tlist;
	public Obj next;

	public Obj( String name)
    {
		super( name);
		vlist = new ArrayList<Vertex>(50);
		elist = new ArrayList<Edge>(50);
		tlist = new ArrayList<Triangle>(50);
    }

	public Obj()
    {
		super();
		vlist = new ArrayList<Vertex>(50);
		elist = new ArrayList<Edge>(50);
		tlist = new ArrayList<Triangle>(50);
    }

	public Ctm ctm()
	{
		return ctm;
	}

	public Vertex v( int i)
	{
		return (Vertex)vlist.get(i);
	}

	public Edge e( int i)
	{
		return (Edge)elist.get(i);
	}

	public Triangle t( int i)
	{
		return (Triangle)tlist.get(i);
	}

	public void render( Render rend, Mat4f xform, Mat4f nxform, float time)
    {
		Mat4f my_xform   = MemMgr.Mat4f();
		Mat4f my_n_xform = MemMgr.Mat4f();
		
		Alg.mult( ctm().ctm(), xform, my_xform);
		Alg.mult( ctm().normal_ctm(), nxform, my_n_xform);
		
		if (mat.PARTICLE == true) {
			if (mat._lightmodel == Material.FLARE) {
				rend.addToTranspQueue( my_xform, my_n_xform, this);
			} else if (mat._lightmodel == Material.SPRITE) {
				Shader.drawParticle( my_xform, this);
			}
		} else {
			if ( mat.TRIANGLES == true) {
				if (mat._lightmodel == Material.TRANSP ) {
					rend.addToTranspQueue( my_xform, my_n_xform, this);
				} else {
//					System.out.println( "----- Drawing: " + name + " -----");
					rend.drawTriangles( my_xform, tlist);
				}
			}
			if ( mat.WIREFRAME == true) {
				Shader.drawWireframe( my_xform, elist, mat);
			}
			if ( mat.POINTSET == true) {
				Shader.drawPointset( my_xform, vlist, mat);
			}
		}
		
		if (mat._lightmodel != Material.TRANSP && mat.PARTICLE != true) {
			MemMgr.done( my_xform);
			MemMgr.done( my_n_xform);
		}
    }

	public void addVertex( Vertex v)
	{
		vlist.add(v);	
	}

	public void addEdge( Edge e, int v1, int v2)
	{
		try {
			elist.add(e);
			Vertex vrtx1 = (Vertex)vlist.get(v1);
			Vertex vrtx2 = (Vertex)vlist.get(v2);
			e.v1 = vrtx1;
			e.v2 = vrtx2;
			vrtx1.elist.add(e);
			vrtx2.elist.add(e);

		} catch (Exception ex) {
			System.err.println("addEdge: oops. v1: " + v1 + "  v2: " + v2);
		}
	}

	public void addTriangle( Triangle t, int v1, int v2, int v3, int e1, int e2, int e3)
	{
		try {

			tlist.add( t);

			Vertex vrtx1 = (Vertex)vlist.get(v1);
			Vertex vrtx2 = (Vertex)vlist.get(v2);
			Vertex vrtx3 = (Vertex)vlist.get(v3);
			Edge   edge1 = (Edge)  elist.get(e1);
			Edge   edge2 = (Edge)  elist.get(e2);
			Edge   edge3 = (Edge)  elist.get(e3);
			
			t.v1 = vrtx1;
			t.v2 = vrtx2;
			t.v3 = vrtx3;
			t.e1 = edge1;
			t.e2 = edge2;
			t.e3 = edge3;

			vrtx1.tlist.add(t);
			vrtx2.tlist.add(t);
			vrtx3.tlist.add(t);
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

	public void addTriangleNoEdge( Triangle t, int v1, int v2, int v3)
    {
		try {
			tlist.add( t);
			
			Vertex vrtx1 = (Vertex)vlist.get(v1);
			Vertex vrtx2 = (Vertex)vlist.get(v2);
			Vertex vrtx3 = (Vertex)vlist.get(v3);
			
			t.v1 = vrtx1;
			t.v2 = vrtx2;
			t.v3 = vrtx3;
			
			vrtx1.tlist.add(t);
			vrtx2.tlist.add(t);
			vrtx3.tlist.add(t);
			
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
			System.err.println("addTriangle: oops." + e.getMessage());
			e.printStackTrace();
		}
    }

	public void calcVertexNormals()
	{
		Vec3f    vert_n = null;
		Vertex   vert   = null;
		Triangle tri    = null;

		for (int i=0; i<vlist.size(); i++) {

			vert = (Vertex)vlist.get(i);
			vert_n = MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0);
			for (int j=0; j<vert.tlist.size(); j++) {
				tri = (Triangle)vert.tlist.get(j);
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

	/**
	 * Apparently this doesn't do anything yet.
	 */
	public void createFacesFromVertsAndNormals( float [] coords, int [] indices, float [] normals,
						int [] normalIndices,  boolean ccw)
    {
		createFacesFromVerts( coords, indices, ccw);
		
		for (int i=0; i<normalIndices.length; i++) {
		}

    }
    

    /**
     * createFacesFromVerts
	 * Apparently this also doesn't do anything yet.  it looks like plenty of thought
	 * was put into it, but it's not called by code elsewhere.  I wonder exactly when
	 * and why I wrote this originally.
     * <BR>
     * <BL>
     * <LI> coords - list of floating point values representing the X,Y,Z
     *   coordinates of each vertex in this face set.  A list of vertices
     *   is generated from the coords list by breaking it up into triplets.
     * <LI> indices - list of indeces into the vertex set.  Indices are grouped
     *   into sets of three or more.  Sets are separated by the dummy index
     *   "-1".  Each set of 3+ indices represets a polygon face.
     * </BL>
     */
    public void createFacesFromVerts( float [] coords, int [] indices, boolean ccw)
    {
		// Make sure number of coords is divisible by three.
		// We're relying on assumption that each coord has an X, Y, and Z
		// component present in list.

		int num_read = (coords.length / 3) * 3;
		//	System.err.println("Will read this many vert coords: " + num_read);
		if (coords.length % 3 != 0) {
			System.err.println("Obj.createFacesFromVerts(): ");
			System.err.println(" Number of 'coords' passed in should be divisible by 3!");
			System.err.println(" Number of 'coords': " + coords.length );
			//return;
		}
		
		// Add each vertex (as specific by a coordinate triplet in 'coord' list) to this Obj

		int cur_coord = 0;
		while (cur_coord < num_read-2) {
			Vertex new_vert = MemMgr.Vertex( coords[cur_coord], coords[cur_coord+1], coords[cur_coord+2] );
			//  System.err.println( "Adding vertex!: " + coords[cur_coord] + " "  +  coords[cur_coord+1] + " " + coords[cur_coord+2] );
			addVertex( new_vert);
			cur_coord+= 3;
		}
		
		// Make faces by walking down 'indices' list and making triangles out
		// of the vertex indices listed.  Sets of indices belonging to a single
		// face are separated by the index "-1".  Note: faces may be listed as having
		// more than three vertices, but in our internal representation we break up
		// facing having more than three vertices into sub-triangles.  This system
		// currently only supports triangle-based rendering.
		// Also: it is assumed that vertices are listed in clockwise order, for
		// deteriming surface normal direction.
		
		ArrayList<Integer> face_indx = new ArrayList<Integer>(10);
		int cur_indx = 0;
		
		while (cur_indx < indices.length) {

			// Copy next group of indices out of index list (up to but not
			// including the "-1" separator, which is discarded)

			while (indices[cur_indx] != -1) {
			face_indx.add( Integer.valueOf(indices[cur_indx]) );
			cur_indx++;
			}

			// Discard the -1 separator by incrementing past it
			cur_indx++;

			// Build a face out of the contents of face_indx
			addFace( face_indx, ccw);

			// Clear out face list for next group of indices, and repeat.
			face_indx.clear();
		}
    }

    protected void addFace( ArrayList<Integer> face_indx, boolean ccw)
    {
		// If face has more than three vertices, break it into
		// a fan of triangles.  Fan will have its shared base
		// vertex at face_indx[0], and there will be (num_verts-2)
		// triangles in it.

		// Face must have minimum three vertices.  If less, face is invalid; return.
		if (face_indx.size() < 3) {
			System.err.println("Obj.addFace(): invalid face.  Need min. 3 vertices.");
			return;
		}

		// Get base vertex for fan of triangles.
		int base_indx = ((Integer)face_indx.get(0)).intValue();
		int indx1 = 0;
		int indx2 = 0;
		if (base_indx >= this.vlist.size() || base_indx < 0) {
			System.err.println("Obj.addFace(): vertex index greater than # vertices: " + base_indx);
			System.err.println(" Error on first vertex (base of fan).  Entire face lost.");
			return;
		}

		// Loop across remaining vertices in face, building a fan of triangles.
		for (int i=1; i<face_indx.size()-1; i++) {

			// Get next two indices from list
			indx1 = ((Integer)face_indx.get(i)).intValue();
			indx2 = ((Integer)face_indx.get(i+1)).intValue();

			// Make sure indices are in range
			if (indx1 >= this.vlist.size() || indx1 < 0 ) {
			System.err.println("Obj.addFace(): vertex index out of range: " + indx1 );
			System.err.println(" Lost one triangle out of face.");
			continue;
			}
			if (indx2 >= this.vlist.size() || indx2 < 0 ) {
			System.err.println("Obj.addFace(): vertex index out of range: " + indx2 );
			System.err.println(" Lost one triangle out of face.");
			continue;
			}
			
			// If indices are in range, make a triangle out of the base vertex and
			// the vertices at indx1 and indx2, and add it to this Obj.
			Triangle t = MemMgr.Triangle();
			if (ccw == true) {
				addTriangleNoEdge( t,  base_indx, indx2, indx1);
			} else {
				addTriangleNoEdge( t,  base_indx, indx1, indx2);
			}
		}
    }

    /**
     * createEdgesFromVerts
     * <BR>
     * <BL>
     * <LI> coords - list of floating point values representing the X,Y,Z
     *   coordinates of each vertex in this line set.  A list of vertices
     *   is generated from the coords list by breaking it up into triplets.
     * <LI> indices - list of indices into the vertex set.  Indices are grouped
     *   into sets of two or more.  Sets are separated by the dummy index
     *   "-1".  Each set of 2+ indices represets a polyline.
     * </BL>
     */
    public void createEdgesFromVerts( float [] coords, int [] indices)
    {
		// Make sure number of coords is divisible by three.
		// We're relying on assumption that each coord has an X, Y, and Z
		// component present in list.

		int num_read = (coords.length / 3) * 3;
		System.err.println("Will read this many vert coords: " + num_read);
		if (coords.length % 3 != 0) {
			System.err.println("Obj.createFacesFromVerts(): ");
			System.err.println(" Number of 'coords' passed in should be divisible by 3!");
			System.err.println(" Number of 'coords': " + coords.length );
			//return;
		}
		
		// Add each vertex (as specified by a coordinate triplet in 'coord' list) to this Obj

		int cur_coord = 0;
		while (cur_coord < num_read-2) {
			Vertex new_vert = MemMgr.Vertex( coords[cur_coord], coords[cur_coord+1], coords[cur_coord+2] );
			// System.err.println( "Adding vertex!: " + coords[cur_coord] + " "  +  coords[cur_coord+1] + " " + coords[cur_coord+2] );
			addVertex( new_vert);
			cur_coord+= 3;
		}
		
		// Make polylines by walking down 'indices' list and chaining together
		// the vertices listed.  Sets of indices belonging to a single
		// polyline are separated by the index "-1".
		
		ArrayList<Integer> line_indx = new ArrayList<Integer>(10);
		int cur_indx = 0;
		
		while (cur_indx < indices.length) {

			// Copy next group of indices out of index list (up to but not
			// including the "-1" separator, which is discarded)

			while (indices[cur_indx] != -1) {
			line_indx.add( Integer.valueOf(indices[cur_indx]) );
			cur_indx++;
			}

			// Discard the -1 separator by incrementing past it
			cur_indx++;

			// Build a polyline out of the contents of face_indx
			addPolyline( line_indx);

			// Clear out line list for next group of indices, and repeat.
			line_indx.clear();
		}
    }

    protected void addPolyline( ArrayList<Integer> line_indx)
    {
		// Polyline must have minimum two vertices.  If less, polyline is invalid; return.
		if (line_indx.size() < 2) {
			System.err.println("Obj.addPolyline(): invalid polyline.  Need min. 2 vertices.");
			return;
		}

		int indx1 = 0;
		int indx2 = 0;

		// Loop across vertices in list, building a polyline segment by segment
		for (int i=0; i<line_indx.size()-1; i++) {

			// Get next two indices from list
			indx1 = ((Integer)line_indx.get(i)).intValue();
			indx2 = ((Integer)line_indx.get(i+1)).intValue();

			// Make sure indices are in range
			if (indx1 >= this.vlist.size() || indx1 < 0 ) {
				System.err.println("Obj.addPolyline(): vertex index out of range: " + indx1 );
				continue;
			}
			if (indx2 >= this.vlist.size() || indx2 < 0 ) {
				System.err.println("Obj.addPolyline(): vertex index out of range: " + indx2 );
				System.err.println(" Lost one segment out of polyline.");
				continue;
			}
			
			// If indices are in range, make a line segment out of vertex pair
			Edge e = MemMgr.Edge();
			addEdge( e, indx1, indx2);
		}
	}

}
