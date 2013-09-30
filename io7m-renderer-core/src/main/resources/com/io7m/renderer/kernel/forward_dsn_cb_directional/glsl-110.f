#version 110
// Generated: 2013-09-30 14:44:07 +0000
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

vec3
com_io7m_renderer_Normals_unpack (sampler2D map, vec2 uv)
{
  vec3 rgb = texture2D(map, uv).xyz;
  {
    return ((rgb * 2.0) + -1.0);
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
com_io7m_renderer_Normals_transform (vec3 m, vec3 t, vec3 b, vec3 n)
{
  mat3 mat = mat3(t, b, n);
  {
    return normalize((mat * m));
  }
}

vec3
com_io7m_renderer_DirectionalLight_specular_color (struct_com_io7m_renderer_DirectionalLight_t light, struct_com_io7m_renderer_DirectionalLight_directions d, float s)
{
  float factor = pow(max(0.0, dot(d.reflection, d.stl)), s);
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
  }
}

vec3
com_io7m_renderer_DirectionalLight_diffuse_specular (struct_com_io7m_renderer_DirectionalLight_t light, vec3 n, vec3 p, float s)
{
  struct_com_io7m_renderer_DirectionalLight_directions d = com_io7m_renderer_DirectionalLight_directions(light, p, n);
  vec3 dc = com_io7m_renderer_DirectionalLight_diffuse_color(light, d);
  vec3 sc = com_io7m_renderer_DirectionalLight_specular_color(light, d, s);
  {
    return (dc + sc);
  }
}

// section: parameters
uniform struct_com_io7m_renderer_DirectionalLight_t light;
uniform float shininess;
uniform sampler2D t_diffuse_0;
uniform sampler2D t_normal;
uniform mat3 m_normal;
// section: inputs
varying vec3 f_normal;
varying vec3 f_tangent;
varying vec3 f_bitangent;
varying vec2 f_uv;
varying vec4 f_position;
// section: outputs
// output 0 out_0 omitted
// section: main
void
main (void)
{
  vec3 m = com_io7m_renderer_Normals_unpack(t_normal, f_uv);
  vec3 n = normalize(f_normal);
  vec3 t = normalize(f_tangent);
  vec3 b = normalize(f_bitangent);
  vec3 r = com_io7m_renderer_Normals_transform(m, t, b, n);
  vec3 e = (m_normal * r);
  vec3 light_term = com_io7m_renderer_DirectionalLight_diffuse_specular(light, e, f_position.xyz, shininess);
  vec3 albedo = texture2D(t_diffuse_0, f_uv).xyz;
  vec4 rgba = vec4((albedo * light_term), 1.0);
  gl_FragColor = rgba;
}
