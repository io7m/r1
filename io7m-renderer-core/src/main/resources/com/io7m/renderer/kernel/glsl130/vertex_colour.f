#version 130

in vec4 f_colour;

out vec4 out_frag_color;

void
main (void)
{
  out_frag_color = f_colour;
}
