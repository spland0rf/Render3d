package com.splandorf.render3d.math;

//================================================================
//
// Alg.java
//
// a fully-interactive psychoelectronic journey into your
// philo-sensorium and out the back of your amygdala. 
//
// (c) zbigniew mufosowicz -- 1899
//================================================================

import com.splandorf.render3d.MemMgr;

public class Alg
{
	//Trigonometry
	public static Vec3f temp1 = null;
	public static Vec3f temp2 = null;
	public static Vec3f temp3 = null;
		

	public static float SIN[]    = new float [2048];
	public static float COS[]    = new float [2048];	 
	public static float ARCSIN[] = new float [2048];
	public static float ARCCOS[] = new float [2048];
	public static float ARCTAN[] = new float [2048];
	public static Mat4f IDENT_MAT = MemMgr.Mat4f( (float)1.0, (float)0.0, (float)0.0, (float)0.0,
									   (float)0.0, (float)1.0, (float)0.0, (float)0.0,
									   (float)0.0, (float)0.0, (float)1.0, (float)0.0,
									   (float)0.0, (float)0.0, (float)0.0, (float)1.0);
	final public static float PI=(float)3.14159265;
	final public static float PI2=(float)2.0*PI;
	final public static float RAD_TO_TABLE = (float)2048.0 / PI2;
	final public static float INT_TO_TABLE = RAD_TO_TABLE / (float)65536.0;
	final public static float RAD_TO_DEG = (float)180.0 / PI;
	final public static float DEG_TO_RAD = PI / (float)180.0;

	public static void initTrig()
	{
		for(int i=0;i<2048;i++)
		{
			COS[i]=(float)Math.cos( (float)i / (float)2048.0 * PI2 );
			SIN[i]=(float)Math.sin( (float)i / (float)2048.0 * PI2 );
			ARCSIN[i] = (float)Math.asin( ((float)i/(float)1024.0)-(float)1.0 );
			ARCCOS[i] = (float)Math.acos( ((float)i/(float)1024.0)-(float)1.0 );
			ARCTAN[i] = (float)Math.atan( ((float)i/(float)1024.0)-(float)1.0 );
		}

		temp1 = MemMgr.Vec3f();
		temp2 = MemMgr.Vec3f();
		temp3 = MemMgr.Vec3f();
	}

	public static void print( Mat4f m)
	{
		System.err.println("");
		System.err.println(" |  " + m.m00 + "  " + m.m01 + "  " + m.m02 + "  " + m.m03);
		System.err.println(" |  " + m.m10 + "  " + m.m11 + "  " + m.m12 + "  " + m.m13);
		System.err.println(" |  " + m.m20 + "  " + m.m21 + "  " + m.m22 + "  " + m.m23);
		System.err.println(" |  " + m.m30 + "  " + m.m31 + "  " + m.m32 + "  " + m.m33);

	}

	public static void print( Vec3f v)
	{
		System.err.println("");
		System.err.println(" [  " + v.x + "  " + v.y + "  " + v.z + "  ]");

	}

	public static float crop(float a, float b, float c)
	{
		if (a<b) return (b);
		if (a>c-1) return (c-1);
		return a;
	}
	
	public static int crop(int a, int b, int c)
	{	
		if (a<b) return (b);
		if (a>c-1) return (c-1);
		return a;
	}
	
	public static boolean inrange(int a, int b, int c)
	{
		return((a>=b)&&(a<c));
	}
	
	public static boolean inrange(float a, float b, float c)
	{
		return((a>=b)&&(a<c));
	}
	
	public static void sub(Vec3f a, Vec3f b, Vec3f dest)
	{
		dest.x = a.x-b.x;
		dest.y = a.y-b.y;
		dest.z = a.z-b.z;
	}

	public static void sub(Vec3f a, Vec3f b)
	{
		a.x -= b.x;
		a.y -= b.y;
		a.z -= b.z;
	}

	public static void sub(Vec3i a, Vec3i b, Vec3i dest)
	{
		dest.x = a.x-b.x;
		dest.y = a.y-b.y;
		dest.z = a.z-b.z;
	}

	public static void sub(Vec3i a, Vec3i b)
	{
		a.x -= b.x;
		a.y -= b.y;
		a.z -= b.z;
	}

	public static void sub(VecSf a, VecSf b, VecSf dest)
	{
		dest.a = a.a-b.a;
		dest.b = a.b-b.b;
	}

	public static void sub(VecSf a, VecSf b)
	{
		a.a -= b.a;
		a.b -= b.b;
	}

	public static void sub(VecSi a, VecSi b, VecSi dest)
	{
		dest.a = a.a-b.a;
		dest.b = a.b-b.b;
	}

	public static void sub(VecSi a, VecSi b)
	{
		a.a -= b.a;
		a.b -= b.b;
	}

	public static void add(Vec3f a, Vec3f b, Vec3f dest)
	{
		dest.x = a.x+b.x;
		dest.y = a.y+b.y;
		dest.z = a.z+b.z;
	}

	public static void add(Vec3f a, Vec3f b)
	{
		a.x += b.x;
		a.y += b.y;
		a.z += b.z;
	}

	public static void add(Vec3i a, Vec3i b, Vec3i dest)
	{
		dest.x = a.x+b.x;
		dest.y = a.y+b.y;
		dest.z = a.z+b.z;
	}

	public static void add(Vec3i a, Vec3i b)
	{
		a.x += b.x;
		a.y += b.y;
		a.z += b.z;
	}

	public static void add(VecSf a, VecSf b, VecSf dest)
	{
		dest.a = a.a+b.a;
		dest.b = a.b+b.b;
	}

	public static void add(VecSf a, VecSf b)
	{
		a.a += b.a;
		a.b += b.b;
	}

	public static void add(VecSi a, VecSi b, VecSi dest)
	{
		dest.a = a.a+b.a;
		dest.b = a.b+b.b;
	}

	public static void add(VecSi a, VecSi b)
	{
		a.a += b.a;
		a.b += b.b;
	}

	public static void mult( Vec3f v, float m)
	{
		v.x *= m;
		v.y *= m;
		v.z *= m;
	}

	public static void mult( Vec3f v, float m, Vec3f dest)
	{
		dest.x = v.x * m;
		dest.y = v.y * m;
		dest.z = v.z * m;
	}

	public static float length(Vec3f v)
	{
		return (float)Math.sqrt( v.x * v.x + v.y * v.y + v.z * v.z);
	}
	
	public static void normalize(Vec3f v, Vec3f dest)
	{
		float l = length(v);
		dest.x = v.x/l;
		dest.y = v.y/l;
		dest.z = v.z/l;
	}

	public static void normalize(Vec3f v)
	{
		float l = length(v);
		v.x /= l;
		v.y /= l;
		v.z /= l;
	}

	public static void cross( Vec3f a, Vec3f b, Vec3f dest)
	{		
		dest.x = a.y * b.z - b.y * a.z;
		dest.y = a.z * b.x - b.z * a.x;
		dest.z = a.x * b.y - b.x * a.y;
	}

	public static void cross( Vec3f a, Vec3f b)
	{		
		float x = a.y * b.z - b.y * a.z;
		float y = a.z * b.x - b.z * a.x;
		float z = a.x * b.y - b.x * a.y;
		a.x = x;
		a.y = y;
		a.z = z;
	}

	//============================================
	// Good old fashioned cartesian inner product
	//============================================
	public static float dot( Vec3f a, Vec3f b)
	{
		return (a.x * b.x + a.y * b.y + a.z * b.z);
	}

	public static float dot( Vec3i a, Vec3i b)
	{
		return (float)((a.x * b.x + a.y * b.y + a.z * b.z) / 65536.0);
	}

	//============================================
	// Dot product in spherical coordinates.
	//============================================
	public static float dot( VecSf a, VecSf b)
	{
		int AA = (int)(a.a * RAD_TO_TABLE)%2048;
		int BA = (int)(b.a * RAD_TO_TABLE)%2048;
		int AB_M_BB = (int)((a.b-b.b) * RAD_TO_TABLE)%2048;

		while (AA<0) AA+=2048;
		while (BA<0) BA+=2048;
		while (AB_M_BB<0) AB_M_BB+=2048;

		return ( SIN[AA] * SIN[BA] * COS[AB_M_BB] + COS[AA] * COS[BA] );
	}

	public static float dot( VecSi a, VecSi b)
	{
		int AA = (int)(a.a * INT_TO_TABLE);
		int BA = (int)(b.a * INT_TO_TABLE);
		int AB_M_BB = (int)((a.b-b.b) * INT_TO_TABLE);

		return ( SIN[AA] * SIN[BA] * COS[AB_M_BB] + COS[AA] * COS[BA] );
	}

	//====================================
	// Converts from cartesian coords to
	// spherical coords by means of:
	//
	// beta2 = arcsin(y)
	// w = x/cos(beta2)
	// alpha2 = arccos(w)
	// if (z/cos(beta2) < 0) alpha2 = Pi*2 - alpha2
	//====================================
	public static void cart2sphere( Vec3f cart, VecSf sphere)
	{
		int n = (int)((cart.y+1.0)*1024.0)%2048;
		while (n<0) n+=2048;
		sphere.b      = ARCSIN[ n ];
		n = (int)(sphere.b * RAD_TO_TABLE)%2048;
		while (n<0) n+=2048;
		float cosBeta = COS[ n ];
		n = (int)((cart.x/cosBeta+1.0)*1024.0)%2048;
		while (n<0) n+=2048;
		sphere.a      = ARCCOS[ n ];
		if (cart.z/cosBeta < 0) sphere.a = PI2 - sphere.a;
	}

	public static void cart2sphere( Vec3i cart, VecSi sphere)
	{

	}

	//====================================
	// Converts from spherical coords to
	// cartesian coords by means of:
	//
	// x = r*cos(alpha)*cos(beta)
	// y = r*sin(beta)
	// z = r*sin(alpha)*cos(beta)
	//====================================
	public static void sphere2cart( VecSf sphere, Vec3f cart)
	{
		int n = (int)(sphere.b*RAD_TO_TABLE)%2048;
		while (n<0) n+=2048;
		float cosb = COS[ n ]; 
		cart.y = SIN[ n ];
		n = (int)(sphere.a*RAD_TO_TABLE)%2048;
		while (n<0) n+=2048;
		cart.x = COS[ n ] * cosb;
		cart.z = SIN[ n ] * cosb;
	}

	public static void sphere2cart( VecSi sphere, Vec3i cart)
	{

	}

	public static void normal( Vec3f a, Vec3f b, Vec3f c, Vec3f dest)
	{
		float ax = b.x - a.x;
		float ay = b.y - a.y;
		float az = b.z - a.z;
		float bx = c.x - a.x;
		float by = c.y - a.y;
		float bz = c.z - a.z;

		dest.x = ay*bz - by*az;
		dest.y = az*bx - bz*ax;
		dest.z = ax*by - bx*ay;

		float length = (float)Math.sqrt( dest.x * dest.x + 
								  		 dest.y * dest.y + 
										 dest.z * dest.z
										);
		dest.x /= length;
		dest.y /= length;
		dest.z /= length;
	}

	public static void transpose( Mat4f a, Mat4f b)
	{
		b.m00 = a.m00;
		b.m01 = a.m10;
		b.m02 = a.m20;
		b.m03 = a.m30;
		b.m10 = a.m01;
		b.m11 = a.m11;
		b.m12 = a.m21;
		b.m13 = a.m31;
		b.m20 = a.m02;
		b.m21 = a.m12;
		b.m22 = a.m22;
		b.m23 = a.m32;
		b.m30 = a.m03;
		b.m31 = a.m13;
		b.m32 = a.m23;
		b.m33 = a.m33;
	}

	public static void orient( Mat4f m, Vec3f from, Vec3f at, Vec3f up)
	{
		// Turn "at" and "up" points into the vectors
		// that point from "from" to "at" and "up, 
		// respectively.
		sub( at, from, temp1);
		sub( up, from, temp2);
		
		vec_orient( m, from, temp1, temp2);
	}

	public static void vec_orient( Mat4f m, Vec3f from, Vec3f at, Vec3f up)
	{
		// Encode "from" location into the translation
		// entries in the orient matrix.
		// Also frees up "from" for use as a temp variable.
		set_trans( m, from.x, from.y, from.z);
		
		normalize( at, temp1);
		normalize( up, temp2);

		// Force "up" to be perpendicular to "at"
		mult( temp1, dot(temp1,temp2), temp3);
		sub( temp2, temp3);
		normalize( temp2);

		// "from" = cross product of "at" and "up"
		cross( temp1, temp2, temp3);
		normalize( temp3);

		// x-axis is "from", i.e., temp3
		// y-axis is "up", i.e., temp2
		// -z axis is "at", i.e., temp1
		// x, y, and z become column vectors
		// in the upper-left rotation 3x3 of
		// the orientation matrix.
		m.m00 = temp3.x;
		m.m10 = temp3.y;
		m.m20 = temp3.z;
		m.m01 = temp2.x;
		m.m11 = temp2.y;
		m.m21 = temp2.z;
		m.m02 = -temp1.x;
		m.m12 = -temp1.y;
		m.m22 = -temp1.z;
	}
	
	public static void mult(Mat4f a, Mat4f b, Mat4f dest)
	{	
		dest.m00 = b.m00 * a.m00 + b.m01 * a.m10 + b.m02 * a.m20 + b.m03 * a.m30;
		dest.m01 = b.m00 * a.m01 + b.m01 * a.m11 + b.m02 * a.m21 + b.m03 * a.m31;
		dest.m02 = b.m00 * a.m02 + b.m01 * a.m12 + b.m02 * a.m22 + b.m03 * a.m32;
		dest.m03 = b.m00 * a.m03 + b.m01 * a.m13 + b.m02 * a.m23 + b.m03 * a.m33;
		dest.m10 = b.m10 * a.m00 + b.m11 * a.m10 + b.m12 * a.m20 + b.m13 * a.m30;
		dest.m11 = b.m10 * a.m01 + b.m11 * a.m11 + b.m12 * a.m21 + b.m13 * a.m31;
		dest.m12 = b.m10 * a.m02 + b.m11 * a.m12 + b.m12 * a.m22 + b.m13 * a.m32;
		dest.m13 = b.m10 * a.m03 + b.m11 * a.m13 + b.m12 * a.m23 + b.m13 * a.m33;
		dest.m20 = b.m20 * a.m00 + b.m21 * a.m10 + b.m22 * a.m20 + b.m23 * a.m30;
		dest.m21 = b.m20 * a.m01 + b.m21 * a.m11 + b.m22 * a.m21 + b.m23 * a.m31;
		dest.m22 = b.m20 * a.m02 + b.m21 * a.m12 + b.m22 * a.m22 + b.m23 * a.m32;
		dest.m23 = b.m20 * a.m03 + b.m21 * a.m13 + b.m22 * a.m23 + b.m23 * a.m33;
		dest.m30 = b.m30 * a.m00 + b.m31 * a.m10 + b.m32 * a.m20 + b.m33 * a.m30;
		dest.m31 = b.m30 * a.m01 + b.m31 * a.m11 + b.m32 * a.m21 + b.m33 * a.m31;
		dest.m32 = b.m30 * a.m02 + b.m31 * a.m12 + b.m32 * a.m22 + b.m33 * a.m32;
		dest.m33 = b.m30 * a.m03 + b.m31 * a.m13 + b.m32 * a.m23 + b.m33 * a.m33;
	}

	public static void premult(Mat4f b, Mat4f a, Mat4f dest)
	{	
		dest.m00 = b.m00 * a.m00 + b.m01 * a.m10 + b.m02 * a.m20 + b.m03 * a.m30;
		dest.m01 = b.m00 * a.m01 + b.m01 * a.m11 + b.m02 * a.m21 + b.m03 * a.m31;
		dest.m02 = b.m00 * a.m02 + b.m01 * a.m12 + b.m02 * a.m22 + b.m03 * a.m32;
		dest.m03 = b.m00 * a.m03 + b.m01 * a.m13 + b.m02 * a.m23 + b.m03 * a.m33;
		dest.m10 = b.m10 * a.m00 + b.m11 * a.m10 + b.m12 * a.m20 + b.m13 * a.m30;
		dest.m11 = b.m10 * a.m01 + b.m11 * a.m11 + b.m12 * a.m21 + b.m13 * a.m31;
		dest.m12 = b.m10 * a.m02 + b.m11 * a.m12 + b.m12 * a.m22 + b.m13 * a.m32;
		dest.m13 = b.m10 * a.m03 + b.m11 * a.m13 + b.m12 * a.m23 + b.m13 * a.m33;
		dest.m20 = b.m20 * a.m00 + b.m21 * a.m10 + b.m22 * a.m20 + b.m23 * a.m30;
		dest.m21 = b.m20 * a.m01 + b.m21 * a.m11 + b.m22 * a.m21 + b.m23 * a.m31;
		dest.m22 = b.m20 * a.m02 + b.m21 * a.m12 + b.m22 * a.m22 + b.m23 * a.m32;
		dest.m23 = b.m20 * a.m03 + b.m21 * a.m13 + b.m22 * a.m23 + b.m23 * a.m33;
		dest.m30 = b.m30 * a.m00 + b.m31 * a.m10 + b.m32 * a.m20 + b.m33 * a.m30;
		dest.m31 = b.m30 * a.m01 + b.m31 * a.m11 + b.m32 * a.m21 + b.m33 * a.m31;
		dest.m32 = b.m30 * a.m02 + b.m31 * a.m12 + b.m32 * a.m22 + b.m33 * a.m32;
		dest.m33 = b.m30 * a.m03 + b.m31 * a.m13 + b.m32 * a.m23 + b.m33 * a.m33;
	}

	public static void premult(Mat4f b, Mat4f a)
	{	
		float m00 = b.m00 * a.m00 + b.m01 * a.m10 + b.m02 * a.m20 + b.m03 * a.m30;
		float m01 = b.m00 * a.m01 + b.m01 * a.m11 + b.m02 * a.m21 + b.m03 * a.m31;
		float m02 = b.m00 * a.m02 + b.m01 * a.m12 + b.m02 * a.m22 + b.m03 * a.m32;
		float m03 = b.m00 * a.m03 + b.m01 * a.m13 + b.m02 * a.m23 + b.m03 * a.m33;
		float m10 = b.m10 * a.m00 + b.m11 * a.m10 + b.m12 * a.m20 + b.m13 * a.m30;
		float m11 = b.m10 * a.m01 + b.m11 * a.m11 + b.m12 * a.m21 + b.m13 * a.m31;
		float m12 = b.m10 * a.m02 + b.m11 * a.m12 + b.m12 * a.m22 + b.m13 * a.m32;
		float m13 = b.m10 * a.m03 + b.m11 * a.m13 + b.m12 * a.m23 + b.m13 * a.m33;
		float m20 = b.m20 * a.m00 + b.m21 * a.m10 + b.m22 * a.m20 + b.m23 * a.m30;
		float m21 = b.m20 * a.m01 + b.m21 * a.m11 + b.m22 * a.m21 + b.m23 * a.m31;
		float m22 = b.m20 * a.m02 + b.m21 * a.m12 + b.m22 * a.m22 + b.m23 * a.m32;
		float m23 = b.m20 * a.m03 + b.m21 * a.m13 + b.m22 * a.m23 + b.m23 * a.m33;
		float m30 = b.m30 * a.m00 + b.m31 * a.m10 + b.m32 * a.m20 + b.m33 * a.m30;
		float m31 = b.m30 * a.m01 + b.m31 * a.m11 + b.m32 * a.m21 + b.m33 * a.m31;
		float m32 = b.m30 * a.m02 + b.m31 * a.m12 + b.m32 * a.m22 + b.m33 * a.m32;
		float m33 = b.m30 * a.m03 + b.m31 * a.m13 + b.m32 * a.m23 + b.m33 * a.m33;

		b.m00 = m00;
		b.m01 = m01;
		b.m02 = m02;
		b.m03 = m03;
		b.m10 = m10;
		b.m11 = m11;
		b.m12 = m12;
		b.m13 = m13;
		b.m20 = m20;
		b.m21 = m21;
		b.m22 = m22;
		b.m23 = m23;
		b.m30 = m30;
		b.m31 = m31;
		b.m32 = m32;
		b.m33 = m33;
	}

	public static void mult(Mat4f a, Mat4f b)
	{	
		float m00 = b.m00 * a.m00 + b.m01 * a.m10 + b.m02 * a.m20 + b.m03 * a.m30;
		float m01 = b.m00 * a.m01 + b.m01 * a.m11 + b.m02 * a.m21 + b.m03 * a.m31;
		float m02 = b.m00 * a.m02 + b.m01 * a.m12 + b.m02 * a.m22 + b.m03 * a.m32;
		float m03 = b.m00 * a.m03 + b.m01 * a.m13 + b.m02 * a.m23 + b.m03 * a.m33;
		float m10 = b.m10 * a.m00 + b.m11 * a.m10 + b.m12 * a.m20 + b.m13 * a.m30;
		float m11 = b.m10 * a.m01 + b.m11 * a.m11 + b.m12 * a.m21 + b.m13 * a.m31;
		float m12 = b.m10 * a.m02 + b.m11 * a.m12 + b.m12 * a.m22 + b.m13 * a.m32;
		float m13 = b.m10 * a.m03 + b.m11 * a.m13 + b.m12 * a.m23 + b.m13 * a.m33;
		float m20 = b.m20 * a.m00 + b.m21 * a.m10 + b.m22 * a.m20 + b.m23 * a.m30;
		float m21 = b.m20 * a.m01 + b.m21 * a.m11 + b.m22 * a.m21 + b.m23 * a.m31;
		float m22 = b.m20 * a.m02 + b.m21 * a.m12 + b.m22 * a.m22 + b.m23 * a.m32;
		float m23 = b.m20 * a.m03 + b.m21 * a.m13 + b.m22 * a.m23 + b.m23 * a.m33;
		float m30 = b.m30 * a.m00 + b.m31 * a.m10 + b.m32 * a.m20 + b.m33 * a.m30;
		float m31 = b.m30 * a.m01 + b.m31 * a.m11 + b.m32 * a.m21 + b.m33 * a.m31;
		float m32 = b.m30 * a.m02 + b.m31 * a.m12 + b.m32 * a.m22 + b.m33 * a.m32;
		float m33 = b.m30 * a.m03 + b.m31 * a.m13 + b.m32 * a.m23 + b.m33 * a.m33;

		a.m00 = m00;
		a.m01 = m01;
		a.m02 = m02;
		a.m03 = m03;
		a.m10 = m10;
		a.m11 = m11;
		a.m12 = m12;
		a.m13 = m13;
		a.m20 = m20;
		a.m21 = m21;
		a.m22 = m22;
		a.m23 = m23;
		a.m30 = m30;
		a.m31 = m31;
		a.m32 = m32;
		a.m33 = m33;
	}

	public static void pre_rot_x( Mat4f a, float x)
	{
		float sin = SIN[ ((int)(x*RAD_TO_TABLE)+2048)%2048 ];
		float cos = COS[ ((int)(x*RAD_TO_TABLE)+2048)%2048 ];
		
		float m01 = a.m01 * cos + a.m02 * -sin;
		float m02 = a.m01 * sin + a.m02 * cos;
		float m11 = a.m11 * cos + a.m12 * -sin;
		float m12 = a.m11 * sin + a.m12 * cos;
		float m21 = a.m21 * cos + a.m22 * -sin;
		float m22 = a.m21 * sin + a.m22 * cos;
		float m31 = a.m31 * cos + a.m32 * -sin;
		float m32 = a.m31 * sin + a.m32 * cos;

		a.m01 = m01;
		a.m02 = m02;
		a.m11 = m11;
		a.m12 = m12;
		a.m21 = m21;
		a.m22 = m22;
		a.m31 = m31;
		a.m32 = m32;
	}

	public static void post_rot_x( Mat4f a, float x)
	{
		float sin = SIN[ ((int)(x*RAD_TO_TABLE)+2048)%2048 ];
		float cos = COS[ ((int)(x*RAD_TO_TABLE)+2048)%2048 ];
		
		float m10 = a.m10 *  cos + a.m20 * sin;
		float m11 = a.m11 *  cos + a.m21 * sin;
		float m12 = a.m12 *  cos + a.m22 * sin;
		float m13 = a.m13 *  cos + a.m23 * sin;
		float m20 = a.m10 * -sin + a.m20 * cos;
		float m21 = a.m11 * -sin + a.m21 * cos;
		float m22 = a.m12 * -sin + a.m22 * cos;
		float m23 = a.m13 * -sin + a.m23 * cos;

		a.m10 = m10;
		a.m11 = m11;
		a.m12 = m12;
		a.m13 = m13;
		a.m20 = m20;
		a.m21 = m21;
		a.m22 = m22;
		a.m23 = m23;
	}

	public static void set_rot_x( Mat4f m, float x)
	{
		float sin = SIN[ ((int)(x*RAD_TO_TABLE)+2048)%2048 ];
		float cos = COS[ ((int)(x*RAD_TO_TABLE)+2048)%2048 ];
		m.m00 = (float)1.0;
		m.m01 = (float)0.0;
		m.m02 = (float)0.0;
		m.m03 = (float)0.0;
		m.m10 = (float)0.0;
		m.m11 = cos;
		m.m12 = sin;
		m.m13 = (float)0.0;
		m.m20 = (float)0.0;
		m.m21 = -sin;
		m.m22 = cos;
		m.m23 = (float)0.0;
		m.m30 = (float)0.0;
		m.m31 = (float)0.0;
		m.m32 = (float)0.0;
		m.m33 = (float)1.0;
	}

	public static void pre_rot_y( Mat4f a, float y)
	{
		float sin = SIN[ ((int)(y*RAD_TO_TABLE)+2048)%2048 ];
		float cos = COS[ ((int)(y*RAD_TO_TABLE)+2048)%2048 ];
		
		float m00 = a.m00 * cos + a.m02 * -sin;
		float m02 = a.m00 * sin + a.m02 * cos;
		float m10 = a.m10 * cos + a.m12 * -sin;
		float m12 = a.m10 * sin + a.m12 * cos;
		float m20 = a.m20 * cos + a.m22 * -sin;
		float m22 = a.m20 * sin + a.m22 * cos;
		float m30 = a.m30 * cos + a.m32 * -sin;
		float m32 = a.m30 * sin + a.m32 * cos;

		a.m00 = m00;
		a.m02 = m02;
		a.m10 = m10;
		a.m12 = m12;
		a.m20 = m20;
		a.m22 = m22;
		a.m30 = m30;
		a.m32 = m32;
	}

	public static void post_rot_y( Mat4f a, float y)
	{
		float sin = SIN[ ((int)(y*RAD_TO_TABLE)+2048)%2048 ];
		float cos = COS[ ((int)(y*RAD_TO_TABLE)+2048)%2048 ];
		
		float m00 = a.m00 *  cos + a.m20 * sin;
		float m01 = a.m01 *  cos + a.m21 * sin;
		float m02 = a.m02 *  cos + a.m22 * sin;
		float m03 = a.m03 *  cos + a.m23 * sin;
		float m20 = a.m00 * -sin + a.m20 * cos;
		float m21 = a.m01 * -sin + a.m21 * cos;
		float m22 = a.m02 * -sin + a.m22 * cos;
		float m23 = a.m03 * -sin + a.m23 * cos;

		a.m00 = m00;
		a.m01 = m01;
		a.m02 = m02;
		a.m03 = m03;
		a.m20 = m20;
		a.m21 = m21;
		a.m22 = m22;
		a.m23 = m23;
	}

	public static void set_rot_y( Mat4f m, float y)
	{
		float sin = SIN[ ((int)(y*RAD_TO_TABLE)+2048)%2048 ];
		float cos = COS[ ((int)(y*RAD_TO_TABLE)+2048)%2048 ];
		m.m00 = cos;
		m.m01 = (float)0.0;
		m.m02 = sin;
		m.m03 = (float)0.0;
		m.m10 = (float)0.0;
		m.m11 = (float)1.0;
		m.m12 = (float)0.0;
		m.m13 = (float)0.0;
		m.m20 = -sin;
		m.m21 = (float)0.0;
		m.m22 = cos;
		m.m23 = (float)0.0;
		m.m30 = (float)0.0;
		m.m31 = (float)0.0;
		m.m32 = (float)0.0;
		m.m33 = (float)1.0;
	}

	public static void pre_rot_z( Mat4f a, float z)
	{
		float sin = SIN[ ((int)(z*RAD_TO_TABLE)+2048)%2048 ];
		float cos = COS[ ((int)(z*RAD_TO_TABLE)+2048)%2048 ];
		
		float m00 = a.m00 * cos + a.m01 * -sin;
		float m01 = a.m00 * sin + a.m01 * cos;
		float m10 = a.m10 * cos + a.m11 * -sin;
		float m11 = a.m10 * sin + a.m11 * cos;
		float m20 = a.m20 * cos + a.m21 * -sin;
		float m21 = a.m20 * sin + a.m21 * cos;
		float m30 = a.m30 * cos + a.m31 * -sin;
		float m31 = a.m30 * sin + a.m31 * cos;

		a.m00 = m00;
		a.m01 = m01;
		a.m10 = m10;
		a.m11 = m11;
		a.m20 = m20;
		a.m21 = m21;
		a.m30 = m30;
		a.m31 = m31;
	}

	public static void post_rot_z( Mat4f a, float z)
	{
		float sin = SIN[ ((int)(z*RAD_TO_TABLE)+2048)%2048 ];
		float cos = COS[ ((int)(z*RAD_TO_TABLE)+2048)%2048 ];
		
		float m00 = a.m00 *  cos + a.m10 * sin;
		float m01 = a.m01 *  cos + a.m11 * sin;
		float m02 = a.m02 *  cos + a.m12 * sin;
		float m03 = a.m03 *  cos + a.m13 * sin;
		float m10 = a.m00 * -sin + a.m10 * cos;
		float m11 = a.m01 * -sin + a.m11 * cos;
		float m12 = a.m02 * -sin + a.m12 * cos;
		float m13 = a.m03 * -sin + a.m13 * cos;

		a.m00 = m00;
		a.m01 = m01;
		a.m02 = m02;
		a.m03 = m03;
		a.m10 = m10;
		a.m11 = m11;
		a.m12 = m12;
		a.m13 = m13;
	}

	public static void set_rot_z( Mat4f m, float z)
	{
		float sin = SIN[ ((int)(z*RAD_TO_TABLE)+2048)%2048 ];
		float cos = COS[ ((int)(z*RAD_TO_TABLE)+2048)%2048 ];
		m.m00 = cos;
		m.m01 = sin;
		m.m02 = (float)0.0;
		m.m03 = (float)0.0;
		m.m10 = -sin;
		m.m11 = cos;
		m.m12 = (float)0.0;
		m.m13 = (float)0.0;
		m.m20 = (float)0.0;
		m.m21 = (float)0.0;
		m.m22 = (float)1.0;
		m.m23 = (float)0.0;
		m.m30 = (float)0.0;
		m.m31 = (float)0.0;
		m.m32 = (float)0.0;
		m.m33 = (float)1.0;
	}

	public static void set_rot_axis( Mat4f m, Vec3f axis, float angle)
	{

	}

	public static void post_trans( Mat4f m, float x, float y, float z)
	{
		m.m03 += x;
		m.m13 += y;
		m.m23 += z;
	}

	public static void pre_trans( Mat4f m, float x, float y, float z)
	{
		float m03 = m.m00 * x + m.m01 * y + m.m02 * z;
		float m13 = m.m10 * x + m.m11 * y + m.m12 * z;
		float m23 = m.m20 * x + m.m21 * y + m.m22 * z;

		m.m03 += m03;
		m.m13 += m13;
		m.m23 += m23;
	}

	public static void set_trans( Mat4f m, float x, float y, float z)
	{
		m.m00 = (float)1.0;
		m.m01 = (float)0.0;
		m.m02 = (float)0.0;
		m.m03 = x;
		m.m10 = (float)0.0;
		m.m11 = (float)1.0;
		m.m12 = (float)0.0;
		m.m13 = y;
		m.m20 = (float)0.0;
		m.m21 = (float)0.0;
		m.m22 = (float)1.0;
		m.m23 = z;
		m.m30 = (float)0.0;
		m.m31 = (float)0.0;
		m.m32 = (float)0.0;
		m.m33 = (float)1.0;
	}

	public static void set_scale( Mat4f m, float x, float y, float z)
	{
		m.m00 = x;
		m.m01 = (float)0.0;
		m.m02 = (float)0.0;
		m.m03 = (float)0.0;
		m.m10 = (float)0.0;
		m.m11 = y;
		m.m12 = (float)0.0;
		m.m13 = (float)0.0;
		m.m20 = (float)0.0;
		m.m21 = (float)0.0;
		m.m22 = z;
		m.m23 = (float)0.0;
		m.m30 = (float)0.0;
		m.m31 = (float)0.0;
		m.m32 = (float)0.0;
		m.m33 = (float)1.0;
	}

	public static void pre_scale( Mat4f m, float x, float y, float z)
	{
		m.m00 *= x;
		m.m01 *= y;
		m.m02 *= z;
		m.m10 *= x;
		m.m11 *= y;
		m.m12 *= z;
		m.m20 *= x;
		m.m21 *= y;
		m.m22 *= z;
		m.m30 *= x;
		m.m31 *= y;
		m.m32 *= z;
	}

	public static void post_scale( Mat4f m, float x, float y, float z)
	{
		m.m00 *= x;
		m.m01 *= x;
		m.m02 *= x;
		m.m03 *= x;
		m.m10 *= y;
		m.m11 *= y;
		m.m12 *= y;
		m.m13 *= y;
		m.m20 *= z;
		m.m21 *= z;
		m.m22 *= z;
		m.m23 *= z;
	}

	public static void mult(Mat4f b, Vec3f a, Vec3f dest)
	{
		dest.x = a.x * b.m00 + a.y * b.m01 + a.z * b.m02 + b.m03;
		dest.y = a.x * b.m10 + a.y * b.m11 + a.z * b.m12 + b.m13;
		dest.z = a.x * b.m20 + a.y * b.m21 + a.z * b.m22 + b.m23;
	}

	public static void mult(Mat4f b, Vec3f a)
	{
		float x = a.x * b.m00 + a.y * b.m01 + a.z * b.m02 + b.m03;
		float y = a.x * b.m10 + a.y * b.m11 + a.z * b.m12 + b.m13;
		float z = a.x * b.m20 + a.y * b.m21 + a.z * b.m22 + b.m23;
		a.x = x;
		a.y = y;
		a.z = z;
	}

}

