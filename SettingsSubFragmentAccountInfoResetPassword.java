package jp.androidbook.photostohint;

/* 設定モードアカウント管理パスワード変更フラグメント */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsSubFragmentAccountInfoResetPassword extends Fragment {

    //通信画面表示
    FrameLayout connectionLayout;

    //通信失敗画面表示
    ConstraintLayout connectionFailureLayout;

    //入力フォーム画面表示
    LinearLayout inputFormLayout;

    //更新完了確認画面表示
    ConstraintLayout confirmationLayout;

    //戻るボタン
    ImageButton backButton;

    //現行パスワードテキストボックス
    EditText currentPasswordEditText;

    //新パスワードテキストボックス
    EditText newPasswordEditText;

    //新パスワード再入力テキストボックス
    EditText repeatedNewPasswordEditText;

    /* コンストラクタ */
    public SettingsSubFragmentAccountInfoResetPassword(){
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SettingsSubFragmentAccountInfoResetPassword newInstance() {
        //フラグメントインスタンス生成
        SettingsSubFragmentAccountInfoResetPassword fragment = new SettingsSubFragmentAccountInfoResetPassword();

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_settings_sub_fragment_account_info_reset_password, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //入力フォーム画面表示取得
        inputFormLayout=view.findViewById(R.id.settings_sub_fragment_account_info_reset_password_input_form_layout);

        //変更完了確認画面表示取得
        confirmationLayout=view.findViewById(R.id.settings_sub_fragment_account_info_reset_password_confirmation_layout);

        //通信画面表示取得
        connectionLayout=view.findViewById(R.id.settings_sub_fragment_account_info_reset_password_connection_layout);

        //通信失敗画面表示取得
        connectionFailureLayout=view.findViewById(R.id.settings_sub_fragment_account_info_reset_password_connection_failure_layout);

        //現行パスワードテキストボックス取得
        currentPasswordEditText=view.findViewById(R.id.settings_sub_fragment_account_info_reset_password_current_password_text);

        //新パスワードテキストボックス取得
        newPasswordEditText=view.findViewById(R.id.settings_sub_fragment_account_info_reset_password_new_password_text);

        //再入力新パスワードテキストボックス取得
        repeatedNewPasswordEditText=view.findViewById(R.id.settings_sub_fragment_account_info_reset_password_new_repeated_password_text);

        //戻るボタン取得
        backButton=view.findViewById(R.id.settings_sub_fragment_account_info_reset_password_back_button);

        //アップロードボタン取得
        final Button uploadButton=view.findViewById(R.id.settings_sub_fragment_account_info_reset_password_upload_button);

        //通信再試行ボタン取得
        final Button retryConnectionButton=view.findViewById(R.id.settings_sub_fragment_account_info_reset_password_connection_failure_button);

        //変更完了確認ボタン取得
        final Button confirmationButton=view.findViewById(R.id.settings_sub_fragment_account_info_reset_password_confirmation_button);

        //通信画面表示OFF
        confirmationLayout.setVisibility(View.GONE);

        //通信失敗画面表示OFF
        connectionLayout.setVisibility(View.GONE);

        //変更完了確認画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

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
                ((CallBack) getParentFragment()).onSettingsSubFragmentAccountInfoResetPasswordInteraction();
            }
        });

        //変更アップロードボタン押下時処理設定
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //現行パスワードの文字数が不正なら
                if(currentPasswordEditText.getText().toString().length()<Constants.Account.MIN_PASSWORD_LENGTH ||Constants.Account.MAX_PASSWORD_LENGTH <currentPasswordEditText.getText().toString().length())
                {
                    //トーストで警告
                    Toast.makeText(getContext(), "現在のパスワードの文字数が不正です。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //現行パスワードに不正な文字が含まれているなら
                if(!currentPasswordEditText.getText().toString().matches("^[a-zA-Z0-9]+$"))
                {
                    //トーストで警告
                    Toast.makeText(getContext(), "現在のパスワードに無効な文字が含まれています。\n有効："+Constants.Account.CHAR_PASSWORD_CONTAIN, Toast.LENGTH_LONG).show();
                    return;
                }

                //新パスワードの文字数が不正なら
                if(newPasswordEditText.getText().toString().length()<Constants.Account.MIN_PASSWORD_LENGTH ||Constants.Account.MAX_PASSWORD_LENGTH <newPasswordEditText.getText().toString().length())
                {
                    //トーストで警告
                    Toast.makeText(getContext(), "新しいパスワードの文字数が不正です。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //新パスワードに不正な文字が含まれているら
                if(!newPasswordEditText.getText().toString().matches("^[a-zA-Z0-9]+$"))
                {
                    //トーストで警告
                    Toast.makeText(getContext(), "新しいパスワードに無効な文字が含まれています。\n有効："+Constants.Account.CHAR_PASSWORD_CONTAIN, Toast.LENGTH_LONG).show();
                    return;
                }

                //新パスワードと再入力が一致しないなら
                if(!newPasswordEditText.getText().toString().equals(repeatedNewPasswordEditText.getText().toString()))
                {
                    //トーストで警告
                    Toast.makeText(getContext(), "再入力されたパスワードが上記パスワードと一致しません。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //変更アップロードHTTP通信実行
                uploadInfo();            }
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

                //変更アップロードHTTP通信実行
                uploadInfo();
            }
        });

        //変更完了確認ボタン押下時処理設定
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
                ((CallBack) getParentFragment()).onSettingsSubFragmentAccountInfoResetPasswordInteraction();
            }
        });
    }

    /* 変更アップロードHTTP通信 */
    private void uploadInfo()
    {
        //通信画面表示ON
        connectionLayout.setVisibility(View.VISIBLE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //入力フォーム画面表示OFF
        inputFormLayout.setVisibility(View.GONE);

        //変更完了確認画面表示OFF
        confirmationLayout.setVisibility(View.GONE);

        //戻るボタン表示OFF
        backButton.setVisibility(View.GONE);

        try {
            //SharedPreferencesに保存されているユーザID取得
            SharedPreferences prefs=getActivity().getSharedPreferences(Constants.SharedPreferences.ACCOUNT_DATA, Context.MODE_PRIVATE);
            String user_id=prefs.getString(Constants.SharedPreferences.ID_KEY,null);

            //JSON生成
            JSONObject json=new JSONObject();

            //JSONに各パラメータ追加
            json.put("user_id",user_id);
            json.put("password",currentPasswordEditText.getText().toString());
            json.put("new_password",newPasswordEditText.getText().toString());

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

                        //戻るボタン表示ON
                        backButton.setVisibility(View.VISIBLE);

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
                            int result = json.getInt("result");
                            switch(result)
                            {
                                case 0:
                                    //通信画面表示OFF
                                    connectionLayout.setVisibility(View.GONE);

                                    //変更完了確認画面表示ON
                                    confirmationLayout.setVisibility(View.VISIBLE);
                                    break;
                                case 1:
                                    //トーストで警告
                                    Toast.makeText(getContext(), "パスワードが不正です。", Toast.LENGTH_SHORT).show();

                                    //通信画面表示OFF
                                    connectionLayout.setVisibility(View.GONE);

                                    //入力フォーム画面表示ON
                                    inputFormLayout.setVisibility(View.VISIBLE);

                                    //戻るボタン表示ON
                                    backButton.setVisibility(View.VISIBLE);

                                    break;
                                case 2:
                                    //トーストで警告
                                    Toast.makeText(getContext(), "入力されたメールアドレスは既に使用されています。", Toast.LENGTH_SHORT).show();

                                    //通信画面表示OFF
                                    connectionLayout.setVisibility(View.GONE);

                                    //入力フォーム画面表示ON
                                    inputFormLayout.setVisibility(View.VISIBLE);

                                    //戻るボタン表示ON
                                    backButton.setVisibility(View.VISIBLE);

                                    break;
                            }
                        }

                        //"status"パラメータがfalseなら（webサーバーエラー発生時）
                        else {
                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //通信失敗画面表示ON
                            connectionFailureLayout.setVisibility(View.VISIBLE);

                            //戻るボタン表示ON
                            backButton.setVisibility(View.VISIBLE);
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

                        //戻るボタン表示ON
                        backButton.setVisibility(View.VISIBLE);
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/update_account.php",json.toString());
        }

        //JSON例外発生時
        catch (JSONException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();

            //通信画面表示OFF
            connectionLayout.setVisibility(View.GONE);

            //通信失敗画面表示ON
            connectionFailureLayout.setVisibility(View.VISIBLE);

            //戻るボタン表示ON
            backButton.setVisibility(View.VISIBLE);
        }
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onSettingsSubFragmentAccountInfoResetPasswordInteraction();
    }
}
