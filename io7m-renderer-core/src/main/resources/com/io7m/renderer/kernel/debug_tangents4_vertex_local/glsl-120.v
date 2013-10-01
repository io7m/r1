#version 120


uniform mat4 m_modelview;
uniform mat4 m_projection;
attribute vec3 v_position;
attribute vec4 v_tangent4;
varying vec3 f_tangent;


void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  vec3 tangent = v_tangent4.xyz;
  gl_Position = position;
  f_tangent = tangent;
}