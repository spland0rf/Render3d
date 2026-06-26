package com.splandorf.render3d.scene;

import com.splandorf.render3d.math.*;
import com.splandorf.render3d.MemMgr;

public class GeometryFactory
{
	public static Obj makeSphere( int long_res, int lat_res, float s_start, float s_end,
		float t_start, float t_end)
	{
		int n_pts = long_res+2;
		float [] y = new float[ n_pts ];
		float [] r = new float[ n_pts ];
		
		double long_step = Math.PI / (double)(n_pts-1);
		double longi     = 0.0;

		for (int i=0; i<n_pts; i++) {
			y[i] = (float)(Math.cos( longi));
			r[i] = (float)(Math.sin( longi));
			longi += long_step;
		}

		return makeOpenRevolve( lat_res, y, r, s_start, s_end, t_start, t_end);

	}

	public static Obj makeTorus( int long_res, int lat_res, float inner_rad, float outer_rad, float s_start, float s_end,
		float t_start, float t_end)
	{
		int n_pts = long_res;
		float [] y = new float[ n_pts ];
		float [] r = new float[ n_pts ];
		
		double long_step = Math.PI * (double)2.0 / (double)n_pts;
		double longi     = 0.0;

		for (int i=0; i<n_pts; i++) {
			y[i] = (float)(Math.cos( longi) * outer_rad);
			r[i] = inner_rad + (float)(Math.sin( longi) * outer_rad);
			longi += long_step;
		}

		return makeClosedRevolve( lat_res, y, r, s_start, s_end, t_start, t_end);
	}

	public static Obj makeRandomPointfield( int n_points, float radius)
    {
		float x, y, z;
		Obj pointfield = MemMgr.Obj();
		for ( int i=0; i<n_points; i++) {
			x = ((float)(Math.random()) * (float)2.0 * radius) - radius;
			y = ((float)(Math.random()) * (float)2.0 * radius) - radius;
			z = ((float)(Math.random()) * (float)2.0 * radius) - radius;
			pointfield.addVertex( MemMgr.Vertex( x, y, z) );
		}
		return pointfield;
    }

	public static int[][] makeGaussianDotArrays( int resolution, int step_size)
    {
		int [][] dot_array = new int[step_size][resolution*resolution];
		double radius, distance, intensity, distX, distY;

		for (int k=0; k<step_size; k++) {

			radius = (double)(step_size-k) / (double)step_size;
			
			for (int j=0; j<resolution; j++) {
			
			for (int i=0; i<resolution; i++) {
				
				distX = ((double)i - (double)(resolution/2)) / (double)(resolution/2);
				distY = ((double)j - (double)(resolution/2)) / (double)(resolution/2) ;
				distance = Math.sqrt( distX*distX + distY*distY) * 0.6;
				if (distance < radius) {
				intensity = 1.0 - (distance/radius);
				double inten2 = intensity*intensity;

				//			intensity = (inten2*inten2*intensity) *2.0;
				/*
				if (intensity<0.05) {
					intensity += intensity;
				} else {
					intensity += 0.05;
				}
				*/
				
				//			intensity *= intensity;
				//			intensity *= intensity;
				intensity = inten2 * inten2;
				intensity *= 1.5;
				
				if (intensity > 1.0) intensity=1.0;
				} else {
				intensity = 0;
				}
				dot_array[k][j*resolution+i] = (int)(255.0  * intensity);
			}
			}
		}
		return dot_array;
    }

    public static int[] makeWuUnitPointArray( int resolution)
    {
		int [] wu_array = new int[ resolution * resolution];

		int wu_00, wu_01, wu_10, wu_11;

		for (int j=0; j<resolution; j++) {
			
			for (int i=0; i<resolution; i++) {

				wu_00 = ((resolution-i) * (resolution-j))*4;
				wu_01 = ((resolution-i) * j) * 4;
				wu_10 = (i * (resolution-j)) * 4;
				wu_11 = (i * j) * 4;
				if (wu_00 == 256) wu_00 = 255;
				if (wu_01 == 256) wu_01 = 255;
				if (wu_10 == 256) wu_10 = 255;
				if (wu_11 == 256) wu_11 = 255;
				
				//		System.out.println("wu_00: " + wu_00 + "  wu_01: " + wu_01 + "  wu_10: " + wu_10 + "  wu_11: " + wu_11 + "  sum: " + (wu_00 + wu_01 + wu_10 + wu_11));

				wu_array[ j*resolution + i] = (wu_00<<24) + (wu_01<<16) + (wu_10<<8) + (wu_11) ;

			}

		}

		return wu_array;
    }
    

    


	public static Obj makeOpenRevolve(int res, float [] y, float [] r, float s_start, float s_end,
		float t_start, float t_end)
	{
		int lati = y.length-2;
		int longi  = res;
		Obj obj = MemMgr.Obj();

		// North and South pole points
		Vertex n_pole = MemMgr.Vertex();
		n_pole.p = MemMgr.Vec3f( (float)0.0, y[0], (float)0.0);
		Vertex s_pole = MemMgr.Vertex();
		s_pole.p = MemMgr.Vec3f( (float)0.0, y[y.length-1], (float)0.0);
		int N_POLE = longi * lati;
		int S_POLE = longi * lati + 1;

		double longi_step = Math.PI * 2.0  / (double)(longi);

		// Grid of vertices, [col,row] = [longi,lati]
		Vertex [] verts = new Vertex[ longi * lati];
		for (int j=0; j<lati; j++) {
			for (int i=0; i<longi; i++) {
				verts[i+longi*j]   = MemMgr.Vertex();
				verts[i+longi*j].p = MemMgr.Vec3f(
					(float)(Math.sin( (double)i * longi_step)) * r[j+1],
					y[j+1],
					(float)(Math.cos( (double)i * longi_step)) * r[j+1]
					);
				obj.addVertex( verts[i+longi*j] );
			}
		}

		// Add north and south poles
		obj.addVertex( n_pole);
		obj.addVertex( s_pole);

		// Grid of edges (vertical, horizontal, and diagonal)
		// Note: v-edges have extra column, h-edges extra row
		Edge [] v_edges = new Edge[  longi    * (lati-1) ];
		Edge [] h_edges = new Edge[ (longi-1) *  lati    ];
		Edge [] d_edges = new Edge[ (longi-1) * (lati-1) ];
		Edge [] s_edges = new Edge[ 2 * lati - 1 ];
		Edge [] c1_edges = new Edge[ longi ];
		Edge [] c2_edges = new Edge[ longi ];


		// Edges are stored in indexed order, with the vertical set
		// added first, followed by the horizontal set, then the
		// diagonal set, then the seam edges (to zip the grid
		// back onto itself, turning it into a tube), and finally 
		// the endcap edges.
		// Since each set begins at the index after where the last
		// set ended, we must keep track (for each set) of the index
		// into the whole collection where each set begins.  This
		// allows easier indexing within a particular set.
		int V_EDGE = 0;
		// Offset into edge list by V_EDGE
		int H_EDGE = longi * (lati-1);
		// Offset into edge list by V_EDGE + H_EDGE
		int D_EDGE = H_EDGE + (longi-1) * lati;
		// Offset into the extra seam edges
		int S_EDGE = D_EDGE + (longi-1) * (lati-1);
		// Offset into the cap edges
		int C1_EDGE = S_EDGE + 2*lati - 1;
		int C2_EDGE = C1_EDGE + longi;

		// Vertical edges: [ longi * {lati-1) ]
		for (int j=0; j<lati-1; j++) {
			for (int i=0; i<longi; i++) {
				v_edges[i+longi*j] = MemMgr.Edge();
				obj.addEdge( 
					v_edges[i+longi*j],
					i + longi*j,
					i + longi*(j+1)
					);
			}
		}
		// Horizontal edges: [ (longi-1) * lati ]
		for (int j=0; j<lati; j++) {
			for (int i=0; i<longi-1; i++) {
				h_edges[i+(longi-1)*j] = MemMgr.Edge();
				obj.addEdge( 
					h_edges[i+(longi-1)*j],
					i   + longi*j,
					i+1 + longi*j
					);
			}
		}
		// Diagonal edges: [ (longi-1) * {lati-1) ]
		for (int j=0; j<lati-1; j++) {
			for (int i=0; i<longi-1; i++) {
				d_edges[i+(longi-1)*j] = MemMgr.Edge();
				obj.addEdge( 
					d_edges[i+(longi-1)*j],
					i   + longi*(j+1),
					i+1 + longi*j
					);
			}
		}
		// Seam edges. Four (instead of five) edges are
		// needed for each square face (2 triangles) because	
		// the rightmost edge of each square face is
		// actually a pre-exising edge borrowed from the 
		// very first left-most edge on the opposite side
		// of the grid. By using one edge from the right-most
		// side and one from the left-most side of the grid,
		// these new faces "zip-up" the 2D grid into a cylindrical
		// tube. 
		int k=0;
		for (; k<lati-1; k++)
		{
			// Add horizontal edge zipping seam from right-most
			// vertex back to left-most vertex
			s_edges[2*k] = MemMgr.Edge();
			obj.addEdge( 
				s_edges[2*k],
				// Right-most vertex in grid
				k*longi + longi-1,
				// Left-most vertex in grid
				k*longi
				);
			// Add diagonal edge zipping seam from right-most
			// vertex in grid to left-most one.
			s_edges[2*k+1] = MemMgr.Edge();
			obj.addEdge(
				s_edges[ 2*k+1 ],
				// Right vertex
				k*longi,
				// Left vertex
				(k+1)*longi + longi-1
				);
		}
		// Add one more edge at bottom
		s_edges[2*k] = MemMgr.Edge();
		obj.addEdge(
				s_edges[2*k],
				// Right-most vertex in grid
				k*longi + longi-1,
				// Left-most vertex in grid
				k*longi 
				);

		// Add cap edges.
		// North pole
		for (int i=0; i<longi; i++) {
			c1_edges[i] = MemMgr.Edge();
			obj.addEdge( c1_edges[i], i, N_POLE);
		}
		// South pole
		for (int i=0; i<longi; i++) {
			c2_edges[i] = MemMgr.Edge();
			obj.addEdge( c2_edges[i], (lati-1)*longi + i, S_POLE);
		}
				


		// Now add the faces.

		// Grid faces
		Triangle tri = null;

		for (int j=0; j<lati-1; j++) {
			for (int i=0; i<longi-1; i++) {
				tri = MemMgr.Triangle();
				tri.setTexture(
					(s_start + (s_end-s_start) * (float) i    / (float)longi),
					(t_start + (t_end-t_start) * (float)(j+1) / (float)(lati+1) ),
					(s_start + (s_end-s_start) * (float)(i+1) / (float)longi),
					(t_start + (t_end-t_start) * (float)(j+1) / (float)(lati+1) ),
					(s_start + (s_end-s_start) * (float) i    / (float)longi),
					(t_start + (t_end-t_start) * (float)(j+2) / (float)(lati+1) )
					);
				obj.addTriangle( 
					tri,
					i   +  j   *longi,
					i+1 +  j   *longi,
					i   + (j+1)*longi,
					H_EDGE + i   + j   *(longi-1),
					D_EDGE + i   + j   *(longi-1),
					V_EDGE + i   + j   * longi
					);
				tri = MemMgr.Triangle();
				tri.setTexture(
					(s_start + (s_end-s_start) * (float)(i+1) / (float)longi),
					(t_start + (t_end-t_start) * (float)(j+1) / (float)(lati+1) ),
					(s_start + (s_end-s_start) * (float)(i+1) / (float)longi),
					(t_start + (t_end-t_start) * (float)(j+2) / (float)(lati+1) ),
					(s_start + (s_end-s_start) * (float) i    / (float)longi),
					(t_start + (t_end-t_start) * (float)(j+2) / (float)(lati+1) )
					);
				obj.addTriangle( 
					tri,
					i+1 +  j   *longi,
					i+1 + (j+1)*longi,
					i   + (j+1)*longi,
					V_EDGE + i+1 + j   * longi,
					H_EDGE + i   +(j+1)*(longi-1),
					D_EDGE + i   + j   *(longi-1)
					);
			}
		}

		// Seam faces
		for (int j=0; j<lati-1; j++) {
			tri = MemMgr.Triangle();
			tri.setTexture(
				(s_start + (s_end-s_start) * (float)(longi-1)  / (float)longi),
				(t_start + (t_end-t_start) * (float)(j+1)      / (float)(lati+1) ),
				(s_end),
				(t_start + (t_end-t_start) * (float)(j+1)      / (float)(lati+1) ),
				(s_start + (s_end-s_start) * (float)(longi-1)  / (float)longi),
				(t_start + (t_end-t_start) * (float)(j+2)      / (float)(lati+1) )
				);
			obj.addTriangle( 
				tri,
				j*longi + longi-1,
				j*longi,
				(j+1)*longi + longi-1,
				S_EDGE + j*2,
				S_EDGE + j*2+1,
				V_EDGE + j * longi + longi-1
				);
			tri = MemMgr.Triangle();
			tri.setTexture(
				(s_end),
				(t_start + (t_end-t_start) * (float)(j+1)     / (float)(lati+1) ),
				(s_end),
				(t_start + (t_end-t_start) * (float)(j+2)     / (float)(lati+1) ),
				(s_start + (s_end-s_start) * (float)(longi-1) / (float)longi),
				(t_start + (t_end-t_start) * (float)(j+2)     / (float)(lati+1) )
				);
			obj.addTriangle( 
				tri,
				j*longi,
				(j+1)*longi,
				(j+1)*longi + longi-1,
				V_EDGE + j * longi,
				S_EDGE + (j+1)*2,
				S_EDGE + j*2+1
				);
		}

		// Endcap faces
		// North pole
		k=0;
		for (; k<longi-1; k++) {
			tri = MemMgr.Triangle();
			tri.setTexture(
				(s_start + (s_end-s_start) * (float) k    / (float)longi),
				(t_start + (t_end-t_start) * (float) 1.0  / (float)(lati+1) ),
				(s_start + (s_end-s_start) * (float)(2*k+1)  / (float)(2*longi)),
				(t_start ),
				(s_start + (s_end-s_start) * (float)(k+1) / (float)longi),
				(t_start + (t_end-t_start) * (float) 1.0  / (float)(lati+1) )
				);
			obj.addTriangle( 
				tri,
				k  ,
				N_POLE,
				k+1,
				C1_EDGE + k,
				C1_EDGE + k + 1,
				H_EDGE + k
				);
		}
		tri = MemMgr.Triangle();
		tri.setTexture(
			(s_start + (s_end-s_start) * (float) k    / (float)longi),
			(t_start + (t_end-t_start) * (float) 1.0  / (float)(lati+1) ),
			(s_start + (s_end-s_start) * (float)(2*k+1) / (float)(2*longi)),
			(t_start ),
			(s_end),
			(t_start + (t_end-t_start) * (float) 1.0  / (float)(lati+1) )
			);
		obj.addTriangle( 
			tri,
			k  ,
			N_POLE,
			0,
			C1_EDGE + k,
			C1_EDGE,
			S_EDGE
			);

		// South pole
		k=0;
		for (; k<longi-1; k++) {
			tri = MemMgr.Triangle();
			tri.setTexture(
				(s_start + (s_end-s_start) * (float)(k+1)   / (float)longi),
				(t_start + (t_end-t_start) * (float)(lati)  / (float)(lati+1) ),
				(s_start + (s_end-s_start) * (float)(2*k+1) / (float)(2*longi)),
				(t_end ),
				(s_start + (s_end-s_start) * (float) k      / (float)longi),
				(t_start + (t_end-t_start) * (float)(lati)  / (float)(lati+1) )
				);
			obj.addTriangle( 
				tri,
				(lati-1) * longi + k+1,
				S_POLE,
				(lati-1) * longi + k,
				C2_EDGE + k + 1,
				C2_EDGE + k,
				H_EDGE + (lati-1) * (longi-1) + k
				);
		}
		tri = MemMgr.Triangle();
		tri.setTexture(
			(s_end),
			(t_start + (t_end-t_start) * (float)(lati)  / (float)(lati+1) ),
			(s_start + (s_end-s_start) * (float)(2*k+1) / (float)(2*longi)),
			(t_end ),
			(s_start + (s_end-s_start) * (float) k      / (float)longi),
			(t_start + (t_end-t_start) * (float)(lati)  / (float)(lati+1) )
			);
		obj.addTriangle( 
			tri,
			(lati-1) * longi,
			S_POLE,
			(lati-1) * longi + k,
			C2_EDGE,
			C2_EDGE + k,
			S_EDGE + lati*2-2
			);


		obj.calcVertexNormals();

		return obj;
	}


	
	public static Obj makeClosedRevolve(int res, float [] y, float [] r, float s_start, float s_end,
		float t_start, float t_end)
	{
		int lati   = y.length;
		int longi  = res;
		double longi_step = Math.PI * 2.0  / (double)(longi);
		Obj obj = MemMgr.Obj();

		// Grid of vertices, [x,y] = [(longi+1),(lati+1)]
		int [] verts = new int[ (longi+1) * (lati+1)];
		Vertex vert = null;
		for (int j=0; j<lati; j++) {
			for (int i=0; i<longi; i++) {
				vert   = MemMgr.Vertex();
				vert.p = MemMgr.Vec3f(
					(float)(Math.sin( (double)i * longi_step)) * r[j],
					y[j],
					(float)(Math.cos( (double)i * longi_step)) * r[j]
					);
				verts[i+(longi+1)*j] = i+longi*j;
				obj.addVertex( vert );
			}
		}
		// Fill extra (bottom,right) row & column w/ copies 
		// of the vertices from the opposite side (top/left).  
		// This will have the effect of zipping up the edges
		// of the grid into a torus, but by using duplicate
		// copies of the same vertex on the edges, we can 
		// walk through a grid of (dimensions+1) making triangles
		// but wind up with a closed toroidal shape w/
		// resolution of mere (dimensions).  This means we
		// don't need to do a special case loop on the "seam" 
		// to zip up the edges.

		// extra column 
		for (int j=0; j<lati; j++) {
			verts[ longi + (longi+1)*j] = verts[ 0 + (longi+1)*j];
		}
		// extra row
		for (int i=0; i<longi; i++) {
			verts[ i + (longi+1)*lati] = verts[ i];
		}
		// extra corner
		verts[ (lati+1)*(longi+1)-1] = verts[ 0 ];

		// Grid of edges (vertical, horizontal, and diagonal)
		// Note: edges have extra (duplicate) row and column
		// to ease in face-creation w/o needing special cases 
		// for the seams
		int [] edges = new int[ (longi+1) * (lati+1) * 3];

		// Edges are stored in indexed order, with the vertical set
		// added first, followed by the horizontal set, then the
		// diagonal set.
		// Since each set begins at the index after where the last
		// set ended, we must keep track (for each set) of the index
		// into the whole collection where each set begins.  This
		// allows easier indexing within a particular set.
		int V_EDGE = 0;
		// Offset into edge list by V_EDGE
		int H_EDGE = longi * lati;
		// Offset into edge list by V_EDGE + H_EDGE
		int D_EDGE = H_EDGE + longi * lati;

		// Vertical Edges
		for (int j=0; j<lati; j++) {
			for (int i=0; i<longi; i++) {
				edges[ V_EDGE + i+(longi+1)*j] = V_EDGE + i+longi*j;
				obj.addEdge( 
					MemMgr.Edge(),
					verts[ i + (longi+1)*j ],
					verts[ i + (longi+1)*(j+1) ]
					);
			}
		}
		// Horizontal edges
		for (int j=0; j<lati; j++) {
			for (int i=0; i<longi; i++) {
				edges[ H_EDGE + i+(longi+1)*j] = H_EDGE + i+longi*j;
				obj.addEdge( 
					MemMgr.Edge(),
					verts[ i   + (longi+1)*j ],
					verts[ i+1 + (longi+1)*j ]
					);
			}
		}
		// Diagonal Edges
		for (int j=0; j<lati; j++) {
			for (int i=0; i<longi; i++) {
				edges[ D_EDGE + i+(longi+1)*j] = D_EDGE + i+longi*j;
				obj.addEdge( 
					MemMgr.Edge(),
					verts[ i   + (longi+1)*(j+1) ],
					verts[ i+1 + (longi+1)*j     ]
					);
			}
		}
		// extra columns
		for (int j=0; j<lati; j++) {
			edges[ V_EDGE + longi + (longi+1)*j] = edges[ V_EDGE + 0 + (longi+1)*j];
			edges[ H_EDGE + longi + (longi+1)*j] = edges[ H_EDGE + 0 + (longi+1)*j];
			edges[ D_EDGE + longi + (longi+1)*j] = edges[ D_EDGE + 0 + (longi+1)*j];
		}
		// extra rows
		for (int i=0; i<longi; i++) {
			edges[ V_EDGE + i + (longi+1)*lati] = edges[ V_EDGE + i];
			edges[ H_EDGE + i + (longi+1)*lati] = edges[ H_EDGE + i];
			edges[ D_EDGE + i + (longi+1)*lati] = edges[ D_EDGE + i];
		}
		// extra corners
		edges[ V_EDGE + (lati+1)*(longi+1)-1] = edges[ V_EDGE + 0 ];
		edges[ H_EDGE + (lati+1)*(longi+1)-1] = edges[ H_EDGE + 0 ];
		edges[ D_EDGE + (lati+1)*(longi+1)-1] = edges[ D_EDGE + 0 ];

		// Now add the faces.

		// Grid faces
		Triangle tri = null;

		for (int j=0; j<lati; j++) {
			for (int i=0; i<longi; i++) {
				tri = MemMgr.Triangle();
				tri.setTexture(
					(s_start + (s_end-s_start) * (float) i    / (float)longi),
					(t_start + (t_end-t_start) * (float)(j+1) / (float)(lati+1) ),
					(s_start + (s_end-s_start) * (float)(i+1) / (float)longi),
					(t_start + (t_end-t_start) * (float)(j+1) / (float)(lati+1) ),
					(s_start + (s_end-s_start) * (float) i    / (float)longi),
					(t_start + (t_end-t_start) * (float)(j+2) / (float)(lati+1) )
					);
				obj.addTriangle( 
					tri,
					verts[ i   +  j    * (longi+1) ],
					verts[ i+1 +  j    * (longi+1) ],
					verts[ i   + (j+1) * (longi+1) ],
					edges[ H_EDGE + i   + j   *(longi+1) ],
					edges[ D_EDGE + i   + j   *(longi+1) ],
					edges[ V_EDGE + i   + j   *(longi+1) ]
					);
				tri = MemMgr.Triangle();
				tri.setTexture(
					(s_start + (s_end-s_start) * (float)(i+1) / (float)longi),
					(t_start + (t_end-t_start) * (float)(j+1) / (float)(lati+1) ),
					(s_start + (s_end-s_start) * (float)(i+1) / (float)longi),
					(t_start + (t_end-t_start) * (float)(j+2) / (float)(lati+1) ),
					(s_start + (s_end-s_start) * (float) i    / (float)longi),
					(t_start + (t_end-t_start) * (float)(j+2) / (float)(lati+1) )
					);
				obj.addTriangle( 
					tri,
					verts[ i+1 +  j    * (longi+1) ],
					verts[ i+1 + (j+1) * (longi+1) ],
					verts[ i   + (j+1) * (longi+1) ],
					edges[ V_EDGE + i+1 + j    * (longi+1) ],
					edges[ H_EDGE + i   +(j+1) * (longi+1) ],
					edges[ D_EDGE + i   + j    * (longi+1) ]
					);
			}
		}

		obj.calcVertexNormals();

		return obj;
	}


	public static Obj makeRing()
	{
		int [] text = new int[128*128];
		Obj obj = MemMgr.Obj();

		for (int i=0; i<4; i++) {

			for (int j=0; j<4; j++) {
				
				for (int k=0; k<16; k++) {
					
					for (int l=0; l<16; l++) {

						text[ i*32 + j*32*128 + k+16 + (l+16)*128 ] =
						text[ i*32 + j*32*128 + k + l*128 ] =
							(255<<24) + (255<<16);
						text[ i*32 + j*32*128 + k+16 + l*128 ] =
						text[ i*32 + j*32*128 + k + (l+16)*128 ] =
							(255<<24) + (255<<16) + (255<<8) + 255;
					}
				}
			}
		}

		int [] text2 = new int[128*128];

		for (int i=0; i<2; i++) {

			for (int j=0; j<2; j++) {
				
				for (int k=0; k<32; k++) {
					
					for (int l=0; l<32; l++) {

						text2[ i*64 + j*64*128 + k+32 + (l+32)*128 ] =
						text2[ i*64 + j*64*128 + k + l*128 ] =
							(255<<24) + (255<<16) + 255;
						text2[ i*64 + j*64*128 + k+32 + l*128 ] =
						text2[ i*64 + j*64*128 + k + (l+32)*128 ] = 0;
					}
				}
			}
		}

		Vertex v0 = MemMgr.Vertex( (float)-1.0, (float) 1.0, (float)-1.0);
		Vertex v1 = MemMgr.Vertex( (float) 1.0, (float) 1.0, (float)-1.0);
		Vertex v2 = MemMgr.Vertex( (float) 1.0, (float)-1.0, (float)-1.0);
		Vertex v3 = MemMgr.Vertex( (float)-1.0, (float)-1.0, (float)-1.0);
		Vertex v4 = MemMgr.Vertex( (float)-1.0, (float) 1.0, (float) 1.0);
		Vertex v5 = MemMgr.Vertex( (float) 1.0, (float) 1.0, (float) 1.0);
		Vertex v6 = MemMgr.Vertex( (float) 1.0, (float)-1.0, (float) 1.0);
		Vertex v7 = MemMgr.Vertex( (float)-1.0, (float)-1.0, (float) 1.0);

		obj.addVertex( v0);
		obj.addVertex( v1);
		obj.addVertex( v2);
		obj.addVertex( v3);
		obj.addVertex( v4);
		obj.addVertex( v5);
		obj.addVertex( v6);
		obj.addVertex( v7);

		Edge e00 = MemMgr.Edge();
		Edge e01 = MemMgr.Edge();
		Edge e02 = MemMgr.Edge();
		Edge e03 = MemMgr.Edge();
		Edge e04 = MemMgr.Edge();
		Edge e05 = MemMgr.Edge();
		Edge e06 = MemMgr.Edge();
		Edge e07 = MemMgr.Edge();
		Edge e08 = MemMgr.Edge();
		Edge e09 = MemMgr.Edge();
		Edge e10 = MemMgr.Edge();
		Edge e11 = MemMgr.Edge();
		Edge e12 = MemMgr.Edge();
		Edge e13 = MemMgr.Edge();
		Edge e14 = MemMgr.Edge();
		Edge e15 = MemMgr.Edge();

		obj.addEdge( e00, 0, 1);
		obj.addEdge( e01, 1, 2);
		obj.addEdge( e02, 2, 3);
		obj.addEdge( e03, 3, 0);
		obj.addEdge( e04, 4, 5);
		obj.addEdge( e05, 5, 6);
		obj.addEdge( e06, 6, 7);
		obj.addEdge( e07, 7, 4);
		obj.addEdge( e08, 4, 0);
		obj.addEdge( e09, 5, 1);
		obj.addEdge( e10, 6, 2);
		obj.addEdge( e11, 7, 3);
		obj.addEdge( e12, 3, 1);
		obj.addEdge( e13, 6, 1);
		obj.addEdge( e14, 7, 5);
		obj.addEdge( e15, 7, 0);

		Triangle t01 = MemMgr.Triangle();
		Triangle t00 = MemMgr.Triangle();
		Triangle t02 = MemMgr.Triangle();
		Triangle t03 = MemMgr.Triangle();
		Triangle t04 = MemMgr.Triangle();
		Triangle t05 = MemMgr.Triangle();
		Triangle t06 = MemMgr.Triangle();
		Triangle t07 = MemMgr.Triangle();

		//------------------------------------------------------- 
		// Hard-coding some materials to test different rendering
		//  modes.  In a normal usage these should NOT be set here

		// Transp cyan fogged
		Material texture = MemMgr.Material();
		texture._lightmodel  = Material.FOG;
		texture._color = (255<<24) + (100<<16) + (255<<8) + 255;
		texture.TEXTURE = false;
		texture.TRANSPARENT = true;
		texture._transp_R = 150;
		texture._transp_G = 150;
		texture._transp_B = 150;
		texture._fog_R = 0;
		texture._fog_G = 0;
		texture._fog_B = 0;
		texture._fog_near = (float)2.5;
		texture._fog_far  = (float)4.5;
		texture._fog_near_val = (float)0.0;
		texture._fog_far_val  = (float)1.0;

		t00.mat = texture;
		t01.mat = texture;
		t04.mat = texture;
		t05.mat = texture;

		// Red/white transp texture fogged
		texture = MemMgr.Material();
		texture._lightmodel  = Material.FOG;
		texture._color = (255<<24) + (100<<16) + (255<<8) + 255;
		texture.TEXTURE = true;
		texture._texture = text;
		texture.TRANSPARENT = true;
		texture._transp_R = 150;
		texture._transp_G = 150;
		texture._transp_B = 150;
		texture._fog_R = 0;
		texture._fog_G = 0;
		texture._fog_B = 0;
		texture._fog_near = (float)2.5;
		texture._fog_far  = (float)4.5;
		texture._fog_near_val = (float)0.0;
		texture._fog_far_val  = (float)1.0;

		t02.mat = texture;
		t03.mat = texture;
		t02.setTexture( (float)0.0, (float)0.0, (float)1.0, (float)0.0, (float)0.0, (float)1.0); 
		t03.setTexture( (float)1.0, (float)0.0, (float)1.0, (float)1.0, (float)0.0, (float)1.0); 

		// Purple transp texture fogged in decal cutout checkerboard patter
		texture = MemMgr.Material();
		texture._lightmodel  = Material.FOG;
		texture._color = (255<<24) + (100<<16) + (255<<8) + 255;
		texture.TEXTURE = true;
		texture._texture = text2;
		texture.TRANSPARENT = true;
		texture._transp_R = 180;
		texture._transp_G = 180;
		texture._transp_B = 180;
		texture._fog_R = 0;
		texture._fog_G = 0;
		texture._fog_B = 0;
		texture._fog_near = (float)2.5;
		texture._fog_far  = (float)4.5;
		texture._fog_near_val = (float)0.0;
		texture._fog_far_val  = (float)1.0;

		t06.mat = texture;
		t07.mat = texture;
		t06.setTexture( (float)1.0, (float)0.0, (float)1.0, (float)1.0, (float)0.0, (float)0.0); 
		t07.setTexture( (float)1.0, (float)1.0, (float)0.0, (float)1.0, (float)0.0, (float)0.0); 

		obj.addTriangle( t00,  0, 3, 1,  0, 3, 12 );
		obj.addTriangle( t01,  1, 3, 2,  1, 12, 2 );
		obj.addTriangle( t02,  5, 1, 6,  9, 13, 5 );
		obj.addTriangle( t03,  1, 2, 6,  1, 10, 13);
		obj.addTriangle( t04,  4, 5, 7,  7, 4, 14 );
		obj.addTriangle( t05,  5, 6, 7,  5, 6, 14 );
		obj.addTriangle( t06,  4, 7, 0,  7, 15, 8 );
		obj.addTriangle( t07,  7, 3, 0,  15, 11, 3);

		obj.calcVertexNormals();
		
		return obj;
	}

	public static Obj makeCube()
	{
		int [] text = new int[128*128];
		Obj obj = MemMgr.Obj();

		for (int i=0; i<4; i++) {

			for (int j=0; j<4; j++) {
				
				for (int k=0; k<16; k++) {
					
					for (int l=0; l<16; l++) {

						text[ i*32 + j*32*128 + k+16 + (l+16)*128 ] =
						text[ i*32 + j*32*128 + k + l*128 ] =
							(255<<24) + (255<<16);
						text[ i*32 + j*32*128 + k+16 + l*128 ] =
						text[ i*32 + j*32*128 + k + (l+16)*128 ] =
							(255<<24) + (255<<16) + (255<<8) + 255;
					}
				}
			}
		}

		Vertex v0 = MemMgr.Vertex( (float)-1.0, (float) 1.0, (float)-1.0);
		Vertex v1 = MemMgr.Vertex( (float) 1.0, (float) 1.0, (float)-1.0);
		Vertex v2 = MemMgr.Vertex( (float) 1.0, (float)-1.0, (float)-1.0);
		Vertex v3 = MemMgr.Vertex( (float)-1.0, (float)-1.0, (float)-1.0);
		Vertex v4 = MemMgr.Vertex( (float)-1.0, (float) 1.0, (float) 1.0);
		Vertex v5 = MemMgr.Vertex( (float) 1.0, (float) 1.0, (float) 1.0);
		Vertex v6 = MemMgr.Vertex( (float) 1.0, (float)-1.0, (float) 1.0);
		Vertex v7 = MemMgr.Vertex( (float)-1.0, (float)-1.0, (float) 1.0);

		obj.addVertex( v0);
		obj.addVertex( v1);
		obj.addVertex( v2);
		obj.addVertex( v3);
		obj.addVertex( v4);
		obj.addVertex( v5);
		obj.addVertex( v6);
		obj.addVertex( v7);

		Edge e00 = MemMgr.Edge();
		Edge e01 = MemMgr.Edge();
		Edge e02 = MemMgr.Edge();
		Edge e03 = MemMgr.Edge();
		Edge e04 = MemMgr.Edge();
		Edge e05 = MemMgr.Edge();
		Edge e06 = MemMgr.Edge();
		Edge e07 = MemMgr.Edge();
		Edge e08 = MemMgr.Edge();
		Edge e09 = MemMgr.Edge();
		Edge e10 = MemMgr.Edge();
		Edge e11 = MemMgr.Edge();
		Edge e12 = MemMgr.Edge();
		Edge e13 = MemMgr.Edge();
		Edge e14 = MemMgr.Edge();
		Edge e15 = MemMgr.Edge();
		Edge e16 = MemMgr.Edge();
		Edge e17 = MemMgr.Edge();

		obj.addEdge( e00, 0, 1);
		obj.addEdge( e01, 1, 2);
		obj.addEdge( e02, 2, 3);
		obj.addEdge( e03, 3, 0);
		obj.addEdge( e04, 4, 5);
		obj.addEdge( e05, 5, 6);
		obj.addEdge( e06, 6, 7);
		obj.addEdge( e07, 7, 4);
		obj.addEdge( e08, 4, 0);
		obj.addEdge( e09, 5, 1);
		obj.addEdge( e10, 6, 2);
		obj.addEdge( e11, 7, 3);
		obj.addEdge( e12, 3, 1);
		obj.addEdge( e13, 6, 1);
		obj.addEdge( e14, 7, 5);
		obj.addEdge( e15, 7, 0);
		obj.addEdge( e16, 4, 1);
		obj.addEdge( e17, 7, 2);

		Triangle t01 = MemMgr.Triangle();
		Triangle t00 = MemMgr.Triangle();
		Triangle t02 = MemMgr.Triangle();
		Triangle t03 = MemMgr.Triangle();
		Triangle t04 = MemMgr.Triangle();
		Triangle t05 = MemMgr.Triangle();
		Triangle t06 = MemMgr.Triangle();
		Triangle t07 = MemMgr.Triangle();
		Triangle t08 = MemMgr.Triangle();
		Triangle t09 = MemMgr.Triangle();
		Triangle t10 = MemMgr.Triangle();
		Triangle t11 = MemMgr.Triangle();
		
		/** 
		// Hard-coding some materials to test different rendering
		//  modes.  In a normal usage these should NOT be set here

		// Gouraud shaded
		Material texture = MemMgr.Material();
		texture._lightmodel  = Material.FOG;
		texture._color = (255<<24) + (255<<16) + (255<<8) + 255;
		texture.TEXTURE = false;
		texture._fog_R = 0;
		texture._fog_G = 0;
		texture._fog_B = 0;
		texture._fog_near = (float)2.5;
		texture._fog_far  = (float)4.5;
		texture._fog_near_val = (float)0.0;
		texture._fog_far_val  = (float)1.0;
		t02.mat = texture;
		t03.mat = texture;
		t06.mat = texture;
		t07.mat = texture;
		t08.mat = texture;
		t09.mat = texture;
		t10.mat = texture;
		t11.mat = texture;
		
		// Red/white checkerboard texture, solid shaded
		texture = MemMgr.Material();
		t00.setTexture( (float)1.0, (float)0.0, (float)1.0, (float)1.0, (float)0.0, (float)0.0); 
		t01.setTexture( (float)0.0, (float)0.0, (float)1.0, (float)1.0, (float)0.0, (float)1.0); 
		texture.TEXTURE  = true;
		texture._lightmodel = Material.FOG;
		texture._fog_R = 0;
		texture._fog_G = 0;
		texture._fog_B = 0;
		texture._fog_near = (float)2.5;
		texture._fog_far  = (float)4.5;
		texture._fog_near_val = (float)0.0;
		texture._fog_far_val  = (float)1.0;
		texture._texture = text;
		texture.SPEED = Material.FAST16;
		t00.mat = texture;
		t01.mat = texture;

		// Red/white checkerboard texture, flat shaded
		texture = MemMgr.Material();
		t04.setTexture( (float)0.0, (float)0.0, (float)2.0, (float)0.0, (float)0.0, (float)1.0); 
		t05.setTexture( (float)2.0, (float)0.0, (float)2.0, (float)1.0, (float)0.0, (float)1.0); 
		texture.TEXTURE  = true;
		texture._lightmodel = Material.FOG;
		texture._fog_R = 0;
		texture._fog_G = 0;
		texture._fog_B = 0;
		texture._fog_near = (float)2.5;
		texture._fog_far  = (float)4.5;
		texture._fog_near_val = (float)0.0;
		texture._fog_far_val  = (float)1.0;
		texture._texture = Render.loadTexture("scarymetal.gif"); 
		texture.SPEED = Material.FAST16;
		t04.mat = texture;
		t05.mat = texture;
		*/

		obj.addTriangle( t00,  0, 3, 1,  0, 3, 12 );
		obj.addTriangle( t01,  1, 3, 2,  1, 12, 2 );
		obj.addTriangle( t02,  5, 1, 6,  9, 13, 5 );
		obj.addTriangle( t03,  1, 2, 6,  1, 10, 13);
		obj.addTriangle( t04,  4, 5, 7,  7, 4, 14 );
		obj.addTriangle( t05,  5, 6, 7,  5, 6, 14 );
		obj.addTriangle( t06,  4, 7, 0,  7, 15, 8 );
		obj.addTriangle( t07,  7, 3, 0,  15, 11, 3);
		obj.addTriangle( t08,  4, 0, 1,  8, 0, 16 );
		obj.addTriangle( t09,  4, 1, 5,  16, 9, 4 );
		obj.addTriangle( t10,  7, 2, 3,  11, 17, 2);
		obj.addTriangle( t11,  7, 6, 2,  17, 6, 10);
		obj.calcVertexNormals();

		return obj;
	}

	public static Obj makeOpenBox()
    {
		int [] text = new int[128*128];
		Obj obj = MemMgr.Obj();
		
		for (int i=0; i<4; i++) {
			
			for (int j=0; j<4; j++) {
			
				for (int k=0; k<16; k++) {
					
					for (int l=0; l<16; l++) {
					
						text[ i*32 + j*32*128 + k+16 + (l+16)*128 ] =
							text[ i*32 + j*32*128 + k + l*128 ] =
							(255<<24) + (255<<16);
						text[ i*32 + j*32*128 + k+16 + l*128 ] =
							text[ i*32 + j*32*128 + k + (l+16)*128 ] =
							(255<<24) + (255<<16) + (255<<8) + 255;
					}
				}
			}
		}
		
		Vertex v0 = MemMgr.Vertex( (float)-1.0, (float) 1.0, (float)-1.0);
		Vertex v1 = MemMgr.Vertex( (float) 1.0, (float) 1.0, (float)-1.0);
		Vertex v2 = MemMgr.Vertex( (float) 1.0, (float)-1.0, (float)-1.0);
		Vertex v3 = MemMgr.Vertex( (float)-1.0, (float)-1.0, (float)-1.0);
		Vertex v4 = MemMgr.Vertex( (float)-1.0, (float) 1.0, (float) 1.0);
		Vertex v5 = MemMgr.Vertex( (float) 1.0, (float) 1.0, (float) 1.0);
		Vertex v6 = MemMgr.Vertex( (float) 1.0, (float)-1.0, (float) 1.0);
		Vertex v7 = MemMgr.Vertex( (float)-1.0, (float)-1.0, (float) 1.0);
		
		obj.addVertex( v0);
		obj.addVertex( v1);
		obj.addVertex( v2);
		obj.addVertex( v3);
		obj.addVertex( v4);
		obj.addVertex( v5);
		obj.addVertex( v6);
		obj.addVertex( v7);
		
		Edge e00 = MemMgr.Edge();
		Edge e01 = MemMgr.Edge();
		Edge e02 = MemMgr.Edge();
		Edge e03 = MemMgr.Edge();
		Edge e04 = MemMgr.Edge();
		Edge e05 = MemMgr.Edge();
		Edge e06 = MemMgr.Edge();
		Edge e07 = MemMgr.Edge();
		Edge e08 = MemMgr.Edge();
		Edge e09 = MemMgr.Edge();
		Edge e10 = MemMgr.Edge();
		Edge e11 = MemMgr.Edge();
		//Edge e12 = MemMgr.Edge();
		Edge e13 = MemMgr.Edge();
		//Edge e14 = MemMgr.Edge();
		Edge e15 = MemMgr.Edge();
		Edge e16 = MemMgr.Edge();
		Edge e17 = MemMgr.Edge();
		
		obj.addEdge( e00, 0, 1);
		obj.addEdge( e01, 1, 2);
		obj.addEdge( e02, 2, 3);
		obj.addEdge( e03, 3, 0);
		obj.addEdge( e04, 4, 5);
		obj.addEdge( e05, 5, 6);
		obj.addEdge( e06, 6, 7);
		obj.addEdge( e07, 7, 4);
		obj.addEdge( e08, 4, 0);
		obj.addEdge( e09, 5, 1);
		obj.addEdge( e10, 6, 2);
		obj.addEdge( e11, 7, 3);
		//addEdge( e12, 3, 1);
		obj.addEdge( e13, 6, 1);
		//addEdge( e14, 7, 5);
		obj.addEdge( e15, 7, 0);
		obj.addEdge( e16, 4, 1);
		obj.addEdge( e17, 7, 2);
		
		Triangle t01 = MemMgr.Triangle();
		Triangle t00 = MemMgr.Triangle();
		Triangle t02 = MemMgr.Triangle();
		Triangle t03 = MemMgr.Triangle();
		Triangle t04 = MemMgr.Triangle();
		Triangle t05 = MemMgr.Triangle();
		Triangle t06 = MemMgr.Triangle();
		Triangle t07 = MemMgr.Triangle();
		Triangle t08 = MemMgr.Triangle();
		Triangle t09 = MemMgr.Triangle();
		Triangle t10 = MemMgr.Triangle();
		Triangle t11 = MemMgr.Triangle();
		
		//addTriangle( t00,  0, 3, 1,  0, 3, 12 );
		//addTriangle( t01,  1, 3, 2,  1, 12, 2 );
		
		obj.addTriangle( t02,  5, 1, 6,  9, 12, 5 );
		obj.addTriangle( t03,  1, 2, 6,  1, 10, 12);
		
		//addTriangle( t04,  4, 5, 7,  7, 4, 14 );
		//addTriangle( t05,  5, 6, 7,  5, 6, 14 );
		
		obj.addTriangle( t06,  4, 7, 0,  7, 13, 8 );
		obj.addTriangle( t07,  7, 3, 0,  13, 11, 3);
		obj.addTriangle( t08,  4, 0, 1,  8, 0, 14 );
		obj.addTriangle( t09,  4, 1, 5,  14, 9, 4 );
		obj.addTriangle( t10,  7, 2, 3,  11, 15, 2);
		obj.addTriangle( t11,  7, 6, 2,  15, 6, 10);
		
		obj.calcVertexNormals();

		return obj;
    }
    

}