#version 100


uniform mat3 m_normal;
uniform mat4 m_modelview;
uniform mat4 m_projection;
attribute vec3 v_position;
attribute vec3 v_normal;
attribute vec4 v_tangent4;
varying vec3 f_bitangent;

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
  vec4 position = ((m_projection * m_modelview) * vec4(v_position, 1.0));
  vec3 bitangent = (m_normal * p_com_io7m_renderer_Normals_bitangent(v_normal, v_tangent4));
  gl_Position = position;
  f_bitangent = bitangent;
}
