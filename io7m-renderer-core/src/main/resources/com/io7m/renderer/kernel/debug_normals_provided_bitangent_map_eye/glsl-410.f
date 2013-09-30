#version 410
// Generated: 2013-09-30 12:47:00 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
// section: terms
vec3
com_io7m_renderer_Normals_unpack (sampler2D map, vec2 uv)
{
  vec3 rgb = texture(map, uv).xyz;
  {
    return ((rgb * 2.0) + -1.0);
  }
}

vec3
com_io7m_renderer_Normals_transform (vec3 m, vec3 t, vec3 b, vec3 n)
{
  mat3 mat = mat3(t, b, n);
  {
    return normalize((mat * m));
  }
}

vec4
com_io7m_renderer_Normals_to_rgba (vec3 n)
{
  vec3 xyz = ((n + 1.0) * 0.5);
  {
    return vec4(normalize(xyz), 1.0);
  }
}

// section: parameters
uniform mat3 m_normal;
uniform sampler2D t_normal;
// section: inputs
in vec3 f_normal;
in vec3 f_tangent;
in vec3 f_bitangent;
in vec2 f_uv;
// section: outputs
out vec4 out_0;
// section: main
void
main (void)
{
  vec3 m = com_io7m_renderer_Normals_unpack(t_normal, f_uv);
  vec3 n = normalize(f_normal);
  vec3 t = normalize(f_tangent);
  vec3 b = normalize(f_bitangent);
  vec3 r = com_io7m_renderer_Normals_transform(m, t, b, n);
  vec3 e = (m_normal * r);
  vec4 rgba = com_io7m_renderer_Normals_to_rgba(e);
  out_0 = rgba;
}
