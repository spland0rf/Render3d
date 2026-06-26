//================================================================
//
// MemMgr.java
//
// a fully-interactive psychoelectronic journey into your
// philo-sensorium and out the back of your amygdala. 
//
// (c) zbigniew mufosowicz -- 1899
//================================================================

package com.splandorf.render3d;

import com.splandorf.render3d.math.*;
import com.splandorf.render3d.scene.*;

public class MemMgr extends Object
{
	static protected int n_Vec3f;
	static protected int n_Vec3i;
	static protected int n_VecSf;
	static protected int n_VecSi;
	static protected int n_Mat4f;
	static protected int n_Vertex;
	static protected int n_Span;
	static protected int n_Triangle;
	static protected int n_Obj;
	static protected int n_Material;
	static protected int n_Light;
	static protected int n_Edge;

	static protected int BATCHSIZE_Vec3f    = 2048;
	static protected int BATCHSIZE_Vec3i    = 512;
	static protected int BATCHSIZE_VecSf    = 512;
	static protected int BATCHSIZE_VecSi    = 128;
	static protected int BATCHSIZE_Mat4f    = 256;
	static protected int BATCHSIZE_Vertex   = 2048;
	static protected int BATCHSIZE_Span     = 2048;
	static protected int BATCHSIZE_Triangle = 1024;
	static protected int BATCHSIZE_Obj      = 16;
	static protected int BATCHSIZE_Material = 8;
	static protected int BATCHSIZE_Light    = 8;
	static protected int BATCHSIZE_Edge	  = 1024;

	static protected Vec3f    LIST_Vec3f;
	static protected Vec3i    LIST_Vec3i;
	static protected VecSf    LIST_VecSf;
	static protected VecSi    LIST_VecSi;
	static protected Mat4f    LIST_Mat4f;
	static protected Vertex   LIST_Vertex;
	static protected Span     LIST_Span;
	static protected Triangle LIST_Triangle;
	static protected Obj      LIST_Obj;
	static protected Material LIST_Material;
	static protected Light    LIST_Light;
	static protected Edge		LIST_Edge;

	static protected Vec3f    temp_Vec3f;
	static protected Vec3i    temp_Vec3i;
	static protected VecSf    temp_VecSf;
	static protected VecSi    temp_VecSi;
	static protected Mat4f    temp_Mat4f;
	static protected Vertex   temp_Vertex;
	static protected Span     temp_Span;
	static protected Triangle temp_Triangle;
	static protected Obj      temp_Obj;
	static protected Light    temp_Light;
	static protected Material temp_Material;
	static protected Edge		temp_Edge;
	
	static protected Vec3f    temp2_Vec3f;
	static protected Vec3i    temp2_Vec3i;
	static protected VecSf    temp2_VecSf;
	static protected VecSi    temp2_VecSi;
	static protected Mat4f    temp2_Mat4f;
	static protected Vertex   temp2_Vertex;
	static protected Span     temp2_Span;
	static protected Triangle temp2_Triangle;
	static protected Obj      temp2_Obj;
	static protected Light    temp2_Light;
	static protected Material temp2_Material;
	static protected Edge		temp2_Edge;

	public MemMgr()
	{
		// Creating an initial batch of all data types.
		muster_Vec3f();
		muster_Vec3i();
		muster_VecSf();
		muster_VecSi();
		muster_Mat4f();
		muster_Vertex();
		muster_Obj();
		muster_Span();
		muster_Material();
		muster_Light();
		muster_Edge();
	}

	//=================================
	//
	// Muster() methods.  Adds BATCHSIZE
	// number of new objects onto object
	// queues.
	//
	//=================================

	static protected void muster_Vec3f()
	{
		Vec3f temp;
		//System.err.println("New batch of " + BATCHSIZE_Vec3f + " Vec3f");
		n_Vec3f += BATCHSIZE_Vec3f;
		for (int i=0; i<BATCHSIZE_Vec3f; i++) {
			temp = new Vec3f();
			temp.next = LIST_Vec3f;
			LIST_Vec3f = temp;
		}
		//BATCHSIZE_Vec3f *= 2;
	}

	static protected void muster_Vec3i() 
	{
		Vec3i temp;
		n_Vec3i += BATCHSIZE_Vec3i;
		for (int i=0; i<BATCHSIZE_Vec3i; i++) {
			temp = new Vec3i();
			temp.next = LIST_Vec3i;
			LIST_Vec3i = temp;
		}
		BATCHSIZE_Vec3i *= 2;
	}

	static protected void muster_VecSf() 
	{
		VecSf temp;
		n_VecSf += BATCHSIZE_VecSf;
		for (int i=0; i<BATCHSIZE_VecSf; i++) {
			temp = new VecSf();
			temp.next = LIST_VecSf;
			LIST_VecSf = temp;
		}
		//BATCHSIZE_VecSf *= 2;
	}

	static protected void muster_VecSi() 
	{
		VecSi temp;
		n_VecSi += BATCHSIZE_VecSi;
		for (int i=0; i<BATCHSIZE_VecSi; i++) {
			temp = new VecSi();
			temp.next = LIST_VecSi;
			LIST_VecSi = temp;
		}
		BATCHSIZE_VecSi *= 2;
	}

	static protected void muster_Mat4f() 
	{
		Mat4f temp;
		n_Mat4f += BATCHSIZE_Mat4f;
		for (int i=0; i<BATCHSIZE_Mat4f; i++) {
			temp = new Mat4f();
			temp.next = LIST_Mat4f;
			LIST_Mat4f = temp;
		}
		BATCHSIZE_Mat4f *= 2;
	}

	static protected void muster_Vertex() 
	{
		Vertex temp;
		n_Vertex += BATCHSIZE_Vertex;
		for (int i=0; i<BATCHSIZE_Vertex; i++) {
			temp = new Vertex();
			temp.next = LIST_Vertex;
			LIST_Vertex = temp;
		}
		BATCHSIZE_Vertex *= 2;
	}

	static protected void muster_Triangle() 
	{
		Triangle temp;
		n_Triangle += BATCHSIZE_Triangle;
		for (int i=0; i<BATCHSIZE_Triangle; i++) {
			temp = new Triangle();
			temp.next = LIST_Triangle;
			LIST_Triangle = temp;
		}
		BATCHSIZE_Triangle *= 2;
	}

	static protected void muster_Span() 
	{
		Span temp;
		n_Span += BATCHSIZE_Span;
		for (int i=0; i<BATCHSIZE_Span; i++) {
			temp = new Span();
			temp.next = LIST_Span;
			LIST_Span = temp;
		}
		BATCHSIZE_Span *= 2;
	}

	static protected void muster_Edge() 
	{
		Edge temp;
		n_Edge += BATCHSIZE_Edge;
		for (int i=0; i<BATCHSIZE_Edge; i++) {
			temp = new Edge();
			temp.next = LIST_Edge;
			LIST_Edge = temp;
		}
		BATCHSIZE_Edge *= 2;
	}

	static protected void muster_Obj() 
	{
		Obj temp;
		n_Obj += BATCHSIZE_Obj;
		for (int i=0; i<BATCHSIZE_Obj; i++) {
			temp = new Obj();
			temp.next = LIST_Obj;
			LIST_Obj = temp;
		}
		BATCHSIZE_Obj *= 2;
	}

	static protected void muster_Material() 
	{
		Material temp;
		n_Material += BATCHSIZE_Material;
		for (int i=0; i<BATCHSIZE_Material; i++) {
			temp = new Material();
			temp.next = LIST_Material;
			LIST_Material = temp;
		}
		BATCHSIZE_Material *= 2;
	}

	static protected void muster_Light() 
	{
		Light temp;
		n_Light += BATCHSIZE_Light;
		for (int i=0; i<BATCHSIZE_Light; i++) {
			temp = new Light();
			temp.next = LIST_Light;
			LIST_Light = temp;
		}
		BATCHSIZE_Light *= 2;
	}

	//=================================
	//
	// Get() methods.  Pulls the head
	// object off the queue.  If queue
	// empty, musters more objects to 
	// fill it first.
	//
	//=================================

	static protected Vec3f get_Vec3f()
	{
		if (n_Vec3f <= 0) {
			muster_Vec3f();
		}
		temp2_Vec3f = LIST_Vec3f;
		LIST_Vec3f = LIST_Vec3f.next;
		n_Vec3f--;

		return temp2_Vec3f;
	}

	static protected Vec3i get_Vec3i()
	{
		if (n_Vec3i <= 0) {
			muster_Vec3i();
		}
		temp2_Vec3i = LIST_Vec3i;
		LIST_Vec3i = LIST_Vec3i.next;
		n_Vec3i--;

		return temp2_Vec3i;
	}

	static protected VecSf get_VecSf()
	{
		if (n_VecSf <= 0) {
			muster_VecSf();
		}
		temp2_VecSf = LIST_VecSf;
		LIST_VecSf = LIST_VecSf.next;
		n_VecSf--;

		return temp2_VecSf;
	}

	static protected VecSi get_VecSi()
	{
		if (n_VecSi <= 0) {
			muster_VecSi();
		}
		temp2_VecSi = LIST_VecSi;
		LIST_VecSi = LIST_VecSi.next;
		n_VecSi--;

		return temp2_VecSi;
	}

	static public Span Span()
	{
		if (n_Span <= 0) {
			muster_Span();
		}
		temp2_Span = LIST_Span;
		LIST_Span = LIST_Span.next;
		n_Span--;

		return temp2_Span;
	}

	static public Edge Edge()
	{
		if (n_Edge <= 0) {
			muster_Edge();
		}
		temp2_Edge = LIST_Edge;
		LIST_Edge = LIST_Edge.next;
		n_Edge--;

		return temp2_Edge;
	}

	static public Triangle Triangle()
	{
		if (n_Triangle <= 0) {
			muster_Triangle();
		}
		temp2_Triangle = LIST_Triangle;
		LIST_Triangle = LIST_Triangle.next;
		n_Triangle--;

		return temp2_Triangle;
	}

	static public Vertex Vertex()
	{
		if (n_Vertex <= 0) {
			muster_Vertex();
		}
		temp2_Vertex = LIST_Vertex;
		LIST_Vertex = LIST_Vertex.next;
		n_Vertex--;

		return temp2_Vertex;
	}

	static public Vertex Vertex( float x, float y, float z)
	{
		if (n_Vertex <= 0) {
			muster_Vertex();
		}
		temp2_Vertex = LIST_Vertex;
		LIST_Vertex = LIST_Vertex.next;
		n_Vertex--;

		temp2_Vertex.p = Vec3f( x, y, z);

		return temp2_Vertex;
	}

	static protected Mat4f get_Mat4f()
	{
		if (n_Mat4f <= 0) {
			muster_Mat4f();
		}
		temp2_Mat4f = LIST_Mat4f;
		LIST_Mat4f = LIST_Mat4f.next;
		n_Mat4f--;

		return temp2_Mat4f;
	}

	static public Obj Obj()
	{
		if (n_Obj <= 0) {
			muster_Obj();
		}
		temp2_Obj = LIST_Obj;
		LIST_Obj = LIST_Obj.next;
		n_Obj--;

		return temp2_Obj;
	}

	static public Material Material()
	{
		if (n_Material <= 0) {
			muster_Material();
		}
		temp2_Material = LIST_Material;
		LIST_Material = LIST_Material.next;
		n_Material--;

		return temp2_Material;
	}

	static public Light Light()
	{
		if (n_Light <= 0) {
			muster_Light();
		}
		temp2_Light = LIST_Light;
		LIST_Light = LIST_Light.next;
		n_Light--;

		return temp2_Light;
	}

	//=================================
	//
	// done().  recycles data types.
	//
	//=================================

	static public void done( Vec3f data)
	{
		data.next = LIST_Vec3f;
		LIST_Vec3f = data;
		n_Vec3f++;
	}

	static public void done( Vec3i data)
	{
		data.next = LIST_Vec3i;
		LIST_Vec3i = data;
		n_Vec3i++;
	}

	static public void done( VecSf data)
	{
		data.next = LIST_VecSf;
		LIST_VecSf = data;
		n_VecSf++;
	}

	static public void done( VecSi data)
	{
		data.next = LIST_VecSi;
		LIST_VecSi = data;
		n_VecSi++;
	}

	static public void done( Mat4f data)
	{
		data.next = LIST_Mat4f;
		LIST_Mat4f = data;
		n_Mat4f++;
	}

	static public void done( Span data)
	{
		data.next = LIST_Span;
		LIST_Span = data;
		n_Span++;
	}

	static public void done( Triangle data)
	{
		data.next = LIST_Triangle;
		LIST_Triangle = data;
		n_Triangle++;
	}

	static public void done( Obj data)
	{
		data.next = LIST_Obj;
		LIST_Obj = data;
		n_Obj++;
	}

	static public void done( Material data)
	{
		data.next = LIST_Material;
		LIST_Material = data;
		n_Material++;
	}

	static public void done( Light data)
	{
		data.next = LIST_Light;
		LIST_Light = data;
		n_Light++;
	}

	static public void done( Vertex data)
	{
		data.next = LIST_Vertex;
		LIST_Vertex = data;
		n_Vertex++;
	}

	//=================================
	//
	// Public accessor methods.
	// Each returns a fresh object
	// off the queue.  Some take 
	// initialization params.
	//
	//=================================

	static public Vec3f Vec3f()
	{
		if (n_Vec3f <= 0) {
			muster_Vec3f();
		}
		temp2_Vec3f = LIST_Vec3f;
		LIST_Vec3f = LIST_Vec3f.next;
		n_Vec3f--;

		return temp2_Vec3f;
	}

	static public Vec3f Vec3f( float a, float b, float c)
	{
		temp_Vec3f = Vec3f();
		temp_Vec3f.x = a;
		temp_Vec3f.y = b;
		temp_Vec3f.z = c;

		return temp_Vec3f;
	}

	static public Vec3i Vec3i()
	{
		return get_Vec3i();
	}

	static public Vec3i Vec3i( int a, int b, int c)
	{
		temp_Vec3i = get_Vec3i();
		temp_Vec3i.x = a;
		temp_Vec3i.y = b;
		temp_Vec3i.z = c;

		return temp_Vec3i;
	}

	static public VecSf VecSf()
	{
		if (n_VecSf <= 0) {
			muster_VecSf();
		}
		temp2_VecSf = LIST_VecSf;
		LIST_VecSf = LIST_VecSf.next;
		n_VecSf--;

		return temp2_VecSf;
	}

	static public VecSf VecSf( float a, float b)
	{
		temp_VecSf = get_VecSf();
		temp_VecSf.a = a;
		temp_VecSf.b = b;

		return temp_VecSf;
	}

	static public VecSi VecSi()
	{
		return get_VecSi();
	}

	static public VecSi VecSi( int a, int b, int c)
	{
		temp_VecSi = get_VecSi();
		temp_VecSi.a = a;
		temp_VecSi.b = b;

		return temp_VecSi;
	}

	static public Mat4f Mat4f()
	{
		temp_Mat4f = get_Mat4f();
		temp_Mat4f.m00 = (float)1.0;
		temp_Mat4f.m11 = (float)1.0;
		temp_Mat4f.m22 = (float)1.0;
		temp_Mat4f.m33 = (float)1.0;
		return temp_Mat4f;
	}

	static public Mat4f Mat4f(
		float m00, float m01, float m02, float m03,
		float m10, float m11, float m12, float m13,
		float m20, float m21, float m22, float m23,
		float m30, float m31, float m32, float m33
		)
	{
		temp_Mat4f = get_Mat4f();

		temp_Mat4f.m00 = m00;
		temp_Mat4f.m01 = m01;
		temp_Mat4f.m02 = m02;
		temp_Mat4f.m03 = m03;

		temp_Mat4f.m10 = m10;
		temp_Mat4f.m11 = m11;
		temp_Mat4f.m12 = m12;
		temp_Mat4f.m13 = m13;

		temp_Mat4f.m20 = m20;
		temp_Mat4f.m21 = m21;
		temp_Mat4f.m22 = m22;
		temp_Mat4f.m23 = m23;

		temp_Mat4f.m30 = m30;
		temp_Mat4f.m31 = m31;
		temp_Mat4f.m32 = m32;
		temp_Mat4f.m33 = m33;

		return temp_Mat4f;
	}

	static public Mat4f Mat4f( Mat4f other)
	{
		temp_Mat4f = get_Mat4f();

		temp_Mat4f.m00 = other.m00;
		temp_Mat4f.m01 = other.m01;
		temp_Mat4f.m02 = other.m02;
		temp_Mat4f.m03 = other.m03;

		temp_Mat4f.m10 = other.m10;
		temp_Mat4f.m11 = other.m11;
		temp_Mat4f.m12 = other.m12;
		temp_Mat4f.m13 = other.m13;

		temp_Mat4f.m20 = other.m20;
		temp_Mat4f.m21 = other.m21;
		temp_Mat4f.m22 = other.m22;
		temp_Mat4f.m23 = other.m23;

		temp_Mat4f.m30 = other.m30;
		temp_Mat4f.m31 = other.m31;
		temp_Mat4f.m32 = other.m32;
		temp_Mat4f.m33 = other.m33;

		return temp_Mat4f;
	}

	static public Mat4f Mat4f_rot( float x, float y, float z)
	{
		temp_Mat4f = get_Mat4f();
		if (x != 0) {
			Alg.set_rot_x( temp_Mat4f, x);
		}
		if (y != 0) {
			Alg.set_rot_y( temp_Mat4f, y);
		}
		if (z != 0) {
			Alg.set_rot_z( temp_Mat4f, z);
		}
		return temp_Mat4f;
	}

	static public Mat4f Mat4f_trans( float x, float y, float z)
	{
		temp_Mat4f = get_Mat4f();
		Alg.set_trans( temp_Mat4f, x, y, z);
		return temp_Mat4f;
	}

	static public Mat4f Mat4f_scale( float x, float y, float z)
	{
		temp_Mat4f = get_Mat4f();
		Alg.set_scale( temp_Mat4f, x, y, z);
		return temp_Mat4f;		
	}


}

