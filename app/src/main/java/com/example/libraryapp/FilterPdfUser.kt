// FilterPdfUser.kt

package com.example.libraryapp

import android.widget.Filter
import com.example.librarylink.ModelPdf

class FilterPdfUser(private var filterList: ArrayList<ModelPdf>, private var adapterPdfUser: AdapterPdfUser) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val results = FilterResults()
        val filteredModels = ArrayList<ModelPdf>()

        constraint?.let {
            val searchTerm = it.toString().uppercase()
            for (model in filterList) {
                if (model.title.uppercase().contains(searchTerm)) {
                    filteredModels.add(model)
                }
            }
        }

        results.apply {
            count = filteredModels.size
            values = filteredModels
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        val filteredList = results.values as ArrayList<ModelPdf>
        adapterPdfUser.updateList(filteredList)
    }
}
