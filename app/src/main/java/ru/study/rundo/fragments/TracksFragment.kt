package ru.study.rundo.fragments

import android.app.ProgressDialog
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.study.rundo.*
import ru.study.rundo.activities.MainActivity
import ru.study.rundo.activities.RunActivity
import ru.study.rundo.adapters.TracksAdapter
import ru.study.rundo.interfaces.Handler
import ru.study.rundo.interfaces.MapSwitcher
import ru.study.rundo.models.TracksList

class TracksFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tracks, container, false)
    }

    lateinit var add: FloatingActionButton
    lateinit var tracksRecyclerView: RecyclerView
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var tracksRecyclerViewAdapter: TracksAdapter
    lateinit var progressDialog: ProgressDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        add = view.findViewById(R.id.add)
        tracksRecyclerView = view.findViewById(R.id.tracksRecyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutTracks)

        val tracksDatabase = TracksDatabase(requireContext())
        val tracksList = tracksDatabase.getTracksList()
        tracksDatabase.close()
        val sharedPreferences =
            activity?.getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE)
        val isTokenValid =
            sharedPreferences?.getBoolean(MainActivity.IS_TOKEN_VALID, true)
        val isSessionActive =
            sharedPreferences?.getBoolean(MainActivity.IS_SESSION_ACTIVE, false)

        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tracksRecyclerView.adapter = TracksAdapter(tracksList) {
            if (activity is MapSwitcher) {
                (activity as MapSwitcher).switchToMap(it)
            }
        }
        tracksRecyclerViewAdapter = tracksRecyclerView.adapter as TracksAdapter

        if (isTokenValid == false) {
            add.hide()
        }
        if (isSessionActive == false) {
            sharedPreferences.edit().putBoolean(MainActivity.IS_SESSION_ACTIVE, true).apply()
            progressDialog = ProgressDialog(requireActivity())
            progressDialog.show()
            updateTrackList(dismissProgressDialog = { progressDialog.dismiss() })
        }

        swipeRefreshLayout.setOnRefreshListener {
            updateTrackList(stopRefresh = { swipeRefreshLayout.isRefreshing = false })
        }

        add.setOnClickListener {
            RunActivity.startActivity(view.context)
        }
    }

    private fun updateTrackList(
        dismissProgressDialog: (() -> Unit)? = null,
        stopRefresh: (() -> Unit)? = null
    ) {
        val workWithServer = WorkWithServer(requireContext())
        val tracksDatabase = TracksDatabase(requireContext())
        workWithServer.addListenerGetTracks(object : Handler<TracksList> {
            override fun onSuccess(result: TracksList) {
                tracksDatabase.refreshData(result.tracks)
                tracksRecyclerViewAdapter.updateList(tracksDatabase.getTracksList())
                dismissProgressDialog?.let { it() }
                stopRefresh?.let { it() }
            }

            override fun onError(error: String) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                dismissProgressDialog?.let { it() }
                stopRefresh?.let { it() }
            }
        })
        workWithServer.getTracks()
    }

    override fun onPause() {
        super.onPause()
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
}

