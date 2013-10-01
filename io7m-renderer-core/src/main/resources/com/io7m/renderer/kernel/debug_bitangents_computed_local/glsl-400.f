#version 400


in vec3 f_bitangent;
out vec4 out_0;

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
  vec4 rgba = p_com_io7m_renderer_Normals_to_rgba(f_bitangent);
  out_0 = rgba;
}
