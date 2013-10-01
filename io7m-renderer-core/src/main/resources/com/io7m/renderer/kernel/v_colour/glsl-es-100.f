#version 100

precision highp float;
precision highp int;

varying vec4 f_colour;



void
main (void)
{
  gl_FragColor = f_colour;
}
