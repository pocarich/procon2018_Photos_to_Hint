package jp.androidbook.photostohint;

/* ログイン画面ログインフラグメント */

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
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginSubFragmentSignIn extends Fragment {

    //遷移ID
    public static final int TRANSITION_ID_LOGIN = 0;
    public static final int TRANSITION_ID_SIGNUP = 2;

    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //メインコンテンツ画面表示
    private ConstraintLayout mainLayout;

    //IDテキストボックス
    private EditText idText;

    //パスワードテキストボックス
    private EditText passwordText;

    /* コンストラクタ */
    public LoginSubFragmentSignIn() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static LoginSubFragmentSignIn newInstance() {
        //フラグメントインスタンス生成
        LoginSubFragmentSignIn fragment = new LoginSubFragmentSignIn();

        //フラグメントインスタンス返す
        return fragment;
    }

    /* View生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_login_sub_fragment_sign_in, container, false);
    }

    /* View生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //通信画面表示取得
        connectionLayout=view.findViewById(R.id.login_sub_fragment_download_layout);

        //通信失敗画面表示取得
        connectionFailureLayout=view.findViewById(R.id.login_sub_fragment_connection_failure_layout);

        //メインコンテンツ画面表示取得
        mainLayout=view.findViewById(R.id.login_sub_fragment_sign_in_form_layout);

        //IDテキストボックス取得
        idText=view.findViewById(R.id.login_sub_fragment_sign_in_id_text);

        //パスワードテキストボックス取得
        passwordText=view.findViewById(R.id.login_sub_fragment_sign_in_password_text);

        //通信再試行ボタン取得
        final Button retryButton=view.findViewById(R.id.login_sub_fragment_connection_failure_button);

        //ログインボタン取得
        final Button signinButton=view.findViewById(R.id.login_sub_fragment_sign_in_login_button);

        //final Button passwordButton=view.findViewById(R.id.login_sub_fragment_sign_in_password_button);

        //新規登録ボタン取得
        final Button signupButton=view.findViewById(R.id.login_sub_fragment_sign_in_signup_button);

        //通信画面表示OFF
        connectionLayout.setVisibility(View.GONE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //メインコンテンツ画面表示ON
        mainLayout.setVisibility(View.VISIBLE);

        //ログインボタン押下時処理設定
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //入力iDが空なら
                if(idText.getText().toString().length()==0)
                {
                    //警告表示
                    Toast.makeText(getContext(), "IDが未入力です。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //入力IDに無効な文字が含まれている場合
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

                //入力パスワードに無効な文字が含まれている場合
                if(!passwordText.getText().toString().matches("^[a-zA-Z0-9]+$"))
                {
                    //警告表示
                    Toast.makeText(getContext(), "パスワードに無効な文字が含まれています。\n有効："+Constants.Account.CHAR_PASSWORD_CONTAIN, Toast.LENGTH_LONG).show();
                    return;
                }

                //ログインHTTP通信実行
                upload();
            }
        });

        /*passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                v.startAnimation(scaleAnimation);

                //((CallBack) getParentFragment()).onLoginSubFragmentSignInInteraction(1);
            }
        });*/

        //新規登録ボタン押下時処理設定
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

                //フラグメント遷移コールバック実行
                ((CallBack) getParentFragment()).onLoginSubFragmentSignInInteraction(2,null);
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

                //ログインHTTP通信実行
                upload();
            }
        });
    }

    /* ログインHTTP通信 */
    private void upload()
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

            //JSONに入力ユーザID追加
            json.put("user_id",idText.getText().toString());

            //JSONに入力パスワード追加
            json.put("password",passwordText.getText().toString());

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
                            //"result"パラメータ（入力IDに紐づけられたメールアドレス）取得
                            String mailAddress=json.getString("result");

                            //SharedPreferencesにIDとメールアドレスを保存
                            SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SharedPreferences.ACCOUNT_DATA, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(Constants.SharedPreferences.ID_KEY, idText.getText().toString());
                            editor.putString(Constants.SharedPreferences.MAIL_ADDRESS_KEY,mailAddress);
                            editor.apply();

                            //フラグメント遷移コールバック実行
                            ((CallBack) getParentFragment()).onLoginSubFragmentSignInInteraction(0,idText.getText().toString());
                        }

                        //"status"パラメータがfalseのとき（webサーバエラー発生時）
                        else {
                            //"result"パラメータ取得
                            int result = json.getInt("result");

                            if(result==-1)
                            {
                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //通信失敗画面表示ON
                                connectionFailureLayout.setVisibility(View.VISIBLE);
                            }
                            if (result == 1) {
                                //警告表示
                                Toast.makeText(getContext(), "IDが不正です。", Toast.LENGTH_SHORT).show();

                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //メインコンテンツ画面表示ON
                                mainLayout.setVisibility(View.VISIBLE);
                            } else if (result == 2) {
                                //警告表示
                                Toast.makeText(getContext(), "パスワードが不正です。", Toast.LENGTH_SHORT).show();

                                //通信画面表示OFF
                                connectionLayout.setVisibility(View.GONE);

                                //メインコンテンツ画面表示ON
                                mainLayout.setVisibility(View.VISIBLE);
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
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/signin.php",json.toString());
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
    public interface CallBack {
        void onLoginSubFragmentSignInInteraction(int transitionID,String userID);
    }
}
