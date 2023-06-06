package helper

object MessageHelper {
    val mMessageUtil = MessageHandler()
    val mLogUtil = MessageHandler()

    class MessageHandler {
        private val messageListenerArrayList: ArrayList<MessageListener> = ArrayList()
        fun send(message: Message) {
            messageListenerArrayList.map {
                it.handle(message)
            }
        }

        fun addListener(handle: (Message) -> Unit) {
            messageListenerArrayList.add(object : MessageListener {
                override fun handle(message: Message) {
                    handle(message)
                }
            })
        }
    }

    interface MessageListener {
        fun handle(message: Message)
    }

    class Message(
        val string: String = "",
        val args: ArrayList<Any>? = null,
    )
}