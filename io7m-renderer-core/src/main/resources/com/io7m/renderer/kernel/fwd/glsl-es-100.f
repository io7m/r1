#version 100

precision highp float;
precision highp int;

uniform vec4 f_diffuse;



void
main (void)
{
  gl_FragColor = f_diffuse;
}
