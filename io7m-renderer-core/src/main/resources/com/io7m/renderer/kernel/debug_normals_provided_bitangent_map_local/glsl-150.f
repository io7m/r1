#version 150


uniform sampler2D t_normal;
in vec3 f_normal;
in vec3 f_tangent;
in vec3 f_bitangent;
in vec2 f_uv;
out vec4 out_0;

vec3
p_com_io7m_renderer_Normals_unpack (sampler2D map, vec2 uv)
{
  vec3 rgb = texture(map, uv).xyz;
  {
    return ((rgb * 2.0) + -1.0);
  }
}

vec3
p_com_io7m_renderer_Normals_transform (vec3 m, vec3 t, vec3 b, vec3 n)
{
  mat3 mat = mat3(t, b, n);
  {
    return normalize((mat * m));
  }
}

vec4
p_com_io7m_renderer_Normals_to_rgba (vec3 n)
{
  vec3 xyz = ((n + 1.0) * 0.5);
  {
    return vec4(normalize(xyz), 1.0);
  }
}


void
main (void)
{
  vec3 m = p_com_io7m_renderer_Normals_unpack(t_normal, f_uv);
  vec3 n = normalize(f_normal);
  vec3 t = normalize(f_tangent);
  vec3 b = normalize(f_bitangent);
  vec3 r = p_com_io7m_renderer_Normals_transform(m, t, b, n);
  vec4 rgba = p_com_io7m_renderer_Normals_to_rgba(r);
  out_0 = rgba;
}
