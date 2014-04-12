package com.io7m.renderer.kernel.sandbox;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.KRendererDeferredType;
import com.io7m.renderer.types.RException;

public final class SBKRendererDeferred implements SBKRendererType
{
  private final @Nonnull KRendererDeferredType actual;

  public SBKRendererDeferred(
    final @Nonnull KRendererDeferredType in_actual)
  {
    this.actual = in_actual;
  }

  @Override public void accept(
    final SBKRendererVisitor v)
    throws RException,
      ConstraintError
  {
    v.visitDeferred(this.actual);
  }

  public @Nonnull KRendererDeferredType getActual()
  {
    return this.actual;
  }
}
