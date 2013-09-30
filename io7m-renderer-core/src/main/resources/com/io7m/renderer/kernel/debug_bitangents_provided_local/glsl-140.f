#version 140
// Generated: 2013-09-30 14:44:17 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
// section: terms
vec4
com_io7m_renderer_Normals_to_rgba (vec3 n)
{
  vec3 xyz = ((n + 1.0) * 0.5);
  {
    return vec4(normalize(xyz), 1.0);
  }
}

// section: parameters
// section: inputs
in vec3 f_bitangent;
// section: outputs
out vec4 out_0;
// section: main
void
main (void)
{
  vec4 rgba = com_io7m_renderer_Normals_to_rgba(f_bitangent);
  out_0 = rgba;
}
