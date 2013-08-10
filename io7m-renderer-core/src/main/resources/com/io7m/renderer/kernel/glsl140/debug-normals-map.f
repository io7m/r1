#version 140

uniform sampler2D t_normal;

in vec2 f_uv;
in vec3 f_normal;

out vec4 out_frag_color;

void
main (void)
{
  vec3 N  = normalize (f_normal);
  vec3 NM = texture2D (t_normal, f_uv).rgb;
  NM = (NM * 2.0) - 1.0;

  out_frag_color = vec4(normalize (N + NM), 1);
}
