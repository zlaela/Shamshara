package net.tribls.shamshara.adapter

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.tribls.shamshara.R
import net.tribls.shamshara.models.Message
import net.tribls.shamshara.services.UserDataService
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private val context: Context,
    private val messages: ArrayList<Message>
) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, messages[position])
    }

    override fun getItemCount(): Int = messages.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message_list_item, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userImage = itemView.findViewById<AppCompatImageView>(R.id.user_icon)
        private val messageSender = itemView.findViewById<AppCompatTextView>(R.id.message_sender)
        private val messageDate = itemView.findViewById<AppCompatTextView>(R.id.message_date)
        private val messageBody = itemView.findViewById<AppCompatTextView>(R.id.message_text)

        fun bind(context: Context, message: Message) {
            // Bind the message to the view
            val resourceId = context.resources.getIdentifier(message.userAvatar, "drawable", context.packageName)
            userImage.setImageResource(resourceId)
            userImage.setBackgroundColor(UserDataService.getAvatarColor())
            messageSender.text = message.userName
            messageDate.text = getFriendlyTime(message.timeStamp)
            messageBody.text = message.messageBody
        }


        private fun getFriendlyTime(isoString:String) : String{
            val isoFormatter = SimpleDateFormat("MM-dd-yyy 'T' HH:mm:ss.S 'Z'", Locale.getDefault())
            isoFormatter.timeZone = TimeZone.getTimeZone("UTC")

            // Parse out converted date
            var convertedDate = Date()
            try {
                convertedDate = isoFormatter.parse(isoString)
            } catch (exception: ParseException){
                Log.d(TAG, "Cannot parse date")
            }

            val friendlyDate = SimpleDateFormat("E, h:mm a", Locale.getDefault())
            return friendlyDate.format(convertedDate)
        }
    }

    companion object {
        private val TAG = MessagesAdapter::class.java.canonicalName?: "MessagesAdapter"
    }
}