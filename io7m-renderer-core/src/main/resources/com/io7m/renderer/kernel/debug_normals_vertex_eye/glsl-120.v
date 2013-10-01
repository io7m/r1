#version 120


uniform mat3 m_normal;
uniform mat4 m_modelview;
uniform mat4 m_projection;
attribute vec3 v_position;
attribute vec3 v_normal;
varying vec3 f_normal;


void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  vec3 normal = (m_normal * v_normal);
  gl_Position = position;
  f_normal = normal;
}