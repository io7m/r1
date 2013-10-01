#version 100

precision highp float;
precision highp int;

uniform vec4 f_ccolour;



void
main (void)
{
  gl_FragColor = f_ccolour;
}
