package com.ait.postmark.data

import android.util.Log
import com.ait.postmark.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.net.Uri
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Firestore reads/writes for journal entries.
 * Supabase reads/writes for images.
 */
class EntryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private companion object { const val TAG = "EntryRepository" }

    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Storage)
    }

    private fun entriesRef() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("entries")
    } ?: throw IllegalStateException("Not signed in")

    /** Real-time stream of all entries for the current user, newest first. */
    fun observeEntries(): Flow<List<Entry>> = callbackFlow {
        val registration = entriesRef()
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(Entry::class.java)?.also { it.id = doc.id }
                }
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    suspend fun add(entry: Entry): String {
        val ref = entriesRef().add(entry).await()
        return ref.id
    }

    /**
     * Updates the editable fields of an existing entry. Uses a field map rather
     * than set() so the server-managed createdAt timestamp is left untouched.
     */
    suspend fun update(entry: Entry) {
        entriesRef().document(entry.id).update(
            mapOf(
                "date" to entry.date,
                "location" to entry.location,
                "geo" to entry.geo,
                "body" to entry.body,
                "photoUrl" to entry.photoUrl,
                "photoPath" to entry.photoPath
            )
        ).await()
    }

    suspend fun uploadPhoto(uri: Uri, contentResolver: android.content.ContentResolver): UploadedPhoto = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        val name = "${UUID.randomUUID()}.jpg"
        val path = "$uid/$name"

        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Could not read image bytes")

        supabase.storage
            .from("photos")
            .upload(path, bytes, upsert = false)

        val url = supabase.storage
            .from("photos")
            .publicUrl(path)

        UploadedPhoto(url = url, path = path)
    }

    suspend fun delete(entryId: String) {
        // Read the entry first so we can clean up its photo after the doc is gone.
        val entry = entriesRef().document(entryId).get().await().toObject(Entry::class.java)
        entriesRef().document(entryId).delete().await()
        // Delete the reference first, the object second: a dangling reference
        // (broken image) is worse than an orphaned file.
        entry?.let { deleteStoredPhoto(it.photoPath, it.photoUrl) }
    }

    suspend fun deleteAll() {
        // Firestore doesn't have a true "delete collection" — fetch and batch.
        val snap = entriesRef().get().await()
        val paths = snap.documents.mapNotNull {
            storagePath(it.toObject(Entry::class.java))
        }

        val batch = db.batch()
        snap.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()

        if (paths.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                runCatching { supabase.storage.from("photos").delete(paths) }
                    .onFailure { Log.w(TAG, "Failed to delete ${paths.size} stored photo(s)", it) }
            }
        }
    }

    /**
     * Best-effort deletion of a stored photo, resolving the storage path from
     * [path] or, for legacy entries without one, from the public [url].
     */
    suspend fun deleteStoredPhoto(path: String?, url: String? = null) {
        val target = path?.takeIf { it.isNotBlank() } ?: pathFromUrl(url) ?: return
        withContext(Dispatchers.IO) {
            runCatching { supabase.storage.from("photos").delete(target) }
                .onSuccess { Log.d(TAG, "Deleted stored photo: $target") }
                .onFailure { Log.w(TAG, "Failed to delete stored photo: $target", it) }
        }
    }

    /** Resolves an entry's storage path, falling back to its public URL for legacy entries. */
    private fun storagePath(entry: Entry?): String? =
        entry?.photoPath?.takeIf { it.isNotBlank() } ?: pathFromUrl(entry?.photoUrl)

    /** publicUrl format: <SUPABASE_URL>/storage/v1/object/public/photos/<uid>/<name> */
    private fun pathFromUrl(url: String?): String? =
        url?.substringAfter("/photos/", "")?.takeIf { it.isNotBlank() }
}

data class UploadedPhoto(val url: String, val path: String)
