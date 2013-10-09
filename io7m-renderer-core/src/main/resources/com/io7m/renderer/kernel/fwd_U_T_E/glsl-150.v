#version 150


uniform mat3 m_normal;
uniform mat4 m_modelview;
uniform mat4 m_projection;
in vec3 v_position;
in vec3 v_normal;
in vec2 v_uv;
out vec4 f_position;
out vec3 f_normal;
out vec2 f_uv;


void
main (void)
{
  vec4 clip_position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  vec4 position = (m_modelview * vec4(v_position, 1.0));
  vec3 normal = (m_normal * v_normal);
  gl_Position = clip_position;
  f_position = position;
  f_normal = normal;
  f_uv = v_uv;
}
