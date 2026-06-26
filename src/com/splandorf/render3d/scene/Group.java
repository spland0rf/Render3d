package com.splandorf.render3d.scene;

import com.splandorf.render3d.*;
import com.splandorf.render3d.scene.*;
import com.splandorf.render3d.math.*;
import com.splandorf.render3d.math.Mat4f;
import java.util.ArrayList;

public class Group extends Node
{
    public ArrayList<Node> children = new ArrayList<Node>(10);

    public Group(String new_name)
    {
        super( new_name);
    }

    public Group() {
        super();
    }

    public void addChild( Node node)
    {
	    children.add( node);
    }

    public void render( Render rend, Mat4f xform, Mat4f nxform, float time)
    {
        Mat4f my_xform   = MemMgr.Mat4f();
        Mat4f my_n_xform = MemMgr.Mat4f();

        Alg.mult( ctm().ctm(), xform, my_xform);
        Alg.mult( ctm().normal_ctm(), nxform, my_n_xform);

        Node child;
        for (int i=0; i<children.size(); i++) {
            child = (Node)children.get(i);
            child.render( rend, my_xform, my_n_xform, time);
        }

        MemMgr.done( my_xform);
        MemMgr.done( my_n_xform);
    }
}
