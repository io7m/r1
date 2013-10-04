#version 100

precision highp float;
precision highp int;
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

uniform pt_com_io7m_renderer_SphericalLight_t light;
uniform sampler2D t_diffuse_0;
varying vec2 f_uv;
varying vec3 f_normal;
varying vec4 f_position;


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
p_com_io7m_renderer_SphericalLight_diffuse_color (pt_com_io7m_renderer_SphericalLight_t light, pt_com_io7m_renderer_SphericalLight_directions d)
{
  float factor = max(0.0, dot(d.stl, d.normal));
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
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
p_com_io7m_renderer_SphericalLight_diffuse_only (pt_com_io7m_renderer_SphericalLight_t light, vec3 n, vec3 p)
{
  pt_com_io7m_renderer_SphericalLight_directions d = p_com_io7m_renderer_SphericalLight_directions(light, p, n);
  float a = p_com_io7m_renderer_SphericalLight_attenuation(light, d.distance);
  vec3 c = p_com_io7m_renderer_SphericalLight_diffuse_color(light, d);
  {
    return (c * a);
  }
}


void
main (void)
{
  vec3 n = normalize(f_normal);
  vec3 light_term = p_com_io7m_renderer_SphericalLight_diffuse_only(light, n, f_position.xyz);
  vec3 albedo = texture2D(t_diffuse_0, f_uv).xyz;
  vec4 rgba = vec4((albedo * light_term), 1.0);
  gl_FragColor = rgba;
}
