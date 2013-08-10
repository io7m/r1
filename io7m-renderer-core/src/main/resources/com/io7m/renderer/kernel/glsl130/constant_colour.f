#version 130

uniform vec4 f_ccolour;

out vec4 out_frag_color;

void
main (void)
{
  out_frag_color = f_ccolour;
}
