const emailRegex = new RegExp('^(?=.{8,50}$)([\\da-z\\-_.]{4,})@([\\da-z][\\da-z\\-]*[\\da-z]\\.)?([\\da-z][\\da-z\\-]*[\\da-z])\\.([a-z]{2,15})(\\.[a-z]{2,3})?$');
const passwordRegex = new RegExp('^([\\da-zA-Z`~!@#$%^&*()\\-_=+\\[{\\]}\\\\|;:\'",<.>/?]{8,50})$');
const emailCodeRegex = new RegExp('^(\\d{6})$');
const $loading = document.getElementById('loading');
const $recoverForm = document.getElementById('recoverForm');
$recoverForm['pEmailCodeSendButton'].addEventListener('click', () => {
    const $emailLabel = $recoverForm.querySelector('label:has(input[name="pEmail"])');
    if ($recoverForm['pEmail'].value == '') {
        $emailLabel.setValid(false, '이메일을 입력해 주세요.');
        $recoverForm['pEmail'].focus();
        return;
    }
    if (!emailRegex.test($recoverForm['pEmail'].value)) {
        $emailLabel.setValid(false, '올바른 이메일을 입력해주세요.');
        $recoverForm['pEmail'].focus();
        $recoverForm['pEmail'].select();
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', $recoverForm['pEmail'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        $loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('인증번호 전송', `요청을 처리하는 도중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response['result']) {
            case'success':
                $recoverForm['pEmailSalt'].value = response.salt;
                $recoverForm['pEmail'].setDisabled(true);
                $recoverForm['pEmailCodeSendButton'].setDisabled(true);
                $recoverForm['pEmailCode'].setDisabled(false);
                $recoverForm['pEmailCodeVerifyButton'].setDisabled(false);
                $recoverForm['pEmailCode'].focus();
                dialog.showSimpleOk('인증번호 전송', '입력하신 이메일로 인증버호를 전송하였습니다. 해당 인증버호는 10분가만 유효하니 유의해 주세요.');
                break;
            case'failure':
                dialog.showSimpleOk('인증번호 전송', '알 수 없는 이유로 인증번호를 전송하지 못하였습니다. 잠시 후 다시 시도해 주세요.')
                break;
            default:
                dialog.showSimpleOk('인증번호 전송', `입력하신 이메일 '${$recoverForm['pEmail'].value}'을/를 사용중인 계정을 찾지 못하였습니다`);
                break;
        }
    };
    xhr.open('POST', '/user/recover-password-email');
    xhr.send(formData);

    $loading.show();
})
$recoverForm['pEmailCodeVerifyButton'].addEventListener('click', () => {
    const $emailLabel = $recoverForm.querySelector('label:has(input[name="pEmail"])');
    if ($recoverForm['pEmailCode'].value == '') {
        $emailLabel.setValid(false, '인증번호를 입력해주세요.')
        return;
    }
    if (!emailCodeRegex.test($recoverForm['pEmailCode'].value)) {
        $emailLabel.setValid(false, '올바른 인증번호를 입력해주세요.');
        $recoverForm['pEmailCode'].focus();
        $recoverForm['pEmailCode'].select();
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', $recoverForm['pEmail'].value);
    formData.append('code', $recoverForm['pEmailCode'].value);
    formData.append('salt', $recoverForm['pEmailSalt'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {

            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'success':
                $recoverForm['pEmailCode'].setDisabled(true);
                $recoverForm['pEmailCodeVerifyButton'].setDisabled(true);
                $recoverForm['pPassword'].setDisabled(false);
                $recoverForm['pPasswordCheck'].setDisabled(false);
                $recoverForm['pPassword'].focus();
                dialog.showSimpleOk('인증번호', '인증번호 인증에 성공하였습니다.');
                break;
            case 'failure':
                dialog.showSimpleOk('인증번호', '인증번호 인증에 실패하였습니다. 다시 시도해 주세요');
                $recoverForm['pEmailCode'].focus();
                break;
            default:
                break;
        }
    };
    xhr.open('PATCH', '/user/recover-password-email');
    xhr.send(formData);
})

$recoverForm.onsubmit = (e) => {
    e.preventDefault();
    if ($recoverForm['type'].value === 'email') {
        alert('이메일 찾기');
    } else if ($recoverForm['type'].value === 'password') {
        if (!$recoverForm['pEmailCodeSendButton'].hasAttribute('disabled') ||
            !$recoverForm['pEmailCodeVerifyButton'].hasAttribute('disabled')) {
            dialog.showSimpleOk('비밀번호 재설정', '이메일 인증을 완료해주세요.');
            return;
        }
        const $passwordLabel = $recoverForm.querySelector('label:has(input[name="pPassword"])');
        $passwordLabel.setValid(true);
        if ($recoverForm['pPassword'].value === '') {
            $passwordLabel.setValid(false, '비밀번호를 입력해 주세요.');
            $recoverForm['pPassword'].focus();
            return;
        }
        if (!passwordRegex.test($recoverForm['pPassword'].value)) {
            $passwordLabel.setValid(false, '올바른 비밀번호를 입력해 주세요.');
            $recoverForm['pPassword'].focus();
            return;
        }
        if ($recoverForm['pPasswordCheck'].value === '') {
            $passwordLabel.setValid(false, '비밀번호를 한번 더 입력해 주세요.');
            $recoverForm['pPasswordCheck'].focus();
            return;
        }
        if ($recoverForm['pPassword'].value !== $recoverForm['pPasswordCheck'].value) {
            $passwordLabel.setValid(false, '비밀번호를 일치하지 않습니다.');
            $recoverForm['pPasswordCheck'].focus();
            return;
        }
        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('email', $recoverForm['pEmail'].value)
        formData.append('code', $recoverForm['pEmailCode'].value)
        formData.append('salt', $recoverForm['pEmailSalt'].value)
        formData.append('password', $recoverForm['pPassword'].value)
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            if (xhr.status < 200 || xhr.status >= 300) {
                dialog.showSimpleOk('비밀번호 재설정', '요청을 처리하는 도중 오류가 발생하였습니다. 잠시 . 다시 시도해 주세요.');
                return;
            }
            const response = JSON.parse(xhr.responseText);
            switch (response.result) {
                case 'success':
                    dialog.showSimpleOk('비밀번호', '비밀번호를 재설정하였습니다. 확인 버튼을 클릭하면 로그인 페이지로 이동합니다.', {
                        onOkCallback: () => location.href = "/user/login",
                    });
                    break;
                case'failure':
                    dialog.showSimpleOk('비밀번호', '재설정 실패');
                    break;
                default:
                    dialog.showSimpleOk('비밀번호 재설정', '알 . 없는 이유로 비밀번호를 재설정하지 못하였습니다. 잠시 . 다시 시도해 주세요.');
                    break;
            }
        };
        xhr.open('POST', '/user/recover-password');
        xhr.send(formData);
    }
}