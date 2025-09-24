package digital.tonima.kairos.model

data class Event(
    val id: Long,
    val title: String,
    val startTime: Long, // In milliseconds since epoch
    var isAlarmEnabled: Boolean = false
) {
    val uniqueIntentId: Int
        get() = (id.toString() + startTime.toString()).hashCode()
}
