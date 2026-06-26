package com.splandorf.render3d.scene;

public class Material extends Object
{
   // Lighting models
    final public static int DONT_DRAW = -1;
    final public static int SOLID     = 0;
    final public static int FLAT      = 1;
    final public static int LIGHT_MAP = 2;
    final public static int GOURAUD   = 3;
    final public static int PHONG     = 4;
    final public static int FOG       = 5;
    final public static int TRANSP    = 6;
    final public static int FLARE     = 7;
    final public static int SPRITE        = 8;
    final public static int BILLBOARD     = 9;
    final public static int INTENSITY_FLARE = 10;
    
    // Rendering speeds (for texture map modes)
    final public static int FAST      = 2;
    final public static int FAST16    = 1;
    final public static int SLOW      = 0;
    
    // Fog types
    final public static int LINEAR    = 0;
    final public static int SQUARE    = 1;

    // Point/line type
    final public static int PIXEL     = 0;
    final public static int WU        = 1;
    final public static int GAUSSIAN  = 2;
    final public static int THICK     = 3;

    public int [][] _gaussian_dots = null;
    public int _gaussian_res = 16;
    public int _n_gaussian_dots = 16;
    public int _near_color;
    public int _far_color;
    public int _f_red, _f_green, _f_blue;
    public int _near_red, _near_green, _near_blue;
    public int _far_red, _far_green, _far_blue;

    // Surface models/attributes
    public boolean BACKFACE_CULL = true;
    public boolean WIREFRAME     = false;
    public boolean POINTSET      = false;
    public boolean TRIANGLES     = true;
    public boolean TEXTURE       = false;
    public boolean BUMP          = false;
    public boolean ENV           = false;
    public boolean TRANSPARENT   = false;
    public boolean ANTIALIAS     = true;
    public boolean PARTICLE      = false;
    public int     SPEED         = FAST;
    
    public int [] _light_map   = null;
    public int [] _texture     = null;
    public int [] _bump_map    = null;
    public int [] _env_map     = null;
    public int [] _color_blend = null;
    public int [] _wu_array     = null;
    
    public int _lightmodel = 0;
    public int _color = 0;
    public int _pointstyle = WU;
    public int _linestyle = WU;
    
    public int _sprite_width;
    public int _sprite_height;
    
    public int _amb_R  = 0;
    public int _amb_G  = 0;
    public int _amb_B  = 0;
    public int _dif_R  = 0;
    public int _dif_G  = 0;
    public int _dif_B  = 0;
    public int _spec_R = 0;
    public int _spec_G = 0;
    public int _spec_B = 0;
    public int _fog_R  = 0;
    public int _fog_G  = 0;
    public int _fog_B  = 0;
    public int _transp_R = 0;
    public int _transp_G = 0;
    public int _transp_B = 0;
    
    // Fog attributes
    public float _fog_near;
    public float _fog_far;
    public float _fog_near_val;
    public float _fog_far_val;
    public int   _fog_type;

    public Material next;
}
