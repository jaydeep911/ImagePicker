package androidnjd.imagepicker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity {

    private ImagePickerUtil imagePickerUtill;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imagePickerUtill= new ImagePickerUtil(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickerUtill.showImagePicker(new ImagePickerUtil.ImagePickerCompleteListiner() {
                    @Override
                    public void setImage(String sdcardPath) {

                        Toast.makeText(MainActivity.this, sdcardPath,Toast.LENGTH_LONG);
                    }

                    @Override
                    public void setImageCancel() {
                        Toast.makeText(MainActivity.this, "Image Pick Cancel",Toast.LENGTH_LONG);
                    }
                },"Pick Image");
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePickerUtill.onActivityResult(requestCode,resultCode,data);
    }
}
