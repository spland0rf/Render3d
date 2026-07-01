package com.splandorf.render3d.shader;

import com.splandorf.render3d.Render;
import com.splandorf.render3d.scene.*;

public class GouraudTriangle extends Shader 
{
	public static void drawGouraudTriangle( Triangle t, Material mat)
	{
		// Order vertices {v1,v2,v3} by increasing Y
		Vertex v1 = t.v1;
		Vertex v2 = t.v2;
		Vertex v3 = t.v3;
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
		int lz = v1.zbuf;
		int rz = v1.zbuf;
		int lr = v1.r;
		int lg = v1.g;
		int lb = v1.b;
		int rr = v1.r;
		int rg = v1.g;
		int rb = v1.b;
		int dy_1_2 = v2.y-v1.y;
		int dy_1_3 = v3.y-v1.y;
		int dy_2_3 = v3.y-v2.y;
		int dx_1_2=0, dx_1_3=0, dx_2_3=0;
		int dz_1_2=0, dz_1_3=0, dz_2_3=0;
		int dr_1_2=0, dr_1_3=0, dr_2_3=0;
		int dg_1_2=0, dg_1_3=0, dg_2_3=0;
		int db_1_2=0, db_1_3=0, db_2_3=0;
		if (dy_1_2 != 0) {
			dx_1_2 = ((v2.x<<16)-(v1.x<<16)) / dy_1_2;
			dz_1_2 = (v2.zbuf-v1.zbuf) / dy_1_2;
			dr_1_2 = (v2.r-v1.r) / dy_1_2;
			dg_1_2 = (v2.g-v1.g) / dy_1_2;
			db_1_2 = (v2.b-v1.b) / dy_1_2;
		}
		if (dy_1_3 != 0) {
			dx_1_3 = ((v3.x<<16)-(v1.x<<16)) / dy_1_3;
			dz_1_3 = (v3.zbuf-v1.zbuf) / dy_1_3;
			dr_1_3 = (v3.r-v1.r) / dy_1_3;
			dg_1_3 = (v3.g-v1.g) / dy_1_3;
			db_1_3 = (v3.b-v1.b) / dy_1_3;
		}
		if (dy_2_3 != 0) {
			dx_2_3 = ((v3.x<<16)-(v2.x<<16)) / dy_2_3;
			dz_2_3 = (v3.zbuf-v2.zbuf) / dy_2_3;
			dr_2_3 = (v3.r-v2.r) / dy_2_3;
			dg_2_3 = (v3.g-v2.g) / dy_2_3;
			db_2_3 = (v3.b-v2.b) / dy_2_3;
		}

		// Draw top half
		if (dy_1_2 != 0) {

			for (int i=v1.y; i<v2.y; i++) {
				if (i>0 && i<_height) {
					drawGouraudSpan( i, lx>>16, rx>>16, lz, rz, lr, rr, lg, rg, lb, rb, mat);
				}
				lx += dx_1_3;
				rx += dx_1_2;
				lz += dz_1_3;
				rz += dz_1_2;
				lr += dr_1_3;
				rr += dr_1_2;
				lg += dg_1_3;
				rg += dg_1_2;
				lb += db_1_3;
				rb += db_1_2;
			}
		}

		// Set these by hand just in case top half doesn't exist.
		// (Case where v1 and v2 lie on a horizontal line.)
		// This way, bottom half will *be* the whole, correct
		// triangle in this special case.
		rx = v2.x<<16;
		rz = v2.zbuf;
		rr = v2.r;
		rg = v2.g;
		rb = v2.b;

		// Draw bottom half
		if (dy_2_3 != 0) {

			for (int i=v2.y; i<v3.y; i++) {
				if (i>0 && i<_height) {
					drawGouraudSpan( i, lx>>16, rx>>16, lz, rz, lr, rr, lg, rg, lb, rb, mat);
				}
				lx += dx_1_3;
				rx += dx_2_3;
				lz += dz_1_3;
				rz += dz_2_3;
				lr += dr_1_3;
				rr += dr_2_3;
				lg += dg_1_3;
				rg += dg_2_3;
				lb += db_1_3;
				rb += db_2_3;
			}
		}
	}


	public static void drawGouraudSpan( 
		int y, int lx, int rx, int lz, int rz, int lr, int rr,
		int lg, int rg, int lb, int rb, Material mat
		)
	{
		// Make sure we're drawing left->right.
		// (Scan conversion algorithm can send 
		//  in left and right swapped.)
		int temp;
		if (lx>rx) {
			temp = lx;
			lx = rx;
			rx = temp;
			temp = lz;
			lz = rz;
			rz = temp;
			temp = lr;
			lr = rr;
			rr = temp;
			temp = lg;
			lg = rg;
			rg = temp;
			temp = lb;
			lb = rb;
			rb = temp;
		}

		int tcol = 0;
		int z = lz;
		int r = lr;
		int g = lg;
		int b = lb;
		int dz = 0;
		int dr = 0;
		int dg = 0;
		int db = 0;
		if (lx != rx) {
			dz = (rz-lz) / (rx-lx);
			dr = (rr-lr) / (rx-lx);
			dg = (rg-lg) / (rx-lx);
			db = (rb-lb) / (rx-lx);
		}

		// Clipping against sides of screen;
		if (rx >= _width) {
			rx = _width-1;
		}
		if (lx < 0) {
			z += -lx * dz;
			r += -lx * dr;
			g += -lx * dg;
			b += -lx * db;
			lx = 0;
		}
		int pixel = lx + y * _width;
		// Render the puppy
		for (int i=lx; i<rx; i++) {
			if (i>=0 && i<_width) {
				if ( z > _zbuf[ pixel ] ) {
					if (mat.TRANSPARENT) {
						tcol = _pix [ pixel ];
						_pix [ pixel ] =
							(255<<24) +
							(((((r>>16) * (255-mat._transp_R))>>8) +
							 ((((tcol>>16)&255) * mat._transp_R)>>8))<<16) +
							(((((g>>16) * (255-mat._transp_G))>>8) +
							 ((((tcol>>8 )&255) * mat._transp_G)>>8))<<8) +
							(((((b>>16) * (255-mat._transp_B))>>8) +
							 ((( tcol     &255) * mat._transp_B)>>8)) );
 
					} else {
						_pix [ pixel ] = (255<<24) + (r&(255<<16)) + ((g>>8)&(255<<8)) + (b>>16);
					}
					_zbuf[ pixel ] = z;
				}
			}
			pixel++;
			z += dz;
			r += dr;
			g += dg;
			b += db;
		}
	}




	// Slow! Texture-corrects every pixel!
	public static void drawGouraudTextureTriangle( Triangle t, Material mat)
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
		float lz = v1.zbuf;
		float rz = v1.zbuf;
		float ls = v1.invs;
		float rs = v1.invs;
		float lt = v1.invt;
		float rt = v1.invt;
		int lr = v1.r;
		int lg = v1.g;
		int lb = v1.b;
		int rr = v1.r;
		int rg = v1.g;
		int rb = v1.b;
		int dy_1_2 = v2.y-v1.y;
		int dy_1_3 = v3.y-v1.y;
		int dy_2_3 = v3.y-v2.y;
		int dx_1_2=0, dx_1_3=0, dx_2_3=0;
		int dr_1_2=0, dr_1_3=0, dr_2_3=0;
		int dg_1_2=0, dg_1_3=0, dg_2_3=0;
		int db_1_2=0, db_1_3=0, db_2_3=0;
		float dz_1_2=0, dz_1_3=0, dz_2_3=0;
		float ds_1_2=0, ds_1_3=0, ds_2_3=0;
		float dt_1_2=0, dt_1_3=0, dt_2_3=0;
		if (dy_1_2 != 0) {
			dx_1_2 = ((v2.x<<16)-(v1.x<<16)) / dy_1_2;
			dz_1_2 = (v2.zbuf-v1.zbuf) / (float)dy_1_2;
			ds_1_2 = (v2.invs-v1.invs) / (float)dy_1_2;
			dt_1_2 = (v2.invt-v1.invt) / (float)dy_1_2;
			dr_1_2 = (v2.r-v1.r) / dy_1_2;
			dg_1_2 = (v2.g-v1.g) / dy_1_2;
			db_1_2 = (v2.b-v1.b) / dy_1_2;
		}
		if (dy_1_3 != 0) {
			dx_1_3 = ((v3.x<<16)-(v1.x<<16)) / dy_1_3;
			dz_1_3 = (v3.zbuf-v1.zbuf) / (float)dy_1_3;
			ds_1_3 = (v3.invs-v1.invs) / (float)dy_1_3;
			dt_1_3 = (v3.invt-v1.invt) / (float)dy_1_3;
			dr_1_3 = (v3.r-v1.r) / dy_1_3;
			dg_1_3 = (v3.g-v1.g) / dy_1_3;
			db_1_3 = (v3.b-v1.b) / dy_1_3;
		}
		if (dy_2_3 != 0) {
			dx_2_3 = ((v3.x<<16)-(v2.x<<16)) / dy_2_3;
			dz_2_3 = (v3.zbuf-v2.zbuf) / (float)dy_2_3;
			ds_2_3 = (v3.invs-v2.invs) / (float)dy_2_3;
			dt_2_3 = (v3.invt-v2.invt) / (float)dy_2_3;
			dr_2_3 = (v3.r-v2.r) / dy_2_3;
			dg_2_3 = (v3.g-v2.g) / dy_2_3;
			db_2_3 = (v3.b-v2.b) / dy_2_3;
		}

		// Draw top half
		if (dy_1_2 != 0) {

			for (int i=v1.y; i<v2.y; i++) {
				if (i>0 && i<_height) {
					if (mat.SPEED == Material.SLOW) {
						drawGouraudTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, lr, rr, lg, rg, lb, rb, mat);
					} else if (mat.SPEED == Material.FAST16) {
						drawFast16gouraudTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, lr, rr, lg, rg, lb, rb, mat);
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
				lr += dr_1_3;
				rr += dr_1_2;
				lg += dg_1_3;
				rg += dg_1_2;
				lb += db_1_3;
				rb += db_1_2;
			}
		}

		// Set these by hand just in case top half doesn't exist.
		// (Case where v1 and v2 lie on a horizontal line.)
		// This way, bottom half will *be* the whole, correct
		// triangle in this special case.
		rx = v2.x<<16;
		rz = v2.zbuf;
		rs = v2.invs;
		rt = v2.invt;
		rr = v2.r;
		rg = v2.g;
		rb = v2.b;

		// Draw bottom half
		if (dy_2_3 != 0) {

			for (int i=v2.y; i<v3.y; i++) {
				if (i>0 && i<_height) {
					if (mat.SPEED == Material.SLOW) {
						drawGouraudTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, lr, rr, lg, rg, lb, rb, mat);
					} else if (mat.SPEED == Material.FAST16) {
						drawFast16gouraudTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, lr, rr, lg, rg, lb, rb, mat);
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
				lr += dr_1_3;
				rr += dr_2_3;
				lg += dg_1_3;
				rg += dg_2_3;
				lb += db_1_3;
				rb += db_2_3;
			}
		}
	}

	// Slow!  Texture-corrects every pixel!
	public static void drawGouraudTextureSpan( 
		int y, int lx, int rx, float lz, float rz, 
		float ls, float rs, float lt, float rt, 
		int lr, int rr, int lg, int rg, int lb, int rb, 
		Material mat)
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
			tempi = lr;
			lr = rr;
			rr = tempi;
			tempi = lg;
			lg = rg;
			rg = tempi;
			tempi = lb;
			lb = rb;
			rb = tempi;
		}

		int tcol = 0;
		boolean fog = (mat._lightmodel == Material.FOG);
		float z = lz;
		float s = ls;
		float t = lt;
		int r = lr;
		int g = lg;
		int b = lb;
		float dz = (float)0.0;
		float ds = (float)0.0;
		float dt = (float)0.0;
		int dr = 0;
		int dg = 0;
		int db = 0;
		int red, green, blue;

		if (lx != rx) {
			dz = (rz-lz) / (float)(rx-lx);
			ds = (rs-ls) / (float)(rx-lx);
			dt = (rt-lt) / (float)(rx-lx);
			dr = (rr-lr) / (rx-lx);
			dg = (rg-lg) / (rx-lx);
			db = (rb-lb) / (rx-lx);
		}
		int texel = 0;
		int zint = 0;
		float zinv = (float)0.0;

		// Clip against sides of screen
		if (rx >= _width) {
			rx = _width-1;
		}
		if (lx < 0) {
			z += -lx * dz;
			s += -lx * ds;
			t += -lx * dt;
			r += -lx * dr;
			g += -lx * dg;
			b += -lx * db;
			lx = 0;
		}
		// Render that puppy
		int pixel = lx + y * _width;
		for (int i=lx; i<rx; i++) {
			if (i>=0 && i<_width) {
				zinv = (float)1.0/z;
				zint = (int)( (zinv/(float)1000.0)*(float)(Integer.MAX_VALUE) );

				if ( zint < _zbuf[ pixel ] ) {
					// Extract (r,g,b) texture color for this pixel
					texel = mat._texture[ ((int)(s*zinv*128.0))%128 + (((int)(t*zinv*128.0))%128)*128 ];
					if (texel!=0) {
						red   = (texel>>16)&255;
						green = (texel>>8 )&255;
						blue  =  texel     &255;

						// If surface is transparent but *not* fogged, then light attentuation 
						// (gouraud lighting) blend must happpen before the transparency blend w/ the background.
						if (mat.TRANSPARENT && !fog) {
							tcol = _pix [ pixel ];
							// First, attenuate by factor *light* ([r,g,b] light intensity at this pixel)
							red   = (red   * (r>>16)) >> 8;
							green = (green * (g>>16)) >> 8;
							blue  = (blue  * (b>>16)) >> 8;
							// Then blend (lit) surface texture color with background color by ratio *transp*
							red   = ( red   * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
							green = ( green * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
							blue  = ( blue  * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
						}

						//  If the surface is non-transparent, whether fogged or not, then
						// perform gouraud attenuation.  In the case of fog, the gouraud
						// attenuation performs (half of) the fog blend.  In the case of 
						// gouraud shading, it lights the triangle.
						// If surface is transparent and fogged, then the transparency blend
						// w/background must happen before the fog (gouraud) blend happens
						else {
							
							if (mat.TRANSPARENT) {
								tcol = _pix [ pixel ];
								// First, blend surface texture color with background color by ratio *transp*
								red   = ( red   * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
								green = ( green * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
								blue  = ( blue  * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
							}
							// Then, attenuate by gouraud factor.
							// In case of no fog, this just lights the texture map as per standard gouraud shading.
							// In case of fog, it performs the (object-color half of the) fog blend instead.
							red   = (red   * (r>>16)) >> 8;
							green = (green * (g>>16)) >> 8;
							blue  = (blue  * (b>>16)) >> 8;
						}
							
						// Other half of the fog blend: weighted fog color;
						if (fog) {
							red   += (mat._fog_R * (255-(r>>16)) )>>8;
							green += (mat._fog_G * (255-(g>>16)) )>>8;
							blue  += (mat._fog_B * (255-(b>>16)) )>>8;
						}

						_pix [ pixel ] = (255<<24) + (red<<16) + (green<<8) + blue;
						_zbuf[ pixel ] = zint;
					}
				}
			}
			pixel++;
			z += dz;
			s += ds;
			t += dt;
			r += dr;
			g += dg;
			b += db;
		}
	}

		// Medium!  Texture-corrects every 16 pixels!
	public static void drawFast16gouraudTextureSpan( 
		int y, int lx, int rx, float lz, float rz, 
		float ls, float rs, float lt, float rt, 
		int lr, int rr, int lg, int rg, int lb, int rb, 
		Material mat)
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
			tempi = lr;
			lr = rr;
			rr = tempi;
			tempi = lg;
			lg = rg;
			rg = tempi;
			tempi = lb;
			lb = rb;
			rb = tempi;
		}

		int tcol = 0;
		boolean fog = (mat._lightmodel == Material.FOG);
		float z = lz;
		float s = ls;
		float t = lt;
		int r = lr;
		int g = lg;
		int b = lb;
		float dz = (float)0.0;
		float ds = (float)0.0;
		float dt = (float)0.0;
		int dr = 0;
		int dg = 0;
		int db = 0;
		int red, green, blue;

		if (lx != rx) {
			dz = (rz-lz) / (float)(rx-lx);
			ds = (rs-ls) / (float)(rx-lx);
			dt = (rt-lt) / (float)(rx-lx);
			dr = (rr-lr) / (rx-lx);
			dg = (rg-lg) / (rx-lx);
			db = (rb-lb) / (rx-lx);
		}
		int texel = 0;

		// Clip against sides of screen
		if (rx >= _width) rx = _width-1;
		if (lx < 0) {
			z += -lx * dz;
			s += -lx * ds;
			t += -lx * dt;
			r += -lx * dr;
			g += -lx * dg;
			b += -lx * db;
			lx = 0;
		}
		int subdivs   = (rx-lx) / Render.SUBDIV_SIZE;
		int remainder = (rx-lx) % Render.SUBDIV_SIZE;

		float ZCONV = (float)(Integer.MAX_VALUE) / (float)1000.0;
		float rzf = z;
		float rsf = s;
		float rtf = t;
		int zi = 0;
		int si = 0;
		int ti = 0;
		int dzi;
		int dsi;
		int dti;
		int rzi = 0;
		int rsi = 0;
		int rti = 0;
		if (rzf != 0.0) {
			rzi = (int)(ZCONV / rzf);
			rsi = (int)(rsf / rzf * (float)65536.0);
			rti = (int)(rtf / rzf * (float)65536.0);
		}

		// Render that puppy
		int pixel = lx + y * _width;
		
		// As many SUBDIV-sized pixel chunks as fit into the span.
		for (int k=0; k<subdivs; k++) {
						
			rzf += dz * (float)Render.SUBDIV_SIZE;
			rsf += ds * (float)Render.SUBDIV_SIZE;
			rtf += dt * (float)Render.SUBDIV_SIZE;
			zi  =  rzi;
			si  =  rsi;
			ti  =  rti;
			if (rzf != (float)0.0) {
				rzi =  (int)(ZCONV / rzf);
				rsi =  (int)(rsf / rzf * (float)65536.0);
				rti =  (int)(rtf / rzf * (float)65536.0);
			} else {
				rzi = 0;
				rsi = 0;
				rti = 0;
			}
			dzi = (rzi - zi) / Render.SUBDIV_SIZE;
			dsi = (rsi - si) / Render.SUBDIV_SIZE;
			dti = (rti - ti) / Render.SUBDIV_SIZE;

			for (int i=0; i<Render.SUBDIV_SIZE; i++) {

				if ( zi < _zbuf[ pixel ] ) {
					texel = mat._texture[ (si>>9)%128 + ((ti>>9)%128)*128 ];				
					if (texel!=0) {
						red   = (texel>>16)&255;
						green = (texel>>8 )&255;
						blue  =  texel     &255;

						// If surface is transparent but *not* fogged, then light attentuation 
						// (gouraud lighting) blend must happpen before the transparency blend w/ the background.
						if (mat.TRANSPARENT && !fog) {
							tcol = _pix [ pixel ];
							// First, attenuate by factor *light* ([r,g,b] light intensity at this pixel)
							red   = (red   * (r>>16)) >> 8;
							green = (green * (g>>16)) >> 8;
							blue  = (blue  * (b>>16)) >> 8;
							// Then blend (lit) surface texture color with background color by ratio *transp*
							red   = ( red   * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
							green = ( green * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
							blue  = ( blue  * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
						}

						//  If the surface is non-transparent, whether fogged or not, then
						// perform gouraud attenuation.  In the case of fog, the gouraud
						// attenuation performs (half of) the fog blend.  In the case of 
						// gouraud shading, it lights the triangle.
						// If surface is transparent and fogged, then the transparency blend
						// w/background must happen before the fog (gouraud) blend happens
						else {
							
							if (mat.TRANSPARENT) {
								tcol = _pix [ pixel ];
								// First, blend surface texture color with background color by ratio *transp*
								red   = ( red   * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
								green = ( green * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
								blue  = ( blue  * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
							}
							// Then, attenuate by gouraud factor.
							// In case of no fog, this just lights the texture map as per standard gouraud shading.
							// In case of fog, it performs the (object-color half of the) fog blend instead.
							red   = (red   * (r>>16)) >> 8;
							green = (green * (g>>16)) >> 8;
							blue  = (blue  * (b>>16)) >> 8;
						}
							
						// Other half of the fog blend: weighted fog color;
						if (fog) {
							red   += (mat._fog_R * (255-(r>>16)) )>>8;
							green += (mat._fog_G * (255-(g>>16)) )>>8;
							blue  += (mat._fog_B * (255-(b>>16)) )>>8;
						}

						_pix [ pixel ] = (255<<24) + (red<<16) + (green<<8) + blue;
						_zbuf[ pixel ] = zi;
					}
				}
				pixel++;
				zi += dzi;
				si += dsi;
				ti += dti;
				r += dr;
				g += dg;
				b += db;
			}
		}

		if (remainder > 0) {

			// Remainder of row (less than SUBDIV # pixels)
			rzf += dz * (float)remainder;
			rsf += ds * (float)remainder;
			rtf += dt * (float)remainder;
			zi  =  rzi;
			si  =  rsi;
			ti  =  rti;
			if (rzf != (float)0.0) {
				rzi =  (int)(ZCONV / rzf);
				rsi =  (int)(rsf / rzf * (float)65536.0);
				rti =  (int)(rtf / rzf * (float)65536.0);
			} else {
				rzi = 0;
				rsi = 0;
				rti = 0;
			}
			dzi = (rzi - zi) / remainder;
			dsi = (rsi - si) / remainder;
			dti = (rti - ti) / remainder;

			for (int i=0; i<remainder; i++) {

				if ( zi < _zbuf[ pixel ] ) {
					texel = mat._texture[ (si>>9)%128 + ((ti>>9)%128)*128 ];
					if (texel!=0) {
						red   = (texel>>16)&255;
						green = (texel>>8 )&255;
						blue  =  texel     &255;

						// If surface is transparent but *not* fogged, then light attentuation 
						// (gouraud lighting) blend must happpen before the transparency blend w/ the background.
						if (mat.TRANSPARENT && !fog) {
							tcol = _pix [ pixel ];
							// First, attenuate by factor *light* ([r,g,b] light intensity at this pixel)
							red   = (red   * (r>>16)) >> 8;
							green = (green * (g>>16)) >> 8;
							blue  = (blue  * (b>>16)) >> 8;
							// Then blend (lit) surface texture color with background color by ratio *transp*
							red   = ( red   * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
							green = ( green * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
							blue  = ( blue  * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
						}

						//  If the surface is non-transparent, whether fogged or not, then
						// perform gouraud attenuation.  In the case of fog, the gouraud
						// attenuation performs (half of) the fog blend.  In the case of 
						// gouraud shading, it lights the triangle.
						// If surface is transparent and fogged, then the transparency blend
						// w/background must happen before the fog (gouraud) blend happens
						else {
							
							if (mat.TRANSPARENT) {
								tcol = _pix [ pixel ];
								// First, blend surface texture color with background color by ratio *transp*
								red   = ( red   * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
								green = ( green * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
								blue  = ( blue  * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
							}
							// Then, attenuate by gouraud factor.
							// In case of no fog, this just lights the texture map as per standard gouraud shading.
							// In case of fog, it performs the (object-color half of the) fog blend instead.
							red   = (red   * (r>>16)) >> 8;
							green = (green * (g>>16)) >> 8;
							blue  = (blue  * (b>>16)) >> 8;
						}
							
						// Other half of the fog blend: weighted fog color;
						if (fog) {
							red   += (mat._fog_R * (255-(r>>16)) )>>8;
							green += (mat._fog_G * (255-(g>>16)) )>>8;
							blue  += (mat._fog_B * (255-(b>>16)) )>>8;
						}

						_pix [ pixel ] = (255<<24) + (red<<16) + (green<<8) + blue;
						_zbuf[ pixel ] = zi;
					}
				}
				pixel++;
				zi += dzi;
				si += dsi;
				ti += dti;
				r += dr;
				g += dg;
				b += db;
			}
		}
	}


	// Fast! Incorrect z interpolation!
	public static void drawFastGouraudTextureTriangle( Triangle t, Material mat)
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

		int lx = v1.x<<16;
		int rx = v1.x<<16;
		int lz = v1.z;
		int rz = v1.z;
		int ls = v1.s;
		int rs = v1.s;
		int lt = v1.t;
		int rt = v1.t;
		int lr = v1.r;
		int lg = v1.g;
		int lb = v1.b;
		int rr = v1.r;
		int rg = v1.g;
		int rb = v1.b;
		int dy_1_2 = v2.y-v1.y;
		int dy_1_3 = v3.y-v1.y;
		int dy_2_3 = v3.y-v2.y;
		int dx_1_2=0, dx_1_3=0, dx_2_3=0;
		int dr_1_2=0, dr_1_3=0, dr_2_3=0;
		int dg_1_2=0, dg_1_3=0, dg_2_3=0;
		int db_1_2=0, db_1_3=0, db_2_3=0;
		int dz_1_2=0, dz_1_3=0, dz_2_3=0;
		int ds_1_2=0, ds_1_3=0, ds_2_3=0;
		int dt_1_2=0, dt_1_3=0, dt_2_3=0;
		if (dy_1_2 != 0) {
			dx_1_2 = ((v2.x<<16)-(v1.x<<16)) / dy_1_2;
			dz_1_2 = (v2.z-v1.z) / dy_1_2;
			ds_1_2 = (v2.s-v1.s) / dy_1_2;
			dt_1_2 = (v2.t-v1.t) / dy_1_2;
			dr_1_2 = (v2.r-v1.r) / dy_1_2;
			dg_1_2 = (v2.g-v1.g) / dy_1_2;
			db_1_2 = (v2.b-v1.b) / dy_1_2;
		}
		if (dy_1_3 != 0) {
			dx_1_3 = ((v3.x<<16)-(v1.x<<16)) / dy_1_3;
			dz_1_3 = (v3.z-v1.z) / dy_1_3;
			ds_1_3 = (v3.s-v1.s) / dy_1_3;
			dt_1_3 = (v3.t-v1.t) / dy_1_3;
			dr_1_3 = (v3.r-v1.r) / dy_1_3;
			dg_1_3 = (v3.g-v1.g) / dy_1_3;
			db_1_3 = (v3.b-v1.b) / dy_1_3;
		}
		if (dy_2_3 != 0) {
			dx_2_3 = ((v3.x<<16)-(v2.x<<16)) / dy_2_3;
			dz_2_3 = (v3.z-v2.z) / dy_2_3;
			ds_2_3 = (v3.s-v2.s) / dy_2_3;
			dt_2_3 = (v3.t-v2.t) / dy_2_3;
			dr_2_3 = (v3.r-v2.r) / dy_2_3;
			dg_2_3 = (v3.g-v2.g) / dy_2_3;
			db_2_3 = (v3.b-v2.b) / dy_2_3;
		}

		// Draw top half
		if (dy_1_2 != 0) {

			for (int i=v1.y; i<v2.y; i++) {
				if (i>0 && i<_height) {
					drawFastGouraudTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, lr, rr, lg, rg, lb, rb, mat);
				}
				lx += dx_1_3;
				rx += dx_1_2;
				lz += dz_1_3;
				rz += dz_1_2;
				ls += ds_1_3;
				rs += ds_1_2;
				lt += dt_1_3;
				rt += dt_1_2;
				lr += dr_1_3;
				rr += dr_1_2;
				lg += dg_1_3;
				rg += dg_1_2;
				lb += db_1_3;
				rb += db_1_2;
			}
		}

		// Set these by hand just in case top half doesn't exist.
		// (Case where v1 and v2 lie on a horizontal line.)
		// This way, bottom half will *be* the whole, correct
		// triangle in this special case.
		rx = v2.x<<16;
		rz = v2.z;
		rs = v2.s;
		rt = v2.t;
		rr = v2.r;
		rg = v2.g;
		rb = v2.b;

		// Draw bottom half
		if (dy_2_3 != 0) {

			for (int i=v2.y; i<v3.y; i++) {
				if (i>0 && i<_height) {
					drawFastGouraudTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, lr, rr, lg, rg, lb, rb, mat);
				}
				lx += dx_1_3;
				rx += dx_2_3;
				lz += dz_1_3;
				rz += dz_2_3;
				ls += ds_1_3;
				rs += ds_2_3;
				lt += dt_1_3;
				rt += dt_2_3;
				lr += dr_1_3;
				rr += dr_2_3;
				lg += dg_1_3;
				rg += dg_2_3;
				lb += db_1_3;
				rb += db_2_3;
			}
		}
	}

	// Fast!  Incorrect z interpolation!
	public static void drawFastGouraudTextureSpan( 
		int y, int lx, int rx, int lz, int rz, 
		int ls, int rs, int lt, int rt, 
		int lr, int rr, int lg, int rg, int lb, int rb, 
		Material mat)
	{
		// Make sure we're drawing left->right.
		// (Scan conversion algorithm can send 
		//  in left and right swapped.)
		int tempi;
		if (lx>rx) {
			tempi = lx;  lx = rx;  rx = tempi;
			tempi = lz;  lz = rz;  rz = tempi;
			tempi = ls;  ls = rs;  rs = tempi;
			tempi = lt;  lt = rt;  rt = tempi;
			tempi = lr;  lr = rr;  rr = tempi;
			tempi = lg;  lg = rg;  rg = tempi;
			tempi = lb;  lb = rb;  rb = tempi;
		}

		int tcol = 0;
		boolean fog = (mat._lightmodel == Material.FOG);
		int z = lz;
		int s = ls;
		int t = lt;
		int r = lr;
		int g = lg;
		int b = lb;
		int dz = 0;
		int ds = 0;
		int dt = 0;
		int dr = 0;
		int dg = 0;
		int db = 0;
		int red, green, blue;

		if (lx != rx) {
			dz = (rz-lz) / (rx-lx);
			ds = (rs-ls) / (rx-lx);
			dt = (rt-lt) / (rx-lx);
			dr = (rr-lr) / (rx-lx);
			dg = (rg-lg) / (rx-lx);
			db = (rb-lb) / (rx-lx);
		}
		int texel = 0;

		// Clip against sides of screen
		if (rx >= _width) rx = _width-1;
		if (lx < 0) {
			z += -lx * dz;
			s += -lx * ds;
			t += -lx * dt;
			r += -lx * dr;
			g += -lx * dg;
			b += -lx * db;
			lx = 0;
		}
		// Render that puppy
		int pixel = lx + y * _width;
		for (int i=lx; i<rx; i++) {
			if (i>=0 && i<_width) {

				if ( z < _zbuf[ pixel ] ) {
					// Extract (r,g,b) texture color for this pixel
					texel = mat._texture[ (s>>9)%128 + ((t>>9)%128)*128 ];
					if (texel!=0) {
						red   = (texel>>16)&255;
						green = (texel>>8 )&255;
						blue  =  texel     &255;

						// If surface is transparent but *not* fogged, then light attentuation 
						// (gouraud lighting) blend must happpen before the transparency blend w/ the background.
						if (mat.TRANSPARENT && !fog) {
							tcol = _pix [ pixel ];
							// First, attenuate by factor *light* ([r,g,b] light intensity at this pixel)
							red   = (red   * (r>>16)) >> 8;
							green = (green * (g>>16)) >> 8;
							blue  = (blue  * (b>>16)) >> 8;
							// Then blend (lit) surface texture color with background color by ratio *transp*
							red   = ( red   * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
							green = ( green * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
							blue  = ( blue  * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
						}

						//  If the surface is non-transparent, whether fogged or not, then
						// perform gouraud attenuation.  In the case of fog, the gouraud
						// attenuation performs (half of) the fog blend.  In the case of 
						// gouraud shading, it lights the triangle.
						// If surface is transparent and fogged, then the transparency blend
						// w/background must happen before the fog (gouraud) blend happens
						else {
							
							if (mat.TRANSPARENT) {
								tcol = _pix [ pixel ];
								// First, blend surface texture color with background color by ratio *transp*
								red   = ( red   * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
								green = ( green * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
								blue  = ( blue  * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
							}
							// Then, attenuate by gouraud factor.
							// In case of no fog, this just lights the texture map as per standard gouraud shading.
							// In case of fog, it performs the (object-color half of the) fog blend instead.
							red   = (red   * (r>>16)) >> 8;
							green = (green * (g>>16)) >> 8;
							blue  = (blue  * (b>>16)) >> 8;
						}
							
						// Other half of the fog blend: weighted fog color;
						if (fog) {
							red   += (mat._fog_R * (255-(r>>16)) )>>8;
							green += (mat._fog_G * (255-(g>>16)) )>>8;
							blue  += (mat._fog_B * (255-(b>>16)) )>>8;
						}

						_pix [ pixel ] = (255<<24) + (red<<16) + (green<<8) + blue;
						_zbuf[ pixel ] = z;
					}
				}
			}
			pixel++;
			z += dz;
			s += ds;
			t += dt;
			r += dr;
			g += dg;
			b += db;
		}
	}
}

