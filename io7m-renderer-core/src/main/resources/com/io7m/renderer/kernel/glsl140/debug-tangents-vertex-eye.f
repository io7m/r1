#version 140

in vec3 f_tangent;

out vec4 out_frag_color;

void
main (void)
{
  out_frag_color = vec4(f_tangent, 1);
}
