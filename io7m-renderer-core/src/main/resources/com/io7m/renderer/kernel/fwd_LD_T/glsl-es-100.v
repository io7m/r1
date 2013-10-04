#version 100


uniform mat3 m_normal;
uniform mat4 m_modelview;
uniform mat4 m_projection;
attribute vec3 v_normal;
attribute vec3 v_position;
attribute vec2 v_uv;
varying vec2 f_uv;
varying vec3 f_normal;


void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  vec3 normal = (m_normal * v_normal);
  gl_Position = position;
  f_uv = v_uv;
  f_normal = normal;
}
