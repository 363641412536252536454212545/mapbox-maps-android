package com.mapbox.maps.testapp.examples

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.bindgen.Value
import com.mapbox.common.TileDataDomain
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.*
import com.mapbox.maps.testapp.R
import kotlinx.android.synthetic.main.activity_map_view_customization.*

/**
 * Example of customizing your [MapView].
 * This example will show how to create [MapView] both programmatically and from xml
 * and apply various options.
 */
class MapViewCustomizationActivity : AppCompatActivity() {

  private lateinit var customMapView: MapView
  // Users should keep a reference to the customised tileStore instance (if there's any)
  private val tileStore by lazy {
    TileStore.create().also {
      // Users need to make sure the custom TileStore is initialised properly with valid access token
      it.setOption(
        TileStoreOptions.MAPBOX_ACCESS_TOKEN,
        TileDataDomain.MAPS,
        Value(ResourceOptionsManager.getDefault(this).resourceOptions.accessToken)
      )
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Set the application-scoped ResourceOptionsManager with customised token, tile store and tile store usage mode
    // so that all MapViews created with default config will apply these settings.
    ResourceOptionsManager.getDefault(this, getString(R.string.mapbox_access_token)).update {
      tileStore(tileStore)
      tileStoreUsageMode(TileStoreUsageMode.READ_ONLY)
    }

    setContentView(R.layout.activity_map_view_customization)

    // all options provided in xml file - so we just load style
    mapView.getMapboxMap().loadStyleUri(Style.DARK)
    configureMapViewFromCode()
  }

  private fun configureMapViewFromCode() {
    // set map options
    val mapOptions = MapOptions.Builder().applyDefaultParams(this)
      .constrainMode(ConstrainMode.HEIGHT_ONLY)
      .glyphsRasterizationOptions(
        GlyphsRasterizationOptions.Builder()
          .rasterizationMode(GlyphsRasterizationMode.IDEOGRAPHS_RASTERIZED_LOCALLY)
          // Font family is required when the GlyphsRasterizationMode is set to IDEOGRAPHS_RASTERIZED_LOCALLY or ALL_GLYPHS_RASTERIZED_LOCALLY
          .fontFamily("sans-serif")
          .build()
      )
      .build()

    // plugins configuration
    val plugins = listOf(
      PLUGIN_LOGO_CLASS_NAME,
      PLUGIN_ATTRIBUTION_CLASS_NAME
    )

    // set token and cache size for this particular map view, these settings will overwrite the default value.
    val resourceOptions = ResourceOptions.Builder().applyDefaultParams(this)
      .accessToken(getString(R.string.mapbox_access_token))
      .tileStoreUsageMode(TileStoreUsageMode.DISABLED)
      .build()

    // set initial camera position
    val initialCameraOptions = CameraOptions.Builder()
      .center(Point.fromLngLat(-122.4194, 37.7749))
      .zoom(9.0)
      .bearing(120.0)
      .build()

    val mapInitOptions =
      MapInitOptions(this, resourceOptions, mapOptions, plugins, initialCameraOptions, true)

    // create view programmatically and add to root layout
    customMapView = MapView(this, mapInitOptions)
    val params = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      0,
      1.0f
    )
    root.addView(customMapView, params)
    // load style to map view
    customMapView.getMapboxMap().loadStyleUri(Style.SATELLITE)
  }

  override fun onStart() {
    super.onStart()
    mapView.onStart()
    customMapView.onStart()
  }

  override fun onStop() {
    super.onStop()
    mapView.onStop()
    customMapView.onStop()
  }

  override fun onLowMemory() {
    super.onLowMemory()
    mapView.onLowMemory()
    customMapView.onLowMemory()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.onDestroy()
    customMapView.onDestroy()
    // Restore the default application-scoped resource settings, otherwise it will affect other activities
    ResourceOptionsManager.destroyDefault()
  }
}