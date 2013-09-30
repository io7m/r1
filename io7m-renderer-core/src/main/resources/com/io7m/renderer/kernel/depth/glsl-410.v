#version 410
// Generated: 2013-09-30 14:43:58 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
// section: terms
// section: parameters
uniform mat4 m_modelview;
uniform mat4 m_projection;
// section: inputs
in vec3 v_position;
// section: outputs
// section: main
void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  gl_Position = position;
}
