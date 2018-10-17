package jp.androidbook.photostohint;

/* 探索モードお題撮影フラグメント3 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;

public class SearchCameraSubFragment3 extends Fragment {

    //遷移ID
    public static final int TRANSITION_ID_BACK = 0;
    public static final int TRANSITION_ID_NEXT = 1;

    //Bundleキー
    private static final String TAKEN_IMAGE_KEY="TAKEN_IMAGE";
    private static final String HINT_POINT_KEY="HINT_POINT";
    private static final String SEARCH_POINT_KEY="SEARCH_POINT";
    private static final String USER_LATITUDE_KEY="USER_LATITUDE";
    private static final String USER_LONGIUTDE_KEY="USER_LONGITUDE";
    private static final String THEME_LATITUDE_KEY="THEME_LATITUDE";
    private static final String THEME_LONGITUDE_KEY="THEME_LONGITUDE";
    private static final String THEME_ID_KEY="THEME_ID";

    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //正解画面表示
    private ConstraintLayout trueLayout;

    //不正解画面表示
    private ConstraintLayout falseLayout;

    //撮影写真
    private Bitmap takenImage;

    //ヒントポイント
    private int hintPoint;

    //探索ポイント
    private int searchPoint;

    //ユーザ位置（緯度経度）
    private double userLatitude;
    private double userLongiude;

    //お題位置（緯度経度）
    private double themeLatitude;
    private double themeLongitude;

    //お題ID
    private int theme_id;

    /* コンストラクタ */
    public SearchCameraSubFragment3() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SearchCameraSubFragment3 newInstance(Bitmap takenImage,int hintPoint,int searchPoint,double userLatitude,double userLongiude,double themeLatitude,double themeLongitude,int theme_id) {
        //フラグメントインスタンス生成
        SearchCameraSubFragment3 fragment = new SearchCameraSubFragment3();

        //バンドル生成
        Bundle args = new Bundle();

        //バイト配列出力ストリーム生成
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        //撮影写真をJPEG形式でバイト配列出力ストリームに入力
        takenImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        //撮影写真のバイト配列をバンドルに追加
        args.putByteArray(TAKEN_IMAGE_KEY, stream.toByteArray());

        //バンドルにヒントポイント追加
        args.putInt(HINT_POINT_KEY,hintPoint);

        //バンドルに探索ポイント追加
        args.putInt(SEARCH_POINT_KEY,searchPoint);

        //バンドルにユーザ位置（緯度経度）追加
        args.putDouble(USER_LATITUDE_KEY,userLatitude);
        args.putDouble(USER_LONGIUTDE_KEY,userLongiude);

        //バンドルにお題位置（緯度経度）追加
        args.putDouble(THEME_LATITUDE_KEY,themeLatitude);
        args.putDouble(THEME_LONGITUDE_KEY,themeLongitude);

        //バンドルにお題ID追加
        args.putInt(THEME_ID_KEY,theme_id);

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
            //バイト配列からtakenImage生成
            byte[] takenImageBytes = getArguments().getByteArray(TAKEN_IMAGE_KEY);
            takenImage = BitmapFactory.decodeByteArray(takenImageBytes, 0, takenImageBytes.length);

            hintPoint=getArguments().getInt(HINT_POINT_KEY);
            searchPoint=getArguments().getInt(SEARCH_POINT_KEY);
            userLatitude=getArguments().getDouble(USER_LATITUDE_KEY);
            userLongiude=getArguments().getDouble(USER_LONGIUTDE_KEY);
            themeLatitude=getArguments().getDouble(THEME_LATITUDE_KEY);
            themeLongitude=getArguments().getDouble(THEME_LONGITUDE_KEY);
            theme_id=getArguments().getInt(THEME_ID_KEY);
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_search_camera_sub_fragment3, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //通信画面表示取得
        connectionLayout=view.findViewById(R.id.search_camera_sub_fragment3_download_layout);

        //通信失敗画面表示取得
        connectionFailureLayout=view.findViewById(R.id.search_camera_sub_fragment3_connection_failure_layout);

        //正解画面表示取得
        trueLayout=view.findViewById(R.id.search_camera_sub_fragment3_true_layout);

        //不正解画面表示取得
        falseLayout=view.findViewById(R.id.search_camera_sub_fragment3_false_layout);

        //ヒントポイントテキストビュー取得
        TextView hintPointText=view.findViewById(R.id.search_camera_sub_fragment3_hint_point_text);

        //探索ポイントテキストビュー取得
        TextView searchPointText=view.findViewById(R.id.search_camera_sub_fragment3_search_point_text);

        //正解確認ボタン取得
        Button trueConfirmationButton=view.findViewById(R.id.search_camera_sub_fragment3_true_confirmation_button);

        //不正解確認ボタン取得
        Button falseConfirmationButton=view.findViewById(R.id.search_camera_sub_fragment3_false_confirmation_button);

        //通信再試行ボタン取得
        Button retryConnectionButton=view.findViewById(R.id.search_camera_sub_fragment3_connection_failure_button);

        //ヒントポイントセット
        hintPointText.setText(""+(hintPoint+Constants.Search.GAIN_HINT_POINT)+"(+"+Constants.Search.GAIN_HINT_POINT+")");

        //探索ポイントセット
        searchPointText.setText(""+(searchPoint+Constants.Search.GAIN_SEARCH_POINT)+"(+"+Constants.Search.GAIN_SEARCH_POINT+")");

        switch(Constants.Common.DEBUG_MODE)
        {
            //DEBUG_MODEがオフなら
            case 0:
                //お題位置とユーザ位置の間の距離（緯度経度）
                double diff_latitude=userLatitude-themeLatitude;
                double diff_longitude=userLongiude-themeLongitude;

                //距離が閾値未満なら
                if(Math.sqrt(diff_latitude*diff_latitude+diff_longitude*diff_longitude)<Constants.Search.HINT_LEVEL3_LOCATION_RADIUS)
                {
                    //写真照合HTTP通信実行
                    connection();
                }

                //距離が閾値以上なら門前払い
                else
                {
                    //通信画面表示OFF
                    connectionLayout.setVisibility(View.GONE);

                    //通信失敗画面表示OFF
                    connectionFailureLayout.setVisibility(View.GONE);

                    //正解画面表示OFF
                    trueLayout.setVisibility(View.GONE);

                    //不正解画面表示ON
                    falseLayout.setVisibility(View.VISIBLE);
                }
                break;

            //DEBUG_MODEがオンなら
            case 1:
                //お題-ユーザ間の距離に関係なく写真照合HTTP通信実行
                connection();
                break;
        }

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

                //写真照合HTTP通信実行
                connection();
            }
        });

        //正解確認ボタン押下時処理設定
        trueConfirmationButton.setOnClickListener(new View.OnClickListener() {
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
                ((CallBack)getParentFragment()).onSearchCameraSubFragment3Interaction(TRANSITION_ID_NEXT);
            }
        });

        //不正解確認ボタン押下時処理設定
        falseConfirmationButton.setOnClickListener(new View.OnClickListener() {
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
                ((CallBack)getParentFragment()).onSearchCameraSubFragment3Interaction(TRANSITION_ID_BACK);
            }
        });
    }

    /* 写真照合HTTP通信 */
    private void connection()
    {
        //通信画面表示ON
        connectionLayout.setVisibility(View.VISIBLE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //正解画面表示OFF
        trueLayout.setVisibility(View.GONE);

        //不正解画面表示OFF
        falseLayout.setVisibility(View.GONE);

        try {
            //JSON生成
            JSONObject json=new JSONObject();

            //JSONにユーザ位置追加（緯度経度）
            json.put("latitude",userLatitude);
            json.put("longitude",userLongiude);

            //JSONにお題ID追加
            json.put("theme_id",theme_id);

            //JSONに撮影写真データ(base64)追加
            json.put("image",Function.encodeTobase64(takenImage));

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
                            JSONArray jsonArray=json.getJSONArray("result");

                            //照合結果取得
                            String result = jsonArray.get(0).toString();

                            //照合結果が"0"なら（不正解時）
                            if(result.equals("0"))
                            {
                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //正解画面表示OFF
                                trueLayout.setVisibility(View.GONE);

                                //不正解画面表示ON
                                falseLayout.setVisibility(View.VISIBLE);
                            }

                            //照合結果が"1"なら
                            else
                            {
                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //正解画面表示ON
                                trueLayout.setVisibility(View.VISIBLE);

                                //不正解画面表示OFF
                                falseLayout.setVisibility(View.GONE);
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/search/judge.php",json.toString());
        }

        //JSON例外発生時
        catch(JSONException e){
            //ログに例外メッセージ出力
            e.printStackTrace();

            //通信画面表示OFF
            connectionLayout.setVisibility(View.GONE);

            //通信失敗画面表示ON
            connectionFailureLayout.setVisibility(View.VISIBLE);
        }
    }

    /* フラグメント破棄時 */
    @Override
    public void onDetach() {
        super.onDetach();

        //bitmapクリア
        Function.cleanupBitmap(takenImage);
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onSearchCameraSubFragment3Interaction(int transitionID);
    }
}
