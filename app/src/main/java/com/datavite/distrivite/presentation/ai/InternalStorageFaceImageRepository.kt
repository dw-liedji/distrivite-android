package com.datavite.distrivite.presentation.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.inject.Inject

class InternalStorageFaceImageRepository @Inject constructor (private val context: Context) {

    fun createAndSaveBitmaps(userBitmap:UserBitmap){
        Log.i("distrivite-ai", "Starting saving images...")
        userBitmap.bitmaps.forEachIndexed { index, bitmap ->
            createAndSaveBitmap(userBitmap.userName, bitmap, userBitmap.userName+"$index")
        }
    }

    private fun createAndSaveBitmap(userName: String, bitmap: Bitmap, imageName: String): String? {
        // Get the app's internal storage directory
        val internalStorageDir = context.filesDir

        // Create the "face_images" directory
        val faceImagesDir = File(internalStorageDir, "face_images")
        if (!faceImagesDir.exists()) {
            faceImagesDir.mkdir() // Create the directory if it doesn't exist
        }

        // Create the user-specific subdirectory
        val userDir = File(faceImagesDir, userName)
        if (!userDir.exists()) {
            userDir.mkdir() // Create the user directory if it doesn't exist
        }

        // Create the file object for the bitmap
        val imageFile = File(userDir, "$imageName.png")

        // Write the bitmap to the file using a FileOutputStream
        return try {
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) // Compress as PNG
                Log.i("distrivite-ai", "Image $imageName save successfully")
                out.flush() // Ensure data is written
            }
            imageFile.absolutePath // Return the file path if successful
        } catch (e: IOException) {
            e.printStackTrace()
            null // Return null if there was an error
        }
    }

    fun readImagesFromInternalStorage(): List<UserBitmap> {
        Log.i("distrivite-ai", "Stat Reading face images...")

        // Get the app's internal storage directory
        val internalStorageDir = context.filesDir

        // Access the "face_images" directory
        val faceImagesDir = File(internalStorageDir, "face_images")

        // List to store the pairs of userName and bitmap
        val imagesList = mutableListOf<UserBitmap>()

        // Check if the "face_images" directory exists
        if (faceImagesDir.exists() && faceImagesDir.isDirectory) {
            // Iterate over each user directory
            faceImagesDir.listFiles()?.forEach { userDir ->
                if (userDir.isDirectory) {

                    val userName = userDir.name
                    val bitmaps = mutableListOf<Bitmap>()

                    // Iterate over each image file in the user directory
                    userDir.listFiles()?.forEach { imageFile ->
                        if (imageFile.isFile && imageFile.extension == "png") {
                            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                            if (bitmap != null) {
                                bitmaps.add(bitmap)
                                Log.i("distrivite-ai", "Reading face image ${imageFile.absoluteFile}...")
                            }
                        }
                    }

                    imagesList.add(UserBitmap(userName = userName, bitmaps = bitmaps))
                }
            }

        }

        return imagesList
    }

    fun saveSerializedImageData(data : ArrayList<Pair<String,FloatArray>> ) {
        val serializedDataFile = File(context.filesDir, "serialized_face_images")

        try {
            // Use `use` to ensure resources are closed properly
            ObjectOutputStream(FileOutputStream(serializedDataFile)).use { objectOutputStream ->
                objectOutputStream.writeObject(data)
                objectOutputStream.flush()
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., IOException)
            e.printStackTrace()
        }
    }

    fun loadSerializedImageData() : List<Pair<String, FloatArray>> {
        val serializedDataFile = File(context.filesDir, "serialized_face_images")

        // Run a safe block of code that will catch exceptions
        return runCatching {
            // Use `use` to ensure resources are closed properly
            ObjectInputStream(FileInputStream(serializedDataFile)).use { objectInputStream ->
                // Read the object and check if it is of the expected type
                val data = objectInputStream.readObject()
                if (data is ArrayList<*>) {
                    // Verify that each element is of type Pair<String, FloatArray>
                    data.filterIsInstance<Pair<String, FloatArray>>()
                } else {
                    // Return an empty list if the cast is not possible
                    emptyList<Pair<String, FloatArray>>() as ArrayList<Pair<String, FloatArray>>
                }
            }
        }.getOrElse { exception ->
            // Handle exceptions, returning an empty list if there is an error
            exception.printStackTrace()
            emptyList<Pair<String, FloatArray>>() as ArrayList<Pair<String, FloatArray>>
        }
    }


    // Use this method to save a Bitmap to the internal storage ( app-specific storage ) of your device.
    // To see the image, go to "Device File Explorer" -> "data" -> "data" -> "com.datavite.distrivite" -> "files"
    fun saveBitmapToRoot(context: Context, image: Bitmap, name: String) {
        val fileOutputStream = FileOutputStream(File(context.filesDir.absolutePath + "/$name.png"))
        image.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
    }

    // Get the image as a Bitmap from given Uri
    // Source -> https://developer.android.com/training/data-storage/shared/documents-files#bitmap
    private fun getBitmapFromUri( uri: Uri): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    // Get the image as a Bitmap from given Uri and fix the rotation using the Exif interface
    // Source -> https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
    private fun getFixedBitmap( imageFileUri : Uri) : Bitmap {
        var imageBitmap = getBitmapFromUri( imageFileUri )
        val exifInterface = ExifInterface(context.contentResolver.openInputStream(imageFileUri)!!)
        imageBitmap =
            when (exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION ,
                ExifInterface.ORIENTATION_UNDEFINED )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> BitmapUtils.rotateBitmap(imageBitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> BitmapUtils.rotateBitmap(imageBitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> BitmapUtils.rotateBitmap(imageBitmap, 270f)
                else -> imageBitmap
            }
        return imageBitmap
    }

}