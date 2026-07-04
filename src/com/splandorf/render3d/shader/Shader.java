package com.splandorf.render3d.shader;

import java.util.ArrayList;

import com.splandorf.render3d.MemMgr;
import com.splandorf.render3d.math.Alg;
import com.splandorf.render3d.math.Mat4f;
import com.splandorf.render3d.math.Vec3f;
import com.splandorf.render3d.math.VecSf;
import com.splandorf.render3d.scene.Edge;
import com.splandorf.render3d.scene.Light;
import com.splandorf.render3d.scene.Material;
import com.splandorf.render3d.scene.Obj;
import com.splandorf.render3d.scene.Triangle;
import com.splandorf.render3d.scene.Vertex;

public class Shader {
    protected static int _width  = 0;
	protected static int _height = 0;
	protected static int [] _pix = null;
	protected static int [] _zbuf = null;
	protected static float _camera_zoom;

	public static void initShader( int width, int height, int [] pix, int [] zbuf, float cameraZoom)
	{
		_width  = width;
		_height = height;
		_pix    = pix;
		_zbuf   = zbuf;
		_camera_zoom = cameraZoom;
	}

	public static void illuminate( Vec3f n, Vec3f lightIntensity, ArrayList<Light> lights)
	{
		float dot   = (float)0.0;
		Light l = null;
		lightIntensity.x = (float)0.0;
		lightIntensity.y = (float)0.0;
		lightIntensity.z = (float)0.0;

		for (int i=0; i<lights.size(); i++) {
			l = (Light)lights.get(i);
			dot = -Alg.dot( l.dir, n);
			if (dot > 0.0) {
				lightIntensity.x += dot * l.intensity * l.red;
				lightIntensity.y += dot * l.intensity * l.green;
				lightIntensity.z += dot * l.intensity * l.blue;
				lightIntensity.w += dot * l.intensity;
			}
		}
		//light.x += _amb_red;
		//light.y += _amb_green;
		//light.z += _amb_blue;
		if (lightIntensity.x > 1.0) lightIntensity.x = (float)1.0;
		if (lightIntensity.y > 1.0) lightIntensity.y = (float)1.0;
		if (lightIntensity.z > 1.0) lightIntensity.z = (float)1.0;
		if (lightIntensity.w > 1.0) lightIntensity.w = (float)1.0;
	}

	public static void illuminate( VecSf n, Vec3f lightIntensity, ArrayList<Light> lights)
	{
		float dot   = (float)0.0;
		Light l = null;
		lightIntensity.x = (float)0.0;
		lightIntensity.y = (float)0.0;
		lightIntensity.z = (float)0.0;

		for (int i=0; i<lights.size(); i++) {
			l = (Light)lights.get(i);
			dot = -Alg.dot( l.s_dir, n);
			if (dot > 0.0) {
				lightIntensity.x += dot * l.intensity * l.red;
				lightIntensity.y += dot * l.intensity * l.green;
				lightIntensity.z += dot * l.intensity * l.blue;
				lightIntensity.w += dot * l.intensity;
			}
		}
		//light.x += _amb_red;
		//light.y += _amb_green;
		//light.z += _amb_blue;
		if (lightIntensity.x > 1.0) lightIntensity.x = (float)1.0;
		if (lightIntensity.y > 1.0) lightIntensity.y = (float)1.0;
		if (lightIntensity.z > 1.0) lightIntensity.z = (float)1.0;
		if (lightIntensity.w > 1.0) lightIntensity.w = (float)1.0;
	}
	
	public static void drawTriangleWithMaterial (
		Triangle t,
		Vec3f p1,
		Vec3f p2,
		Vec3f p3,
		Vec3f light,
		ArrayList<Light> lights,
		Material mat
		) 
	{
		int color = 0;
		int red, green, blue; /* inten; */
		float fog;
		Vec3f n  = MemMgr.Vec3f();
	
		// TRANSP light model
		if (mat._lightmodel == Material.TRANSP) {
			
			TranspTriangle.drawTranspTriangle( t, mat);
		}
		// PHONG lighting model (calculate specular highlights from each light source)
		else if (mat._lightmodel == Material.PHONG) {

			if (mat.BUMP==false) {
				PhongTriangle.drawPhongTriangle( t, mat);
			} else {
				BumpTriangle.drawBumpTriangle( t, mat);
			}
		} 
		else if (mat._lightmodel == Material.GOURAUD) {
				
			if (mat.TEXTURE == true) {

				// Get light contribution at vertex 1
//				Alg.mult( t.obj.ctm().normal_ctm(), t.v1.n, n);
//				Alg.normalize( n);
				illuminate( t.v1.w_n, light, lights);
				t.v1.r = ((int)((float)255.0 * light.x)<<16);
				t.v1.g = ((int)((float)255.0 * light.y)<<16);
				t.v1.b = ((int)((float)255.0 * light.z)<<16);
				// Get light contribution at vertex 2
//				Alg.mult( t.obj.ctm().normal_ctm(), t.v2.n, n);
//				Alg.normalize( n);
				illuminate( t.v2.w_n, light, lights);
				t.v2.r = ((int)((float)255.0 * light.x)<<16);
				t.v2.g = ((int)((float)255.0 * light.y)<<16);
				t.v2.b = ((int)((float)255.0 * light.z)<<16);
				// Get light contribution at vertex 3
//				Alg.mult( t.obj.ctm().normal_ctm(), t.v3.n, n);
//				Alg.normalize( n);
				illuminate( t.v3.w_n, light, lights);
				t.v3.r = ((int)((float)255.0 * light.x)<<16);
				t.v3.g = ((int)((float)255.0 * light.y)<<16);
				t.v3.b = ((int)((float)255.0 * light.z)<<16);
			
				if (mat.SPEED >= Material.FAST) {
					GouraudTriangle.drawFastGouraudTextureTriangle( t, mat);
				} else {
					GouraudTriangle.drawGouraudTextureTriangle( t, mat);
				}
			} else {

				color = mat._color;
				// Get light contribution at vertex 1
//				Alg.mult( t.obj.ctm().normal_ctm(), t.v1.n, n);
//				Alg.normalize( n);
				illuminate( t.v1.w_n, light, lights);
				t.v1.r = ((int)((float)((color>>16) & 255) * light.x)<<16);
				t.v1.g = ((int)((float)((color>>8 ) & 255) * light.y)<<16);
				t.v1.b = ((int)((float)( color      & 255) * light.z)<<16);
				// Get light contribution at vertex 2
//				Alg.mult( t.obj.ctm().normal_ctm(), t.v2.n, n);
//				Alg.normalize( n);
				illuminate( t.v2.w_n, light, lights);
				t.v2.r = ((int)((float)((color>>16) & 255) * light.x)<<16);
				t.v2.g = ((int)((float)((color>>8 ) & 255) * light.y)<<16);
				t.v2.b = ((int)((float)( color      & 255) * light.z)<<16);
				// Get light contribution at vertex 3
//				Alg.mult( t.obj.ctm().normal_ctm(), t.v3.n, n);
//				Alg.normalize( n);	
				illuminate( t.v3.w_n, light, lights);
				t.v3.r = ((int)((float)((color>>16) & 255) * light.x)<<16);
				t.v3.g = ((int)((float)((color>>8 ) & 255) * light.y)<<16);
				t.v3.b = ((int)((float)( color      & 255) * light.z)<<16);

				GouraudTriangle.drawGouraudTriangle( t, mat);
			}
		} 
		// FOG lighting model (increasing blend w/ background color based on distance), textured or solid color
		else if (mat._lightmodel == Material.FOG) {
				
			// If FOG + texture
			if (mat.TEXTURE == true) {
				// This is to correct for the effect
				// of a camera zoom factor on fog.
				// We want fog to remain an illusion of
				// constant world-space.  But adding
				// a zoom factor to the camera moves
				// points closer-to/farther-from the
				// camera.  We want the points themselves
				// to move, but we want fog calculated on
				// them as if the zoom factor really were 1.0

				p1.z *= _camera_zoom;
				p2.z *= _camera_zoom;
				p3.z *= _camera_zoom;
				// Get fog contribution at vertex 1
				if (p1.z < mat._fog_near) {
					fog = mat._fog_near_val;
				} else if (p1.z > mat._fog_far) {
					fog = mat._fog_far_val;
				} else {
					fog = (p1.z-mat._fog_near) / (mat._fog_far - mat._fog_near);
					if (mat._fog_type == Material.SQUARE) fog *= fog;
					fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
				}
				t.v1.r = t.v1.g = t.v1.b = ((int)((float)255.0 * (1.0-fog))<<16);
				// Get fog contribution at vertex 2
				if (p2.z < mat._fog_near) {
					fog = mat._fog_near_val;
				} else if (p2.z > mat._fog_far) {
					fog = mat._fog_far_val;
				} else {
					fog = (p2.z-mat._fog_near) / (mat._fog_far - mat._fog_near);
					if (mat._fog_type == Material.SQUARE) fog *= fog;
					fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
				}
				t.v2.r = t.v2.g = t.v2.b = ((int)((float)255.0 * (1.0-fog))<<16);
				// Get fog contribution at vertex 3
				if (p3.z < mat._fog_near) {
					fog = mat._fog_near_val;
				} else if (p3.z > mat._fog_far) {
					fog = mat._fog_far_val;
				} else {
					fog = (p3.z-mat._fog_near) / (mat._fog_far - mat._fog_near);
					if (mat._fog_type == Material.SQUARE) fog *= fog;
					fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
				}
				t.v3.r = t.v3.g = t.v3.b = ((int)((float)255.0 * (1.0-fog))<<16);

				if (mat.SPEED >= Material.FAST) {
					GouraudTriangle.drawFastGouraudTextureTriangle( t, mat);
				} else {
					GouraudTriangle.drawGouraudTextureTriangle( t, mat);
				}

			// If FOG + no texture (just solid color)
			} else {

				// Get fog contribution at vertex 1
				color = mat._color;
				// Get fog contribution at vertex 1
				if (p1.z < mat._fog_near) {
					fog = mat._fog_near_val;
				} else if (p1.z > mat._fog_far) {
					fog = mat._fog_far_val;
				} else {
					fog = (p1.z-mat._fog_near) / (mat._fog_far - mat._fog_near);
					if (mat._fog_type == Material.SQUARE) {
						fog *= fog;
					}
					fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
				}
				t.v1.r = ( (int)((float)((color>>16) & 255) * (1.0-fog)) +
							(int)((float)(mat._fog_R) * fog )               ) << 16;
				t.v1.g = ( (int)((float)((color>>8 ) & 255) * (1.0-fog)) +
							(int)((float)(mat._fog_G) * fog )               ) << 16;
				t.v1.b = ( (int)((float)( color      & 255) * (1.0-fog)) +
							(int)((float)(mat._fog_B) * fog )               ) << 16;

				// Get fog contribution at vertex 2
				if (p2.z < mat._fog_near) {
					fog = mat._fog_near_val;
				} else if (p2.z > mat._fog_far) {
					fog = mat._fog_far_val;
				} else {
					fog = (p2.z-mat._fog_near) / (mat._fog_far - mat._fog_near);
					if (mat._fog_type == Material.SQUARE) {
						fog *= fog;
					}
					fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
				}
				t.v2.r = ( (int)((float)((color>>16) & 255) * (1.0-fog)) +
							(int)((float)(mat._fog_R) * fog )               ) << 16;
				t.v2.g = ( (int)((float)((color>>8 ) & 255) * (1.0-fog)) +
							(int)((float)(mat._fog_G) * fog )               ) << 16;
				t.v2.b = ( (int)((float)( color      & 255) * (1.0-fog)) +
							(int)((float)(mat._fog_B) * fog )               ) << 16;
				// Get fog contribution at vertex 3
				if (p3.z < mat._fog_near) {
					fog = mat._fog_near_val;
				} else if (p3.z > mat._fog_far) {
					fog = mat._fog_far_val;
				} else {
					fog = (p3.z-mat._fog_near) / (mat._fog_far - mat._fog_near);
					if (mat._fog_type == Material.SQUARE) {
						fog *= fog;
					}
					fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
				}
				t.v3.r = ( (int)((float)((color>>16) & 255) * (1.0-fog)) +
							(int)((float)(mat._fog_R) * fog )               ) << 16;
				t.v3.g = ( (int)((float)((color>>8 ) & 255) * (1.0-fog)) +
							(int)((float)(mat._fog_G) * fog )               ) << 16;
				t.v3.b = ( (int)((float)( color      & 255) * (1.0-fog)) +
							(int)((float)(mat._fog_B) * fog )               ) << 16;

				GouraudTriangle.drawGouraudTriangle( t, mat);
			}
		} 
		// FLAT lighting model (cosine of surface normal to all lights) - can be textured or solid color
		else if (mat._lightmodel == Material.FLAT) {

			if (mat.TEXTURE == true) {
				
				// Find light contribution to face.
				illuminate( n, light, lights);
				
				color = mat._color;
				red   = (int)((float)255.0 * light.x);
				green = (int)((float)255.0 * light.y);
				blue  = (int)((float)255.0 * light.z);
				if (red  >255) red   = 255;
				if (green>255) green = 255;
				if (blue >255) blue  = 255;
				mat._dif_R = red;
				mat._dif_G = green;
				mat._dif_B = blue;
				
				if (mat.SPEED >= Material.FAST) {
					FlatTriangle.drawFastFlatTextureTriangle( t, mat);
				} else {
					FlatTriangle.drawFlatTextureTriangle( t, mat);	
				}
			} else {
			
				// Find light contribution to face.
				illuminate( n, light, lights);
				
				color = mat._color;
				red   = (int)((float)((color>>16) & 255) * light.x);
				green = (int)((float)((color>>8 ) & 255) * light.y);
				blue  = (int)((float)( color      & 255) * light.z);
				if (red  >255) red   = 255;
				if (green>255) green = 255;
				if (blue >255) blue  = 255;
				color = (255<<24) + (red<<16) + (green<<8) + (blue);
			
				SolidTriangle.drawSolidTriangle( t, color, mat);
			}
		} 
		// SOLID lighting model (constant color, no lighting contribution, but could be textured)
		else if (mat._lightmodel == Material.SOLID) {

			if (mat.TEXTURE == true) {
				if (mat.SPEED >= Material.FAST) {
					SolidTriangle.drawFastSolidTextureTriangle( t, mat);
				} else {
					SolidTriangle.drawSolidTextureTriangle( t, mat);	
				}
			} else {
				SolidTriangle.drawSolidTriangle( t, mat._color, mat);
			}
		}
		MemMgr.done( n);
	}

	public static void drawPointset( Mat4f m, ArrayList<Vertex> vlist, Material mat)
    {
		Particle.drawPointset( m, vlist, mat);
	}

    public static void drawWireframe( Mat4f m, ArrayList<Edge> elist, Material mat)
    {
		Line.drawWireframe( m, elist, mat);
	}

	public static void drawParticle( Mat4f m, Obj obj) {
		Particle.drawParticle( m, obj);
	}

}