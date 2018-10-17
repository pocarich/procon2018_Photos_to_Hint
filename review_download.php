<?php
/* 指定地域のユーザ口コミ取得 */

//データベース操作クラス
class MyDB
{
     //mysqli使用
     public $mysqli;

     /* コンストラクタ */
     function __construct($host_or_ip,$user_id,$password,$db_name)
     {
          //データベースアクセス
          $this->mysqli = new mysqli($host_or_ip,$user_id,$password,$db_name);

          //コネクションエラー発生時
          if($this->mysqli->connect_error)
          {
               //処理中断
               echo $this->mysqli->connect_error;
               exit;
          }
          else
          {
               //文字コード"UTF-8"使用
               $this->mysqli->set_charset("utf8");
          }
     }

     /* デストラクタ */
     function __destruct()
     {
          //データベースアクセス終了
          $this->mysqli->close();
     }

     /* クエリ実行 */
     function query($sql)
     {
          //クエリを実行して結果を受け取る
          $result = $this->mysqli->query($sql);

          //エラー発生時
          if($result == FALSE)
          {
               //エラーをJSON形式で返す
               $error = $this->mysqli->errno.": ".$this->mysqli->error;
               $rtn = array(
                    'status' => FALSE,
                    'count' => 0,
                    'result' => "",
                    'error' => $error
               );
               return json_encode($rtn);
          }

          //ブーリアン結果(SELECT文以外)の場合
          if($result === TRUE)
          {
               //変更を受けたレコード数をJSON形式で返す
               $rtn = array(
                    'status' => TRUE,
                    'count' => $this->mysqli->affected_rows,
                    'result' => "",
                    'error' => ""
               );
               return json_encode($rtn);
          }

          //ブーリアン結果でない場合
          else
          {
               //取得データをJSON形式で返す
               $data = array();
               while($row = $result->fetch_assoc())
               {
                    $data[]=$row;
               }
               $result->close();

               $rtn = array(
                    'status' => TRUE,
                    'count' => $this->mysqli->affected_rows,
                    'result' => $data,
                    'error' =>""
               );
               return json_encode($rtn);
          }

          //文字列のエスケープ
          function escape($str)
          {
               return $this->mysqli->real_escape_string($str);
          }
     }   
}

//ヘッダ：JSON形式、文字コード"UTF-8"
header('Content-Type: application/json; charset=utf-8');

//クライアントからのデータ受信
$params=json_decode(file_get_contents('php://input'),true);

//必要なパラメータが受信データに含まれていない場合エラー
if(!isset($params["region_name"]))
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => 'some parameters are not set.'
	);
	exit(json_encode($rtn));
}


//データベース接続
$db = new MyDB("*****","*****","*****","*****");

//日をまたいだユーザ口コミを取得するクエリ
$select_expired_data_sql="SELECT review_id FROM parsonal_review WHERE insert_time < DATE_SUB(NOW(),INTERVAL 1 DAY)";

//日をまたいだユーザ口コミを削除するクエリ
$delete_expired_data_sql="DELETE FROM parsonal_review WHERE insert_time < DATE_SUB(NOW(),INTERVAL 1 DAY)";

//指定地域のユーザ口コミを取得するクエリ
$select_inspired_data_sql="SELECT * FROM parsonal_review WHERE region_name='{$params["region_name"]}'";

//日をまたいだユーザ口コミを取得
$expired_data_list=$db->query($select_expired_data_sql);
$expired_data_list=json_decode($expired_data_list,true);

//エラー発生時エラー内容をJSON形式で送信して終了
if($expired_data_list['status']===FALSE)
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => $expired_data_list['error']
	);
	exit(json_encode($rtn));
}

//日をまたいだユーザ口コミに対応する写真を削除
foreach($expired_data_list['result'] as $expired_data)
{
	unlink('img/image'.$expired_data['review_id'].'.jpg');
}

//日をまたいだユーザ口コミを削除
$result=$db->query($delete_expired_data_sql);
$result=json_decode($result,true);

//エラー発生時エラー内容をJSON形式で送信して終了
if($result['status']===FALSE)
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => $result['error']
	);
	exit(json_encode($rtn));
}

//指定地域のユーザ口コミを取得して送信
echo $db->query($select_inspired_data_sql);