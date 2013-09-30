#version 110
// Generated: 2013-09-30 12:46:42 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
// section: terms
// section: parameters
uniform mat3 m_normal;
uniform mat4 m_modelview;
uniform mat4 m_projection;
// section: inputs
attribute vec3 v_normal;
attribute vec3 v_position;
attribute vec2 v_uv;
// section: outputs
varying vec2 f_uv;
varying vec3 f_normal;
varying vec4 f_position;
// section: main
void
main (void)
{
  vec4 clip_position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  vec4 position = (m_modelview * vec4(v_position, 1.0));
  vec3 normal = (m_normal * v_normal);
  gl_Position = clip_position;
  f_position = position;
  f_uv = v_uv;
  f_normal = normal;
}
