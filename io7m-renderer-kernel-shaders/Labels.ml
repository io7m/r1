(*| Copyright © 2013 <code@io7m.com> http://io7m.com                         *)
(*|                                                                          *)
(*| Permission to use, copy, modify, and/or distribute this software for any *)
(*| purpose with or without fee is hereby granted, provided that the above   *)
(*| copyright notice and this permission notice appear in all copies.        *)
(*|                                                                          *)
(*| THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES *)
(*| WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF         *)
(*| MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR  *)
(*| ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES   *)
(*| WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN    *)
(*| ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF  *)
(*| OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.           *)

(* Generic fold principle for any type with first and next. *)

let gen_fold
  (f : 'a -> 'b -> 'b)
  (x : 'b)
  (first : 'a)
  (next : 'a -> 'a option)
: 'b =
  let rec k d x =
    let r = f d x in
    begin match next d with
    | Some n -> k n r
    | None -> r
    end
  in k first x

(* Albedo properties *)

type lalbedo =
  | LAlbedoColour
  | LAlbedoTextured

let ld_first = LAlbedoColour

let ld_next = function
  | LAlbedoColour -> Some LAlbedoTextured
  | LAlbedoTextured -> None

let ld_code = function
  | LAlbedoColour -> Some "BC"
  | LAlbedoTextured -> Some "BT"

let ld_fold f x = gen_fold f x ld_first ld_next

(* Normal properties *)

type lnormal =
  | LNormalsNone
  | LNormalsVertex
  | LNormalsMapped

let ln_code = function
  | LNormalsNone -> None
  | LNormalsVertex -> Some "NV"
  | LNormalsMapped -> Some "NM"

let ln_next = function
  | LNormalsNone -> Some LNormalsVertex
  | LNormalsVertex -> Some LNormalsMapped
  | LNormalsMapped -> None

let ln_first = LNormalsNone

let ln_fold f x = gen_fold f x ln_first ln_next

(* Specular properties *)

type lspecular =
  | LSpecularNone
  | LSpecularConstant
  | LSpecularMapped

let ls_code = function
  | LSpecularNone -> None
  | LSpecularConstant -> Some "SC"
  | LSpecularMapped -> Some "SM"

let ls_first = LSpecularNone

let ls_next = function
  | LSpecularNone -> Some LSpecularConstant
  | LSpecularConstant -> Some LSpecularMapped
  | LSpecularMapped -> None

let ls_fold f x = gen_fold f x ls_first ls_next

(* Alpha properties *)

type lalpha =
  | LAlphaOpaque
  | LAlphaTranslucent

let la_code = function
  | LAlphaOpaque -> Some "AO"
  | LAlphaTranslucent -> Some "AT"

let la_first = LAlphaOpaque

let la_next = function
  | LAlphaOpaque -> Some LAlphaTranslucent
  | LAlphaTranslucent -> None

let la_fold f x = gen_fold f x la_first la_next

(* Environment properties *)

type lenvironment =
  | LEnvironmentNone
  | LEnvironmentReflective
  | LEnvironmentRefractive
  | LEnvironmentReflectiveRefractive

let le_code = function
  | LEnvironmentNone -> None
  | LEnvironmentReflective -> Some "EL"
  | LEnvironmentRefractive -> Some "ER"
  | LEnvironmentReflectiveRefractive -> Some "ELR"

let le_first = LEnvironmentNone

let le_next = function
  | LEnvironmentNone -> Some LEnvironmentReflective
  | LEnvironmentReflective -> Some LEnvironmentRefractive
  | LEnvironmentRefractive -> Some LEnvironmentReflectiveRefractive
  | LEnvironmentReflectiveRefractive -> None

let le_fold f x = gen_fold f x le_first le_next

(* Emissive properties *)

type lemissive =
  | LEmissiveNone
  | LEmissiveConstant
  | LEmissiveMapped

let lm_code = function
  | LEmissiveNone -> None
  | LEmissiveConstant -> Some "MC"
  | LEmissiveMapped -> Some "MM"

let lm_first = LEmissiveNone

let lm_next = function
  | LEmissiveNone -> Some LEmissiveConstant
  | LEmissiveConstant -> Some LEmissiveMapped
  | LEmissiveMapped -> None

let lm_fold f x = gen_fold f x lm_first lm_next

type light =
  | LLightDirectional
  | LLightSpherical
  | LLightDirectionalShadowMapped
  | LLightSphericalShadowMapped

let light_code = function
  | LLightDirectional -> "LD"
  | LLightSpherical -> "LS"
  | LLightDirectionalShadowMapped -> "LDSM"
  | LLightSphericalShadowMapped -> "LSSM"

let light_first = LLightDirectional

let light_next = function
  | LLightDirectional -> Some LLightSpherical
  | LLightSpherical -> Some LLightDirectionalShadowMapped
  | LLightDirectionalShadowMapped -> Some LLightSphericalShadowMapped
  | LLightSphericalShadowMapped -> None

let light_fold f x = gen_fold f x light_first light_next

(* Labels *)

type label =
  | LLit of lalpha * lalbedo * lemissive * lnormal * lenvironment * light * lspecular
  | LUnlit of lalpha * lalbedo * lemissive

let each_light
  (xs : label list)
  (a : lalpha)
  (d : lalbedo)
  (m : lemissive)
  (n : lnormal)
  (e : lenvironment)
  (l : light) : label list =
  ls_fold (fun s xs -> LLit (a, d, m, n, e, l, s) :: xs) xs

let each_environment
  (xs : label list)
  (a : lalpha)
  (d : lalbedo)
  (m : lemissive)
  (n : lnormal)
  (e : lenvironment) : label list =
  light_fold (fun l xs -> each_light xs a d m n e l) xs

let with_normals
  (xs : label list)
  (a : lalpha)
  (d : lalbedo)
  (m : lemissive)
  (n : lnormal) : label list =
  le_fold (fun e xs -> each_environment xs a d m n e) xs

let without_normals
  (xs : label list)
  (a : lalpha)
  (d : lalbedo)
  (m : lemissive) : label list =
  LUnlit (a, d, m) :: xs

let each_normal
  (xs : label list)
  (a : lalpha)
  (d : lalbedo)
  (m : lemissive)
  (n : lnormal) : label list =
  begin match n with
  | LNormalsNone -> without_normals xs a d m
  | LNormalsVertex -> with_normals xs a d m n
  | LNormalsMapped -> with_normals xs a d m n
  end

let each_emissive
  (xs : label list)
  (a : lalpha)
  (d : lalbedo)
  (m : lemissive) : label list =
  ln_fold (fun n xs -> each_normal xs a d m n) xs

let each_albedo
  (xs : label list)
  (a : lalpha)
  (d : lalbedo) : label list =
  lm_fold (fun m xs -> each_emissive xs a d m) xs

let each_alpha
  (xs : label list)
  (a : lalpha) : label list =
  ld_fold (fun d xs -> each_albedo xs a d) xs

let label_all (_ : unit) =
  la_fold (fun a xs -> each_alpha xs a) []

let opt_code : string option -> string = function
  | Some s -> "_" ^ s
  | None -> ""

let label_code = function
  | LLit (a, d, m, n, e, l, s) ->
      String.concat "" [
      light_code l;
      opt_code (la_code a);
      opt_code (ld_code d);
      opt_code (lm_code m);
      opt_code (ln_code n);
      opt_code (le_code e);
      opt_code (ls_code s)
      ]
  | LUnlit (a, d, m) ->
      String.concat "" [
      "U";
      opt_code (la_code a);
      opt_code (ld_code d);
      opt_code (lm_code m)
      ]

let label_implies_uv l =
  begin match l with
  | LLit (_, d, m, n, _, _, s) ->
    begin match d with
    | LAlbedoTextured -> true
    | LAlbedoColour ->
      begin match m with
      | LEmissiveMapped -> true
      | LEmissiveNone | LEmissiveConstant ->
        begin match n with
        | LNormalsMapped -> true
        | LNormalsNone | LNormalsVertex ->
          begin match s with
          | LSpecularMapped -> true
          | LSpecularConstant | LSpecularNone -> false
          end
        end
      end
    end
  | LUnlit (_, d, m) ->
    begin match d with
    | LAlbedoTextured -> true
    | LAlbedoColour ->
      begin match m with
      | LEmissiveMapped -> true
      | LEmissiveNone | LEmissiveConstant -> false
      end
    end
  end
