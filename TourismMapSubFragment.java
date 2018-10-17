package jp.androidbook.photostohint;

/* 観光モードマップフラグメント */

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class TourismMapSubFragment extends Fragment implements OnMapReadyCallback {

    //遷移ID
    public static final int TRANSITION_ID_ADD_REVIEW = 0;
    public static final int TRANSITION_ID_INFORMATION = 1;

    //Bundleキー
    private static final String USER_ID_KEY="USER_ID";
    private static final String REGION_NAME_KEY="REGION_NAME";

    //GoogleMapインスタンス
    private GoogleMap gmap;

    //位置情報プロバイダー
    private FusedLocationProviderClient fusedLocationProviderClient;

    //位置情報プロバイダー設定
    private SettingsClient settingsClient;

    //位置情報取得時コールバック
    private LocationCallback locationCallback;

    //位置情報設定
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;

    //位置情報取得がアクティブか
    private boolean requestingLocationUpdates;

    //ユーザ位置（緯度経度）
    private LatLng nowLocation;

    //ユーザ位置マーカー
    private Marker locationMarker;

    //デフォルトで用意されている吹き出し種類数
    private int balloonTypeOffset;

    //吹き出しデータのダウンロードが完了しているか
    private boolean balloonDownloaded;

    //ダウンロード待ちの吹き出し画像数
    private int waitingLoadBalloonBitmapNum;

    //吹き出し種類名
    private ArrayList<String> balloonTypeNames=new ArrayList<>();

    //吹き出し画像
    private ArrayList<Bitmap> balloonBitmaps=new ArrayList<>();

    //マーカーリスト
    private ArrayList<Marker> markerList=new ArrayList<Marker>();

    //各種吹き出し表示ON/OFF
    private ArrayList<Boolean> dispBalloonTypes=new ArrayList<>();

    //マーカーID -> マーカー情報
    private HashMap<String,SpotInfoWindowInfo> markerIdentifier=new HashMap<String,SpotInfoWindowInfo>();

    //ユーザID
    private String userID;

    //地名
    private String regionName;

    //フラグメントがアクティブか
    private boolean isActive;

    /* コンストラクタ */
    public TourismMapSubFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static TourismMapSubFragment newInstance(String userID,String regionName) {
        //フラグメントインスタンス生成
        TourismMapSubFragment fragment = new TourismMapSubFragment();

        //バンドル生成
        Bundle args = new Bundle();

        //バンドルに各種パラメータ追加
        args.putString(USER_ID_KEY,userID);
        args.putString(REGION_NAME_KEY,regionName);

        //フラグメントインスタンスにバンドルセット
        fragment.setArguments(args);

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* フラグメント生成時処理 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //newInstanceで設定したパラメータを取得
        if (getArguments() != null) {
            userID=getArguments().getString(USER_ID_KEY);
            regionName=getArguments().getString(REGION_NAME_KEY);
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_tourism_map_sub, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //口コミ投稿ボタン取得
        ImageButton addReviewButton=view.findViewById(R.id.tourism_map_sub_fragment_add_review_button);

        //インフォメーションボタン取得
        ImageButton informationButton=view.findViewById(R.id.tourism_map_sub_fragment_openData_button);

        //吹き出し表示変更ボタン取得
        ImageButton balloonDisplayButton=view.findViewById(R.id.tourism_map_sub_fragment_balloon_disp_button);

        //カメラリセット（ユーザ位置に合わせる）ボタン取得
        ImageButton myLocationButton=view.findViewById(R.id.tourism_map_sub_fragment_my_location_button);

        //表示更新ボタン取得
        ImageButton updateButton=view.findViewById(R.id.tourism_map_sub_fragment_update_button);

        //地名テキストビュー取得
        TextView regionNameTextView=view.findViewById(R.id.tourism_map_sub_fragment_location_name_text);

        //地名セット
        regionNameTextView.setText(regionName);

        //口コミ投稿ボタン押下時処理設定
        addReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //測位が完了していないなら実行不可
                if(nowLocation==null)
                {
                    //トーストで警告
                    Toast toast = Toast.makeText(getActivity(),"測位が完了するまで実行できません。",Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //フラグメント遷移コールバック実行
                ((CallBack)getParentFragment()).onTourismMapSubFragmentInteraction(TRANSITION_ID_ADD_REVIEW,nowLocation);
            }
        });

        //インフォメーションボタン押下時処理設定
        informationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //フラグメント遷移コールバック実行
                ((CallBack)getParentFragment()).onTourismMapSubFragmentInteraction(TRANSITION_ID_INFORMATION,null);
            }
        });

        //表示更新ボタン押下時処理設定
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //初期化処理実行
                init();
            }
        });

        //吹き出し表示変更ボタン押下時処理設定
        balloonDisplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //吹き出しデータダウンロードが終了していないなら実行不可
                if(!balloonDownloaded)
                {
                    //トーストで警告表示
                    Toast toast = Toast.makeText(getActivity(),"データ取得が完了するまで実行できません。",Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //吹き出し表示ON/OFF ArrayList<boolean> -> boolean[]
                boolean[] tempDispBalloonTypes=new boolean[balloonTypeNames.size()];
                String[] tempBalloonTypeNames=new String[balloonTypeNames.size()];
                for(int i=0;i<balloonTypeNames.size();i++)
                {
                    tempDispBalloonTypes[i]=dispBalloonTypes.get(i);
                    tempBalloonTypeNames[i]=balloonTypeNames.get(i);
                }

                //アラートダイアログ表示
                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder.setTitle("各種吹き出しの表示/非表示");

                //各種吹き出し表示ON/OFF切り替え
                builder.setMultiChoiceItems(tempBalloonTypeNames,tempDispBalloonTypes,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                dispBalloonTypes.set(which,isChecked);
                            }
                        });

                //OKボタン押下時処理
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //吹き出し表示ON/OFFを適用
                        for(Marker tempMarker:markerList)
                        {
                            if(dispBalloonTypes.get(markerIdentifier.get(tempMarker.getId()).getType()))
                            {
                                tempMarker.setVisible(true);
                            }
                            else
                            {
                                tempMarker.setVisible(false);
                            }
                        }

                        //吹き出し表示ON/OFF設定をSharedPreferencesに保存
                        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SharedPreferences.SETTINGS_DATA, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        for(int i=0;i<balloonTypeNames.size();i++)
                        {
                            editor.putBoolean(Constants.SharedPreferences.DISPLAY_BALLOON_KEY+i, dispBalloonTypes.get(i));
                        }
                        editor.apply();
                    }
                });
                builder.show();
            }
        });

        //カメラリセット（ユーザ位置に合わせる）ボタン押下時処理設定
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //GoogleMapが有効で測位が完了しているとき
                if(gmap!=null&&nowLocation!=null)
                {
                    //ユーザの位置が中心になるようにカメラを移動
                    gmap.moveCamera(CameraUpdateFactory.newLatLng(nowLocation));
                }

                //GoogleMapが無効または測位が完了していないとき
                else {
                    //トーストで警告表示
                    Toast toast = Toast.makeText(getActivity(),"測位が完了するまで実行できません。",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    /* 初期化処理 */
    private void init()
    {
        //マーカークリア
        for(Marker marker:markerList)
        {
            marker.remove();
        }

        //各種変数初期化
        nowLocation=null;
        balloonTypeNames.clear();
        balloonBitmaps.clear();
        markerList.clear();
        dispBalloonTypes.clear();
        markerIdentifier.clear();

        //デフォルトで用意されている吹き出し種類名追加
        balloonTypeNames.add(Constants.Map.MARKER_NAME_MY_LOCATION);
        balloonTypeNames.add(Constants.Map.MARKER_NAME_THEME_SPOT);
        balloonTypeNames.add(Constants.Map.MARKER_NAME_EVENT_INFO);
        balloonTypeNames.add(Constants.Map.MARKER_NAME_REVIEW);

        //デフォルトで用意されている吹き出し種類について表示ON/OFF追加（SharedPreferencesにセーブデータがあれば読み込む）
        SharedPreferences prefs=getActivity().getSharedPreferences(Constants.SharedPreferences.SETTINGS_DATA, Context.MODE_PRIVATE);
        dispBalloonTypes.add(prefs.getBoolean(Constants.SharedPreferences.DISPLAY_BALLOON_KEY +Constants.Map.MARKER_ID_MY_LOCATION,true));
        dispBalloonTypes.add(prefs.getBoolean(Constants.SharedPreferences.DISPLAY_BALLOON_KEY +Constants.Map.MARKER_ID_THEME_SPOT,true));
        dispBalloonTypes.add(prefs.getBoolean(Constants.SharedPreferences.DISPLAY_BALLOON_KEY +Constants.Map.MARKER_ID_EVENT_SPOT,true));
        dispBalloonTypes.add(prefs.getBoolean(Constants.SharedPreferences.DISPLAY_BALLOON_KEY +Constants.Map.MARKER_ID_REVIEW_SPOT,true));

        //デフォルトで用意されている吹き出し種類数取得
        balloonTypeOffset=balloonTypeNames.size();

        //ダウンロード分の吹き出し表示ON/OFFダミー用意（SharedPreferencesにセーブデータがあれば読み込む）
        for(int i=0;i<30;i++)
        {
            dispBalloonTypes.add(prefs.getBoolean(Constants.SharedPreferences.DISPLAY_BALLOON_KEY +(balloonTypeOffset+i),false));
        }

        //デフォルトで用意されている吹き出し種類について吹き出し画像セット
        balloonBitmaps.add(null);
        balloonBitmaps.add(BitmapFactory.decodeResource(getResources(),R.drawable.map_marker1));
        balloonBitmaps.add(BitmapFactory.decodeResource(getResources(),R.drawable.map_marker2));
        balloonBitmaps.add(BitmapFactory.decodeResource(getResources(),R.drawable.map_marker3));

        //ダウンロード分の吹き出し種類について吹き出し画像ダミー用意
        for(int i=0;i<30;i++)
        {
            balloonBitmaps.add(null);
        }

        //位置情報管理インスタンス取得
        LocationManager locationManager =(LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        //GPSがOFFになっているなら
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            //アラートダイアログ表示
            new AlertDialog.Builder(getActivity())
                    .setTitle("注意")
                    .setMessage("サービスを利用するためにはGPSをONにする必要があります。")

                    //OKボタン押下時
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //GPS設定画面に移動
                            Intent settingsIntent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(settingsIntent);
                        }
                    })
                    .show();
            return;
        }

        //フラグメントアクティブ
        isActive=true;

        //GPSパーミッションチェック
        checkGPSPermission();
    }

    /* 停止時処理 */
    private void reset()
    {
        //フラグメント非アクティブ
        isActive=false;

        //位置情報測位停止
        if (requestingLocationUpdates) {
            stopLocationUpdates();
        }

        //マーカークリア
        for(Marker marker:markerList)
        {
            marker.remove();
        }

        //吹き出し画像クリア
        for(Bitmap bitmap:balloonBitmaps)
        {
            Function.cleanupBitmap(bitmap);
        }

        //各種変数リセット
        gmap=null;
        fusedLocationProviderClient=null;
        settingsClient=null;
        locationCallback=null;
        locationRequest=null;
        locationSettingsRequest=null;
        nowLocation=null;
        locationMarker=null;

        balloonTypeNames.clear();
        balloonBitmaps.clear();
        markerList.clear();
        dispBalloonTypes.clear();
        markerIdentifier.clear();
    }

    /* GPSパーミッションチェック */
    private void checkGPSPermission()
    {
        //APIレベルが23以上なら
        if(Build.VERSION.SDK_INT>=23)
        {
            //GPSパーミッションが許可されているなら
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
            {
                //マップ初期化
                activateFragment();
            }

            //GPSパーミッションが許可されていないなら
            else
            {
                //以前に許可をリクエストしたことがあるなら
                if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    //許可リクエスト
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Constants.Permission.REQUEST_CODE_ACCESS_FINE_LOCATION);
                }

                //初めてのリクエストなら
                else
                {
                    //トーストで説明表示
                    Toast toast = Toast.makeText(getActivity(),"許可されないとホーム画面の機能を利用することができません",Toast.LENGTH_SHORT);
                    toast.show();

                    //許可リクエスト
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Constants.Permission.REQUEST_CODE_ACCESS_FINE_LOCATION);
                }
            }
        }

        //APIレベルが23未満なら
        else
        {
            //マップ初期化
            activateFragment();
        }
    }

    /* パーミッションリクエスト結果コールバック */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //GPSパーミッションのリクエストのとき
        if(requestCode==Constants.Permission.REQUEST_CODE_ACCESS_FINE_LOCATION)
        {
            //承認された場合
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                //マップ初期化
                activateFragment();
            }

            //拒否された場合
            else
            {
                //トーストで警告表示
                Toast toast = Toast.makeText(getActivity(),"許可されないとホーム画面の機能を利用することができません",Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    /* マップ初期化 */
    private void activateFragment() {
        //GPS初期化
        activateGPS();

        //GoogleMap初期化
        activateGoogleMap();

        //測位スタート
        startLocationUpdates();
    }

    /* GPS初期化 */
    private void activateGPS()
    {
        //位置情報プロバイダー取得
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        //位置情報プロバイダー設定取得
        settingsClient = LocationServices.getSettingsClient(getActivity());

        //位置情報取得時コールバック設定
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //位置情報保持
                double lat = locationResult.getLastLocation().getLatitude() ;
                double lng = locationResult.getLastLocation().getLongitude() ;
                nowLocation = new LatLng(lat, lng);

                //GoogleMapが有効のとき
                if (gmap != null) {
                    //ユーザ位置マーカーが存在しないとき
                    if(locationMarker==null) {
                        //ユーザ位置マーカー生成
                        locationMarker=gmap.addMarker(new MarkerOptions().position(nowLocation).title("Your Location"));

                        //マーカーリストに登録
                        markerList.add(locationMarker);

                        //詳細ウィンドウ情報登録
                        markerIdentifier.put(locationMarker.getId(),new SpotInfoWindowInfo(nowLocation.latitude,nowLocation.longitude,Constants.Map.MARKER_ID_MY_LOCATION,null,null,null,null));
                    }
                    //ユーザ位置マーカーが存在するとき
                    else
                    {
                        //マーカー位置更新
                        locationMarker.setPosition(nowLocation);
                    }
                }
            }
        };

        //位置情報設定生成インスタンス生成
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        //位置情報設定セット
        builder.addLocationRequest(locationRequest);

        //位置情報設定生成
        locationSettingsRequest = builder.build();
        locationRequest = LocationRequest.create();

        //測位モード設定
        locationRequest.setPriority(Constants.GPS.LOCATION_PRIORITY);

        //測位モードに応じて測位間隔セット
        switch(Constants.GPS.LOCATION_PRIORITY) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                locationRequest.setInterval(Constants.GPS.UPDATE_INTERVAL_HIGH);
                locationRequest.setFastestInterval(Constants.GPS.FASTEST_UPDATE_INTERVAL);
                break;
            case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
                locationRequest.setInterval(Constants.GPS.UPDATE_INTERVAL_LOW);
                locationRequest.setFastestInterval(Constants.GPS.FASTEST_UPDATE_INTERVAL);
                break;
            case LocationRequest.PRIORITY_LOW_POWER:
                break;
            case LocationRequest.PRIORITY_NO_POWER:
                break;
        }
    }

    /* GoogleMap初期化 */
    private void activateGoogleMap()
    {
        /* GoogleMap表示 */

        //GoogleMapフラグメント生成
        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();

        //GoogleMap準備完了時コールバック設定
        supportMapFragment.getMapAsync(this);

        //トランザクション生成
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        //FrameLayoutに組み込む
        fragmentTransaction.replace(R.id.tourism_map_sub_fragment_frameLayout0, supportMapFragment);

        //遷移実行
        fragmentTransaction.commit();
    }

    /* GoogleMap準備完了時コールバック */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //GoogleMapインスタンス保持
        gmap = googleMap;

        //マーカーセット
        addMarkers();

        //領域・ヒント円描画
        drawRegion();

        //カメラ設定
        setCamera();
    }

    /* マーカーセット */
    private void addMarkers() {

        //マーカータッチ時詳細ウィンドウ表示設定
        gmap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            /* 詳細ウィンドウ表示 */
            @Override
            public View getInfoWindow(Marker marker) {

                //登録されているマーカーなら
                if(markerIdentifier.containsKey(marker.getId())) {
                    View view=null;

                    //ウィンドウ情報取得
                    SpotInfoWindowInfo tempInfo=markerIdentifier.get(marker.getId());

                    //マーカー種類に応じてビュー加工
                    switch(tempInfo.getType()) {
                        //ユーザ位置マーカーなら
                        case Constants.Map.MARKER_ID_MY_LOCATION:
                            //何も表示しない
                            break;

                        //お題スポットマーカーなら
                        case Constants.Map.MARKER_ID_THEME_SPOT: {
                            view = getLayoutInflater().inflate(R.layout.tourism_map_info_window, null);

                            //タイトルテキストビュー取得
                            TextView title =view.findViewById(R.id.tourism_map_info_window_title);

                            //写真ビュー取得
                            ImageView image =view.findViewById(R.id.tourism_map_info_window_image);

                            //説明テキストビュー取得
                            TextView description =view.findViewById(R.id.tourism_map_info_window_description);

                            //口コミタイトルテキストビュー取得
                            TextView reviewTitle=view.findViewById(R.id.tourism_map_info_window_review_title);

                            //口コミ表示取得(3件分)
                            LinearLayout[] reviewLayoutList=new LinearLayout[Constants.Map.MAX_DISPLAYED_THEME_SPOT_REVIEW_NUM];
                            reviewLayoutList[0]=view.findViewById(R.id.tourism_map_info_window_review_layout0);
                            reviewLayoutList[1]=view.findViewById(R.id.tourism_map_info_window_review_layout1);
                            reviewLayoutList[2]=view.findViewById(R.id.tourism_map_info_window_review_layout2);

                            //口コミテキストビュー取得(3件分)
                            TextView[] reviewTextList=new TextView[Constants.Map.MAX_DISPLAYED_THEME_SPOT_REVIEW_NUM];
                            reviewTextList[0]=view.findViewById(R.id.tourism_map_info_window_review_text0);
                            reviewTextList[1]=view.findViewById(R.id.tourism_map_info_window_review_text1);
                            reviewTextList[2]=view.findViewById(R.id.tourism_map_info_window_review_text2);

                            //タイトルセット
                            title.setText(tempInfo.getTitle());

                            //写真セット
                            image.setImageBitmap(tempInfo.getImage());

                            //説明セット
                            description.setText(tempInfo.getDescription());

                            //口コミが存在しないなら
                            if(tempInfo.getReviewList().size()==0)
                            {
                                //口コミタイトル表示OFF
                                reviewTitle.setVisibility(View.GONE);
                            }

                            //口コミが存在するなら表示する
                            for(int i=0;i<Constants.Map.MAX_DISPLAYED_THEME_SPOT_REVIEW_NUM;i++)
                            {
                                if(i<tempInfo.getReviewList().size())
                                {
                                    reviewLayoutList[i].setVisibility(View.VISIBLE);
                                    reviewTextList[i].setText(tempInfo.getReviewList().get(i));
                                }
                                else
                                {
                                    reviewLayoutList[i].setVisibility(View.GONE);
                                }
                            }
                        }
                        break;

                        //イベントスポットマーカーなら
                        case Constants.Map.MARKER_ID_EVENT_SPOT: {
                            view = getLayoutInflater().inflate(R.layout.tourism_map_info_window3, null);

                            //タイトルテキストビュー取得
                            TextView title =view.findViewById(R.id.tourism_map_info_window3_title);

                            //説明テキストビュー取得
                            TextView description =view.findViewById(R.id.tourism_map_info_window3_description);

                            //タイトルセット
                            title.setText(tempInfo.getTitle());

                            //説明セット
                            description.setText(tempInfo.getDescription());
                        }
                        break;

                        //口コミマーカーなら
                        case Constants.Map.MARKER_ID_REVIEW_SPOT: {
                            view = getLayoutInflater().inflate(R.layout.tourism_map_info_window2, null);

                            //写真ビュー取得
                            ImageView image =view.findViewById(R.id.tourism_map_info_window2_image);

                            //説明テキストビュー取得
                            TextView description =view.findViewById(R.id.tourism_map_info_window2_description);

                            //写真セット
                            image.setImageBitmap(tempInfo.getImage());

                            //説明セット
                            description.setText(tempInfo.getDescription());
                        }
                        break;

                        //それ以外（ダウンロード分）なら
                        default:
                        {
                            //説明が存在するなら
                            if(tempInfo.getDescription()!=null&&0<tempInfo.getDescription().length())
                            {
                                view = getLayoutInflater().inflate(R.layout.tourism_map_info_window4, null);

                                //説明テキストビュー取得
                                TextView description =view.findViewById(R.id.tourism_map_info_window4_description);

                                //説明セット
                                description.setText(tempInfo.getDescription());
                            }
                        }
                        break;
                    }

                    return view;
                }
                return null;
            }

            //InfoContentsは使用しない
            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

        //吹き出し情報取得HTTP通信実行
        balloonDownloadConnection();
    }

    /* 吹き出し情報取得HTTP通信 */
    private void balloonDownloadConnection()
    {
        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONに地名追加
            json.put("region_name",regionName);

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String data) {
                    //フラグメントが破棄されているなら
                    if(!isActive)
                    {
                        //コールバック処理中断
                        return;
                    }

                    //受信データが空なら（通信失敗時）
                    if (data == "")
                    {
                        //トーストで警告表示
                        Toast.makeText(getContext(), "吹き出し情報のダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();

                        //コールバック処理中断
                        return;
                    }

                    try {
                        //受信データからJSON生成
                        JSONObject json = new JSONObject(data);

                        //"status"パラメータ取得
                        Boolean status = json.getBoolean("status");

                        //"status"パラメータがtrueなら（通信成功時）
                        if (status) {
                            //"result"パラメータ取得
                            JSONArray result = json.getJSONArray("result");

                            //画像ダウンロード待ち吹き出し種類数セット
                            waitingLoadBalloonBitmapNum =result.length();

                            //各吹き出し種類について
                            for(int i=0;i<result.length();i++)
                            {
                                //iの定数宣言
                                final int final_i=i;

                                //フラグメントが破棄されているなら
                                if(!isActive)
                                {
                                    //コールバック処理中断
                                    return;
                                }

                                //名前取得
                                balloonTypeNames.add(result.getJSONObject(i).getString("name"));

                                //HTTP通信インスタンス生成
                                HttpBitmapDownloadAsync httpBitmapDownloadAsync = new HttpBitmapDownloadAsync();

                                //通信終了後処理設定
                                httpBitmapDownloadAsync.setCallBack(new HttpBitmapDownloadAsync.CallBack() {
                                    @Override
                                    public void onAsyncTaskResult(Bitmap bitmap) {
                                        //フラグメントが破棄されているなら
                                        if(!isActive)
                                        {
                                            //コールバック処理中断
                                            return;
                                        }

                                        //受信データが空なら（通信失敗時）
                                        if(bitmap==null)
                                        {
                                            //トーストで警告
                                            Toast.makeText(getContext(), "アイコン画像のダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();

                                            //コールバック処理中断
                                            return;
                                        }

                                        //拡大させた画像を取得
                                        bitmap=Bitmap.createScaledBitmap(bitmap,90,135,false);

                                        //吹き出し画像セット
                                        balloonBitmaps.set(balloonTypeOffset+final_i,bitmap);

                                        //ダウンロード待ちカウント消費
                                        waitingLoadBalloonBitmapNum--;

                                        //全ての画像がダウンロードされたら
                                        if(waitingLoadBalloonBitmapNum ==0)
                                        {
                                            //各種マーカーセット
                                            setMyLocationMarker();
                                            setThemeSpotMarker();
                                            setEventSpotMarker();
                                            setReviewSpotMarker();
                                            setDownloadedMarkSpotMarker();
                                        }
                                    }
                                });

                                //HTTP通信実行
                                httpBitmapDownloadAsync.execute("https://" + Constants.Http.SERVER_IP + "/open_data/photos/balloon/" + result.getJSONObject(i).getInt("balloon_id") + ".png");
                            }

                            //吹き出しダウンロード完了
                            balloonDownloaded =true;
                        }

                        //"status"パラメータがfalseなら（webサーバーエラー発生時）
                        else {
                            //トーストで警告
                            Toast.makeText(getContext(), "吹き出し情報のダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                        }
                    }

                    //JSON例外発生時
                    catch (JSONException e) {
                        //ログに例外メッセージ出力
                        e.printStackTrace();

                        //トーストで警告
                        Toast.makeText(getContext(), "吹き出し情報のダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/open_data/get_balloon_type.php", json.toString());
        }

        //JSON例外発生時
        catch (JSONException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();

            //トーストで警告
            Toast.makeText(getContext(), "吹き出し情報のダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
        }
    }

    /* ユーザ位置マーカー表示 */
    private void setMyLocationMarker()
    {
        //カメラ初期位置
        LatLng defaultLocation = new LatLng(Constants.Map.DEFAULT_LATITUDE, Constants.Map.DEFAULT_LONGITUDE);

        //ユーザ位置マーカー生成
        locationMarker = gmap.addMarker(new MarkerOptions().position(defaultLocation).title("Your Location"));

        //ユーザ位置マーカー表示がOFFなら
        if(!dispBalloonTypes.get(Constants.Map.MARKER_ID_MY_LOCATION))
        {
            //非表示にする
            locationMarker.setVisible(false);
        }

        //マーカーリストに登録
        markerList.add(locationMarker);

        //詳細ウィンドウ情報登録
        markerIdentifier.put(locationMarker.getId(), new SpotInfoWindowInfo(defaultLocation.latitude, defaultLocation.longitude, Constants.Map.MARKER_ID_MY_LOCATION, null, null, null, null));
    }

    /* 発見済みお題スポットマーカー表示 */
    private void setThemeSpotMarker()
    {
        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONに各パラメータ追加
            json.put("user_id", userID);
            json.put("region_name",regionName);
            json.put("found_state", Constants.Search.STATE_FOUND);

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String data) {
                    //フラグメントが破棄されているなら
                    if(!isActive)
                    {
                        //コールバック処理中断
                        return;
                    }

                    //受信データが空なら（通信失敗時）
                    if (data == "")
                    {
                        //トーストで警告
                        Toast.makeText(getContext(), "お題スポットのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();

                        //コールバック処理中断
                        return;
                    }

                    try {
                        //受信データからJSON生成
                        JSONObject json = new JSONObject(data);

                        //"status"パラメータ取得
                        Boolean status = json.getBoolean("status");

                        //"status"パラメータがtrueなら（通信成功時）
                        if (status) {
                            //"result"パラメータ(発見済みお題スポット)取得
                            JSONArray dataArray = json.getJSONArray("result");

                            //各お題スポットについて
                            for (int i = 0; i < dataArray.length(); i++) {
                                //各種パラメータ取得
                                JSONObject result = dataArray.getJSONObject(i);
                                final int id = result.getInt("theme_id");
                                final double latitude = result.getDouble("latitude");
                                final double longitude = result.getDouble("longitude");
                                final String name = result.getString("location_name");
                                final String description = result.getString("description");

                                //発見済みお題スポットマーカー表示2実行
                                setThemeSpotMarker2(id,latitude,longitude,name,description);
                            }
                        }

                        //"status"パラメータがfalseなら（webサーバーエラー発生時）
                        else
                        {
                            //トーストで警告
                            Toast.makeText(getContext(), "お題スポットのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                        }
                    }

                    //JSON例外発生時
                    catch (JSONException e) {
                        //ログに例外メッセージ出力
                        e.printStackTrace();

                        //トーストで警告
                        Toast.makeText(getContext(), "お題スポットのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/open_data/map_theme_spot.php", json.toString());
        }

        //JSON例外発生時
        catch (JSONException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();

            //トーストで警告
            Toast.makeText(getContext(), "お題スポットのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
        }
    }

    /* 覇権済みお題スポットマーカー表示2 */
    private void setThemeSpotMarker2(final int id,final double latitude,final double longitude,final String name,final String description)
    {
        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONに各パラメータ追加
            json.put("region_name", regionName);
            json.put("location_name", name);

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String data) {
                    //フラグメントが破棄されているなら
                    if(!isActive)
                    {
                        //コールバック処理中断
                        return;
                    }

                    //受信データが空なら（通信失敗時）
                    if (data == "") {
                        //トーストで警告
                        Toast.makeText(getContext(), "お題スポットレビューのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                        //コールバック処理中断
                        return;
                    }

                    try {
                        //受信データからJSON生成
                        JSONObject json = new JSONObject(data);

                        //"status"パラメータ取得
                        Boolean status = json.getBoolean("status");

                        //"status"パラメータがtrueなら（通信成功時）
                        if (status) {
                            //"result"パラメータ取得
                            JSONArray dataArray = json.getJSONArray("result");

                            //口コミリスト生成
                            final ArrayList<String> reviewList = new ArrayList<>();

                            //口コミ取得
                            for (int j = 0; j < dataArray.length(); j++) {
                                reviewList.add(dataArray.getJSONObject(j).getString("comment"));
                            }

                            //HTTP通信インスタンス生成
                            HttpBitmapDownloadAsync httpBitmapDownloadAsync = new HttpBitmapDownloadAsync();

                            //通信終了後処理設定
                            httpBitmapDownloadAsync.setCallBack(new HttpBitmapDownloadAsync.CallBack() {
                                @Override
                                public void onAsyncTaskResult(Bitmap bitmap) {
                                    //フラグメントが破棄されているなら
                                    if(!isActive)
                                    {
                                        //コールバック処理中断
                                        return;
                                    }

                                    //受信データが空なら（通信失敗時）
                                    if(bitmap==null)
                                    {
                                        //トーストで警告
                                        Toast.makeText(getContext(), "お題スポット写真のダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();

                                        //コールバック処理中断
                                        return;
                                    }

                                    //お題スポット位置（緯度経度）生成
                                    LatLng themeLocation = new LatLng(latitude, longitude);

                                    //マーカーオプション生成
                                    MarkerOptions tempMarkerOptions = new MarkerOptions();

                                    //位置セット
                                    tempMarkerOptions.position(themeLocation);

                                    //吹き出し画像セット
                                    BitmapDescriptor tempIcon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker1);
                                    tempMarkerOptions.icon(tempIcon);

                                    //マーカーセット
                                    Marker tempMarker = gmap.addMarker(tempMarkerOptions);

                                    //お題スポット表示がOFFなら
                                    if(!dispBalloonTypes.get(Constants.Map.MARKER_ID_THEME_SPOT))
                                    {
                                        //非表示にする
                                        tempMarker.setVisible(false);
                                    }

                                    //マーカーリストに登録
                                    markerList.add(tempMarker);

                                    //詳細ウィンドウ情報登録
                                    markerIdentifier.put(tempMarker.getId(), new SpotInfoWindowInfo(latitude, longitude, Constants.Map.MARKER_ID_THEME_SPOT, name, description, bitmap, reviewList));
                                }
                            });

                            //HTTP通信実行
                            httpBitmapDownloadAsync.execute("https://" + Constants.Http.SERVER_IP + "/open_data/photos/theme/" + id + ".jpg");
                        }

                        //"status"パラメータがfalseなら(webサーバーエラー発生時)
                        else {
                            //トーストで警告
                            Toast.makeText(getContext(), "お題スポットレビューのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                        }
                    }

                    //JSON例外発生時
                    catch (JSONException e) {
                        //ログに例外メッセージ出力
                        e.printStackTrace();

                        //トーストで警告
                        Toast.makeText(getContext(), "お題スポットレビューのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/review/download_theme_review.php", json.toString());
        }

        //JSON例外発生時
        catch (JSONException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();

            //トーストで警告
            Toast.makeText(getContext(), "お題スポットレビューのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
        }
    }

    /* イベントスポット表示 */
    private void setEventSpotMarker()
    {
        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONに地名追加
            json.put("region_name", regionName);

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String str) {
                    //フラグメントが破棄されているなら
                    if(!isActive)
                    {
                        //コールバック処理中断
                        return;
                    }

                    //受信データが空なら（通信失敗時）
                    if (str == "")
                    {
                        //トーストで警告
                        Toast.makeText(getContext(), "イベント情報のダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();

                        //コールバック処理中断
                        return;
                    }

                    try {
                        //受信データからJSON生成
                        JSONObject json = new JSONObject(str);

                        //"status"パラメータ取得
                        Boolean status = json.getBoolean("status");

                        //"status"パラメータがtrueなら（通信成功時）
                        if (status) {
                            //"result"パラメータ(イベント情報)取得
                            JSONArray dataArray = json.getJSONArray("result");

                            //各イベント情報について
                            for (int i = 0; i < dataArray.length(); i++) {
                                //各パラメータ取得
                                JSONObject data = dataArray.getJSONObject(i);
                                final int id = data.getInt("id");
                                final double latitude = data.getDouble("latitude");
                                final double longitude = data.getDouble("longitude");
                                final String title = data.getString("title");
                                final String description = data.getString("description");

                                //イベントスポット位置（緯度経度）生成
                                LatLng eventLocation = new LatLng(latitude, longitude);

                                //マーカーオプション生成
                                MarkerOptions tempMarkerOptions = new MarkerOptions();

                                //位置セット
                                tempMarkerOptions.position(eventLocation);

                                //マーカー画像セット
                                BitmapDescriptor tempIcon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker2);
                                tempMarkerOptions.icon(tempIcon);

                                //マーカーセット
                                Marker tempMarker = gmap.addMarker(tempMarkerOptions);

                                //イベントスポットが非表示設定なら
                                if(!dispBalloonTypes.get(Constants.Map.MARKER_ID_EVENT_SPOT))
                                {
                                    //非表示にする
                                    tempMarker.setVisible(false);
                                }

                                //マーカーリストに登録
                                markerList.add(tempMarker);

                                //詳細ウィンドウ情報登録
                                markerIdentifier.put(tempMarker.getId(), new SpotInfoWindowInfo(latitude, longitude, Constants.Map.MARKER_ID_EVENT_SPOT, title, description, null, null));
                            }
                        }

                        //"status"パラメータがfalseなら（webサーバーエラー発生時）
                        else {
                            //トーストで警告
                            Toast.makeText(getContext(), "イベント情報のダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                        }
                    }

                    //JSON例外発生時
                    catch (JSONException e) {
                        //ログに例外メッセージ出力
                        e.printStackTrace();

                        //トーストで警告
                        Toast.makeText(getContext(), "イベント情報のダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/open_data/map_event_spot.php",json.toString());
        }

        //JSON例外発生時
        catch (JSONException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();

            //トーストで警告
            Toast.makeText(getContext(), "イベント情報のダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
        }
    }

    /* 口コミスポット表示 */
    private void setReviewSpotMarker()
    {
        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONに地名追加
            json.put("region_name", regionName);

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String data) {
                    //フラグメントが破棄されているなら
                    if(!isActive)
                    {
                        //コールバック処理中断
                        return;
                    }

                    //受信データが空なら（通信失敗時）
                    if (data == "")
                    {
                        //トーストで警告
                        Toast.makeText(getContext(), "口コミスポットのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();

                        //コールバック処理中断
                        return;
                    }

                    try {
                        //受信データからJSON生成
                        JSONObject json = new JSONObject(data);

                        //"status"パラメータ取得
                        Boolean status = json.getBoolean("status");

                        //"status"パラメータがtrueなら（通信成功時）
                        if (status) {
                            //"result"パラメータ（口コミスポット）取得
                            JSONArray dataArray = json.getJSONArray("result");

                            //各口コミスポットについて
                            for (int i = 0; i < dataArray.length(); i++) {
                                //各パラメータ取得
                                JSONObject result = dataArray.getJSONObject(i);
                                final int id = result.getInt("review_id");
                                final double latitude =result.getDouble("latitude");
                                final double longitude = result.getDouble("longitude");
                                final String description = result.getString("message");

                                //HTTP通信インスタンス生成
                                HttpBitmapDownloadAsync httpBitmapDownloadAsync = new HttpBitmapDownloadAsync();

                                //通信終了後処理設定
                                httpBitmapDownloadAsync.setCallBack(new HttpBitmapDownloadAsync.CallBack() {
                                    @Override
                                    public void onAsyncTaskResult(Bitmap bitmap) {
                                        //フラグメントが破棄されているなら
                                        if(!isActive)
                                        {
                                            //コールバック処理中断
                                            return;
                                        }

                                        //受信データが空なら（通信失敗時）
                                        if(bitmap==null)
                                        {
                                            //トーストで警告
                                            Toast.makeText(getContext(), "口コミスポット写真のダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();

                                            //コールバック処理中断
                                            return;
                                        }

                                        //口コミスポット位置（緯度経度）生成
                                        LatLng reviewLocation = new LatLng(latitude, longitude);

                                        //マーカーオプション生成
                                        MarkerOptions tempMarkerOptions = new MarkerOptions();

                                        //位置セット
                                        tempMarkerOptions.position(reviewLocation);

                                        //マーカー画像セット
                                        BitmapDescriptor tempIcon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker3);
                                        tempMarkerOptions.icon(tempIcon);

                                        //マーカー生成
                                        Marker tempMarker = gmap.addMarker(tempMarkerOptions);

                                        //口コミスポットが非表示設定なら
                                        if(!dispBalloonTypes.get(Constants.Map.MARKER_ID_REVIEW_SPOT))
                                        {
                                            //非表示にする
                                            tempMarker.setVisible(false);
                                        }

                                        //マーカーリストに登録
                                        markerList.add(tempMarker);

                                        //詳細ウィンドウ情報登録
                                        markerIdentifier.put(tempMarker.getId(), new SpotInfoWindowInfo(latitude, longitude, Constants.Map.MARKER_ID_REVIEW_SPOT, null, description, bitmap, null));
                                    }
                                });

                                //HTTP通信実行
                                httpBitmapDownloadAsync.execute("https://" + Constants.Http.SERVER_IP + "/review/img/image" + id + ".jpg");
                            }
                        }

                        //"status"パラメータがfalseなら（webサーバーエラー発生時）
                        else {
                            //トーストで警告
                            Toast.makeText(getContext(), "口コミスポットのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                        }
                    }

                    //JSON例外発生時
                    catch (JSONException e) {
                        //ログに例外メッセージ出力
                        e.printStackTrace();

                        //トーストで警告
                        Toast.makeText(getContext(), "口コミスポットのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/review/review_download.php", json.toString());
        }

        //JSON例外発生時
        catch (JSONException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();

            //トーストで警告
            Toast.makeText(getContext(), "口コミスポットのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
        }
    }

    /* ダウンロードマーカー表示 */
    private void setDownloadedMarkSpotMarker()
    {
        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONに地名追加
            json.put("region_name",regionName);

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String data) {
                    //フラグメントが破棄されているなら
                    if(!isActive)
                    {
                        //コールバック処理中断
                        return;
                    }

                    //受信データが空なら（通信失敗時）
                    if (data == "")
                    {
                        //トーストで警告
                        Toast.makeText(getContext(), "マーカーのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();

                        //コールバック処理中断
                        return;
                    }

                    try {
                        //受信データからJSON取得
                        JSONObject json = new JSONObject(data);

                        //"status"パラメータ取得
                        Boolean status = json.getBoolean("status");

                        //"status"パラメータがtrueなら（通信成功時）
                        if(status)
                        {
                            //"result"パラメータ（ダウンロードマーカー）取得
                            JSONArray dataArray = json.getJSONArray("result");

                            //各ダウンロードマーカーについて
                            for (int i = 0; i < dataArray.length(); i++) {
                                //各パラメータ取得
                                JSONObject result = dataArray.getJSONObject(i);
                                final int type=result.getInt("type");
                                final double latitude = result.getDouble("latitude");
                                final double longitude = result.getDouble("longitude");
                                final String title = result.getString("location_name");
                                final String description = result.getString("description");

                                //ダウンロードマーカー位置（緯度経度）生成
                                LatLng downloadedMarkerLocation = new LatLng(latitude, longitude);

                                //マーカーオプション生成
                                MarkerOptions tempMarkerOptions = new MarkerOptions();

                                //位置セット
                                tempMarkerOptions.position(downloadedMarkerLocation);

                                //フラグメントが破棄されているなら
                                if(balloonBitmaps.size()==0||balloonBitmaps.get(balloonTypeOffset+type).isRecycled())
                                {
                                    //コールバック処理中断
                                    break;
                                }

                                //マーカー画像セット
                                tempMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(balloonBitmaps.get(balloonTypeOffset+type)));

                                //マーカー生成
                                Marker tempMarker = gmap.addMarker(tempMarkerOptions);

                                //ダウンロードマーカー表示がOFFになっていたら
                                if(!dispBalloonTypes.get(balloonTypeOffset+type))
                                {
                                    //非表示にする
                                    tempMarker.setVisible(false);
                                }

                                //マーカーリストに登録
                                markerList.add(tempMarker);

                                //詳細ウィンドウ情報登録
                                markerIdentifier.put(tempMarker.getId(), new SpotInfoWindowInfo(latitude, longitude, balloonTypeOffset+type, title, description, null, null));
                            }
                        }

                        //"status"パラメータがfalseなら（webサーバーエラー発生時）
                        else
                        {
                            //トーストで警告
                            Toast.makeText(getContext(), "マーカーのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                        }
                    }

                    //JSON例外発生時
                    catch (JSONException e) {
                        //ログに例外メッセージ出力
                        e.printStackTrace();

                        //トーストで警告
                        Toast.makeText(getContext(), "マーカーのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/open_data/map_mark_spot.php", json.toString());
        }

        //JSON例外発生時
        catch (JSONException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();

            //トーストで警告
            Toast.makeText(getContext(), "マーカーのダウンロードに失敗しました。", Toast.LENGTH_SHORT).show();
        }
    }

    /* 領域・ヒント円描画 */
    private void drawRegion()
    {
        //八戸市中心街領域描画
        PolygonOptions options=new PolygonOptions();
        options.add(new LatLng(40.505,141.479));
        options.add(new LatLng(40.517,141.479));
        options.add(new LatLng(40.517,141.496));
        options.add(new LatLng(40.505,141.496));
        options.strokeColor(Color.RED);
        options.strokeWidth(6);
        gmap.addPolygon(options);
    }

    /* カメラ初期化 */
    private void setCamera()
    {
        //初期位置生成
        LatLng defaultLocation = new LatLng(Constants.Map.DEFAULT_LATITUDE, Constants.Map.DEFAULT_LONGITUDE);

        //カメラ変更インスタンス生成
        CameraPosition.Builder builder=new CameraPosition.Builder(gmap.getCameraPosition());

        //ズーム率セット
        builder.zoom(Constants.Map.DEFAULT_ZOOM_RATE);

        //初期位置セット
        builder.target(defaultLocation);

        //カメラ更新
        gmap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
    }

    /* 測位開始 */
    private void startLocationUpdates()
    {
        //位置情報取得設定確認
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {

            /* 成功時 */
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //GPSパーミッションが許可されていなければ中断
                if(ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }
                //測位開始
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {

            /* 失敗時 */
            @Override
            public void onFailure(@NonNull Exception e) {
                //測位を開始しない
                if(((ApiException)e).getStatusCode()== LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE)
                {
                    //位置情報リクエストOFF
                    requestingLocationUpdates=false;
                }
            }
        });

        //位置情報リクエストON
        requestingLocationUpdates=true;
    }

    /* 測位停止 */
    private void stopLocationUpdates()
    {
        //位置情報取リクエストを無効化する
        fusedLocationProviderClient.removeLocationUpdates(locationCallback).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {

            /* 成功時 */
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //位置情報リクエストOFF
                requestingLocationUpdates=false;
            }
        });
    }

    /* フラグメント生成時 */
    @Override
    public void onResume()
    {
        super.onResume();

        //初期化
        init();
    }

    /* フラグメント一時停止時 */
    @Override
    public void onPause() {
        super.onPause();

        //停止時処理
        reset();
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onTourismMapSubFragmentInteraction(int transitionID,LatLng nowLocation);
    }
}