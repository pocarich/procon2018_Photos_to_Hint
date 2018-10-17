package jp.androidbook.photostohint;

/* 探索モードお題撮影フラグメント5 */

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
import org.json.JSONException;
import org.json.JSONObject;

public class SearchCameraSubFragment5 extends Fragment {

    //Bundleキー
    private static final String USER_ID_KEY="USER_ID";
    private static final String REGION_NAME_KEY="REGION_NAME";
    private static final String LOCATION_NAME_KEY="LOCATION_NAME";
    private static final String COMMENT_KEY="COMMENT";

    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //メインコンテンツ画面表示
    private ConstraintLayout mainLayout;

    //ユーザID
    private String userID;

    //地名
    private String regionName;

    //お題スポット名
    private String locationName;

    //コメント
    private String comment;

    /* コンストラクタ */
    public SearchCameraSubFragment5() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SearchCameraSubFragment5 newInstance(String userID,String regionName,String locationName,String comment) {
        //フラグメントインスタンス生成
        SearchCameraSubFragment5 fragment = new SearchCameraSubFragment5();

        //バンドル生成
        Bundle args = new Bundle();

        //バンドルにユーザID追加
        args.putString(USER_ID_KEY,userID);

        //バンドルに地名追加
        args.putString(REGION_NAME_KEY,regionName);

        //バンドルにお題スポット名追加
        args.putString(LOCATION_NAME_KEY,locationName);

        //バンドルにコメント追加
        args.putString(COMMENT_KEY,comment);

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
            locationName=getArguments().getString(LOCATION_NAME_KEY);
            comment=getArguments().getString(COMMENT_KEY);
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_search_camera_sub_fragment5, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //通信画面表示取得
        connectionLayout=view.findViewById(R.id.search_camera_sub_fragment5_connection_layout);

        //通信失敗画面表示取得
        connectionFailureLayout=view.findViewById(R.id.search_camera_sub_fragment5_connection_failure_layout);

        //通信再試行ボタン取得
        Button retryConnectionButton=view.findViewById(R.id.search_camera_sub_fragment5_connection_failure_button);

        //メインコンテンツ画面表示取得
        mainLayout=view.findViewById(R.id.search_camera_sub_fragment5_main_layout);

        //確認ボタン取得
        Button confirmationButton=view.findViewById(R.id.search_camera_sub_fragment5_confirmation_button);

        //正解後処理のためのHTTP通信実行
        connection();

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
                ((CallBack)getParentFragment()).onSearchCameraSubFragment5Interaction();
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

                //正解後処理のためのHTTP通信実行
                connection();
            }
        });
    }

    /* 正解後処理のためのHTTP通信 */
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
            JSONObject json=new JSONObject();

            //JSONにユーザID追加
            json.put("user_id",userID);

            //JSONに地名追加
            json.put("region_name",regionName);

            //JSONにお題スポット名追加
            json.put("target_location_name",locationName);

            //JSONに獲得ヒントポイント追加
            json.put("gain_hint_point",Constants.Search.GAIN_HINT_POINT);

            //JSONに獲得探索ポイント追加
            json.put("gain_search_point",Constants.Search.GAIN_SEARCH_POINT);

            //JSONに発見済み状態値追加
            json.put("found_state",Constants.Search.STATE_FOUND);

            //JSONにコメント追加
            json.put("comment",comment);

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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/search/found_location.php",json.toString());
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

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onSearchCameraSubFragment5Interaction();
    }
}
