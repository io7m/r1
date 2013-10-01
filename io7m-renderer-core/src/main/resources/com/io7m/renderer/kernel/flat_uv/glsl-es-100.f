#version 100

precision highp float;
precision highp int;

uniform sampler2D t_diffuse_0;
varying vec2 f_uv;



void
main (void)
{
  vec4 rgba = texture2D(t_diffuse_0, f_uv);
  gl_FragColor = rgba;
}
