#version 140

in vec3 v_position;
in vec4 v_colour;

uniform mat4 m_modelview;
uniform mat4 m_projection;

out vec4 f_colour;

void
main()
{
  gl_Position = m_projection * m_modelview * vec4(v_position, 1.0);
  f_colour    = v_colour;
}
