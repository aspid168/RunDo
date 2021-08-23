package ru.study.rundo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.study.rundo.R
import ru.study.rundo.models.Track
import java.text.DateFormat.getDateInstance
import java.text.DateFormat.getDateTimeInstance
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class TracksAdapter(private var tracks: List<Track>, private val onClick: (track: Track) -> Unit) :
    RecyclerView.Adapter<TracksAdapter.ViewHolder>() {

    class ViewHolder(private val view: View, private val onClick: (track: Track) -> Unit) :
        RecyclerView.ViewHolder(view) {
        lateinit var dateAndTimeDetails: TextView
        lateinit var distanceDetails: TextView
        lateinit var timeDetails: TextView

        fun bind(position: Int, track: Track) {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(track.time)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(track.time) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(track.time)
            )
            val distance = track.distance.roundToInt()
            dateAndTimeDetails = view.findViewById(R.id.dateAndTimeDetails)
            distanceDetails = view.findViewById(R.id.distanceDetails)
            timeDetails = view.findViewById(R.id.timeDetails)
            dateAndTimeDetails.text = convertDate(track.beginsAt)
            distanceDetails.text = view.resources.getString(R.string.distanceDetails, distance)
            timeDetails.text = view.resources.getString(R.string.timeDetails, minutes, seconds)
            view.setOnClickListener {
                onClick(track)
            }
            setBackgroundColor(position)
        }

        private fun convertDate(unixTime: Long): String {
            return getDateTimeInstance(3, 3).format(Date(unixTime))
        }

        private fun setBackgroundColor(position: Int) {
            if (position % 2 == 0) {
                view.setBackgroundColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.trackItemBackground
                    )
                )
            } else {
                view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.white))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_adapter_element, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, tracks[position])
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    fun updateList(tracks: List<Track>) {
        this.tracks = tracks
        notifyDataSetChanged()
    }
}
