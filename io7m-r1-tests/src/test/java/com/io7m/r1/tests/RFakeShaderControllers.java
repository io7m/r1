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

package com.io7m.r1.tests;

import java.util.Map;

import com.io7m.jcanephora.FragmentShaderType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.ProgramAttributeType;
import com.io7m.jcanephora.ProgramUniformType;
import com.io7m.jcanephora.ProgramUsableType;
import com.io7m.jcanephora.VertexShaderType;
import com.io7m.jcanephora.fake.FakeShaderControlType;

public final class RFakeShaderControllers
{
  public static FakeShaderControlType newNull()
  {
    return new FakeShaderControlType() {
      @Override public void onVertexShaderCompile(
        final String name,
        final VertexShaderType v)
        throws JCGLException
      {
        System.err.println("shader-control: vertex shader compile: "
          + name
          + " "
          + v);
      }

      @Override public void onProgramCreate(
        final String name,
        final ProgramUsableType program,
        final Map<String, ProgramUniformType> uniforms,
        final Map<String, ProgramAttributeType> attributes)
        throws JCGLException
      {
        System.err.println("shader-control: program create: "
          + name
          + " "
          + program);
      }

      @Override public void onFragmentShaderCompile(
        final String name,
        final FragmentShaderType f)
        throws JCGLException
      {
        System.err.println("shader-control: fragment shader compile: "
          + name
          + " "
          + f);
      }
    };
  }
}
