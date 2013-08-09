#version 120

uniform float f_alpha;

in vec3 f_colour;

void
main (void)
{
  gl_FragColor = vec4(f_colour, f_alpha);
}
