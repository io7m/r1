#version 400


uniform mat3 m_normal;
uniform mat4 m_modelview;
uniform mat4 m_projection;
in vec3 v_position;
in vec3 v_bitangent;
out vec3 f_bitangent;


void
main (void)
{
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  vec3 bitangent = (m_normal * v_bitangent);
  gl_Position = position;
  f_bitangent = bitangent;
}