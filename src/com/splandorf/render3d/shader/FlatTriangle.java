package com.splandorf.render3d.shader;

static class FlatTriangle extends Shader {

	// Slow! Texture-corrects every pixel!
	public void flatTextureTriangle( Triangle t, Material mat)
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
					if (mat.SPEED == Material.SLOW) {
						drawFlatTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
					} else if (mat.SPEED == Material.FAST16) {
						drawFast16flatTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
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
					if (mat.SPEED == Material.SLOW) {
						drawFlatTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
					} else if (mat.SPEED == Material.FAST16) {
						drawFast16flatTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
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

	// Slow!  Texture-corrects every pixel!
	public void drawFlatTextureSpan( int y, int lx, int rx, float lz, float rz, 
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

		int tcol = 0;
		int r_i = mat._dif_R;
		int g_i = mat._dif_G;
		int b_i = mat._dif_B;
		int r;
		int g;
		int b;
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
						r = (((texel>>16)&255) * r_i)>>8;
						g = (((texel>>8 )&255) * g_i)>>8;
						b = ( (texel     &255) * b_i)>>8;	
						if (mat.TRANSPARENT) {
							tcol = _pix [ pixel ];
							// First, blend surface texture color with background color by ratio *transp*
							r = ( r * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
							g = ( g * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
							b = ( b * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
						}

						_pix [ pixel ] = (255<<24) + (r<<16) + (g<<8) + b;
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

	// Slow!  Texture-corrects every pixel!
	public void drawFast16flatTextureSpan( int y, int lx, int rx, float lz, float rz, 
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

		int tcol = 0;
		int r_i = mat._dif_R;
		int g_i = mat._dif_G;
		int b_i = mat._dif_B;
		int r;
		int g;
		int b;
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

		// Clip against sides of screen
		if (rx >= _width) rx = _width-1;
		if (lx < 0) {
			z += -lx * dz;
			s += -lx * ds;
			t += -lx * dt;
			lx = 0;
		}

		int subdivs   = (rx-lx) / SUBDIV_SIZE;
		int remainder = (rx-lx) % SUBDIV_SIZE;

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
						
			rzf += dz * (float)SUBDIV_SIZE;
			rsf += ds * (float)SUBDIV_SIZE;
			rtf += dt * (float)SUBDIV_SIZE;
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
			dzi = (rzi - zi) / SUBDIV_SIZE;
			dsi = (rsi - si) / SUBDIV_SIZE;
			dti = (rti - ti) / SUBDIV_SIZE;

			for (int i=0; i<SUBDIV_SIZE; i++) {

				if ( zi < _zbuf[ pixel ] ) {
					texel = mat._texture[ (si>>9)%128 + ((ti>>9)%128)*128 ];
					if (texel!=0) {
						r = (((texel>>16)&255) * r_i)>>8;
						g = (((texel>>8 )&255) * g_i)>>8;
						b = ( (texel     &255) * b_i)>>8;	
						if (mat.TRANSPARENT) {
							tcol = _pix [ pixel ];
							// First, blend surface texture color with background color by ratio *transp*
							r = ( r * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
							g = ( g * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
							b = ( b * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
						}

						_pix [ pixel ] = (255<<24) + (r<<16) + (g<<8) + b;
						_zbuf[ pixel ] = zi;
					}
				}
				pixel++;
				zi += dzi;
				si += dsi;
				ti += dti;
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
						r = (((texel>>16)&255) * r_i)>>8;
						g = (((texel>>8 )&255) * g_i)>>8;
						b = ( (texel     &255) * b_i)>>8;	
						if (mat.TRANSPARENT) {
							tcol = _pix [ pixel ];
							// First, blend surface texture color with background color by ratio *transp*
							r = ( r * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
							g = ( g * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
							b = ( b * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
						}

						_pix [ pixel ] = (255<<24) + (r<<16) + (g<<8) + b;
						_zbuf[ pixel ] = zi;
					}
				}
				pixel++;
				zi += dzi;
				si += dsi;
				ti += dti;
			}
		}
	}


	// Fast! Incorrect z-interpolation!
	public void fastFlatTextureTriangle( Triangle t, Material mat)
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
		int dy_1_2 = v2.y-v1.y;
		int dy_1_3 = v3.y-v1.y;
		int dy_2_3 = v3.y-v2.y;
		int dx_1_2=0, dx_1_3=0, dx_2_3=0;
		int dz_1_2=0, dz_1_3=0, dz_2_3=0;
		int ds_1_2=0, ds_1_3=0, ds_2_3=0;
		int dt_1_2=0, dt_1_3=0, dt_2_3=0;
		if (dy_1_2 != 0) {
			dx_1_2 = ((v2.x<<16)-(v1.x<<16)) / dy_1_2;
			dz_1_2 = (v2.z-v1.z) / dy_1_2;
			ds_1_2 = (v2.s-v1.s) / dy_1_2;
			dt_1_2 = (v2.t-v1.t) / dy_1_2;
		}
		if (dy_1_3 != 0) {
			dx_1_3 = ((v3.x<<16)-(v1.x<<16)) / dy_1_3;
			dz_1_3 = (v3.z-v1.z) / dy_1_3;
			ds_1_3 = (v3.s-v1.s) / dy_1_3;
			dt_1_3 = (v3.t-v1.t) / dy_1_3;
		}
		if (dy_2_3 != 0) {
			dx_2_3 = ((v3.x<<16)-(v2.x<<16)) / dy_2_3;
			dz_2_3 = (v3.z-v2.z) / dy_2_3;
			ds_2_3 = (v3.s-v2.s) / dy_2_3;
			dt_2_3 = (v3.t-v2.t) / dy_2_3;
		}

		// Draw top half
		if (dy_1_2 != 0) {

			for (int i=v1.y; i<v2.y; i++) {
				if (i>0 && i<_height) {
					drawFastFlatTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
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
		rz = v2.z;
		rs = v2.s;
		rt = v2.t;

		// Draw bottom half
		if (dy_2_3 != 0) {

			for (int i=v2.y; i<v3.y; i++) {
				if (i>0 && i<_height) {
					drawFastFlatTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
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

	// Slow!  Texture-corrects every pixel!
	public void fastFlatTextureSpan( int y, int lx, int rx, int lz, int rz, 
									int ls, int rs, int lt, int rt, Material mat)
	{
		// Make sure we're drawing left->right.
		// (Scan conversion algorithm can send 
		//  in left and right swapped.)
		int tempi;
		if (lx>rx) {
			tempi = lx;
			lx = rx;
			rx = tempi;
			tempi = lz;
			lz = rz;
			rz = tempi;
			tempi = ls;
			ls = rs;
			rs = tempi;
			tempi = lt;
			lt = rt;
			rt = tempi;
		}

		int tcol = 0;
		int r_i = mat._dif_R;
		int g_i = mat._dif_G;
		int b_i = mat._dif_B;
		int r;
		int g;
		int b;

		int z = lz;
		int s = ls;
		int t = lt;
		int dz = 0;
		int ds = 0;
		int dt = 0;
		if (lx != rx) {
			dz = (rz-lz) / (rx-lx);
			ds = (rs-ls) / (rx-lx);
			dt = (rt-lt) / (rx-lx);
		}
		int texel = 0;
		int zint = 0;

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

				if ( z < _zbuf[ pixel ] ) {
					texel = mat._texture[ (s>>9)%128 + ((t>>9)%128)*128 ];
					if (texel!=0) {
						r = (((texel>>16)&255) * r_i)>>8;
						g = (((texel>>8 )&255) * g_i)>>8;
						b = ( (texel     &255) * b_i)>>8;	
						if (mat.TRANSPARENT) {
							tcol = _pix [ pixel ];
							// First, blend surface texture color with background color by ratio *transp*
							r = ( r * (255-mat._transp_R) + ((tcol>>16)&255) * mat._transp_R) >> 8;
							g = ( g * (255-mat._transp_G) + ((tcol>>8 )&255) * mat._transp_G) >> 8;
							b = ( b * (255-mat._transp_B) + ( tcol     &255) * mat._transp_B) >> 8;
						}

						_pix [ pixel ] = (255<<24) + (r<<16) + (g<<8) + b;
						_zbuf[ pixel ] = z;
					}
				}
			}
			pixel++;
			z += dz;
			s += ds;
			t += dt;
		}
	}
}

