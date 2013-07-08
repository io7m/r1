#version 130

uniform sampler2D t_diffuse_0;
uniform vec3 l_color;
uniform vec3 l_direction;
uniform float l_intensity;

in vec2 f_uv;
in vec3 f_normal;

out vec4 out_frag_color;

void
main (void)
{
  vec3 f_normal_n = normalize(f_normal);

  vec3 l_direction_reversed = -l_direction;
  float l_diffuse_factor    = max(0, dot (l_direction_reversed, f_normal_n));
  vec4 l_diffuse_color      = vec4(l_color, 1.0) * l_intensity * l_diffuse_factor;

  out_frag_color = texture2D(t_diffuse_0, vec2(f_uv.x, 1.0 - f_uv.y)) * l_diffuse_color;
}
