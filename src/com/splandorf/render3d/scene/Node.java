package com.splandorf.render3d.scene;

import com.splandorf.render3d.Render;
import com.splandorf.render3d.math.*;

abstract class Node
{
    Ctm ctm;
    String name;
    
    public Node(String new_name)
    {
        ctm = new Ctm();
        name = new_name;
    }

    public Node() {
        ctm = new Ctm();
    }

    public void setName(String new_name)
    {
        name = new_name;
    }   

    public String getName()
    {
        return name;
    }   

    public Ctm ctm()
    {
	    return ctm;
    }

    public abstract void render( Render rend, Mat4f xform, Mat4f nxform,  float time);
}