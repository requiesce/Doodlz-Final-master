// MainActivityFragment.java
// Fragment in which the DoodleView is displayed
package com.deitel.doodlz;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

public class MainActivityFragment extends Fragment {
   private DoodleView doodleView; // handles touch events and draws
   private float acceleration;
   private float currentAcceleration;
   private float lastAcceleration;
   private boolean dialogOnScreen = false;

   // value used to determine whether user shook the device to erase
   private static final int ACCELERATION_THRESHOLD = 100000;

   // used to identify the request for using external storage, which
   // the save image feature needs
   private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 1;

   // called when Fragment's view needs to be created
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);

      View view =
         inflater.inflate(R.layout.fragment_main, container, false);

      setHasOptionsMenu(true); // this fragment has menu items to display

      // get reference to the DoodleView
      doodleView = (DoodleView) view.findViewById(R.id.doodleView);

      // initialize acceleration values
      acceleration = 0.00f;
      currentAcceleration = SensorManager.GRAVITY_EARTH;
      lastAcceleration = SensorManager.GRAVITY_EARTH;
      return view;
   }

   // start listening for sensor events
   @Override
   public void onResume() {
      super.onResume();
      enableAccelerometerListening(); // listen for shake event
   }

   // enable listening for accelerometer events
   private void enableAccelerometerListening() {
      // get the SensorManager
      SensorManager sensorManager =
         (SensorManager) getActivity().getSystemService(
            Context.SENSOR_SERVICE);

      // register to listen for accelerometer events
      sensorManager.registerListener(sensorEventListener,
         sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
         SensorManager.SENSOR_DELAY_NORMAL);
   }

   // stop listening for accelerometer events
   @Override
   public void onPause() {
      super.onPause();
      disableAccelerometerListening(); // stop listening for shake
   }

   // disable listening for accelerometer events
   private void disableAccelerometerListening() {
      // get the SensorManager
      SensorManager sensorManager =
         (SensorManager) getActivity().getSystemService(
            Context.SENSOR_SERVICE);

      // stop listening for accelerometer events
      sensorManager.unregisterListener(sensorEventListener,
         sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
   }

   // event handler for accelerometer events
   private final SensorEventListener sensorEventListener =
      new SensorEventListener() {
         // use accelerometer to determine whether user shook device
         @Override
         public void onSensorChanged(SensorEvent event) {
            // ensure that other dialogs are not displayed
            if (!dialogOnScreen) {
               // get x, y, and z values for the SensorEvent
               float x = event.values[0];
               float y = event.values[1];
               float z = event.values[2];

               // save previous acceleration value
               lastAcceleration = currentAcceleration;

               // calculate the current acceleration
               currentAcceleration = x * x + y * y + z * z;

               // calculate the change in acceleration
               acceleration = currentAcceleration *
                  (currentAcceleration - lastAcceleration);

               // if the acceleration is above a certain threshold
               if (acceleration > ACCELERATION_THRESHOLD)
                  confirmErase();
            }
         }

         // required method of interface SensorEventListener
         @Override
         public void onAccuracyChanged(Sensor sensor, int accuracy) {}
      };

   // confirm whether image should be erase
   private void confirmErase() {
      EraseImageDialogFragment fragment = new EraseImageDialogFragment();
      fragment.show(getFragmentManager(), "erase dialog");
   }

   // displays the fragment's menu items
   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.doodle_fragment_menu, menu);
   }

   // handle choice from options menu
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // switch based on the MenuItem id
      switch (item.getItemId()) {
         case R.id.color:
            ColorDialogFragment colorDialog = new ColorDialogFragment();
            colorDialog.show(getFragmentManager(), "color dialog");
            return true; // consume the menu event
         case R.id.line_width:
            LineWidthDialogFragment widthDialog =
               new LineWidthDialogFragment();
            widthDialog.show(getFragmentManager(), "line width dialog");
            return true; // consume the menu event

         case R.id.background_color:
            BackgroundColorFragment backgroundColorDialog = new BackgroundColorFragment();
            backgroundColorDialog.show(getFragmentManager(), "background color dialog");
            return true;

         case R.id.background_image:
            ////BackgroundImageFragment backgroundImageDialog = new BackgroundImageFragment();
            ////backgroundImageDialog.show(getFragmentManager(), "background image dialog");
            getBackgroundImage();
            return true;

         case R.id.delete_drawing:
            confirmErase(); // confirm before erasing image
            return true; // consume the menu event
         case R.id.save:
            saveImage(); // check permission and save current image
            return true; // consume the menu event
         case R.id.print:
            doodleView.printImage(); // print the current images
            return true; // consume the menu event
      }

      return super.onOptionsItemSelected(item);
   }

//////////////////////////////////////////////////////////////////////////////////
/*
   int SELECT_PHOTO = 1;

   private void getBackgroundImage()
  {
      Intent selectPhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
      selectPhotoIntent.setType("image/*");
      selectPhotoIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
      startActivityForResult(Intent.createChooser(selectPhotoIntent,"Complete Action Using"), SELECT_PHOTO);
  }

      @Override
      public void onActivityResult(int requestCode, int resultCode, Intent data) {
          super.onActivityResult(requestCode, resultCode, data);

          if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null) {
              Uri pickedImage = data.getData();
              //set the selected image to ImageView

              //mImageView.setImageURI(pickedImage);
              //Bitmap passedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pickedImage);

               // Context context = getActivity().getApplicationContext();
              Bitmap  passedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pickedImage);
             // Bitmap passedBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), pickedImage);


              doodleView.setBackgroundImage(passedBitmap);

              //DoodleView.setBackgroundImage(pickedImage);

          }
      } */

/////////////////////////////////////////////////////////////////////////////////////
int SELECT_PHOTO = 1;

    private void getBackgroundImage()
    {
      /*  Intent selectPhotoIntent = new Intent(Intent.ACTION_PICK); //ACTION_GET_CONTENT
        selectPhotoIntent.setType("image/*");
        //selectPhotoIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
        startActivityForResult(Intent.createChooser(selectPhotoIntent,"Complete Action Using"), SELECT_PHOTO);
*/


        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECT_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

      /*  // Here we need to check if the activity that was triggers was the Image Gallery.
        // If it is the requestCode will match the LOAD_IMAGE_RESULTS value.
        // If the resultCode is RESULT_OK and there is some data we know that an image was picked.
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null) {
            // Let's read picked image data - its URI
            Uri pickedImage = data.getData();
            String uriToString = pickedImage.toString();

            String path = Environment.getExternalStorageDirectory().getPath();
            String myJpgPath = path + "/Download/download.jpg";
            Uri testImage = Uri.parse(myJpgPath);

            // Let's read picked image path using content resolver
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContext().getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap passedBitmap = BitmapFactory.decodeFile(uriToString, options);

            //Bitmap passedBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), pickedImage);

            // Do something with the bitmap
           // doodleView.setBackgroundImage(passedBitmap, pickedImage);


            // At the end remember to close the cursor or you will end with the RuntimeException!
            cursor.close();

            ImageView test = (ImageView)getActivity().findViewById(R.id.imageView3);
            //test.setImageBitmap(passedBitmap);
            //test.setBackgroundColor(Color.CYAN);
            //doodleView.setBackgroundImage(passedBitmap, Uri.parse(imagePath));

            */



        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContext().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            //ImageView imageView = (ImageView) findViewById(R.id.imgView);
           // imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            doodleView.setBackgroundImage(picturePath);
        }









/*
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                final Bitmap passedBitmap = BitmapFactory.decodeStream(imageStream);

                doodleView.setBackgroundImage(passedBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }else {
        }

*/





        }






















    //////////////////////////////////////////////////////////////////////////////////


   // requests for the permission needed for saving the image if
   // necessary or saves the image if the app already has permission
   private void saveImage() {
      // checks if the app does not have permission needed
      // to save the image
      if (getContext().checkSelfPermission(
         Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
         PackageManager.PERMISSION_GRANTED) {

         // shows an explanation for why permission is needed
         if (shouldShowRequestPermissionRationale(
            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder builder =
               new AlertDialog.Builder(getActivity());

            // set Alert Dialog's message
            builder.setMessage(R.string.permission_explanation);

            // add an OK button to the dialog
            builder.setPositiveButton(android.R.string.ok,
               new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                     // request permission
                     requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        SAVE_IMAGE_PERMISSION_REQUEST_CODE);
                  }
               }
            );

            // display the dialog
            builder.create().show();
         }
         else {
            // request permission
            requestPermissions(
               new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
               SAVE_IMAGE_PERMISSION_REQUEST_CODE);
         }
      }
      else { // if app already has permission to write to external storage
         doodleView.saveImage(); // save the image
      }
   }

   // called by the system when the user either grants or denies the
   // permission for saving an image
   @Override
   public void onRequestPermissionsResult(int requestCode,
      String[] permissions, int[] grantResults) {
      // switch chooses appropriate action based on which feature
      // requested permission
      switch (requestCode) {
         case SAVE_IMAGE_PERMISSION_REQUEST_CODE:
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
               doodleView.saveImage(); // save the image
            return;
      }
   }

   // returns the DoodleView
   public DoodleView getDoodleView() {
      return doodleView;
   }

   // indicates whether a dialog is displayed
   public void setDialogOnScreen(boolean visible) {
      dialogOnScreen = visible;
   }
}
