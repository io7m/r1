#version 400
// Generated: 2013-09-30 12:47:05 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
// section: terms
// section: parameters
uniform mat3 m_normal;
uniform mat4 m_modelview;
uniform mat4 m_projection;
// section: inputs
in vec3 v_position;
in vec3 v_tangent3;
// section: outputs
out vec3 f_tangent;
// section: main
void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  vec3 tangent = (m_normal * v_tangent3);
  gl_Position = position;
  f_tangent = tangent;
}
