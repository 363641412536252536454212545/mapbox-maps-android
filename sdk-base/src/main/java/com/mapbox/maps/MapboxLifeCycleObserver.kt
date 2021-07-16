package com.mapbox.maps

/**
 * MapboxLifeCycleObserver interface defines the lifecycle events that needed by MapView.
 */
interface MapboxLifeCycleObserver {
  /**
   * Called to start rendering
   */
  fun onStart()

  /**
   * Called to stop rendering
   */
  fun onStop()

  /**
   * Called to dispose the renderer
   */
  fun onDestroy()
}