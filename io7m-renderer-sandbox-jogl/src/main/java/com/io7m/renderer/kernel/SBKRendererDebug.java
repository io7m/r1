package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.types.RException;

public final class SBKRendererDebug implements SBKRendererType
{
  private final @Nonnull KRendererDebugType actual;

  public SBKRendererDebug(
    final @Nonnull KRendererDebugType in_actual)
  {
    this.actual = in_actual;
  }

  @Override public void accept(
    final SBKRendererVisitor v)
    throws RException,
      ConstraintError
  {
    v.visitDebug(this.actual);
  }

  public @Nonnull KRendererDebugType getActual()
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
