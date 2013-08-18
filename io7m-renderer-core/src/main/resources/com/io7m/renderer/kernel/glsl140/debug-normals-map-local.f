#version 140

uniform sampler2D t_normal;

in vec2 f_uv;
in vec3 f_normal;
in vec3 f_tangent;
in vec3 f_bitangent;

out vec4 out_frag_color;

vec3
normal_texture_unpack(
  in sampler2D texture,
  in vec2 uv)
{
  vec3 rgb = texture2D (texture, uv).xyz;
  return (rgb * 2.0) - 1.0;
}

vec4
normal_display(
  in vec3 N)
{
  return vec4 (normalize ((N + 1.0) * 0.5), 1.0);
}

vec3
normal_bumped(
  in vec3 M,
  in vec3 N,
  in vec3 T,
  in vec3 B)
{
  mat3 TBN = mat3 (T, B, N);
  return normalize (TBN * M);
}

void
main (void)
{
  vec3 T = normal_texture_unpack (t_normal, f_uv);
  vec3 N = normal_bumped(
    T,
    normalize (f_normal),
    normalize (f_tangent),
    normalize (f_bitangent));

  out_frag_color = normal_display (N);
}
