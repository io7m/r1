#version 420

struct pt_com_io7m_renderer_SphericalLight_t {
  vec3 color;
  vec3 position;
  float intensity;
  float radius;
  float falloff;
};
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
struct pt_com_io7m_renderer_SphericalLight_directions {
  vec3 ots;
  vec3 normal;
  vec3 stl;
  float distance;
  vec3 reflection;
};

uniform pt_com_io7m_renderer_Materials_t material;
uniform pt_com_io7m_renderer_SphericalLight_t light;
uniform vec4 f_diffuse;
uniform sampler2D t_specular;
uniform sampler2D t_normal;
uniform mat3 m_normal;
in vec3 f_normal;
in vec3 f_tangent;
in vec3 f_bitangent;
in vec2 f_uv;
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
p_com_io7m_renderer_Normals_unpack (sampler2D map, vec2 uv)
{
  vec3 rgb = texture(map, uv).xyz;
  {
    return ((rgb * 2.0) + -1.0);
  }
}

vec3
p_com_io7m_renderer_Normals_transform (vec3 m, vec3 t, vec3 b, vec3 n)
{
  mat3 mat = mat3(t, b, n);
  {
    return normalize((mat * m));
  }
}

vec3
p_com_io7m_renderer_Normals_bump (sampler2D t_normal, mat3 m_normal, vec3 n, vec3 t, vec3 b, vec2 uv)
{
  vec3 m = p_com_io7m_renderer_Normals_unpack(t_normal, uv);
  vec3 nn = normalize(n);
  vec3 nt = normalize(t);
  vec3 nb = normalize(b);
  vec3 r = p_com_io7m_renderer_Normals_transform(m, nt, nb, nn);
  {
    return (m_normal * r);
  }
}

vec3
p_com_io7m_renderer_SphericalLight_specular_color (pt_com_io7m_renderer_SphericalLight_t light, pt_com_io7m_renderer_SphericalLight_directions d, pt_com_io7m_renderer_Materials_specular s)
{
  float factor = pow(max(0.0, dot(d.reflection, d.stl)), s.exponent);
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return (color * s.intensity);
  }
}

pt_com_io7m_renderer_SphericalLight_directions
p_com_io7m_renderer_SphericalLight_directions (pt_com_io7m_renderer_SphericalLight_t light, vec3 p, vec3 n)
{
  vec3 position_diff = (p - light.position);
  vec3 ots = normalize(p);
  {
    vec3 _tmp_1 = ots;
    vec3 _tmp_2 = n;
    vec3 _tmp_3 = normalize((-position_diff));
    float _tmp_4 = length(position_diff);
    vec3 _tmp_5 = reflect(ots, n);
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
  vec3 sc = p_com_io7m_renderer_SphericalLight_specular_color(light, d, material.specular);
  {
    return ((dc + sc) * a);
  }
}

pt_com_io7m_renderer_Materials_specular
_tmp_11 (void)
{
  float _tmp_9 = material.specular.exponent;
  float _tmp_10 = texture(t_specular, f_uv).x;
  return pt_com_io7m_renderer_Materials_specular(_tmp_9, _tmp_10);
}

pt_com_io7m_renderer_Materials_t
_tmp_12 (void)
{
  pt_com_io7m_renderer_Materials_diffuse _tmp_6 = material.diffuse;
  pt_com_io7m_renderer_Materials_environment _tmp_7 = material.environment;
  pt_com_io7m_renderer_Materials_specular _tmp_8 = _tmp_11();
  return pt_com_io7m_renderer_Materials_t(_tmp_6, _tmp_8, _tmp_7);
}


void
main (void)
{
  vec3 n = p_com_io7m_renderer_Normals_bump(t_normal, m_normal, normalize(f_normal), normalize(f_tangent), normalize(f_bitangent), f_uv);
  pt_com_io7m_renderer_Materials_t m = _tmp_12();
  vec3 light_term = p_com_io7m_renderer_SphericalLight_diffuse_specular(light, n, f_position.xyz, m);
  vec3 albedo = f_diffuse.xyz;
  vec4 rgba = vec4((albedo * light_term), 1.0);
  out_0 = rgba;
}
