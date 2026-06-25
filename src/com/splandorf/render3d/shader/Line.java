package com.splandorf.render3d.shader;

public class Line extends Shader
{
	/** 
	public static void solidLine( Edge e, int color)
	{
		// Make sure x2 lies to the right of x1
		Vertex v1 = e.v1;
		Vertex v2 = e.v2;
		Vertex temp;
		if (v1.x > v2.x) {
			temp = v1;
			v1 = v2;
			v2 = temp;
		}

		int x = v1.x;
		int y = v1.y;
		int z = v1.z;
		int incrementor = 0;

		int dx = v2.x - v1.x;
		int dy = v2.y - v1.y;
		int dz = v2.z - v1.z;

		if (dx == 0 && dy == 0) return;

		int pixel = x + y * _width;

		// Top two cases: slope >1 or slope <1, but dY+
		if (dy>0) {
			
			// Upper right quarter, slope >1
			if (dy > dx) {

				dz /= dy;
				while (y<=v2.y) {
					if (x>=0 && x<_width && y>=0 && y<_height) {
						if ( z < _zbuf[ pixel ] ) { 
							try {
								_pix [ pixel ] = color;
								_zbuf[ pixel ] = z;
							} catch (Exception j) {
								System.err.println("1: " + x + " " + y + " " + pixel);
							}
						}
					}
					y++;
					z += dz;
					pixel += _width;
					incrementor += dx;
					if (incrementor >= dy) {
						incrementor -= dy;
						pixel++;
						x++;
					}
				}
					
			// Upper right quarter, slope <1
			} else {

				dz /= dx;
				while (x<=v2.x) {
					if (x>=0 && x<_width && y>=0 && y<_height) {
						if ( z < _zbuf[ pixel ] ) { 
							try {
								_pix [ pixel ] = color;
								_zbuf[ pixel ] = z;
							} catch (Exception j) {
								System.err.println("1: " + x + " " + y + " " + pixel);
							}
						}
					}
					x++;
					z += dz;
					pixel++;
					incrementor += dy;
					if (incrementor >= dx) {
						incrementor -= dx;
						y++;
						pixel += _width;
					}
				}
			}

		// Bottom two cases: slope >-1 or slope <-1, but dY-
		} else {

			// Lower right quarter, slope <-1
			if (dx < -dy) {

				dz /= -dy;
				while (y>=v2.y) {
					if (x>=0 && x<_width && y>=0 && y<_height) {
						if ( z < _zbuf[ pixel ] ) { 
							try {
								_pix [ pixel ] = color;
								_zbuf[ pixel ] = z;
							} catch (Exception j) {
								System.err.println("1: " + x + " " + y + " " + pixel);
							}
						}
					}
					y--;
					z += dz;
					pixel -= _width;
					incrementor += dx;
					if (incrementor >= -dy) {
						incrementor -= -dy;
						x++;
						pixel++;
					}
				}

			// Lower right quarter, slope >-1
			} else {

				dz /= dx;
				while (x<=v2.x) {
					if (x>=0 && x<_width && y>=0 && y<_height) {
						if ( z < _zbuf[ pixel ] ) { 
							try {
								_pix [ pixel ] = color;
								_zbuf[ pixel ] = z;
							} catch (Exception j) {
								System.err.println("1: " + x + " " + y + " " + pixel);
							}
						}
					}
					x++;
					z += dz;
					pixel++;
					incrementor += -dy;
					if (incrementor >= dx) {
						incrementor -= dx;
						y--;
						pixel -= _width;
					}
				}
			}
		}
	}
		*/

	public static void drawSolidLine( Edge e, int color)
    {
		// Make sure x2 lies to the right of x1
		Vertex v1 = e.v1;
		Vertex v2 = e.v2;
		Vertex temp;
		if (v1.x > v2.x) {
			temp = v1;
			v1 = v2;
			v2 = temp;
		}
		
		int x = v1.x;
		int y = v1.y;
		int z = v1.zbuf;
		int incrementor = 0;
		
		int dx = v2.x - v1.x;
		int dy = v2.y - v1.y;
		int dz = v2.zbuf - v1.zbuf;
		
		if (dx == 0 && dy == 0) {
			return;
		}
		int pixel = x + y * _width;
		
		// Top two cases: slope >1 or slope <1, but dY+
		if (dy>0) {
			
			// Upper right quarter, slope >1
			if (dy > dx) {
				
				dz /= dy;

				dx = dx * 65536 / dy;
				dy = 65536;

				while (y<=v2.y) {
					if (x>=2 && x<_width-2 && y>=2 && y<_height-2) {
						if ( z > _zbuf[ pixel ] ) {
							try {
								_pix [ pixel ] = color;
								_zbuf[ pixel ] = z;
							} catch (Exception j) {
								System.err.println("1: " + x + " " + y + " " + pixel);
							}
						}
					}
					y++;
					z += dz;
					pixel += _width;
					incrementor += dx;
					if (incrementor >= dy) {
						incrementor -= dy;
						pixel++;
						x++;
					}
				}
			
			// Upper right quarter, slope <1
			} else {
			
				dz /= dx;
				dy = dy * 65536 / dx;
				dx = 65536;
				while (x<=v2.x) {
					if (x>=2 && x<_width-2 && y>=2 && y<_height-2) {
						if ( z > _zbuf[ pixel ] ) {
							try {
								_pix [ pixel ] = color;
								_zbuf[ pixel ] = z;
							} catch (Exception j) {
								System.err.println("2: " + x + " " + y + " " + pixel);
							}
						}
					}
					x++;
					z += dz;
					pixel++;
					incrementor += dy;
					if (incrementor >= dx) {
						incrementor -= dx;
						y++;
						pixel += _width;
					}
				}
			}
			
			// Bottom two cases: slope >-1 or slope <-1, but dY-
		} else {
			
			// Lower right quarter, slope <-1
			if (dx < -dy) {
			
			dy *= -1;
			dz /= dy;
			dx = dx * 65536 / dy;
			dy = 65536;
			while (y>=v2.y) {
				if (x>=2 && x<_width-2 && y>=2 && y<_height-2) {
					if ( z > _zbuf[ pixel ] ) {
						try {
							_pix [ pixel ] = color;
							_zbuf[ pixel ] = z;
						} catch (Exception j) {
							System.err.println("3: " + x + " " + y + " " + pixel);
						}
					}
				}
				y--;
				z += dz;
				pixel -= _width;
				incrementor += dx;
				if (incrementor >= dy) {
					incrementor -= dy;
					x++;
					pixel++;
				}
			}
			
			// Lower right quarter, slope >-1
			} else {
			
				dz /= dx;
				dy = dy * 65536 / dx;
				dx = 65536;
				while (x<=v2.x) {
					if (x>=2 && x<_width-2 && y>=2 && y<_height-2) {
						if ( z > _zbuf[ pixel ] ) {
							try {
								_pix [ pixel ] = color;
								_zbuf[ pixel ] = z;
							} catch (Exception j) {
								System.err.println("4: " + x + " " + y + " " + pixel);
							}
						}
					}	
					x++;
					z += dz;
					pixel++;
					incrementor += -dy;
					if (incrementor >= dx) {
						incrementor -= dx;
						y--;
						pixel -= _width;
					}
				}
			}
		}
    }

    public static void drawWuLine( Edge e, int color)
    {
		// Make sure x2 lies to the right of x1
		Vertex v1 = e.v1;
		Vertex v2 = e.v2;
		Vertex temp;
		if (v1.x > v2.x) {
			temp = v1;
			v1 = v2;
			v2 = temp;
		}
		int r, g, b;
		int red   = (color>>16) & 255;
		int green = (color>>8) & 255;
		int blue  = (color) & 255;
		int x = v1.x;
		int y = v1.y;
		int z = v1.zbuf;
		int incrementor = 0;
		int contrib;
		int pcol;
		
		int dx = v2.x - v1.x;
		int dy = v2.y - v1.y;
		int dz = v2.zbuf - v1.zbuf;
		
		if (dx == 0 && dy == 0) return;
		
		int pixel = x + y * _width;
		
		// Top two cases: slope >1 or slope <1, but dY+
		if (dy>0) {
			
			// Upper right quarter, slope >1
			if (dy > dx) {
			
			dz /= dy;

			dx = dx * 65536 / dy;
			dy = 65536;

			while (y<=v2.y) {
				if (x>=2 && x<_width-2 && y>=2 && y<_height-2) {
					if ( z > _zbuf[ pixel ] ) {
						try {
							contrib = 255 - (incrementor>>8);
							pcol = _pix [ pixel ];
							// Blend line color with background color by ratio (contrib/255)
							r = ( red   * contrib + ((pcol>>16)&255) * (255-contrib)) >> 8;
							g = ( green * contrib + ((pcol>>8 )&255) * (255-contrib)) >> 8;
							b = ( blue  * contrib + ( pcol     &255) * (255-contrib)) >> 8;
							_pix [ pixel ] = (255<<24) + (r<<16) + (g<<8) + b;

							contrib = 255 - contrib;
							pcol = _pix [ pixel +1];
							// Blend line color with background color by ratio (contrib/255)
							r = ( red   * contrib + ((pcol>>16)&255) * (255-contrib)) >> 8;
							g = ( green * contrib + ((pcol>>8 )&255) * (255-contrib)) >> 8;
							b = ( blue  * contrib + ( pcol     &255) * (255-contrib)) >> 8;
							_pix [ pixel +1] = (255<<24) + (r<<16) + (g<<8) + b;
						} catch (Exception j) {
						S	ystem.err.println("1: " + x + " " + y + " " + pixel);
						}
					}
				}
				y++;
				z += dz;
				pixel += _width;
				incrementor += dx;
				if (incrementor >= dy) {
					incrementor -= dy;
					pixel++;
					x++;
				}
			}
			
			// Upper right quarter, slope <1
			} else {
			
				dz /= dx;
				dy = dy * 65536 / dx;
				dx = 65536;
				while (x<=v2.x) {
					if (x>=2 && x<_width-2 && y>=2 && y<_height-2) {
						if ( z > _zbuf[ pixel ] ) {
							try {

								contrib = 255 - (incrementor>>8);
								pcol = _pix [ pixel ];
								// Blend line color with background color by ratio (contrib/255)
								r = ( red   * contrib + ((pcol>>16)&255) * (255-contrib)) >> 8;
								g = ( green * contrib + ((pcol>>8 )&255) * (255-contrib)) >> 8;
								b = ( blue  * contrib + ( pcol     &255) * (255-contrib)) >> 8;
								_pix [ pixel ] = (255<<24) + (r<<16) + (g<<8) + b;

								contrib = 255 - contrib;
								pcol = _pix [ pixel + _width];
								// Blend line color with background color by ratio (contrib/255)
								r = ( red   * contrib + ((pcol>>16)&255) * (255-contrib)) >> 8;
								g = ( green * contrib + ((pcol>>8 )&255) * (255-contrib)) >> 8;
								b = ( blue  * contrib + ( pcol     &255) * (255-contrib)) >> 8;
								_pix [ pixel + _width] = (255<<24) + (r<<16) + (g<<8) + b;

							} catch (Exception j) {
								System.err.println("2: " + x + " " + y + " " + pixel);
							}
						}
					}
					x++;
					z += dz;
					pixel++;
					incrementor += dy;
					if (incrementor >= dx) {
						incrementor -= dx;
						y++;
						pixel += _width;
					}
				}
			}
			
		// Bottom two cases: slope >-1 or slope <-1, but dY-
		} else {
			
			// Lower right quarter, slope <-1
			if (dx < -dy) {
				dy *= -1;
				dz /= dy;
				dx = dx * 65536 / dy;
				dy = 65536;
				while (y>=v2.y) {
					if (x>=2 && x<_width-2 && y>=2 && y<_height-2) {
						if ( z > _zbuf[ pixel ] ) {
							try {
								contrib = 255 - (incrementor>>8);
								pcol = _pix [ pixel ];
								// Blend line color with background color by ratio (contrib/255)
								r = ( red   * contrib + ((pcol>>16)&255) * (255-contrib)) >> 8;
								g = ( green * contrib + ((pcol>>8 )&255) * (255-contrib)) >> 8;
								b = ( blue  * contrib + ( pcol     &255) * (255-contrib)) >> 8;
								_pix [ pixel ] = (255<<24) + (r<<16) + (g<<8) + b;

								contrib = 255 - contrib;
								pcol = _pix [ pixel + 1];
								// Blend line color with background color by ratio (contrib/255)
								r = ( red   * contrib + ((pcol>>16)&255) * (255-contrib)) >> 8;
								g = ( green * contrib + ((pcol>>8 )&255) * (255-contrib)) >> 8;
								b = ( blue  * contrib + ( pcol     &255) * (255-contrib)) >> 8;
								_pix [ pixel + 1] = (255<<24) + (r<<16) + (g<<8) + b;
							} catch (Exception j) {
								System.err.println("3: " + x + " " + y + " " + pixel);
							}	
						}
					}
					y--;
					z += dz;
					pixel -= _width;
					incrementor += dx;
					if (incrementor >= dy) {
						incrementor -= dy;
						x++;
						pixel++;
					}
				}
			
			// Lower right quarter, slope >-1
			} else {
			
				dz /= dx;
				dy = dy * 65536 / dx;
				dx = 65536;
				while (x<=v2.x) {
					if (x>=2 && x<_width-2 && y>=2 && y<_height-2) {
						if ( z > _zbuf[ pixel ] ) {
							try {
								contrib = 255 - (incrementor>>8);
								pcol = _pix [ pixel ];
								// Blend line color with background color by ratio (contrib/255)
								r = ( red   * contrib + ((pcol>>16)&255) * (255-contrib)) >> 8;
								g = ( green * contrib + ((pcol>>8 )&255) * (255-contrib)) >> 8;
								b = ( blue  * contrib + ( pcol     &255) * (255-contrib)) >> 8;
								_pix [ pixel ] = (255<<24) + (r<<16) + (g<<8) + b;

								contrib = 255 - contrib;
								pcol = _pix [ pixel - _width];
								// Blend line color with background color by ratio (contrib/255)
								r = ( red   * contrib + ((pcol>>16)&255) * (255-contrib)) >> 8;
								g = ( green * contrib + ((pcol>>8 )&255) * (255-contrib)) >> 8;
								b = ( blue  * contrib + ( pcol     &255) * (255-contrib)) >> 8;
								_pix [ pixel - _width] = (255<<24) + (r<<16) + (g<<8) + b;
							} catch (Exception j) {
								System.err.println("4: " + x + " " + y + " " + pixel);
							}
						}
					}
					x++;
					z += dz;
					pixel++;
					incrementor += -dy;
					if (incrementor >= dx) {
						incrementor -= dx;
						y--;
						pixel -= _width;
					}
				}
			}
		}
    }

    
    
    public static void drawAntiAliasedLine( Edge e, float thick, int color)
    {
		// Make sure x2 lies to the right of x1
		Vertex v1 = e.v1;
		Vertex v2 = e.v2;
		Vertex temp;
		if (v1.x > v2.x) {
			temp = v1;
			v1 = v2;
			v2 = temp;
		}
		int r, g, b;
		int red   = (color>>16)&255;
		int green = (color>>8) & 255;
		int blue  = (color) & 255;
		int pcol;
		
		int x = v1.x;
		int y = v1.y;
		int z = v1.zbuf;
		int incrementor = 0;
		
		int dx = v2.x - v1.x;
		int dy = v2.y - v1.y;
		int dz = v2.zbuf - v1.zbuf;
		
		if (dx == 0 && dy == 0) return;
		
		// Calculate the amount of pixel intensity
		// we need to increase by as a function of
		// line slope.  I.e., there are as many pixels
		// in a 45' line w/ a given dx as in a horizontal
		// one with the same dx, even though the 45' line
		// is sqrt(2.0) times longer.  Therefore, each pixel
		// on the 45' line appears to be 1.0/(sqrt(2.0)) times
		// dimmer.  Thus we need to increase the intensity of
		// pixels on 45' lines by a factor of 1.0/(sqrt(2.0)).
		double hypot = (float)0.0;
		int ax = Math.abs(dx);
		int ay = Math.abs(dy);
		if ( ax > ay ) {
			hypot = (float)ay / (float)ax;
		} else if (ay > ax) {
			hypot = (float)ax / (float)ay;
		} else if (ay == ax) {
			hypot = 1.0;
		}
		hypot = Math.sqrt( hypot*hypot + (float)1.0);
		int thickness = (int)((float)255.0 * thick * hypot);
		//int adj = thickness / 512;
		int adj = (thickness)/512;
		//System.err.print(adj + " " );
		//adj = 0;
		int nextpix;
		int pixel = x + y * _width;
		
		
		
		// Top two cases: slope >1 or slope <1, but dY+
		if (dy>0) {
			
			// Upper right quarter, slope >1
			if (dy > dx) {
			
				pixel -= adj;
				dz /= dy;
				
				int bigint = 65536 / dy;
				dx *= bigint;
				dy *= bigint;
				int contrib;  // intensity contribution for current pixel
				int rem;  // remainder for this column/row's intensity
				
				while (y<=v2.y) {
					if (x>=4 && x<_width-4 && y>=4 && y<_height-4) {
						if ( z > _zbuf[ pixel ] ) {
							contrib = 255-(incrementor>>8);
							rem = thickness;
							nextpix = 0;
							while (contrib > 0) {
								rem -= contrib;
								if (contrib < 255) {
									pcol = _pix [ pixel  + nextpix];
									// Blend line color with background color by ratio (contrib/255)
									r = ( red   * contrib + ((pcol>>16)&255) * (255-contrib)) >> 8;
									g = ( green * contrib + ((pcol>>8 )&255) * (255-contrib)) >> 8;
									b = ( blue  * contrib + ( pcol     &255) * (255-contrib)) >> 8;
									_pix [ pixel + nextpix ] = (255<<24) + (r<<16) + (g<<8) + b;
								} else {
									_pix [ pixel + nextpix ] = color;
								}
								//_zbuf[ pixel + nextpix ] = z;
								contrib = rem;
								if (contrib > 255) {
									contrib = 255;
								}
								nextpix += 1;
							}
						}
					}
					y++;
					z += dz;
					pixel += _width;
					incrementor += dx;
					if (incrementor >= dy) {
					incrementor -= dy;
					pixel++;
					x++;
					}
				}
				
				// Upper right quarter, slope <1
				} else {
				
					pixel -= adj * _width;
					
					dz /= dx;
					int bigint = 65536 / dx;
					dx *= bigint;
					dy *= bigint;
					int contrib;
					int rem;
					
					while (x<=v2.x) {
						if (x>=4 && x<_width-4 && y>=4 && y<_height-4) {
							if ( z > _zbuf[ pixel ] ) {
								contrib = 255-(incrementor>>8);
								rem = thickness;
								nextpix = 0;
								while (contrib > 0) {
									rem -= contrib;
									if (contrib < 255) {
										pcol = _pix [ pixel  + nextpix];
										// Blend line color with background color by ratio (contrib/255)
										r = ( red   * contrib + ((pcol>>16)&255) * (255-contrib)) >> 8;
										g = ( green * contrib + ((pcol>>8 )&255) * (255-contrib)) >> 8;
										b = ( blue  * contrib + ( pcol     &255) * (255-contrib)) >> 8;
										_pix [ pixel + nextpix ] = (255<<24) + (r<<16) + (g<<8) + b;
									} else {
										_pix [ pixel + nextpix ] = color;
									}
									//_zbuf[ pixel + nextpix ] = z;
									contrib = rem;
									if (contrib > 255) {
										contrib = 255;
									}
									nextpix += _width;
								}
							}
						}
						x++;
						z += dz;
						pixel++;
						incrementor += dy;
						if (incrementor >= dx) {
							incrementor -= dx;
							y++;
							pixel += _width;
						}
					}
				}
				
				// Bottom two cases: slope >-1 or slope <-1, but dY-
			} else {
				
				// Lower right quarter, slope <-1
				if (dx < -dy) {
				
					pixel -= adj;
					
					dz /= -dy;
					int bigint = 65536 / -dy;
					dx *= bigint;
					dy *= bigint;
					int contrib;
					int rem;
					
					while (y>=v2.y) {
						if (x>=4 && x<_width-4 && y>=4 && y<_height-4) {
							if ( z > _zbuf[ pixel ] ) {
								contrib = 255-(incrementor>>8);
								rem = thickness;
								nextpix = 0;
								while (contrib > 0) {
									rem -= contrib;
									if (contrib < 255) {
										pcol = _pix [ pixel  + nextpix];
										// Blend line color with background color by ratio (contrib/255)
										r = ( red   * contrib + ((pcol>>16)&255) * (255-contrib)) >> 8;
										g = ( green * contrib + ((pcol>>8 )&255) * (255-contrib)) >> 8;
										b = ( blue  * contrib + ( pcol     &255) * (255-contrib)) >> 8;
										_pix [ pixel + nextpix ] = (255<<24) + (r<<16) + (g<<8) + b;
									} else {
										_pix [ pixel + nextpix ] = color;
									}
									//_zbuf[ pixel + nextpix ] = z;
									contrib = rem;
									if (contrib > 255) {
										contrib = 255;
									}
									nextpix += 1;
								}
							}
						}
						y--;
						z += dz;
						pixel -= _width;
						incrementor += dx;
						if (incrementor >= -dy) {
							incrementor -= -dy;
							x++;
							pixel++;
						}
					}
				
				// Lower right quarter, slope >-1
				} else {
				
				pixel += adj * _width;
				
				dz /= dx;
				int bigint = 65536 / dx;
				dx *= bigint;
				dy *= bigint;
				int contrib;
				int rem;
				
				while (x<=v2.x) {
					if (x>=4 && x<_width-4 && y>=4 && y<_height-4) {
						if ( z > _zbuf[ pixel ] ) {
							contrib = 255-(incrementor>>8);
							rem = thickness;
							nextpix = 0;
							while (contrib > 0) {
								rem -= contrib;
								if (contrib < 255) {
									pcol = _pix [ pixel + nextpix ];
									// Blend line color with background color by ratio (contrib/255)
									r = ( red   * contrib + ((pcol>>16)&255) * (255-contrib)) >> 8;
									g = ( green * contrib + ((pcol>>8 )&255) * (255-contrib)) >> 8;
									b = ( blue  * contrib + ( pcol     &255) * (255-contrib)) >> 8;
									_pix [ pixel +nextpix ] = (255<<24) + (r<<16) + (g<<8) + b;
								} else {
									_pix [ pixel + nextpix ] = color;
								}
								//_zbuf[ pixel + nextpix ] = z;
								contrib = rem;
								if (contrib > 255) {
									contrib = 255;
								}
								nextpix -= _width;
							}
						}
					}
					x++;
					z += dz;
					pixel++;
					incrementor += -dy;
					if (incrementor >= dx) {
						incrementor -= dx;
						y--;
						pixel -= _width;
					}
				}
			}
		}
    }	

	public static void drawColorLine( int x1, int y1, int x2, int y2, float z1, float z2, Material mat)
	{
		// Make sure x2 lies to the right of x1
		int temp_x;
		int temp_y;
		float temp_z;
		if (x2 < x1) {
			temp_x = x2;
			x2 = x1;
			x1 = temp_x;
			temp_y = y2;
			y2 = y1;
			y1 = temp_y;
			temp_z = z2;
			z2 = z1;
			z1 = temp_z;
		}

		int x = x1;
		int y = y1;
		float z = z1;
		int pixel = x + y * _width;
		int incrementor = 0;

		int dx = x2 - x1;
		int dy = y2 - y1;
		float dz = (float)0.0;
		int zdepth = 0;

		// Top two cases: slope >1 or slope <1, but dY+
		if (dy>0) {
			
			// Upper right quarter, slope >1
			if (dy > dx) {

				dz = (z2-z1) / (float)dy;
				while (y<=y2) {
					zdepth = (int)(z/(float)1000.0 * (float)(Integer.MAX_VALUE));
					if ( _zbuf[ pixel] > zdepth) {
						_zbuf[ pixel] = zdepth;
						if (z>mat._cue_far) {
							_pix[ pixel ] = mat._color_blend[255];
						} else if (z<mat._cue_near) {
							_pix[ pixel ] = mat._color_blend[0];
						} else {
							_pix[ pixel ] = mat._color_blend[ (int)( (float)255.0*(z-mat._cue_near)/(mat._cue_far-mat._cue_near) ) ];
						}
					}
					y++;
					pixel += _width;
					z += dz;
					incrementor += dx;
					if (incrementor >= dy) {
						incrementor -= dy;
						pixel++;
					}
				}
					
			// Upper right quarter, slope <1
			} else {

				dz = (z2-z1) / (float)dx;
				while (x<=x2) {
					zdepth = (int)(z/(float)1000.0 * (float)(Integer.MAX_VALUE));
					if ( _zbuf[ pixel] > zdepth) {
						_zbuf[ pixel] = zdepth;
						if (z>mat._cue_far) {
							_pix[ pixel ] = mat._color_blend[255];
						} else if (z<mat._cue_near) {
							_pix[ pixel ] = mat._color_blend[0];
						} else {
							_pix[ pixel ] = mat._color_blend[ (int)( (float)255.0*(z-mat._cue_near)/(mat._cue_far-mat._cue_near) ) ];
						}
					}
					x++;
					pixel++;
					z += dz;
					incrementor += dy;
					if (incrementor >= dx) {
						incrementor -= dx;
						y++;
						pixel += _width;
					}
				}
			}

		// Bottom two cases: slope >-1 or slope <-1, but dY-
		} else {

			// Lower right quarter, slope <-1
			if (dx < -dy) {

				dz = (z2-z1) / (float)-dy;
				while (y>=y2) {
					zdepth = (int)(z/(float)1000.0 * (float)(Integer.MAX_VALUE));
					if ( _zbuf[ pixel] > zdepth) {
						_zbuf[ pixel] = zdepth;
						if (z>mat._cue_far) {
							_pix[ pixel ] = mat._color_blend[255];
						} else if (z<mat._cue_near) {
							_pix[ pixel ] = mat._color_blend[0];
						} else {
							_pix[ pixel ] = mat._color_blend[ (int)( (float)255.0*(z-mat._cue_near)/(mat._cue_far-mat._cue_near) ) ];
						}
					}
					y--;
					pixel -= _width;
					z += dz;
					incrementor += dx;
					if (incrementor >= -dy) {
						incrementor -= -dy;
						x++;
						pixel++;
					}
				}

			// Lower right quarter, slope >-1
			} else {

				dz = (z2-z1) / (float)dx;
				while (x<=x2) {
					zdepth = (int)(z/(float)1000.0 * (float)(Integer.MAX_VALUE));
					if ( _zbuf[ pixel] > zdepth) {
						_zbuf[ pixel] = zdepth;
						if (z>mat._cue_far) {
							_pix[ pixel ] = mat._color_blend[255];
						} else if (z<mat._cue_near) {
							_pix[ pixel ] = mat._color_blend[0];
						} else {
							_pix[ pixel ] = mat._color_blend[ (int)( (float)255.0*(z-mat._cue_near)/(mat._cue_far-mat._cue_near) ) ];
						}
					}
					x++;
					pixel++;
					z += dz;
					incrementor += -dy;
					if (incrementor >= dx) {
						incrementor -= dx;
						y--;
						pixel -= _width;
					}
				}
			}
		}
	}



}

//==========================