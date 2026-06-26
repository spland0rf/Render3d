//================================================================
//
// render.java
//
// a fully-interactive psychoelectronic journey into your
// philo-sensorium and out the back of your amygdala. 
//
// (c) zbigniew mufosowicz -- 1899
//================================================================

package com.splandorf.render3d;


import javax.swing.*;
import javax.imageio.*;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;
import com.splandorf.render3d.math.*;
import com.splandorf.render3d.scene.*;
import com.splandorf.render3d.shader.*;


public class Render extends JPanel implements Runnable
{
//    MemoryImageSource mis = null;
//    Image temp_image = null;
    BufferedImage _framebuffer_image = null;
	JFrame _jFrame = null;

    int [] _pix = null;
    int [] _zbuf = null;
    int [] _zero_pix = null;
    int [] _maxint_pix = null;
//    int [] _env_map = null;
    int [] _background = null;
//    int [] _wu_array = null;

    static int _width  = 640;
    static int _height = 480;
    int _bgcolor = 0;
    MemMgr _mgr = null;
    Thread _renderThread = null;
    boolean _initialized=false;
	boolean _running = true;
    double _incr = 0.0;
    int _garbage_counter = 0;
    
    public static int SUBDIV_SIZE = 16;
    public static int MAX24BIT = (255<<16) + (255<<8) + 255;

    Group _scene_root = null;

    Ctm _cam_ctm = null;
    Vec3f _from = null;
    Vec3f _at = null;
    Vec3f _up = null;
    float _zoom = (float)1.3;
    
    // Light info
    ArrayList<Light> _lights = null;
    float _amb_red   = (float)0.0;
    float _amb_green = (float)0.0;
    float _amb_blue  = (float)0.0;
    
    // Temporaries for testing:
	/* 
    int [] _red_blend;
    int _red_color;
    int _blue_color;
    int [] _blue_blend;
    */

    Obj _obj1 = null;
    Obj _obj2 = null;
    Obj _obj3 = null;
    Obj _obj4 = null;
    Obj _obj5 = null;

    Mat4f _temp_mat1 = null;
    Mat4f _temp_mat2 = null;

	ArrayList<Object> _transpQueue =  new ArrayList<Object>( 10);

    public static void main(String[] args) throws IOException {
        System.out.print("Render.main( ");
        for (int i = 0; i < args.length; i++) {
            System.out.print(args[i] + ", ");
        }
        System.out.println(")");
        Render me = new Render();
        int w = Render._width;
        int h = Render._height;
        me.init();

        me._jFrame = new JFrame();
        me._jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        me._jFrame.setSize(w, h);
        me._jFrame.add(me);
        me._jFrame.setVisible(true);

        me._renderThread = new Thread( me);
		me._running = true;
        me._renderThread.start();
	}

	public Render()
    {
//		_app = this;
    }
    

	public void paint(Graphics gx)
	{
		System.out.println("Paint.");

//		_framebuffer_image.setRGB(0, 0, _width, _height, _pix, 0, _width);
        gx.drawImage( _framebuffer_image, 0, 0, null);
	}

	/**
	public void start()
	{
		if (_renderThread == null)
		{
			_renderThread = new Thread(this);
			_renderThread.;
		}
	}
	
	public void stop()
	{
		if (_renderThread != null)
		{
			_renderThread.stop();
			_renderThread = null;
		}
	}
		*/

	public void run()
	{
		long startTime = System.currentTimeMillis();
		long oldTime = startTime;
		long newTime = 0;
		int  printFramerate = 0;
		
		System.arraycopy( _background, 0, _pix, 0, _height*_width);
		System.arraycopy( _maxint_pix, 0, _zbuf, 0, _height*_width);
		
		float _time = (float)0.0;
		while (_running) {
			
			renderScene( _time);
			_time += 0.01;
			
			_jFrame.repaint();
			Thread.yield();
			
			//			try {
			//			    _renderThread.sleep(2000);
			//			} catch (Exception e) {}
			
			_garbage_counter++;
			if (_garbage_counter>=50) {
				System.gc();
				_garbage_counter = 0;
				printFramerate++;
			}
			if (printFramerate>=2) {
				newTime = System.currentTimeMillis();
				System.err.println("fps: " + (100000 / (newTime-oldTime)) );
				oldTime = newTime;
				printFramerate = 0;
			}
		}
	}


	public void init()
    {		
		_scene_root = new Group( "ROOT");

		//parseVRML();

		/**
		// Add grid bag layout so we can force the applet
		// to lay itself out nicely
		GridBagLayout      gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout( gbl);
		gbc.ipadx  = 0;
		gbc.ipady  = 0;
		gbc.gridx  = 0;
		gbc.gridy  = 0;
		gbc.insets = new Insets(0,0,0,0);
		gbc.fill   = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		_canvas = new Canvas();
		_canvas.resize( _width, _height);
		_canvas.setBackground( java.awt.Color.white);
		
		gbl.setConstraints(_canvas, gbc);
		add(_canvas);
		*/
		
		// Make our front buffer image
		_framebuffer_image = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
		// Instead of creating the pixel array in memory, then copying it to the Framebuffer Image,
		// try fetching the BufferedImages' own underlying int[] pixel array for direct editing.
		// _pix = new int[ _width * _height];
		_pix = ((DataBufferInt) _framebuffer_image.getRaster().getDataBuffer()).getData();

		_zbuf = new int[ _width * _height];
		_zero_pix = new int[ _width * _height];
		_maxint_pix = new int[ _width * _height];
		_bgcolor = (255<<24) + (0<<16) + (0<<8) + 0;
		for (int i=0; i<_width*_height; i++) {
			_zero_pix[i] = _bgcolor;
			_maxint_pix[i] = Integer.MAX_VALUE;
		}

		/** 
		_red_color = (255<<24) + (250<<16);
		_red_blend = new int[256];
		makeBlendArray( _red_color, _bgcolor, _red_blend);
		_blue_color = (255<<24) + (255<<16) + (255<<8) + (255);
		_blue_blend = new int[256];
		makeBlendArray( _blue_color, _bgcolor, _blue_blend);
		*/

		_background = loadTexture( "/textures/night_sky_background.jpg", null);
		if (_background==null) {
			_background = new int[_width*_height];
		}
    }

	public void renderScene( float cur_time)
    {
		if (_initialized) {
			
			System.arraycopy( _background, 0, _pix, 0, _height*_width);
			System.arraycopy( _zero_pix, 0, _zbuf, 0, _height*_width);
			
			
			//_incr = 10.0;
			_incr += 0.01;
			_from.z = (float)(Math.sin(_incr) * 3.5);
			_from.x = (float)(Math.cos(_incr) * 5.5);
			_from.y = (float) Math.sin(_incr * 5.0) * (float)2.0 + (float)0.0;
			//_from.y = -(float)2.0;
			
			_cam_ctm.orient( _from, _at, _up);
			
			_scene_root.render( this, _cam_ctm.inv_ctm(), Alg.IDENT_MAT, cur_time );
			
			if (_transpQueue.size() > 0) {
				renderTranspObjects();
			}
			
			// mis.newPixels( 0, 0, _width, _height);
			
			// Blit new image to offscreen buffer
			//_framebuffer_image.getGraphics().drawImage( temp_image, 0, 0, this);
			
			// Blit offscreen buffer to _canvas
			//_canvas.getGraphics().drawImage( temp_image, 0, 0, this);
			
		} else {
			initScene();
		}
    }

    public int[] loadTexture( String filename, Material mat)
    {
        InputStream is = null; 
		BufferedImage textImage = null;
        
        try {
			if (!filename.startsWith("/")) {
				filename = "/" + filename;
			}
			is = getClass().getResourceAsStream(filename);
            textImage = ImageIO.read(is);
            
        } catch (Exception ex) {
			System.err.println("Render.loadTexture(): Couldn't load " + filename);
            ex.printStackTrace();
            
        } finally {
            try {
                is.close();  
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

		if (textImage == null) {
			return null;
		}
    
		
		// Grab pixel array
		int src_width = textImage.getWidth();
		int src_height = textImage.getHeight();
		int[] src_pixels = ((DataBufferInt) textImage.getRaster().getDataBuffer()).getData();
		boolean hasAlphaChannel = textImage.getAlphaRaster() != null;
		
		if (hasAlphaChannel) {
			// Set all pixels whose opacity is <255 to zero.
			// If a texture pixel is zero, it is treated as
			// a decal cut-out, and not rendered at all.
			for (int i=0; i<src_width * src_height; i++) {
				if ( ((src_pixels[i]>>24)&255) != 255) {
					src_pixels[i] = 0;
				}
			}
		}
		if (mat != null) {
			mat._sprite_width = src_width;
			mat._sprite_height = src_height;
			// We can't just save it as a texture on the material -- we don't know if it's:
			// - A texture
			// - A bump map
			// - An environment map
			// - A sprite
// 			mat._texture = src_pixels;
		}
		return src_pixels;
    }

    public int[] loadBumpMap( String filename, Material mat)
    {
		int [] height_map = loadTexture( filename, mat);

		int [] bump_map = new int[256*256];

		if (height_map == null || height_map.length != 256*256) {
			System.err.println("Bump maps must be 256x256 pixels!");
			// Return dummy bump map with all normals pointing straight out of the surface.
			for (int i=0; i<256*256; i++) {
				bump_map[i] = (127<<8) + 127;
			}
			return bump_map;
		}

		// Assume bump map is greyscale -- but keep only the one 8-bit color channel (Blue) for 
		// height information.  The other two channels (Red and Green) are ignored.
		for (int i=0; i<256*256; i++) {
			height_map[i] = (height_map[i])&255;
		}

		int s;
		int t;

		for (int i=0; i<256; i++) {
			for ( int j=0; j<256; j++) {

				if ( (i==0||i==255) && (j==0||j==255) ) {
					s = 0;
					t = 0;
				} else {
					if (i==0) {
					s = height_map[ 1 + j*256] - height_map[ 255 + j*256];
					} else if (i==255) {
					s = height_map[ 0 + j*256] - height_map[ 254 + j*256];
					} else {
					s = height_map[ i+1 + j*256] - height_map[ i-1 + j*256];
					}
					
					if (j==0) {
					t = height_map[ i + 1*256] - height_map[ i + 255*256];
					} else if (j==255) {
					t = height_map[ i + 0*256] - height_map[ i + 254*256];
					} else {
					t = height_map[ i + (j+1)*256] - height_map[ i + (j-1)*256];
					}
				}
				s = (s+255)/2;
				t = (t+255)/2;
					
				bump_map[ i + 256*j ] = (s<<8) + t;
			}
		}
		return bump_map;
    }

    
    public void addLight( com.splandorf.render3d.scene.Light l)
    {
		VecSf s_dir = MemMgr.VecSf();
		Alg.cart2sphere( l.dir, s_dir);
		l.s_dir = s_dir;
		_lights.add( l);
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
    
	public boolean inrange(int a, int b, int c)
	{
		return((a>=b)&&(a<c));
	}
	
	public boolean inrange(float a, float b, float c)
	{
		return((a>=b)&&(a<c));
	}

	public static int[] makeLightMap()
	{
		int [] map = new int[128*128];
		float NX;
		float NY;
		float NZ;
		for (int j=0;j<128;j++)
		{
			for (int i=0;i<128;i++)
			{
				NX=((float)i-63)/63;
				NY=((float)j-63)/63;
				NZ=(float)(1-Math.sqrt(NX*NX+NY*NY));
				map[i+j*128]=crop((int)(NZ*255),0,255);
			}
		}
		return map;
	}

/*
 * Old initialize.  Keep around for funsies.
 * 
	public void initialize()
	{
		_mgr = new MemMgr();
		System.err.println("init.");
		Alg.initTrig();

		// Initialize lights, ambient and directional.
		_amb_red   = (float)0.3;
		_amb_green = (float)0.2;
		_amb_blue  = (float)0.2;
		_lights = new ArrayList(10);

		// Light #1
		Light fresh_light = MemMgr.Light();
		Vec3f light_vec = MemMgr.Vec3f( (float)0.0, (float)-1.0, (float)-1.0);
		Alg.normalize( light_vec);
		fresh_light.dir = light_vec;
		fresh_light.red   = (float)1.0;
		fresh_light.green = (float)0.0;
		fresh_light.blue  = (float)0.0;
		fresh_light.intensity = (float)1.0;
		addLight( fresh_light);

		// Light #2
		fresh_light = MemMgr.Light();
		light_vec = MemMgr.Vec3f( (float)0.0, (float)-1.0, (float)1.0);
		Alg.normalize( light_vec);
		fresh_light.dir = light_vec;
		fresh_light.red   = (float)0.0;
		fresh_light.green = (float)0.0;
		fresh_light.blue  = (float)1.0;
		fresh_light.intensity = (float)1.0;
		addLight( fresh_light);

		// Light #3
		fresh_light = MemMgr.Light();
		light_vec = MemMgr.Vec3f( (float)0.0, (float)-1.0, (float)0.0);
		Alg.normalize( light_vec);
		fresh_light.dir = light_vec;
		fresh_light.red   = (float)1.0;
		fresh_light.green = (float)1.0;
		fresh_light.blue  = (float)1.0;
		fresh_light.intensity = (float)1.0;
		addLight( fresh_light);

		// Light #4
		fresh_light = MemMgr.Light();
		light_vec = MemMgr.Vec3f( (float)0.2, (float)0.0, (float)1.0);
		Alg.normalize( light_vec);
		fresh_light.dir = light_vec;
		fresh_light.red   = (float)0.0;
		fresh_light.green = (float)1.0;
		fresh_light.blue  = (float)0.7;
		fresh_light.intensity = (float)1.0;
		addLight( fresh_light);


		_from = MemMgr.Vec3f();
		_at = MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0);
		_up = MemMgr.Vec3f((float)0.0, (float)100.0, (float)0.0);

		_obj1 = MemMgr.Obj();
		_obj1.makeCube();


		_cam_ctm = new Ctm();

		_cam_ctm.set_trans( (float)0.0, (float)0.0, (float)3.0);
		float x_scale = (float)1.0;
		float y_scale = (float)1.0;
		if (_width != _height) {
			if (_width > _height) {
				y_scale = (float)_height / (float) _width;
			} else {
				x_scale = (float)_width / (float)_height;
			}
		}
		_cam_ctm.set_scale( x_scale, y_scale, (float)-(_zoom));
	
		_temp_mat1 = MemMgr.Mat4f();
		_temp_mat2 = MemMgr.Mat4f();
		
		_initialized = true;

	}

	*/

	public void initScene()
	{
		System.err.println("init.");
		_mgr = new MemMgr();
		Alg.initTrig();
		
		// Initialize lights, ambient and directional.
		_amb_red   = (float)0.3;
		_amb_green = (float)0.2;
		_amb_blue  = (float)0.2;
		_lights = new ArrayList<Light>(10);
		
		// Light #1
		Light fresh_light = MemMgr.Light();
		Vec3f light_vec = MemMgr.Vec3f( (float)0.0, (float)-1.0, (float)-1.0);
		Alg.normalize( light_vec);
		fresh_light.dir = light_vec;
		fresh_light.red   = (float)1.0;
		fresh_light.green = (float)0.0;
		fresh_light.blue  = (float)0.0;
		fresh_light.intensity = (float)1.0;
		addLight( fresh_light);
		
		// Light #2
		fresh_light = MemMgr.Light();
		light_vec = MemMgr.Vec3f( (float)0.0, (float)-1.0, (float)1.0);
		Alg.normalize( light_vec);
		fresh_light.dir = light_vec;
		fresh_light.red   = (float)0.0;
		fresh_light.green = (float)0.0;
		fresh_light.blue  = (float)1.0;
		fresh_light.intensity = (float)1.0;
		addLight( fresh_light);
		
		// Light #3
		fresh_light = MemMgr.Light();
		light_vec = MemMgr.Vec3f( (float)0.0, (float)-1.0, (float)0.0);
		Alg.normalize( light_vec);
		fresh_light.dir = light_vec;
		fresh_light.red   = (float)1.0;
		fresh_light.green = (float)1.0;
		fresh_light.blue  = (float)1.0;
		fresh_light.intensity = (float)1.0;
		addLight( fresh_light);
		
		// Light #4
		fresh_light = MemMgr.Light();
		light_vec = MemMgr.Vec3f( (float)0.2, (float)0.0, (float)1.0);
		Alg.normalize( light_vec);
		fresh_light.dir = light_vec;
		fresh_light.red   = (float)0.0;
		fresh_light.green = (float)1.0;
		fresh_light.blue  = (float)0.7;
		fresh_light.intensity = (float)1.0;
		addLight( fresh_light);
		
		_from = MemMgr.Vec3f();
		_at = MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0);
		_up = MemMgr.Vec3f((float)0.0, (float)100.0, (float)0.0);
		
		makeTestScene();
		
		_cam_ctm = new Ctm();
		
		_cam_ctm.set_trans( (float)0.0, (float)0.0, (float)3.5);
		float x_scale = (float)1.0;
		float y_scale = (float)1.0;
		if (_width != _height) {
			if (_width > _height) {
				y_scale = (float)_height / (float) _width;
			} else {
				x_scale = (float)_width / (float)_height;
			}
		}
		_cam_ctm.set_scale( x_scale, -y_scale, (float)-(_zoom));
		
		_temp_mat1 = MemMgr.Mat4f();
		_temp_mat2 = MemMgr.Mat4f();
		
		_initialized = true;
    }
    
    
    protected void makeTestScene()
    {		
		Obj wu_points = GeometryFactory.makeRandomPointfield( 200, (float)5.0);
		wu_points.setName( "WU_POINTS");
		Material wu_mat = MemMgr.Material();
		wu_mat.TRIANGLES = false;
		wu_mat.WIREFRAME = false;
		wu_mat.POINTSET = true;
		wu_mat._color = (255<<24) + (255<<16) + (0<<8) + (255);
		wu_mat._near_color = (255<<24) + (255<<16) + (200<<8) + (200);
		wu_mat._far_color = (255<<24) + (100<<16) + (100<<8) + (255);
		wu_mat._near_red = 500;
		wu_mat._near_green = 200;
		wu_mat._near_blue = 200;
		wu_mat._far_red = 120;
		wu_mat._far_green = 120;
		wu_mat._far_blue = 500;
		wu_mat._gaussian_res = 32;
		wu_mat._n_gaussian_dots = 20;
		wu_mat._gaussian_dots = GeometryFactory.makeGaussianDotArrays( 32, 20);
		wu_mat._wu_array = GeometryFactory.makeWuUnitPointArray( 8);
		wu_mat._pointstyle = Material.GAUSSIAN;
		wu_points.mat = wu_mat;
		
		_obj1 = GeometryFactory.makeOpenBox();
		_obj1.setName( "CUBE1");
		Material t1  = MemMgr.Material();
		t1._lightmodel = Material.TRANSP;
		t1._color = (255<<24) + (70<<16) + (50<<8) + 0;
		t1.BACKFACE_CULL = false;
		t1.WIREFRAME = true;
		t1.TRIANGLES = true;
		t1.ANTIALIAS = true;
		t1._linestyle = Material.THICK;
		_obj1.mat = t1;
		
		_obj4 = GeometryFactory.makeOpenBox();
		_obj4.setName( "CUBE4");
		Material t4 = MemMgr.Material();
		t4._lightmodel = Material.TRANSP;
		t4._color = (255<<24) + (0<<16) + (70<<8) + 50;
		t4.BACKFACE_CULL = false;
		t4.WIREFRAME = true;
		t4.TRIANGLES = true;
		t4.ANTIALIAS = true;
		t4._linestyle = Material.THICK;
		_obj4.mat = t4;
		
		_obj5 = GeometryFactory.makeOpenBox();
		_obj5.setName( "CUBE5");
		Material t5 = MemMgr.Material();
		t5._lightmodel = Material.TRANSP;
		t5._color = (255<<24) + (50<<16) + (0<<8) + 70;
		t5.BACKFACE_CULL = false;
		t5.WIREFRAME = true;
		t5.TRIANGLES = true;
		t5.ANTIALIAS = true;
		t5._linestyle = Material.THICK;
		_obj5.mat = t5;
				
		// Metal donut
		_obj2 = GeometryFactory.makeTorus(16, 24, (float)0.75, (float)0.25, (float)0.0, (float)6.0, (float)0.0, (float)2.0);
		_obj2.setName( "METAL_DONUT");
	
		Material texture = MemMgr.Material();
		texture._lightmodel  = Material.PHONG;
		texture._color = (255<<24) + (255<<16) + (100<<8) + 255;
		//		texture.TEXTURE = true;
		texture.SPEED = Material.FAST;
		texture._env_map = loadTexture("/environments/envplane.gif", texture);
		//		texture._env_map = makeLightMap();
		texture._bump_map = loadBumpMap( "/textures/weave_height2.gif", texture);
		texture._fog_R = 0;
		texture._fog_G = 0;
		texture._fog_B = 0;
		texture._fog_near = (float)2.5;
		texture._fog_far  = (float)4.5;
		texture._fog_near_val = (float)0.0;
		texture._fog_far_val  = (float)1.0;
		texture.ANTIALIAS = true;
		texture._linestyle = Material.THICK;
		texture.BUMP = true;
		_obj2.mat = texture;
		
		// Sphere
		_obj3 = GeometryFactory.makeSphere( 7, 13, (float)0.0, (float)2.0, (float)0.0, (float)2.0);
		_obj3.setName( "SPHERE");
		texture = MemMgr.Material();
		texture._lightmodel  = Material.GOURAUD;
		texture._color = (255<<24) + (255<<16) + (255<<8) + 255;
		//		texture.TEXTURE = true;
		texture.SPEED = Material.FAST;
		//		texture.TRANSPARENT = true;
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
		texture.ANTIALIAS = true;
		texture._linestyle = Material.THICK;
		texture.TRIANGLES = true;
		texture.WIREFRAME = true;
		_obj3.mat = texture;


		Obj flare1 = new Obj( "FLARE1");
		texture = MemMgr.Material();
		texture._lightmodel = Material.FLARE;
		texture.TRIANGLES = false;
		texture.PARTICLE  = true;
		texture.WIREFRAME = false;
		texture._texture = loadTexture( "/sprites/flare1.jpg", texture);
		flare1.ctm().set_trans( (float)0.0, (float)2.7, (float)0.0);
		flare1.mat = texture;
		
		Obj flare2 = new Obj( "FLARE2");
		texture = MemMgr.Material();
		texture._lightmodel = Material.FLARE;
		texture.TRIANGLES = false;
		texture.PARTICLE  = true;
		texture.WIREFRAME = false;
		texture._texture = loadTexture( "/sprites/flare.jpg", texture);
		flare2.ctm().set_trans( (float)0.0, (float)-2.7, (float)0.0);
		flare2.mat = texture;
		
		Obj flare3 = new Obj( "FLARE3");
		texture = MemMgr.Material();
		texture._lightmodel = Material.FLARE;
		texture.TRIANGLES = false;
		texture.PARTICLE  = true;
		texture.WIREFRAME = false;
		texture._texture = loadTexture( "/sprites/flare3.jpg", texture);
		flare3.ctm().set_trans( (float)0.0, (float)0.0, (float)2.7);
		flare3.mat = texture;
		
		Obj flare4 = new Obj( "FLARE4");
		texture = MemMgr.Material();
		texture._lightmodel = Material.FLARE;
		texture.TRIANGLES = false;
		texture.PARTICLE  = true;
		texture.WIREFRAME = false;
		texture._texture = loadTexture( "/sprites/flare3.jpg", texture);
		flare4.ctm().set_trans( (float)0.0, (float)0.0, (float)-2.7);
		flare4.mat = texture;
		
		Anim flare_rot = new Anim("FLARE_ROT");
		flare_rot.addChild( flare1);
		flare_rot.addChild( flare2);
		flare_rot.addChild( flare3);
		flare_rot.addChild( flare4);
		flare_rot.initAnim( 
			Anim.ROT, Anim.CONTINUOUS, 
			(float)0.0, (float)99.0, (float)2.8,
			MemMgr.Vec3f( (float)0.0, (float)0.0, (float)0.0),
			MemMgr.Vec3f( (float)6.28318, (float)0.0, (float)0.0) 
		);
		_scene_root.addChild( flare_rot);
		
		/*
		Anim rot_root = new Anim("ROT1", this);
		rot_root.initAnim( Anim.ROT, Anim.PING_PONG, (float)0.0, (float)99.0, (float)6.0,
		MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0),
		MemMgr.Vec3f((float)0.0, (float)0.0, (float)6.28318) );
		_scene_root.addChild( rot_root);
		*/
		
		_obj1.ctm().set_scale( (float)1.3, (float)0.5, (float)0.5);
		Anim cube1Anim = new Anim("ANIM1");
		cube1Anim.initAnim( Anim.ROT, Anim.CONTINUOUS, (float)0.0, (float)99.0, (float)9.1,
					MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0),
					MemMgr.Vec3f((float)0.0, (float)6.28318, (float)0.0) );
		cube1Anim.addChild( _obj1);
		_scene_root.addChild( cube1Anim);
		
		_obj4.ctm().set_scale( (float)0.6, (float)1.75, (float)0.4);
		Anim cube4Anim = new Anim("ANIM4");
		cube4Anim.initAnim( Anim.ROT, Anim.CONTINUOUS, (float)0.0, (float)99.0, (float)8.1,
					MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0),
					MemMgr.Vec3f((float)6.28318, (float)0.0, (float)0.0) );
		cube4Anim.addChild( _obj4);
		_scene_root.addChild( cube4Anim);
		
		_obj5.ctm().set_scale( (float)0.5, (float)0.6, (float)1.8);
		Anim cube5Anim = new Anim("ANIM5");
		cube5Anim.initAnim( Anim.ROT, Anim.CONTINUOUS, (float)0.0, (float)99.0, (float)7.0,
					MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0),
					MemMgr.Vec3f((float)0.0, (float)0.0, (float)6.28318) );
		cube5Anim.addChild( _obj5);
		_scene_root.addChild( cube5Anim);
		
		_obj2.ctm().set_scale( (float)1.6, (float)1.6, (float)1.6);
		Group xform2 = new Group("XFORM2");
		xform2.ctm().set_trans( (float)-2.9, (float)0.0, (float)0.0);
		Anim cube2Anim = new Anim("ANIM2");
		cube2Anim.initAnim( Anim.ROT, Anim.CONTINUOUS, (float)0.0, (float)99.0, (float)2.8,
					MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0),
					MemMgr.Vec3f((float)0.0, (float)0.0, (float)6.28318) );
		cube2Anim.addChild( _obj2);
		xform2.addChild( cube2Anim);
		//	_scene_root.addChild( xform2);
		
		_obj3.ctm().set_scale( (float)1.6, (float)1.6, (float)1.6);
		Group xform3 = new Group("XFORM3");
		xform3.ctm().set_trans( (float)2.9, (float)0.0, (float)0.0);
		Anim cube3Anim = new Anim("ANIM3");
		cube3Anim.initAnim( Anim.ROT, Anim.CONTINUOUS, (float)0.0, (float)99.0, (float)3.0,
					MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0),
					MemMgr.Vec3f((float)0.0, (float)0.0, (float)6.28318) );
		cube3Anim.addChild( _obj3);
		xform3.addChild( cube3Anim);
		//	_scene_root.addChild( xform3);
		
		_scene_root.addChild( wu_points);

    }
    
/* 
	protected void makeBlendArray( int color1, int color2, int [] colors)
	{
		
		float rf = (float)((color1>>16) & 255);
		float gf = (float)((color1>>8) & 255);
		float bf = (float)((color1) & 255);
		float rb = (float)((color2>>16) & 255);
		float gb = (float)((color2>>8) & 255);
		float bb = (float)((color2) & 255);


		float blend1 = (float)0.0;
		float blend2 = (float)0.0;

		for (int i=0; i<256; i++) {
			blend1 = (float)i/(float)255.0;
			blend2 = (float)1.0 - blend1;
			colors[i] = ((int)(blend2 * rf + blend1 * rb)<<16) +
						((int)(blend2 * gf + blend1 * gb)<<8) +
						((int)(blend2 * bf + blend1 * bb)) +
						(255<<24);
		}
	}
		*/


	public void addToTranspQueue( Mat4f xform, Mat4f nxform, Obj obj)
    {
		_transpQueue.add( xform);
		_transpQueue.add( nxform);
		_transpQueue.add( obj);
    }
    
    protected void renderTranspObjects()
    {
		try {
			Mat4f  xform;
			Mat4f  nxform;
			Obj    obj;
			
			while (_transpQueue.size() > 0) {
				xform  = (Mat4f)_transpQueue.get(0);
				nxform = (Mat4f)_transpQueue.get(1);
				obj    = (Obj)  _transpQueue.get(2);
				if (obj.mat.PARTICLE == true) {
					Particle.drawParticle( xform, obj);
				} else {
					drawTriangles( xform, obj.tlist);
				}
				_transpQueue.remove(0);
				_transpQueue.remove(0);
				_transpQueue.remove(0);
				MemMgr.done( xform);
				MemMgr.done( nxform);
			}
		} catch (Exception e) {
			System.err.println("Error, wrong datatypes stored in transpQueue!");
		}
    }

	public void illuminate( Vec3f n, Vec3f light)
	{
		float dot   = (float)0.0;
		Light l = null;
		light.x = (float)0.0;
		light.y = (float)0.0;
		light.z = (float)0.0;

		for (int i=0; i<_lights.size(); i++) {
			l = (Light)_lights.get(i);
			dot = -Alg.dot( l.dir, n);
			if (dot > 0.0) {
				light.x += dot * l.intensity * l.red;
				light.y += dot * l.intensity * l.green;
				light.z += dot * l.intensity * l.blue;
				light.w += dot * l.intensity;
			}
		}
		//light.x += _amb_red;
		//light.y += _amb_green;
		//light.z += _amb_blue;
		if (light.x > 1.0) light.x = (float)1.0;
		if (light.y > 1.0) light.y = (float)1.0;
		if (light.z > 1.0) light.z = (float)1.0;
		if (light.w > 1.0) light.w = (float)1.0;
	}

	public void illuminate( VecSf n, Vec3f light)
	{
		float dot   = (float)0.0;
		Light l = null;
		light.x = (float)0.0;
		light.y = (float)0.0;
		light.z = (float)0.0;

		for (int i=0; i<_lights.size(); i++) {
			l = (Light)_lights.get(i);
			dot = -Alg.dot( l.s_dir, n);
			if (dot > 0.0) {
				light.x += dot * l.intensity * l.red;
				light.y += dot * l.intensity * l.green;
				light.z += dot * l.intensity * l.blue;
				light.w += dot * l.intensity;
			}
		}
		//light.x += _amb_red;
		//light.y += _amb_green;
		//light.z += _amb_blue;
		if (light.x > 1.0) light.x = (float)1.0;
		if (light.y > 1.0) light.y = (float)1.0;
		if (light.z > 1.0) light.z = (float)1.0;
		if (light.w > 1.0) light.w = (float)1.0;
	}

	public void drawTriangles( Mat4f m, ArrayList<Triangle> tlist)
    {
		Vec3f p1 = MemMgr.Vec3f();
		Vec3f p2 = MemMgr.Vec3f();
		Vec3f p3 = MemMgr.Vec3f();
		Vec3f c  = MemMgr.Vec3f();
		
		Vec3f test1  = MemMgr.Vec3f();
		Vec3f test2  = MemMgr.Vec3f();
		Vec3f test3  = MemMgr.Vec3f();
			
		Vec3f light = MemMgr.Vec3f();
		Vec3f look = MemMgr.Vec3f( (float)0.0, (float)0.0, (float)-1.0);
		Alg.mult( _cam_ctm.ctm(), look);
		int cx = _width / 2;
		int cy = _height / 2;
		float xm = (float)cx*(float)0.6;
		float ym = (float)cy*(float)0.6;
		/* 
		int color = 0;
		int red, green, blue, inten;
		float fog;
		*/
		Triangle t   = null;
		Material mat = null;
		
		for (int i=0; i<tlist.size(); i++) {
			
			// Find triangle normal
			t = (Triangle)tlist.get(i);
			mat = t.mat;
			if (mat==null) {
				mat = t.obj.mat;
			}
			Alg.mult( m, t.v1.p, p1);
			Alg.mult( m, t.v2.p, p2);
			Alg.mult( m, t.v3.p, p3);
			
			// Clip against viewport (drop triangles behind eye)
			if (p1.z > 0.0 && p2.z > 0.0 && p3.z > 0.0 ) {
			
				float xf, yf;
				
				// Calculate screen coordintes.  Store integer screen pixel location
				// [0..width, 0..height], and 16-bit fixed-point subpixel remainder
				// (to be used for subpixel-accurate rendering of lines and polygons].
				
				// X and Y coords must be calculated to accurately determine visibility.
				// Z coords must only be calculated if polygon is indeed visible (later).

				// X-coords
				xf     = (p1.x / p1.z * xm);
				t.v1.x = (int)xf;
				t.v1.xfrac = (int)( (xf - (float)t.v1.x) * (float)65536.0);
				t.v1.x += cx;
				xf     = (p2.x / p2.z * xm);
				t.v2.x = (int)xf;
				t.v2.xfrac = (int)( (xf - (float)t.v2.x) * (float)65536.0);
				t.v2.x += cx;
				xf     = (p3.x / p3.z * xm);
				t.v3.x = (int)xf;
				t.v3.xfrac = (int)( (xf - (float)t.v3.x) * (float)65536.0);
				t.v3.x += cx;
				
				// Y-coords
				yf     = (p1.y / p1.z * ym);
				t.v1.y = (int)yf;
				t.v1.yfrac = (int)( (yf - (float)t.v1.y) * (float)65536.0);
				t.v1.y += cy;
				yf     = (p2.y / p2.z * ym);
				t.v2.y = (int)yf;
				t.v2.yfrac = (int)( (yf - (float)t.v2.y) * (float)65536.0);
				t.v2.y += cy;
				yf     = (p3.y / p3.z * ym);
				t.v3.y = (int)yf;
				t.v3.yfrac = (int)( (yf - (float)t.v3.y) * (float)65536.0);
				t.v3.y += cy;
				
				test1.x = (float)(t.v2.x - t.v1.x);
				test1.y = (float)(t.v2.y - t.v1.y);
				test1.z = (float)0.0;
				test2.x = (float)(t.v3.x - t.v2.x);
				test2.y = (float)(t.v3.y - t.v2.y);
				test2.z = (float)0.0;

				// Check to see if front-facing based on screen-space
				// projection of ordered verts 1,2,3: are they
				// clockwise (visible) or counterclockwise (invisible)?
				// Note: cross-product of screen-space projected points determines
				// ordering.  Positive = clockwise, negative = counterclockwise

				Alg.normalize( test1);
				Alg.normalize( test2);
				Alg.cross( test1, test2, test3);
					
				if ( test3.z > -0.01 || mat.BACKFACE_CULL == false) {

					// Z-calculations need only be done if polygon is to
					// be rendered.
				
					// Z-coords, stored as integer value between [0..(MAXINT/1000)]
					// This means that all z-values > 1000.0 will be in error.
					t.v1.z = (int)( (p1.z/(float)1000.0)*(float)(Integer.MAX_VALUE) );
					t.v2.z = (int)( (p2.z/(float)1000.0)*(float)(Integer.MAX_VALUE) );
					t.v3.z = (int)( (p3.z/(float)1000.0)*(float)(Integer.MAX_VALUE) );
					
					// Calculate 1/z and store in 16-bit fixed-point format.
					// [used for polygon scan-conversion, z-buffering, and such].
					// 1/z is linear in screen-space.
					t.v1.invz = (float)1.0 / p1.z;
					t.v2.invz = (float)1.0 / p2.z;
					t.v3.invz = (float)1.0 / p3.z;
					t.v1.zbuf = (int)(t.v1.invz * (float)10000.0);
					if (t.v1.zbuf > MAX24BIT) t.v1.zbuf = MAX24BIT;
					if (t.v1.zbuf < 1) t.v1.zbuf = 1;
					t.v2.zbuf = (int)(t.v2.invz * (float)10000.0);
					if (t.v2.zbuf > MAX24BIT) t.v2.zbuf = MAX24BIT;
					if (t.v2.zbuf < 1) t.v2.zbuf = 1;
					t.v3.zbuf = (int)(t.v3.invz * (float)10000.0);
					if (t.v3.zbuf > MAX24BIT) t.v3.zbuf = MAX24BIT;
					if (t.v3.zbuf < 1) t.v3.zbuf = 1;
		    		    
					drawTriangleWithMaterial( t, p1, p2, p3, light, mat);
				}
			}	
		}
		MemMgr.done( p1 );
		MemMgr.done( p2 );
		MemMgr.done( p3 );
		MemMgr.done( c  );
		MemMgr.done( light );
		MemMgr.done( look );
	}

	public void drawTriangleWithMaterial (
		Triangle t,
		Vec3f p1,
		Vec3f p2,
		Vec3f p3,
		Vec3f light,
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

			// Get light contribution at vertex 1
			color = mat._color;
			Alg.mult( t.obj.ctm().normal_ctm(), t.v1.n, t.v1.w_n);
			Alg.normalize( t.v1.w_n);
			// Get light contribution at vertex 2
			Alg.mult( t.obj.ctm().normal_ctm(), t.v2.n, t.v2.w_n);
			Alg.normalize( t.v2.w_n);
			// Get light contribution at vertex 3
			Alg.mult( t.obj.ctm().normal_ctm(), t.v3.n, t.v3.w_n);
			Alg.normalize( t.v3.w_n);
		
			PhongTriangle.drawPhongTriangle( t, mat);
		} 
		else if (mat._lightmodel == Material.GOURAUD) {
				
			if (mat.TEXTURE == true) {

				// Get light contribution at vertex 1
				color = mat._color;
				Alg.mult( t.obj.ctm().normal_ctm(), t.v1.n, n);
				Alg.normalize( n);
				illuminate( n, light);
				t.v1.r = ((int)((float)255.0 * light.x)<<16);
				t.v1.g = ((int)((float)255.0 * light.y)<<16);
				t.v1.b = ((int)((float)255.0 * light.z)<<16);
				// Get light contribution at vertex 2
				Alg.mult( t.obj.ctm().normal_ctm(), t.v2.n, n);
				Alg.normalize( n);
				illuminate( n, light);
				t.v2.r = ((int)((float)255.0 * light.x)<<16);
				t.v2.g = ((int)((float)255.0 * light.y)<<16);
				t.v2.b = ((int)((float)255.0 * light.z)<<16);
				// Get light contribution at vertex 3
				Alg.mult( t.obj.ctm().normal_ctm(), t.v3.n, n);
				Alg.normalize( n);
				illuminate( n, light);
				t.v3.r = ((int)((float)255.0 * light.x)<<16);
				t.v3.g = ((int)((float)255.0 * light.y)<<16);
				t.v3.b = ((int)((float)255.0 * light.z)<<16);
			
				if (mat.SPEED >= Material.FAST) {
					GouraudTriangle.drawFastGouraudTextureTriangle( t, mat);
				} else {
					GouraudTriangle.drawGouraudTextureTriangle( t, mat);
				}
			} else {

				// Get light contribution at vertex 1
				color = mat._color;
				Alg.mult( t.obj.ctm().normal_ctm(), t.v1.n, n);
				Alg.normalize( n);
				illuminate( n, light);
				t.v1.r = ((int)((float)((color>>16) & 255) * light.x)<<16);
				t.v1.g = ((int)((float)((color>>8 ) & 255) * light.y)<<16);
				t.v1.b = ((int)((float)( color      & 255) * light.z)<<16);
				// Get light contribution at vertex 2
				Alg.mult( t.obj.ctm().normal_ctm(), t.v2.n, n);
				Alg.normalize( n);
				illuminate( n, light);
				t.v2.r = ((int)((float)((color>>16) & 255) * light.x)<<16);
				t.v2.g = ((int)((float)((color>>8 ) & 255) * light.y)<<16);
				t.v2.b = ((int)((float)( color      & 255) * light.z)<<16);
				// Get light contribution at vertex 3
				Alg.mult( t.obj.ctm().normal_ctm(), t.v3.n, n);
				Alg.normalize( n);
				illuminate( n, light);
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

				p1.z *= _zoom;
				p2.z *= _zoom;
				p3.z *= _zoom;
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
					if (mat._fog_type == Material.SQUARE) fog *= fog;
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
					if (mat._fog_type == Material.SQUARE) fog *= fog;
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
					if (mat._fog_type == Material.SQUARE) fog *= fog;
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
				illuminate( n, light);
				
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
				illuminate( n, light);
				
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

	public void drawPointset( Mat4f m, ArrayList<Vertex> vlist, Material mat)
    {
		Particle.drawPointset( m, vlist, mat);
	}

    public void drawWireframe( Mat4f m, ArrayList<Edge> elist, Material mat)
    {
		Line.drawWireframe( m, elist, mat);
	}
}

