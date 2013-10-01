#version 120


uniform mat4 m_modelview;
uniform mat4 m_projection;
attribute vec3 v_position;
attribute vec3 v_tangent3;
varying vec3 f_tangent;


void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  gl_Position = position;
  f_tangent = v_tangent3;
}
