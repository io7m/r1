package com.io7m.renderer.kernel.sandbox;

import com.io7m.renderer.kernel.KRendererForwardType;
import com.io7m.renderer.types.RException;

public final class SBKRendererForward implements SBKRendererType
{
  private final KRendererForwardType actual;

  public SBKRendererForward(
    final KRendererForwardType in_actual)
  {
    this.actual = in_actual;
  }

  @Override public void accept(
    final SBKRendererVisitor v)
    throws RException
  {
    v.visitForward(this.actual);
  }

  public KRendererForwardType getActual()
  {
    return this.actual;
  }
}
