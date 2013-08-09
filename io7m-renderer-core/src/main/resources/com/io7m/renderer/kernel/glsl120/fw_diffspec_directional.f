#version 120

uniform sampler2D t_diffuse_0;

uniform vec3  l_color;
uniform float l_intensity;
uniform vec3  l_direction;

attribute vec4 f_position;
attribute vec2 f_uv;
attribute vec3 f_normal;

void
main (void)
{
  vec3 V = normalize (f_position.xyz);
  vec3 N = normalize (f_normal);
  vec3 L = normalize (-l_direction);
  vec3 R = reflect (V, N);

  float l_diffuse_factor = max (0, dot (L, N));
  vec3 l_diffuse_color   = l_color * l_intensity * l_diffuse_factor;

  float l_spec_factor = pow (max (dot (R, L), 0.0), 64);
  vec3 l_spec_color   = l_color * l_intensity * l_spec_factor;

  vec3 surface = texture2D(t_diffuse_0, f_uv).rgb;

  gl_FragColor = vec4 (surface * (l_diffuse_color + l_spec_color), 1.0);
}
