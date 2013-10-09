#version 130

struct pt_com_io7m_renderer_Materials_diffuse {
  vec4 colour;
  float mix;
};
struct pt_com_io7m_renderer_Materials_specular {
  float exponent;
  float intensity;
};
struct pt_com_io7m_renderer_Materials_environment {
  float mix;
};
struct pt_com_io7m_renderer_Materials_t {
  pt_com_io7m_renderer_Materials_diffuse diffuse;
  pt_com_io7m_renderer_Materials_specular specular;
  pt_com_io7m_renderer_Materials_environment environment;
};

uniform pt_com_io7m_renderer_Materials_t material;
uniform sampler2D t_diffuse_0;
in vec2 f_uv;
out vec4 out_0;

vec4
p_com_io7m_renderer_Diffuse_diffuse (sampler2D t, vec2 u, pt_com_io7m_renderer_Materials_diffuse m)
{
  vec4 tc = texture(t, u);
  vec3 c = mix(m.colour.xyz, tc.xyz, m.mix);
  {
    return vec4(c, 1.0);
  }
}


void
main (void)
{
  vec4 rgba = p_com_io7m_renderer_Diffuse_diffuse(t_diffuse_0, f_uv, material.diffuse);
  out_0 = rgba;
}
