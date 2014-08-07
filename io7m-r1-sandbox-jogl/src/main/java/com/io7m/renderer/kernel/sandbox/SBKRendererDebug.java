package com.io7m.renderer.kernel.sandbox;

import com.io7m.renderer.kernel.KRendererDebugType;
import com.io7m.renderer.types.RException;

public final class SBKRendererDebug implements SBKRendererType
{
  private final KRendererDebugType actual;

  public SBKRendererDebug(
    final KRendererDebugType in_actual)
  {
    this.actual = in_actual;
  }

  @Override public void accept(
    final SBKRendererVisitor v)
    throws RException
  {
    v.visitDebug(this.actual);
  }

  public KRendererDebugType getActual()
  {
    return this.actual;
  }
}
