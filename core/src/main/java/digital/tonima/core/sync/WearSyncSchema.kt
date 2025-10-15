package digital.tonima.core.sync

/**
 * Constants that define the phone↔wear data layer schema (paths and keys).
 * Shared by app and wear modules to avoid string drift.
 */
object WearSyncSchema {
    const val PATH_EVENTS_24H = "/kairos/events24h"
    const val KEY_EVENTS = "events"
    const val KEY_ID = "id"
    const val KEY_TITLE = "title"
    const val KEY_START = "start"
    const val KEY_RECUR = "recurring"
    const val KEY_GENERATED_AT = "generated_at"
}
