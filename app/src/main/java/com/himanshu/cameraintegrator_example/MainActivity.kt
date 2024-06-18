package com.himanshu.cameraintegrator_example

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.himanshu.cameraintegrator.ImageCallback
import com.himanshu.cameraintegrator.ImagesSizes
import com.himanshu.cameraintegrator.integrator.CameraIntegrator
import com.himanshu.cameraintegrator.integrator.GalleryIntegrator
import com.himanshu.cameraintegrator.storage.StorageMode

class MainActivity : AppCompatActivity() {
    private lateinit var image : ImageView
    private lateinit var cameraBtn : View
    private lateinit var galleryBtn : ImageView

    private val cameraIntegrator : CameraIntegrator by lazy {
        CameraIntegrator(this).apply {
            this.setRequiredImageSize(ImagesSizes.OPTIMUM_BIG)
            this.setImageDirectoryName("camera_gallery_integrator_pics")
            this.setStorageMode(StorageMode.INTERNAL_FILE_STORAGE)
        }
    }

    private val galleryIntegrator : GalleryIntegrator by lazy {
        GalleryIntegrator(this).apply {
            this.setRequiredImageSize(ImagesSizes.OPTIMUM_BIG)
            this.setImageDirectoryName("camera_gallery_integrator_pics")
            this.setStorageMode(StorageMode.INTERNAL_FILE_STORAGE)
        }
    }

    private val imageCall = ImageCallback{requestedBy, result,err ->

        err?.printStackTrace()

        if(result != null){
            Glide.with(this).load(result.bitmap).into(image)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViews()
    }

    private fun findViews() {
        image = findViewById(R.id.image_view)
        findViewById<View>(R.id.camera_btn_1).apply {
            setOnClickListener {
                cameraIntegrator.initiateCapture()
            }
        }

        findViewById<View>(R.id.gallery_btn_1).apply {
            setOnClickListener {
                galleryIntegrator.initiateImagePick()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CameraIntegrator.REQUEST_IMAGE_CAPTURE) {
            cameraIntegrator.parseResults(requestCode, resultCode, data, imageCall)
        } else if(requestCode == GalleryIntegrator.REQUEST_IMAGE_PICK){
            galleryIntegrator.parseResults(requestCode, resultCode, data, imageCall)
        }
    }
}