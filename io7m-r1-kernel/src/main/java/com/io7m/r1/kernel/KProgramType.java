/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

package com.io7m.r1.kernel;

import com.io7m.jcanephora.ProgramType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jparasol.core.JPFragmentShaderMetaType;
import com.io7m.jparasol.core.JPUncompactedProgramShaderMeta;
import com.io7m.jparasol.core.JPVertexShaderMetaType;

/**
 * The type of kernel programs.
 */

public interface KProgramType
{
  /**
   * @return A reference to the program's executable interface
   */

  JCBExecutorType getExecutable();

  /**
   * @return The fragment shader metadata.
   */

  JPFragmentShaderMetaType getFragmentShaderMeta();

  /**
   * @return Information about the compiled program
   */

  JPUncompactedProgramShaderMeta getMeta();

  /**
   * @return The compiled program
   */

  ProgramType getProgram();

  /**
   * @return The vertex shader metadata.
   */

  JPVertexShaderMetaType getVertexShaderMeta();

}
