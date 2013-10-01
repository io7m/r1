#version 100

precision highp float;
precision highp int;

varying vec2 f_uv;



void
main (void)
{
  vec4 rgba = vec4(f_uv, 0.0, 1.0);
  gl_FragColor = rgba;
}
