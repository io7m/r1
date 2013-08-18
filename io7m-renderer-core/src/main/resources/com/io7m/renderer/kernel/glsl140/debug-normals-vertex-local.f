#version 140

in vec3 f_normal;

out vec4 out_frag_color;

vec4
normal_display(
  vec3 N)
{
  return vec4 (normalize ((N + 1.0) * 0.5), 1.0);
}

void
main (void)
{
  out_frag_color = normal_display (f_normal);
}
