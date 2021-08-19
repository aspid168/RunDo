package ru.study.rundo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.study.rundo.R
import ru.study.rundo.models.Track
import java.text.DateFormat.getDateTimeInstance
import java.util.*

class TracksAdapter(private var tracks: List<Track>, private val onClick: (track: Track) -> Unit): RecyclerView.Adapter<TracksAdapter.ViewHolder>() {

    class ViewHolder(private val view: View, private val onClick: (track: Track) -> Unit): RecyclerView.ViewHolder(view) {
        lateinit var dateAndTimeDetails: TextView
        lateinit var distanceDetails: TextView
        lateinit var timeDetails: TextView

        fun bind(position: Int, track: Track) {
            dateAndTimeDetails = view.findViewById(R.id.dateAndTimeDetails)
            distanceDetails = view.findViewById(R.id.distanceDetails)
            timeDetails = view.findViewById(R.id.timeDetails)
            dateAndTimeDetails.text = convertDate(track.beginsAt)
            distanceDetails.text = track.distance.toString()
            timeDetails.text = (track.time / 1000.0).toString()
            view.setOnClickListener {
                onClick(track)
            }
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

        private fun convertDate(unixTime: Long): String {
            return  getDateTimeInstance().format(Date(unixTime))
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
