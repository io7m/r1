#version 130


uniform mat3 m_normal;
uniform mat4 m_modelview;
uniform mat4 m_projection;
in vec3 v_normal;
in vec3 v_position;
out vec3 f_normal;
out vec4 f_position;


void
main (void)
{
  vec4 clip_position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  vec4 position = (m_modelview * vec4(v_position, 1.0));
  vec3 normal = (m_normal * v_normal);
  gl_Position = clip_position;
  f_position = position;
  f_normal = normal;
}
