package com.splandorf.render3d.scene;

import com.splandorf.render3d.*;
import com.splandorf.render3d.math.*;

public class Anim extends Group
{
    int style;

    float start_time;
    float end_time;
    float last_time = (float)0.0;
    float duration;
    float cycle;

    Vec3f start_val = null;
    Vec3f end_val   = null;
    Vec3f raw_rate  = null;

    int xform = 0;

    boolean running    = false;

    final static public int NONE       = 0;
    final static public int ONE_SHOT   = 1;
    final static public int LOOP       = 2;
    final static public int PING_PONG  = 3;
    final static public int CONTINUOUS = 4;
    final static public int JUMP       = 5;

    final static public int TRANS = 1;
    final static public int ROT   = 2;
    final static public int SCALE = 3;

    public Anim( String new_name)
    {
	    super( new_name);
    }

    public void render( Render rend, Mat4f xform, Mat4f nxform, float time)
    {
        updateAnim( time);

        super.render( rend, xform, nxform, time);
    }

    /**
     * Initialize animation.
     * <BL>
     *  <LI>xform - type of transformation.  Can be either TRANS, ROT, or SCALE
     *  <LI>style - animation behavior.  Can be NONE, LOOP, ONE_SHOT, PING_PONG, or CONTINUOUS.
     *  <LI>start_time and end_time - time in seconds, renderer begins at zero.
     *  <LI>start_val and end_val - vectors encoding values for anim at start_time and end_time.\
     * </BL>
     * An Anim can be used for only one type of transformation (trans, rot, or scale).
     * Successive calls to this method specifying different xform types will cause the
     * previously running animation to immediately terminate, and be replaced with the
     * current animation. (i.e., an Anim cannot transform scaling and rotation simultaneously.)
     */
    public void initAnim( int xform, int style, float start_time, float end_time, float cycle, Vec3f start_val, Vec3f end_val)
    {
        duration   = (end_time - start_time);
        if (duration <= (float)0.0) {
            System.err.println("Zero or negative anim time in setAnim()!");
            return;
        }
        if (start_val == null || end_val == null) {
            System.err.println("NULL start or end values in setAnim()!");
            return;
        }

        this.start_time = start_time;
        this.end_time   = end_time;
        this.start_val  = start_val;
        this.end_val    = end_val;
        this.style      = style;
        this.cycle      = cycle;
        this.xform      = xform;

        if (raw_rate == null) {
            raw_rate = MemMgr.Vec3f();
        }
        // We can treat ONE_SHOTs as LOOPs that just loop once, between
        // start_time and end_time.  [This is accomplished by setting
        // "cycle" to be equal to "duration", so the animation runs
        // exactly once over its lifecycle.]
        if (style == ONE_SHOT) {
            style = LOOP;
            cycle = duration;
        }

        raw_rate.x  = (end_val.x - start_val.x) / cycle;
        raw_rate.y  = (end_val.y - start_val.y) / cycle;
        raw_rate.z  = (end_val.z - start_val.z) / cycle;

        running = true;
    }

    public void startAnim()
    {
	    running = true;
    }

    public void stopAnim()
    {
	    running = false;
    }
    
    /**
     * updateAnim
     * <BR>
     * Given the current time in seconds, calculates the appropriate value
     * for the animation to assume at the current time.  A given animation 
     * with a particular start_time, end_time, start_val, and end_val will 
     * take on different values depending on whether its style is LOOP, 
     * PING_PONG, ONE_SHOT, or CONTINUOUS.
     * Updates the Anim's Ctm to reflect the current animation value.
     */
    public void updateAnim(float cur_time)
    {
        if (running == false) {
            return;
        }

        float elapsed_time = cur_time - last_time;
        float ratio        = (float)0.0;
        
        if (style==NONE) {
            return;

        } else if (style==LOOP) {
            ratio = ( elapsed_time % cycle ) / cycle;
            setAnim( ratio);

        } else if (style==PING_PONG) {
            ratio = ( elapsed_time % ((float)2.0 * cycle) ) / cycle;
            if (ratio >= (float)0.0 && ratio < (float)1.0) {
                setAnim( ratio);
            } 
            else if (ratio >= (float)1.0 && ratio <= (float)2.0) {
                ratio = (float)2.0 - ratio;
                setAnim( ratio);
            }

        } else if (style==JUMP) {
            if (last_time < end_time && cur_time >= end_time) {
                setAnim( (float)1.0 );
            }

        } else if (style==CONTINUOUS) {
            ratio = (cur_time - start_time) / cycle;
            setAnim( ratio);
        }

        last_time = cur_time;

        // Make sure CONTINUOUS animation loops never time out.
        // [Always set last_time to zero before comparing to end_time.]
        if (style==CONTINUOUS) {
            last_time = (float)0.0;
        }

        if (last_time > end_time) {
            running = false;
        }
    }
    
    /**
     * setAnim
     * <BR>
     * For a given ratio [0..1] between start_val and end_val,
     * set the Anim's CTM to reflect this new interpolated value.
     * Note: for CONTINUOUS type animations, ratio can be > 1.0
     */
    protected void setAnim( float ratio)
    {
        if (xform == TRANS) {

            ctm.set_trans( start_val.x + ratio * raw_rate.x,
                start_val.y + ratio * raw_rate.y,
                start_val.z + ratio * raw_rate.z
                );

        } else if (xform == ROT) {

            ctm.set_rot( Alg.IDENT_MAT);
            ctm.post_rot_x( (start_val.x + ratio * raw_rate.x) % (float)6.28318);
            ctm.post_rot_y( (start_val.y + ratio * raw_rate.y) % (float)6.28318);
            ctm.post_rot_z( (start_val.z + ratio * raw_rate.z) % (float)6.28318);

        } else if (xform == SCALE) {

            ctm.set_scale( start_val.x + ratio * raw_rate.x,
                start_val.y + ratio * raw_rate.y,
                start_val.z + ratio * raw_rate.z
                );

        }
    }

}
