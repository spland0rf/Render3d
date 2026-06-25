package com.splandorf.render3d.shader;

public class Sprite extends Shader {
  
    public void drawParticle( Mat4f m, Obj obj)
    {
		Vec3f loc = MemMgr.Vec3f( (float)0.0, (float)0.0, (float)0.0);
		
		Alg.mult( m, loc);
		
		if (loc.z < (float)0.0) return;
		
		int cx = this.size().width / 2;
		int cy = this.size().height / 2;
		float xm = (float)cx*(float)0.6;
		float ym = (float)cy*(float)0.6;
		int x    = cx + (int)(loc.x / loc.z * xm);
		int y    = cy + (int)(loc.y / loc.z * ym);
		int zbuf = (int)((float)10000.0 / loc.z);
		if (zbuf > MAX24BIT) zbuf = MAX24BIT;
		if (zbuf < 1) zbuf = 1;
		
		rMaterial mat = obj.mat;
		
		if (mat._lightmodel == rMaterial.FLARE || mat._lightmodel == rMaterial.SPRITE)  {
			
			int start_y = y - mat._sprite_height / 2;
			int end_y   = y + mat._sprite_height / 2;
			int start_x = x - mat._sprite_width  / 2;
			int end_x   = x + mat._sprite_width  / 2;
			
			int sprite_y = 0;
			
			if (start_y > _height || end_y < 0 || start_x > _width || end_y < 0) {
				return;
			}
			if (start_y < 0) {
				sprite_y += -start_y;
				start_y = 0;
			}
			if (end_y >= _height) {
				end_y = _height-1;
			}
			
			for (int i=start_y; i<end_y; i++) {
				draawSpriteSpan( i, sprite_y, start_x, end_x, zbuf, mat);
				sprite_y++;
			}
			
		} else if (mat._lightmodel == rMaterial.BILLBOARD) {
			
		}
		
		MemMgr.done( loc);
		
    }


}