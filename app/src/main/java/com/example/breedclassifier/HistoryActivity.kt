package com.example.breedclassifier

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val listView: ListView = findViewById(R.id.listViewHistory)

        // Get SharedPreferences
        val sharedPrefs = getSharedPreferences("breed_prefs", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPrefs.getString("prediction_history", null)

        // Deserialize JSON to List<String>
        val type = object : TypeToken<List<String>>() {}.type
        val history: List<String> = if (json != null) {
            gson.fromJson(json, type)
        } else {
            listOf() // empty list if no history
        }

        // Show history in ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, history.reversed())
        // reversed() so most recent predictions appear first
        listView.adapter = adapter
    }
}
