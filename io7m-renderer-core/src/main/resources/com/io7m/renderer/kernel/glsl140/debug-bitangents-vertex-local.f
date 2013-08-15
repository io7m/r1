#version 140

in vec3 f_normal;
in vec3 f_tangent;

out vec4 out_frag_color;

void
main (void)
{
  vec3 N = normalize (f_normal);
  vec3 T = normalize (f_tangent);
  T = normalize (T - (dot (T, N) * N));
  vec3 B = cross (T, N);
  B = (B + 1.0) * 0.5;
  out_frag_color = vec4(B, 1.0);
}
