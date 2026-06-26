package com.splandorf.render3d;

import com.splandorf.render3d.Ctm;
import com.splandoorf.render3d.Render;

abstract class Node
{
    Ctm ctm;
    String name;
    
    public Node(String new_name)
    {
        ctm = MemMgr.Ctm();
        name = new_name;
    }

    public Node() {
        ctm = MemMgr.Ctm();
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