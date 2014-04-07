package com.io7m.renderer.kernel.sandbox;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.KRendererForwardType;
import com.io7m.renderer.types.RException;

public final class SBKRendererForward implements SBKRendererType
{
  private final @Nonnull KRendererForwardType actual;

  public SBKRendererForward(
    final @Nonnull KRendererForwardType in_actual)
  {
    this.actual = in_actual;
  }

  @Override public void accept(
    final SBKRendererVisitor v)
    throws RException,
      ConstraintError
  {
    v.visitForward(this.actual);
  }

  public @Nonnull KRendererForwardType getActual()
  {
    return this.actual;
  }

  @Override public void close()
    throws RException,
      ConstraintError
  {
    this.actual.rendererClose();
  }
}
