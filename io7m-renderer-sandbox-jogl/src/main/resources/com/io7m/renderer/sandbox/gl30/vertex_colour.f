#version 130

uniform float f_alpha;

in vec3 f_colour;

out vec4 out_frag_color;

void
main (void)
{
  out_frag_color = vec4(f_colour, f_alpha);
}