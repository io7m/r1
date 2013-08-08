#version 110

uniform sampler2D t_diffuse_0;
uniform vec3  l_color;
uniform float l_intensity;
uniform vec3  l_direction;

attribute vec2 f_uv;
attribute vec3 f_normal;

void
main (void)
{
  vec3 N = normalize(f_normal);
  vec3 L = normalize(-l_direction);

  float l_diffuse_factor = max (0, dot (L, N));
  vec3 l_diffuse_color   = l_color * l_intensity * l_diffuse_factor;

  vec3 surface = texture2D(t_diffuse_0, f_uv).rgb;

  gl_FragColor = vec4 (surface * l_diffuse_color, 1.0);
}
