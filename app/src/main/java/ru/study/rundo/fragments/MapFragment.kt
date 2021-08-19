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
import ru.study.rundo.models.Track
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val TRACK_EXTRA = "TRACK_EXTRA"
        // todo serializable error
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    lateinit var mMap: GoogleMap
    lateinit var distance: TextView
    lateinit var distanceDetails: TextView
    lateinit var time: TextView
    lateinit var timeDetails: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val track = arguments?.getSerializable(TRACK_EXTRA) as Track
        distance = view.findViewById(R.id.distance)
        distanceDetails = view.findViewById(R.id.distanceDetails)
        time = view.findViewById(R.id.time)
        timeDetails = view.findViewById(R.id.timeDetails)
        distanceDetails.text =
            resources.getString(R.string.distanceDetails, track.distance.roundToInt())
        timeDetails.text = resources.getString(
            R.string.timeDetails,
            TimeUnit.MILLISECONDS.toMinutes(track.time),
            TimeUnit.MILLISECONDS.toSeconds(track.time) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(track.time))
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val track = arguments?.getSerializable(TRACK_EXTRA) as Track

        val polyLineOptions = PolylineOptions()
        val builder = LatLngBounds.Builder()

        mMap.addMarker(
            MarkerOptions().position(LatLng(track.points.first().lng, track.points.first().lat))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title("start")
        )
        mMap.addMarker(
            MarkerOptions().position(LatLng(track.points.last().lng, track.points.last().lat))
                .title("finish")
        )

        track.points.forEach {
            polyLineOptions.add(LatLng(it.lng, it.lat))
            polyLineOptions.width(12f)
            polyLineOptions.color(Color.BLACK)
            polyLineOptions.geodesic(true)
            builder.include(LatLng(it.lng, it.lat))
        }
        mMap.addPolyline(polyLineOptions)

        mMap.setOnMapLoadedCallback {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200))
        }
    }
}
