
/**
					} else if (mat._lightmodel == Material.FOG) {
						
						if (mat.TEXTURE == true) {

							// This is to correct for the effect
							// of a camera zoom factor on fog.
							// We want fog to remain an illusion of
							// constant world-space.  But adding
							// a zoom factor to the camera moves
							// points closer-to/farther-from the
							// camera.  We want the points themselves
							// to move, but we want fog calculated on
							// them as if the zoom factor really were 1.0

							p1.z *= _zoom;
							p2.z *= _zoom;
							p3.z *= _zoom;
							// Get fog contribution at vertex 1
							if (p1.z < mat._fog_near) {
								fog = mat._fog_near_val;
							} else if (p1.z > mat._fog_far) {
								fog = mat._fog_far_val;
							} else {
								fog = (p1.z-mat._fog_near) / (mat._fog_far - mat._fog_near);
								if (mat._fog_type == Material.SQUARE) fog *= fog;
								fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
							}
							t.v1.r = t.v1.g = t.v1.b = ((int)((float)255.0 * (1.0-fog))<<16);
							// Get fog contribution at vertex 2
							if (p2.z < mat._fog_near) {
								fog = mat._fog_near_val;
							} else if (p2.z > mat._fog_far) {
								fog = mat._fog_far_val;
							} else {
								fog = (p2.z-mat._fog_near) / (mat._fog_far - mat._fog_near);
								if (mat._fog_type == Material.SQUARE) fog *= fog;
								fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
							}
							t.v2.r = t.v2.g = t.v2.b = ((int)((float)255.0 * (1.0-fog))<<16);
							// Get fog contribution at vertex 3
							if (p3.z < mat._fog_near) {
								fog = mat._fog_near_val;
							} else if (p3.z > mat._fog_far) {
								fog = mat._fog_far_val;
							} else {
								fog = (p3.z-mat._fog_near) / (mat._fog_far - mat._fog_near);
								if (mat._fog_type == Material.SQUARE) fog *= fog;
								fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
							}
							t.v3.r = t.v3.g = t.v3.b = ((int)((float)255.0 * (1.0-fog))<<16);

							if (mat.SPEED >= Material.FAST) {
								fastGouraudTextureTriangle( t, mat);
							} else {
								gouraudTextureTriangle( t, mat);
							}

						} else {

							// Get fog contribution at vertex 1
							color = mat._color;
							// Get fog contribution at vertex 1
							if (p1.z < mat._fog_near) {
								fog = mat._fog_near_val;
							} else if (p1.z > mat._fog_far) {
								fog = mat._fog_far_val;
							} else {
								fog = (p1.z-mat._fog_near) / (mat._fog_far - mat._fog_near);
								if (mat._fog_type == Material.SQUARE) fog *= fog;
								fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
							}
							t.v1.r = ( (int)((float)((color>>16) & 255) * (1.0-fog)) +
									   (int)((float)(mat._fog_R) * fog )               ) << 16;
							t.v1.g = ( (int)((float)((color>>8 ) & 255) * (1.0-fog)) +
									   (int)((float)(mat._fog_G) * fog )               ) << 16;
							t.v1.b = ( (int)((float)( color      & 255) * (1.0-fog)) +
									   (int)((float)(mat._fog_B) * fog )               ) << 16;

							// Get fog contribution at vertex 2
							if (p2.z < mat._fog_near) {
								fog = mat._fog_near_val;
							} else if (p2.z > mat._fog_far) {
								fog = mat._fog_far_val;
							} else {
								fog = (p2.z-mat._fog_near) / (mat._fog_far - mat._fog_near);
								if (mat._fog_type == Material.SQUARE) fog *= fog;
								fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
							}
							t.v2.r = ( (int)((float)((color>>16) & 255) * (1.0-fog)) +
									   (int)((float)(mat._fog_R) * fog )               ) << 16;
							t.v2.g = ( (int)((float)((color>>8 ) & 255) * (1.0-fog)) +
									   (int)((float)(mat._fog_G) * fog )               ) << 16;
							t.v2.b = ( (int)((float)( color      & 255) * (1.0-fog)) +
									   (int)((float)(mat._fog_B) * fog )               ) << 16;
							// Get fog contribution at vertex 3
							if (p3.z < mat._fog_near) {
								fog = mat._fog_near_val;
							} else if (p3.z > mat._fog_far) {
								fog = mat._fog_far_val;
							} else {
								fog = (p3.z-mat._fog_near) / (mat._fog_far - mat._fog_near);
								if (mat._fog_type == Material.SQUARE) fog *= fog;
								fog = mat._fog_near_val + fog * (mat._fog_far_val - mat._fog_near_val);
							}
							t.v3.r = ( (int)((float)((color>>16) & 255) * (1.0-fog)) +
									   (int)((float)(mat._fog_R) * fog )               ) << 16;
							t.v3.g = ( (int)((float)((color>>8 ) & 255) * (1.0-fog)) +
									   (int)((float)(mat._fog_G) * fog )               ) << 16;
							t.v3.b = ( (int)((float)( color      & 255) * (1.0-fog)) +
									   (int)((float)(mat._fog_B) * fog )               ) << 16;

							gouraudTriangle( t, mat);
						}

//=========================

*/