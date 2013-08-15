#version 140

in vec3 v_position;
in vec3 v_tangent;

uniform mat4 m_modelview;
uniform mat4 m_projection;
uniform mat3 m_normal;

out vec3 f_tangent;

void
main()
{
  gl_Position = m_projection * m_modelview * vec4(v_position, 1.0);
  f_tangent   = m_normal * v_tangent;
}
