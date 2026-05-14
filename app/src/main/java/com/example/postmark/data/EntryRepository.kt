package com.example.postmark.data

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

    val supabase = createSupabaseClient(
        supabaseUrl = "https://rebbansdrlkzbgndhobe.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJlYmJhbnNkcmxremJnbmRob2JlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg2NjY3NDcsImV4cCI6MjA5NDI0Mjc0N30.r1CoFxjaLUUPTvTY5axaEXfudKXm4WPirgBGrB4AUUs"
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

    suspend fun uploadPhoto(uri: Uri, contentResolver: android.content.ContentResolver): String = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        val name = "${UUID.randomUUID()}.jpg"
        val path = "$uid/$name"

        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Could not read image bytes")

        supabase.storage
            .from("photos")
            .upload(path, bytes, upsert = false)

        supabase.storage
            .from("photos")
            .publicUrl(path)
    }

    suspend fun delete(entryId: String) {
        entriesRef().document(entryId).delete().await()
    }

    suspend fun deleteAll() {
        // Firestore doesn't have a true "delete collection" — fetch and batch.
        val snap = entriesRef().get().await()
        val batch = db.batch()
        snap.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }
}
