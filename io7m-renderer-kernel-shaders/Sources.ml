(*| Copyright � 2013 <code@io7m.com> http://io7m.com                         *)
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

let preamble =
"--\n" ^
"-- Copyright (c) 2013 <code@io7m.com> http://io7m.com\n" ^
"--\n" ^
"-- Permission to use, copy, modify, and/or distribute this software for any\n" ^
"-- purpose with or without fee is hereby granted, provided that the above\n" ^
"-- copyright notice and this permission notice appear in all copies.\n" ^
"--\n" ^
"-- THE SOFTWARE IS PROVIDED \"AS IS\" AND THE AUTHOR DISCLAIMS ALL WARRANTIES\n" ^
"-- WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF\n" ^
"-- MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY\n" ^
"-- SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES\n" ^
"-- WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN\n" ^
"-- ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR\n" ^
"-- IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.\n" ^
"--\n" ^
"\n" ^
"package com.io7m.renderer;\n"

let main (_ : unit) : unit =
  begin let labels = Labels.label_all () in
    List.iter (fun l ->
      let code = Labels.label_code l in
      let c = open_out ("out-parasol/fwd_" ^ code ^ ".p") in
        begin
          output_string c preamble;
          output_string c "\n";
          output_string c (String.concat "" (Shaders.module_start ("Fwd_" ^ code)));
          output_string c "\n";
          output_string c (String.concat "" (Shaders.fwd_vertex_shader l));
          output_string c "\n";
          output_string c (String.concat "" (Shaders.fwd_fragment_shader l));
          output_string c "\n";
          output_string c (String.concat "" (Shaders.fwd_program_shader l));
          output_string c "\n";
          output_string c (String.concat "" (Shaders.module_end));
          output_string c "\n";
          flush c;
          close_out c;
        end
    ) labels;
  end

let _ = main ()
