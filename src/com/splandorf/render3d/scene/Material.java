package com.splandorf.render3d;

class Material extends Object
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
