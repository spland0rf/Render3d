package com.splandorf.render3d.scene;

import com.splandorf.render3d.*;
import com.splandorf.render3d.math.*;


public class Ctm
{
	public Vec3f _scale      = null;
	public Vec3f _center     = null;
	public Mat4f _rot        = null;
	public Vec3f _trans      = null;
	public Mat4f _ctm        = null;
	public Vec3f _inv_scale  = null;
	public Vec3f _inv_center = null;
	public Mat4f _inv_rot    = null;
	public Vec3f _inv_trans  = null;
	public Mat4f _inv_ctm    = null;
	public Mat4f _transp     = null;

	public boolean _inv_dirty = true;
	public boolean _dirty     = true;

	public Ctm()
	{
		_scale      = MemMgr.Vec3f((float)1.0, (float)1.0, (float)1.0);
		_center     = MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0);
		_rot        = MemMgr.Mat4f();
		_trans      = MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0);
		_ctm        = MemMgr.Mat4f();
		_inv_scale  = MemMgr.Vec3f((float)1.0, (float)1.0, (float)1.0);
		_inv_center = MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0);
		_inv_rot    = MemMgr.Mat4f();
		_inv_trans  = MemMgr.Vec3f((float)0.0, (float)0.0, (float)0.0);
		_inv_ctm    = MemMgr.Mat4f();
		_transp     = MemMgr.Mat4f();
	}

	public Vec3f scale()
	{
		return _scale;
	}

	public Vec3f center()
	{
		return _center;
	}

	public Mat4f rot()
	{
		return _rot;
	}

	public Vec3f trans()
	{
		return _trans;
	}

	public void set_scale( float x, float y, float z)
	{
		_scale.x = x;
		_scale.y = y;
		_scale.z = z;
		_inv_scale.x = (float)1.0/x;
		_inv_scale.y = (float)1.0/y;
		_inv_scale.z = (float)1.0/z;

		_dirty     = true;
		_inv_dirty = true;
	}

	public void set_center( float x, float y, float z)
	{
		_center.x = x;
		_center.y = y;
		_center.z = z;
		_inv_center.x = -x;
		_inv_center.y = -y;
		_inv_center.z = -z;

		_dirty     = true;
		_inv_dirty = true;
	}

	public void set_rot( Mat4f m)
	{
		_rot.m00 = _inv_rot.m00 = m.m00;
		_rot.m01 = _inv_rot.m10 = m.m01;
		_rot.m02 = _inv_rot.m20 = m.m02;
		_rot.m10 = _inv_rot.m01 = m.m10;
		_rot.m11 = _inv_rot.m11 = m.m11;
		_rot.m12 = _inv_rot.m21 = m.m12;
		_rot.m20 = _inv_rot.m02 = m.m20;
		_rot.m21 = _inv_rot.m12 = m.m21;
		_rot.m22 = _inv_rot.m22 = m.m22;

		_dirty     = true;
		_inv_dirty = true;
	}

	public void set_trans( float x, float y, float z)
	{
		_trans.x = x;
		_trans.y = y;
		_trans.z = z;
		_inv_trans.x = -x;
		_inv_trans.y = -y;
		_inv_trans.z = -z;

		_dirty     = true;
		_inv_dirty = true;
	}

	public void scale( float x, float y, float z)
	{
		_scale.x *= x;
		_scale.y *= y;
		_scale.z *= z;
		_inv_scale.x /= x;
		_inv_scale.y /= y;
		_inv_scale.z /= z;

		_dirty     = true;
		_inv_dirty = true;
	}

	public void move_center( float x, float y, float z)
	{
		_center.x += x;
		_center.y += y;
		_center.z += z;
		_inv_center.x += -x;
		_inv_center.y += -y;
		_inv_center.z += -z;

		_dirty     = true;
		_inv_dirty = true;
	}

	public void rot( Mat4f m)
	{
		Alg.mult( _rot, m);
		Alg.transpose( m, _transp);
		Alg.premult( _inv_rot, _transp );
			
		_dirty     = true;
		_inv_dirty = true;
	}

	public void pre_rot_x( float x)
	{
		Alg.pre_rot_x( _rot, x);
		Alg.post_rot_x( _inv_rot, -x);		
		_dirty     = true;
		_inv_dirty = true;
	}
	public void post_rot_x( float x)
	{
		Alg.post_rot_x( _rot, x);
		Alg.pre_rot_x( _inv_rot, -x);
		_dirty     = true;
		_inv_dirty = true;
	}
	public void pre_rot_y( float y)
	{
		Alg.pre_rot_y( _rot, y);
		Alg.post_rot_y( _inv_rot, -y);
		_dirty     = true;
		_inv_dirty = true;
	}
	public void post_rot_y( float y)
	{
		Alg.post_rot_y( _rot, y);
		Alg.pre_rot_y( _inv_rot, -y);
		_dirty     = true;
		_inv_dirty = true;
	}
	public void pre_rot_z( float z)
	{
		Alg.pre_rot_z( _rot, z);
		Alg.post_rot_z( _inv_rot, -z);
		_dirty     = true;
		_inv_dirty = true;
	}
	public void post_rot_z( float z)
	{
		Alg.post_rot_z( _rot, z);
		Alg.pre_rot_z( _inv_rot, -z);
		_dirty     = true;
		_inv_dirty = true;
	}

	public void trans( float x, float y, float z)
	{
		_trans.x += x;
		_trans.y += y;
		_trans.z += z;
		_inv_trans.x += -x;
		_inv_trans.y += -y;
		_inv_trans.z += -z;

		_dirty     = true;
		_inv_dirty = true;
	}

	public void orient( Vec3f from, Vec3f at, Vec3f up)
	{
		Alg.orient( _rot, from, at, up);
		set_trans( from.x, from.y, from.z);
		_rot.m03 = (float)0.0;
		_rot.m13 = (float)0.0;
		_rot.m23 = (float)0.0;
		set_rot( _rot);
	}

	public void clear()
	{
		set_scale        ( (float)1.0, (float)1.0, (float)1.0);
		set_center       ( (float)0.0, (float)0.0, (float)0.0);
		Alg.set_trans( _rot, (float)0.0, (float)0.0, (float)0.0);		
		Alg.set_trans( _inv_rot, (float)0.0, (float)0.0, (float)0.0);		
		set_trans        ( (float)0.0, (float)0.0, (float)0.0);
		_dirty     = true;
		_inv_dirty = true;
	}

	public Mat4f ctm() 
	{
		if (_dirty==false) {	
			return _ctm;
		} else {
			Alg.set_scale ( _ctm, _scale.x, _scale.y, _scale.z);
			Alg.post_trans( _ctm, _center.x, _center.y, _center.z);
			Alg.mult      ( _ctm, _rot);
			Alg.post_trans( _ctm, _trans.x-_center.x, _trans.y-_center.y, _trans.z-_center.z);
			_dirty = false;
			return _ctm;
		}
	}

	public Mat4f inv_ctm()
	{
		if (_inv_dirty==false) {	
			return _inv_ctm;
		} else {
			Alg.set_scale( _inv_ctm, _inv_scale.x, _inv_scale.y, _inv_scale.z);
			Alg.pre_trans( _inv_ctm, _inv_center.x, _inv_center.y, _inv_center.z);
			Alg.premult  ( _inv_ctm, _inv_rot);
			Alg.pre_trans( _inv_ctm, _inv_trans.x-_inv_center.x, _inv_trans.y-_inv_center.y, _inv_trans.z-_inv_center.z);
			_inv_dirty = false;
			return _inv_ctm;
		}
	}

	public Mat4f normal_ctm() 
	{
		if (_dirty==false) {	
			return _ctm;
		} else {
			Alg.set_scale ( _ctm, _inv_scale.x, _inv_scale.y, _inv_scale.z);
			Alg.post_trans( _ctm, _center.x, _center.y, _center.z);
			Alg.mult      ( _ctm, _rot);
			Alg.post_trans( _ctm, _trans.x-_center.x, _trans.y-_center.y, _trans.z-_center.z);
			_dirty = false;
			return _ctm;
		}
	}

	public Mat4f inv_normal_ctm()
	{
		if (_inv_dirty==false) {	
			return _inv_ctm;
		} else {
			Alg.set_scale( _inv_ctm, _scale.x, _scale.y, _scale.z);
			Alg.pre_trans( _inv_ctm, _inv_center.x, _inv_center.y, _inv_center.z);
			Alg.premult  ( _inv_ctm, _inv_rot);
			Alg.pre_trans( _inv_ctm, _inv_trans.x-_inv_center.x, _inv_trans.y-_inv_center.y, _inv_trans.z-_inv_center.z);
			_inv_dirty = false;
			return _inv_ctm;
		}
	}

}


