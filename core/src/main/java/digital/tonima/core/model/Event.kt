package digital.tonima.core.model

data class Event(
    val id: Long,
    val title: String,
    val startTime: Long,
    var isAlarmEnabled: Boolean = false,
    val isRecurring: Boolean = false
) {
    val uniqueIntentId: Int
        get() = (id.toString() + startTime.toString()).hashCode()
}
