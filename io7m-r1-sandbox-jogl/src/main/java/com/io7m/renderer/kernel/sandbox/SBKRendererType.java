package com.io7m.renderer.kernel.sandbox;

import com.io7m.renderer.types.RException;

public interface SBKRendererType
{
  void accept(
    final SBKRendererVisitor v)
    throws RException;
}
