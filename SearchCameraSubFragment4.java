package jp.androidbook.photostohint;

/* 探索モードお題撮影フラグメント4 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;

public class SearchCameraSubFragment4 extends Fragment {


    //Bundleキー
    private static final String THEME_IMAGE_KEY="THEME_IMAGE";
    private static final String LOCATION_NAME_KEY="LOCATION_NAME";
    private static final String DESCRIPTION_KEY="DESCRIPTION";

    //お題スポット名
    private String locationName;

    //お題スポット説明
    private String description;

    //お題写真
    private Bitmap themeImage;

    //お題写真ビュー
    private ImageView themeImageView;

    /* コンストラクタ */
    public SearchCameraSubFragment4() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SearchCameraSubFragment4 newInstance(String locationName,String description,Bitmap themeImage) {
        //フラグメントインスタンス生成
        SearchCameraSubFragment4 fragment = new SearchCameraSubFragment4();

        //バンドル生成
        Bundle args = new Bundle();

        //バイト配列出力ストリーム生成
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        //撮影写真をJPEG形式でバイト配列出力ストリームに入力
        themeImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        //撮影写真のバイト配列をバンドルに追加
        args.putByteArray(THEME_IMAGE_KEY, stream.toByteArray());

        //バンドルにお題スポット名追加
        args.putString(LOCATION_NAME_KEY,locationName);

        //バンドルにお題スポット説明追加
        args.putString(DESCRIPTION_KEY,description);

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
            byte[] takenImageBytes = getArguments().getByteArray(THEME_IMAGE_KEY);
            themeImage = BitmapFactory.decodeByteArray(takenImageBytes, 0, takenImageBytes.length);

            locationName=getArguments().getString(LOCATION_NAME_KEY);
            description=getArguments().getString(DESCRIPTION_KEY);
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_search_camera_sub_fragment4, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //お題写真ビュー取得
        themeImageView=view.findViewById(R.id.search_camera_sub_fragment4_takenImage);

        //お題スポット名テキストビュー取得
        final TextView locationNameText=view.findViewById(R.id.search_camera_sub_fragment4_title_text);

        //お題スポット説明テキストビュー取得
        final TextView descriptionText=view.findViewById(R.id.search_camera_sub_fragment4_tip_text);

        //コメントボックス取得
        final EditText commentText=view.findViewById(R.id.search_camera_sub_fragment4_message_editText);

        //投稿ボタン取得
        final Button publishButton=view.findViewById(R.id.search_camera_sub_fragment4_publish_button);

        //お題写真セット
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

        //撮影写真ビューにサイズ設定を適用
        themeImageView.setLayoutParams(layoutParams);


        //お題スポット名セット
        locationNameText.setText(locationName);

        //お題スポット説明セット
        descriptionText.setText(description);

        //投稿ボタン押下時処理設定
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //コメント取得
                String reviewString=commentText.getText().toString();

                //コメント最大長を超えた分をカット
                reviewString=reviewString.substring(0,Math.min(reviewString.length(),Constants.Common.MAX_THEME_SPOT_REVIEW_STRING_LENGTH));

                //フラグメント遷移コールバック実行
                ((CallBack)getParentFragment()).onSearchCameraSubFragment4Interaction(reviewString);
            }
        });
    }

    /* フラグメント破棄時処理 */
    @Override
    public void onDetach() {
        super.onDetach();

        //ImageViewクリア
        Function.cleanupView(themeImageView);

        //bitmapクリア
        Function.cleanupBitmap(themeImage);
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onSearchCameraSubFragment4Interaction(String message);
    }
}
