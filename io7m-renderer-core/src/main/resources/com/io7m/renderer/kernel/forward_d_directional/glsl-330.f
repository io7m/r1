#version 330

struct pt_com_io7m_renderer_DirectionalLight_t {
  vec3 color;
  vec3 direction;
  float intensity;
};
struct pt_com_io7m_renderer_DirectionalLight_directions {
  vec3 otl;
  vec3 normal;
  vec3 stl;
  vec3 reflection;
};

uniform pt_com_io7m_renderer_DirectionalLight_t light;
uniform sampler2D t_diffuse_0;
in vec2 f_uv;
in vec3 f_normal;
out vec4 out_0;

vec3
p_com_io7m_renderer_DirectionalLight_diffuse_color (pt_com_io7m_renderer_DirectionalLight_t light, pt_com_io7m_renderer_DirectionalLight_directions d)
{
  float factor = max(0.0, dot(d.stl, d.normal));
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
  }
}

pt_com_io7m_renderer_DirectionalLight_directions
p_com_io7m_renderer_DirectionalLight_directions (pt_com_io7m_renderer_DirectionalLight_t light, vec3 p, vec3 n)
{
  vec3 otl = normalize(p);
  {
    vec3 _tmp_1 = otl;
    vec3 _tmp_2 = n;
    vec3 _tmp_3 = normalize((-light.direction));
    vec3 _tmp_4 = reflect(otl, n);
    return pt_com_io7m_renderer_DirectionalLight_directions(_tmp_1, _tmp_2, _tmp_3, _tmp_4);
  }
}

vec3
p_com_io7m_renderer_DirectionalLight_diffuse_only (pt_com_io7m_renderer_DirectionalLight_t light, vec3 n)
{
  pt_com_io7m_renderer_DirectionalLight_directions d = p_com_io7m_renderer_DirectionalLight_directions(light, vec3(0.0, 0.0, 0.0), n);
  vec3 c = p_com_io7m_renderer_DirectionalLight_diffuse_color(light, d);
  {
    return c;
  }
}


void
main (void)
{
  vec3 n = normalize(f_normal);
  vec3 light_term = p_com_io7m_renderer_DirectionalLight_diffuse_only(light, n);
  vec3 albedo = texture(t_diffuse_0, f_uv).xyz;
  vec4 rgba = vec4((albedo * light_term), 1.0);
  out_0 = rgba;
}
