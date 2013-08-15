#version 140

in vec3 v_position;

uniform mat4 m_modelview;
uniform mat4 m_projection;

void
main()
{
  gl_Position = m_projection * m_modelview * vec4(v_position, 1.0);
}
