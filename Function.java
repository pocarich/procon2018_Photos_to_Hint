package jp.androidbook.photostohint;

/* 汎用関数クラス */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import java.io.ByteArrayOutputStream;

public final class Function {

    /* bitmapをbase64にエンコード */
    public static String encodeTobase64(Bitmap image)
    {
        //バイト配列出力ストリーム生成
        ByteArrayOutputStream baos=new ByteArrayOutputStream();

        //bitmapをJPEG形式でバイト配列出力ストリームに入力
        image.compress(Bitmap.CompressFormat.JPEG,100,baos);

        //バイト配列出力ストリームからバイト配列取得
        byte[] b=baos.toByteArray();

        //バイト配列をbase64にエンコード
        String imageEncoded=Base64.encodeToString(b, Base64.NO_WRAP);

        //base64を返す
        return imageEncoded;
    }

    /* base64をデコードしてbitmap生成 */
    public static Bitmap decodeBase64(String input)
    {
        //base64をデコードしてバイト配列取得
        byte[] decodedByte=Base64.decode(input,0);

        //バイト配列からbitmapを生成して返す
        return BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.length);
    }

    /* ビューのクリア */
    public static final void cleanupView(View view)
    {
        //ImageButtonのインスタンスなら
        if(view instanceof ImageButton)
        {
            //ビューをImageButtonにキャスト
            ImageButton imageButton=(ImageButton)view;

            //画像にnullをセット（明示的にbitmapの参照を外す）
            imageButton.setImageDrawable(null);

            //背景にnullをセット（明示的にbitmapの参照を外す）
            imageButton.setBackground(null);
        }

        //ImageViewのインスタンスなら
        else if(view instanceof ImageView)
        {
            //ビューをImageViewにキャスト
            ImageView imageView=(ImageView)view;

            //画像にnullをセット（明示的にbitmapの参照を外す）
            imageView.setImageDrawable(null);

            //背景にnullをセット（明示的にbitmapの参照を外す）
            imageView.setBackground(null);
        }

        //ビューがnullでないなら
        if(view!=null)
        {
            //nullを代入(明示的にインスタンスへの参照を外す)
            view=null;
        }
    }

    /* bitmapのクリア */
    public static final void cleanupBitmap(Bitmap bitmap)
    {
        //bitmapがnullでないなら
        if(bitmap!=null)
        {
            //bitmap解放
            bitmap.recycle();

            //nullを代入（明示的にインスタンスへの参照を外す）
            bitmap=null;
        }
    }

    //外部からのインスタンス生成制限
    private Function(){}
}
