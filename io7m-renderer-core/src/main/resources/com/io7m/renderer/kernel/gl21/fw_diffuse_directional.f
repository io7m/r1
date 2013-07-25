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

  float l_diffuse_factor = max(0, dot (L, N));
  vec4 l_diffuse_color   = vec4(l_color, 1.0) * l_intensity * l_diffuse_factor;

  gl_FragColor = texture2D(t_diffuse_0, f_uv) * l_diffuse_color;
}
