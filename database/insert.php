<?php 

    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');


    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");


    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android){

        // 안드로이드 코드의 postParameters 변수에 적어준 이름을 가지고 값을 전달 받습니다.

        $name=$_POST['name'];
        $birth=$_POST['birth'];
        $email=$_POST['email'];
        $gender=$_POST['gender'];
        $video=$_POST['video'];
        $agreement=$_POST['agreement'];

        if(empty($name)){
            $errMSG = "이름을 입력하세요.";
        }
        else if(empty($email)){
            $errMSG = "이메일을 입력하세요.";
        }
        else if(empty($birth)){
            $errMSG = "생년월일을 입력하세요.";
        }
        else if(empty($gender)){
            $errMSG = "성별을 선택하세요.";
        }
        else if(empty($video)){
            $errMSG = "동영상이 존재하지 않습니다.";
        }
        else if(empty($agreement)){
            $errMSG = "개인정보이용동의에 동의하지 않았습니다.";
        }

        try{
            // SQL문을 실행하여 데이터를 MySQL 서버의 person 테이블에 저장합니다. 
            $stmt = $con->prepare('INSERT INTO ldcc_member_info(member_name, member_birthdate, member_email, member_gender, member_facevideo, member_agreement) VALUES(:name, :birth, :email, :gender, :video, :agreement)');
            $stmt->bindParam(':name', $name);
            $stmt->bindParam(':birth', $birth);
            $stmt->bindParam(':email', $email);
            $stmt->bindParam(':gender', $gender);
            $stmt->bindParam(':video', $video);
            $stmt->bindParam(':agreement', $agreement);

            if($stmt->execute()){
                $successMSG = "새로운 사용자를 추가했습니다.";
            }
            else{
                $errMSG = "사용자 추가 에러";
            }

        } catch(PDOException $e) {
            die("Database error: " . $e->getMessage()); 
        }
    }

?>


<?php 
    if (isset($errMSG)) echo $errMSG;
    if (isset($successMSG)) echo $successMSG;

    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
    
    if(!$android){
?>
    <html>
        <body>
            <form action="<?php $_PHP_SELF ?>" method="POST">
                Name: <input type="text" name="name"/>
                Birth: <input type="text" name="birth"/>
                Email: <input type="email" name="email"/>
                Gender: <input type="text" name="gender"/>
                <input type="submit" name="submit"/>
            </form>
        <body>
    <html>

<?php
    }
?>
    