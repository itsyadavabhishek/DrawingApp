package com.example.drawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    var mImageButtonCurrentpaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_DrawingApp)
        GlobalScope.launch {
            delay(2000)
        }
        setContentView(R.layout.activity_main)

        drawing_view.setSizeForBrush(20.toFloat())

        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        ib_undo.setOnClickListener {
            drawing_view.onClickUndo()
        }
        ib_save.setOnClickListener {
            if (isReadStorageAllowed()){
                BitMapAsyncTask(getBitmapFromView(fl_drawing_View_Container)).execute()
            }else{
                requestStoragepermission()
            }
        }

        ib_gallery.setOnClickListener {
            if (isReadStorageAllowed()) {

                val pickPhotoIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )

                startActivityForResult(pickPhotoIntent, GALLERY)


            } else {
                requestStoragepermission()
            }
        }

        mImageButtonCurrentpaint = ll_paint_colors[1] as ImageButton
        mImageButtonCurrentpaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )





    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main,menu)
        return true

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       when(item.itemId) {
           R.id.clear_area -> {
               //clear area func.
               drawing_view.onClickClear()
               Log.e("Tag", "onOptionsItemSelected:clear area func. ",)
               return true
           }
           R.id.redo -> {
               //fuc for redo
               drawing_view.onClickRedo()
               return true
           }
           R.id.more_colors -> {
               //fuc for more colors
               return true
           }
           R.id.change_background -> {
               //fuc for change background color


               return true
           }
           R.id.about -> {
               //fuc to tell about us
               Toast.makeText(this, "This is Simple Drawing application built for kids " +
                       "to learn about the colours and Images.  ", Toast.LENGTH_SHORT).show()
               return true
           }
           R.id.exit -> {
               finish()
               return true
           }
           R.id.image1 ->{

               iv_background.setImageResource(R.drawable.image1)
               return true
           }
           R.id.image2 ->{

               iv_background.setImageResource(R.drawable.image2)
               return true
           }
           R.id.image3 ->{

               iv_background.setImageResource(R.drawable.image3)
               return true
           }
           R.id.image4 ->{

               iv_background.setImageResource(R.drawable.image4)
               return true
           }
           R.id.image5 ->{

               iv_background.setImageResource(R.drawable.image5)
               return true
           }
           else -> return super.onOptionsItemSelected(item)
       }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                try {
                    if (data!!.data != null) {
                        iv_background.visibility = View.VISIBLE
                        iv_background.setImageURI(data.data)
                    } else {
                        Toast.makeText(
                            this,
                            "Error in Parsing the image or its corrupted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
    }

    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size")
        val smallbtn = brushDialog.ib_small
        smallbtn.setOnClickListener {
            drawing_view.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }

        val mediumbtn = brushDialog.ib_medium
        mediumbtn.setOnClickListener {
            drawing_view.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }

        val largebtn = brushDialog.ib_large
        largebtn.setOnClickListener {
            drawing_view.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    fun paintClicked(view: View) {
        if (view !== mImageButtonCurrentpaint) {
            val imageButton = view as ImageButton

            val colorTag = imageButton.tag.toString()
            drawing_view.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )
            mImageButtonCurrentpaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )
            mImageButtonCurrentpaint = view
        }
    }

    private fun requestStoragepermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
        ) {
            Toast.makeText(
                this, "Need to add Background permission",
                Toast.LENGTH_SHORT
            ).show()
        }
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), STORAGE_PERMISSION_CODE
        )

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0]
                == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    this,
                    "Permission granted you can read the storage file",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this, "oops permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(
            view.width,
            view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)
        val bgdrawable = view.background
        if (bgdrawable != null) {
            bgdrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)

        return returnedBitmap
    }

    private inner class BitMapAsyncTask(val mBitmap: Bitmap) : AsyncTask<Any, Void, String>() {
        override fun doInBackground(vararg params: Any?): String {

            var result = ""
            if (mBitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val f = File(
                        externalCacheDir!!.absoluteFile.toString() +
                                File.separator + "DrawingApp_" + System.currentTimeMillis() / 1000
                                + ".png"
                    )
                    val fos = FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    result =f.absolutePath
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!result!!.isEmpty()){
                Toast.makeText(this@MainActivity, "File save successfully : $result"
                    , Toast.LENGTH_SHORT).show()
            }else{

                Toast.makeText(this@MainActivity, "File save Unsuccessful"
                    , Toast.LENGTH_SHORT).show()

            }

            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null){
                    path , uri -> val shareIntent = Intent()
                        shareIntent.action = Intent.ACTION_SEND
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                        shareIntent.type = "image/png"

                startActivity(
                    Intent.createChooser(
                        shareIntent,"Share"
                    )
                )


            }
        }

    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2


    }
}