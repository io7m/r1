#version 330


uniform mat4 m_modelview;
uniform mat4 m_projection;
in vec3 v_normal;
in vec4 v_tangent4;
in vec3 v_position;
in vec2 v_uv;
out vec4 f_position;
out vec2 f_uv;
out vec3 f_normal;
out vec3 f_tangent;
out vec3 f_bitangent;

vec3
p_com_io7m_renderer_Normals_bitangent (vec3 n, vec4 t)
{
  vec3 p = cross(n, t.xyz);
  {
    return (p * t.w);
  }
}


void
main (void)
{
  vec4 clip_position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  vec4 position = (m_modelview * vec4(v_position, 1.0));
  vec3 tangent = v_tangent4.xyz;
  vec3 bitangent = p_com_io7m_renderer_Normals_bitangent(v_normal, v_tangent4);
  gl_Position = clip_position;
  f_uv = v_uv;
  f_normal = v_normal;
  f_tangent = tangent;
  f_bitangent = bitangent;
  f_position = position;
}
