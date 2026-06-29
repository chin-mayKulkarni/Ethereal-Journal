package com.diary.ai.data.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// DailyFilePayload — the canonical JSON schema for each YYYY-MM-DD.json file
// stored in the Google Drive appDataFolder.
//
// One file per calendar day, per user. The file is owned by the user's Drive
// account, so the userId does NOT appear in the payload — it is implied by the
// OAuth context of the request.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Top-level wrapper that is serialized to / deserialized from a Drive file
 * named exactly [dateString].json (e.g. "2026-06-28.json").
 *
 * @property dateString    The calendar date this file represents (YYYY-MM-DD).
 * @property lastUpdated   The MAX of all [NoteEntryItem.lastModified] values in
 *                         [entries]. Stored as a convenience field so the sync
 *                         worker can compare freshness without deserializing the
 *                         full [entries] list.
 * @property entries       The ordered list of diary entries for this date.
 */
@Serializable
data class DailyFilePayload(
    @SerialName("dateString")  val dateString: String,
    @SerialName("lastUpdated") val lastUpdated: Long,
    @SerialName("entries")     val entries: List<NoteEntryItem>
)

/**
 * Represents a single diary entry within a [DailyFilePayload].
 *
 * This is intentionally a flat DTO — it mirrors [NoteEntity] fields that
 * are meaningful in a cross-device context. Fields that are device-local
 * (e.g. userId, which is re-attached during upsert) are excluded.
 *
 * @property id           UUID v4 — stable across devices; acts as the
 *                        primary key during upsert reconciliation.
 * @property content      The raw text content of the diary entry.
 * @property mediaType    One of "TEXT", "VOICE", "PHOTO_OCR" — preserved
 *                        so the UI can render the correct entry type icon.
 * @property mediaPath    Nullable path to the associated media asset.
 *                        The actual media file sync is handled separately;
 *                        this field is a reference only.
 * @property lastModified Unix epoch milliseconds (UTC). Used by the LWW
 *                        (Last Write Wins) conflict resolution algorithm:
 *                        the entry with the highest [lastModified] wins.
 * @property syncStatus   Snapshot of the sync state at the time of upload.
 *                        Always written as "SYNCHRONIZED" in uploaded files.
 */
@Serializable
data class NoteEntryItem(
    @SerialName("id")           val id: String,
    @SerialName("content")      val content: String,
    @SerialName("mediaType")    val mediaType: String,
    @SerialName("mediaPath")    val mediaPath: String?,
    @SerialName("lastModified") val lastModified: Long,
    @SerialName("syncStatus")   val syncStatus: String = "SYNCHRONIZED"
)
