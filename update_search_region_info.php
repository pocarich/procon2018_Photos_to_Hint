<?php
/* 指定地域の探索状況更新 */


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
if(!isset($params["user_id"])||!isset($params["region_name"]))
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

//指定地域の探索状況について変更のあるカラムを更新するクエリ
$sql="UPDATE search_region_info SET ";
$setParams=FALSE;
if(array_key_exists("hint_point",$params))
{
	$sql.="hint_point = '{$params["hint_point"]}',";
	$setParams=TRUE;
}
if(array_key_exists("search_point",$params))
{
	$sql.="search_point = '{$params["search_point"]}',";
	$setParams=TRUE;
}
if(array_key_exists("target_location_name",$params))
{
	$sql.="target_location_name = '{$params["target_location_name"]}',";
	$setParams=TRUE;
}
if(array_key_exists("hint_level",$params))
{
	$sql.="hint_level = '{$params["hint_level"]}',";
	$setParams=TRUE;
}
if(!$setParams)
{
	$rtn = array(
             'status' => FALSE,
             'error' =>"no params are set."
        );
        exit(json_encode($rtn));
}
$sql=substr($sql,0,-1);
$sql.=" WHERE user_id='{$params["user_id"]}' AND region_name='{$params["region_name"]}'";

//指定地域の探索状況を更新して結果を送信
echo $db->query($sql);