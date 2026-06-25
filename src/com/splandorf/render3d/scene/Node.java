package com.splandorf.render3d;

import com.splandorf.render3d.Ctm;

abstract class Node
{
    Ctm ctm;
    String name;
    render rend;
    
    public Node(String new_name, render rend)
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