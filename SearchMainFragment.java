package jp.androidbook.photostohint;

/* 探索モード管理フラグメント */

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Random;

public class SearchMainFragment extends Fragment implements SearchGetThemeSpotSubFragment.CallBack ,SearchGetThemeSpotSubFragment2.ConnectionCallBack,SearchGetThemeSpotSubFragment2.EndCallBack,SearchMapSubFragment.CallBack,SearchHintSubFragment.BackCallBack,SearchHintSubFragment.LevelChangeCallBack,SearchCameraSubFragment.CallBack,SearchCameraSubFragment2.CallBack,SearchCameraSubFragment3.CallBack,SearchCameraSubFragment4.CallBack,SearchCameraSubFragment5.CallBack{

    //Bundleキー
    private static final String USER_ID_KEY="USER_ID";
    private static final String REGION_NAME_KEY="REGION_NAME";

    //ユーザID
    private String userID;

    //地名
    private String regionName;

    //お題スポット名
    private String targetLocationName;

    //お題スポット説明
    private String description;

    //お題スポット位置（緯度経度）
    private double themeLatitude;
    private double themeLongitude;

    //レベル毎ヒント円中心位置（緯度経度）
    private double hintLatitude0;
    private double hintLatitude1;
    private double hintLatitude2;
    private double hintLongitude0;
    private double hintLongitude1;
    private double hintLongitude2;

    //ヒントレベル
    private int hintLevel;

    //ヒントポイント
    private int hintPoint;

    //探索ポイント
    private int searchPoint;

    //お題スポット探索状態値
    private int searchLocationState;

    //お題スポット写真
    private Bitmap themeImage;

    //撮影写真
    private Bitmap takenImage;

    //お題スポットID
    private int themeID;

    //残りお題スポット数
    private int restThemeSpotNum;

    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //フラグメント遷移コールバック
    private CallBack callBack;

    //ユーザ位置（緯度経度）
    private LatLng userLocation;

    /* コンストラクタ */
    public SearchMainFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SearchMainFragment newInstance(String userID,String regionName) {
        //フラグメントインスタンス生成
        SearchMainFragment fragment = new SearchMainFragment();

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
        return inflater.inflate(R.layout.fragment_search_main, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //通信画面表示取得
        connectionLayout=view.findViewById(R.id.search_main_fragment_fragment_download_layout);

        //通信失敗画面表示取得
        connectionFailureLayout=view.findViewById(R.id.search_main_fragment_connection_failure_layout);

        //通信再試行ボタン取得
        final Button retryConnectionButton=view.findViewById(R.id.search_main_fragment_connection_failure_button);

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

        try {
            //JSON生成
            JSONObject json2 = new JSONObject();

            //JSONに各パラメータ追加
            json2.put("user_id", userID);
            json2.put("region_name", regionName);
            json2.put("found_state",Constants.Search.STATE_FOUND);

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

                        //"status"パラメータがtrueのとき（通信成功時）
                        if (status) {
                            //"result"パラメータ取得
                            JSONArray dataArray = json.getJSONArray("result");

                            //"result"パラメータデータ数（未発見お題数）保持
                            restThemeSpotNum=dataArray.length();

                            //"result"パラメータデータ数が0（全お題発見済み）なら
                            if (dataArray.length() == 0) {
                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //通信失敗画面表示OFF
                                connectionFailureLayout.setVisibility(View.GONE);


                                /* SearchGetThemeSpotSubFragment遷移 */

                                //トランザクション生成
                                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                                //遷移時アニメーション設定
                                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                                //SearchGetThemeSpotSubFragmentインスタンス生成
                                SearchGetThemeSpotSubFragment searchGetThemeSpotSubFragment = SearchGetThemeSpotSubFragment.newInstance(true);

                                //FrameLayoutに組み込む
                                fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout, searchGetThemeSpotSubFragment);

                                //遷移実行
                                fragmentTransaction.commit();
                            }

                            //"result"パラメータデータが存在する（未発見お題スポットが存在する）なら
                            else {
                                //お題取得HTTP通信2実行
                                connection2();
                            }
                        }

                        //"status"パラメータがfalseのとき（webサーバーエラー発生時）
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/get_undiscovered_location_data.php", json2.toString());
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
    private void connection2() {
        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONに各パラメータ追加
            json.put("user_id", userID);
            json.put("region_name", regionName);

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
                            JSONArray resultArray = json.getJSONArray("result");

                            //"result"パラメータから最初の要素取得
                            JSONObject result = resultArray.getJSONObject(0);

                            //各種パラメータ取得
                            targetLocationName = result.getString("target_location_name");
                            hintPoint = result.getInt("hint_point");
                            searchPoint = result.getInt("search_point");
                            hintLevel = result.getInt("hint_level");

                            //お題が設定されていない場合
                            if (result.isNull("target_location_name")) {
                                targetLocationName = null;

                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //通信失敗画面表示OFF
                                connectionFailureLayout.setVisibility(View.GONE);


                                /* SearchGetThemeSpotSubFragmentに遷移 */

                                //トランザクション生成
                                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                                //遷移時アニメーション設定
                                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                                //SearchGetThemeSpotSubFragmentインスタンス生成
                                SearchGetThemeSpotSubFragment searchGetThemeSpotSubFragment = SearchGetThemeSpotSubFragment.newInstance(false);

                                //FrameLayoutに組み込む
                                fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout, searchGetThemeSpotSubFragment);

                                //遷移実行
                                fragmentTransaction.commit();
                            }

                            //お題が設定されている場合
                            else {
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/get_search_region_data.php", json.toString());
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
            //JSON例外発生時
            JSONObject json = new JSONObject();

            //JSONに各種パラメータ追加
            json.put("user_id", userID);
            json.put("region_name", regionName);
            json.put("location_name", targetLocationName);

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

                            //"result"パラメータが空なら（webサーバーエラー発生時）
                            if (dataArray.length() == 0) {
                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //通信失敗画面表示ON
                                connectionFailureLayout.setVisibility(View.VISIBLE);
                            }

                            //"result"パラメータが空でないなら
                            else {
                                //最初の要素を取得
                                JSONObject nowLocationJSON = dataArray.getJSONObject(0);

                                //各種パラメータ取得
                                searchLocationState = nowLocationJSON.getInt("state");
                                hintLatitude0 = nowLocationJSON.getDouble("latitude0");
                                hintLatitude1 = nowLocationJSON.getDouble("latitude1");
                                hintLatitude2 = nowLocationJSON.getDouble("latitude2");
                                hintLongitude0 = nowLocationJSON.getDouble("longitude0");
                                hintLongitude1 = nowLocationJSON.getDouble("longitude1");
                                hintLongitude2 = nowLocationJSON.getDouble("longitude2");

                                //お題取得HTTP通信4実行
                                connection4();
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/get_specific_search_location_data.php", json.toString());
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
            JSONObject json = new JSONObject();

            //JSONに各種パラメータ追加
            json.put("user_id", userID);
            json.put("region_name", regionName);
            json.put("location_name", targetLocationName);

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

                            //"result"パラメータが空なら（webサーバーエラー発生時）
                            if (dataArray.length() == 0) {
                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //通信失敗画面表示ON
                                connectionFailureLayout.setVisibility(View.VISIBLE);
                            }

                            //"result"パラメータが空でないなら
                            else {
                                //各種パラメータ取得
                                themeLatitude = dataArray.getJSONObject(0).getDouble("latitude");
                                themeLongitude = dataArray.getJSONObject(0).getDouble("longitude");
                                description = dataArray.getJSONObject(0).getString("description");
                                themeID = dataArray.getJSONObject(0).getInt("theme_id");

                                //お題探索状態値がヒントレベル未満なら
                                if (searchLocationState < hintLevel) {
                                    /* hintLevelレベルでのヒント円中心計算 */

                                    //乱数生成インスタンス生成
                                    Random rand=new Random();

                                    //0°～360°の範囲でランダムな角度取得
                                    double randAngle=rand.nextDouble()*2*Math.PI;

                                    //方向決定
                                    double diffLat=Math.cos(randAngle);
                                    double diffLng=Math.sin(randAngle);

                                    //0～1の間のランダムな実数取得
                                    double randLength=rand.nextDouble();

                                    //ヒントレベルに応じて距離設定
                                    switch(hintLevel)
                                    {
                                        case 1:
                                            //0～Lv1ヒント円最大半径の範囲でランダムな距離設定
                                            diffLat*=randLength*Constants.Search.HINT_LEVEL1_LOCATION_RADIUS;
                                            diffLng*=randLength*Constants.Search.HINT_LEVEL1_LOCATION_RADIUS;

                                            //ヒント円中心決定
                                            hintLatitude0 = themeLatitude + diffLat;
                                            hintLongitude0 = themeLongitude + diffLng;
                                            break;
                                        case 2:
                                            //0～Lv2ヒント円最大半径の範囲でランダムな距離設定
                                            diffLat*=randLength*Constants.Search.HINT_LEVEL2_LOCATION_RADIUS;
                                            diffLng*=randLength*Constants.Search.HINT_LEVEL2_LOCATION_RADIUS;

                                            //ヒント円中心決定
                                            hintLatitude1 = themeLatitude + diffLat;
                                            hintLongitude1 = themeLongitude + diffLng;
                                            break;
                                        case 3:
                                            //0～Lv3ヒント円最大半径の範囲でランダムな距離設定
                                            diffLat*=randLength*Constants.Search.HINT_LEVEL3_LOCATION_RADIUS;
                                            diffLng*=randLength*Constants.Search.HINT_LEVEL3_LOCATION_RADIUS;

                                            //ヒント円中心決定
                                            hintLatitude2 = themeLatitude + diffLat;
                                            hintLongitude2 = themeLongitude + diffLng;
                                            break;
                                    }
                                }

                                //お題取得HTTP通信5実行
                                connection5();
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/get_specific_theme_spot_data.php", json.toString());
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
        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //お題探索状態値がヒントレベル以下なら
            if (searchLocationState < hintLevel) {
                //お題探索状態値更新
                json.put("state", hintLevel);
                searchLocationState = hintLevel;
            }

            //JSONに各種パラメータ追加
            json.put("user_id", userID);
            json.put("region_name", regionName);
            json.put("location_name", targetLocationName);
            json.put("latitude0", hintLatitude0);
            json.put("longitude0", hintLongitude0);
            json.put("latitude1", hintLatitude1);
            json.put("longitude1", hintLongitude1);
            json.put("latitude2", hintLatitude2);
            json.put("longitude2", hintLongitude2);

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
                            //HTTP通信インスタンス生成
                            HttpBitmapDownloadAsync httpBitmapDownloadAsync = new HttpBitmapDownloadAsync();

                            //通信終了後処理設定
                            httpBitmapDownloadAsync.setCallBack(new HttpBitmapDownloadAsync.CallBack() {
                                @Override
                                public void onAsyncTaskResult(Bitmap bitmap) {
                                    //受信データが空なら（通信失敗時）
                                    if(bitmap==null)
                                    {
                                        //通信画面表示OFF
                                        connectionLayout.setVisibility(View.GONE);

                                        //通信失敗画面表示ON
                                        connectionFailureLayout.setVisibility(View.VISIBLE);

                                        //コールバック処理中断
                                        return;
                                    }

                                    //bitmap保持
                                    themeImage = bitmap;

                                    //通信画面表示OFF
                                    connectionLayout.setVisibility(View.GONE);

                                    //通信失敗画面表示OFF
                                    connectionFailureLayout.setVisibility(View.GONE);


                                    /* SearchMapSubFragmentに遷移 */

                                    //トランザクション生成
                                    FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                                    //フラグメント遷移アニメーション設定
                                    fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                                    //SearchMapSubFragmentインスタンス生成
                                    SearchMapSubFragment searchMapSubFragment = SearchMapSubFragment.newInstance(userID, regionName, themeImage, hintLevel, hintLatitude0, hintLongitude0, hintLatitude1, hintLongitude1, hintLatitude2, hintLongitude2, restThemeSpotNum);

                                    //バックスタックに追加
                                    fragmentTransaction.addToBackStack(null);

                                    //FrameLayoutに組み込む
                                    fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout, searchMapSubFragment);

                                    //遷移実行
                                    fragmentTransaction.commit();
                                }
                            });

                            //HTTP通信実行
                            httpBitmapDownloadAsync.execute("https://" + Constants.Http.SERVER_IP + "/open_data/photos/theme/" + themeID + ".jpg");
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/update_search_location_info.php", json.toString());
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

    /* フラグメント生成時処理 */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //アクティヴィティがコールバックを実装している場合
        if (context instanceof CallBack) {
            //コールバック設定
            callBack = (CallBack) context;
        }

        //アクティヴィティがコールバックを実装していない場合
        else {
            //実行時例外を投げる
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    /* フラグメント破棄時処理 */
    @Override
    public void onDetach() {
        super.onDetach();

        //コールバックを外す
        callBack = null;
    }

    /* SearchGetThemeSpotSubFragmentコールバック */
    @Override
    public void onSearchGetThemeSpotSubFragmentInteraction()
    {
        /* SearchGetThemeSpotSubFragment2に遷移 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction =getChildFragmentManager().beginTransaction();

        //SearchGetThemeSpotSubFragment2生成
        SearchGetThemeSpotSubFragment2 searchGetThemeSpotSubFragment2=SearchGetThemeSpotSubFragment2.newInstance(userID,regionName,null);

        //フラグメント遷移アニメーション設定
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);

        //FrameLayoutに組み込む
        fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout,searchGetThemeSpotSubFragment2);

        //遷移実行
        fragmentTransaction.commit();
    }

    /* SearchGetThemeSpotSubFragment2データ更新コールバック */
    @Override
    public void onSearchGetThemeSpotSubFragment2ConnectionInteraction(String targetLocationName,String description,int hintLevel,int searchLocationState,double hintLatitude0,double hintLatitude1,double hintLatitude2,double hintLongitude0,double hintLongitude1,double hintLongitude2,double themeLatitude,double themeLongitude,int themeID,Bitmap themeImage)
    {
        //各種パラメータ保持
        this.targetLocationName=targetLocationName;
        this.description=description;
        this.hintLevel=hintLevel;
        this.searchLocationState=searchLocationState;
        this.hintLatitude0=hintLatitude0;
        this.hintLatitude1=hintLatitude1;
        this.hintLatitude2=hintLatitude2;
        this.hintLongitude0=hintLongitude0;
        this.hintLongitude1=hintLongitude1;
        this.hintLongitude2=hintLongitude2;
        this.themeLatitude=themeLatitude;
        this.themeLongitude=themeLongitude;
        this.themeImage=themeImage;
        this.themeID=themeID;
    }

    /* SearchGetThemeSpotSubFragment2フラグメント遷移コールバック */
    @Override
    public void onSearchGetThemeSpotSubFragment2EndInteraction()
    {
        /* SearchMapSubFragmentに遷移 */

        //SearchMapSubFragmentインスタンス生成
        SearchMapSubFragment searchMapSubFragment=SearchMapSubFragment.newInstance(userID,regionName,themeImage,hintLevel,hintLatitude0,hintLongitude0,hintLatitude1,hintLongitude1,hintLatitude2,hintLongitude2,restThemeSpotNum);

        //トランザクション生成
        FragmentTransaction fragmentTransaction =getChildFragmentManager().beginTransaction();

        //フラグメント遷移アニメーション設定
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);

        //バックスタックに追加
        fragmentTransaction.addToBackStack(null);

        //FrameLayoutに組み込む
        fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout,searchMapSubFragment);

        //遷移実行
        fragmentTransaction.commit();
    }

    /* SearchMapSubFragmentコールバック */
    @Override
    public void onSearchMapSubFragmentInteraction(int transitionID, LatLng userLocation) {
        //ユーザ位置保持
        this.userLocation = userLocation;

        //遷移IDに応じてフラグメント遷移実行
        switch (transitionID) {
            case SearchMapSubFragment.TRANSITION_ID_HINT: {
                /* SearchHintSubFragmentに遷移 */

                //SearchHintSubFragmentインスタンス生成
                SearchHintSubFragment searchHintSubFragment = SearchHintSubFragment.newInstance(userID, regionName, targetLocationName, hintLevel, hintLatitude0, hintLongitude0, hintLatitude1, hintLongitude1, hintLatitude2, hintLongitude2, hintPoint, searchLocationState, themeLatitude, themeLongitude);

                //トランザクション生成
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                //フラグメント遷移アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout, searchHintSubFragment);

                //遷移実行
                fragmentTransaction.commit();
            }
            break;
            case SearchMapSubFragment.TRANSITION_ID_CAMERA: {
                /* SearchCameraSubFragmentに遷移 */

                //SearchCameraSubFragmentインスタンス生成
                SearchCameraSubFragment searchCameraSubFragment = SearchCameraSubFragment.newInstance();

                //トランザクション生成
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                //フラグメント遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout, searchCameraSubFragment);

                //遷移実行
                fragmentTransaction.commit();
            }
            break;
            case SearchMapSubFragment.TRANSITION_ID_THEME_CHANGE: {
                /* SearchGetThemeSpotSubFragment2に遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction =getChildFragmentManager().beginTransaction();

                //SearchGetThemeSpotSubFragment2インスタンス生成
                SearchGetThemeSpotSubFragment2 searchGetThemeSpotSubFragment2=SearchGetThemeSpotSubFragment2.newInstance(userID,regionName,targetLocationName);

                //フラグメント遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout,searchGetThemeSpotSubFragment2);

                //遷移実行
                fragmentTransaction.commit();
            }
            break;
        }
    }

    /* SearchHintSubFragmentフラグメント遷移コールバック */
    @Override
    public void onSearchHintSubFragmentBackInteraction()
    {
        /* SearchMapSubFragmentに遷移 */

        //SearchMapSubFragmentインスタンス生成
        SearchMapSubFragment searchMapSubFragment=SearchMapSubFragment.newInstance(userID,regionName,themeImage,hintLevel,hintLatitude0,hintLongitude0,hintLatitude1,hintLongitude1,hintLatitude2,hintLongitude2,restThemeSpotNum);

        //トランザクション生成
        FragmentTransaction fragmentTransaction =getChildFragmentManager().beginTransaction();

        //フラグメント遷移アニメーション設定
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);

        //バックスタックに追加
        fragmentTransaction.addToBackStack(null);

        //FrameLayoutに組み込む
        fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout,searchMapSubFragment);

        //遷移実行
        fragmentTransaction.commit();
    }

    /* SearchHintSubFragmentレベル変更コールバック */
    @Override
    public void onSearchHintSubFragmentLevelChangeInteraction(int hintLevel,int hintPoint,int searchLocationState,double[] hintLatitudeList,double[] hintLongitudeList)
    {
        //各種パラメータ保持
        this.hintLevel=hintLevel;
        this.hintPoint=hintPoint;
        this.searchLocationState=searchLocationState;
        hintLatitude0=hintLatitudeList[0];
        hintLatitude1=hintLatitudeList[1];
        hintLatitude2=hintLatitudeList[2];
        hintLongitude0=hintLongitudeList[0];
        hintLongitude1=hintLongitudeList[1];
        hintLongitude2=hintLongitudeList[2];
    }

    /* SearchCameraSubFragmentコールバック */
    @Override
    public void onSearchCameraSubFragmentInteraction(int transitionID,Bitmap takenImage)
    {
        //遷移IDに応じてフラグメント遷移
        switch(transitionID) {
            case SearchCameraSubFragment.TRANSITION_ID_BACK:
                //前のフラグメントに戻る
                getChildFragmentManager().popBackStack();
                break;
            case SearchCameraSubFragment.TRANSITION_ID_NEXT:
                /* SearchCameraSubFragment2に遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction =getChildFragmentManager().beginTransaction();

                //SearchCameraSubFragment2インスタンス生成
                SearchCameraSubFragment2 searchCameraSubFragment2=SearchCameraSubFragment2.newInstance(takenImage,themeImage);

                //フラグメント遷移アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout,searchCameraSubFragment2);

                //遷移実行
                fragmentTransaction.commit();
                break;
        }
    }

    /* SearchCameraSubFragment2コールバック */
    @Override
    public void onSearchCameraSubFragment2Interaction(int transitionID,Bitmap takenImage)
    {
        //遷移IDに応じてフラグメント遷移
        switch(transitionID) {
            case SearchCameraSubFragment2.TRANSITION_ID_BACK:
                //前のフラグメントに戻る
                getChildFragmentManager().popBackStack();
                break;
            case SearchCameraSubFragment2.TRANSITION_ID_NEXT:
                //写真保持
                this.takenImage=takenImage;

                /* SearchCameraSubFragment3に遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction =getChildFragmentManager().beginTransaction();

                //SearchCameraSubFragment3インスタンス生成
                SearchCameraSubFragment3 searchCameraSubFragment3=SearchCameraSubFragment3.newInstance(takenImage,hintPoint,searchPoint,userLocation.latitude,userLocation.longitude,themeLatitude,themeLongitude,themeID);

                //フラグメント遷移アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout,searchCameraSubFragment3);

                //遷移実行
                fragmentTransaction.commit();
                break;
        }
    }

    /* SearchCameraSubFragment3コールバック */
    @Override
    public void onSearchCameraSubFragment3Interaction(int transitionID)
    {
        //遷移IDに応じてフラグメント遷移
        switch(transitionID)
        {
            case SearchCameraSubFragment3.TRANSITION_ID_BACK:{
                //前のフラグメントに戻る
                getChildFragmentManager().popBackStack();
            }
                break;
            case SearchCameraSubFragment3.TRANSITION_ID_NEXT:{
                /* SearchCameraSubFragment4に遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction =getChildFragmentManager().beginTransaction();

                //SearchCameraSubFragment4インスタンス生成
                SearchCameraSubFragment4 searchCameraSubFragment4=SearchCameraSubFragment4.newInstance(targetLocationName,description,themeImage);

                //フラグメント遷移アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout,searchCameraSubFragment4);

                //遷移実行
                fragmentTransaction.commit();
            }
                break;
        }
    }

    /* SearchCameraSubFragment4コールバック */
    @Override
    public void onSearchCameraSubFragment4Interaction(String message)
    {
        /* SearchCameraSubFragment5に遷移 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction =getChildFragmentManager().beginTransaction();

        //SearchCameraSubFragment5インスタンス生成
        SearchCameraSubFragment5 searchCameraSubFragment5=SearchCameraSubFragment5.newInstance(userID,regionName,targetLocationName,message);

        //フラグメント遷移アニメーション設定
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);

        //FrameLayoutに組み込む
        fragmentTransaction.replace(R.id.search_main_fragment_fragment_layout,searchCameraSubFragment5);

        //遷移実行
        fragmentTransaction.commit();
    }

    /* SearchCameraSubFragment5コールバック */
    @Override
    public void onSearchCameraSubFragment5Interaction()
    {
        //フラグメント遷移コールバック実行
        callBack.onSearchMainFragmentInteraction();
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onSearchMainFragmentInteraction();
    }
}
