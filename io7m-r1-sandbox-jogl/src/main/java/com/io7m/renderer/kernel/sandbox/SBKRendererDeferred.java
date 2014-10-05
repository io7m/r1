package com.io7m.renderer.kernel.sandbox;

import com.io7m.renderer.kernel.KRendererDeferredType;
import com.io7m.renderer.types.RException;

public final class SBKRendererDeferred implements SBKRendererType
{
  private final KRendererDeferredType actual;

  public SBKRendererDeferred(
    final KRendererDeferredType in_actual)
  {
    this.actual = in_actual;
  }

  @Override public void accept(
    final SBKRendererVisitor v)
    throws RException
  {
    v.visitDeferred(this.actual);
  }

  public KRendererDeferredType getActual()
  {
    return this.actual;
  }
}
