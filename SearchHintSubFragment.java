package jp.androidbook.photostohint;

/* 探索モードヒント設定フラグメント */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

public class SearchHintSubFragment extends Fragment {

    //Bundleキー
    private static final String USER_ID_KEY="USER_ID";
    private static final String REGION_NAME_KEY="REGION_NAME";
    private static final String LOCATION_NAME_KEY="LOCATION_NAME";
    private static final String HINT_LEVEL_KEY="HINT_LEVEL";
    private static final String HINT_POINT_KEY="HINT_POINT";
    private static final String SEARCH_LOCATION_STATE_KEY="SEARCH_LOCATION_STATE";
    private static final String HINT_LATITUDE0_KEY="HINT_LATITUDE0";
    private static final String HINT_LATITUDE1_KEY="HINT_LATITUDE1";
    private static final String HINT_LATITUDE2_KEY="HINT_LATITUDE2";
    private static final String HINT_LONGITUDE0_KEY="HINT_LONGITUDE0";
    private static final String HINT_LONGITUDE1_KEY="HINT_LONGITUDE1";
    private static final String HINT_LONGITUDE2_KEY="HINT_LONGITUDE2";
    private static final String THEME_LATITUDE_KEY="THEME_LATITUDE";
    private static final String THEME_LONGITUDE_KEY="THEME_LONGITUDE";

    //ユーザID
    private String userID;

    //地名
    private String regionName;

    //お題スポット名
    private String targetLocationName;

    //ヒントレベル
    private int hintLevel;

    //ヒントポイント
    private int hintPoint;

    //お題スポット探索状態値
    private int searchLocationState;

    //お題スポット位置（緯度経度）
    private double targetLatitude;
    private double targetLongitude;

    //各レベルヒント円中心位置（緯度経度）
    private double[] hintLatitudeList=new double[3];
    private double[] hintLongitudeList=new double[3];

    //選択レベルラジオボタンID
    private int selectedRadioButtonID;

    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //メインコンテンツ画面表示
    private ConstraintLayout mainLayout;

    //戻るボタン
    private ImageButton backButton;

    //ヒントポイントテキストビュー
    private TextView hintPointText;

    //HTTP通信種類
    private int connectionType;

    //RadioButtonID->ヒントレベル
    private HashMap<Integer,Integer> id_to_level=new HashMap<Integer, Integer>();

    //ヒントレベル->必要ヒントポイント
    private HashMap<Integer,Integer> level_to_requiredPoint=new HashMap<Integer, Integer>();

    /* コンストラクタ */
    public SearchHintSubFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SearchHintSubFragment newInstance(String userID,String regionName,String targetLocationName,int hintLevel,double hintLatitude0,double hintLongitude0,double hintLatitude1,double hintLongitude1,double hintLatitude2,double hintLongitude2,int hintPoint,int searchLocationState,double targetLatitude,double targetLongitude) {
        //フラグメントインスタンス生成
        SearchHintSubFragment fragment = new SearchHintSubFragment();

        //バンドル生成
        Bundle args = new Bundle();

        //バンドルに各種パラメータ追加
        args.putString(USER_ID_KEY,userID);
        args.putString(REGION_NAME_KEY,regionName);
        args.putString(LOCATION_NAME_KEY,targetLocationName);
        args.putInt(HINT_LEVEL_KEY,hintLevel);
        args.putInt(HINT_POINT_KEY,hintPoint);
        args.putInt(SEARCH_LOCATION_STATE_KEY,searchLocationState);
        args.putDouble(HINT_LATITUDE0_KEY,hintLatitude0);
        args.putDouble(HINT_LATITUDE1_KEY,hintLatitude1);
        args.putDouble(HINT_LATITUDE2_KEY,hintLatitude2);
        args.putDouble(HINT_LONGITUDE0_KEY,hintLongitude0);
        args.putDouble(HINT_LONGITUDE1_KEY,hintLongitude1);
        args.putDouble(HINT_LONGITUDE2_KEY,hintLongitude2);
        args.putDouble(THEME_LATITUDE_KEY,targetLatitude);
        args.putDouble(THEME_LONGITUDE_KEY,targetLongitude);

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
            targetLocationName=getArguments().getString(LOCATION_NAME_KEY);
            hintLevel=getArguments().getInt(HINT_LEVEL_KEY);
            hintPoint=getArguments().getInt(HINT_POINT_KEY);
            searchLocationState=getArguments().getInt(SEARCH_LOCATION_STATE_KEY);
            hintLatitudeList[0]=getArguments().getDouble(HINT_LATITUDE0_KEY);
            hintLatitudeList[1]=getArguments().getDouble(HINT_LATITUDE1_KEY);
            hintLatitudeList[2]=getArguments().getDouble(HINT_LATITUDE2_KEY);
            hintLongitudeList[0]=getArguments().getDouble(HINT_LONGITUDE0_KEY);
            hintLongitudeList[1]=getArguments().getDouble(HINT_LONGITUDE1_KEY);
            hintLongitudeList[2]=getArguments().getDouble(HINT_LONGITUDE2_KEY);
            targetLatitude=getArguments().getDouble(THEME_LATITUDE_KEY);
            targetLongitude=getArguments().getDouble(THEME_LONGITUDE_KEY);
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_search_hint_sub, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //RadioButtonIDとレベルを紐づける
        id_to_level.put(R.id.search_hint_sub_fragment_radio_button0,1);
        id_to_level.put(R.id.search_hint_sub_fragment_radio_button1,2);
        id_to_level.put(R.id.search_hint_sub_fragment_radio_button2,3);

        //レベルと必要ヒントポイントを紐づける
        level_to_requiredPoint.put(2,Constants.Search.REQUIRED_HINT_POINT_LEVEL_2);
        level_to_requiredPoint.put(3,Constants.Search.REQUIRED_HINT_POINT_LEVEL_3);

        //通信画面表示取得
        connectionLayout=view.findViewById(R.id.search_hint_sub_fragment_download_layout);

        //通信失敗画面表示取得
        connectionFailureLayout=view.findViewById(R.id.search_hint_sub_fragment_connection_failure_layout);

        //メイン近哲画面表示取得
        mainLayout=view.findViewById(R.id.search_hint_sub_fragment_hint_layout);

        //戻るボタン取得
        backButton=view.findViewById(R.id.search_hint_sub_fragment_backButton);

        //ヒントポイントテキストビュー取得
        hintPointText=view.findViewById(R.id.search_hint_sub_fragment_hint_point_text);

        //通信再試行ボタン取得
        final Button retryConnectionButton=view.findViewById(R.id.search_hint_sub_fragment_connection_failure_button);

        //ヒントレベルラジオボタンリスト取得
        final RadioGroup hintLevelRadioGroup=view.findViewById(R.id.search_hint_sub_fragment_radio_group);

        //通信画面表示OFF
        connectionLayout.setVisibility(View.GONE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //メインコンテンツ画面表示ON
        mainLayout.setVisibility(View.VISIBLE);

        //ヒントレベルに対応したラジオボタンIDセット
        switch(hintLevel)
        {
            case 1:
                selectedRadioButtonID=R.id.search_hint_sub_fragment_radio_button0;
                break;
            case 2:
                selectedRadioButtonID=R.id.search_hint_sub_fragment_radio_button1;
                break;
            case 3:
                selectedRadioButtonID=R.id.search_hint_sub_fragment_radio_button2;
                break;
        }

        //初期ヒントレベルに対応したラジオボタン選択
        RadioButton firstSelectedButton=view.findViewById(selectedRadioButtonID);
        firstSelectedButton.setChecked(true);

        //ヒントポイントセット
        hintPointText.setText(""+hintPoint);

        //viewのfinal宣言
        final View final_view=view;

        //ヒントレベルラジオボタン選択時処理
        hintLevelRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, final int checkedId) {
                //解放済みレベルなら
                if(id_to_level.get(checkedId)<=searchLocationState)
                {
                    //レベル変更
                    selectedRadioButtonID=checkedId;

                    //HTTP通信種類セット
                    connectionType=0;

                    //レベル変更HTTP通信実行
                    connection();
                }

                //未開放で現在のレベル+1なら
                else if(id_to_level.get(checkedId)==searchLocationState+1)
                {
                    //所持ヒントポイントが必要ヒントポイント以上なら
                    if(level_to_requiredPoint.get(id_to_level.get(checkedId))<=hintPoint)
                    {
                        //選択されたラジオボタンのIDの定数宣言
                        final int final_checkedId=checkedId;

                        //アラートダイアログ表示
                        new AlertDialog.Builder(getActivity())
                                .setTitle("確認")
                                .setMessage("ヒントポイントを"+level_to_requiredPoint.get(id_to_level.get(checkedId))+"支払ってレベルを上げます。よろしいですか？")

                                //OKボタン
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //ヒントレベル更新
                                        selectedRadioButtonID=checkedId;

                                        //HTTP通信種類セット
                                        connectionType=1;

                                        //ヒントレベルアップHTTP通信実行
                                        connection2();
                                    }
                                })

                                //キャンセルボタン
                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //ラジオボタン選択を元に戻す
                                        RadioButton firstSelectedButton=final_view.findViewById(selectedRadioButtonID);
                                        firstSelectedButton.setChecked(true);
                                    }
                                })
                                .show();
                    }

                    //所持ヒントポイントが足りない場合
                    else
                    {
                        //アラートダイアログ表示
                        new AlertDialog.Builder(getActivity())
                                .setTitle("エラー")
                                .setMessage("レベルを上げるにはヒントポイントが"+level_to_requiredPoint.get(id_to_level.get(checkedId))+"必要です。")

                                //OKボタン
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //ラジオボタン選択を元に戻す
                                        RadioButton firstSelectedButton=final_view.findViewById(selectedRadioButtonID);
                                        firstSelectedButton.setChecked(true);
                                    }
                                })
                                .show();
                    }
                }

                //未開放で現在のレベル+2以上なら
                else
                {
                    //アラートダイアログ表示
                    new AlertDialog.Builder(getActivity())
                            .setTitle("エラー")
                            .setMessage("レベルは1ずつ上げていく必要があります。")

                            //OKボタン押下時
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //ラジオボタン選択を元に戻す
                                    RadioButton firstSelectedButton=final_view.findViewById(selectedRadioButtonID);
                                    firstSelectedButton.setChecked(true);
                                }
                            })
                            .show();

                }
            }
        });

        //戻るボタン押下時処理設定
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //フラグメント遷移コールバック
                ((BackCallBack)getParentFragment()).onSearchHintSubFragmentBackInteraction();
            }
        });

        //通信再試行ボタン押下時処理設定
        retryConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //通信種類を参照して通信選択
                switch(connectionType)
                {
                    case 0:
                        //レベル変更HTTP通信実行
                        connection();
                        break;
                    case 1:
                        //レベルアップHTTP通信実行
                        connection2();
                        break;
                }
            }
        });
    }

    /* レベル変更HTTP通信実行 */
    private void connection()
    {
        //通信画面表示ON
        connectionLayout.setVisibility(View.VISIBLE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //メインコンテンツ画面表示OFF
        mainLayout.setVisibility(View.GONE);

        //戻るボタン表示OFF
        backButton.setVisibility(View.GONE);

        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONに各パラメータ追加
            json.put("user_id",userID);
            json.put("region_name", regionName);
            json.put("target_location_name",targetLocationName);
            json.put("hint_level",id_to_level.get(selectedRadioButtonID));

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String data) {
                    //受信データが空なら（通信失敗時）
                    if (data == "") {
                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示ON
                        connectionFailureLayout.setVisibility(View.VISIBLE);

                        //コールバック処理中断
                        return;
                    }

                    try {
                        //受信データからJSON生成
                        JSONObject json = new JSONObject(data);

                        //"status"パラメータ取得
                        Boolean status = json.getBoolean("status");

                        //"status"パラメータがtrueなら
                        if (status) {
                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //メインコンテンツ画面表示ON
                            mainLayout.setVisibility(View.VISIBLE);

                            //戻るボタン表示ON
                            backButton.setVisibility(View.VISIBLE);

                            //ヒントレベル更新
                            hintLevel=id_to_level.get(selectedRadioButtonID);

                            //レベル変更コールバック実行
                            ((LevelChangeCallBack)getParentFragment()).onSearchHintSubFragmentLevelChangeInteraction(hintLevel,hintPoint,searchLocationState,hintLatitudeList,hintLongitudeList);
                        }

                        //"status"パラメータがfalseなら（webサーバーエラー発生時）
                        else {
                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //通信失敗画面表示ON
                            connectionFailureLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    //JSON例外発生時
                    catch (JSONException e) {
                        //ログに例外メッセージ出力
                        e.printStackTrace();

                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示ON
                        connectionFailureLayout.setVisibility(View.VISIBLE);
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/update_search_region_info.php", json.toString());
        }

        //JSON例外発生時
        catch (JSONException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();

            //通信画面表示OFF
            connectionLayout.setVisibility(View.GONE);

            //通信失敗画面表示ON
            connectionFailureLayout.setVisibility(View.VISIBLE);
        }
    }

    /* ヒントレベルアップHTTP通信 */
    private void connection2()
    {
        //通信画面表示ON
        connectionLayout.setVisibility(View.VISIBLE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //メインコンテンツ画面表示OFF
        mainLayout.setVisibility(View.GONE);

        //戻るボタン表示OFF
        backButton.setVisibility(View.GONE);

        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONに各種パラメータ追加
            json.put("user_id",userID);
            json.put("region_name", regionName);
            json.put("target_location_name",targetLocationName);
            json.put("hint_level",id_to_level.get(selectedRadioButtonID));
            json.put("hint_point",hintPoint-level_to_requiredPoint.get(id_to_level.get(selectedRadioButtonID)));

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String data) {
                    //受信データが空なら（通信失敗時）
                    if (data == "") {
                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示ON
                        connectionFailureLayout.setVisibility(View.VISIBLE);

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
                            //ヒントレベルアップHTTP通信2実行
                            connection2_2();
                        }

                        //"status"パラメータがfalseなら（webサーバーエラー発生時）
                        else {
                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //通信失敗画面表示ON
                            connectionFailureLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    //JSON例外発生時
                    catch (JSONException e) {
                        //ログに例外メッセージ出力
                        e.printStackTrace();

                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示ON
                        connectionFailureLayout.setVisibility(View.VISIBLE);
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/update_search_region_info.php", json.toString());
        }

        //JSON例外発生時
        catch (JSONException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();

            //通信画面表示OFF
            connectionLayout.setVisibility(View.GONE);

            //通信失敗画面表示ON
            connectionFailureLayout.setVisibility(View.VISIBLE);
        }
    }

    /* ヒントレベルアップHTTP通信2 */
    private void connection2_2()
    {
        try {
            //乱数生成インスタンス生成
            Random rand2=new Random();

            //0°～360°の範囲でランダムな角度取得
            double randAngle=rand2.nextDouble()*2*Math.PI;

            //方向決定
            double diffLat=Math.cos(randAngle);
            double diffLng=Math.sin(randAngle);

            //0～1の間のランダムな実数取得
            double randLength=rand2.nextDouble();

            //ヒントレベルに応じて距離設定
            switch(id_to_level.get(selectedRadioButtonID))
            {
                case 1:
                    //0～Lv1ヒント円最大半径の範囲でランダムな距離設定
                    diffLat*=randLength*Constants.Search.HINT_LEVEL1_LOCATION_RADIUS;
                    diffLng*=randLength*Constants.Search.HINT_LEVEL1_LOCATION_RADIUS;

                    //ヒント円中心決定
                    hintLatitudeList[0]=targetLatitude+diffLat;
                    hintLongitudeList[0]=targetLongitude+diffLng;
                    break;
                case 2:
                    //0～Lv2ヒント円最大半径の範囲でランダムな距離設定
                    diffLat*=randLength*Constants.Search.HINT_LEVEL2_LOCATION_RADIUS;
                    diffLng*=randLength*Constants.Search.HINT_LEVEL2_LOCATION_RADIUS;

                    //ヒント円中心決定
                    hintLatitudeList[1]=targetLatitude+diffLat;
                    hintLongitudeList[1]=targetLongitude+diffLng;
                    break;
                case 3:
                    //0～Lv3ヒント円最大半径の範囲でランダムな距離設定
                    diffLat*=randLength*Constants.Search.HINT_LEVEL3_LOCATION_RADIUS;
                    diffLng*=randLength*Constants.Search.HINT_LEVEL3_LOCATION_RADIUS;

                    //ヒント円中心決定
                    hintLatitudeList[2]=targetLatitude+diffLat;
                    hintLongitudeList[2]=targetLongitude+diffLng;
                    break;
            }

            //JSON生成
            JSONObject json2 = new JSONObject();

            //JSONに各種パラメータ追加
            json2.put("user_id",userID);
            json2.put("region_name", regionName);
            json2.put("location_name",targetLocationName);
            json2.put("state",searchLocationState+1);
            json2.put("latitude0",hintLatitudeList[0]);
            json2.put("longitude0",hintLongitudeList[0]);
            json2.put("latitude1",hintLatitudeList[1]);
            json2.put("longitude1",hintLongitudeList[1]);
            json2.put("latitude2",hintLatitudeList[2]);
            json2.put("longitude2",hintLongitudeList[2]);

            //ヒントレベルアップ
            searchLocationState=hintLevel;

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String data) {
                    //受信データが空なら（通信失敗時）
                    if (data == "") {
                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示ON
                        connectionFailureLayout.setVisibility(View.VISIBLE);

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
                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //メインコンテンツ画面表示ON
                            mainLayout.setVisibility(View.VISIBLE);

                            //戻るボタン表示ON
                            backButton.setVisibility(View.VISIBLE);

                            //ヒントレベルアップ
                            hintLevel=id_to_level.get(selectedRadioButtonID);
                            searchLocationState++;

                            //必要ヒントポイント分ヒントポイント消費
                            hintPoint-=level_to_requiredPoint.get(hintLevel);
                            hintPointText.setText(""+hintPoint);

                            //ヒントレベル変更コールバック実行
                            ((LevelChangeCallBack)getParentFragment()).onSearchHintSubFragmentLevelChangeInteraction(hintLevel,hintPoint,searchLocationState,hintLatitudeList,hintLongitudeList);
                        }

                        //"status"パラメータがfalseなら(webサーバーエラー発生時)
                        else {
                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //通信失敗画面表示ON
                            connectionFailureLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    //JSON例外発生時
                    catch (JSONException e) {
                        //ログに例外メッセージ出力
                        e.printStackTrace();

                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示ON
                        connectionFailureLayout.setVisibility(View.VISIBLE);
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/update_search_location_info.php", json2.toString());
        }

        //JSON例外発生時
        catch (JSONException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();

            //通信画面表示OFF
            connectionLayout.setVisibility(View.GONE);

            //通信失敗画面表示ON
            connectionFailureLayout.setVisibility(View.VISIBLE);
        }
    }

    /* フラグメント遷移コールバック */
    public interface BackCallBack {
        void onSearchHintSubFragmentBackInteraction();
    }

    /* ヒントレベル変更コールバック */
    public interface LevelChangeCallBack {
        void onSearchHintSubFragmentLevelChangeInteraction(int hintLevel,int hintPoint,int searchLocationState,double[] hintLatitudeList,double[] hintLongitudeList);
    }
}
