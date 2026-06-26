package com.splandorf.render3d.shader;

import java.util.ArrayList;

import com.splandorf.render3d.MemMgr;
import com.splandorf.render3d.Render;
import com.splandorf.render3d.math.*;
import com.splandorf.render3d.scene.*;


public class Particle extends Shader {

    public static void drawPointset( Mat4f m, ArrayList<Vertex> vlist, Material mat)
    {
		Vec3f p = MemMgr.Vec3f();
		int cx = _width / 2;
		int cy = _height / 2;
		float xm = (float)cx*(float)0.6;
		float ym = (float)cy*(float)0.6;
		float realx;
		float realy;
		int fixedx;
		int fixedy;

		Vertex v;
		
		for (int i=0; i<vlist.size(); i++) {
			
			v = (Vertex)vlist.get(i);
			Alg.mult( m, v.p, p);
			
			if (p.z > (float)0.0) {

				realx = (float)cx + (p.x / p.z * xm);
				realy = (float)cy + (p.y / p.z * ym);
				v.x = (int)realx;
				v.y = (int)realy;
				if (v.x > 0 && v.x < (_width-1) && v.y > 0 && v.y < (_height-1) ) {
					fixedx = (int)((realx - (float)v.x) * 8.0);
					fixedy = (int)((realy - (float)v.y) * 8.0);
					
					v.invz = (float)1.0 / p.z;
					v.zbuf = (int)(v.invz * (float)10000.0);
					if (v.zbuf > Render.MAX24BIT) {
						v.zbuf = Render.MAX24BIT;
					}
					if (v.zbuf < 1) {
						v.zbuf = 1;
					}
					
					if (mat._pointstyle == Material.WU) {

						Particle.drawWuPoint( v, fixedx, fixedy, mat);
					} 
					else if (mat._pointstyle == Material.GAUSSIAN) {
				
						int scale = 0;
						
						if (v.invz < (float)1.0 && v.invz > (float)0.0) {
							scale = mat._n_gaussian_dots-1 - (int)(v.invz * (float)mat._n_gaussian_dots);
							int factor = (int)((float)255.0 * v.invz);
							mat._f_red   = ((mat._near_red  *factor + mat._far_red  *(255-factor))>>8);
							mat._f_green = ((mat._near_green*factor + mat._far_green*(255-factor))>>8);
							mat._f_blue  = ((mat._near_blue *factor + mat._far_blue *(255-factor))>>8);
						} else {
							mat._f_red   = mat._near_red;
							mat._f_green = mat._near_green;
							mat._f_blue  = mat._near_blue;
						}
						//			System.err.println( "z-distance: " + p.z + "  map#:  " + scale);
						mat._texture = mat._gaussian_dots[ scale];
						mat._lightmodel = Material.INTENSITY_FLARE;
						mat._sprite_width = mat._gaussian_res;
						mat._sprite_height = mat._gaussian_res;
						//			mat._color = color;

						int start_y = v.y - mat._gaussian_res / 2;
						int end_y   = v.y + mat._gaussian_res / 2;
						int start_x = v.x - mat._gaussian_res / 2;
						int end_x   = v.x + mat._gaussian_res / 2;
						
						int sprite_y = 0;
						
						if (start_y > _height || end_y < 0 || start_x > _width || end_y < 0) {
							return;
						}
						if (start_y < 0) {
							sprite_y += -start_y;
							start_y = 0;
						}
						if (end_y >= _height) {
							end_y = _height-1;
						}
					
						for (int l=start_y; l<end_y; l++) {
							//			    System.err.print("x");
							Particle.drawSpriteSpan( l, sprite_y, start_x, end_x, v.zbuf, mat);
							sprite_y++;
						}
					//			System.err.println("");
					}
				}
			}
		}
		MemMgr.done( p);
    }

  
    public static void drawParticle( Mat4f m, Obj obj)
    {
		Vec3f loc = MemMgr.Vec3f( (float)0.0, (float)0.0, (float)0.0);
		
		Alg.mult( m, loc);
		
		if (loc.z < (float)0.0) return;
		
		int cx = _width / 2;
		int cy = _height / 2;
		float xm = (float)cx*(float)0.6;
		float ym = (float)cy*(float)0.6;
		int x    = cx + (int)(loc.x / loc.z * xm);
		int y    = cy + (int)(loc.y / loc.z * ym);
		int zbuf = (int)((float)10000.0 / loc.z);
		if (zbuf > Render.MAX24BIT) {
            zbuf = Render.MAX24BIT;
        }
		if (zbuf < 1) zbuf = 1;
		
		Material mat = obj.mat;
		
		if (mat._lightmodel == Material.FLARE || mat._lightmodel == Material.SPRITE)  {
			
			int start_y = y - mat._sprite_height / 2;
			int end_y   = y + mat._sprite_height / 2;
			int start_x = x - mat._sprite_width  / 2;
			int end_x   = x + mat._sprite_width  / 2;
			
			int sprite_y = 0;
			
			if (start_y > _height || end_y < 0 || start_x > _width || end_y < 0) {
				return;
			}
			if (start_y < 0) {
				sprite_y += -start_y;
				start_y = 0;
			}
			if (end_y >= _height) {
				end_y = _height-1;
			}
			
			for (int i=start_y; i<end_y; i++) {
				drawSpriteSpan( i, sprite_y, start_x, end_x, zbuf, mat);
				sprite_y++;
			}
			
		} else if (mat._lightmodel == Material.BILLBOARD) {
			// Implement billboard, i.e.: simple blit of the sprite with
            // no transparency?
		}
		
		MemMgr.done( loc);
    }

     // Fast!  No texture correction!
    public static void drawSpriteSpan( int y, int sprite_y, int lx, int rx, int z, Material mat)
    {
        int r, g, b, scol, pcol;
        int sexel = mat._sprite_width * sprite_y;

        // Clip against sides of screen
        if (rx >= _width) {
            rx = _width-1;
        }
        if (lx < 0) {
            sexel += -lx;
            lx = 0;
        }
        
        // Render that puppy
        int pixel = lx + y * _width;
        for (int i=lx; i<rx; i++) {
            if ( z > _zbuf[ pixel ] ) {
            
                if (mat._lightmodel == Material.FLARE) {
                    
                    scol = mat._texture[ sexel ];
                    pcol = _pix[ pixel ];
                    r = ((scol>>16)&255) + ((pcol>>16)&255);
                    g = ((scol>>8)&255) + ((pcol>>8)&255);
                    b = ((scol)&255) + ((pcol)&255);
                    if (r>255) r=255;
                    if (g>255) g=255;
                    if (b>255) b=255;
                    _pix[ pixel] = (255<<24) + (r<<16) + (g<<8) + b;    
                } 
                else if (mat._lightmodel == Material.INTENSITY_FLARE) {
                    
                    scol = (mat._texture[ sexel ])&255;
                    pcol = _pix[ pixel ];
                    r = ((scol*mat._f_red  )>>8) + ((pcol>>16)&255);
                    g = ((scol*mat._f_green)>>8) + ((pcol>>8)&255);
                    b = ((scol*mat._f_blue )>>8) + ((pcol)&255);
                    if (r>255) r=255;
                    if (g>255) g=255;
                    if (b>255) b=255;
                    _pix[ pixel] = (255<<24) + (r<<16) + (g<<8) + b;    
                } 
                else if (mat._lightmodel == Material.SPRITE) {
                    
                    _pix[ pixel ] = mat._texture[ sexel];
                }
            }
            pixel++;
            sexel++;
        }
    }




    public static void drawWuPoint( Vertex v, int xoff, int yoff, Material mat)
    {
		//	System.out.println("---------------------------------------");
		int wuoffset=0, wucolors=0, wu_00=0, wu_01=0, wu_10=0, wu_11=0, wu_red=0, wu_green=0, wu_blue=0;
		wuoffset = xoff + yoff * 8;
		int pixel = v.x + v.y * _width;
		int color, red, green, blue;

		//System.err.println("X: " + v.x + "  Y: " + v.y + "  xoff: " + xoff + "  yoff: " + yoff);

		try {
		    wucolors = mat._wu_array[wuoffset];
		} catch (Exception e) {
			System.out.println("Problem with _wu_array: " + xoff + " "  + yoff);
			e.printStackTrace();
		}

		wu_00 = (wucolors>>24)&255;
		wu_01 = (wucolors>>16)&255;
		wu_10 = (wucolors>>8)&255;
		wu_11 = (wucolors)&255;
		wu_red = (mat._color>>16)&255;
		wu_green = (mat._color>>8)&255;
		wu_blue = (mat._color)&255;

		//	System.err.println("Before intensity: ");
		//	System.err.println("wu_00: " + wu_00 + "  wu_01: " + wu_01 + "  wu_10: " + wu_10 + "  wu_11: " + wu_11);
		//	System.err.println("red: " + wu_red + "  green: " + wu_green + "  blue: " + wu_blue);

		/*
		float intensity = v.invz;
		if (intensity<(float)1.0 && intensity>(float)0.0) {
			wu_00 = (int)((float)wu_00 * intensity);
			wu_01 = (int)((float)wu_01 * intensity);
			wu_10 = (int)((float)wu_10 * intensity);
			wu_11 = (int)((float)wu_11 * intensity);
		}
		*/

		//	System.err.println("After intensity: ");
		//	System.err.println("wu_00: " + wu_00 + "  wu_01: " + wu_01 + "  wu_10: " + wu_10 + "  wu_11: " + wu_11);

		try {

            if ( v.zbuf > _zbuf[ pixel] && v.x>=0 && v.x<(_width-1) && v.y>=0 && v.y<(_height-1) ) {
            
                color = _pix[ pixel];
                red   = ((color>>16)&255) + ((wu_red  *wu_00)>>8);
                green = ((color>> 8)&255) + ((wu_green*wu_00)>>8);
                blue  = ((color    )&255) + ((wu_blue *wu_00)>>8);
                if (red   > 255) red   = 255;
                if (green > 255) green = 255;
                if (blue  > 255) blue  = 255;

                //	    System.err.println("r: " + red + " g: " + green + " b: " + blue);
                _pix[pixel] = (255<<24) + (red<<16) + (green<<8) + blue;

                color = _pix[ pixel+1];
                red   = ((color>>16)&255) + ((wu_red  *wu_01)>>8);
                green = ((color>> 8)&255) + ((wu_green*wu_01)>>8);
                blue  = ((color    )&255) + ((wu_blue *wu_01)>>8);
                if (red   > 255) red   = 255;
                if (green > 255) green = 255;
                if (blue  > 255) blue  = 255;
                _pix[pixel+1] = (255<<24) + (red<<16) + (green<<8) + blue;
                
                color = _pix[ pixel+_width];
                red   = ((color>>16)&255) + ((wu_red  *wu_10)>>8);
                green = ((color>> 8)&255) + ((wu_green*wu_10)>>8);
                blue  = ((color    )&255) + ((wu_blue *wu_10)>>8);
                if (red   > 255) red   = 255;
                if (green > 255) green = 255;
                if (blue  > 255) blue  = 255;
                _pix[pixel+_width] = (255<<24) + (red<<16) + (green<<8) + blue;
                
                color = _pix[ pixel+_width+1];
                red   = ((color>>16)&255) + ((wu_red  *wu_11)>>8);
                green = ((color>> 8)&255) + ((wu_green*wu_11)>>8);
                blue  = ((color    )&255) + ((wu_blue *wu_11)>>8);
                if (red   > 255) red   = 255;
                if (green > 255) green = 255;
                if (blue  > 255) blue  = 255;
                _pix[pixel+_width+1] = (255<<24) + (red<<16) + (green<<8) + blue;
                
            }
		} catch (Exception e) {
			System.out.println("Problem plotting wu point: " + v.x + " " + v.y);
		}
    }

}