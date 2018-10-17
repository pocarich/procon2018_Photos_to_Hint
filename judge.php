<?php
/* お題スポット写真とユーザ撮影写真の照合正否判定 */

//ヘッダ：JSON形式、文字コード"UTF-8"
header('Content-Type: application/json; charset=utf-8');

//クライアントからのデータ受信
$params=json_decode(file_get_contents('php://input'),true);

//必要なパラメータが受信データに含まれていない場合エラー
if(!isset($params['image'])||!isset($params['theme_id']))
{
	$rtn=array(
		'status' => FALSE,
		'result' => "0"
	);
	exit(json_encode($rtn));
}

//ユーザ撮影写真データ(base64)取得
$image=$params['image'];

//base64から写真取得
$image=str_replace('data:image/png;base64,','',$image);
$image=str_replace(' ','+',$image);
$image=base64_decode($image);

//JPEG形式で一時保存
file_put_contents("./takenImage.jpg",$image);

//正否判定実行
exec('./judge '.$params['theme_id'],$out,$res);

//一時保存した写真削除
unlink("./takenImage.jpg");

//正否判定を送信
if($res==0)
{
	$status=TRUE;
}
else
{
	$status=FALSE;
}

$rtn=array(
	'status' => $status,
        'result' => $out,
);
echo json_encode($rtn);
