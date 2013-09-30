#version 430
// Generated: 2013-09-30 12:47:00 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
// section: terms
// section: parameters
uniform mat4 m_modelview;
uniform mat4 m_projection;
// section: inputs
in vec3 v_position;
in vec3 v_normal;
in vec3 v_tangent3;
in vec3 v_bitangent;
in vec2 v_uv;
// section: outputs
out vec2 f_uv;
out vec3 f_normal;
out vec3 f_tangent;
out vec3 f_bitangent;
// section: main
void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  gl_Position = position;
  f_normal = v_normal;
  f_tangent = v_tangent3;
  f_bitangent = v_bitangent;
  f_uv = v_uv;
}
