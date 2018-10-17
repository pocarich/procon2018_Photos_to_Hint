package jp.androidbook.photostohint;

/* ログイン画面新規登録フラグメント */

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
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginSubFragmentSignUp extends Fragment {

    //IDテキストボックス取得
    private EditText idText;

    //パスワードテキストボックス取得
    private EditText passwordText;

    //メールアドレステキストボックス取得
    private EditText mailAddressText;

    //新規登録フォーム表示
    private ScrollView signUpFormLayout;

    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //登録完了確認画面表示
    private ConstraintLayout confirmationLayout;

    //戻るボタン
    private ImageButton backButton;

    /* コンストラクタ */
    public LoginSubFragmentSignUp() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static LoginSubFragmentSignUp newInstance() {
        //インスタンス生成
        LoginSubFragmentSignUp fragment = new LoginSubFragmentSignUp();

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* View生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_login_sub_fragment_sign_up, container, false);
    }

    /* View生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //新規登録フォーム表示
        signUpFormLayout=view.findViewById(R.id.login_sub_fragment_sign_up_form_layout);

        //通信画面表示取得
        connectionLayout=view.findViewById(R.id.login_sub_fragment_sign_up_connection_layout);

        //通信失敗画面表示取得
        connectionFailureLayout=view.findViewById(R.id.login_sub_fragment_sign_up_connection_failure_layout);

        //登録完了確認画面表示取得
        confirmationLayout=view.findViewById(R.id.login_sub_fragment_sign_up_confirmation_layout);

        //IDテキストボックス取得
        idText =view.findViewById(R.id.login_sub_fragment_sign_up_id_text);

        //パスワードテキストボックス取得
        passwordText=view.findViewById(R.id.login_sub_fragment_sign_up_passwrord_text);

        //パスワード再入力テキストボックス取得
        final EditText repeatedPasswordText=view.findViewById(R.id.login_sub_fragment_sign_up_password_repeated_text);

        //メールアドレステキストボックス取得
        mailAddressText=view.findViewById(R.id.login_sub_fragment_sign_up_mail_address_text);

        //登録ボタン取得
        final Button signupButton=view.findViewById(R.id.login_sub_fragment_sign_up_register_button);

        //通信再試行ボタン取得
        final Button retryConnectionButton=view.findViewById(R.id.login_sub_fragment_sign_up_connection_failure_button);

        //確認ボタン取得
        final Button confirmationButton=view.findViewById(R.id.login_sub_fragment_sign_up_confirmation_button);

        //戻るボタン取得
        backButton=view.findViewById(R.id.login_sub_fragment_sign_up_backButton);

        //通信画面表示OFF
        connectionLayout.setVisibility(View.GONE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //確認画面表示OFF
        confirmationLayout.setVisibility(View.GONE);

        //登録ボタン押下時処理設定
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //入力IDが空なら
                if(idText.getText().toString().length()==0)
                {
                    //警告表示
                    Toast.makeText(getContext(), "IDが未入力です。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //入力IDに不正な文字が含まれている場合
                if(!idText.getText().toString().matches("^[a-zA-Z0-9_\\-]+$"))
                {
                    //警告表示
                    Toast.makeText(getContext(), "IDに無効な文字が含まれています。\n有効："+Constants.Account.CHAR_ID_CONTAIN, Toast.LENGTH_LONG).show();
                    return;
                }

                //入力パスワードの文字数が不正の場合
                if(passwordText.getText().toString().length()<Constants.Account.MIN_PASSWORD_LENGTH ||Constants.Account.MAX_PASSWORD_LENGTH <passwordText.getText().toString().length())
                {
                    //警告表示
                    Toast.makeText(getContext(), "パスワードの文字数が不正です。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //入力パスワードに不正な文字が含まれている場合
                if(!passwordText.getText().toString().matches("^[a-zA-Z0-9]+$"))
                {
                    //警告表示
                    Toast.makeText(getContext(), "パスワードに無効な文字が含まれています。\n有効："+Constants.Account.CHAR_PASSWORD_CONTAIN, Toast.LENGTH_LONG).show();
                    return;
                }

                //再入力パスワードが不一致の場合
                if(!passwordText.getText().toString().equals(repeatedPasswordText.getText().toString()))
                {
                    //警告表示
                    Toast.makeText(getContext(), "再入力されたパスワードが上記パスワードと一致しません。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //入力メールアドレスが空のとき
                if(mailAddressText.getText().toString().length()==0)
                {
                    //警告表示
                    Toast.makeText(getContext(), "メールアドレスが未入力です。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //入力メールアドレスに無効な文字が含まれている場合
                if(!mailAddressText.getText().toString().matches("^[a-zA-Z0-9@._\\-]+$"))
                {
                    //警告表示
                    Toast.makeText(getContext(), "メールアドレスに無効な文字が含まれています。\n有効："+Constants.Account.CHAR_MAIL_CONTAIN, Toast.LENGTH_LONG).show();
                    return;
                }

                //入力メースアドレスのフォーマットが不正の場合
                if(!mailAddressText.getText().toString().matches("^[a-zA-Z0-9@._\\-]+@[a-zA-Z0-9@._\\-]+$"))
                {
                    //警告表示
                    Toast.makeText(getContext(), "無効なメールアドレスです。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //新規登録HTTP通信実行
                connection();
            }
        });

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

                //戻るコールバック実行
                ((CallBack) getParentFragment()).onLoginSubFragmentSignUpInteraction();
            }
        });

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

                //戻るコールバック実行
                ((CallBack) getParentFragment()).onLoginSubFragmentSignUpInteraction();
            }
        });

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

                //新規登録HTTP通信実行
                connection();
            }
        });
    }

    /* 新規登録HTTP通信 */
    private void connection()
    {
        //通信画面表示ON
        connectionLayout.setVisibility(View.VISIBLE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //新規登録フォーム表示OFF
        signUpFormLayout.setVisibility(View.GONE);

        //登録完了確認画面表示OFF
        confirmationLayout.setVisibility(View.GONE);

        //戻るボタン表示OFF
        backButton.setVisibility(View.GONE);

        try {
            //JSON生成
            JSONObject json=new JSONObject();

            //JSONにID追加
            json.put("user_id",idText.getText().toString());

            //JSONにパスワード追加
            json.put("password",passwordText.getText().toString());

            //JSONにメールアドレス追加
            json.put("email",mailAddressText.getText().toString());

            //JSONに初期ヒントポイント追加
            json.put("hint_point",Constants.Search.DEFAULT_HINT_POINT);

            //JSONに初期探索ポイント追加
            json.put("search_point",Constants.Search.DEFAULT_SEARCH_POINT);

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
                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //登録環境確認画面表示ON
                            confirmationLayout.setVisibility(View.VISIBLE);

                            //戻るボタン表示ON
                            backButton.setVisibility(View.VISIBLE);
                        }

                        //"status"パラメータがfalseのとき（webサーバエラー発生時）
                        else {
                            //"result"パラメータ取得
                            int result = json.getInt("result");

                            if (result == -1) {
                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //通信失敗画面表示ON
                                connectionFailureLayout.setVisibility(View.VISIBLE);

                                //戻るボタン表示ON
                                backButton.setVisibility(View.VISIBLE);
                            }else if (result == 1) {
                                //警告表示
                                Toast.makeText(getContext(), "入力されたIDは既に使用されています。", Toast.LENGTH_SHORT).show();

                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //新規登録フォーム表示ON
                                signUpFormLayout.setVisibility(View.VISIBLE);

                                //戻るボタン表示ON
                                backButton.setVisibility(View.VISIBLE);
                            } else if (result == 2) {
                                //警告表示
                                Toast.makeText(getContext(), "入力されたメールアドレスは既に使用されています。", Toast.LENGTH_SHORT).show();

                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //新規登録フォーム表示ON
                                signUpFormLayout.setVisibility(View.VISIBLE);

                                //戻るボタン表示ON
                                backButton.setVisibility(View.VISIBLE);
                            }
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
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/signup.php",json.toString());
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
        void onLoginSubFragmentSignUpInteraction();
    }
}
