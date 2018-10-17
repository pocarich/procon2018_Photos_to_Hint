package jp.androidbook.photostohint;

/* アルバムモードお題スポット詳細表示フラグメント */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;

public class AlbumSubFragment3 extends Fragment
{
    //Bundleキー
    private static final String USER_ID_KEY="USER_ID";
    private static final String REGION_NAME_KEY="REGION_NAME";
    private static final String LOCATION_NAME_KEY="LOCATION_NAME";
    private static final String DESCRIPTION_KEY="DESCRIPTION";
    private static final String PHOTO_KEY="PHOTO";

    //ユーザID
    private String userID;

    //地名
    private String regionName;

    //お題スポット名
    private String locationName;

    //お題スポット説明
    private String description;

    //お題スポットへのコメント
    private String loadedComment;

    //お題スポット写真
    private Bitmap photo;

    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //メインコンテンツ表示
    private FrameLayout mainLayout;

    //お題スポットコメントボックス
    private EditText commentEditText;

    //コメントが変更されているか
    private boolean commentChanged;

    /* コンストラクタ */
    public AlbumSubFragment3()
    {
        // 空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static AlbumSubFragment3 newInstance(String userID, String regionName, String locationName, String description, Bitmap photo)
    {
        //インスタンス生成
        AlbumSubFragment3 fragment = new AlbumSubFragment3();

        //バンドル生成
        Bundle args = new Bundle();

        //バイト配列出力ストリーム
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        //お題スポット写真をJPEG形式でバイト配列出力ストリームに入力
        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        //お題スポット写真のバイト配列をバンドルに追加
        args.putByteArray(PHOTO_KEY, stream.toByteArray());

        //バンドルにユーザID追加
        args.putString(USER_ID_KEY, userID);

        //バンドルに地名追加
        args.putString(REGION_NAME_KEY, regionName);

        //バンドルにお題スポット名追加
        args.putString(LOCATION_NAME_KEY, locationName);

        //バンドルにお題スポット説明追加
        args.putString(DESCRIPTION_KEY, description);

        //フラグメントインスタンスにバンドル追加
        fragment.setArguments(args);

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* フラグメント生成時処理 */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //newInstanceで設定したパラメータを取得
        if (getArguments() != null) {
            //バイト配列からbitmap作成
            byte[] takenImagebytes = getArguments().getByteArray(PHOTO_KEY);
            photo = BitmapFactory.decodeByteArray(takenImagebytes, 0, takenImagebytes.length);

            userID = getArguments().getString(USER_ID_KEY);
            regionName = getArguments().getString(REGION_NAME_KEY);
            locationName = getArguments().getString(LOCATION_NAME_KEY);
            description = getArguments().getString(DESCRIPTION_KEY);
        }
    }

    /* View生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_album_sub_fragment3, container, false);
    }

    /* View生成時処理 */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //通信画面表示取得
        connectionLayout = view.findViewById(R.id.album_sub_fragment3_connection_layout);

        //通信失敗画面表示取得
        connectionFailureLayout = view.findViewById(R.id.album_sub_fragment3_connection_failure_layout);

        //メインコンテンツ画面表示取得
        mainLayout = view.findViewById(R.id.album_sub_fragment3_main_layout);

        //お題スポット名表示用テキストビュー取得
        TextView locationNameText = view.findViewById(R.id.album_sub_fragment3_title_text);

        //お題スポット写真表示用イメージビュー取得
        ImageView spotPhotoView = view.findViewById(R.id.album_sub_fragment3_takenImage);

        //お題スポット説明表示用テキストビュー取得
        TextView descriptionText = view.findViewById(R.id.album_sub_fragment3_tip_text);

        //コメントテキストボックス取得
        commentEditText = view.findViewById(R.id.album_sub_fragment3_message_editText);

        //戻るボタン取得
        ImageButton backButton = view.findViewById(R.id.album_sub_fragment3_backButton);

        //通信再試行ボタン取得
        Button retryButton = view.findViewById(R.id.album_sub_fragment3_connection_failure_button);

        //お題スポット名セット
        locationNameText.setText(locationName);

        //お題スポット説明文セット
        descriptionText.setText(description);

        //お題スポット写真セット
        spotPhotoView.setImageBitmap(photo);


        /* お題スポット写真表示用イメージビューのサイズ設定 */

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

        //お題スポット写真表示用イメージビューにサイズ設定を適用
        spotPhotoView.setLayoutParams(layoutParams);


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

                //コメントに変更がない場合
                if (commentEditText.getText().toString().equals(loadedComment)) {
                    //戻るコールバック実行
                    ((CallBack) getParentFragment()).onAlbumSubFragment3Interaction();
                }

                //コメントに変更があった場合
                else {
                    //コメント変更ON
                    commentChanged = true;

                    //コメント変更反映のためのHTTP通信実行
                    updateInfo();
                }
            }
        });

        //通信再試行ボタン押下時処理設定
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //コメント変更保存時の再試行なら
                if (commentChanged) {
                    //コメント変更反映のためのHTTP通信実行
                    updateInfo();
                }

                //データ取得時の再試行なら
                else {
                    //データ取得のためのHTTP通信実行
                    loadInfo();
                }
            }
        });

        //データ取得のためのHTTP通信実行
        loadInfo();
    }

    /* データ取得のためのHTTP通信 */
    private void loadInfo() {

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

            //JSONにお題スポット名追加
            json.put("location_name", locationName);

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String result) {
                    //通信結果が空なら（通信失敗時）
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

                            //"result"パラメータが空なら（webサーバーエラー発生時）
                            if (dataArray.length() == 0) {
                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //通信失敗画面表示ON
                                connectionFailureLayout.setVisibility(View.VISIBLE);
                            }

                            //"result"パラメータにデータがあれば
                            else {
                                //"comment"パラメータ取得
                                loadedComment = dataArray.getJSONObject(0).getString("comment");

                                //コメントテキストボックスに取得したコメントをセット
                                commentEditText.setText(loadedComment);

                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //通信失敗画面表示OFF
                                connectionFailureLayout.setVisibility(View.GONE);

                                //メインコンテンツ画面表示ON
                                mainLayout.setVisibility(View.VISIBLE);
                            }
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/get_album_comment.php", json.toString());
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

    /* コメント変更反映のためのHTTP通信 */
    private void updateInfo() {
        //通信画面表示ON
        connectionLayout.setVisibility(View.VISIBLE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //メインコンテンツ表示OFF
        mainLayout.setVisibility(View.GONE);

        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONにユーザID追加
            json.put("user_id", userID);

            //JSONに地名追加
            json.put("region_name", regionName);

            //JSONにお題スポット名追加
            json.put("location_name", locationName);

            //JSONにコメント追加
            json.put("comment", commentEditText.getText().toString());

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String result) {
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

                        //"status"パラメータがtrueのとき（通信成功時）
                        if (status) {
                            //戻るコールバック実行
                            ((CallBack) getParentFragment()).onAlbumSubFragment3Interaction();
                        }

                        //"status"パラメータがfalseのとき（webサーバエラー発生時）
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/update_album_comment.php", json.toString());
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

    /* フラグメント破棄時 */
    @Override
    public void onDetach() {
        super.onDetach();

        //photo（bitmap）のリソース解放
        Function.cleanupBitmap(photo);
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onAlbumSubFragment3Interaction();
    }
}
