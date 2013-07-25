#version 110

uniform float f_alpha;

varying vec3 f_colour;

void
main (void)
{
  gl_FragColor = vec4(f_colour, f_alpha);
}