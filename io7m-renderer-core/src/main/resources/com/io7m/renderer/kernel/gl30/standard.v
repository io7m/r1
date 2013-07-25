#version 130

in vec3 v_colour;
in vec3 v_position;
in vec3 v_normal;
in vec2 v_uv;

uniform mat4 m_modelview;
uniform mat4 m_projection;
uniform mat3 m_normal;

out vec2 f_uv;
out vec3 f_normal;
out vec3 f_normal_tangent;
out vec3 f_colour;

void
main()
{
  gl_Position      = m_projection * m_modelview * vec4(v_position, 1.0);
  f_uv             = v_uv;
  f_normal         = m_normal * v_normal;
  f_normal_tangent = v_normal;
  f_colour         = v_colour;
}
