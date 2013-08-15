#version 140

in vec3 v_position;
in vec3 v_normal;
in vec3 v_tangent;
in vec2 v_uv;

uniform mat4 m_modelview;
uniform mat4 m_projection;
uniform mat3 m_normal;

out vec2 f_uv;
out vec3 f_normal;
out vec3 f_tangent;
out vec4 f_position;

void
main()
{
  gl_Position = m_projection * m_modelview * vec4(v_position, 1.0);
  f_uv        = v_uv;
  f_normal    = m_normal * v_normal;
  f_tangent   = m_normal * v_tangent;
  f_position  = m_modelview * vec4(v_position, 1.0);
}
