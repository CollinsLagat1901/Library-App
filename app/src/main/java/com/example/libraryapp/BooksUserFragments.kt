package com.example.libraryapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.libraryapp.databinding.FragmentBooksUserBinding
import com.example.librarylink.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class BooksUserFragments : Fragment{

    //view binding fragment
    private lateinit var binding: FragmentBooksUserBinding

    public companion object{
        private const val TAG = " BOOKS_USER_TAG"

        //receiving data from activity to ;load books e.g, categoryId, uid
        public fun newInsatance(categoryId: String, category: String, uid: String) : BooksUserFragments{
            val fragment = BooksUserFragments()
            //put data to bundle
            val args =Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments =args
            return fragment
        }
    }

    private var categoryId =""
    private var category =""
    private var uid =""

   private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get arguments that we passed in the new instance
        val args = arguments
        if (args !=null){
            categoryId =args.getString("categoryId")!!
            category =args.getString("category")!!
             uid =args.getString("uid")!!
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding =FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        //load pdf
        Log.d(TAG, "onCreateView: Category: $category")
        if (category =="All"){
            //load all books

            loadAllBooks()
        }
        else if (category =="Most Viewed"){
            //load most viewd books
            loadMostViewedDownloadedBooks("viewcount")
        }
        else if (category == "Most Downloaded")
        {
            //load most download books
        loadMostViewedDownloadedBooks("downloadsCount")
        }
        else{
            loadCategorizedBooks()
        }

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    // Implement your search logic here
                    adapterPdfUser.filter.filter(s)
                } catch (e: Exception) {
                    Log.d(TAG, "onTextChanged: SEARCH EXCEPTION: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        return binding.root
    }

    private fun loadAllBooks() {

        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object  :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before start adding data into it
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add list
                    pdfArrayList.add(model!!)

                }
                adapterPdfUser =AdapterPdfUser(context!!, pdfArrayList)

                //set adapter to recyclerview
                binding.booksRv.adapter =adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadCategorizedBooks() {

        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
       ref.orderByChild("categoryId").equalTo(categoryId)
           .addValueEventListener(object  :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list before start adding data into it
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)
                        //add list
                        pdfArrayList.add(model!!)

                    }
                    adapterPdfUser =AdapterPdfUser(context!!, pdfArrayList)

                    //setadapter to recyclerview
                    binding.booksRv.adapter =adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10) //load most viewed or most downloaded books .
            .addValueEventListener(object  :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before start adding data into it
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add list
                    pdfArrayList.add(model!!)

                }
                adapterPdfUser =AdapterPdfUser(context!!, pdfArrayList)

                //setadapter to recyclerview
                binding.booksRv.adapter =adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}