package com.splandorf.render3d;

import com.splandorf.render3d.Ctm;
import com.splandorf.render3d.Node;

class Group extends Node
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
