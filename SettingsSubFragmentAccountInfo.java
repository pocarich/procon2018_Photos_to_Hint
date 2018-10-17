package jp.androidbook.photostohint;

/* 設定モードアカウント管理フラグメント */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class SettingsSubFragmentAccountInfo extends Fragment {

    //遷移ID
    public static final int TRANSITION_ID_BACK = 0;
    public static final int TRANSITION_ID_RESET_PASSWORD = 1;
    public static final int TRANSITION_ID_RESET_MAIL_ADDRESS=2;
    public static final int TRANSITION_ID_LOGOUT=3;

    /* コンストラクタ */
    public SettingsSubFragmentAccountInfo(){
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SettingsSubFragmentAccountInfo newInstance() {
        //フラグメントインスタンス生成
        SettingsSubFragmentAccountInfo fragment = new SettingsSubFragmentAccountInfo();

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_settings_sub_fragment_account_info, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //SharedPreferencesに保存されているユーザIDとメールアドレス取得
        SharedPreferences prefs=getActivity().getSharedPreferences(Constants.SharedPreferences.ACCOUNT_DATA, Context.MODE_PRIVATE);
        String userID=prefs.getString(Constants.SharedPreferences.ID_KEY,null);
        String mailAddress=prefs.getString(Constants.SharedPreferences.MAIL_ADDRESS_KEY,null);

        //戻るボタン取得
        final ImageButton backButton=view.findViewById(R.id.settings_sub_fragment_account_info_back_button);

        //ユーザIDテキストビュー取得
        final TextView idText=view.findViewById(R.id.settings_sub_fragment_account_info_id_text);

        //マールアドレステキストビュー取得
        final TextView mailAddressText=view.findViewById(R.id.settings_sub_fragment_account_info_mail_address_text);

        //パスワード変更ボタン取得
        final Button resetPasswordButton=view.findViewById(R.id.settings_sub_fragment_account_info_reset_password_button);

        //メールアドレス変更ボタン取得
        final Button resetMailAddressButton=view.findViewById(R.id.settings_sub_fragment_account_info_reset_mail_address_button);

        //ログアウトボタン取得
        final Button logoutButton=view.findViewById(R.id.settings_sub_fragment_account_info_logout_button);

        //ユーザIDセット
        idText.setText(userID);

        //メールアドレスセット
        mailAddressText.setText(mailAddress);

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

                //フラグメント遷移コールバック実行
                ((CallBack) getParentFragment()).onSettingsSubFragmentAccountInfoInteraction(TRANSITION_ID_BACK);
            }
        });

        //パスワード変更ボタン押下時処理設定
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //フラグメント遷移コールバック実行
                ((CallBack) getParentFragment()).onSettingsSubFragmentAccountInfoInteraction(TRANSITION_ID_RESET_PASSWORD);
            }
        });

        //メールアドレス変更ボタン押下時処理設定
        resetMailAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //フラグメント遷移コールバック実行
                ((CallBack) getParentFragment()).onSettingsSubFragmentAccountInfoInteraction(TRANSITION_ID_RESET_MAIL_ADDRESS);
            }
        });

        //ログアウトボタン押下時処理設定
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //アラートダイアログ表示
                new AlertDialog.Builder(getActivity())
                        .setTitle("確認")
                        .setMessage("ログアウトします。よろしいですか？")

                        //OKボタン押下時
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //SharedpReferencesに保存されているユーザIDリセット
                                SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SharedPreferences.ACCOUNT_DATA, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(Constants.SharedPreferences.ID_KEY, null);
                                editor.apply();

                                //フラグメント遷移コールバック実行
                                ((CallBack) getParentFragment()).onSettingsSubFragmentAccountInfoInteraction(TRANSITION_ID_LOGOUT);
                            }
                        })

                        //キャンセルボタン押下時
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onSettingsSubFragmentAccountInfoInteraction(int transitionID);
    }
}
