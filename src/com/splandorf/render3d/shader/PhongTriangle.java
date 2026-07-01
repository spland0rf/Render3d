package com.splandorf.render3d.shader;

import com.splandorf.render3d.scene.*;

public class PhongTriangle extends Shader
{
	public static void drawPhongTriangle( Triangle t, Material mat)
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

		int color = mat._color;
//		VecSf n1 = MemMgr.VecSf();
//		VecSf n2 = MemMgr.VecSf();
//		VecSf n3 = MemMgr.VecSf();
//		Alg.cart2sphere( v1.w_n, n1);
//		Alg.cart2sphere( v2.w_n, n2);
//		Alg.cart2sphere( v3.w_n, n3);

		int lx = v1.x<<16;
		int rx = v1.x<<16;
		int lz = v1.z;
		int rz = v1.z;
		int la = (int)(v1.w_n.x * (float)32768.0 + (float)32768.0);
		int ra = (int)(v1.w_n.x * (float)32768.0 + (float)32768.0);
		int lb = (int)(v1.w_n.y * (float)32768.0 + (float)32768.0);
		int rb = (int)(v1.w_n.y * (float)32768.0 + (float)32768.0);
		//System.err.print("Normal: ");
		//Alg.print( v1.w_n);
		int dy_1_2 = v2.y-v1.y;
		int dy_1_3 = v3.y-v1.y;
		int dy_2_3 = v3.y-v2.y;
		int dx_1_2=0, dx_1_3=0, dx_2_3=0;
		int dz_1_2=0, dz_1_3=0, dz_2_3=0;
		int da_1_2=0, da_1_3=0, da_2_3=0;
		int db_1_2=0, db_1_3=0, db_2_3=0;
		if (dy_1_2 != 0) {
			dx_1_2 = ((v2.x<<16)-(v1.x<<16)) / dy_1_2;
			dz_1_2 = (v2.z-v1.z) / dy_1_2;
			da_1_2 = (int)((v2.w_n.x-v1.w_n.x)*(float)32768.0) / dy_1_2;
			db_1_2 = (int)((v2.w_n.y-v1.w_n.y)*(float)32768.0) / dy_1_2;
		}
		if (dy_1_3 != 0) {
			dx_1_3 = ((v3.x<<16)-(v1.x<<16)) / dy_1_3;
			dz_1_3 = (v3.z-v1.z) / dy_1_3;
			da_1_3 = (int)((v3.w_n.x-v1.w_n.x)*(float)32768.0) / dy_1_3;
			db_1_3 = (int)((v3.w_n.y-v1.w_n.y)*(float)32768.0) / dy_1_3;
		}
		if (dy_2_3 != 0) {
			dx_2_3 = ((v3.x<<16)-(v2.x<<16)) / dy_2_3;
			dz_2_3 = (v3.z-v2.z) / dy_2_3;
			da_2_3 = (int)((v3.w_n.x-v2.w_n.x)*(float)32768.0) / dy_2_3;
			db_2_3 = (int)((v3.w_n.y-v2.w_n.y)*(float)32768.0) / dy_2_3;
		}

		// Draw top half
		if (dy_1_2 != 0) {

			for (int i=v1.y; i<v2.y; i++) {
				if (i>0 && i<_height) {
					drawPhongSpan( i, lx>>16, rx>>16, lz, rz, la, ra, lb, rb, color, mat);
				}
				lx += dx_1_3;
				rx += dx_1_2;
				lz += dz_1_3;
				rz += dz_1_2;
				la += da_1_3;
				ra += da_1_2;
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
		ra = (int)(v2.w_n.x*32768.0 + 32768.0);
		rb = (int)(v2.w_n.y*32768.0 + 32768.0);

		// Draw bottom half
		if (dy_2_3 != 0) {

			for (int i=v2.y; i<v3.y; i++) {
				if (i>0 && i<_height) {
					drawPhongSpan( i, lx>>16, rx>>16, lz, rz, la, ra, lb, rb, color, mat);
				}
				lx += dx_1_3;
				rx += dx_2_3;
				lz += dz_1_3;
				rz += dz_2_3;
				la += da_1_3;
				ra += da_2_3;
				lb += db_1_3;
				rb += db_2_3;
			}
		}
	}

	public static void drawPhongSpan( int y, int lx, int rx, int lz, int rz, 
		int la, int ra, int lb, int rb, int color, Material mat)
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
		}
/* 
		int t_r = 0;
		int t_g = 0;
		int t_b = 0;
		int red, green, blue;
		int tcol;
		if (mat.TRANSPARENT) {
			t_r = (((color>>16)&255)*(255-mat._transp_R))>>8;
			t_g = (((color>>8 )&255)*(255-mat._transp_G))>>8;
			t_b = (( color     &255)*(255-mat._transp_B))>>8;
		}
*/
		int z = lz;
		int a = la;
		int b = lb;
		int dz = 0;
		int da = 0;
		int db = 0;
		if (lx != rx) {
			dz = (rz-lz) / (rx-lx);
			da = (ra-la) / (rx-lx);
			db = (rb-lb) / (rx-lx);
		}

		// Clipping against sides of screen;
		if (rx >= _width) rx = _width-1;
		if (lx < 0) {
			z += -lx * dz;
			a += -lx * da;
			b += -lx * db;
			lx = 0;
		}
		//int s, t;
		// Render that puppy
		int pixel = lx + y * _width;
		for (int i=lx; i<rx; i++) {
			if (i>=0 && i<_width) {
				if ( z > _zbuf[ pixel ] ) {

//					if (mat.TRANSPARENT) {
//						tcol = _pix[ pixel];
//						_pix[ pixel ] = 
//							(255<<24) +
//							((t_r + ((((tcol>>16)&255)*mat._transp_R)>>8))<<16) +
//							((t_g + ((((tcol>>8 )&255)*mat._transp_R)>>8))<<8)  +
//							 (t_b + ((( tcol     &255)*mat._transp_R)>>8));
//
//					} else {
	
//					n.a = a;
//					n.b = b;
//					illuminate( n, l);
//					red   = (((color>>16) & 255) * (int)(l.x*255.0))>>8;
//					green = (((color>>8)  & 255) * (int)(l.y*255.0))>>8;
//					blue  = (( color      & 255) * (int)(l.z*255.0))>>8;

					// "Fake" Phong lighting by using a pre-computed environment map!
					// Need to do some archaeology on where that environment map comes from:
					// loaded from a file, or precomputed based on light positions in the scene?
					_pix [ pixel ] = mat._env_map[ (a>>9)%128 + ((127-(b>>9))%128)*128 ]; //(255<<24) + (red<<16) + (green<<8) + blue;
					_zbuf[ pixel ] = z;
//					}
				}
			}
			pixel++;
			z += dz;
			a += da;
			b += db;
		}
	}
}
