package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

interface SBSceneChangeListenerRegistration
{
  public void sceneChangeListenerAdd(
    final @Nonnull SBSceneChangeListener listener);
}