#version 120
// Generated: 2013-09-30 12:46:44 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
struct struct_com_io7m_renderer_SphericalLight_t {
  vec3 color;
  vec3 position;
  float intensity;
  float radius;
  float falloff;
};
struct struct_com_io7m_renderer_SphericalLight_directions {
  vec3 otl;
  vec3 normal;
  vec3 stl;
  float distance;
  vec3 reflection;
};
// section: terms
float
com_io7m_renderer_SphericalLight_attenuation (struct_com_io7m_renderer_SphericalLight_t light, float distance)
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
com_io7m_renderer_SphericalLight_specular_color (struct_com_io7m_renderer_SphericalLight_t light, struct_com_io7m_renderer_SphericalLight_directions d, float s)
{
  float factor = pow(max(0.0, dot(d.reflection, d.stl)), s);
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
  }
}

struct_com_io7m_renderer_SphericalLight_directions
com_io7m_renderer_SphericalLight_directions (struct_com_io7m_renderer_SphericalLight_t light, vec3 p, vec3 n)
{
  vec3 position_diff = (p - light.position);
  vec3 otl = normalize(p);
  {
    vec3 _tmp_1 = otl;
    vec3 _tmp_2 = n;
    vec3 _tmp_3 = normalize((-position_diff));
    float _tmp_4 = length(position_diff);
    vec3 _tmp_5 = reflect(otl, n);
    return struct_com_io7m_renderer_SphericalLight_directions(_tmp_1, _tmp_2, _tmp_3, _tmp_4, _tmp_5);
  }
}

vec3
com_io7m_renderer_SphericalLight_diffuse_color (struct_com_io7m_renderer_SphericalLight_t light, struct_com_io7m_renderer_SphericalLight_directions d)
{
  float factor = max(0.0, dot(d.stl, d.normal));
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
  }
}

vec3
com_io7m_renderer_SphericalLight_diffuse_specular (struct_com_io7m_renderer_SphericalLight_t light, vec3 n, vec3 p, float s)
{
  struct_com_io7m_renderer_SphericalLight_directions d = com_io7m_renderer_SphericalLight_directions(light, p, n);
  float a = com_io7m_renderer_SphericalLight_attenuation(light, d.distance);
  vec3 dc = com_io7m_renderer_SphericalLight_diffuse_color(light, d);
  vec3 sc = com_io7m_renderer_SphericalLight_specular_color(light, d, s);
  {
    return ((dc + sc) * a);
  }
}

// section: parameters
uniform struct_com_io7m_renderer_SphericalLight_t light;
uniform float shininess;
uniform sampler2D t_diffuse_0;
// section: inputs
varying vec2 f_uv;
varying vec3 f_normal;
varying vec4 f_position;
// section: outputs
// output 0 out_0 omitted
// section: main
void
main (void)
{
  vec3 n = normalize(f_normal);
  vec3 light_term = com_io7m_renderer_SphericalLight_diffuse_specular(light, n, f_position.xyz, shininess);
  vec3 albedo = texture2D(t_diffuse_0, f_uv).xyz;
  vec4 rgba = vec4((albedo * light_term), 1.0);
  gl_FragColor = rgba;
}
