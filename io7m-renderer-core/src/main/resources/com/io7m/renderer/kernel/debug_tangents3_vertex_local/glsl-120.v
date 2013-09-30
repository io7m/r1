#version 120
// Generated: 2013-09-30 12:47:06 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
// section: terms
// section: parameters
uniform mat4 m_modelview;
uniform mat4 m_projection;
// section: inputs
attribute vec3 v_position;
attribute vec3 v_tangent3;
// section: outputs
varying vec3 f_tangent;
// section: main
void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  gl_Position = position;
  f_tangent = v_tangent3;
}
