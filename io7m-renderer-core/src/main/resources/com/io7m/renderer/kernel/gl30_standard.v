#version 130

in vec3 v_position;
in vec3 v_normal;
in vec2 v_uv;

uniform mat4 m_modelview;
uniform mat4 m_projection;
uniform mat3 m_normal;

out vec2 f_uv;
out vec3 f_normal;

void
main()
{
  gl_Position = m_projection * m_modelview * vec4(v_position, 1.0);
  f_uv        = v_uv;
  f_normal    = m_normal * v_normal;
}