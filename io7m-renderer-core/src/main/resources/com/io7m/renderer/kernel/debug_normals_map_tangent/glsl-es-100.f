#version 100

precision highp float;
precision highp int;

uniform sampler2D t_normal;
varying vec2 f_uv;



void
main (void)
{
  vec4 rgba = vec4(texture2D(t_normal, f_uv).xyz, 1.0);
  gl_FragColor = rgba;
}
