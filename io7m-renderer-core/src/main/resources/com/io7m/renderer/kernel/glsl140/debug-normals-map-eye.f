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
  
  T = normalize (T - dot (T, N) * N);
  
  vec3 B  = cross (T, N);
  vec3 NM = texture2D (t_normal, f_uv).xyz;
  NM = 2.0 * (NM - 1.0);
  
  mat3 TBN = mat3 (T, B, N);
  return normalize (TBN * NM);
}

void
main (void)
{
  out_frag_color = vec4 (bumped_normal (), 1.0);
}
