package com.example.libraryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.libraryapp.databinding.ActivityPdfDetailsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfDetailsBinding

    private var bookId =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_details)
        binding =ActivityPdfDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get bookId from intent

        bookId =intent.getStringExtra("bookId")!!

        // increment book view count, whenever this page starts
        Myapplication.incrementBookViewCount(bookId)

        loadDetails()
        //handle backButton, go back
        binding.backBtn.setOnClickListener {
            val intent = Intent(this, DashBoardUser::class.java)
            startActivity(intent)
        }
    }

    private fun loadDetails() {
        //books > bookId > details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${ snapshot.child("downloadsCount").value}"
                    val timestamp ="${snapshot.child("timestamp").value}"
                    val title    = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewcount ="${snapshot.child("viewcount").value}"


                    //formate date
                    val date =Myapplication.formatTimeStamp(timestamp.toLong())

                    Myapplication.loadCategory(categoryId, binding.categoryTv)

                    //load pdf thubmail, pages count
                    Myapplication.loadPdfFromUrlSinglePage("$url", "$title", binding.pdfView, binding.progressBar, binding.pagesTv)
                    Myapplication.loadPdfSize("$url", "$title", binding.sizeTv)

                    //set data
                    binding.titleTv.text =title
                    binding.descriptionTV.text =description
                    binding.viewTv.text =viewcount
                    binding.downloadTv.text =downloadsCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}