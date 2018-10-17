package jp.androidbook.photostohint;

/* アルバムモード地域選択フラグメントクラス */

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlbumSubFragment extends Fragment
{
    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //地域選択画面表示
    private FrameLayout mainLayout;

    //地域名リスト表示
    private LinearLayout regionNameListLayout;

    /* コンストラクタ */
    public AlbumSubFragment()
    {
        // 空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static AlbumSubFragment newInstance()
    {
        //インスタンス生成
        AlbumSubFragment fragment = new AlbumSubFragment();

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* View生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_album_sub, container, false);
    }

    /* View生成時処理 */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //通信画面表示取得
        connectionLayout = view.findViewById(R.id.album_sub_fragment_connection_layout);

        //通信失敗画面表示取得
        connectionFailureLayout = view.findViewById(R.id.album_sub_fragment_connection_failure_layout);

        //地域選択画面表示取得
        mainLayout = view.findViewById(R.id.album_sub_fragment_main_layout);

        //地域名リスト表示取得
        regionNameListLayout = view.findViewById(R.id.album_sub_fragment_region_name_list_layout);

        //通信再試行ボタン取得
        Button connectionRetryButton = view.findViewById(R.id.album_sub_fragment_connection_failure_button);

        //通信再試行ボタンにボタン押下時の処理設定
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

                //HTTP通信再試行
                updateInfo();
            }
        });

        //HTTP通信開始
        updateInfo();
    }

    /* HTTP通信(地域名取得) */
    private void updateInfo()
    {
        //通信画面表示ON
        connectionLayout.setVisibility(View.VISIBLE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //地域選択画面表示OFF
        mainLayout.setVisibility(View.GONE);

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
                        //"result"パラメータ(地域情報)取得
                        JSONArray dataArray = json.getJSONArray("result");

                        //各地域情報について
                        for (int i = 0; i < dataArray.length(); i++) {
                            //i番目の要素取得
                            JSONObject data = dataArray.getJSONObject(i);

                            //i番目の要素から"region_name"パラメータ（地域名）取得
                            final String title = data.getString("region_name");


                            /* 地域選択ボタン生成 */

                            //LinearLayoutパラメータ生成
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                            //layout_margin設定(left 5dp,top 5dp,right 5dp,bottom 0dp)
                            layoutParams.setMargins((int) getResources().getDimension(R.dimen.dp5), (int) getResources().getDimension(R.dimen.dp5), (int) getResources().getDimension(R.dimen.dp5), 0);

                            //layout_height 50dp
                            layoutParams.height = (int) getResources().getDimension(R.dimen.dp50);

                            //ボタン生成
                            Button newButton = new Button(getActivity());

                            //ボタンに上記LinearLayoutパラメータ設定
                            newButton.setLayoutParams(layoutParams);

                            //padding設定(left 15dp,top 0dp,right 15dp,bottom 0dp)
                            newButton.setPadding((int) getResources().getDimension(R.dimen.dp15), 0, (int) getResources().getDimension(R.dimen.dp15), 0);

                            //背景画像設定
                            newButton.setBackgroundResource(R.drawable.ui_message_box0);

                            //表示可能領域オーバー時、オーバーした分を省略
                            newButton.setEllipsize(TextUtils.TruncateAt.END);

                            //横スクロールON
                            newButton.setHorizontallyScrolling(true);

                            //1行表示ON
                            newButton.setSingleLine(true);

                            //左詰め、垂直方向中央揃え
                            newButton.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

                            //テキストサイズ18sp
                            newButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.sp18));

                            //ボタンテキストに地域名設定
                            newButton.setText(title);

                            //ボタン押下時処理設定
                            newButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v)
                                {
                                    //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                                    ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.9f, 1f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    scaleAnimation.setDuration(50);
                                    scaleAnimation.setRepeatCount(1);
                                    scaleAnimation.setRepeatMode(Animation.REVERSE);

                                    //UIサイズアニメーション開始
                                    v.startAnimation(scaleAnimation);

                                    //地域選択時コールバック実行
                                    ((CallBack) getParentFragment()).onAlbumSubFragmentInteraction(title);
                                }
                            });

                            //生成したボタンを地域名リストに追加
                            regionNameListLayout.addView(newButton);
                        }

                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示OFF
                        connectionFailureLayout.setVisibility(View.GONE);

                        //地域選択画面表示ON
                        mainLayout.setVisibility(View.VISIBLE);
                    }

                    //通信エラー発生時
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
        httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/open_data/get_theme_region_name.php");
    }

    /* フラグメント遷移コールバック */
    public interface CallBack
    {
        void onAlbumSubFragmentInteraction(String regionName);
    }
}
