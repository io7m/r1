#version 420


uniform mat4 m_modelview;
uniform mat4 m_projection;
in vec3 v_position;
in vec4 v_colour;
out vec4 f_colour;


void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  gl_Position = position;
  f_colour = v_colour;
}