package com.braver.googlefit

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.braver.googlefit.databinding.AdapterGoogleFitBinding


class GoogleFitAdapter(
    private val googleFitDataItemList: List<GoogleFitDataModel>
) :
    RecyclerView.Adapter<GoogleFitAdapter.GoogleFitSyncViewHolder>() {
    private lateinit var context: Context
    override fun getItemCount() = googleFitDataItemList.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GoogleFitSyncViewHolder {
        context = parent.context
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding: AdapterGoogleFitBinding =
            AdapterGoogleFitBinding.inflate(layoutInflater, parent, false)
        return GoogleFitSyncViewHolder(itemBinding)
    }


    override fun onBindViewHolder(holder: GoogleFitSyncViewHolder, position: Int) {
        val googleFitDataModel: GoogleFitDataModel = googleFitDataItemList[position]
        holder.adapterGoogleFitBinding.moduleTitleTextView.text =
            googleFitDataModel.moduleName
        holder.adapterGoogleFitBinding.dateTextView.text =
            googleFitDataModel.createdDate
        // Value layout One
        if (googleFitDataModel.dataTypeOne.isNotEmpty()) {
            holder.adapterGoogleFitBinding.valueOneTitleTextView.text =
                googleFitDataModel.dataTypeOne
            holder.adapterGoogleFitBinding.valueOneDataTextView.text =
                googleFitDataModel.dataValueOne
            holder.adapterGoogleFitBinding.valueOneMeasureTextView.text =
                googleFitDataModel.dataOneMeasure
            holder.adapterGoogleFitBinding.valueOneTitleTextView.visibility = View.VISIBLE
            holder.adapterGoogleFitBinding.valueOneDataTextView.visibility = View.VISIBLE
            holder.adapterGoogleFitBinding.valueOneMeasureTextView.visibility = View.VISIBLE
        } else {
            holder.adapterGoogleFitBinding.valueOneTitleTextView.visibility = View.GONE
            holder.adapterGoogleFitBinding.valueOneDataTextView.visibility = View.GONE
            holder.adapterGoogleFitBinding.valueOneMeasureTextView.visibility = View.GONE
        }

        // Value layout Two
        if (googleFitDataModel.dataTypeTwo.isNotEmpty()) {
            holder.adapterGoogleFitBinding.valueTwoTitleTextView.text =
                googleFitDataModel.dataTypeTwo
            holder.adapterGoogleFitBinding.valueTwoDataTextView.text =
                googleFitDataModel.dataValueTwo
            holder.adapterGoogleFitBinding.valueTwoMeasureTextView.text =
                googleFitDataModel.dataTwoMeasure
            holder.adapterGoogleFitBinding.valueTwoTitleTextView.visibility = View.VISIBLE
            holder.adapterGoogleFitBinding.valueTwoDataTextView.visibility = View.VISIBLE
            holder.adapterGoogleFitBinding.valueTwoMeasureTextView.visibility = View.VISIBLE
        } else {
            holder.adapterGoogleFitBinding.valueTwoTitleTextView.visibility = View.GONE
            holder.adapterGoogleFitBinding.valueTwoDataTextView.visibility = View.GONE
            holder.adapterGoogleFitBinding.valueTwoMeasureTextView.visibility = View.GONE
        }

        // Value layout Three
        if (googleFitDataModel.dataTypeThree.isNotEmpty()) {
            holder.adapterGoogleFitBinding.valueThreeTitleTextView.text =
                googleFitDataModel.dataTypeThree
            holder.adapterGoogleFitBinding.valueThreeDataTextView.text =
                googleFitDataModel.dataValueThree
            holder.adapterGoogleFitBinding.valueThreeMeasureTextView.text =
                googleFitDataModel.dataThreeMeasure
            holder.adapterGoogleFitBinding.valueThreeTitleTextView.visibility = View.VISIBLE
            holder.adapterGoogleFitBinding.valueThreeDataTextView.visibility = View.VISIBLE
            holder.adapterGoogleFitBinding.valueThreeMeasureTextView.visibility = View.VISIBLE
        } else {
            holder.adapterGoogleFitBinding.valueThreeTitleTextView.visibility = View.GONE
            holder.adapterGoogleFitBinding.valueThreeDataTextView.visibility = View.GONE
            holder.adapterGoogleFitBinding.valueThreeMeasureTextView.visibility = View.GONE
        }

        // Value layout Four
        if (googleFitDataModel.dataTypeFour.isNotEmpty()) {
            holder.adapterGoogleFitBinding.valueFourTitleTextView.text =
                googleFitDataModel.dataTypeFour
            holder.adapterGoogleFitBinding.valueFourDataTextView.text =
                googleFitDataModel.dataValueFour
            holder.adapterGoogleFitBinding.valueFourMeasureTextView.text =
                googleFitDataModel.dataFourMeasure
            holder.adapterGoogleFitBinding.valueFourTitleTextView.visibility = View.VISIBLE
            holder.adapterGoogleFitBinding.valueFourDataTextView.visibility = View.VISIBLE
            holder.adapterGoogleFitBinding.valueFourMeasureTextView.visibility = View.VISIBLE
        } else {
            holder.adapterGoogleFitBinding.valueFourTitleTextView.visibility = View.GONE
            holder.adapterGoogleFitBinding.valueFourDataTextView.visibility = View.GONE
            holder.adapterGoogleFitBinding.valueFourMeasureTextView.visibility = View.GONE
        }
    }

    inner class GoogleFitSyncViewHolder(val adapterGoogleFitBinding: AdapterGoogleFitBinding) :
        RecyclerView.ViewHolder(adapterGoogleFitBinding.root)
}