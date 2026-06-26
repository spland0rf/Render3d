package com.splandorf.render3d;

import com.splandorf.render3d.Ctm;
import com.splandorf.render3d.Node;
import com.splandorf.render3d.Render;

class Group extends Node
{
    public Vector children = new Vector(10);;

    public Group(String new_name)
    {
        super( new_name);
    }

    public Group() {
        super();
    }

    public void addChild( rNode rNode)
    {
        super();
	    children.addElement( rNode);
    }

    public void render( Render rend, Mat4f xform, Mat4f nxform, float time)
    {
        Mat4f my_xform   = MemMgr.Mat4f();
        Mat4f my_n_xform = MemMgr.Mat4f();

        Alg.mult( ctm().ctm(), xform, my_xform);
        Alg.mult( ctm().normal_ctm(), nxform, my_n_xform);

        rNode child;
        for (int i=0; i<children.size(); i++) {
            child = (rNode)children.elementAt(i);
            child.render( rend, my_xform, my_n_xform, time);
        }

        MemMgr.done( my_xform);
        MemMgr.done( my_n_xform);
    }
}
