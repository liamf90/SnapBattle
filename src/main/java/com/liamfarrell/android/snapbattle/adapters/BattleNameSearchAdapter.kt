package com.liamfarrell.android.snapbattle.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.SuggestionsResponse
import java.util.ArrayList

class BattleNameSuggestionsAdapter(context: Context, var battleNameList: List<SuggestionsResponse>) : ArrayAdapter<SuggestionsResponse>(context, 0, battleNameList) {

    private val mFilter = object : Filter() {
        override fun convertResultToString(resultValue: Any): String {
            return (resultValue as SuggestionsResponse).battleName
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            if (constraint != null) {
                val suggestions = ArrayList<SuggestionsResponse>()
                for (suggestion in  battleNameList) {
                    // Note: change the "contains" to "startsWith" if you only want starting matches
                    if (suggestion.getBattleName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(suggestion)
                    }
                }
                results.values = suggestions
                results.count = suggestions.size
            }
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
            clear()
            if (results != null && results.count > 0) {
                // we have filtered results
                addAll(results.values as ArrayList<SuggestionsResponse>)
            } else {
                // no filter, add entire original list back in
                addAll(battleNameList)
            }
            notifyDataSetChanged()
        }
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: (context as Activity).layoutInflater.inflate(
                R.layout.list_item_search_battle, parent, false)
        val battleNameTextView = convertView?.findViewById<TextView>(R.id.battleNameTextView)
        val battleNameCountTextView = convertView?.findViewById<TextView>(R.id.battleNameCountTextView)
        val battleSuggestion = getItem(position)
        battleNameTextView?.text = battleSuggestion!!.battleName
        battleNameCountTextView?.setText(context.getResources().getQuantityString(R.plurals.battle_count, battleSuggestion.count, battleSuggestion.count))
        return view!!
    }

    override fun getFilter(): Filter {
        return mFilter
    }


}