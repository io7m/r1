#version 130


uniform mat3 m_normal;
uniform mat4 m_modelview;
uniform mat4 m_projection;
in vec3 v_normal;
in vec3 v_position;
in vec2 v_uv;
out vec2 f_uv;
out vec3 f_normal;


void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  vec3 normal = (m_normal * v_normal);
  gl_Position = position;
  f_uv = v_uv;
  f_normal = normal;
}
