package com.example.libraryapp

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.librarylink.Constants
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale

class Myapplication : Application() {

    companion object {

        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            // format dd/MM/yy
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        // function to get pdf size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            // using url we can get file and its metadata from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetadata ->
                    Log.d(TAG, "loadPdfSize: got metadata ")
                    val bytes = storageMetadata.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    // convert bytes to kb/mb
                    val kb = bytes / 1024
                    val mb = kb / 1024
                    if (mb >= 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)} bytes"
                    }
                }
                .addOnFailureListener { e ->
                    // failure to get metadata
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
                }
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {

            val TAG = "PDF_THUMBNNAIL_TAG"
            // using url we can get file and its metadata from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->

                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    // SET TO PDFVIEW
                    pdfView.fromBytes(bytes)
                        .pages(0) // show first page only
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages ->
                            Log.d(TAG, "loadPdfFromUrlSinglePage: Pages: $nbPages")
                            // pdf loaded, we can set page count, pdf thumbnail
                            progressBar.visibility = View.INVISIBLE

                            // if pagesTv param is not null then set page number
                            if (pagesTv != null) {
                                pagesTv.text = "$nbPages"
                            }
                        }
                        .load()
                }
                .addOnFailureListener { e ->

                    // failure to get metadata
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
                }
        }

        fun loadCategory(categoryId: String, categoryTv: TextView) {

            // load category using category id from firebase
            val ref = FirebaseDatabase.getInstance().getReference("categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // get category
                        val category = "${snapshot.child("category").value}"
                        // set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // handle cancellation if needed
                    }
                })
        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {
            // param details
            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: deleting..")

            // progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle ...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Deleting from storage...")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Deleted from storage")
                    Log.d(TAG, "deleteBook: Deleting from database now ")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Successfully deleted..", Toast.LENGTH_SHORT)
                                .show()
                            Log.d(TAG, "deleteBook: Deleted from db too ...")
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Log.d(
                                TAG,
                                "deleteBook: Failed to delete from the db due to ${e.message}"
                            )
                            Toast.makeText(
                                context,
                                "Failed to delete due to ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Log.d(TAG, "deleteBook: Failed to delete from the storage due to ${e.message}")
                    Toast.makeText(
                        context,
                        "Failed to delete due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        fun incrementBookViewCount(bookId: String) {
            //get current book views count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var viewcount = "${snapshot.child("viewcount").value}"

                        if (viewcount == "" || viewcount == "null") {
                            viewcount = "0"
                        }
                        // 2 increment views count
                        val newViewCount = viewcount.toLong() + 1

                        //setup data to update in db
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewcount"] = newViewCount

                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }
}