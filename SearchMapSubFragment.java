package jp.androidbook.photostohint;

/* 探索モードマップフラグメント */

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
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchMapSubFragment extends Fragment implements OnMapReadyCallback {

    //遷移ID
    public static final int TRANSITION_ID_HINT = 0;
    public static final int TRANSITION_ID_CAMERA = 1;
    public static final int TRANSITION_ID_THEME_CHANGE = 2;

    //Bundleキー
    private static final String USER_ID_KEY="USER_ID";
    private static final String REGION_NAME_KEY="REGION_NAME";
    private static final String HINT_LEVEL_KEY="HINT_LEVEL";
    private static final String HINT_LATITUDE0_KEY="HINT_LATITUDE0";
    private static final String HINT_LATITUDE1_KEY="HINT_LATITUDE1";
    private static final String HINT_LATITUDE2_KEY="HINT_LATITUDE2";
    private static final String HINT_LONGITUDE0_KEY="HINT_LONGITUDE0";
    private static final String HINT_LONGITUDE1_KEY="HINT_LONGITUDE1";
    private static final String HINT_LONGITUDE2_KEY="HINT_LONGITUDE2";
    private static final String REST_THEME_NUM_KEY="REST_THEME_NUM";
    private static final String THEME_IMAGE_KEY="THEME_IMAGE";

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

    //ヒント円アニメーションハンドラー
    private Handler circleAnimationHandler;

    //ヒント円アニメーション
    private Runnable circleAnimationRunnable;

    //ヒント円アニメーションカウンター
    private int circleAnimationCounter;

    //ヒント円
    private Circle hintCircle;

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

    //お題スポット写真
    private Bitmap themeImage;

    //ユーザID
    private String userID;

    //ヒントレベル
    private int hintLevel;

    //地名
    private String regionName;

    //未発見お題スポット数
    private int restThemeNum;

    //各レベルヒント円の中心位置（緯度経度）
    private double[] hintLatitudeList=new double[3];
    private double[] hintLongitudeList=new double[3];

    //フラグメントがアクティブか
    private boolean isActive;

    /* コンストラクタ */
    public SearchMapSubFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SearchMapSubFragment newInstance(String userID,String regionName,Bitmap themeImage,int hintLevel,double hintLatitude0,double hintLongitude0,double hintLatitude1,double hintLongitude1,double hintLatitude2,double hintLongitude2,int restThemeNum) {
        //フラグメントインスタンス生成
        SearchMapSubFragment fragment = new SearchMapSubFragment();

        //バンドル生成
        Bundle args = new Bundle();

        //バイト配列出力ストリーム生成
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        //お題スポット写真をJPEG形式でバイト配列出力ストリームに入力
        themeImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        //お題スポット写真のバイト配列をバンドルに追加
        args.putByteArray(THEME_IMAGE_KEY, stream.toByteArray());

        //バンドルに各種パラメータ追加
        args.putInt(HINT_LEVEL_KEY,hintLevel);
        args.putDouble(HINT_LATITUDE0_KEY,hintLatitude0);
        args.putDouble(HINT_LATITUDE1_KEY,hintLatitude1);
        args.putDouble(HINT_LATITUDE2_KEY,hintLatitude2);
        args.putDouble(HINT_LONGITUDE0_KEY,hintLongitude0);
        args.putDouble(HINT_LONGITUDE1_KEY,hintLongitude1);
        args.putDouble(HINT_LONGITUDE2_KEY,hintLongitude2);
        args.putString(USER_ID_KEY,userID);
        args.putString(REGION_NAME_KEY,regionName);
        args.putInt(REST_THEME_NUM_KEY,restThemeNum);

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
            //バイト配列からthemeImage生成
            byte[] themeImageBytes = getArguments().getByteArray(THEME_IMAGE_KEY);
            themeImage = BitmapFactory.decodeByteArray(themeImageBytes, 0, themeImageBytes.length);

            hintLevel=getArguments().getInt(HINT_LEVEL_KEY);
            hintLatitudeList[0]=getArguments().getDouble(HINT_LATITUDE0_KEY);
            hintLatitudeList[1]=getArguments().getDouble(HINT_LATITUDE1_KEY);
            hintLatitudeList[2]=getArguments().getDouble(HINT_LATITUDE2_KEY);
            hintLongitudeList[0]=getArguments().getDouble(HINT_LONGITUDE0_KEY);
            hintLongitudeList[1]=getArguments().getDouble(HINT_LONGITUDE1_KEY);
            hintLongitudeList[2]=getArguments().getDouble(HINT_LONGITUDE2_KEY);
            userID=getArguments().getString(USER_ID_KEY);
            regionName=getArguments().getString(REGION_NAME_KEY);
            restThemeNum=getArguments().getInt(REST_THEME_NUM_KEY);
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_search_map_sub, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //お題スポット画像表示ボタン取得
        final ImageButton themeImageButton=view.findViewById(R.id.search_map_sub_fragment_disp_theme_image_button);

        //ヒントボタン取得
        final ImageButton hintButton=view.findViewById(R.id.search_map_sub_fragment_hint_button);

        //カメラボタン取得
        final ImageButton cameraButton=view.findViewById(R.id.search_map_sub_fragment_camera_button);

        //吹き出し表示変更ボタン取得
        final ImageButton balloonDisplayButton=view.findViewById(R.id.search_map_sub_fragment_balloon_disp_button);

        //カメラリセット（ユーザ位置に合わせる）ボタン取得
        final ImageButton myLocationButton=view.findViewById(R.id.search_map_sub_fragment_my_location_button);

        //表示更新ボタン取得
        final ImageButton updateButton=view.findViewById(R.id.search_map_sub_fragment_update_button);

        //お題変更ボタン取得
        final Button renewThemeButton=view.findViewById(R.id.search_map_sub_fragment_renewal_theme_button);

        //地名テキストビュー取得
        final TextView regionNameTextView=view.findViewById(R.id.search_map_sub_fragment_location_name_text);

        //お題スポット写真画面表示
        final FrameLayout themeImageLayout=view.findViewById(R.id.search_map_sub_fragment_theme_image_layout);

        //お題スポット写真ビュー
        final ImageView themeImageView=view.findViewById(R.id.search_map_sub_fragment_theme_image);

        //地名セット
        regionNameTextView.setText(Constants.Common.TARGET_REGION);

        //お題スポット写真セット
        themeImageView.setImageBitmap(themeImage);


        /* 写真ビューのサイズ設定 */

        //ディスプレイメトリクス生成
        DisplayMetrics dm = new DisplayMetrics();

        //ディスプレイサイズ取得
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        //お題スポット写真表示用イメージビューの横幅をディスプレイ横幅の0.9倍に設定
        double width = 0.9 * dm.widthPixels;

        //上で設定した横幅を基準に16:9でサイズ設定
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) width, (int) (width * Constants.Common.ASPECT_RATIO_VERTICAL / Constants.Common.ASPECT_RATIO_HORIZONTAL));

        //水平方向に中央揃え
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        //お題写真ビューにサイズ設定を適用
        themeImageView.setLayoutParams(layoutParams);


        //お題スポット写真画面表示OFF
        themeImageLayout.setVisibility(View.GONE);

        //お題スポット写真画面表示タッチ時処理
        themeImageLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //お題スポット写真画面表示OFF
                themeImageLayout.setVisibility(View.GONE);
                return true;
            }
        });

        //お題スポット画像表示ボタン押下時処理設定
        themeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //お題スポット写真画面表示ON/OFF切り替え
                switch(themeImageLayout.getVisibility())
                {
                    case View.VISIBLE:
                        themeImageLayout.setVisibility(View.GONE);
                        break;
                    case View.GONE:
                        themeImageLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        //ヒントボタン押下時処理
        hintButton.setOnClickListener(new View.OnClickListener() {
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
                ((CallBack)getParentFragment()).onSearchMapSubFragmentInteraction(0,nowLocation);
            }
        });

        //カメラボタン押下時処理設定
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //測位が完了していないなら実行不可
                if(nowLocation==null)
                {
                    //トーストで警告表示
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
                ((CallBack)getParentFragment()).onSearchMapSubFragmentInteraction(1,nowLocation);
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

        //お題変更ボタン押下時処理設定
        renewThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //最後のお題なら実行不可
                if(restThemeNum==1)
                {
                    //トーストで警告表示
                    Toast toast = Toast.makeText(getActivity(),"これが最後のお題です。",Toast.LENGTH_SHORT);
                    toast.show();
                }

                //他にお題が残っているなら
                else {
                    //アラートダイアログ表示
                    new AlertDialog.Builder(getActivity())
                            .setTitle("確認")
                            .setMessage("お題の変更を行います。本当によろしいですか？")

                            //OKボタン押下時
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //フラグメント遷移コールバック実行
                                    ((CallBack) getParentFragment()).onSearchMapSubFragmentInteraction(2, null);
                                }
                            })

                            //キャンセル押下時
                            .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
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

        //ヒント円アニメーション停止
        if(circleAnimationHandler!=null) {
            circleAnimationHandler.removeCallbacks(circleAnimationRunnable);
            circleAnimationHandler=null;
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
        fragmentTransaction.replace(R.id.search_map_sub_fragment_frameLayout0, supportMapFragment);

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

        //ヒントレベルに応じたヒント円描画
        switch(hintLevel)
        {
            case 1:
                CircleOptions opt=new CircleOptions();
                hintCircle=gmap.addCircle(new CircleOptions().center( new LatLng(hintLatitudeList[0],hintLongitudeList[0])).strokeColor(Color.BLUE).strokeWidth(3).fillColor(getResources().getColor(R.color.colorTransParentBlue)).radius(Constants.Search.HINT_LEVEL1_CIRCLE_RADIUS));
                break;
            case 2:
                hintCircle=gmap.addCircle(new CircleOptions().center( new LatLng(hintLatitudeList[1],hintLongitudeList[1])).strokeColor(Color.BLUE).strokeWidth(3).fillColor(getResources().getColor(R.color.colorTransParentBlue)).radius(Constants.Search.HINT_LEVEL2_CIRCLE_RADIUS));
                break;
            case 3:
                hintCircle=gmap.addCircle(new CircleOptions().center( new LatLng(hintLatitudeList[2],hintLongitudeList[2])).strokeColor(Color.BLUE).strokeWidth(3).fillColor(getResources().getColor(R.color.colorTransParentBlue)).radius(Constants.Search.HINT_LEVEL3_CIRCLE_RADIUS));
                break;
        }

        //ヒント円アニメーション初期化
        circleAnimationCounter=0;
        circleAnimationHandler=new Handler();

        //拡大縮小アニメーション
        circleAnimationRunnable=new Runnable() {
            @Override
            public void run() {
                circleAnimationCounter++;
                switch(hintLevel)
                {
                    case 1:
                        hintCircle.setRadius(Constants.Search.HINT_LEVEL1_CIRCLE_RADIUS+Constants.Map.HINT_CIRCLE_ANIMATION_AMPLITUDE+Constants.Map.HINT_CIRCLE_ANIMATION_AMPLITUDE*Math.sin(2*Math.PI*Constants.Map.HINT_CIRCLE_ANIMATION_FPS/1000.0*circleAnimationCounter));
                        break;
                    case 2:
                        hintCircle.setRadius(Constants.Search.HINT_LEVEL2_CIRCLE_RADIUS+Constants.Map.HINT_CIRCLE_ANIMATION_AMPLITUDE+Constants.Map.HINT_CIRCLE_ANIMATION_AMPLITUDE*Math.sin(2*Math.PI*Constants.Map.HINT_CIRCLE_ANIMATION_FPS/1000.0*circleAnimationCounter));
                        break;
                    case 3:
                        hintCircle.setRadius(Constants.Search.HINT_LEVEL3_CIRCLE_RADIUS+Constants.Map.HINT_CIRCLE_ANIMATION_AMPLITUDE+Constants.Map.HINT_CIRCLE_ANIMATION_AMPLITUDE*Math.sin(2*Math.PI*Constants.Map.HINT_CIRCLE_ANIMATION_FPS/1000.0*circleAnimationCounter));
                        break;
                }
                circleAnimationHandler.postDelayed(this,1000/Constants.Map.HINT_CIRCLE_ANIMATION_FPS);
            }

        };

        //アニメーション開始
        circleAnimationHandler.post(circleAnimationRunnable);
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

    /* フラグメント破棄時 */
    @Override
    public void onDetach() {
        super.onDetach();

        //themeImageクリア
        Function.cleanupBitmap(themeImage);
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
        void onSearchMapSubFragmentInteraction(int transitionID,LatLng nowLocation);
    }
}