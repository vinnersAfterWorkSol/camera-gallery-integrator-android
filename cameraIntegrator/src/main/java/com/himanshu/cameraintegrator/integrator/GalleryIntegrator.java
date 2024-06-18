package com.himanshu.cameraintegrator.integrator;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.himanshu.cameraintegrator.ImageCallback;
import com.himanshu.cameraintegrator.ImagesSizes;
import com.himanshu.cameraintegrator.RequestSource;
import com.himanshu.cameraintegrator.Result;
import com.himanshu.cameraintegrator.exceptions.RuntimePermissionNotGrantedException;
import com.himanshu.cameraintegrator.storage.ImageStorageHelper;
import com.himanshu.cameraintegrator.storage.StorageMode;

import java.io.File;
import java.io.FileNotFoundException;


/**
 * Created by Himanshu on 4/30/2018.
 */

public class GalleryIntegrator extends Integrator {


    /**
     * Constant to be used for identifying image pick action
     */
    public static final int REQUEST_IMAGE_PICK = 2;


    private static final String INTENT_EXTRA_FILE_DIRECTORY_NAME = "gallery_image_directory_name";
    private static final String INTENT_EXTRA_FINAL_REQUIRED_SIZE = "gallery_required_size";

    /**
     * Activity Context
     */
    private Activity activityRef;

    /**
     * Reference of calling Activity
     */
    private Fragment fragmentReference;

    /**
     * Public Directory Name it can be
     * <ul>
     * <li>{@link Environment#DIRECTORY_MUSIC}</li>
     * <li>{@link Environment#DIRECTORY_PODCASTS}</li>
     * <li>{@link Environment#DIRECTORY_RINGTONES}</li>
     * <li>{@link Environment#DIRECTORY_ALARMS}</li>
     * <li>{@link Environment#DIRECTORY_NOTIFICATIONS}</li>
     * <li>{@link Environment#DIRECTORY_PICTURES}</li>     *
     * <li>{@link Environment#DIRECTORY_MOVIES}</li>
     * <li>{@link Environment#DIRECTORY_DOWNLOADS}</li>
     * <li>{@link Environment#DIRECTORY_DCIM}</li>
     * <li>{@link Environment#DIRECTORY_DOCUMENTS}</li>     *
     * </ul>
     */
    private String publicDirectoryName;

    /**
     * Directory Name Where Image will be kept
     */
    private String imageDirectoryName;


    /**
     * Required Size of the image
     * this can be one of the {@link ImagesSizes}
     */
    private @NonNull
    int requiredImageSize;

    /**
     * @param activityRef path where the new image we clicked should be stored
     */
    public GalleryIntegrator(Activity activityRef) {
        super(activityRef);
        this.activityRef = activityRef;
    }

    public GalleryIntegrator(Fragment fragmentReference) {
        super(fragmentReference.getActivity());
        this.fragmentReference = fragmentReference;
        this.activityRef = fragmentReference.getActivity();
    }

    public void setRequiredImageSize(@ImagesSizes.ImageSize int requiredImageSize) {
        this.requiredImageSize = requiredImageSize;
    }

    /**
     * Sets Public Directory Name
     *
     * @param publicDirectoryName it can be
     *                            <ul>
     *                            <li>{@link Environment#DIRECTORY_MUSIC}</li>
     *                            <li>{@link Environment#DIRECTORY_PODCASTS}</li>
     *                            <li>{@link Environment#DIRECTORY_RINGTONES}</li>
     *                            <li>{@link Environment#DIRECTORY_ALARMS}</li>
     *                            <li>{@link Environment#DIRECTORY_NOTIFICATIONS}</li>
     *                            <li>{@link Environment#DIRECTORY_PICTURES}</li>
     *                            <li>{@link Environment#DIRECTORY_MOVIES}</li>
     *                            <li>{@link Environment#DIRECTORY_DOWNLOADS}</li>
     *                            <li>{@link Environment#DIRECTORY_DCIM}</li>
     *                            <li>{@link Environment#DIRECTORY_DOCUMENTS}</li>
     *                            </ul>
     */
    public void setPublicDirectoryName(String publicDirectoryName) {
        this.publicDirectoryName = publicDirectoryName;
    }

    public void setImageDirectoryName(String directoryName) {
        this.imageDirectoryName = directoryName;
    }

    /**
     * Initiates Image Pick Process
     *
     * @throws ActivityNotFoundException if There is no application to pick image
     */
    public void initiateImagePick() throws ActivityNotFoundException, RuntimePermissionNotGrantedException {

        // Read Storage is required to pick image Until Android 12
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            checkForReadStoragePermissions();
        }

        //Checking Write Permission If Output File is Supposed to be stored in the External Storage
        if (storageMode == StorageMode.EXTERNAL_CACHE_STORAGE || storageMode == StorageMode.EXTERNAL_PUBLIC_STORAGE)
            checkForWriteStoragePermissions();

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        if (fragmentReference == null)
            activityRef.startActivityForResult(intent, REQUEST_IMAGE_PICK);
        else
            fragmentReference.startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }


    @Override
    public void parseResults(int requestCode, int resultCode, Intent data, ImageCallback resultCallback) {

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            Uri selectedImageUri = data.getData();

            if (selectedImageUri != null) {
                String imagePath = getRealPathFromURI(activityRef, selectedImageUri);


                if (imagePath != null) {


                    taskExecutors.diskIO().execute(new Runnable() {
                        @Override
                        public void run() {

                            //Reference to file picked By User
                            File mFile = new File(imagePath);
                            File destFile = null;

                            switch (storageMode) {
                                case INTERNAL_FILE_STORAGE:

                                    //Creating A File In Internal Storage which app will use
                                    if (imageDirectoryName != null)
                                        destFile = ImageStorageHelper.createInternalImageFile(mContext, imageDirectoryName, mFile.getName());
                                    else
                                        destFile = ImageStorageHelper.createInternalImageFile(mContext, mFile.getName());

                                    break;

                                case INTERNAL_CACHE_STORAGE:

                                    //Creating A File In Internal Storage which app will use
                                    if (imageDirectoryName != null)
                                        destFile = ImageStorageHelper.createCacheImageFile(mContext, imageDirectoryName, mFile.getName());
                                    else
                                        destFile = ImageStorageHelper.createCacheImageFile(mContext, mFile.getName());

                                    break;

                                case EXTERNAL_CACHE_STORAGE:

                                    //Creating A File In Internal Storage which app will use
                                    if (imageDirectoryName != null)
                                        destFile = ImageStorageHelper.createExternalCacheImageFile(mContext, imageDirectoryName + "/" + mFile.getName());
                                    else
                                        destFile = ImageStorageHelper.createExternalCacheImageFile(mContext, mFile.getName());

                                    break;

                                case EXTERNAL_FILE_STORAGE:

                                    //Creating A File In Internal Storage which app will use
                                    if (imageDirectoryName != null)
                                        destFile = ImageStorageHelper.createExternalImageFile(mContext, imageDirectoryName, mFile.getName());
                                    else
                                        destFile = ImageStorageHelper.createExternalImageFile(mContext, mFile.getName());

                                    break;
                                case EXTERNAL_PUBLIC_STORAGE: {

                                    File storageDir;

                                    // Getting a reference to Target storage directory
                                    if (publicDirectoryName != null) {

                                        if (imageDirectoryName != null)
                                            storageDir = Environment.getExternalStoragePublicDirectory(publicDirectoryName + "/" + imageDirectoryName);
                                        else
                                            storageDir = Environment.getExternalStoragePublicDirectory(publicDirectoryName);

                                    } else
                                        storageDir = Environment.getExternalStoragePublicDirectory(imageDirectoryName);

                                    // Creating directory if not made already

                                    if (!storageDir.exists())
                                        storageDir.mkdirs();

                                    destFile = new File(storageDir, mFile.getName());
                                }
                            }


                            //Getting Bitmap Of Required Size
                            Bitmap requiredSizeImage;
                            try {
                                requiredSizeImage = getBitmapInRequiredSize(mFile, requiredImageSize);
                            } catch (FileNotFoundException e) {
                                resultCallback.onResult(RequestSource.SOURCE_GALLERY, null, e);
                                return;
                            }

                            // Saving the Bitmap of required size to the required directory
                            ImageStorageHelper.saveTo(destFile, requiredSizeImage);

                            //Preparing Results object
                            Result results = getResults(destFile, requiredSizeImage, RequestSource.SOURCE_GALLERY);

                            //Delivering Results back to main thread
                            taskExecutors.mainThread().execute(() -> resultCallback.onResult(RequestSource.SOURCE_GALLERY, results, null));
                        }
                    });

                }
            }
        }

    }

    @Override
    public void saveState(Bundle outState) {
        outState.putString(INTENT_EXTRA_FILE_DIRECTORY_NAME, imageDirectoryName);
        outState.putInt(INTENT_EXTRA_FINAL_REQUIRED_SIZE, requiredImageSize);
    }

    @Override
    public void restoreState(Bundle savedInstanceState) {
        imageDirectoryName = savedInstanceState.getString(INTENT_EXTRA_FILE_DIRECTORY_NAME, null);
        requiredImageSize = savedInstanceState.getInt(INTENT_EXTRA_FINAL_REQUIRED_SIZE, -1);
    }


    /**
     * Parses #contentURI and returns Absolute path of image
     *
     * @param c
     * @param contentURI
     * @return
     */
    private String getRealPathFromURI(Context c, Uri contentURI) {
        Cursor cursor = c.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            // path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String path = cursor.getString(idx);
            cursor.close();
            return path;
        }
    }

}