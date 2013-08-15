#version 140

in vec3 f_tangent;

out vec4 out_frag_color;

void
main (void)
{
  vec3 T = normalize (f_tangent);
  T = (T + 1.0) * 0.5;
  out_frag_color = vec4(T, 1);
}
