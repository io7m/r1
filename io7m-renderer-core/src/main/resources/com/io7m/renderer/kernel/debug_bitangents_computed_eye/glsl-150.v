#version 150
// Generated: 2013-09-30 14:44:13 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
// section: terms
vec3
com_io7m_renderer_Normals_bitangent (vec3 n, vec4 t)
{
  vec3 p = cross(n, t.xyz);
  {
    return (p * t.w);
  }
}

// section: parameters
uniform mat3 m_normal;
uniform mat4 m_modelview;
uniform mat4 m_projection;
// section: inputs
in vec3 v_position;
in vec3 v_normal;
in vec4 v_tangent4;
// section: outputs
out vec3 f_bitangent;
// section: main
void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  vec3 bitangent = (m_normal * com_io7m_renderer_Normals_bitangent(v_normal, v_tangent4));
  gl_Position = position;
  f_bitangent = bitangent;
}
