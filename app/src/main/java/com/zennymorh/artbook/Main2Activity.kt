package com.zennymorh.artbook

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.ByteArrayOutputStream
import java.util.jar.Manifest

class Main2Activity : AppCompatActivity() {

    var selectedImage : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val intent = intent
        val info = intent.getStringExtra("info")

        //we're checking to see if it is a new page or the details have been previously saved
        if (info.equals("new")){
            val background = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.placeholder)
            imageView2.setImageBitmap(background)
            button.visibility = View.VISIBLE

            editText.setText("")
        } else{
            val name = intent.getStringExtra("name")
            editText.setText(name)

            val artists = intent.getStringExtra("artist")
            artist.setText(artists)

            val chosen = Globals.Chosen
            val bitmap = chosen.returnImage()

            imageView2.setImageBitmap(bitmap)

            button.visibility = View.INVISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun select(view: View){
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),2)
        } else{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 2){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 1)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null){

            val image = data.data

            try{
                selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver, image)
                imageView2.setImageBitmap(selectedImage)
            } catch (e: Exception){
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun save(view: View) {
        val artName = editText.text.toString()
        val artistName = artist.text.toString()

        val outputStream = ByteArrayOutputStream()

        selectedImage?.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
        val byteArray = outputStream.toByteArray()

        try{
            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null)
            database.execSQL("CREATE TABLE IF NOT EXISTS Arts (name VARCHAR, artist VARCHAR, image BLOB)")
            val sqlString = "INSERT INTO Arts (name, artist, image) VALUES (?, ?, ?) "
            val statement = database.compileStatement(sqlString)

            statement.bindString(1, artName)
            statement.bindString(2,artistName)
            statement.bindBlob(3, byteArray)

            statement.execute()
        } catch (e:Exception){
            e.printStackTrace()
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }
}