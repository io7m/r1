precision highp float;
precision highp int;

in  vec3 v_position;
out vec2 f_position_uv;

void
main (void)
{
  vec4 pl_position_clip = vec4 (v_position, 1.0);
  gl_Position           = pl_position_clip;
  f_position_uv         = (pl_position_clip.xy + 1.0) * 0.5;
}

