/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.renderer.kernel;

import java.io.File;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jparasol.xml.PGLSLMetaXML;

public final class SBShader
{
  private final @Nonnull KProgram     program;
  private final @Nonnull File         file;
  private final @Nonnull PGLSLMetaXML meta;

  SBShader(
    final @Nonnull KProgram in_program,
    final @Nonnull File in_file,
    final @Nonnull PGLSLMetaXML in_meta)
    throws ConstraintError
  {
    this.program = Constraints.constrainNotNull(in_program, "Program");
    this.file = Constraints.constrainNotNull(in_file, "File");
    this.meta = Constraints.constrainNotNull(in_meta, "Meta");
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final SBShader other = (SBShader) obj;
    if (!this.file.equals(other.file)) {
      return false;
    }
    if (!this.meta.equals(other.meta)) {
      return false;
    }
    if (!this.program.equals(other.program)) {
      return false;
    }
    return true;
  }

  public @Nonnull File getFile()
  {
    return this.file;
  }

  public @Nonnull PGLSLMetaXML getMeta()
  {
    return this.meta;
  }

  public @Nonnull String getName()
  {
    return this.meta.getName();
  }

  public @Nonnull KProgram getProgram()
  {
    return this.program;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.file.hashCode();
    result = (prime * result) + this.meta.hashCode();
    result = (prime * result) + this.program.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBShader ");
    builder.append(this.program);
    builder.append(" ");
    builder.append(this.file);
    builder.append(" ");
    builder.append(this.meta);
    builder.append("]");
    return builder.toString();
  }
}
