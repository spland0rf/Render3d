package com.splandorf.render3d.shader;

public class TranspTriangle extends Shader {

    public void drawTranspTriangle( Triangle t, rMaterial mat)
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
		int dy_1_2 = v2.y-v1.y;
		int dy_1_3 = v3.y-v1.y;
		int dy_2_3 = v3.y-v2.y;
		int dx_1_2=0, dx_1_3=0, dx_2_3=0;
		int dz_1_2=0, dz_1_3=0, dz_2_3=0;
		if (dy_1_2 != 0) {
			dx_1_2 = ((v2.x<<16)-(v1.x<<16)) / dy_1_2;
			dz_1_2 = (v2.zbuf-v1.zbuf) / dy_1_2;
		}
		if (dy_1_3 != 0) {
			dx_1_3 = ((v3.x<<16)-(v1.x<<16)) / dy_1_3;
			dz_1_3 = (v3.zbuf-v1.zbuf) / dy_1_3;
		}
		if (dy_2_3 != 0) {
			dx_2_3 = ((v3.x<<16)-(v2.x<<16)) / dy_2_3;
			dz_2_3 = (v3.zbuf-v2.zbuf) / dy_2_3;
		}
		
		// Draw top half
		if (dy_1_2 != 0) {
			
			for (int i=v1.y; i<v2.y; i++) {
				if (i>0 && i<_height) {
					drawTranspSpan( i, lx>>16, rx>>16, lz, rz, mat);
				}
				lx += dx_1_3;
				rx += dx_1_2;
				lz += dz_1_3;
				rz += dz_1_2;
			}
		}
		
		// Set these by hand just in case top half doesn't exist.
		// (Case where v1 and v2 lie on a horizontal line.)
		// This way, bottom half will *be* the whole, correct
		// triangle in this special case.
		rx = v2.x<<16;
		rz = v2.zbuf;
		
		// Draw bottom half
		if (dy_2_3 != 0) {
			
			for (int i=v2.y; i<v3.y; i++) {
				if (i>0 && i<_height) {
					drawTranspSpan( i, lx>>16, rx>>16, lz, rz, mat);
				}
				lx += dx_1_3;
				rx += dx_2_3;
				lz += dz_1_3;
				rz += dz_2_3;
			}
		}
    }
    
    public void drawTranspSpan( int y, int lx, int rx, int lz, int rz, rMaterial mat)
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
		}
		
		int r = 0;
		int g = 0;
		int b = 0;
		int color;
		int red   = (mat._color>>16)&255;
		int green = (mat._color>>8)&255;
		int blue  = (mat._color)&255;
		
		int z = lz;
		int dz = 0;
		if (lx != rx) {
			dz = (rz-lz) / (rx-lx);
		}
		
		// Clipping against sides of screen;
		if (rx >= _width) rx = _width-1;
		if (lx < 0) {
			z += -lx * dz;
			lx = 0;
		}
		// Render that puppy
		int pixel = lx + y * _width;
		for (int i=lx; i<rx; i++) {
			if (i>=0 && i<_width) {
				if ( z > _zbuf[ pixel ] ) {
					color = _pix [ pixel ];
					r = ((color>>16)&255) + red;
					g = ((color>>8 )&255) + green;
					b = ( color     &255) + blue;
					if (r>255) r = 255;
					if (g>255) g = 255;
					if (b>255) b = 255;
					_pix [ pixel ] = (255<<24) + (r<<16) + (g<<8) + b;
				}
			}
			pixel++;
			z += dz;
		}
	}
}		

