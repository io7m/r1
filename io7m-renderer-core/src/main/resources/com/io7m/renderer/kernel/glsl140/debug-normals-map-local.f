#version 140

uniform sampler2D t_normal;

in vec2 f_uv;
in vec3 f_normal;
in vec3 f_tangent;

out vec4 out_frag_color;

vec3
bumped_normal (void)
{
  vec3 N = normalize (f_normal);
  vec3 T = normalize (f_tangent);
       T = normalize (T - (dot (T, N) * N));
  vec3 B = cross (T, N);
  vec3 M = texture2D (t_normal, f_uv).xyz;

  mat3 TBN = mat3 (T, B, N);
  return normalize (TBN * M);
}

void
main (void)
{
  vec3 N = bumped_normal ();
  out_frag_color = vec4 (N, 1.0);
}
