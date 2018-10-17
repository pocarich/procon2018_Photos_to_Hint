package jp.androidbook.photostohint;

/* 観光モード口コミ投稿フラグメント2 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class TourismAddReviewSubFragment2 extends Fragment implements Serializable{

    //遷移ID
    public static final int TRANSITION_ID_BACK = 0;
    public static final int TRANSITION_ID_NEXT = 1;

    //Bundleキー
    private static final String TAKEN_IMAGE_KEY="TAKEN_IMAGE";

    //撮影写真ビュー
    private ImageView takenImageView;

    //撮影写真
    private Bitmap takenImage;

    /* コンストラクタ */
    public TourismAddReviewSubFragment2() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static TourismAddReviewSubFragment2 newInstance(Bitmap takenImage) {
        //フラグメントインスタンス生成
        TourismAddReviewSubFragment2 fragment = new TourismAddReviewSubFragment2();

        //バンドル生成
        Bundle args = new Bundle();

        //バイト配列出力ストリーム生成
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        //撮影写真をJPEG形式でバイト配列出力ストリームに入力
        takenImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        //撮影写真のバイト配列をバンドルに追加
        args.putByteArray(TAKEN_IMAGE_KEY, stream.toByteArray());

        //フラグメントインスタンスにバンドルセット
        fragment.setArguments(args);

        //フラグメント委ｈんスタンを返す
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
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_tourism_add_review_sub_fragment2, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //キャンセルボタン取得
        final Button noButton=view.findViewById(R.id.tourism_add_review_sub_fragment2_no_button);

        //OKボタン取得
        final Button yesButton=view.findViewById(R.id.tourism_add_review_sub_fragment2_yes_button);

        //戻るボタン取得
        final ImageButton backButton=view.findViewById(R.id.tourism_add_review_sub_fragment2_backButton);

        //撮影写真ビュー
        takenImageView=view.findViewById(R.id.tourism_add_review_sub_fragment2_takenImage);

        //撮影写真セット
        takenImageView.setImageBitmap(takenImage);

        //ディスプレイメトリクス生成
        DisplayMetrics dm = new DisplayMetrics();

        //ディスプレイサイズ取得
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        //お題スポット写真表示用イメージビューの横幅をディスプレイ横幅の0.9倍に設定
        double width = 0.9 * dm.widthPixels;

        //上で設定した横幅を基準に16:9でサイズ設定
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) width, (int) (width * Constants.Common.ASPECT_RATIO_VERTICAL / Constants.Common.ASPECT_RATIO_HORIZONTAL));

        //layout_margin設定(left 5dp,top 5dp,right 5dp,bottom 0dp)
        layoutParams.setMargins(0, (int) getResources().getDimension(R.dimen.dp15), 0, 0);

        //水平方向に中央揃え
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        //撮影写真ビューにサイズ設定を適用
        takenImageView.setLayoutParams(layoutParams);

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
                ((CallBack)getParentFragment()).onTourismAddReviewSubFragment2Interaction(TRANSITION_ID_BACK,null);
            }
        });

        //キャンセルボタン押下時処理設定
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //撮影開始
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,Constants.Common.REQUEST_CODE_CAMERA);
            }
        });

        //OKボタン押下時処理設定
        yesButton.setOnClickListener(new View.OnClickListener() {
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
                ((CallBack)getParentFragment()).onTourismAddReviewSubFragment2Interaction(TRANSITION_ID_NEXT,takenImage);
            }
        });
    }

    /* 撮影終了時コールバック */
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        //撮影終了時で正常終了した場合
        if(requestCode==Constants.Common.REQUEST_CODE_CAMERA&&resultCode== Activity.RESULT_OK)
        {
            //出力データがあるなら
            if(data.getExtras()!=null)
            {
                //写真取得
                Bitmap takenImage=(Bitmap)data.getExtras().get("data");

                //写真が存在したら
                if(takenImage!=null)
                {
                    //写真セット
                    takenImageView.setImageBitmap(takenImage);
                }
            }
        }
    }

    /* フラグメント破棄時処理 */
    @Override
    public void onDetach() {
        super.onDetach();

        //takenImageViewクリア
        Function.cleanupView(takenImageView);

        //takenImageクリア
        Function.cleanupBitmap(takenImage);
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onTourismAddReviewSubFragment2Interaction(int transitionID,Bitmap takenImage);
    }
}
