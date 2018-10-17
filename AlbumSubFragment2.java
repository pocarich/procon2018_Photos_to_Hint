package jp.androidbook.photostohint;

/* アルバムモード写真リスト表示フラグメントクラス */

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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlbumSubFragment2 extends Fragment
{
    //遷移ID
    public static final int TRANSITION_ID_BACK = 0;
    public static final int TRANSITION_ID_CONTENT_SELECTED = 1;

    //Bundleキー
    private static final String USER_ID_KEY="USER_ID";
    private static final String REGION_NAME_KEY="REGION_NAME";

    //ユーザID
    private String userID;

    //地域名
    private String regionName;

    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //メインコンテンツ表示
    private LinearLayout mainLayout;

    //写真リスト表示
    private ViewGroup tableLayout;

    //探索ポイントテキスト
    private TextView searchPointText;

    /* コンストラクタ */
    public AlbumSubFragment2()
    {
        // 空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static AlbumSubFragment2 newInstance(String userID, String regionName)
    {
        //インスタンス生成
        AlbumSubFragment2 fragment = new AlbumSubFragment2();

        //バンドル生成
        Bundle args = new Bundle();

        //バンドルにユーザID追加
        args.putString(USER_ID_KEY, userID);

        //バンドルに地域名追加
        args.putString(REGION_NAME_KEY, regionName);

        //フラグメントインスタンスにバンドル設定
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
            userID = getArguments().getString(USER_ID_KEY);
            regionName = getArguments().getString(REGION_NAME_KEY);
        }
    }

    /* View生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_album_sub_fragment2, container, false);
    }

    /* View生成時処理 */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //通信画面表示取得
        connectionLayout = view.findViewById(R.id.album_sub_fragment2_connection_layout);

        //通信失敗画面表示取得
        connectionFailureLayout = view.findViewById(R.id.album_sub_fragment2_connection_failure_layout);

        //メインコンテンツ画面表示取得
        mainLayout = view.findViewById(R.id.album_sub_fragment2_main_layout);

        //写真リスト表示取得
        tableLayout = view.findViewById(R.id.album_sub_fragment2_table_layout);

        //探索ポイントテキスト取得
        searchPointText = view.findViewById(R.id.album_sub_fragment2_search_point_text);

        //通信再試行ボタン取得
        Button connectionRetryButton = view.findViewById(R.id.album_sub_fragment2_connection_failure_button);

        //戻るボタン取得
        ImageButton backButton = view.findViewById(R.id.album_sub_fragment2_backButton);

        //戻るボタン押下時処理設定
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //戻るコールバック実行
                ((CallBack) getParentFragment()).onAlbumSubFragment2Interaction(TRANSITION_ID_BACK, null, null, null);
            }
        });

        //通信再試行ボタン押下時処理設定
        connectionRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //HTTP通信開始
                updateInfo();
            }
        });

        //HTTP通信開始
        updateInfo();
    }

    /* HTTP通信 */
    private void updateInfo()
    {
        //通信画面表示ON
        connectionLayout.setVisibility(View.VISIBLE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //メインコンテンツ画面表示OFF
        mainLayout.setVisibility(View.GONE);

        //各種HTTP通信実行
        connection1();
        connection2();
    }

    /* HTTP通信（写真リスト取得） */
    private void connection1()
    {
        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONにユーザID追加
            json.put("user_id", userID);

            //JSONに地名追加
            json.put("region_name", regionName);

            //JSONに発見済み識別値追加
            json.put("found_state", Constants.Search.STATE_FOUND);

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了時コールバック設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String result)
                {

                    //通信結果が空のとき(通信失敗時)
                    if (result == "") {
                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示ON
                        connectionFailureLayout.setVisibility(View.VISIBLE);

                        //コールバック処理中断
                        return;
                    }
                    try {
                        //resultからJSON生成
                        JSONObject json = new JSONObject(result);

                        //"status"パラメータ取得
                        Boolean status = json.getBoolean("status");

                        //statusがtrueのとき（通信成功時）
                        if (status) {
                            //"result"パラメータ(探索済みお題スポット情報)取得
                            JSONArray dataArray = json.getJSONArray("result");

                            //各探索済みお題スポット情報について
                            for (int i = 0; i < dataArray.length(); i++) {
                                //i番目の要素取得
                                JSONObject data = dataArray.getJSONObject(i);

                                //お題スポットID取得
                                final int theme_id = data.getInt("theme_id");

                                //スポット名取得
                                final String location_name = data.getString("location_name");

                                //スポット説明取得
                                final String description = data.getString("description");

                                //行2枚で縦に写真リスト表示
                                if (i % 2 == 0) {
                                    //TableRow追加
                                    getLayoutInflater().inflate(R.layout.album_sub_fragment2_table_row, tableLayout);
                                }

                                //最下行取得
                                TableRow tableRow = (TableRow) tableLayout.getChildAt(i / 2);

                                //最新の写真ボタン取得
                                final ImageButton tempImageButton = (ImageButton) tableRow.getChildAt(i % 2);


                                /* HTTP通信（お題スポット写真取得） */

                                //HTTP通信インスタンス生成
                                HttpBitmapDownloadAsync httpBitmapDownloadAsync = new HttpBitmapDownloadAsync();

                                //HTTP通信終了後処理設定
                                httpBitmapDownloadAsync.setCallBack(new HttpBitmapDownloadAsync.CallBack() {
                                    @Override
                                    public void onAsyncTaskResult(Bitmap bitmap)
                                    {
                                        //通信結果がnullのとき（通信失敗時）
                                        if (bitmap == null) {
                                            //通信画面表示OFF
                                            connectionLayout.setVisibility(View.GONE);

                                            //通信失敗画面表示ON
                                            connectionFailureLayout.setVisibility(View.VISIBLE);

                                            //コールバック処理中断
                                            return;
                                        }

                                        final Bitmap tempBitmap = bitmap;

                                        //写真ボタンに取得した写真をセット
                                        tempImageButton.setImageBitmap(bitmap);

                                        //写真ボタン押下時処理設定
                                        tempImageButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                //写真選択コールバック実行
                                                ((CallBack) getParentFragment()).onAlbumSubFragment2Interaction(TRANSITION_ID_CONTENT_SELECTED, location_name, description, tempBitmap);
                                            }
                                        });
                                    }
                                });

                                //HTTP通信実行
                                httpBitmapDownloadAsync.execute("https://" + Constants.Http.SERVER_IP + "/open_data/photos/theme/" + theme_id + ".jpg");


                                //最終行で写真数が奇数のとき、余り(右側)の写真ボタンを空設定する
                                if (i + 1 == dataArray.length() && i % 2 == 0) {
                                    //最終行TableRow取得
                                    TableRow lastTableRow = (TableRow) tableLayout.getChildAt(i / 2);

                                    //最終行右側の写真ボタン取得
                                    ImageButton restTempImageButton = (ImageButton) lastTableRow.getChildAt(1);

                                    //画像にnullをセット
                                    restTempImageButton.setImageBitmap(null);

                                    //タッチできないようにする
                                    restTempImageButton.setClickable(false);
                                }
                            }

                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //通信失敗画面表示OFF
                            connectionFailureLayout.setVisibility(View.GONE);

                            //メインコンテンツ画面表示ON
                            mainLayout.setVisibility(View.VISIBLE);

                        }

                        //statusがfalseのとき（webサーバーエラー発生時）
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/open_data/get_discovered_theme_spot.php", json.toString());
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

    /* HTTP通信（写真リスト取得）2 */
    private void connection2() {
        try {
            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String result)
                {
                    //通信結果が空のとき(通信失敗時)
                    if (result == "") {
                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示ON
                        connectionFailureLayout.setVisibility(View.VISIBLE);

                        //コールバック処理中断
                        return;
                    }

                    try {
                        //resultからJSON生成
                        JSONObject json = new JSONObject(result);

                        //"status"パラメータ取得
                        Boolean status = json.getBoolean("status");

                        //statusがtrueのとき（通信成功時）
                        if (status) {
                            //"result"パラメータ取得
                            JSONArray dataArray = json.getJSONArray("result");

                            //"result"の最初の要素を取得
                            JSONObject data = dataArray.getJSONObject(0);

                            //探索ポイントを取得してテキストセット
                            searchPointText.setText("" + data.getInt("search_point"));
                        }

                        //statusがfalseのとき（通信失敗時）
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

            //JSON生成
            JSONObject json = new JSONObject();

            //JSONにユーザID追加
            json.put("user_id", userID);

            //JSONに地名追加
            json.put("region_name", regionName);

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

    /* フラグメント遷移コールバック */
    public interface CallBack
    {
        void onAlbumSubFragment2Interaction(int transitionID, String location_name, String description, Bitmap photo);
    }
}
