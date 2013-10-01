#version 100


uniform mat4 m_modelview;
uniform mat4 m_projection;
attribute vec3 v_position;
attribute vec2 v_uv;
varying vec2 f_uv;


void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  gl_Position = position;
  f_uv = v_uv;
}
