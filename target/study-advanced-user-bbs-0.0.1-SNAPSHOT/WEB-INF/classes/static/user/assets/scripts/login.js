const $loginForm = document.getElementById('loginForm');
const emailRegex = new RegExp('^(?=.{8,50}$)([\\da-z\\-_.]{4,})@([\\da-z][\\da-z\\-]*[\\da-z]\\.)?([\\da-z][\\da-z\\-]*[\\da-z])\\.([a-z]{2,15})(\\.[a-z]{2,3})?$');
const passwordRegex = new RegExp('^([\\da-zA-Z`~!@#$%^&*()\\-_=+\\[{\\]}\\\\|;:\'",<.>/?]{8,50})$');
$loginForm.onsubmit=(e)=>{
    e.preventDefault();
    const $emailLabel = $loginForm.querySelector('.--object-label:has(input[name="email"])');
    const $passwordLabel = $loginForm.querySelector('.--object-label:has(input[name="password"])');
    const $labels = [$emailLabel,$passwordLabel];
    $labels.forEach(($label) => $label.setValid(true));

    if ($loginForm['email'].value==='') {
        $emailLabel.setValid(false,'이메일을 입력해주세요.');
        $loginForm['email'].focus();
        return;
    }
    if (!emailRegex.test($loginForm['email'].value)) {
        $emailLabel.setValid(false,'올바른 이메일을 입력해주세요.');
        $loginForm['email'].focus();
        $loginForm['email'].select();
        return;
    }
    if ($loginForm['password'].value==='') {
        $passwordLabel.setValid(false,'비밀번호를 입력해주세요.');
        $loginForm['password'].focus();
        return;
    }
    if (!passwordRegex.test($loginForm['password'].value)) {
        $passwordLabel.setValid(false,'올바른 비밀번호를 입력해주세요.');
        $loginForm['password'].focus();
        $loginForm['password'].select();
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', $loginForm['email'].value);
    formData.append('password', $loginForm['password'].value);
    xhr.onreadystatechange=()=>{
        if(xhr.readyState !== XMLHttpRequest.DONE){
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300){
            
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'failure_suspended':
                dialog.showSimpleOk('로그인', '해당 계정은 이용이 정지된 상태입니다. 관리자에게 문의해 주세요.');
                break;
            case 'success':
                dialog.showSimpleOk('로그인','로그인에 성공하였습니다.')
                if ($loginForm['remember'].checked) {
                    localStorage.setItem('loginEmail', $loginForm['email'].value);
                } else {
                    localStorage.removeItem('loginEmail');
                }
                location.href = new URL(location.href).searchParams.get('forward')?? "/board/list?id=free";
                break;
            case 'failure':
                dialog.showSimpleOk('로그인', '이메일 또는 비밀번호가 올바르지 않습니다.');
                break;
            default:
                dialog.showSimpleOk('로그인', '알 수 없는 오류가 발생했습니다.');
                break;

        }
    };
    xhr.open('POST','/user/login');
    xhr.send(formData);
}

$loginForm['email'].value = localStorage.getItem('loginEmail')??'';

