package com.zennymorh.artbook

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
//funny thing i learnt while building this application is that a database like sql cannot take a large size of data,
//even when i compressed the images, the list view i was displaying my results in still could not take more than 4 items,
// but when i didn't add images, just strings, it could display over 12 items!! what a wawu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val artNameArray = ArrayList<String>()
        val artistNameArray = ArrayList<String>()
        val artImageArray = ArrayList<Bitmap>()

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, artNameArray)

        listView.adapter = arrayAdapter

        try{
            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null)
//            database.execSQL("CREATE TABLE IF NOT EXISTS arts (name VARCHAR, artist VARCHAR, image BLOB)")

            val cursor = database.rawQuery("SELECT * FROM Arts", null)

            val nameIx = cursor.getColumnIndex("name")
            val artistIx = cursor.getColumnIndex("artist")
            val imageIx = cursor.getColumnIndex("image")

            cursor.moveToFirst()

            while (cursor != null){
                artNameArray.add(cursor.getString(nameIx))
                artistNameArray.add(cursor.getString(artistIx))

                val byteArray = cursor.getBlob(imageIx)
                val image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                artImageArray.add(image)

                cursor.moveToNext()

            }
            cursor?.close()
            arrayAdapter.notifyDataSetChanged()
        } catch(e : Exception){
            e.printStackTrace()
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->

            val intent = Intent(applicationContext,Main2Activity::class.java)
            intent.putExtra("name", artNameArray[position])
            intent.putExtra("artist", artistNameArray[position])
            intent.putExtra("info","old")

            val chosen = Globals.Chosen
            chosen.chosenImage = artImageArray[position]

            startActivity(intent)
        }
    }
//this is how to create a menu. Create an android resource folder, and then a menu resource folder.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater.inflate(R.menu.add_art, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_art){
            val intent = Intent(applicationContext, Main2Activity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}