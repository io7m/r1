#version 400
// Generated: 2013-09-30 14:44:01 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
struct struct_com_io7m_renderer_DirectionalLight_t {
  vec3 color;
  vec3 direction;
  float intensity;
};
struct struct_com_io7m_renderer_DirectionalLight_directions {
  vec3 otl;
  vec3 normal;
  vec3 stl;
  vec3 reflection;
};
// section: terms
vec3
com_io7m_renderer_DirectionalLight_diffuse_color (struct_com_io7m_renderer_DirectionalLight_t light, struct_com_io7m_renderer_DirectionalLight_directions d)
{
  float factor = max(0.0, dot(d.stl, d.normal));
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
  }
}

struct_com_io7m_renderer_DirectionalLight_directions
com_io7m_renderer_DirectionalLight_directions (struct_com_io7m_renderer_DirectionalLight_t light, vec3 p, vec3 n)
{
  vec3 otl = normalize(p);
  {
    vec3 _tmp_1 = otl;
    vec3 _tmp_2 = n;
    vec3 _tmp_3 = normalize((-light.direction));
    vec3 _tmp_4 = reflect(otl, n);
    return struct_com_io7m_renderer_DirectionalLight_directions(_tmp_1, _tmp_2, _tmp_3, _tmp_4);
  }
}

vec3
com_io7m_renderer_DirectionalLight_diffuse_only (struct_com_io7m_renderer_DirectionalLight_t light, vec3 n)
{
  struct_com_io7m_renderer_DirectionalLight_directions d = com_io7m_renderer_DirectionalLight_directions(light, vec3(0.0, 0.0, 0.0), n);
  vec3 c = com_io7m_renderer_DirectionalLight_diffuse_color(light, d);
  {
    return c;
  }
}

// section: parameters
uniform struct_com_io7m_renderer_DirectionalLight_t light;
uniform sampler2D t_diffuse_0;
// section: inputs
in vec2 f_uv;
in vec3 f_normal;
// section: outputs
out vec4 out_0;
// section: main
void
main (void)
{
  vec3 n = normalize(f_normal);
  vec3 light_term = com_io7m_renderer_DirectionalLight_diffuse_only(light, n);
  vec3 albedo = texture(t_diffuse_0, f_uv).xyz;
  vec4 rgba = vec4((albedo * light_term), 1.0);
  out_0 = rgba;
}
