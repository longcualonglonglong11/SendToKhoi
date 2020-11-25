package com.apcs2.helperapp.main;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import com.android.volley.toolbox.JsonObjectRequest;
import com.apcs2.helperapp.controller.FireBaseStorageController;
import com.apcs2.helperapp.controller.FirebaseDataController;
import com.apcs2.helperapp.controller.LandMarkController;
import com.apcs2.helperapp.controller.ServerController;
import com.apcs2.helperapp.entity.LandMark;
import com.apcs2.helperapp.entity.Message;
import com.apcs2.helperapp.entity.MessageAdapter;
import com.apcs2.helperapp.repository.FirebaseDataRepository;
import com.apcs2.helperapp.repository.LandMarkRepository;
import com.apcs2.helperapp.repository.MessageRepository;
import com.example.helperapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DatabaseReference;
import com.santalu.maskara.widget.MaskEditText;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import it.sephiroth.android.library.rangeseekbar.RangeSeekBar;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int CAMERA_REQUEST = 1010;
    private static final int MY_CAMERA_PERMISSION_CODE = 1011;
    private static final int VIEW_AVAILABLE_HOUSE = 1111;
    private Marker mMarker;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private ArrayList<Marker> mMarkers;
    ImageButton logout;
    private GoogleMap mMap;
    CardView compleRequest;
    CardView editRequest;
    // private ArrayList<LandMark> landmarks;
    LandMarkRepository landMarkRepository;
    LandMarkController landMarkController;
    ServerController serverController;
    LinearLayout containerLayout;
    LinearLayout distanceLayout;
    ScrollView requestForm;
    ScrollView detailView;
    TextView detailTitle;
    TextView detailDescription;
    TextView detailPhone;
    TextView detailLocation;
    TextView detailPrice;
    EditText eTitle;
    EditText eDescription;
    EditText eLocation;
    MaskEditText ePhone;
    EditText ePrice;
    Spinner sEmergency;
    CheckBox cbCurrentLocation;
    TextView tEmergency;
    TextView detailEmergency;
    SearchView searchRequest;
    FirebaseAuth mFirebaseAuth;
    String userName;
    TextView detailDateTime;
    DatabaseReference drName;
    String nameOfUser;
    String detailCurLatLng;
    String TAG = "INFO";
    int idNotify;
    int newNearRequest;
    ImageView demoImg;
    TextView tmpUrl;
    LinearLayout footerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initComponent();
        //showNumberNewUpdateReq();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private void initComponent() {
        tmpUrl = findViewById(R.id.staticLayout);
        demoImg = findViewById(R.id.img_demo);
        mMarkers = new ArrayList<>();
        detailView = findViewById(R.id.detail_view);
        detailTitle = findViewById(R.id.detail_title);
        detailDescription = findViewById(R.id.detail_description);
        detailPhone = findViewById(R.id.detail_phone);
        detailLocation = findViewById(R.id.detail_location);
        detailPrice = findViewById(R.id.detail_price);
        containerLayout = findViewById(R.id.container);
        requestForm = findViewById(R.id.request_from);

        eTitle = findViewById(R.id.eTitle);
        eDescription = findViewById(R.id.eDescription);
        eLocation = findViewById(R.id.eLocation);
        ePhone = findViewById(R.id.ePhone);
        ePrice = findViewById(R.id.ePrice);
        ePrice.addTextChangedListener(new MoneyTextWatcher(ePrice));
        sEmergency = findViewById(R.id.s_emergency);
        tEmergency = findViewById(R.id.t_emergency);
        cbCurrentLocation = findViewById(R.id.current_location);
        detailEmergency = findViewById(R.id.detail_emergency);
        ArrayAdapter<CharSequence> sEmergencyAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.category_arr, android.R.layout.simple_spinner_item);
        sEmergencyAdapter.setDropDownViewResource(R.layout.emergency_level_spinner);
        sEmergency.setAdapter(sEmergencyAdapter);
        sEmergency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);
                tEmergency.setText(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        searchRequest = findViewById(R.id.sr);
        setSearchOnSearchListener();
        mFirebaseAuth = FirebaseAuth.getInstance();
        //   landMarkController.setUserId("1");

        detailDateTime = findViewById(R.id.detail_dateTime);
        nameOfUser = mFirebaseAuth.getCurrentUser().getEmail();
        distanceLayout = (LinearLayout) findViewById(R.id.linearLayoutShowDistance);
//        nameOfUser = "LONG";

        Log.d(TAG + "username: ", nameOfUser);
        TextView textNameUser = findViewById(R.id.name_of_user);
        String nameOfUserArr[] = nameOfUser.split(getString(R.string.split_sign_email));
        nameOfUser = nameOfUserArr[0];
        textNameUser.setText("Welcome " + nameOfUser + "!");
        idNotify = 0;
        newNearRequest = 0;

        containerLayout = findViewById(R.id.container);
        detailView = findViewById(R.id.detail_view);
        compleRequest = findViewById(R.id.cardView_complete);
        editRequest = findViewById(R.id.card_view_edit);
        landMarkController = new LandMarkController(this);
        Log.d("CCCCCCCCCCCC", getCurUserId());
        landMarkController.setUserId(getCurUserId());
        serverController = new ServerController(landMarkController, this);
        footerLayout = (LinearLayout) findViewById(R.id.footer);
    }

    private void setSearchOnSearchListener() {
        searchRequest.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                try {
                    landMarkController.searchLandMark(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }


    public void showNumberNewUpdateReq() {
        try {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (newNearRequest > 0)
                        Toast.makeText(getApplicationContext(), String.valueOf(newNearRequest) + " " + getString(R.string.number_new_req), Toast.LENGTH_SHORT).show();

                }
            }, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        landMarkController.setMap(mMap);
        checkPermissionLocationAccess();
        LatLng currentLatLng;
        currentLatLng = landMarkController.getDeviceLocation();
        // gotoLocation(currentLatLng);
        landMarkController.gotoLocation(currentLatLng);
        landMarkController.setMarkerOnClickListener(containerLayout, detailView, compleRequest, editRequest, detailTitle, detailPhone, detailDescription, detailEmergency, detailDateTime, detailLocation, detailPrice, tmpUrl);
    }

    public boolean checkPermissionLocationAccess() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void directToCurrentPosition(View view) {
        distanceLayout.setVisibility(View.VISIBLE);
        footerLayout.setVisibility(View.GONE);
        searchRequest.setVisibility(View.VISIBLE);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView_available_house);
        scrollView.setVisibility(View.GONE);
        landMarkController.directToCurrentPosition();
    }

    /////////////Utils onclick
    public void startDial(View view) {
        String phoneNumber = (String) detailPhone.getText().subSequence(7, detailPhone.getText().length());
        call(phoneNumber);
    }

    public void call(String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel: " + phoneNumber)));
    }

    public void sendSMS(String number) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)));
    }

    public void startMessage(View view) {
        sendSMS((String) detailPhone.getText());
    }

    public void createForm(View view) {
        //  Toast.makeText(this, "Tong so landmark la: " + String.valueOf(landmarks.size()), Toast.LENGTH_SHORT).show();
        footerLayout.setVisibility(View.GONE);
        searchRequest.setVisibility(View.GONE);
        containerLayout.setGravity(Gravity.CENTER);
        landMarkController.animateFadout(requestForm);
        requestForm.setVisibility(View.VISIBLE);
        eTitle.setText("");
        eDescription.setText("");
        eLocation.setText("");
        ePhone.setText("");
        footerLayout.setVisibility(View.GONE);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveMarker(View view) {
        String title = String.valueOf(eTitle.getText());
        String description = String.valueOf(eDescription.getText());
        String phone = String.valueOf(ePhone.getText());
        String category = (String) tEmergency.getText();
//        category = category.substring(category.indexOf(":") + 2);
        String location = String.valueOf(eLocation.getText());
        String price = String.valueOf(ePrice.getText());

        landMarkController.saveMarker(title, description, location, phone, category, price, filePath);
    }

    public void choose_current_location(View view) {
        if (cbCurrentLocation.isChecked()) {
            hideLocationText(View.GONE);
        } else {
            hideLocationText(View.VISIBLE);
        }
    }

    private void hideLocationText(int state) {
        TransitionManager.beginDelayedTransition((ViewGroup) eLocation.getParent());
        eLocation.setVisibility(state);
    }

    public void close_form(View view) {
        landMarkController.closeForm(this, searchRequest, requestForm, containerLayout, eTitle, eLocation, eDescription, ePhone);
    }

    public void completeRequest(View view) {
        landMarkController.completeRequest();
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView_available_house);
        if (scrollView.getVisibility() == View.GONE) {
            footerLayout.setVisibility(View.VISIBLE);
            searchRequest.setVisibility(View.VISIBLE);
        }
    }

    public void close_detail_form(View view) {
        landMarkController.close_detail_form();
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView_available_house);
        if (scrollView.getVisibility() == View.GONE) {
            footerLayout.setVisibility(View.VISIBLE);
            searchRequest.setVisibility(View.VISIBLE);
        }
    }

    public void logoutAccount(View view) {
        landMarkController.logout();
    }

    public void openServer(View view) {
        detailView.setVisibility(View.GONE);
        searchRequest.setVisibility(View.GONE);
        footerLayout.setVisibility(View.GONE);

        this.findViewById(R.id.cur_server).setVisibility(View.VISIBLE);

        landMarkController.showServer();

        //      landMarkController.updateServer();
    }

    public String getCurUserId() {
        return mFirebaseAuth.getCurrentUser().getUid();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendMessage(View view) {
        TextView chatBox = findViewById(R.id.chat_box);
        Message message = new Message();
        // Log.d("TEXXXX", (String) chatBox.getText());
        message.setContent(String.valueOf(chatBox.getText()));
        message.setUserId(getCurUserId());
        message.setUserName(nameOfUser);
        serverController.saveMessage(message);
        chatBox.setText("");

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void editRequest(View view) {

        //   TransitionManager.beginDelayedTransition((ViewGroup)button.getParent());
        //    landMarkController.animateFadout(searchRequest);

        //  landMarkController.animateFadout(footer);


        //   landMarkController.animateFadout(containerLayout);

        landMarkController.animateFadout(detailView);
        detailView.setVisibility(View.GONE);

        containerLayout.setGravity(Gravity.CENTER);
        landMarkController.animateFadout(requestForm);
        requestForm.setVisibility(View.VISIBLE);
        cbCurrentLocation.setVisibility(View.GONE);

        //   close_detail_form(view);
        eTitle.setText(detailTitle.getText());
        String description = (String) detailDescription.getText();
        eDescription.setText(description.substring(description.indexOf(":") + 2));

        String phone = (String) detailPhone.getText();

        ePhone.setText(phone.substring(phone.indexOf(":") + 2));

        String location = (String) detailLocation.getText();

        eLocation.setText(location.substring(location.indexOf(":") + 2));
        String price = (String) detailPrice.getText();
        ePrice.setText(price.substring(price.indexOf(":") + 2));
        String category = (String) detailEmergency.getText();

        category = category.substring(category.indexOf(":") + 2);

        int index = -1;
        String[] arr = getResources().getStringArray(R.array.category_arr);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(category))
                index = i;
        }
        TextView staticLayout = findViewById(R.id.staticLayout);
        String imgUrl = (String) staticLayout.getText();
        sEmergency.setSelection(index);
            Picasso.get().load(imgUrl).into(demoImg);

        eLocation.setEnabled(false);
        demoImg.setTag("Edit");


    }


    public void closeServer(View view) {
        findViewById(R.id.cur_server).setVisibility(View.GONE);
        detailView.setVisibility(View.VISIBLE);
    }

    int PICK_IMAGE_REQUEST = 1001;
    Uri filePath;
    // Select Image method

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);

                demoImg.setImageBitmap(bitmap);
                demoImg.setTag("change");
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        } else if (requestCode == CAMERA_REQUEST
                && resultCode == RESULT_OK
                && data != null) {
        //    Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

            Bitmap photo = (Bitmap) data.getExtras().get("data");
       //     demoImg.setImageBitmap(takenImage);
            demoImg.setImageBitmap(photo);
            demoImg.setTag("change");
     //       filePath = Uri.fromFile(photoFile)  ;

        } else if (requestCode == VIEW_AVAILABLE_HOUSE
                && resultCode != RESULT_OK) {
            Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
        }
    }

    public void selectImage(View view) {
        loadImg();
    }

    public void loadImg() {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    public void takePicture(View view) {
        cameraOn();
    }
    public final String APP_TAG = "MyCustomApp";
    public String photoFileName = "photo.jpg";
    File photoFile;

    private void cameraOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                photoFile = getPhotoFileUri(photoFileName);
//
//                Uri fileProvider = FileProvider.getUriForFile(this, "com.codepath.fileprovider", photoFile);
//                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void close_map_direction(View view) {
        distanceLayout.setVisibility(View.GONE);
        footerLayout.setVisibility(View.VISIBLE);
        searchRequest.setVisibility(View.VISIBLE);
        landMarkController.removePolylines();
    }

    public void viewAvailableHouse(View view) {
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView_available_house);
        scrollView.setVisibility(View.VISIBLE);
        searchRequest.setVisibility(View.GONE);
        footerLayout.setVisibility(View.GONE);
        ArrayList<LandMark> list = landMarkController.getLandMarkRepository().getLandMarkArrayList();
        landMarkController.showAvailableHouse(list,
                containerLayout,
                detailView,
                compleRequest,
                editRequest,
                detailTitle,
                detailPhone,
                detailDescription,
                detailEmergency,
                detailDateTime,
                detailLocation,
                detailPrice,
                tmpUrl);
    }

    public void close_view_available_house(View view) {
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView_available_house);
        scrollView.setVisibility(View.GONE);
        searchRequest.setVisibility(View.VISIBLE);
        footerLayout.setVisibility(View.VISIBLE);
    }

    public void option_view_available_house(View view) {
        LinearLayout optionLayout = (LinearLayout) findViewById(R.id.linearLayout_Selection);
        optionLayout.setVisibility(View.VISIBLE);
        final TextView textViewRange = (TextView) findViewById(R.id.textViewRange);
        setCitySpinnerAdapter();
        RangeSeekBar rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onProgressChanged(RangeSeekBar rangeSeekBar, int i, int i1, boolean b) {
                updateRangeText(textViewRange, rangeSeekBar);
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar rangeSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar rangeSeekBar) {

            }
        });
        updateRangeText(textViewRange, rangeSeekBar);
    }

    private void setCitySpinnerAdapter() {
        Spinner citySpinner = (Spinner) findViewById(R.id.option_city);
        final TextView cityTextView = (TextView) findViewById(R.id.city_textView);
        final LinearLayout districtLayout = (LinearLayout) findViewById(R.id.district_layout);
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.city_array, android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(R.layout.emergency_level_spinner);
        citySpinner.setAdapter(cityAdapter);
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);
                cityTextView.setText(item);
                if (item.equals("Thành phố Hồ Chí Minh")) {
                    districtLayout.setVisibility(View.VISIBLE);
                    setDistrictSpinner();
                } else districtLayout.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setDistrictSpinner() {
        Spinner districtSpinner = (Spinner) findViewById(R.id.option_district);
        final TextView districtTextView = (TextView) findViewById(R.id.district_textView);
        ArrayAdapter<CharSequence> districtAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.hcm_district, android.R.layout.simple_spinner_item);
        districtAdapter.setDropDownViewResource(R.layout.emergency_level_spinner);
        districtSpinner.setAdapter(districtAdapter);
        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);
                districtTextView.setText(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }


    private void updateRangeText(TextView textViewRange, RangeSeekBar rangeSeekBar) {
        textViewRange.setText(rangeSeekBar.getProgressStart() + "TR" + " - " + rangeSeekBar.getProgressEnd() + "TR" + " (VND)");
    }

    public void close_option_view_available_house(View view) {
        LinearLayout optionLayout = (LinearLayout) findViewById(R.id.linearLayout_Selection);
        optionLayout.setVisibility(View.GONE);
        CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox_myhouselist);
        checkBox.setChecked(false);
    }


    public void Done_view_house_option(View view) {
        LinearLayout optionLayout = (LinearLayout) findViewById(R.id.linearLayout_Selection);
        optionLayout.setVisibility(View.GONE);
        ArrayList<LandMark> list = landMarkController.getLandMarkRepository().getLandMarkArrayList();
        ArrayList<LandMark> filterList;
        ArrayList<LandMark> filterCityAddressList;
        ArrayList<LandMark> finalFilterList;
        ArrayList<LandMark> filterPriceList;
        CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox_myhouselist);
        TextView textViewRange = (TextView) findViewById(R.id.textViewRange);
        RangeSeekBar rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        TextView cityTextView = (TextView) findViewById(R.id.city_textView);
        TextView districtTextView = (TextView) findViewById(R.id.district_textView);
        if (checkBox.isChecked()) filterList = filterMyHouse(list, getCurUserId());
        else filterList = list;
        if (!textViewRange.getText().equals("0TR - 100TR (VND)")) {
            filterPriceList = filterPrice(filterList, rangeSeekBar.getProgressStart(), rangeSeekBar.getProgressEnd());
        } else filterPriceList = filterList;
        String strCity = (String) cityTextView.getText();
        String strDistrict = (String) districtTextView.getText();
        if (!strCity.equals("Cả nước")) {
            filterCityAddressList = filterCityAddress(filterPriceList, strCity);
        } else filterCityAddressList = filterPriceList;
        if (strCity.equals("Thành phố Hồ Chí Minh") && !strDistrict.equals("All")) {
            finalFilterList = filterHCMCityAddress(filterCityAddressList, strDistrict);
        } else finalFilterList = filterCityAddressList;
        landMarkController.showAvailableHouse(finalFilterList,
                containerLayout,
                detailView,
                compleRequest,
                editRequest,
                detailTitle,
                detailPhone,
                detailDescription,
                detailEmergency,
                detailDateTime,
                detailLocation,
                detailPrice,
                tmpUrl);
    }

    private ArrayList<LandMark> filterHCMCityAddress(ArrayList<LandMark> filterCityList, String strDistrict) {
        ArrayList<LandMark> arrayList = new ArrayList<LandMark>();
        for (int i = 0; i < filterCityList.size(); i++) {
            LandMark landMark = filterCityList.get(i);
            String lmAddress = landMark.getAddress();
            if (lmAddress.contains(strDistrict)) arrayList.add(landMark);
        }
        return arrayList;
    }

    private ArrayList<LandMark> filterCityAddress(ArrayList<LandMark> filterPriceList, String strCity) {
        ArrayList<LandMark> arrayList = new ArrayList<LandMark>();
        for (int i = 0; i < filterPriceList.size(); i++) {
            LandMark landMark = filterPriceList.get(i);
            String lmAddress = landMark.getAddress();
            if (lmAddress.contains(strCity)) arrayList.add(landMark);
        }
        return arrayList;
    }

    private ArrayList<LandMark> filterPrice(ArrayList<LandMark> filterList, int progressStart, int progressEnd) {
        ArrayList<LandMark> arrayList = new ArrayList<LandMark>();
        for (int i = 0; i < filterList.size(); i++) {
            LandMark landMark = filterList.get(i);
            String strPrice = landMark.getPrice();
            String remPrice = strPrice.replaceAll("[,]", "");
            Double price = Double.parseDouble(remPrice) / 1000000;
            if (price >= progressStart && price <= progressEnd) arrayList.add(landMark);
        }
        return arrayList;
    }

    private ArrayList<LandMark> filterMyHouse(ArrayList<LandMark> list, String curUserId) {
        ArrayList<LandMark> filterList = new ArrayList<LandMark>();
        for (int i = 0; i < list.size(); i++) {
            LandMark landMark = list.get(i);
            if (landMark.getUserId().equals(curUserId)) filterList.add(landMark);
        }
        return filterList;
    }


}