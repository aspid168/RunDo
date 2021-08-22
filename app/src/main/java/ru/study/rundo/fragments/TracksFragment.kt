package ru.study.rundo.fragments

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.study.rundo.*
import ru.study.rundo.activities.MainActivity
import ru.study.rundo.activities.RunActivity
import ru.study.rundo.adapters.TracksAdapter
import ru.study.rundo.interfaces.ServerHandler
import ru.study.rundo.interfaces.MapSwitcher
import ru.study.rundo.models.Track
import ru.study.rundo.models.TracksList

class TracksFragment : Fragment(), ServerHandler<TracksList> {

    companion object {
        private const val REFRESHING_STATE = "REFRESHING_STATE"
        private const val IS_SESSION_ACTIVE = "IS_SESSION_ACTIVE"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tracks, container, false)
    }

    private lateinit var add: FloatingActionButton
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var tracksRecyclerViewAdapter: TracksAdapter
    private lateinit var progressDialog: ProgressDialog

    private var tracksList: List<Track> = listOf()
    private var isRefreshing = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        add = view.findViewById(R.id.add)
        tracksRecyclerView = view.findViewById(R.id.tracksRecyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutTracks)
        progressDialog = ProgressDialog(requireActivity())
        progressDialog.setCancelable(false)
        progressDialog.setMessage("fetching data")

        if (savedInstanceState != null) {
            isRefreshing = savedInstanceState.getBoolean(REFRESHING_STATE)
            swipeRefreshLayout.isRefreshing = isRefreshing
        }

        val db = TracksAndNotificationsDatabase(requireContext())
        tracksList = db.getTracksList().sortedByDescending { it.beginsAt }
        db.close()
        val isTokenValid = getIsTokenValid()
        val isSessionActive = getIsSessionActive()
        val token = getToken()

        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tracksRecyclerView.adapter = TracksAdapter(tracksList)
        {
            if (activity is MapSwitcher) {
                (activity as MapSwitcher).switchToMap(it)
            }
        }
        tracksRecyclerViewAdapter = tracksRecyclerView.adapter as TracksAdapter

        if (isTokenValid == false) {
            add.hide()
        }
        if (isSessionActive == false && WorkWithServer.isGetTracksFinished == null) {
            progressDialog.show()
        }

        swipeRefreshLayout.setOnRefreshListener {
            WorkWithServer.addListenerGetTracks(this)
            token?.let { WorkWithServer.getTracks(token) }
            isRefreshing = true
        }

        add.setOnClickListener {
            if (!checkPermissions()) {
                getPermission()
            } else {
                RunActivity.startActivity(view.context)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v("start", "start")
        WorkWithServer.addListenerGetTracks(this)
        if (getIsSessionActive() == false) {
            progressDialog.show()
        }
        if ((isRefreshing || getIsSessionActive() == false) && WorkWithServer.isGetTracksFinished == null) {
            val token = getToken()
            token?.let { WorkWithServer.getTracks(token) }
        }
    }

    override fun onStop() {
        super.onStop()
        WorkWithServer.addListenerGetTracks(null)
        if (getIsSessionActive() == false) {
            progressDialog.dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(REFRESHING_STATE, isRefreshing)
    }

    private fun getSharedPreferences(): SharedPreferences? {
        return activity?.getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE)
    }

    private fun getToken(): String? {
        return getSharedPreferences()?.getString(MainActivity.TOKEN_EXTRA, null)
    }

    private fun getIsTokenValid(): Boolean? {
        return getSharedPreferences()?.getBoolean(MainActivity.IS_TOKEN_VALID, true)
    }

    private fun getIsSessionActive(): Boolean? {
        return getSharedPreferences()?.getBoolean(IS_SESSION_ACTIVE, false)
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat
            .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun getPermission() {
        ActivityCompat.requestPermissions(
            requireActivity() as Activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            1
        )
    }

    override fun onSuccess(result: TracksList) {
        val db = TracksAndNotificationsDatabase(requireContext())
        val token = getToken()
        getSharedPreferences()?.edit()?.putBoolean(IS_SESSION_ACTIVE, true)?.apply()
        token?.let { db.refreshData(result.tracks, token) }
        tracksRecyclerViewAdapter.updateList(
            db.getTracksList().sortedByDescending { it.beginsAt })
        db.close()
        progressDialog.dismiss()
        isRefreshing = false
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onError(error: String) {
        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        progressDialog.dismiss()
        swipeRefreshLayout.isRefreshing = false
    }
}
