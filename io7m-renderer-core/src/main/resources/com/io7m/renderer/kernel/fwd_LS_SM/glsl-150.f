#version 150

struct pt_com_io7m_renderer_Materials_t {
  float specular_exponent;
  float specular_intensity;
};
struct pt_com_io7m_renderer_SphericalLight_t {
  vec3 color;
  vec3 position;
  float intensity;
  float radius;
  float falloff;
};
struct pt_com_io7m_renderer_SphericalLight_directions {
  vec3 otl;
  vec3 normal;
  vec3 stl;
  float distance;
  vec3 reflection;
};

uniform pt_com_io7m_renderer_Materials_t material;
uniform pt_com_io7m_renderer_SphericalLight_t light;
uniform sampler2D t_specular;
uniform vec4 f_diffuse;
in vec2 f_uv;
in vec3 f_normal;
in vec4 f_position;
out vec4 out_0;

float
p_com_io7m_renderer_SphericalLight_attenuation (pt_com_io7m_renderer_SphericalLight_t light, float distance)
{
  float nd = (0.0 - distance);
  float inv_radius = (1.0 / light.radius);
  float linear = ((nd * inv_radius) + 1.0);
  float exponential = clamp(pow(linear, light.falloff), 0.0, 1.0);
  {
    return exponential;
  }
}

vec3
p_com_io7m_renderer_SphericalLight_specular_color (pt_com_io7m_renderer_SphericalLight_t light, pt_com_io7m_renderer_SphericalLight_directions d, pt_com_io7m_renderer_Materials_t material)
{
  float factor = pow(max(0.0, dot(d.reflection, d.stl)), material.specular_exponent);
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return (color * material.specular_intensity);
  }
}

pt_com_io7m_renderer_SphericalLight_directions
p_com_io7m_renderer_SphericalLight_directions (pt_com_io7m_renderer_SphericalLight_t light, vec3 p, vec3 n)
{
  vec3 position_diff = (p - light.position);
  vec3 otl = normalize(p);
  {
    vec3 _tmp_1 = otl;
    vec3 _tmp_2 = n;
    vec3 _tmp_3 = normalize((-position_diff));
    float _tmp_4 = length(position_diff);
    vec3 _tmp_5 = reflect(otl, n);
    return pt_com_io7m_renderer_SphericalLight_directions(_tmp_1, _tmp_2, _tmp_3, _tmp_4, _tmp_5);
  }
}

vec3
p_com_io7m_renderer_SphericalLight_diffuse_color (pt_com_io7m_renderer_SphericalLight_t light, pt_com_io7m_renderer_SphericalLight_directions d)
{
  float factor = max(0.0, dot(d.stl, d.normal));
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
  }
}

vec3
p_com_io7m_renderer_SphericalLight_diffuse_specular (pt_com_io7m_renderer_SphericalLight_t light, vec3 n, vec3 p, pt_com_io7m_renderer_Materials_t material)
{
  pt_com_io7m_renderer_SphericalLight_directions d = p_com_io7m_renderer_SphericalLight_directions(light, p, n);
  float a = p_com_io7m_renderer_SphericalLight_attenuation(light, d.distance);
  vec3 dc = p_com_io7m_renderer_SphericalLight_diffuse_color(light, d);
  vec3 sc = p_com_io7m_renderer_SphericalLight_specular_color(light, d, material);
  {
    return ((dc + sc) * a);
  }
}

pt_com_io7m_renderer_Materials_t
_tmp_8 (void)
{
  float _tmp_6 = material.specular_exponent;
  float _tmp_7 = texture(t_specular, f_uv).x;
  return pt_com_io7m_renderer_Materials_t(_tmp_6, _tmp_7);
}


void
main (void)
{
  vec3 n = normalize(f_normal);
  pt_com_io7m_renderer_Materials_t m = _tmp_8();
  vec3 light_term = p_com_io7m_renderer_SphericalLight_diffuse_specular(light, n, f_position.xyz, m);
  vec3 albedo = f_diffuse.xyz;
  vec4 rgba = vec4((albedo * light_term), 1.0);
  out_0 = rgba;
}
