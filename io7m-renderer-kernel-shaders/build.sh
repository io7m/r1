#!/bin/sh

ocamlopt -annot -w "+a" -c Labels.mli
ocamlopt -annot -w "+a" -c Labels.ml
ocamlopt -annot -w "+a" -c Shaders.ml
ocamlopt -annot -w "+a" -o sources Labels.cmx Shaders.cmx Sources.ml
ocamlopt -annot -w "+a" -o batch Labels.cmx Shaders.cmx Batch.ml
