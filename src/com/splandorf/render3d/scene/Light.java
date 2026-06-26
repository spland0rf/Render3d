package com.splandorf.render3d.scene;

import com.splandorf.render3d.math.Vec3f;
import com.splandorf.render3d.math.VecSf;

public class Light
{
    public Vec3f dir;
    public VecSf s_dir;
    public float red;
    public float green;
    public float blue;
    public float intensity;
    
    public Light next;
}

