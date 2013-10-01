#version 100


uniform mat4 m_modelview;
uniform mat4 m_projection;
attribute vec3 v_position;
attribute vec3 v_normal;
attribute vec3 v_tangent3;
attribute vec3 v_bitangent;
attribute vec2 v_uv;
varying vec2 f_uv;
varying vec3 f_normal;
varying vec3 f_tangent;
varying vec3 f_bitangent;


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
