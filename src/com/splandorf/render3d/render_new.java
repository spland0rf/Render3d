//================================================================
//
// render.java
//
// a fully-interactive psychoelectronic journey into your
// philo-sensorium and out the back of your amygdala.
//
// (c) zbigniew mufosowicz -- 1899
//================================================================

import java.applet.*;
import java.awt.*;
import java.util.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

//import iicm.vrml.pw.*;

//import iicm.vrml.pwsample.*;
import Vec3f;
import Vec3i;
import VecSf;
import VecSi;
import Mat4f;

import MemMgr;


public class render extends Applet implements Runnable
{
    MemoryImageSource mis = null;
    Image temp_image = null;

    int [] _wu_array = null;

    int [] _zero_pix = null;
    int [] _maxint_pix = null;
    int [] _pix = null;
    int [] _zbuf = null;
    int [] _env_map = null;
    int [] _background = null;

    int _width  = -1;
    int _height = -1;
    Image _buffer = null;
    Canvas _canvas = null;
    int _bgcolor = 0;
    MemMgr _mgr = null;
    Thread _renderThread = null;
    boolean _initialized=false;
    double _incr = 0.0;
    int _garbage_counter = 0;
    static Applet _app = null;
    
    int SUBDIV_SIZE = 16;
    int MAX24BIT = (255<<16) + (255<<8) + 255;

    rGroup _scene_root = null;

    Ctm _cam_ctm = null;
    Vec3f _from = null;
    Vec3f _at = null;
    Vec3f _up = null;
    float _zoom = (float)1.3;
    
    // Light info
    Vector _lights = null;
    float _amb_red   = (float)0.0;
    float _amb_green = (float)0.0;
    float _amb_blue  = (float)0.0;
    
    // Temporaries for testing:
    int [] _red_blend;
    int _red_color;
    int _blue_color;
    int [] _blue_blend;
    
    Obj _cube1 = null;
    Obj _cube2 = null;
    Obj _cube3 = null;
    Obj _cube4 = null;
    Obj _cube5 = null;

    Mat4f _temp_mat1 = null;
    Mat4f _temp_mat2 = null;

    Vector _transpQueue = null;
    
    public render()
    {
	_app = this;
    }
    
    void parseVRML()
    {
	/*
	try {
	    String vrmlfile = getParameter( "vrmlfile");
	    URL vrmlurl = new URL( getCodeBase(), vrmlfile);
	    VRMLparser parser = new VRMLparser (new BufferedInputStream ( vrmlurl.openStream() ));
	    long time = System.currentTimeMillis ();
	    GroupNode root = parser.readStream ();
	    time = System.currentTimeMillis () - time;
	    System.out.println ("*** Parsing time (ms): " + time);
	    if (root != null) {
		SamTraverser traverser = new SamTraverser (this);
		
		System.out.println ("====================");
		traverser.traverse (root, _scene_root);
		System.out.println ("====================");
	    } else {
		System.out.println ("error on parsing vrmlfile.wrl");
	    }
	} catch (Exception e) {
	    System.out.println ("error on reading vrmlfile.wrl");
	    System.out.println (e.getMessage ());  // just prints file name
	    e.printStackTrace();
	}
	*/
    }
    
    public void init()
    {
	_scene_root = new rGroup( "ROOT", this);

	//parseVRML();

	_width  = this.size().width;
	_height = this.size().height;
	
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
	
	// Make our front buffer image
	_buffer = _canvas.createImage( _width, _height);
	_pix = new int[ _width * _height];
	_zbuf = new int[ _width * _height];
	_zero_pix = new int[ _width * _height];
	_maxint_pix = new int[ _width * _height];
	//		_bgcolor = (255<<24) + (255<<16) + (200<<8) + 200;
	_bgcolor = (255<<24) + (0<<16) + 0;
	for (int i=0; i<_width*_height; i++) {
		_zero_pix[i] = _bgcolor;
		_maxint_pix[i] = Integer.MAX_VALUE;
	}
	_red_color = (255<<24) + (250<<16);
	_red_blend = new int[256];
	makeBlendArray( _red_color, _bgcolor, _red_blend);
	_blue_color = (255<<24) + (255<<16) + (255<<8) + (255);
	_blue_blend = new int[256];
	makeBlendArray( _blue_color, _bgcolor, _blue_blend);
	
	//_env_map = loadTexture("environments/envplane.gif");

	mis = new MemoryImageSource( _width, _height, _pix, 0, _width);
	mis.setAnimated( true);
	temp_image = createImage( mis);
	
	_transpQueue = new Vector( 10);

	_background = loadTexture( "textures/night_sky_background.jpg", null);

	_wu_array = makeWuUnitPointArray( 8);
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
    
    public static int[] loadTexture( String filename, rMaterial mat)
    {
	// Load the image
	Image texture_img = null;
	try {
	    MediaTracker mt = new MediaTracker(_app);
	    texture_img = _app.getImage( _app.getCodeBase(), filename);
	    mt.addImage( texture_img, 0);
	    mt.waitForAll();
	} catch (InterruptedException e) {
	    System.err.println("render::loadTexture(): Couldn't load " + filename);
	    e.printStackTrace();
	}
	
	// Grab pixel array
	int src_width = texture_img.getWidth( _app);
	int src_height = texture_img.getHeight( _app);
	int[] src_pixels = new int[ src_width * src_height];
	PixelGrabber pg = new PixelGrabber(
					   texture_img, 0, 0, src_width, src_height, src_pixels, 0, src_width
					   );
	try {
	    pg.grabPixels();
	} catch (Exception e) {
	    System.err.println("For some incredibly lame reason, render::loadTexture() couldn't grab pixels.");
	    e.printStackTrace();
	}
	
	// Set all pixels whose opacity is <255 to zero.
	// If a texture pixel is zero, it is treated as
	// a decal cut-out, and not rendered at all.
	for (int i=0; i<src_width * src_height; i++) {
	    if ( ((src_pixels[i]>>24)&255) != 255) {
		src_pixels[i] = 0;
	    }
	}

	if (mat != null) {
	    mat._sprite_width = src_width;
	    mat._sprite_height = src_height;
	}
	return src_pixels;
    }

    public static int[] loadBumpMap( String filename, rMaterial mat)
    {
	int [] height_map = loadTexture( filename, mat);

	int [] bump_map = new int[256*256];

	if (height_map.length != 256*256) {
	    System.err.println("Bump maps must be 256x256 pixels!");
	    for (int i=0; i<256*256; i++) bump_map[i] = (127<<8) + 127;
	    return bump_map;
	}

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
    
    
    public void addLight( Light l)
    {
	VecSf s_dir = MemMgr.VecSf();
	Alg.cart2sphere( l.dir, s_dir);
	l.s_dir = s_dir;
	_lights.addElement( l);
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
	int [] map = new int[256*256];
	float NX;
	float NY;
	float NZ;
	int diggy;
	for (int j=0;j<256;j++) {
	    for (int i=0;i<256; i++) {
		NX=((float)i-127)/127;
		NY=((float)j-127)/127;
		NZ=(float)(1-Math.sqrt(NX*NX+NY*NY));
		diggy = crop((int)(NZ*255),0,255);
		map[i+j*256] = (255<<24) + (diggy<<16) + (diggy<<8) + diggy;
	    }
	}
	return map;
    }
    
    public void initialize()
    {
	_mgr = new MemMgr();
	System.err.println("init.");
	Alg.initTrig();
	
	// Initialize lights, ambient and directional.
	_amb_red   = (float)0.3;
	_amb_green = (float)0.2;
	_amb_blue  = (float)0.2;
	_lights = new Vector(10);
	
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
    
    protected void createRandomPointfield( Obj pointfield, int n_points, float radius)
    {
	float x, y, z;
	for ( int i=0; i<n_points; i++) {
	    x = ((float)(Math.random()) * (float)2.0 * radius) - radius;
	    y = ((float)(Math.random()) * (float)2.0 * radius) - radius;
	    z = ((float)(Math.random()) * (float)2.0 * radius) - radius;
	    pointfield.addVertex( MemMgr.Vertex( x, y, z) );
	}
    }

    
    protected void makeTestScene()
    {
	
	
	Obj wu_points = new Obj("WU", this);
        createRandomPointfield( wu_points, 200, (float)5.0);
	rMaterial wu_mat = MemMgr.rMaterial();
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
	wu_mat._gaussian_dots = makeGaussianDotArrays( 32, 20);
	wu_mat._pointstyle = rMaterial.GAUSSIAN;
	wu_points.mat = wu_mat;
	
	_cube1 = new Obj("CUBE1", this);
	_cube1.makeOpenBox();
	rMaterial t1  = MemMgr.rMaterial();
	t1._lightmodel = rMaterial.TRANSP;
	t1._color = (255<<24) + (70<<16) + (50<<8) + 0;
	t1.BACKFACE_CULL = false;
	t1.WIREFRAME = true;
	t1.TRIANGLES = true;
	t1.ANTIALIAS = true;
	t1._linestyle = rMaterial.THICK;
	_cube1.mat = t1;
	
	_cube4 = new Obj("CUBE4", this);
	_cube4.makeOpenBox();
	rMaterial t4 = MemMgr.rMaterial();
	t4._lightmodel = rMaterial.TRANSP;
	t4._color = (255<<24) + (0<<16) + (70<<8) + 50;
	t4.BACKFACE_CULL = false;
	t4.WIREFRAME = true;
	t4.TRIANGLES = true;
	t4.ANTIALIAS = true;
	t4._linestyle = rMaterial.THICK;
	_cube4.mat = t4;
	
	_cube5 = new Obj("CUBE5", this);
	_cube5.makeOpenBox();
	rMaterial t5 = MemMgr.rMaterial();
	t5._lightmodel = rMaterial.TRANSP;
	t5._color = (255<<24) + (50<<16) + (0<<8) + 70;
	t5.BACKFACE_CULL = false;
	t5.WIREFRAME = true;
	t5.TRIANGLES = true;
	t5.ANTIALIAS = true;
	t5._linestyle = rMaterial.THICK;
	_cube5.mat = t5;
	
	_cube2 = new Obj("CUBE2", this);
	_cube2.makeTorus(16, 24, (float)0.75, (float)0.25, (float)0.0, (float)6.0, (float)0.0, (float)2.0);
	
	
	// Metal donut
	rMaterial texture = MemMgr.rMaterial();
	texture._lightmodel  = rMaterial.PHONG;
	texture._color = (255<<24) + (255<<16) + (100<<8) + 255;
	//		texture.TEXTURE = true;
	texture.SPEED = rMaterial.FAST;
	texture._env_map = loadTexture("environments/envplane.gif", texture);
	//		texture._env_map = makeLightMap();
	texture._bump_map = loadBumpMap( "textures/weave_height2.gif", texture);
	texture._fog_R = 0;
	texture._fog_G = 0;
	texture._fog_B = 0;
	texture._fog_near = (float)2.5;
	texture._fog_far  = (float)4.5;
	texture._fog_near_val = (float)0.0;
	texture._fog_far_val  = (float)1.0;
	texture.ANTIALIAS = true;
	texture._linestyle = rMaterial.THICK;
	texture.BUMP = true;
	_cube2.mat = texture;
	
	_cube3 = new Obj("CUBE3", this);
	_cube3.makeSphere( 7, 13, (float)0.0, (float)2.0, (float)0.0, (float)2.0);
	
	texture = MemMgr.rMaterial();
	texture._lightmodel  = rMaterial.GOURAUD;
	texture._color = (255<<24) + (255<<16) + (255<<8) + 255;
	//		texture.TEXTURE = true;
	texture.SPEED = rMaterial.FAST;
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
	texture._linestyle = rMaterial.THICK;
	texture.TRIANGLES = true;
	texture.WIREFRAME = true;
	_cube3.mat = texture;
	


	Obj flare1 = new Obj( "FLARE1", this);
	texture = MemMgr.rMaterial();
	texture._lightmodel = rMaterial.FLARE;
	texture.TRIANGLES = false;
	texture.PARTICLE  = true;
	texture.WIREFRAME = false;
	texture._texture = loadTexture( "sprites/flare1.jpg", texture);
	flare1.ctm().set_trans( (float)0.0, (float)2.7, (float)0.0);
	flare1.mat = texture;
	
	Obj flare2 = new Obj( "FLARE2", this);
	texture = MemMgr.rMaterial();
	texture._lightmodel = rMaterial.FLARE;
	texture.TRIANGLES = false;
	texture.PARTICLE  = true;
	texture.WIREFRAME = false;
	texture._texture = loadTexture( "sprites/flare.jpg", texture);
	flare2.ctm().set_trans( (float)0.0, (float)-2.7, (float)0.0);
	flare2.mat = texture;
	
	Obj flare3 = new Obj( "FLARE3", this);
	texture = MemMgr.rMaterial();
	texture._lightmodel = rMaterial.FLARE;
	texture.TRIANGLES = false;
	texture.PARTICLE  = true;
	texture.WIREFRAME = false;
	texture._texture = loadTexture( "sprites/flare3.jpg", texture);
	flare3.ctm().set_trans( (float)0.0, (float)0.0, (float)2.7);
	flare3.mat = texture;
	
	Obj flare4 = new Obj( "FLARE4", this);
	texture = MemMgr.rMaterial();
	texture._lightmodel = rMaterial.FLARE;
	texture.TRIANGLES = false;
	texture.PARTICLE  = true;
	texture.WIREFRAME = false;
	texture._texture = loadTexture( "sprites/flare3.jpg", texture);
	flare4.ctm().set_trans( (float)0.0, (float)0.0, (float)-2.7);
	flare4.mat = texture;
	
	Anim flare_rot = new Anim("FLARE_ROT", this);
	flare_rot.addChild( flare1);
	flare_rot.addChild( flare2);
	flare_rot.addChild( flare3);
	flare_rot.addChild( flare4);
	flare_rot.initAnim( Anim.ROT, Anim.CONTINUOUS, (float)0.0, (float)99.0, (float)2.8,
			    MemMgr.Vec3f( (float)0.0, (float)0.0, (float)0.0),
			    MemMgr.Vec3f( (float)6.28318, (float)0.0, (float)0.0) );
		_scene_root.addChild( flare_rot);
	
	/*
	  Anim rot_root = new Anim("ROT1", this);
	  rot_root.initAnim( Anim.ROT, Anim.PING_PONG, (float)0.0, (float)99.0, (float)6.0,
	  MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0),
	  MemMgr.Vec3f((float)0.0, (float)0.0, (float)6.28318) );
	  _scene_root.addChild( rot_root);
	*/
	
	_cube1.ctm().set_scale( (float)1.3, (float)0.5, (float)0.5);
	Anim cube1Anim = new Anim("ANIM1", this);
	cube1Anim.initAnim( Anim.ROT, Anim.CONTINUOUS, (float)0.0, (float)99.0, (float)9.1,
			    MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0),
			    MemMgr.Vec3f((float)0.0, (float)6.28318, (float)0.0) );
	cube1Anim.addChild( _cube1);
	_scene_root.addChild( cube1Anim);
	
	_cube4.ctm().set_scale( (float)0.6, (float)1.75, (float)0.4);
	Anim cube4Anim = new Anim("ANIM4", this);
	cube4Anim.initAnim( Anim.ROT, Anim.CONTINUOUS, (float)0.0, (float)99.0, (float)8.1,
			    MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0),
			    MemMgr.Vec3f((float)6.28318, (float)0.0, (float)0.0) );
	cube4Anim.addChild( _cube4);
	_scene_root.addChild( cube4Anim);
	
	_cube5.ctm().set_scale( (float)0.5, (float)0.6, (float)1.8);
	Anim cube5Anim = new Anim("ANIM5", this);
	cube5Anim.initAnim( Anim.ROT, Anim.CONTINUOUS, (float)0.0, (float)99.0, (float)7.0,
			    MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0),
			    MemMgr.Vec3f((float)0.0, (float)0.0, (float)6.28318) );
	cube5Anim.addChild( _cube5);
	_scene_root.addChild( cube5Anim);
	
	_cube2.ctm().set_scale( (float)1.6, (float)1.6, (float)1.6);
	rGroup xform2 = new rGroup("XFORM2", this);
	xform2.ctm().set_trans( (float)-2.9, (float)0.0, (float)0.0);
	Anim cube2Anim = new Anim("ANIM2", this);
	cube2Anim.initAnim( Anim.ROT, Anim.CONTINUOUS, (float)0.0, (float)99.0, (float)2.8,
			    MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0),
			    MemMgr.Vec3f((float)0.0, (float)0.0, (float)6.28318) );
	cube2Anim.addChild( _cube2);
	xform2.addChild( cube2Anim);
	//	_scene_root.addChild( xform2);
	
	_cube3.ctm().set_scale( (float)1.6, (float)1.6, (float)1.6);
	rGroup xform3 = new rGroup("XFORM3", this);
	xform3.ctm().set_trans( (float)2.9, (float)0.0, (float)0.0);
	Anim cube3Anim = new Anim("ANIM3", this);
	cube3Anim.initAnim( Anim.ROT, Anim.CONTINUOUS, (float)0.0, (float)99.0, (float)3.0,
			    MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0),
			    MemMgr.Vec3f((float)0.0, (float)0.0, (float)6.28318) );
	cube3Anim.addChild( _cube3);
	xform3.addChild( cube3Anim);
	//	_scene_root.addChild( xform3);
	
	_scene_root.addChild( wu_points);

    }
    
    
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
    
    public void paint(Graphics gx)
	{
	    /*
	      System.err.println("Paint.");
	      gx.setColor(new Color( 255,200,200) );
	      gx.fillRect(0,0,this.size().width,this.size().height);
	      gx.setColor(new Color(150,0,0));
	      gx.drawString("rendering nightmare",10,20);
	      gx.drawLine(10,26,160,26);
	      gx.drawString("�1899 by Splandorf M.",10,42);
	    */
	}
    
    public void start()
    {
	if (_renderThread == null)
	    {
		_renderThread = new Thread(this);
		_renderThread.start();
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
    
    public void run()
    {
	long startTime = System.currentTimeMillis();
	long oldTime = startTime;
	long newTime = 0;
	int  framerate = 0;
	
	System.arraycopy( _background, 0, _pix, 0, _height*_width);
	System.arraycopy( _maxint_pix, 0, _zbuf, 0, _height*_width);
	
	float _time = (float)0.0;
	int running = 0;
	while(true) {
	    //newTime = System.currentTimeMillis();
	    
	    renderScene( _time);//(float)(newTime-startTime) / (float)1000.0 );
	    _time += 0.01;
	    
	    _renderThread.yield();
	    
	    //			try {
	    //			    _renderThread.sleep(2000);
	    //			} catch (Exception e) {}
	    
	    _garbage_counter++;
	    if (_garbage_counter>=50) {
		System.gc();
		_garbage_counter = 0;
		framerate++;
	    }
	    if (framerate>=2) {
		newTime = System.currentTimeMillis();
		System.err.println("fps: " + (100000 / (newTime-oldTime)) );
		oldTime = newTime;
		framerate = 0;
	    }
	}
    }
    
    public void addToTranspQueue( Mat4f xform, Mat4f nxform, Obj obj)
    {
	_transpQueue.addElement( xform);
	_transpQueue.addElement( nxform);
	_transpQueue.addElement( obj);
    }
    
    protected void renderTranspObjects()
    {
	try {
	    Mat4f  xform;
	    Mat4f  nxform;
	    Obj    obj;
	    
	    while (_transpQueue.size() > 0) {
		xform  = (Mat4f)_transpQueue.elementAt(0);
		nxform = (Mat4f)_transpQueue.elementAt(1);
		obj    = (Obj)  _transpQueue.elementAt(2);
		if (obj.mat.PARTICLE == true) {
		    drawParticle( xform, obj);
		} else {
		    drawTriangles( xform, nxform, obj.tlist);
		}
		_transpQueue.removeElementAt(0);
		_transpQueue.removeElementAt(0);
		_transpQueue.removeElementAt(0);
		MemMgr.done( xform);
		MemMgr.done( nxform);
	    }
	} catch (Exception e) {
	    System.err.println("Error, wrong datatypes stored in transpQueue!");
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
	    
	    _scene_root.render( _cam_ctm.inv_ctm(), Alg.IDENT_MAT, cur_time );
	    
	    if (_transpQueue.size() > 0) renderTranspObjects();
	    
	    mis.newPixels( 0, 0, _width, _height);
	    
	    // Blit new image to offscreen buffer
	    //_buffer.getGraphics().drawImage( temp_image, 0, 0, this);
	    
	    // Blit offscreen buffer to _canvas
	    _canvas.getGraphics().drawImage( temp_image, 0, 0, this);
	    
	} else {
	    initialize();
	}
    }
    
    public void draw( Mat4f c, Obj o)
    {
	
    }
    
    public void illuminate( Vec3f n, Vec3f light)
    {
	float dot   = (float)0.0;
	Light l = null;
	light.x = (float)0.0;
	light.y = (float)0.0;
	light.z = (float)0.0;
	
	for (int i=0; i<_lights.size(); i++) {
	    l = (Light)_lights.elementAt(i);
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
	    l = (Light)_lights.elementAt(i);
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
    
    public void drawParticle( Mat4f m, Obj obj)
    {
	Vec3f loc = MemMgr.Vec3f( (float)0.0, (float)0.0, (float)0.0);
	
	Alg.mult( m, loc);
	
	if (loc.z < (float)0.0) return;
	
	int cx = this.size().width / 2;
	int cy = this.size().height / 2;
	float xm = (float)cx*(float)0.6;
	float ym = (float)cy*(float)0.6;
	int x    = cx + (int)(loc.x / loc.z * xm);
	int y    = cy + (int)(loc.y / loc.z * ym);
	int zbuf = (int)((float)10000.0 / loc.z);
	if (zbuf > MAX24BIT) zbuf = MAX24BIT;
	if (zbuf < 1) zbuf = 1;
	
	rMaterial mat = obj.mat;
	
	if (mat._lightmodel == rMaterial.FLARE || mat._lightmodel == rMaterial.SPRITE)  {
	    
	    int start_y = y - mat._sprite_height / 2;
	    int end_y   = y + mat._sprite_height / 2;
	    int start_x = x - mat._sprite_width  / 2;
	    int end_x   = x + mat._sprite_width  / 2;
	    
	    int sprite_y = 0;
	    
	    if (start_y > _height || end_y < 0 || start_x > _width || end_y < 0) return;
	    
	    if (start_y < 0) {
		sprite_y += -start_y;
		start_y = 0;
	    }
	    if (end_y >= _height) end_y = _height-1;
	    
	    for (int i=start_y; i<end_y; i++) {
		spriteSpan( i, sprite_y, start_x, end_x, zbuf, mat);
		sprite_y++;
	    }
		
	} else if (mat._lightmodel == rMaterial.BILLBOARD) {
	    
	}
	
	MemMgr.done( loc);
	
    }
    
    public void drawTriangles( Mat4f m, Mat4f nm, Vector tlist)
    {
	Vec3f p1 = MemMgr.Vec3f();
	Vec3f p2 = MemMgr.Vec3f();
	Vec3f p3 = MemMgr.Vec3f();
	Vec3f n  = MemMgr.Vec3f();
	Vec3f c  = MemMgr.Vec3f();
	
	Vec3f test1  = MemMgr.Vec3f();
	Vec3f test2  = MemMgr.Vec3f();
	Vec3f test3  = MemMgr.Vec3f();
		
	Vec3f light = MemMgr.Vec3f();
	Vec3f look = MemMgr.Vec3f( (float)0.0, (float)0.0, (float)-1.0);
	Alg.mult( _cam_ctm.ctm(), look);
	int cx = this.size().width / 2;
	int cy = this.size().height / 2;
	float xm = (float)cx*(float)0.6;
	float ym = (float)cy*(float)0.6;
	int color = 0;
	int red, green, blue, inten;
	float fog;
	
	Triangle t   = null;
	rMaterial mat = null;
	
	for (int i=0; i<tlist.size(); i++) {
	    
	    // Find triangle normal
	    t = (Triangle)tlist.elementAt(i);
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
		    		    
		    
		    if (mat._lightmodel == rMaterial.TRANSP) {
			
				transpTriangle( t, mat);
			    
		    } else if (mat._lightmodel == rMaterial.PHONG) {
			    
			    // Get light contribution at vertex 1
			    color = mat._color;
			    Alg.mult( nm, t.v1.n, t.v1.w_n);
			    Alg.mult( _cam_ctm.inv_normal_ctm(), t.v1.w_n);
			    Alg.normalize( t.v1.w_n);
			    // Get light contribution at vertex 2
			    Alg.mult( nm, t.v2.n, t.v2.w_n);
			    Alg.mult( _cam_ctm.inv_normal_ctm(), t.v2.w_n);
			    Alg.normalize( t.v2.w_n);
			    // Get light contribution at vertex 3
			    Alg.mult( nm, t.v3.n, t.v3.w_n);
			    Alg.mult( _cam_ctm.inv_normal_ctm(), t.v3.w_n);
			    Alg.normalize( t.v3.w_n);
			    
			    if (mat.BUMP==false) {
					phongTriangle( t, mat);
			    } else {
					bumpTriangle( t, mat);
			    }
			} else if (mat._lightmodel == rMaterial.GOURAUD) {
			    
			    if (mat.TEXTURE == true) {
				
				// Get light contribution at vertex 1
				color = mat._color;
				Alg.mult( nm, t.v1.n, n);
				Alg.normalize( n);
				illuminate( n, light);
				t.v1.r = ((int)((float)255.0 * light.x)<<16);
				t.v1.g = ((int)((float)255.0 * light.y)<<16);
				t.v1.b = ((int)((float)255.0 * light.z)<<16);
				// Get light contribution at vertex 2
				Alg.mult( nm, t.v2.n, n);
				Alg.normalize( n);
				illuminate( n, light);
				t.v2.r = ((int)((float)255.0 * light.x)<<16);
				t.v2.g = ((int)((float)255.0 * light.y)<<16);
				t.v2.b = ((int)((float)255.0 * light.z)<<16);
				// Get light contribution at vertex 3
				Alg.mult( nm, t.v3.n, n);
				Alg.normalize( n);
				illuminate( n, light);
				t.v3.r = ((int)((float)255.0 * light.x)<<16);
				t.v3.g = ((int)((float)255.0 * light.y)<<16);
				t.v3.b = ((int)((float)255.0 * light.z)<<16);
				
				if (mat.SPEED >= rMaterial.FAST) {
				    fastGouraudTextureTriangle( t, mat);
				} else {
				    gouraudTextureTriangle( t, mat);
				}
				
			    } else {
				
				// Get light contribution at vertex 1
				color = mat._color;
				Alg.mult( nm, t.v1.n, n);
				Alg.normalize( n);
				illuminate( n, light);
				t.v1.r = ((int)((float)((color>>16) & 255) * light.x)<<16);
				t.v1.g = ((int)((float)((color>>8 ) & 255) * light.y)<<16);
				t.v1.b = ((int)((float)( color      & 255) * light.z)<<16);
				// Get light contribution at vertex 2
				Alg.mult( nm, t.v2.n, n);
				Alg.normalize( n);
				illuminate( n, light);
				t.v2.r = ((int)((float)((color>>16) & 255) * light.x)<<16);
				t.v2.g = ((int)((float)((color>>8 ) & 255) * light.y)<<16);
				t.v2.b = ((int)((float)( color      & 255) * light.z)<<16);
				// Get light contribution at vertex 3
				Alg.mult( nm, t.v3.n, n);
				Alg.normalize( n);
				illuminate( n, light);
				t.v3.r = ((int)((float)((color>>16) & 255) * light.x)<<16);
				t.v3.g = ((int)((float)((color>>8 ) & 255) * light.y)<<16);
				t.v3.b = ((int)((float)( color      & 255) * light.z)<<16);
				
				gouraudTriangle( t, mat);
			    }
			    
			} else if (mat._lightmodel == rMaterial.FOG) {
			    
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
				    if (mat._fog_type == rMaterial.SQUARE) fog *= fog;
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
				    if (mat._fog_type == rMaterial.SQUARE) fog *= fog;
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
				    if (mat._fog_type == rMaterial.SQUARE) fog *= fog;
				    fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
				}
				t.v3.r = t.v3.g = t.v3.b = ((int)((float)255.0 * (1.0-fog))<<16);
				
				if (mat.SPEED >= rMaterial.FAST) {
				    fastGouraudTextureTriangle( t, mat);
				} else {
				    gouraudTextureTriangle( t, mat);
				}
				
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
				    if (mat._fog_type == rMaterial.SQUARE) fog *= fog;
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
				    if (mat._fog_type == rMaterial.SQUARE) fog *= fog;
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
				    if (mat._fog_type == rMaterial.SQUARE) fog *= fog;
				    fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
				}
				t.v3.r = ( (int)((float)((color>>16) & 255) * (1.0-fog)) +
					   (int)((float)(mat._fog_R) * fog )               ) << 16;
				t.v3.g = ( (int)((float)((color>>8 ) & 255) * (1.0-fog)) +
					   (int)((float)(mat._fog_G) * fog )               ) << 16;
				t.v3.b = ( (int)((float)( color      & 255) * (1.0-fog)) +
					   (int)((float)(mat._fog_B) * fog )               ) << 16;
				
				gouraudTriangle( t, mat);
			    }
			    
			} else if (mat._lightmodel == rMaterial.FLAT) {
			    
			    if (mat.TEXTURE == true) {
				
				// Find light contribution to face.
				Alg.mult( nm, t.n, n);
				Alg.normalize( n);
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
				
				if (mat.SPEED >= rMaterial.FAST) {
				    fastFlatTextureTriangle( t, mat);
				} else {
				    flatTextureTriangle( t, mat);
				}
				
			    } else {
				
				// Find light contribution to face.
				Alg.mult( nm, t.n, n);
				Alg.normalize( n);
				illuminate( n, light);
				
				color = mat._color;
				red   = (int)((float)((color>>16) & 255) * light.x);
				green = (int)((float)((color>>8 ) & 255) * light.y);
				blue  = (int)((float)( color      & 255) * light.z);
				if (red  >255) red   = 255;
				if (green>255) green = 255;
				if (blue >255) blue  = 255;
				color = (255<<24) + (red<<16) + (green<<8) + (blue);
				
				solidTriangle( t, color, mat);
			    }
			    
			} else if (mat._lightmodel == rMaterial.SOLID) {
			    
			    if (mat.TEXTURE == true) {
				if (mat.SPEED >= rMaterial.FAST) {
				    fastSolidTextureTriangle( t, mat);
				} else {
				    solidTextureTriangle( t, mat);
				}
			    } else {
				solidTriangle( t, mat._color, mat);
			    }
			}
		}
	    }
	}
	MemMgr.done( p1 );
	MemMgr.done( p2 );
	MemMgr.done( p3 );
	MemMgr.done( test1 );
	MemMgr.done( test2 );
	MemMgr.done( test3 );
	MemMgr.done( n  );
	MemMgr.done( c  );
	MemMgr.done( light );
	MemMgr.done( look );
	
    }
    
    public void drawPointset( Mat4f m, Vector vlist, rMaterial mat)
    {
	Vec3f p = MemMgr.Vec3f();
	int cx = this.size().width / 2;
	int cy = this.size().height / 2;
	float xm = (float)cx*(float)0.6;
	float ym = (float)cy*(float)0.6;
	float realx;
	float realy;
	int fixedx;
	int fixedy;

	Vertex v;
	
	for (int i=0; i<vlist.size(); i++) {
	    
	    v = (Vertex)vlist.elementAt(i);
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
		    if (v.zbuf > MAX24BIT) v.zbuf = MAX24BIT;
		    if (v.zbuf < 1) v.zbuf = 1;
		    
		    if (mat._pointstyle == mat.WU) {

			wuPoint( v, fixedx, fixedy, mat);

		    } else if (mat._pointstyle == mat.GAUSSIAN) {
	    
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
			mat._lightmodel = rMaterial.INTENSITY_FLARE;
			mat._sprite_width = mat._gaussian_res;
			mat._sprite_height = mat._gaussian_res;
			//			mat._color = color;

			int start_y = v.y - mat._gaussian_res / 2;
			int end_y   = v.y + mat._gaussian_res / 2;
			int start_x = v.x - mat._gaussian_res / 2;
			int end_x   = v.x + mat._gaussian_res / 2;
			
			int sprite_y = 0;
			
			if (start_y > _height || end_y < 0 || start_x > _width || end_y < 0) return;
			
			if (start_y < 0) {
			    sprite_y += -start_y;
			    start_y = 0;
			}
			if (end_y >= _height) end_y = _height-1;
	    
			for (int l=start_y; l<end_y; l++) {
			    //			    System.err.print("x");
			    spriteSpan( l, sprite_y, start_x, end_x, v.zbuf, mat);
			    sprite_y++;
			}
			//			System.err.println("");
		    }
		}
	    }
	}
	MemMgr.done( p);
    }

    public void wuPoint( Vertex v, int xoff, int yoff, rMaterial mat)
    {
	//	System.out.println("---------------------------------------");
	int wuoffset=0, wucolors=0, wu_00=0, wu_01=0, wu_10=0, wu_11=0, wu_red=0, wu_green=0, wu_blue=0;
	wuoffset = xoff + yoff * 8;
	int pixel = v.x + v.y * _width;
	int color, red, green, blue;

	//System.err.println("X: " + v.x + "  Y: " + v.y + "  xoff: " + xoff + "  yoff: " + yoff);

	try {
	wucolors = _wu_array[wuoffset];
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

    public void drawWireframe( Mat4f m, Vector elist, rMaterial mat)
    {
	Vec3f p1 = MemMgr.Vec3f();
	Vec3f p2 = MemMgr.Vec3f();
	int cx = this.size().width / 2;
	int cy = this.size().height / 2;
	float xm = (float)cx*(float)0.6;
	float ym = (float)cy*(float)0.6;
	
	Edge e;
	
	for (int i=0; i<elist.size(); i++) {
	    
	    e = (Edge)elist.elementAt(i);
	    Alg.mult( m, e.v1.p, p1);
	    Alg.mult( m, e.v2.p, p2);
	    
	    e.v1.x = cx + (int)(p1.x / p1.z * xm);
	    e.v2.x = cx + (int)(p2.x / p2.z * xm);
	    e.v1.y = cy + (int)(p1.y / p1.z * ym);
	    e.v2.y = cy + (int)(p2.y / p2.z * ym);
	    //			e.v1.z = (int)( (p1.z/(float)1000.0)*(float)(Integer.MAX_VALUE) );
	    //			e.v2.z = (int)( (p2.z/(float)1000.0)*(float)(Integer.MAX_VALUE) );
	    e.v1.invz = (float)1.05 / p1.z;
	    e.v2.invz = (float)1.05 / p2.z;
	    e.v1.zbuf = (int)(e.v1.invz * (float)10000.0);
	    if (e.v1.zbuf > MAX24BIT) e.v1.zbuf = MAX24BIT;
	    if (e.v1.zbuf < 1) e.v1.zbuf = 1;
	    e.v2.zbuf = (int)(e.v2.invz * (float)10000.0);
	    if (e.v2.zbuf > MAX24BIT) e.v2.zbuf = MAX24BIT;
	    if (e.v2.zbuf < 1) e.v2.zbuf = 1;
	    
	    if (mat._linestyle == rMaterial.THICK) {
		float thick = p1.z;
		if (thick < 2.0) {
		    thick = (float)3.1 - (thick*(float)1.0);
		} else {
		    thick = (float)1.1;
		}

		AntiAliasedLine( e, thick, mat._color);

	    } else if (mat._linestyle == rMaterial.WU) {

		wuLine( e, mat._color);

	    } else if (mat._linestyle == rMaterial.PIXEL) {

		solidLine( e, mat._color);
	    }
	}
	MemMgr.done( p1);
	MemMgr.done( p2);
    }
    
    public void solidLine( Edge e, int color)
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


    
    public void wuLine( Edge e, int color)
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

    
    
    public void AntiAliasedLine( Edge e, float thick, int color)
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
				if (contrib > 255) contrib = 255;
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
				if (contrib > 255) contrib = 255;
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
				if (contrib > 255) contrib = 255;
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
				if (contrib > 255) contrib = 255;
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
    
    
    public void solidTriangle( Triangle t, int color, rMaterial mat)
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
			solidSpan( i, lx>>16, rx>>16, lz, rz, color, mat);
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
			solidSpan( i, lx>>16, rx>>16, lz, rz, color, mat);
		lx += dx_1_3;
		rx += dx_2_3;
		lz += dz_1_3;
		rz += dz_2_3;
	    }
	}
    }
    
    public void solidSpan( int y, int lx, int rx, int lz, int rz, int color, rMaterial mat)
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
	
	int t_r = 0;
	int t_g = 0;
	int t_b = 0;
	int tcol;
	if (mat.TRANSPARENT) {
			t_r = (((color>>16)&255)*(255-mat._transp_R))>>8;
			t_g = (((color>>8 )&255)*(255-mat._transp_G))>>8;
			t_b = (( color     &255)*(255-mat._transp_B))>>8;
	}
	
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
		    if (mat.TRANSPARENT) {
			tcol = _pix[ pixel];
			_pix[ pixel ] =
			    (255<<24) +
			    ((t_r + ((((tcol>>16)&255)*mat._transp_R)>>8))<<16) +
			    ((t_g + ((((tcol>>8 )&255)*mat._transp_R)>>8))<<8)  +
			    (t_b + ((( tcol     &255)*mat._transp_R)>>8));
			
		    } else {
			_pix [ pixel ] = color;
		    }
		    _zbuf[ pixel ] = z;
		}
	    }
	    pixel++;
	    z += dz;
	}
    }
    
    
    public void transpTriangle( Triangle t, rMaterial mat)
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
		if (i>0 && i<_height) transpSpan( i, lx>>16, rx>>16, lz, rz, mat);
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
		if (i>0 && i<_height) transpSpan( i, lx>>16, rx>>16, lz, rz, mat);
		lx += dx_1_3;
		rx += dx_2_3;
		lz += dz_1_3;
		rz += dz_2_3;
	    }
	}
    }
    
    public void transpSpan( int y, int lx, int rx, int lz, int rz, rMaterial mat)
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
    
    
    
    // Fast!  No texture correction!
    public void spriteSpan( int y, int sprite_y, int lx, int rx, int z, rMaterial mat)
    {
	int r, g, b, scol, pcol;
	int sexel = mat._sprite_width * sprite_y;

	// Clip against sides of screen
	if (rx >= _width) rx = _width-1;
	if (lx < 0) {
	    sexel += -lx;
	    lx = 0;
	}
	
	// Render that puppy
	int pixel = lx + y * _width;
	for (int i=lx; i<rx; i++) {
	    if ( z > _zbuf[ pixel ] ) {
		
		if (mat._lightmodel == rMaterial.FLARE) {
		    
		    scol = mat._texture[ sexel ];
		    pcol = _pix[ pixel ];
		    r = ((scol>>16)&255) + ((pcol>>16)&255);
		    g = ((scol>>8)&255) + ((pcol>>8)&255);
		    b = ((scol)&255) + ((pcol)&255);
		    if (r>255) r=255;
		    if (g>255) g=255;
		    if (b>255) b=255;
		    _pix[ pixel] = (255<<24) + (r<<16) + (g<<8) + b;
		    
		} else if (mat._lightmodel == rMaterial.INTENSITY_FLARE) {
		    
		    scol = (mat._texture[ sexel ])&255;
		    pcol = _pix[ pixel ];
		    r = ((scol*mat._f_red  )>>8) + ((pcol>>16)&255);
		    g = ((scol*mat._f_green)>>8) + ((pcol>>8)&255);
		    b = ((scol*mat._f_blue )>>8) + ((pcol)&255);
		    if (r>255) r=255;
		    if (g>255) g=255;
		    if (b>255) b=255;
		    _pix[ pixel] = (255<<24) + (r<<16) + (g<<8) + b;
		    
		} else if (mat._lightmodel == rMaterial.SPRITE) {
		    
		    _pix[ pixel ] = mat._texture[ sexel];
		}
	    }
	    pixel++;
	    sexel++;
	}
    }
    
    public void phongTriangle( Triangle t, rMaterial mat)
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
	
	int lx = v1.x<<16;
	int rx = v1.x<<16;
	int lz = v1.zbuf;
	int rz = v1.zbuf;
	int la = (int)(v1.w_n.x * (float)65536.0 + (float)65536.0);
	int ra = (int)(v1.w_n.x * (float)65536.0 + (float)65536.0);
	int lb = (int)(v1.w_n.y * (float)65536.0 + (float)65536.0);
	int rb = (int)(v1.w_n.y * (float)65536.0 + (float)65536.0);
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
	    dz_1_2 = (v2.zbuf-v1.zbuf) / dy_1_2;
	    da_1_2 = (int)((v2.w_n.x-v1.w_n.x)*(float)65536.0) / dy_1_2;
	    db_1_2 = (int)((v2.w_n.y-v1.w_n.y)*(float)65536.0) / dy_1_2;
	}
	if (dy_1_3 != 0) {
	    dx_1_3 = ((v3.x<<16)-(v1.x<<16)) / dy_1_3;
	    dz_1_3 = (v3.zbuf-v1.zbuf) / dy_1_3;
	    da_1_3 = (int)((v3.w_n.x-v1.w_n.x)*(float)65536.0) / dy_1_3;
	    db_1_3 = (int)((v3.w_n.y-v1.w_n.y)*(float)65536.0) / dy_1_3;
	}
	if (dy_2_3 != 0) {
	    dx_2_3 = ((v3.x<<16)-(v2.x<<16)) / dy_2_3;
	    dz_2_3 = (v3.zbuf-v2.zbuf) / dy_2_3;
	    da_2_3 = (int)((v3.w_n.x-v2.w_n.x)*(float)65536.0) / dy_2_3;
	    db_2_3 = (int)((v3.w_n.y-v2.w_n.y)*(float)65536.0) / dy_2_3;
	}
	
	// Draw top half
	if (dy_1_2 != 0) {
	    
	    for (int i=v1.y; i<v2.y; i++) {
		if (i>0 && i<_height) phongSpan( i, lx>>16, rx>>16, lz, rz, la, ra, lb, rb, color, mat);
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
	rz = v2.zbuf;
	ra = (int)(v2.w_n.x*65536.0 + 65536.0);
	rb = (int)(v2.w_n.y*65536.0 + 65536.0);
	
	// Draw bottom half
	if (dy_2_3 != 0) {
	    
	    for (int i=v2.y; i<v3.y; i++) {
		if (i>0 && i<_height) phongSpan( i, lx>>16, rx>>16, lz, rz, la, ra, lb, rb, color, mat);
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
    
    public void phongSpan( int y, int lx, int rx, int lz, int rz,
			   int la, int ra, int lb, int rb, int color, rMaterial mat)
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
		int s, t;
		// Render that puppy
		int pixel = lx + y * _width;
		for (int i=lx; i<rx; i++) {
		    if (i>=0 && i<_width) {
			if ( z > _zbuf[ pixel ] ) {
			    
			    _pix [ pixel ] = mat._env_map[ (a>>9)%256 + ((255-(b>>9))%256)*256 ]; //(255<<24) + (red<<16) + (green<<8) + blue;
			    _zbuf[ pixel ] = z;
			}
		    }
		    pixel++;
		    z += dz;
		    a += da;
		    b += db;
		}
    }
    
    public void bumpTriangle( Triangle t, rMaterial mat)
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
			if (i>0 && i<_height) bumpSpan( i, lx>>16, rx>>16, lz, rz, la, ra, lb, rb, ls, rs, lt, rt, color, mat);
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
			if (i>0 && i<_height) bumpSpan( i, lx>>16, rx>>16, lz, rz, la, ra, lb, rb, ls, rs, lt, rt, color, mat);
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
    
    public void bumpSpan( int y, int lx, int rx, int lz, int rz,
			  int la, int ra, int lb, int rb, int ls, int rs,
			  int lt, int rt, int color, rMaterial mat)
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
	if (rx >= _width) rx = _width-1;
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
    
    
    public void gouraudTriangle( Triangle t, rMaterial mat)
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
		if (i>0 && i<_height) gouraudSpan( i, lx>>16, rx>>16, lz, rz, lr, rr, lg, rg, lb, rb, mat);
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
		if (i>0 && i<_height) gouraudSpan( i, lx>>16, rx>>16, lz, rz, lr, rr, lg, rg, lb, rb, mat);
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
    
    
    public void gouraudSpan( int y, int lx, int rx, int lz, int rz, int lr, int rr,
			     int lg, int rg, int lb, int rb, rMaterial mat)
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
	if (rx >= _width) rx = _width-1;
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
    public void solidTextureTriangle( Triangle t, rMaterial mat)
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
			    if (mat.SPEED == rMaterial.FAST16) {
				fast16solidTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
			    } else if (mat.SPEED == rMaterial.SLOW) {
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
			    if (mat.SPEED == rMaterial.FAST16) {
				fast16solidTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
			    } else if (mat.SPEED == rMaterial.SLOW) {
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
    
    // Slow!  Texture-corrects every pixel!
    public void solidTextureSpan( int y, int lx, int rx, float lz, float rz,
				  float ls, float rs, float lt, float rt, rMaterial mat)
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
	int zbuf = 0;
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
		zbuf = (int)( (zinv/(float)1000.0)*(float)(Integer.MAX_VALUE) );
		
		if ( zbuf < _zbuf[ pixel ] ) {
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
			_zbuf[ pixel ] = zbuf;
		    }
		}
	    }
	    pixel++;
	    z += dz;
	    s += ds;
	    t += dt;
	}
    }
    
    // Fast!  Texture-corrects every 16 pixels!
    public void fast16solidTextureSpan( int y, int lx, int rx, float lz, float rz,
					float ls, float rs, float lt, float rt, rMaterial mat)
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
    
    
    // Fast! Incorrect z interpolation!
    public void fastSolidTextureTriangle( Triangle t, rMaterial mat)
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
			if (i>0 && i<_height)
			    fastTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
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
			if (i>0 && i<_height)
					fastTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
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
    
    // Fast!  No texture correction!
    public void fastTextureSpan( int y, int lx, int rx, int lz, int rz,
				 int ls, int rs, int lt, int rt, rMaterial mat)
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
	
	int r, g, b, tcol;
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
    
    
    
    
    // Slow! Texture-corrects every pixel!
    public void flatTextureTriangle( Triangle t, rMaterial mat)
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
				    if (mat.SPEED == rMaterial.SLOW) {
					flatTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
				    } else if (mat.SPEED == rMaterial.FAST16) {
					fast16flatTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
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
		    if (mat.SPEED == rMaterial.SLOW) {
			flatTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
		    } else if (mat.SPEED == rMaterial.FAST16) {
			fast16flatTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
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
    public void flatTextureSpan( int y, int lx, int rx, float lz, float rz,
				 float ls, float rs, float lt, float rt, rMaterial mat)
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
		int zbuf = 0;
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
			zbuf = (int)( (zinv/(float)1000.0)*(float)(Integer.MAX_VALUE) );
			
			if ( zbuf < _zbuf[ pixel ] ) {
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
				_zbuf[ pixel ] = zbuf;
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
    public void fast16flatTextureSpan( int y, int lx, int rx, float lz, float rz,
				       float ls, float rs, float lt, float rt, rMaterial mat)
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
    public void fastFlatTextureTriangle( Triangle t, rMaterial mat)
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
		if (i>0 && i<_height)
		    fastFlatTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
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
		if (i>0 && i<_height)
		    fastFlatTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, mat);
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
				     int ls, int rs, int lt, int rt, rMaterial mat)
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
	int zbuf = 0;
	
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
    
    
    // Slow! Texture-corrects every pixel!
    public void gouraudTextureTriangle( Triangle t, rMaterial mat)
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
	    dz_1_2 = (v2.invz-v1.invz) / (float)dy_1_2;
	    ds_1_2 = (v2.invs-v1.invs) / (float)dy_1_2;
	    dt_1_2 = (v2.invt-v1.invt) / (float)dy_1_2;
	    dr_1_2 = (v2.r-v1.r) / dy_1_2;
	    dg_1_2 = (v2.g-v1.g) / dy_1_2;
	    db_1_2 = (v2.b-v1.b) / dy_1_2;
	}
	if (dy_1_3 != 0) {
	    dx_1_3 = ((v3.x<<16)-(v1.x<<16)) / dy_1_3;
	    dz_1_3 = (v3.invz-v1.invz) / (float)dy_1_3;
	    ds_1_3 = (v3.invs-v1.invs) / (float)dy_1_3;
	    dt_1_3 = (v3.invt-v1.invt) / (float)dy_1_3;
	    dr_1_3 = (v3.r-v1.r) / dy_1_3;
	    dg_1_3 = (v3.g-v1.g) / dy_1_3;
	    db_1_3 = (v3.b-v1.b) / dy_1_3;
	}
	if (dy_2_3 != 0) {
	    dx_2_3 = ((v3.x<<16)-(v2.x<<16)) / dy_2_3;
	    dz_2_3 = (v3.invz-v2.invz) / (float)dy_2_3;
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
		    if (mat.SPEED == rMaterial.SLOW) {
			gouraudTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, lr, rr, lg, rg, lb, rb, mat);
		    } else if (mat.SPEED == rMaterial.FAST16) {
			fast16gouraudTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, lr, rr, lg, rg, lb, rb, mat);
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
	rz = v2.invz;
	rs = v2.invs;
	rt = v2.invt;
	rr = v2.r;
	rg = v2.g;
	rb = v2.b;
	
	// Draw bottom half
	if (dy_2_3 != 0) {
	    
	    for (int i=v2.y; i<v3.y; i++) {
		if (i>0 && i<_height) {
		    if (mat.SPEED == rMaterial.SLOW) {
			gouraudTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, lr, rr, lg, rg, lb, rb, mat);
		    } else if (mat.SPEED == rMaterial.FAST16) {
			fast16gouraudTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, lr, rr, lg, rg, lb, rb, mat);
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
    public void gouraudTextureSpan( int y, int lx, int rx, float lz, float rz,
				    float ls, float rs, float lt, float rt,
				    int lr, int rr, int lg, int rg, int lb, int rb,
				    rMaterial mat)
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
	boolean fog = (mat._lightmodel == rMaterial.FOG);
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
	int zbuf = 0;
	float zinv = (float)0.0;
	
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
		zinv = (float)1.0/z;
		zbuf = (int)( (zinv/(float)1000.0)*(float)(Integer.MAX_VALUE) );
		
		if ( zbuf < _zbuf[ pixel ] ) {
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
			_zbuf[ pixel ] = zbuf;
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
    public void fast16gouraudTextureSpan( int y, int lx, int rx, float lz, float rz,
					  float ls, float rs, float lt, float rt,
					  int lr, int rr, int lg, int rg, int lb, int rb,
					  rMaterial mat)
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
	boolean fog = (mat._lightmodel == rMaterial.FOG);
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
    public void fastGouraudTextureTriangle( Triangle t, rMaterial mat)
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
		if (i>0 && i<_height)
		    fastGouraudTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, lr, rr, lg, rg, lb, rb, mat);
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
		if (i>0 && i<_height)
		    fastGouraudTextureSpan( i, lx>>16, rx>>16, lz, rz, ls, rs, lt, rt, lr, rr, lg, rg, lb, rb, mat);
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
    public void fastGouraudTextureSpan( int y, int lx, int rx, int lz, int rz,
					int ls, int rs, int lt, int rt,
					int lr, int rr, int lg, int rg, int lb, int rb,
					rMaterial mat)
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
	boolean fog = (mat._lightmodel == rMaterial.FOG);
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


class Vertex
{
    Vec3f p;
    Vec3f n;
    Vec3f w_n;
    
    Vector elist;
    Vector tlist;
    
    int x;
    int y;
    int z;

    int xfrac;
    int yfrac;

    int zbuf; // Z-buffer value.  Equals (10000.0/z), bounded to range [1, MAX24BIT]
    // We use a multiple of 1/z in z-buffer because 1/z interpolates
    // linearly in screen space (as we walk pixels).  (10000.0/z) should
    // be accurate roughly in the z range [0.001, 100], and some errors
    // will appear in distant objects (100<z<1000).  Everything beyond
    // z=1000 will be rife with error.  But that's okay.
    float invz;
    float invs;
    float invt;

    int s;
    int t;
    
    int r;
    int g;
    int b;
    
    Vertex next;
    
    public Vertex()
    {
	elist = new Vector(10);
	tlist = new Vector(10);
    }
    
    public Vertex( float x, float y, float z)
    {
	this();
	p = MemMgr.Vec3f();
	p.x = x;
	p.y = y;
	p.z = z;
    }
}

class Edge
{
    Vertex v1 = null;
    Vertex v2 = null;
    
    Triangle t1 = null;
    Triangle t2 = null;
    
    Edge next = null;
}

class Span
{
    Triangle t;
    
    int xl;
    int xr;
    int yl;
    int yr;
    int zl;
    int zr;
    
    int rl;
    int rr;
    int gl;
    int gr;
    int bl;
    int br;
    
    int cl;
    int cr;
    
    Vec3f nl;
    Vec3f nr;
    VecSf nsl;
    VecSf nsr;
    
    int sl;
    int sr;
    int tl;
    int tr;
    int ul;
    int ur;
    int vl;
    int vr;
    
    Span next;
}

class Triangle
{
    Vertex v1;
    Vertex v2;
    Vertex v3;
    
    Edge e1;
    Edge e2;
    Edge e3;
    
    float s1;
    float t1;
    float s2;
    float t2;
    float s3;
    float t3;
    
    int bs1;
    int bt1;
    int bs2;
    int bt2;
    int bs3;
    int bt3;
    
    Vec3f c;
    Vec3f n;
    
    rMaterial mat = null;
    Obj obj;
    
    Triangle next;
    
    public void setTexture( float s1, float t1, float s2, float t2, float s3, float t3)
    {
	this.s1 = s1;
	this.t1 = t1;
	this.s2 = s2;
	this.t2 = t2;
	this.s3 = s3;
	this.t3 = t3;
    }
    
    public void setBump( float s1, float t1, float s2, float t2, float s3, float t3)
    {
	this.bs1 = (int)(s1 * (float)128.0);
	this.bt1 = (int)(t1 * (float)128.0);
	this.bs2 = (int)(s2 * (float)128.0);
	this.bt2 = (int)(t2 * (float)128.0);
	this.bs3 = (int)(s3 * (float)128.0);
	this.bt3 = (int)(t3 * (float)128.0);
    }
    
    
}


abstract class rNode
{
    Ctm ctm;
    String name;
    render rend;
    
    public rNode(String new_name, render rend)
    {
	ctm = MemMgr.Ctm();
	name = new_name;
	this.rend = rend;
    }

    public Ctm ctm()
    {
	return ctm;
    }

    public abstract void render( Mat4f xform, Mat4f nxform,  float time);
}

class rGroup extends rNode
{
    public Vector children;

    public rGroup(String new_name, render rend)
    {
	super( new_name, rend);
	children = new Vector(10);
    }

    public void addChild( rNode rNode)
    {
	children.addElement( rNode);
    }

    public void render( Mat4f xform, Mat4f nxform, float time)
    {
	Mat4f my_xform   = MemMgr.Mat4f();
	Mat4f my_n_xform = MemMgr.Mat4f();

	Alg.mult( ctm().ctm(), xform, my_xform);
	Alg.mult( ctm().normal_ctm(), nxform, my_n_xform);

	rNode child;
	for (int i=0; i<children.size(); i++) {
	    child = (rNode)children.elementAt(i);
	    child.render( my_xform, my_n_xform, time);
	}

	MemMgr.done( my_xform);
	MemMgr.done( my_n_xform);
    }
}

class Anim extends rGroup
{
    int style;

    float start_time;
    float end_time;
    float last_time = (float)0.0;
    float duration;
    float cycle;

    Vec3f start_val = null;
    Vec3f end_val   = null;
    Vec3f raw_rate  = null;

    int xform = 0;

    boolean running    = false;

    final static public int NONE       = 0;
    final static public int ONE_SHOT   = 1;
    final static public int LOOP       = 2;
    final static public int PING_PONG  = 3;
    final static public int CONTINUOUS = 4;
    final static public int JUMP       = 5;

    final static public int TRANS = 1;
    final static public int ROT   = 2;
    final static public int SCALE = 3;

    public Anim( String new_name, render rend)
    {
	super( new_name, rend);
    }

    public void render( Mat4f xform, Mat4f nxform, float time)
    {
	updateAnim( time);

	super.render( xform, nxform, time);
    }

    /**
     * Initialize animation.
     * <BL>
     *  <LI>xform - type of transformation.  Can be either TRANS, ROT, or SCALE
     *  <LI>style - animation behavior.  Can be NONE, LOOP, ONE_SHOT, PING_PONG, or CONTINUOUS.
     *  <LI>start_time and end_time - time in seconds, renderer begins at zero.
     *  <LI>start_val and end_val - vectors encoding values for anim at start_time and end_time.\
     * </BL>
     * An Anim can be used for only one type of transformation (trans, rot, or scale).
     * Successive calls to this method specifying different xform types will cause the
     * previously running animation to immediately terminate, and be replaced with the
     * current animation. (i.e., an Anim cannot transform scaling and rotation simultaneously.)
     */
    public void initAnim( int xform, int style, float start_time, float end_time, float cycle, Vec3f start_val, Vec3f end_val)
    {
	duration   = (end_time - start_time);
	if (duration <= (float)0.0) {
	    System.err.println("Zero or negative anim time in setAnim()!");
	    return;
	}
	if (start_val == null || end_val == null) {
	    System.err.println("NULL start or end values in setAnim()!");
	    return;
	}

	this.start_time = start_time;
	this.end_time   = end_time;
	this.start_val  = start_val;
	this.end_val    = end_val;
	this.style      = style;
	this.cycle      = cycle;
	this.xform      = xform;

	if (raw_rate == null) raw_rate = MemMgr.Vec3f();

	// We can treat ONE_SHOTs as LOOPs that just loop once, between
	// start_time and end_time.  [This is accomplished by setting
	// "cycle" to be equal to "duration", so the animation runs
	// exactly once over its lifecycle.]
	if (style == ONE_SHOT) {
	    style = LOOP;
	    cycle = duration;
	}

	raw_rate.x  = (end_val.x - start_val.x) / cycle;
	raw_rate.y  = (end_val.y - start_val.y) / cycle;
	raw_rate.z  = (end_val.z - start_val.z) / cycle;

	running = true;
    }

    public void startAnim()
    {
	running = true;
    }

    public void stopAnim()
    {
	running = false;
    }
    
    /**
     * updateAnim.
     * <BR>
     * Given the current time in seconds, calculates the appropriate value
     * for the animation to take.  A given animation with a particular
     * start_time, end_time, start_val, and end_val will take on different
     * values depending on whether its style is LOOP, PING_PONG, ONE_SHOT,
     * or CONTINUOUS.
     * Updates the Anim's Ctm to reflect the current animation value.
     */
    public void updateAnim(float cur_time)
    {
	if (running == false) return;

	float elapsed_time = cur_time - last_time;
	float ratio        = (float)0.0;
	
	if (style==NONE) {
	    return;

	} else if (style==LOOP) {
	    ratio = ( (cur_time - start_time) % cycle ) / cycle;
	    setAnim( ratio);

	} else if (style==PING_PONG) {
	    ratio = ( (cur_time - start_time) % ((float)2.0 * cycle) ) / cycle;
	    if (ratio >= (float)0.0 && ratio < (float)1.0) {
		setAnim( ratio);

	    } else if (ratio >= (float)1.0 && ratio <= (float)2.0) {
		ratio = (float)2.0 - ratio;
		setAnim( ratio);
	    }

	} else if (style==JUMP) {
	    if (last_time < end_time && cur_time >= end_time) {
		setAnim( (float)1.0 );
	    }

	} else if (style==CONTINUOUS) {
	    ratio = (cur_time - start_time) / cycle;
	    setAnim( ratio);
	}

	last_time = cur_time;

	// Make sure CONTINUOUS animation loops never time out.
	// [Always set last_time to zero before comparing to end_time.]
	if (style==CONTINUOUS) {
	    last_time = (float)0.0;
	}

	if (last_time > end_time) running = false;
    }
    
    /**
     * setAnim
     * <BR>
     * For a given ratio [0..1] between start_val and end_val,
     * set the Anim's CTM to reflect this new interpolated value.
     * Note: for CONTINUOUS type animations, ratio can be > 1.0
     */
    protected void setAnim( float ratio)
    {
	if (xform == TRANS) {

	    ctm.set_trans( start_val.x + ratio * raw_rate.x,
			   start_val.y + ratio * raw_rate.y,
			   start_val.z + ratio * raw_rate.z
			   );

	} else if (xform == ROT) {

	    ctm.set_rot( Alg.IDENT_MAT);
	    ctm.post_rot_x( (start_val.x + ratio * raw_rate.x) % (float)6.28318);
	    ctm.post_rot_y( (start_val.y + ratio * raw_rate.y) % (float)6.28318);
	    ctm.post_rot_z( (start_val.z + ratio * raw_rate.z) % (float)6.28318);

	} else if (xform == SCALE) {

	    ctm.set_scale( start_val.x + ratio * raw_rate.x,
			   start_val.y + ratio * raw_rate.y,
			   start_val.z + ratio * raw_rate.z
			   );

	}
    }

}

class Obj extends rNode
{
    public Ctm ctm;
    public rMaterial mat;
    
    public Vector  vlist;
    public Vector  elist;
    public Vector  tlist;
    
    public Obj(String new_name, render rend)
    {
	super(new_name, rend);
	vlist = new Vector(50);
	elist = new Vector(50);
	tlist = new Vector(50);
    }
    
    public Vertex v( int i)
    {
	return (Vertex)vlist.elementAt(i);
    }
    
    public Edge e( int i)
    {
	return (Edge)elist.elementAt(i);
    }
    
    public Triangle t( int i)
    {
	return (Triangle)tlist.elementAt(i);
    }
    
    public void addVertex( Vertex v)
    {
	vlist.addElement(v);
    }
    
    public void addEdge( Edge e, int v1, int v2)
    {
	try {
	    elist.addElement(e);
	    Vertex vrtx1 = (Vertex)vlist.elementAt(v1);
	    Vertex vrtx2 = (Vertex)vlist.elementAt(v2);
	    e.v1 = vrtx1;
	    e.v2 = vrtx2;
	    vrtx1.elist.addElement(e);
	    vrtx2.elist.addElement(e);
	    
	} catch (Exception ex) {
	    System.err.println("addEdge: oops. v1: " + v1 + "  v2: " + v2);
	}
    }
    
    public void addTriangleNoEdge( Triangle t, int v1, int v2, int v3)
    {
	try {
	    
	    tlist.addElement( t);
	    
	    Vertex vrtx1 = (Vertex)vlist.elementAt(v1);
	    Vertex vrtx2 = (Vertex)vlist.elementAt(v2);
	    Vertex vrtx3 = (Vertex)vlist.elementAt(v3);
	    
	    t.v1 = vrtx1;
	    t.v2 = vrtx2;
	    t.v3 = vrtx3;
	    
	    vrtx1.tlist.addElement(t);
	    vrtx2.tlist.addElement(t);
	    vrtx3.tlist.addElement(t);
	    
	    // Calculate normal.
	    Vec3f one    = MemMgr.Vec3f();
	    Vec3f two    = MemMgr.Vec3f();
	    Vec3f normal = MemMgr.Vec3f();
	    Alg.sub( vrtx2.p, vrtx1.p, one);
	    Alg.sub( vrtx3.p, vrtx2.p, two);
	    Alg.normalize( one);
	    Alg.normalize( two);
	    Alg.cross( one, two, normal);
	    Alg.normalize( normal);
	    t.n = normal;
	    MemMgr.done( one);
	    MemMgr.done( two);
	    
	    // Calculate centroid
	    t.c = MemMgr.Vec3f();
	    t.c.x = (t.v1.p.x + t.v2.p.x + t.v3.p.x) / (float)3.0;
	    t.c.y = (t.v1.p.y + t.v2.p.y + t.v3.p.y) / (float)3.0;
	    t.c.z = (t.v1.p.z + t.v2.p.z + t.v3.p.z) / (float)3.0;
	    
	    t.obj = this;
	    
	} catch (Exception e) {
	    System.err.println("addTriangle: oops.");
	}
    }

    public void addTriangle( Triangle t, int v1, int v2, int v3, int e1, int e2, int e3)
    {
	try {
	    
	    tlist.addElement( t);
	    
	    Vertex vrtx1 = (Vertex)vlist.elementAt(v1);
	    Vertex vrtx2 = (Vertex)vlist.elementAt(v2);
	    Vertex vrtx3 = (Vertex)vlist.elementAt(v3);
	    Edge   edge1 = (Edge)  elist.elementAt(e1);
	    Edge   edge2 = (Edge)  elist.elementAt(e2);
	    Edge   edge3 = (Edge)  elist.elementAt(e3);
	    
	    t.v1 = vrtx1;
	    t.v2 = vrtx2;
	    t.v3 = vrtx3;
	    t.e1 = edge1;
	    t.e2 = edge2;
	    t.e3 = edge3;
	    
	    vrtx1.tlist.addElement(t);
	    vrtx2.tlist.addElement(t);
	    vrtx3.tlist.addElement(t);
	    if (edge1.t1 == null) {
		edge1.t1 = t;
	    } else {
		edge1.t2 = t;
	    }
	    if (edge2.t1 == null) {
		edge2.t1 = t;
	    } else {
		edge2.t2 = t;
	    }
	    if (edge3.t1 == null) {
		edge3.t1 = t;
	    } else {
		edge3.t2 = t;
	    }
	    
	    // Calculate normal.
	    Vec3f one    = MemMgr.Vec3f();
	    Vec3f two    = MemMgr.Vec3f();
	    Vec3f normal = MemMgr.Vec3f();
	    Alg.sub( vrtx2.p, vrtx1.p, one);
	    Alg.sub( vrtx3.p, vrtx2.p, two);
	    Alg.normalize( one);
	    Alg.normalize( two);
	    Alg.cross( one, two, normal);
	    Alg.normalize( normal);
	    t.n = normal;
	    MemMgr.done( one);
	    MemMgr.done( two);
	    
	    // Calculate centroid
	    t.c = MemMgr.Vec3f();
	    t.c.x = (t.v1.p.x + t.v2.p.x + t.v3.p.x) / (float)3.0;
	    t.c.y = (t.v1.p.y + t.v2.p.y + t.v3.p.y) / (float)3.0;
	    t.c.z = (t.v1.p.z + t.v2.p.z + t.v3.p.z) / (float)3.0;
	    
	    t.obj = this;
	    
	} catch (Exception e) {
	    System.err.println("addTriangle: oops.");
	}
    }
    
    public void calcVertexNormals()
    {
	System.err.println("Calculating Vertex normals!");
	Vec3f    vert_n = null;
	Vertex   vert   = null;
	Triangle tri    = null;
	
	for (int i=0; i<vlist.size(); i++) {
	    
	    vert = (Vertex)vlist.elementAt(i);
	    vert_n = MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0);
	    for (int j=0; j<vert.tlist.size(); j++) {
		tri = (Triangle)vert.tlist.elementAt(j);
		vert_n.x += tri.n.x;
		vert_n.y += tri.n.y;
		vert_n.z += tri.n.z;
	    }
	    vert_n.x /= (float)vert.tlist.size();
	    vert_n.y /= (float)vert.tlist.size();
	    vert_n.z /= (float)vert.tlist.size();
	    Alg.normalize(vert_n);
	    vert.n = vert_n;
	    vert.w_n = MemMgr.Vec3f();
	}
    }

    public void createFacesFromVertsAndNormals( float [] coords, int [] indices, float [] normals,
						int [] normalIndices,  boolean ccw)
    {
	createFacesFromVerts( coords, indices, ccw);
	
	for (int i=0; i<normalIndices.length; i++) {
	}

    }
    

    /**
     * createFacesFromVerts
     * <BR>
     * <BL>
     * <LI> coords - list of floating point values representing the X,Y,Z
     *   coordinates of each vertex in this face set.  A list of vertices
     *   is generated from the coords list by breaking it up into triplets.
     * <LI> indices - list of indeces into the vertex set.  Indices are grouped
     *   into sets of three or more.  Sets are separated by the dummy index
     *   "-1".  Each set of 3+ indices represets a polygon face.
     * </BL>
     */
    public void createFacesFromVerts( float [] coords, int [] indices, boolean ccw)
    {
	// Make sure number of coords is divisible by three.
	// We're relying on assumption that each coord has an X, Y, and Z
	// component present in list.

	int num_read = (coords.length / 3) * 3;
	//	System.err.println("Will read this many vert coords: " + num_read);
	if (coords.length % 3 != 0) {
	    System.err.println("Obj.createFacesFromVerts(): ");
	    System.err.println(" Number of 'coords' passed in should be divisible by 3!");
	    System.err.println(" Number of 'coords': " + coords.length );
	    //return;
	}
	
	// Add each vertex (as specific by a coordinate triplet in 'coord' list) to this Obj

	int cur_coord = 0;
	while (cur_coord < num_read-2) {
	    Vertex new_vert = MemMgr.Vertex( coords[cur_coord], coords[cur_coord+1], coords[cur_coord+2] );
	    //  System.err.println( "Adding vertex!: " + coords[cur_coord] + " "  +  coords[cur_coord+1] + " " + coords[cur_coord+2] );
	    addVertex( new_vert);
	    cur_coord+= 3;
	}
	
	// Make faces by walking down 'indices' list and making triangles out
	// of the vertex indices listed.  Sets of indices belonging to a single
	// face are separated by the index "-1".  Note: faces may be listed as having
	// more than three vertices, but in our internal representation we break up
	// facing having more than three vertices into sub-triangles.  This system
	// currently only supports triangle-based rendering.
	// Also: it is assumed that vertices are listed in clockwise order, for
	// deteriming surface normal direction.
	
	Vector face_indx = new Vector(10);
	int cur_indx = 0;
	
	while (cur_indx < indices.length) {

	    // Copy next group of indices out of index list (up to but not
	    // including the "-1" separator, which is discarded)

	    while (indices[cur_indx] != -1) {
		face_indx.addElement( new Integer( indices[cur_indx]) );
		cur_indx++;
	    }

	    // Discard the -1 separator by incrementing past it
	    cur_indx++;

	    // Build a face out of the contents of face_indx
	    addFace( face_indx, ccw);

	    // Clear out face list for next group of indices, and repeat.
	    face_indx.removeAllElements();
	}
    }

    protected void addFace( Vector face_indx, boolean ccw)
    {
	// If face has more than three vertices, break it into
	// a fan of triangles.  Fan will have its shared base
	// vertex at face_indx[0], and there will be (num_verts-2)
	// triangles in it.

	// Face must have minimum three vertices.  If less, face is invalid; return.
	if (face_indx.size() < 3) {
	    System.err.println("Obj.addFace(): invalid face.  Need min. 3 vertices.");
	    return;
	}

	// Get base vertex for fan of triangles.
	int base_indx = ((Integer)face_indx.elementAt(0)).intValue();
	int indx1 = 0;
	int indx2 = 0;
	if (base_indx >= this.vlist.size() || base_indx < 0) {
	    System.err.println("Obj.addFace(): vertex index greater than # vertices: " + base_indx);
	    System.err.println(" Error on first vertex (base of fan).  Entire face lost.");
	    return;
	}

	// Loop across remaining vertices in face, building a fan of triangles.
	for (int i=1; i<face_indx.size()-1; i++) {

	    // Get next two indices from list
	    indx1 = ((Integer)face_indx.elementAt(i)).intValue();
	    indx2 = ((Integer)face_indx.elementAt(i+1)).intValue();

	    // Make sure indices are in range
	    if (indx1 >= this.vlist.size() || indx1 < 0 ) {
		System.err.println("Obj.addFace(): vertex index out of range: " + indx1 );
		System.err.println(" Lost one triangle out of face.");
		continue;
	    }
	    if (indx2 >= this.vlist.size() || indx2 < 0 ) {
		System.err.println("Obj.addFace(): vertex index out of range: " + indx2 );
		System.err.println(" Lost one triangle out of face.");
		continue;
	    }
	    
	    // If indices are in range, make a triangle out of the base vertex and
	    // the vertices at indx1 and indx2, and add it to this Obj.
	    Triangle t = MemMgr.Triangle();
	    if (ccw == true) {
		addTriangleNoEdge( t,  base_indx, indx2, indx1);
	    } else {
		addTriangleNoEdge( t,  base_indx, indx1, indx2);
	    }
	}
    }

    /**
     * createEdgesFromVerts
     * <BR>
     * <BL>
     * <LI> coords - list of floating point values representing the X,Y,Z
     *   coordinates of each vertex in this line set.  A list of vertices
     *   is generated from the coords list by breaking it up into triplets.
     * <LI> indices - list of indices into the vertex set.  Indices are grouped
     *   into sets of two or more.  Sets are separated by the dummy index
     *   "-1".  Each set of 2+ indices represets a polyline.
     * </BL>
     */
    public void createEdgesFromVerts( float [] coords, int [] indices)
    {
	// Make sure number of coords is divisible by three.
	// We're relying on assumption that each coord has an X, Y, and Z
	// component present in list.

	int num_read = (coords.length / 3) * 3;
	System.err.println("Will read this many vert coords: " + num_read);
	if (coords.length % 3 != 0) {
	    System.err.println("Obj.createFacesFromVerts(): ");
	    System.err.println(" Number of 'coords' passed in should be divisible by 3!");
	    System.err.println(" Number of 'coords': " + coords.length );
	    //return;
	}
	
	// Add each vertex (as specified by a coordinate triplet in 'coord' list) to this Obj

	int cur_coord = 0;
	while (cur_coord < num_read-2) {
	    Vertex new_vert = MemMgr.Vertex( coords[cur_coord], coords[cur_coord+1], coords[cur_coord+2] );
	    // System.err.println( "Adding vertex!: " + coords[cur_coord] + " "  +  coords[cur_coord+1] + " " + coords[cur_coord+2] );
	    addVertex( new_vert);
	    cur_coord+= 3;
	}
	
	// Make polylines by walking down 'indices' list and chaining together
	// the vertices listed.  Sets of indices belonging to a single
	// polyline are separated by the index "-1".
	
	Vector line_indx = new Vector(10);
	int cur_indx = 0;
	
	while (cur_indx < indices.length) {

	    // Copy next group of indices out of index list (up to but not
	    // including the "-1" separator, which is discarded)

	    while (indices[cur_indx] != -1) {
		line_indx.addElement( new Integer( indices[cur_indx]) );
		cur_indx++;
	    }

	    // Discard the -1 separator by incrementing past it
	    cur_indx++;

	    // Build a polyline out of the contents of face_indx
	    addPolyline( line_indx);

	    // Clear out line list for next group of indices, and repeat.
	    line_indx.removeAllElements();
	}
    }

    protected void addPolyline( Vector line_indx)
    {
	// Polyline must have minimum two vertices.  If less, polyline is invalid; return.
	if (line_indx.size() < 2) {
	    System.err.println("Obj.addPolyline(): invalid polyline.  Need min. 2 vertices.");
	    return;
	}

	int indx1 = 0;
	int indx2 = 0;

	// Loop across vertices in list, building a polyline segment by segment
	for (int i=0; i<line_indx.size()-1; i++) {

	    // Get next two indices from list
	    indx1 = ((Integer)line_indx.elementAt(i)).intValue();
	    indx2 = ((Integer)line_indx.elementAt(i+1)).intValue();

	    // Make sure indices are in range
	    if (indx1 >= this.vlist.size() || indx1 < 0 ) {
		System.err.println("Obj.addPolyline(): vertex index out of range: " + indx1 );
		continue;
	    }
	    if (indx2 >= this.vlist.size() || indx2 < 0 ) {
		System.err.println("Obj.addPolyline(): vertex index out of range: " + indx2 );
		System.err.println(" Lost one segment out of polyline.");
		continue;
	    }
	    
	    // If indices are in range, make a line segment out of vertex pair
	    Edge e = MemMgr.Edge();
	    addEdge( e, indx1, indx2);
	}
    }

    
    public void makeSphere( int long_res, int lat_res, float s_start, float s_end,
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
	
	makeOpenRevolve( lat_res, y, r, s_start, s_end, t_start, t_end);
    }
    
    public void makeCone( int long_res, int lat_res, float s_start, float s_end,
			  float t_start, float t_end)
    {
	// Silhouette has (at minimum) a vertex at its top point, at its bottom
	// corner, and in the center of its base.  There will be <long_res> number
	// of additional vertices along the side of the cone, and another <long_res>
	// vertices along its base.
	// In total, the silhouette of the cone (its longitudinal cross-section)
	// has <long_res+3> vertices.
	int n_pts = long_res*2 + 3;
	float [] y = new float[ n_pts ];
	float [] r = new float[ n_pts ];
	
	// Set top point, bottom corner point, and center base point for
	// cone silhouette (which is half of a triangle)
	y[0] = (float)1.0;
	r[0] = (float)0.0;
	y[long_res+1] = (float)-1.0;
	r[long_res+1] = (float)1.0;
	y[n_pts-1] = (float)-1.0;
	r[n_pts-1] = (float)0.0;
	
	// Fill in points along side of silhouette
	float side_y_step = (float)-2.0 / (float)(long_res+1);
	float side_y = (float)1.0 + side_y_step;
	float side_r_step = (float)1.0 / (float)(long_res+1);
	float side_r = side_r_step;
	
	for (int i=1; i<long_res+1; i++) {
	    y[i] = side_y;
	    r[i] = side_r;
	    side_y += side_y_step;
	    side_r += side_r_step;
	}
	
	// Fill in points along base of silhouette
	float base_y = (float)-1.0;
	float base_r_step = (float)-1.0 / (float)(long_res+1);
	float base_r = (float)1.0 + base_r_step;
	
	for (int i=long_res+2; i<long_res*2+2; i++) {
	    y[i] = base_y;
	    r[i] = base_r;
	    base_r += base_r_step;
	}
	
	makeOpenRevolve( lat_res, y, r, s_start, s_end, t_start, t_end);
	
    }
    
    public void makeTorus( int long_res, int lat_res, float inner_rad, float outer_rad,
			   float s_start, float s_end, float t_start, float t_end)
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
	
	makeClosedRevolve( lat_res, y, r, s_start, s_end, t_start, t_end);
	
    }
    
    
    public void makeOpenRevolve(int res, float [] y, float [] r, float s_start, float s_end,
				float t_start, float t_end)
    {
	int lati = y.length-2;
	int longi  = res;
	
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
		addVertex( verts[i+longi*j] );
	    }
	}
	
	// Add north and south poles
	addVertex( n_pole);
	addVertex( s_pole);
	
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
		addEdge(
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
		addEdge(
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
		addEdge(
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
		addEdge(
			s_edges[2*k],
				// Right-most vertex in grid
			k*longi + longi-1,
				// Left-most vertex in grid
			k*longi
			);
		// Add diagonal edge zipping seam from right-most
		// vertex in grid to left-most one.
		s_edges[2*k+1] = MemMgr.Edge();
		addEdge(
			s_edges[ 2*k+1 ],
				// Right vertex
			k*longi,
				// Left vertex
			(k+1)*longi + longi-1
			);
	    }
	// Add one more edge at bottom
	s_edges[2*k] = MemMgr.Edge();
	addEdge(
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
	    addEdge( c1_edges[i], i, N_POLE);
	}
	// South pole
	for (int i=0; i<longi; i++) {
	    c2_edges[i] = MemMgr.Edge();
	    addEdge( c2_edges[i], (lati-1)*longi + i, S_POLE);
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
		addTriangle(
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
		addTriangle(
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
	    addTriangle(
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
	    addTriangle(
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
	    addTriangle(
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
	addTriangle(
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
	    addTriangle(
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
	addTriangle(
		    tri,
		    (lati-1) * longi,
		    S_POLE,
		    (lati-1) * longi + k,
		    C2_EDGE,
		    C2_EDGE + k,
		    S_EDGE + lati*2-2
		    );
	
	
	calcVertexNormals();
    }
    
    
    
    public void makeClosedRevolve(int res, float [] y, float [] r, float s_start, float s_end,
				  float t_start, float t_end)
    {
	int lati   = y.length;
	int longi  = res;
	
	double longi_step = Math.PI * 2.0  / (double)(longi);
	
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
		addVertex( vert );
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
		addEdge(
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
		addEdge(
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
		addEdge(
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
		addTriangle(
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
		addTriangle(
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
	
	calcVertexNormals();
    }
    
    public void makeOpenBox()
    {
	int [] text = new int[128*128];
	
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
	
	addVertex( v0);
	addVertex( v1);
	addVertex( v2);
	addVertex( v3);
	addVertex( v4);
	addVertex( v5);
	addVertex( v6);
	addVertex( v7);
	
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
	
	addEdge( e00, 0, 1);
	addEdge( e01, 1, 2);
	addEdge( e02, 2, 3);
	addEdge( e03, 3, 0);
	addEdge( e04, 4, 5);
	addEdge( e05, 5, 6);
	addEdge( e06, 6, 7);
	addEdge( e07, 7, 4);
	addEdge( e08, 4, 0);
	addEdge( e09, 5, 1);
	addEdge( e10, 6, 2);
	addEdge( e11, 7, 3);
	//addEdge( e12, 3, 1);
	addEdge( e13, 6, 1);
	//addEdge( e14, 7, 5);
	addEdge( e15, 7, 0);
	addEdge( e16, 4, 1);
	addEdge( e17, 7, 2);
	
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
	
	addTriangle( t02,  5, 1, 6,  9, 12, 5 );
	addTriangle( t03,  1, 2, 6,  1, 10, 12);
	
	//addTriangle( t04,  4, 5, 7,  7, 4, 14 );
	//addTriangle( t05,  5, 6, 7,  5, 6, 14 );
	
	addTriangle( t06,  4, 7, 0,  7, 13, 8 );
	addTriangle( t07,  7, 3, 0,  13, 11, 3);
	addTriangle( t08,  4, 0, 1,  8, 0, 14 );
	addTriangle( t09,  4, 1, 5,  14, 9, 4 );
	addTriangle( t10,  7, 2, 3,  11, 15, 2);
	addTriangle( t11,  7, 6, 2,  15, 6, 10);
	
	calcVertexNormals();
    }
    
    public void makeCube()
    {
	int [] text = new int[128*128];
	
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
	
	addVertex( v0);
	addVertex( v1);
	addVertex( v2);
	addVertex( v3);
	addVertex( v4);
	addVertex( v5);
	addVertex( v6);
	addVertex( v7);
	
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
	
	addEdge( e00, 0, 1);
	addEdge( e01, 1, 2);
	addEdge( e02, 2, 3);
	addEdge( e03, 3, 0);
	addEdge( e04, 4, 5);
	addEdge( e05, 5, 6);
	addEdge( e06, 6, 7);
	addEdge( e07, 7, 4);
	addEdge( e08, 4, 0);
	addEdge( e09, 5, 1);
	addEdge( e10, 6, 2);
	addEdge( e11, 7, 3);
	addEdge( e12, 3, 1);
	addEdge( e13, 6, 1);
	addEdge( e14, 7, 5);
	addEdge( e15, 7, 0);
	addEdge( e16, 4, 1);
	addEdge( e17, 7, 2);
	
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
	
	addTriangle( t00,  0, 3, 1,  0, 3, 12 );
	addTriangle( t01,  1, 3, 2,  1, 12, 2 );
	
	addTriangle( t02,  5, 1, 6,  9, 13, 5 );
	addTriangle( t03,  1, 2, 6,  1, 10, 13);
	
	addTriangle( t04,  4, 5, 7,  7, 4, 14 );
	addTriangle( t05,  5, 6, 7,  5, 6, 14 );
	
	addTriangle( t06,  4, 7, 0,  7, 15, 8 );
	addTriangle( t07,  7, 3, 0,  15, 11, 3);
	addTriangle( t08,  4, 0, 1,  8, 0, 16 );
	addTriangle( t09,  4, 1, 5,  16, 9, 4 );
	addTriangle( t10,  7, 2, 3,  11, 17, 2);
	addTriangle( t11,  7, 6, 2,  17, 6, 10);
	
	calcVertexNormals();
    }
    
    Obj next;
    
    public void render( Mat4f xform, Mat4f nxform, float time)
    {
		Mat4f my_xform   = MemMgr.Mat4f();
		Mat4f my_n_xform = MemMgr.Mat4f();
		
		Alg.mult( ctm().ctm(), xform, my_xform);
		Alg.mult( ctm().normal_ctm(), nxform, my_n_xform);
		
		if (mat.PARTICLE == true) {
			if (mat._lightmodel == rMaterial.FLARE) {
				rend.addToTranspQueue( my_xform, my_n_xform, this);
			} else if (mat._lightmodel == rMaterial.SPRITE) {
				rend.drawParticle( my_xform, this);
			}
		} else {
			if ( mat.TRIANGLES == true) {
			if (mat._lightmodel == rMaterial.TRANSP ) {
				rend.addToTranspQueue( my_xform, my_n_xform, this);
			} else {
				rend.drawTriangles( my_xform, my_n_xform, tlist);
			}
			}
			if ( mat.WIREFRAME == true) {
				rend.drawWireframe( my_xform, elist, mat);
			}
			if ( mat.POINTSET == true) {
				rend.drawPointset( my_xform, vlist, mat);
			}
		}
		
		if (mat._lightmodel != rMaterial.TRANSP && mat.PARTICLE != true) {
			MemMgr.done( my_xform);
			MemMgr.done( my_n_xform);
		}
    }
}



class rMaterial
{
    // Lighting models
    final static int DONT_DRAW = -1;
    final static int SOLID     = 0;
    final static int FLAT      = 1;
    final static int LIGHT_MAP = 2;
    final static int GOURAUD   = 3;
    final static int PHONG     = 4;
    final static int FOG       = 5;
    final static int TRANSP    = 6;
    final static int FLARE     = 7;
    final static int SPRITE        = 8;
    final static int BILLBOARD     = 9;
    final static int INTENSITY_FLARE = 10;
    
    // Rendering speeds (for texture map modes)
    final static int FAST      = 2;
    final static int FAST16    = 1;
    final static int SLOW      = 0;
    
    // Fog types
    final static int LINEAR    = 0;
    final static int SQUARE    = 1;

    // Point/line type
    final static int PIXEL     = 0;
    final static int WU        = 1;
    final static int GAUSSIAN  = 2;
    final static int THICK     = 3;

    int [][] _gaussian_dots = null;
    int _gaussian_res = 16;
    int _n_gaussian_dots = 16;
    int _near_color;
    int _far_color;
    int _f_red, _f_green, _f_blue;
    int _near_red, _near_green, _near_blue;
    int _far_red, _far_green, _far_blue;

    // Surface models/attributes
    boolean BACKFACE_CULL = true;
    boolean WIREFRAME     = false;
    boolean POINTSET      = false;
    boolean TRIANGLES     = true;
    boolean TEXTURE       = false;
    boolean BUMP          = false;
    boolean ENV           = false;
    boolean TRANSPARENT   = false;
    boolean ANTIALIAS     = true;
    boolean PARTICLE      = false;
    int     SPEED         = FAST;
    
    int [] _light_map   = null;
    int [] _texture     = null;
    int [] _bump_map    = null;
    int [] _env_map     = null;
    int [] _color_blend = null;
    
    int _lightmodel = 0;
    int _color = 0;
    int _pointstyle = WU;
    int _linestyle = WU;
    
    int _sprite_width;
    int _sprite_height;
    
    int _amb_R  = 0;
    int _amb_G  = 0;
    int _amb_B  = 0;
    int _dif_R  = 0;
    int _dif_G  = 0;
    int _dif_B  = 0;
    int _spec_R = 0;
    int _spec_G = 0;
    int _spec_B = 0;
    int _fog_R  = 0;
    int _fog_G  = 0;
    int _fog_B  = 0;
    int _transp_R = 0;
    int _transp_G = 0;
    int _transp_B = 0;
    
    // Fog attributes
    float _fog_near;
    float _fog_far;
    float _fog_near_val;
    float _fog_far_val;
    int   _fog_type;

    rMaterial next;
}

class Light
{
    Vec3f dir;
    VecSf s_dir;
    float red;
    float green;
    float blue;
    float intensity;
    
    Light next;
}

