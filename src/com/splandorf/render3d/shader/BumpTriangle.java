package com.splandorf.render3d.shader;

import com.splandorf.render3d.scene.*;


public class BumpTriangle extends Shader
{

	public void bumpTriangle( Triangle t, Material mat)
    {
	// Order vertices {v1,v2,v3} by increasing Y
	Vertex v1 = t.v1;
	Vertex v2 = t.v2;
	Vertex v3 = t.v3;
	v1.s = (int)(t.s1 * (float)(65536.0));
	v2.s = (int)(t.s2 * (float)(65536.0));
	v3.s = (int)(t.s3 * (float)(65536.0));
	v1.t = (int)(t.t1 * (float)(65536.0));
	v2.t = (int)(t.t2 * (float)(65536.0));
	v3.t = (int)(t.t3 * (float)(65536.0));
	Vertex temp = null;
	if (v1.y > v2.y) {
	    temp = v1;
	    v1 = v2;
	    v2 = temp;
	}
	if (v2.y > v3.y) {
	    temp = v2;
	    v2 = v3;
	    v3 = temp;
	}
	if (v1.y > v2.y) {
	    temp = v1;
	    v1 = v2;
	    v2 = temp;
	}
	
	int color = mat._color;
	
	int lx = v1.x<<16;
	int rx = v1.x<<16;
	int lz = v1.zbuf;
	int rz = v1.zbuf;
	int la = (int)(v1.w_n.x * (float)32768.0 + (float)32768.0);
	int ra = (int)(v1.w_n.x * (float)32768.0 + (float)32768.0);
	int lb = (int)(v1.w_n.y * (float)32768.0 + (float)32768.0);
	int rb = (int)(v1.w_n.y * (float)32768.0 + (float)32768.0);
	int ls = v1.s;
	int rs = v1.s;
	int lt = v1.t;
	int rt = v1.t;
	
	int dy_1_2 = v2.y-v1.y;
	int dy_1_3 = v3.y-v1.y;
	int dy_2_3 = v3.y-v2.y;
	int dx_1_2=0, dx_1_3=0, dx_2_3=0;
		int dz_1_2=0, dz_1_3=0, dz_2_3=0;
		int da_1_2=0, da_1_3=0, da_2_3=0;
		int db_1_2=0, db_1_3=0, db_2_3=0;
		int ds_1_2=0, ds_1_3=0, ds_2_3=0;
		int dt_1_2=0, dt_1_3=0, dt_2_3=0;
		
		if (dy_1_2 != 0) {
		    dx_1_2 = ((v2.x<<16)-(v1.x<<16)) / dy_1_2;
		    dz_1_2 = (v2.zbuf-v1.zbuf) / dy_1_2;
		    da_1_2 = (int)((v2.w_n.x-v1.w_n.x)*(float)32768.0) / dy_1_2;
		    db_1_2 = (int)((v2.w_n.y-v1.w_n.y)*(float)32768.0) / dy_1_2;
		    ds_1_2 = (v2.s-v1.s) / dy_1_2;
		    dt_1_2 = (v2.t-v1.t) / dy_1_2;
		}
		if (dy_1_3 != 0) {
		    dx_1_3 = ((v3.x<<16)-(v1.x<<16)) / dy_1_3;
		    dz_1_3 = (v3.zbuf-v1.zbuf) / dy_1_3;
		    da_1_3 = (int)((v3.w_n.x-v1.w_n.x)*(float)32768.0) / dy_1_3;
		    db_1_3 = (int)((v3.w_n.y-v1.w_n.y)*(float)32768.0) / dy_1_3;
		    ds_1_3 = (v3.s-v1.s) / dy_1_3;
		    dt_1_3 = (v3.t-v1.t) / dy_1_3;
		}
		if (dy_2_3 != 0) {
		    dx_2_3 = ((v3.x<<16)-(v2.x<<16)) / dy_2_3;
		    dz_2_3 = (v3.zbuf-v2.zbuf) / dy_2_3;
		    da_2_3 = (int)((v3.w_n.x-v2.w_n.x)*(float)32768.0) / dy_2_3;
		    db_2_3 = (int)((v3.w_n.y-v2.w_n.y)*(float)32768.0) / dy_2_3;
		    ds_2_3 = (v3.s-v2.s) / dy_2_3;
		    dt_2_3 = (v3.t-v2.t) / dy_2_3;
		}
		
		// Draw top half
		if (dy_1_2 != 0) {
		    
		    for (int i=v1.y; i<v2.y; i++) {
			if (i>0 && i<_height) {
				drawBumpSpan( i, lx>>16, rx>>16, lz, rz, la, ra, lb, rb, ls, rs, lt, rt, color, mat);
			}
			lx += dx_1_3;
			rx += dx_1_2;
			lz += dz_1_3;
			rz += dz_1_2;
			la += da_1_3;
			ra += da_1_2;
			lb += db_1_3;
			rb += db_1_2;
			ls += ds_1_3;
			rs += ds_1_2;
				lt += dt_1_3;
				rt += dt_1_2;
		    }
		}
		
		// Set these by hand just in case top half doesn't exist.
		// (Case where v1 and v2 lie on a horizontal line.)
		// This way, bottom half will *be* the whole, correct
		// triangle in this special case.
		rx = v2.x<<16;
		rz = v2.zbuf;
		ra = (int)(v2.w_n.x*32768.0 + 32768.0);
		rb = (int)(v2.w_n.y*32768.0 + 32768.0);
		rs = v2.s;
		rt = v2.t;
		
		// Draw bottom half
		if (dy_2_3 != 0) {
		    
		    for (int i=v2.y; i<v3.y; i++) {
			if (i>0 && i<_height) {
				drawBumpSpan( i, lx>>16, rx>>16, lz, rz, la, ra, lb, rb, ls, rs, lt, rt, color, mat);
			}
			lx += dx_1_3;
			rx += dx_2_3;
			lz += dz_1_3;
			rz += dz_2_3;
			la += da_1_3;
			ra += da_2_3;
			lb += db_1_3;
			rb += db_2_3;
			ls += ds_1_3;
			rs += ds_2_3;
			lt += dt_1_3;
			rt += dt_2_3;
		    }
		}
    }
    
    public static void drawBumpSpan( 
		int y, int lx, int rx, int lz, int rz,
		int la, int ra, int lb, int rb, int ls, int rs,
		int lt, int rt, int color, Material mat
		)
    {
		// Make sure we're drawing left->right.
		// (Scan conversion algorithm can send
		//  in left and right swapped.)
		int temp;
		if (lx>rx) {
			temp = lx;  lx = rx;  rx = temp;
			temp = lz;  lz = rz;  rz = temp;
			temp = la;  la = ra;  ra = temp;
			temp = lb;  lb = rb;  rb = temp;
			temp = ls;  ls = rs;  rs = temp;
			temp = lt;  lt = rt;  rt = temp;
		}
		
		int z = lz;
		int a = la;
		int b = lb;
		int s = ls;
		int t = lt;
		int dz = 0;
		int da = 0;
		int db = 0;
		int ds = 0;
		int dt = 0;
		int ba, bb;
		int bump;
		if (lx != rx) {
			dz = (rz-lz) / (rx-lx);
			da = (ra-la) / (rx-lx);
			db = (rb-lb) / (rx-lx);
			ds = (rs-ls) / (rx-lx);
			dt = (rt-lt) / (rx-lx);
		}
		
		// Clipping against sides of screen;
		if (rx >= _width) {
			rx = _width-1;
		}
		if (lx < 0) {
			z += -lx * dz;
			a += -lx * da;
			b += -lx * db;
			s += -lx * ds;
			t += -lx * dt;
			lx = 0;
		}
		// Render that puppy
		int pixel = lx + y * _width;
		for (int i=lx; i<rx; i++) {
			if (i>=0 && i<_width) {
				if ( z > _zbuf[ pixel ] ) {
					
					bump = mat._bump_map[ (s>>8) % 256 + ( (t>>8) % 256 ) * 256 ];
					// 1) a,b screen -> a,b spherical via lookup table
					// 2) find ds, dt in bumpmap
					// 3) convert vector s,t in bumpmap space to screen-space vectors along a and b
					// 4) add: a += ds * s(a) + dt * t(a), b += ds * s(b) + dt * t(b)
					// 5) reconvert a,b spherical -> a,b screen via reverse lookup table
					// 6) get color for bumped a,b from environment map
					ba = a + 127 * (((bump>>8)&255)-127);
					bb = b + 127 * ((bump&255)-127);
					if (ba>65535) ba = 65535;
					if (ba<0) ba=0;
					if (bb>65535) bb = 65535;
					if (bb<0) bb=0;
					_pix [ pixel ] = mat._env_map[ (ba>>8) + (255-(bb>>8)) * 256 ];
					_zbuf[ pixel ] = z;
				}
			}
			pixel++;
			z += dz;
			a += da;
			b += db;
			s += ds;
			t += dt;
		}
    }
    
	/** 
	 * 
	 * I don't think any of the code below belongs in this file, but I found it here,
	 * so I'm being paranoid about deleting it.  I think it was mis-copied earlier from
	 * render_new.java when trying to break that out into separate files.
	 * Clues: there is no method called "solidBumpTriangle" in render_new.java, and the
	 * code below seems to have nothing to do with bump mapping, only solid texture mapping.
	 * 
	 * 
// Slow! Z corrects every pixel!
	public static void solidBumpTriangle( Triangle t, Material mat)
	{
		// Order vertices {v1,v2,v3} by increasing Y
		Vertex v1 = t.v1;
		Vertex v2 = t.v2;
		Vertex v3 = t.v3;
		v1.invs = t.s1 * t.v1.invz;
		v2.invs = t.s2 * t.v2.invz;
		v3.invs = t.s3 * t.v3.invz;
		v1.invt = t.t1 * t.v1.invz;
		v2.invt = t.t2 * t.v2.invz;
		v3.invt = t.t3 * t.v3.invz;
		Vertex temp = null;
		if (v1.y > v2.y) {
			temp = v1;
			v1 = v2;
			v2 = temp;
		}
		if (v2.y > v3.y) {
			temp = v2;
			v2 = v3;
			v3 = temp;
		}
		if (v1.y > v2.y) {
			temp = v1;
			v1 = v2;
			v2 = temp;
		}

		int lx = v1.x<<16;
		int rx = v1.x<<16;
		float lz = v1.invz;
		float rz = v1.invz;
		float ls = v1.invs;
		float rs = v1.invs;
		float lt = v1.invt;
		float rt = v1.invt;
		int dy_1_2 = v2.y-v1.y;
		int dy_1_3 = v3.y-v1.y;
		int dy_2_3 = v3.y-v2.y;
		int dx_1_2=0, dx_1_3=0, dx_2_3=0;
		float dz_1_2=0, dz_1_3=0, dz_2_3=0;
		float ds_1_2=0, ds_1_3=0, ds_2_3=0;
		float dt_1_2=0, dt_1_3=0, dt_2_3=0;
		if (dy_1_2 != 0) {
			dx_1_2 = ((v2.x<<16)-(v1.x<<16)) / dy_1_2;
			dz_1_2 = (v2.invz-v1.invz) / (float)dy_1_2;
			ds_1_2 = (v2.invs-v1.invs) / (float)dy_1_2;
			dt_1_2 = (v2.invt-v1.invt) / (float)dy_1_2;
		}
		if (dy_1_3 != 0) {
			dx_1_3 = ((v3.x<<16)-(v1.x<<16)) / dy_1_3;
			dz_1_3 = (v3.invz-v1.invz) / (float)dy_1_3;
			ds_1_3 = (v3.invs-v1.invs) / (float)dy_1_3;
			dt_1_3 = (v3.invt-v1.invt) / (float)dy_1_3;
		}
		if (dy_2_3 != 0) {
			dx_2_3 = ((v3.x<<16)-(v2.x<<16)) / dy_2_3;
			dz_2_3 = (v3.invz-v2.invz) / (float)dy_2_3;
			ds_2_3 = (v3.invs-v2.invs) / (float)dy_2_3;
			dt_2_3 = (v3.invt-v2.invt) / (float)dy_2_3;
		}

		// Draw top half
		if (dy_1_2 != 0) {

			for (int i=v1.y; i<v2.y; i++) {
				if (i>0 && i<_height) {
					if (mat.SPEED == Material.FAST16) {
						fast16solidTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
					} else if (mat.SPEED == Material.SLOW) {
						solidTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
					}
				}
				lx += dx_1_3;
				rx += dx_1_2;
				lz += dz_1_3;
				rz += dz_1_2;
				ls += ds_1_3;
				rs += ds_1_2;
				lt += dt_1_3;
				rt += dt_1_2;

			}
		}

		// Set these by hand just in case top half doesn't exist.
		// (Case where v1 and v2 lie on a horizontal line.)
		// This way, bottom half will *be* the whole, correct
		// triangle in this special case.
		rx = v2.x<<16;
		rz = v2.invz;
		rs = v2.invs;
		rt = v2.invt;

		// Draw bottom half
		if (dy_2_3 != 0) {

			for (int i=v2.y; i<v3.y; i++) {
				if (i>0 && i<_height) {
					if (mat.SPEED == Material.FAST16) {
						fast16solidTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
					} else if (mat.SPEED == Material.SLOW) {
						solidTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
					}
				}
				lx += dx_1_3;
				rx += dx_2_3;
				lz += dz_1_3;
				rz += dz_2_3;
				ls += ds_1_3;
				rs += ds_2_3;
				lt += dt_1_3;
				rt += dt_2_3;
			}
		}
	}

	// Slow!  Z-corrects every pixel!
	public static void solidBumpSpan( int y, int lx, int rx, float lz, float rz, 
									float ls, float rs, float lt, float rt, Material mat)
	{
		// Make sure we're drawing left->right.
		// (Scan conversion algorithm can send 
		//  in left and right swapped.)
		int tempi;
		float tempf;
		if (lx>rx) {
			tempi = lx;
			lx = rx;
			rx = tempi;
			tempf = lz;
			lz = rz;
			rz = tempf;
			tempf = ls;
			ls = rs;
			rs = tempf;
			tempf = lt;
			lt = rt;
			rt = tempf;
		}

		int r, g, b, tcol;
		float z = lz;
		float s = ls;
		float t = lt;
		float dz = (float)0.0;
		float ds = (float)0.0;
		float dt = (float)0.0;
		if (lx != rx) {
			dz = (rz-lz) / (float)(rx-lx);
			ds = (rs-ls) / (float)(rx-lx);
			dt = (rt-lt) / (float)(rx-lx);
		}
		int texel = 0;
		int zint = 0;
		float zinv = (float)0.0;

		// Clip against sides of screen
		if (rx >= _width) rx = _width-1;
		if (lx < 0) {
			z += -lx * dz;
			s += -lx * ds;
			t += -lx * dt;
			lx = 0;
		}
		// Render that puppy
		int pixel = lx + y * _width;
		for (int i=lx; i<rx; i++) {
			if (i>=0 && i<_width) {
				zinv = (float)1.0/z;
				zint = (int)( (zinv/(float)1000.0)*(float)(Integer.MAX_VALUE) );

				if ( zint < _zbuf[ pixel ] ) {
					texel = mat._texture[ ((int)(s*zinv*128.0))%128 + (((int)(t*zinv*128.0))%128)*128 ];
					if (texel!=0) {
						if (mat.TRANSPARENT) {
							tcol = _pix [ pixel ];
							// First, blend surface texture color with background color by ratio *transp*
							r = ( ((texel>>16)&255) * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
							g = ( ((texel>>8 )&255) * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
							b = ( ( texel     &255) * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
							_pix [ pixel ] = (255<<24) + (r<<16) + (g<<8) + b;
						} else {
							_pix [ pixel ] = texel;
						}
						_zbuf[ pixel ] = zint;
					}
				}
			}
			pixel++;
			z += dz;
			s += ds;
			t += dt;
		}
	}
		*/
}


