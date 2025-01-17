package com.mapbox.maps.testapp.examples.annotation

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadedListener
import com.mapbox.maps.testapp.R
import com.mapbox.maps.testapp.utils.BitmapUtils.bitmapFromDrawableRes
import com.mapbox.turf.TurfMeasurement
import kotlinx.android.synthetic.main.activity_add_marker_symbol.*
import kotlinx.android.synthetic.main.activity_add_marker_symbol.mapView
import kotlinx.android.synthetic.main.activity_annotation.*
import java.util.*

/**
 * Example showing how to add point annotations and animate them
 */
class AnimatePointAnnotationActivity : AppCompatActivity(), OnMapLoadedListener {
  private var pointAnnotationManager: PointAnnotationManager? = null
  private var animateCarList = listOf<PointAnnotation>()
  private val animators: MutableList<ValueAnimator> = mutableListOf()
  private var noAnimateCarNum: Int = 10
  private var animateCarNum: Int = 10
  private var animateDuration = 5000L
  private lateinit var mapboxMap: MapboxMap
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_annotation)
    mapboxMap = mapView.getMapboxMap()
    mapboxMap.setCamera(
      CameraOptions.Builder()
        .center(
          Point.fromLngLat(
            LONGITUDE,
            LATITUDE
          )
        )
        .zoom(12.0)
        .build()
    )

    mapboxMap.addOnMapLoadedListener(this@AnimatePointAnnotationActivity)
    mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)
    deleteAll.visibility = View.GONE
    changeStyle.visibility = View.GONE
  }

  override fun onMapLoaded() {
    pointAnnotationManager = mapView.annotations.createPointAnnotationManager(mapView).apply {
      bitmapFromDrawableRes(
        this@AnimatePointAnnotationActivity,
        R.drawable.ic_car_top
      )?.let {
        val noAnimationOptionList = mutableListOf<PointAnnotationOptions>()
        for (i in 0 until noAnimateCarNum) {
          noAnimationOptionList.add(
            PointAnnotationOptions()
              .withPoint(getPointInBounds())
              .withIconImage(it)
          )
        }
        create(noAnimationOptionList)
      }
      bitmapFromDrawableRes(
        this@AnimatePointAnnotationActivity,
        R.drawable.ic_taxi_top
      )?.let {
        val animationOptionList = mutableListOf<PointAnnotationOptions>()
        for (i in 0 until animateCarNum) {
          animationOptionList.add(
            PointAnnotationOptions()
              .withPoint(getPointInBounds())
              .withIconImage(it)
          )
        }
        animateCarList = create(animationOptionList)
      }
    }
    animateCars()
  }

  private fun animateCars() {
    cleanAnimation()
    for (i in 0 until animateCarNum) {
      val nextPoint = getPointInBounds()
      val animator =
        ValueAnimator.ofObject(
          CarEvaluator(),
          animateCarList[i].point,
          nextPoint
        ).setDuration(animateDuration)
      animateCarList[i].iconRotate = TurfMeasurement.bearing(animateCarList[i].point, nextPoint)
      animator.interpolator = LinearInterpolator()
      animator.addUpdateListener { valueAnimator ->
        (valueAnimator.animatedValue as Point).let {
          animateCarList[i].point = it
        }
      }
      animator.start()
      animators.add(animator)
    }
    // Endless repeat animations
    animators[0].addListener(
      onEnd = {
        runOnUiThread {
          animateCars()
        }
      }
    )
    animators[0].addUpdateListener {
      // Update all annotations in batch
      pointAnnotationManager?.update(animateCarList)
    }
  }

  private fun getPointInBounds(): Point {
    val bounds: CoordinateBounds =
      mapboxMap.coordinateBoundsForCamera(mapboxMap.cameraState.toCameraOptions())
    val generator = Random()
    val lat =
      bounds.southwest.latitude() + (bounds.northeast.latitude() - bounds.southwest.latitude()) * generator.nextDouble()
    val lon =
      bounds.southwest.longitude() + (bounds.northeast.longitude() - bounds.southwest.longitude()) * generator.nextDouble()
    return Point.fromLngLat(lon, lat)
  }

  private fun cleanAnimation() {
    animators.forEach {
      it.removeAllListeners()
      it.cancel()
    }
    animators.clear()
  }

  override fun onStart() {
    super.onStart()
    mapView.onStart()
  }

  override fun onStop() {
    super.onStop()
    cleanAnimation()
    mapView.onStop()
  }

  override fun onLowMemory() {
    super.onLowMemory()
    mapView.onLowMemory()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.onDestroy()
  }

  private class CarEvaluator : TypeEvaluator<Point> {
    override fun evaluate(
      fraction: Float,
      startValue: Point,
      endValue: Point
    ): Point {
      val lat =
        startValue.latitude() + (endValue.latitude() - startValue.latitude()) * fraction
      val lon =
        startValue.longitude() + (endValue.longitude() - startValue.longitude()) * fraction
      return Point.fromLngLat(lon, lat)
    }
  }

  companion object {
    private const val LATITUDE = 55.665957
    private const val LONGITUDE = 12.550343
  }
}