#version 120

attribute vec4 v_colour;
attribute vec3 v_position;
attribute vec3 v_normal;
attribute vec2 v_uv;

uniform mat4 m_modelview;
uniform mat4 m_projection;
uniform mat3 m_normal;

varying vec2 f_uv;
varying vec3 f_normal;
varying vec3 f_normal_tangent;
varying vec4 f_colour;

void
main()
{
  gl_Position      = m_projection * m_modelview * vec4(v_position, 1.0);
  f_uv             = v_uv;
  f_normal         = m_normal * v_normal;
  f_normal_tangent = v_normal;
  f_colour         = v_colour;
}
