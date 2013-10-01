#version 110


uniform mat4 m_modelview;
uniform mat4 m_projection;
attribute vec3 v_normal;
attribute vec4 v_tangent4;
attribute vec3 v_position;
attribute vec2 v_uv;
varying vec2 f_uv;
varying vec3 f_normal;
varying vec3 f_tangent;
varying vec3 f_bitangent;
varying vec4 f_position;

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
  f_position = position;
  f_uv = v_uv;
  f_normal = v_normal;
  f_tangent = tangent;
  f_bitangent = bitangent;
}
