#version 300

precision highp float;
precision highp int;

in vec2 f_uv;
out vec4 out_0;


void
main (void)
{
  vec4 rgba = vec4(f_uv, 0.0, 1.0);
  out_0 = rgba;
}
