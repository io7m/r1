
type lalbedo = LAlbedoColour | LAlbedoTextured

val ld_first : lalbedo
val ld_next : lalbedo -> lalbedo option
val ld_code : lalbedo -> string option
val ld_fold : (lalbedo -> 'a -> 'a) -> 'a -> 'a

type lnormal = LNormalsNone | LNormalsVertex | LNormalsMapped

val ln_code : lnormal -> string option
val ln_next : lnormal -> lnormal option
val ln_first : lnormal
val ln_fold : (lnormal -> 'a -> 'a) -> 'a -> 'a

type lspecular = LSpecularNone | LSpecularConstant | LSpecularMapped

val ls_code : lspecular -> string option
val ls_first : lspecular
val ls_next : lspecular -> lspecular option
val ls_fold : (lspecular -> 'a -> 'a) -> 'a -> 'a

type lalpha = LAlphaOpaque | LAlphaTranslucent

val la_code : lalpha -> string option
val la_first : lalpha
val la_next : lalpha -> lalpha option
val la_fold : (lalpha -> 'a -> 'a) -> 'a -> 'a

type lenvironment =
    LEnvironmentNone
  | LEnvironmentReflective
  | LEnvironmentRefractive
  | LEnvironmentReflectiveRefractive

val le_code : lenvironment -> string option
val le_first : lenvironment
val le_next : lenvironment -> lenvironment option
val le_fold : (lenvironment -> 'a -> 'a) -> 'a -> 'a

type lemissive = LEmissiveNone | LEmissiveConstant | LEmissiveMapped

val lm_code : lemissive -> string option
val lm_first : lemissive
val lm_next : lemissive -> lemissive option
val lm_fold : (lemissive -> 'a -> 'a) -> 'a -> 'a

type light =
    LLightDirectional
  | LLightSpherical
  | LLightDirectionalShadowMapped
  | LLightSphericalShadowMapped

val light_code : light -> string
val light_first : light
val light_next : light -> light option
val light_fold : (light -> 'a -> 'a) -> 'a -> 'a

type label =
    LLit of lalpha * lalbedo * lemissive * lnormal * lenvironment * light *
      lspecular
  | LUnlit of lalpha * lalbedo * lemissive

val label_all : unit -> label list
val label_code : label -> string
val label_implies_uv : label -> bool
