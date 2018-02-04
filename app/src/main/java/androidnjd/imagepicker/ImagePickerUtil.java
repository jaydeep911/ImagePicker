package androidnjd.imagepicker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by JD on 22-12-2017.
 */

public class ImagePickerUtil {
    private final int CAMERA_REQUEST = 4;
    private final int PICK_FROM_GALLERY = 5;

    private final int FROM_GALLERY = 54;
    private final int FROM_CAMERA = 74;


    private Activity mActivity;

    private ImagePickerCompleteListiner imagePickerCompleteListiner;

    public ImagePickerUtil(Activity activity) {
        this.mActivity = activity;
    }


    public void showImagePicker(ImagePickerCompleteListiner imagePickerCompleteListiner) {


        showImagePicker( imagePickerCompleteListiner, "Add Photo!");
    }

    public void showImagePicker(ImagePickerCompleteListiner imagePickerCompleteListiner, final String title) {
        this.imagePickerCompleteListiner = imagePickerCompleteListiner;
        Dexter.withActivity(mActivity)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
//                            Toast.makeText(mActivity, "All permissions are granted!", Toast.LENGTH_SHORT).show();
                            startImagePickProcess(title);
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(mActivity, "Error occurred! " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }


    private void startImagePickProcess(String title){

        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mActivity);
        builder.setTitle(title);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {


                    captureImageFromCamera(mActivity);
                } else if (items[item].equals("Choose from Library")) {

                    getImgeFromGallery(mActivity);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }


    protected void captureImageFromCamera(Activity activity) {


        try {
            File dir = new
                    File(Environment.getExternalStorageDirectory() + "/.temp");
            dir.mkdirs();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmmss",
                    Locale.US);
            long date = System.currentTimeMillis();
            String strDate = simpleDateFormat.format(date);
            String loadFileName = "profileimage_" + "_" + strDate + ".jpg";

            sdImageMainDirectory = new File(dir,
                    loadFileName);

            sdImageMainDirectory_Copy = new File(sdImageMainDirectory.getAbsolutePath());
            Intent cameraIntent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(sdImageMainDirectory));


            cameraIntent.putExtra("return-data", true);
            activity.startActivityForResult(cameraIntent,
                    FROM_CAMERA);
        } catch (Exception e) {
            // TODO: handle exception

        }
    }


    private String TEMP_SD_PATH;

    protected void getImgeFromGallery(Activity activity) {
        // TODO Auto-generated method stub
        File dir = new
                File(Environment.getExternalStorageDirectory() + "/.temp");
        dir.mkdirs();
        File imageFIle = new File(dir, "_" + System.currentTimeMillis() + ".jpg");
        if (imageFIle.exists()) {
            imageFIle.delete();
        }
        TEMP_SD_PATH = imageFIle.getAbsolutePath();


        try {
            Intent intent;
            if (Build.VERSION.SDK_INT < 19) {
                intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                activity.startActivityForResult(intent, FROM_GALLERY);
            } else {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                activity.startActivityForResult(intent, FROM_GALLERY);
            }
        } catch (Exception e) {
            // TODO: handle exception

        }
    }


    private File sdImageMainDirectory;
    private File sdImageMainDirectory_Copy;

    public String getPathGallery(Uri uri, Activity mActivity) {
        String nrewstr = "";
        try {
            Cursor cursor = mActivity.getContentResolver().query(uri, null,
                    null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id
                    .substring(document_id.lastIndexOf(":") + 1);

            cursor = mActivity
                    .getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            null, MediaStore.Images.Media._ID + " = ? ",
                            new String[]{document_id}, null);
            cursor.moveToFirst();
            nrewstr = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));
            Log.v("TAG", "getPathGallery nrewstr " + nrewstr);
            cursor.close();
        } catch (Exception e) {
            // TODO: handle exception

        }

        return nrewstr;
    }


    public String checkImageNeedToRotate(String imageURI) {

        try {
            /*
             * Check image is rotated or not for horizontal image
			 */
            ExifInterface exif = new ExifInterface(imageURI);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            BitmapFactory.Options options = new BitmapFactory.Options();

            // by setting this field as true, the actual bitmap pixels are not
            // loaded in the memory. Just the bounds are loaded. If
            // you try the use the bitmap here, you will get null.
            options.inJustDecodeBounds = false;
            Bitmap croppedBitmap = BitmapFactory.decodeFile(imageURI, options);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:

                    break;

                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;

                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;

                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;

                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
            }

            croppedBitmap = Bitmap.createBitmap(croppedBitmap, 0, 0,
                    croppedBitmap.getWidth(), croppedBitmap.getHeight(),
                    matrix, true);

            File imageFile = new File(imageURI);
            if (imageFile.exists()) {
                imageFile.delete();
            }

            saveMediaOnSDCardJPG(croppedBitmap, imageURI);
            croppedBitmap.recycle();
        } catch (Exception e) {
            // TODO: handle exception

        } catch (OutOfMemoryError e) {
            // TODO: handle exception

        }

        return imageURI;
    }

    public void saveMediaOnSDCardJPG(Bitmap bitmap, String fileName) {


        try {
            System.gc();
            File mediaFile = new File(fileName);

            if (mediaFile.createNewFile()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bos);
                byte[] bitmapdata = bos.toByteArray();

                // write the bytes in file
                FileOutputStream fos = new FileOutputStream(mediaFile);
                fos.write(bitmapdata);
                fos.close();
            }

            System.gc();
            // Thread.sleep(1000);
        } catch (OutOfMemoryError e) {
            System.gc();

        } catch (Exception e) {

        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            try {
                if (requestCode == FROM_GALLERY) {
                    String gallerypath_str = getPathGallery(
                            data.getData(), mActivity);
                    Uri selectedImageUri = data.getData();
                    completeSetProfilePic(gallerypath_str,
                            FROM_GALLERY);
                } else if (requestCode == FROM_CAMERA) {
                    String gallerypath_strm = checkImageNeedToRotate(sdImageMainDirectory_Copy
                            .toString());

                    completeSetProfilePic(gallerypath_strm,
                            FROM_CAMERA);


                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
        }
    }

    private String updateFilePathforUpload = "";

    private void completeSetProfilePic(String gallerypath_strm, int fromImage) {

        imagePickerCompleteListiner.setImage(gallerypath_strm);
    }


    public interface ImagePickerCompleteListiner {
        public void setImage(String sdcardPath);

        public void setImageCancel();
    }


    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }


    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
        intent.setData(uri);
        mActivity.startActivityForResult(intent, 101);
    }

}

