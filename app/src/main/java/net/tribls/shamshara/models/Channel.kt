package net.tribls.shamshara.models

/**
 * Represents a channel where chats take place
 * From the API we'll be getting
 * @param name  the name of the channel
 * @param description - the description of the channl
 * @param id - the ID of the channel
 * **/
class Channel(private val name: String,
              private val description: String,
              private val id: String){

    /**
     * Because we'll be working with a list view, we'll use the list of channels and use toString to display the name
     */
    override fun toString(): String {
        return "#$name"
    }
}