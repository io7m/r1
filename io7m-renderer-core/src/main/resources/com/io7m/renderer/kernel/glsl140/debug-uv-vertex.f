#version 140

in vec2 f_uv;

out vec4 out_frag_color;

void
main (void)
{
  out_frag_color = vec4(f_uv, 0, 1);
}
