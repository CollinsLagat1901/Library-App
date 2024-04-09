// AdapterPdfUser.kt

package com.example.libraryapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.libraryapp.databinding.RowPdfUserBinding
import com.example.librarylink.ModelPdf

class AdapterPdfUser(
    private val context: Context,
    private var pdfArrayList: ArrayList<ModelPdf>
) : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>(), Filterable {

    private lateinit var binding: RowPdfUserBinding
    private var filter: FilterPdfUser? = null
    private var filterList: ArrayList<ModelPdf> = pdfArrayList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfUser(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.description
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        //convert time
        val date = Myapplication.formatTimeStamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date

        Myapplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)

        Myapplication.loadCategory(categoryId, holder.categoryTv)

        Myapplication.loadPdfSize(url, title, holder.sizeTv)

        //handle click open pdf details page
        holder.itemView.setOnClickListener {
            //pass book id
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfUser(pdfArrayList, this)
        }
        return filter as FilterPdfUser
    }

    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
    }

    fun updateList(newList: ArrayList<ModelPdf>) {
        pdfArrayList = newList
        filterList = newList
        notifyDataSetChanged()
    }
}
