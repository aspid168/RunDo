package ru.study.rundo.fragments

import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import ru.study.rundo.R
import ru.study.rundo.activities.MainActivity
import ru.study.rundo.models.Point
import ru.study.rundo.models.Track
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.roundToInt

class MapFragment : Fragment(), OnMapReadyCallback {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    private lateinit var mMap: GoogleMap
    private lateinit var distance: TextView
    private lateinit var distanceDetails: TextView
    private lateinit var time: TextView
    private lateinit var timeDetails: TextView
    private lateinit var mapFragment: SupportMapFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        distance = view.findViewById(R.id.distance)
        distanceDetails = view.findViewById(R.id.distanceDetails)
        time = view.findViewById(R.id.time)
        timeDetails = view.findViewById(R.id.timeDetails)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val track = arguments?.getSerializable(MainActivity.TRACK_EXTRA)
        if (track is Track) {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(track.time)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(track.time) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(track.time)
            )
            val distance = track.distance.roundToInt()
            distanceDetails.text = resources.getString(R.string.distanceDetails, distance)
            timeDetails.text = resources.getString(R.string.timeDetails, minutes, seconds)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val track = arguments?.getSerializable(MainActivity.TRACK_EXTRA)
        if (track is Track) {
            val pointList = track.points
            val polyLineOptions = PolylineOptions().width(12f).color(Color.BLACK)
            val latLngBoundsBuilder = LatLngBounds.Builder()

            addMarker(pointList.first(), pointList.last())
            pointList.forEach {
                polyLineOptions.add(LatLng(it.lat, it.lng))
                latLngBoundsBuilder.include(LatLng(it.lat, it.lng))
            }
            mMap.addPolyline(polyLineOptions)
            setOnMapReadyCallback(latLngBoundsBuilder.build())
        }
    }

    private fun setOnMapReadyCallback(latLngBoundsBuilder: LatLngBounds) {
        val latLngBoundsPadding = 200
        mMap.setOnMapLoadedCallback {
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    latLngBoundsBuilder,
                    latLngBoundsPadding
                )
            )
        }
    }

    private fun addMarker(start: Point, finish: Point) {
        mMap.addMarker(
            MarkerOptions().position(LatLng(start.lat, start.lng))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title("start")
        )
        mMap.addMarker(
            MarkerOptions().position(LatLng(finish.lat, finish.lng))
                .title("finish")
        )
    }
}
