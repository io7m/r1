#version 120

struct pt_com_io7m_renderer_DirectionalLight_t {
  vec3 color;
  vec3 direction;
  float intensity;
};
struct pt_com_io7m_renderer_Materials_t {
  float specular_exponent;
  float specular_intensity;
};
struct pt_com_io7m_renderer_DirectionalLight_directions {
  vec3 otl;
  vec3 normal;
  vec3 stl;
  vec3 reflection;
};

uniform pt_com_io7m_renderer_DirectionalLight_t light;
uniform pt_com_io7m_renderer_Materials_t material;
uniform sampler2D t_diffuse_0;
uniform sampler2D t_normal;
uniform mat3 m_normal;
varying vec3 f_normal;
varying vec3 f_tangent;
varying vec3 f_bitangent;
varying vec2 f_uv;
varying vec4 f_position;


vec3
p_com_io7m_renderer_DirectionalLight_diffuse_color (pt_com_io7m_renderer_DirectionalLight_t light, pt_com_io7m_renderer_DirectionalLight_directions d)
{
  float factor = max(0.0, dot(d.stl, d.normal));
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
  }
}

vec3
p_com_io7m_renderer_Normals_unpack (sampler2D map, vec2 uv)
{
  vec3 rgb = texture2D(map, uv).xyz;
  {
    return ((rgb * 2.0) + -1.0);
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
p_com_io7m_renderer_Normals_transform (vec3 m, vec3 t, vec3 b, vec3 n)
{
  mat3 mat = mat3(t, b, n);
  {
    return normalize((mat * m));
  }
}

vec3
p_com_io7m_renderer_DirectionalLight_specular_color (pt_com_io7m_renderer_DirectionalLight_t light, pt_com_io7m_renderer_DirectionalLight_directions d, pt_com_io7m_renderer_Materials_t material)
{
  float factor = pow(max(0.0, dot(d.reflection, d.stl)), material.specular_exponent);
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return (color * material.specular_intensity);
  }
}

vec3
p_com_io7m_renderer_DirectionalLight_diffuse_specular (pt_com_io7m_renderer_DirectionalLight_t light, vec3 n, vec3 p, pt_com_io7m_renderer_Materials_t material)
{
  pt_com_io7m_renderer_DirectionalLight_directions d = p_com_io7m_renderer_DirectionalLight_directions(light, p, n);
  vec3 dc = p_com_io7m_renderer_DirectionalLight_diffuse_color(light, d);
  vec3 sc = p_com_io7m_renderer_DirectionalLight_specular_color(light, d, material);
  {
    return (dc + sc);
  }
}


void
main (void)
{
  vec3 m = p_com_io7m_renderer_Normals_unpack(t_normal, f_uv);
  vec3 n = normalize(f_normal);
  vec3 t = normalize(f_tangent);
  vec3 b = normalize(f_bitangent);
  vec3 r = p_com_io7m_renderer_Normals_transform(m, t, b, n);
  vec3 e = (m_normal * r);
  vec3 light_term = p_com_io7m_renderer_DirectionalLight_diffuse_specular(light, e, f_position.xyz, material);
  vec3 albedo = texture2D(t_diffuse_0, f_uv).xyz;
  vec4 rgba = vec4((albedo * light_term), 1.0);
  gl_FragColor = rgba;
}
