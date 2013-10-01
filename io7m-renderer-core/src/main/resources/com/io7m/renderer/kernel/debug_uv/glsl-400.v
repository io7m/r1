#version 400


uniform mat4 m_modelview;
uniform mat4 m_projection;
in vec3 v_position;
in vec2 v_uv;
out vec2 f_uv;


void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  gl_Position = position;
  f_uv = v_uv;
}
