package jp.androidbook.photostohint;

/* 探索モードお題取得フラグメント2 */

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Random;

public class SearchGetThemeSpotSubFragment2 extends Fragment {

    //Bundleキー
    private static final String USER_ID_KEY="USER_ID";
    private static final String REGION_NAME_KEY="REGION_NAME";
    private static final String PRE_LOCATION_NAME_KEY="PRE_LOCATION_NAME";

    //ユーザID
    private String userID;

    //地名
    private String regionName;

    //お題スポット名
    private String targetLocationName;

    //直前に探索していたお題スポット名
    private String preLocationName;

    //お題スポット説明
    private String description;

    //お題スポット探索状態値
    private int searchLocationState;

    //ヒントレベル
    private int hintLevel;

    //Lv1ヒント円の中心位置（緯度経度）
    private double hintLatitude0;
    private double hintLongitude0;

    //Lv2ヒント円の中心位置（緯度経度）
    private double hintLatitude1;
    private double hintLongitude1;

    //Lv3ヒント円の中心位置（緯度経度）
    private double hintLatitude2;
    private double hintLongitude2;

    //ユーザ位置（緯度経度）
    private double latitude;
    private double longitude;

    //お題スポットID
    private int theme_id;

    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //メインコンテンツ画面表示
    private ConstraintLayout mainLayout;

    //取得お題写真ビュー
    private ImageView themeImageView;

    /* コンストラクタ */
    public SearchGetThemeSpotSubFragment2() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SearchGetThemeSpotSubFragment2 newInstance(String userID,String regionName,String preLocationName) {
        //フラグメントインスタンス生成
        SearchGetThemeSpotSubFragment2 fragment = new SearchGetThemeSpotSubFragment2();

        //バンドル生成
        Bundle args = new Bundle();

        //バンドルにユーザID追加
        args.putString(USER_ID_KEY,userID);

        //バンドルに地名追加
        args.putString(REGION_NAME_KEY,regionName);

        //バンドルに直前に探索していたお題スポット名追加
        args.putString(PRE_LOCATION_NAME_KEY,preLocationName);

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
            preLocationName=getArguments().getString(PRE_LOCATION_NAME_KEY);
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_search_get_theme_spot_sub_fragment2, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //メインコンテンツ画面表示取得
        mainLayout=view.findViewById(R.id.search_get_theme_spot_sub_fragment2_image_layout);

        //通信画面表示取得
        connectionLayout=view.findViewById(R.id.search_get_theme_spot_sub_fragment2_download_layout);

        //通信失敗画面表示取得
        connectionFailureLayout=view.findViewById(R.id.search_get_theme_spot_sub_fragment2_connection_failure_layout);

        //お題スポット写真ビュー取得
        themeImageView=view.findViewById(R.id.search_get_theme_spot_sub_fragment2_gotten_image);

        //通信再試行ボタン取得
        final Button retryConnectionButton=view.findViewById(R.id.search_get_theme_spot_sub_fragment2_connection_failure_button);

        //確認ボタン取得
        final Button confirmationButton=view.findViewById(R.id.search_get_theme_spot_sub_fragment2_confirmation_button);

        //確認ボタン押下時処理設定
        confirmationButton.setOnClickListener(new View.OnClickListener() {
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
                ((EndCallBack)getParentFragment()).onSearchGetThemeSpotSubFragment2EndInteraction();
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

                //お題取得HTTP通信実行
                connection();
            }
        });

        //お題取得HTTP通信実行
        connection();
    }

    /* お題取得HTTP通信 */
    private void connection()
    {
        //通信画面表示ON
        connectionLayout.setVisibility(View.VISIBLE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //メインコンテンツ画面表示OFF
        mainLayout.setVisibility(View.GONE);

        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONにユーザID追加
            json.put("user_id", userID);

            //JSONに地名追加
            json.put("region_name", regionName);

            //JSONにお題スポット発見済み状態値追加
            json.put("found_state",Constants.Search.STATE_FOUND);

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
                            //"result"パラメータ取得
                            JSONArray dataArray = json.getJSONArray("result");

                            //"result"パラメータが空なら（webサーバエラー発生時）
                            if (dataArray.length() == 0) {
                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //通信失敗画面表示ON
                                connectionFailureLayout.setVisibility(View.VISIBLE);
                            }

                            //"result"パラメータが空でないなら
                            else {
                                //乱数生成インスタンス生成
                                Random rand = new Random();

                                //0～(取得お題数-1)の範囲でランダムな整数値取得
                                int index = rand.nextInt(dataArray.length());

                                //乱数で選ばれたお題のデータを取得
                                JSONObject newLocationJSON=dataArray.getJSONObject(index);

                                //各種データ取得
                                targetLocationName = newLocationJSON.getString("location_name");
                                searchLocationState=newLocationJSON.getInt("state");
                                hintLatitude0=newLocationJSON.getDouble("latitude0");
                                hintLatitude1=newLocationJSON.getDouble("latitude1");
                                hintLatitude2=newLocationJSON.getDouble("latitude2");
                                hintLongitude0=newLocationJSON.getDouble("longitude0");
                                hintLongitude1=newLocationJSON.getDouble("longitude1");
                                hintLongitude2=newLocationJSON.getDouble("longitude2");

                                //お題変更時なら
                                if(preLocationName!=null) {
                                    //ランダムで選ばれたお題が直前のお題と被った場合
                                    if(targetLocationName.equals(preLocationName))
                                    {
                                        //お題番号を1つ進める
                                        index = (index+1)%dataArray.length();

                                        //各種データ取得
                                        newLocationJSON=dataArray.getJSONObject(index);
                                        targetLocationName = newLocationJSON.getString("location_name");
                                        searchLocationState=newLocationJSON.getInt("state");
                                        hintLatitude0=newLocationJSON.getDouble("latitude0");
                                        hintLatitude1=newLocationJSON.getDouble("latitude1");
                                        hintLatitude2=newLocationJSON.getDouble("latitude2");
                                        hintLongitude0=newLocationJSON.getDouble("longitude0");
                                        hintLongitude1=newLocationJSON.getDouble("longitude1");
                                        hintLongitude2=newLocationJSON.getDouble("longitude2");
                                    }
                                }

                                //お題取得HTTP通信2実行
                                connection2();
                            }
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/get_undiscovered_location_data.php", json.toString());
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

    /* お題取得HTTP通信2 */
    private void connection2()
    {
        try {
            //JSON例外発生時
            JSONObject json2 = new JSONObject();

            //JSONに地名追加
            json2.put("region_name", regionName);

            //JSONにお題スポット名追加
            json2.put("location_name",targetLocationName);

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
                            //"result"パラメータ取得
                            JSONArray dataArray = json.getJSONArray("result");

                            //"result"パラメータが空なら
                            if (dataArray.length() == 0) {
                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //通信失敗画面表示ON
                                connectionFailureLayout.setVisibility(View.VISIBLE);
                            }

                            //"result"パラメータが空でないなら
                            else {
                                /* 各種データ取得 */
                                latitude=dataArray.getJSONObject(0).getDouble("latitude");
                                longitude=dataArray.getJSONObject(0).getDouble("longitude");
                                description=dataArray.getJSONObject(0).getString("description");
                                theme_id=dataArray.getJSONObject(0).getInt("theme_id");

                                //ヒントレベルを1にセット
                                hintLevel = 1;

                                //初めて取得するお題なら
                                if(searchLocationState==0) {
                                    /* Lv1ヒント円の中心を設定 */

                                    //乱数生成インスタンス生成
                                    Random rand = new Random();

                                    //0°～360°の範囲でランダムな角度取得
                                    double randAngle = rand.nextDouble() * 2 * Math.PI;

                                    //0～Lv1ヒント円最大半径の範囲でランダムな半径取得
                                    double randLength=rand.nextDouble()*Constants.Search.HINT_LEVEL1_LOCATION_RADIUS;

                                    //ランダムで生成された角度と半径に基づいて中心計算
                                    double diffLat = randLength * Math.cos(randAngle);
                                    double diffLng = randLength * Math.sin(randAngle);
                                    hintLatitude0 = latitude + diffLat;
                                    hintLongitude0 = longitude + diffLng;
                                }

                                //お題取得HTTP通信3実行
                                connection3();
                            }
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/get_specific_theme_spot_data.php", json2.toString());
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

    /* お題取得HTTP通信3 */
    private void connection3()
    {
        try {
            //JSON生成
            JSONObject json2 = new JSONObject();

            //JSONにユーザID追加
            json2.put("user_id",userID);

            //JSONに地名追加
            json2.put("region_name", regionName);

            //JSONにお題スポット名追加
            json2.put("target_location_name",targetLocationName);

            //JSONにヒントレベル追加
            json2.put("hint_level",hintLevel);

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
                            //お題取得HTTP通信4実行
                            connection4();
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/update_search_region_info.php", json2.toString());
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

    /* お題取得HTTP通信4 */
    private void connection4()
    {
        try {
            //JSON生成
            JSONObject json2 = new JSONObject();

            //JSONにユーザID追加
            json2.put("user_id",userID);

            //JSONに地名追加
            json2.put("region_name", regionName);

            //JSONにお題スポット名追加
            json2.put("location_name",targetLocationName);

            //お題スポット探索状態値がヒントレベルより小さい場合
            if(searchLocationState<hintLevel)
            {
                //お題スポット探索状態値をヒントレベルで上書き
                json2.put("state",hintLevel);
                searchLocationState=hintLevel;
            }

            //JSONにヒント円中心位置追加（緯度経度）
            json2.put("latitude0",hintLatitude0);
            json2.put("longitude0",hintLongitude0);

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
                            //お題取得HTTP通信5実行
                            connection5();
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

    /* お題取得HTTP通信5 */
    private void connection5()
    {
        //HTTP通信インスタンス生成
        HttpBitmapDownloadAsync httpBitmapDownloadAsync=new HttpBitmapDownloadAsync();

        //通信終了後処理設定
        httpBitmapDownloadAsync.setCallBack(new HttpBitmapDownloadAsync.CallBack() {
            @Override
            public void onAsyncTaskResult(Bitmap bitmap) {
                //受信bitmapがnullなら（通信失敗時）
                if(bitmap==null)
                {
                    //通信画面表示OFF
                    connectionLayout.setVisibility(View.GONE);

                    //通信失敗画面表示ON
                    connectionFailureLayout.setVisibility(View.VISIBLE);

                    //コールバック処理中断
                    return;
                }

                //通信画面表示OFF
                connectionLayout.setVisibility(View.GONE);

                //メインコンテンツ画面表示ON
                mainLayout.setVisibility(View.VISIBLE);

                //受信bitmapをビューにセット
                themeImageView.setImageBitmap(bitmap);

                //受信データ保持コールバック実行
                ((ConnectionCallBack)getParentFragment()).onSearchGetThemeSpotSubFragment2ConnectionInteraction(targetLocationName,description,hintLevel,searchLocationState,hintLatitude0,hintLatitude1,hintLatitude2,hintLongitude0,hintLongitude1,hintLongitude2,latitude,longitude,theme_id,bitmap);
            }
        });

        //HTTP通信実行
        httpBitmapDownloadAsync.execute("https://"+Constants.Http.SERVER_IP +"/open_data/photos/theme/"+theme_id+".jpg");
    }

    /* 受信データ保持コールバック */
    public interface ConnectionCallBack{
        void onSearchGetThemeSpotSubFragment2ConnectionInteraction(String targetLocationName,String description,int hintLevel,int searchLocationState,double lat0,double lat1,double lat2,double lng0,double lng1,double lng2,double latitude,double longitude,int theme_id,Bitmap themeImage);
    }

    /* フラグメント遷移コールバック */
    public interface EndCallBack{
        void onSearchGetThemeSpotSubFragment2EndInteraction();
    }
}
