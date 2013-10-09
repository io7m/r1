#version 110

struct pt_com_io7m_renderer_Materials_diffuse {
  vec4 colour;
  float mix;
};
struct pt_com_io7m_renderer_DirectionalLight_directions {
  vec3 ots;
  vec3 normal;
  vec3 stl;
  vec3 reflection;
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
struct pt_com_io7m_renderer_DirectionalLight_t {
  vec3 color;
  vec3 direction;
  float intensity;
};

uniform pt_com_io7m_renderer_Materials_t material;
uniform pt_com_io7m_renderer_DirectionalLight_t light;
uniform sampler2D t_diffuse_0;
uniform sampler2D t_specular;
varying vec4 f_position;
varying vec3 f_normal;
varying vec2 f_uv;


vec3
p_com_io7m_renderer_DirectionalLight_diffuse_color (pt_com_io7m_renderer_DirectionalLight_t light, pt_com_io7m_renderer_DirectionalLight_directions d)
{
  float factor = max(0.0, dot(d.stl, d.normal));
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
  }
}

vec4
p_com_io7m_renderer_Diffuse_diffuse (sampler2D t, vec2 u, pt_com_io7m_renderer_Materials_diffuse m)
{
  vec4 tc = texture2D(t, u);
  vec3 c = mix(m.colour.xyz, tc.xyz, m.mix);
  {
    return vec4(c, 1.0);
  }
}

pt_com_io7m_renderer_DirectionalLight_directions
p_com_io7m_renderer_DirectionalLight_directions (pt_com_io7m_renderer_DirectionalLight_t light, vec3 p, vec3 n)
{
  vec3 ots = normalize(p);
  {
    vec3 _tmp_1 = ots;
    vec3 _tmp_2 = n;
    vec3 _tmp_3 = normalize((-light.direction));
    vec3 _tmp_4 = reflect(ots, n);
    return pt_com_io7m_renderer_DirectionalLight_directions(_tmp_1, _tmp_2, _tmp_3, _tmp_4);
  }
}

vec3
p_com_io7m_renderer_DirectionalLight_specular_color (pt_com_io7m_renderer_DirectionalLight_t light, pt_com_io7m_renderer_DirectionalLight_directions d, pt_com_io7m_renderer_Materials_specular s)
{
  float factor = pow(max(0.0, dot(d.reflection, d.stl)), s.exponent);
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return (color * s.intensity);
  }
}

vec3
p_com_io7m_renderer_DirectionalLight_diffuse_specular (pt_com_io7m_renderer_DirectionalLight_t light, vec3 n, vec3 p, pt_com_io7m_renderer_Materials_t material)
{
  pt_com_io7m_renderer_DirectionalLight_directions d = p_com_io7m_renderer_DirectionalLight_directions(light, p, n);
  vec3 dc = p_com_io7m_renderer_DirectionalLight_diffuse_color(light, d);
  vec3 sc = p_com_io7m_renderer_DirectionalLight_specular_color(light, d, material.specular);
  {
    return (dc + sc);
  }
}

pt_com_io7m_renderer_Materials_specular
_tmp_10 (void)
{
  float _tmp_8 = material.specular.exponent;
  float _tmp_9 = texture2D(t_specular, f_uv).x;
  return pt_com_io7m_renderer_Materials_specular(_tmp_8, _tmp_9);
}

pt_com_io7m_renderer_Materials_t
_tmp_11 (void)
{
  pt_com_io7m_renderer_Materials_diffuse _tmp_5 = material.diffuse;
  pt_com_io7m_renderer_Materials_environment _tmp_6 = material.environment;
  pt_com_io7m_renderer_Materials_specular _tmp_7 = _tmp_10();
  return pt_com_io7m_renderer_Materials_t(_tmp_5, _tmp_7, _tmp_6);
}


void
main (void)
{
  vec3 n = normalize(f_normal);
  pt_com_io7m_renderer_Materials_t m = _tmp_11();
  vec3 light_term = p_com_io7m_renderer_DirectionalLight_diffuse_specular(light, n, f_position.xyz, m);
  vec3 albedo = p_com_io7m_renderer_Diffuse_diffuse(t_diffuse_0, f_uv, m.diffuse).xyz;
  vec4 rgba = vec4((albedo * light_term), 1.0);
  gl_FragColor = rgba;
}
