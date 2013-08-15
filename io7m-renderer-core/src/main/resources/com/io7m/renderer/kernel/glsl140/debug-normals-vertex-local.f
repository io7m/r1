#version 140

in vec3 f_normal;

out vec4 out_frag_color;

void
main (void)
{
  vec3 N = normalize ((f_normal + 1.0) * 0.5);
  out_frag_color = vec4(N, 1);
}
