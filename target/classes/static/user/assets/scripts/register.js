const $loading = document.getElementById('loading');
const $registerForm = document.getElementById('registerForm');
const $step = $registerForm.querySelector(':scope>.step');
const $stepItems = Array.from($step.querySelectorAll(':scope>.item'));
const $termContainer = $registerForm.querySelector(':scope>.container.term');
const $infoContainer = $registerForm.querySelector(':scope>.container.info');
const $completeContainer = $registerForm.querySelector(':scope>.container.complete');
const $containers = [$termContainer, $infoContainer, $completeContainer];
const emailRegex = new RegExp('^(?=.{8,50}$)([\\da-z\\-_.]{4,})@([\\da-z][\\da-z\\-]*[\\da-z]\\.)?([\\da-z][\\da-z\\-]*[\\da-z])\\.([a-z]{2,15})(\\.[a-z]{2,3})?$');
const passwordRegex = new RegExp('^([\\da-zA-Z`~!@#$%^&*()\\-_=+\\[{\\]}\\\\|;:\'",<.>/?]{8,50})$');
const emailCodeRegex = new RegExp('^(\\d{6})$');
const nicknameRegex = new RegExp('^([\\da-zA-Z가-힣]{2,10})$');
const nameRegex = new RegExp('^([가-힣]{2,5})$')
const contactSecondRegex = new RegExp('^(\\d{3,4})$');
const contactThirdRegex = new RegExp('^(\\d{4})$');

let currentStep = 0;
$registerForm['emailCodeSendButton'].addEventListener('click', () => {
    const $emailLabel = $registerForm.querySelector('.--object-label:has(input[name="email"])');
    if ($registerForm['email'].value === '') {
        $emailLabel.setValid(false, '이메일을 입력해 주세요.');
        $registerForm['email'].focus();
        return;
    }
    if (!emailRegex.test($registerForm['email'].value)) {
        $emailLabel.setValid(false, '올바른 이메일을 입력해주세요.');
        $registerForm['email'].focus();
        $registerForm['email'].select();
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append("email", $registerForm["email"].value);
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
            case 'success':
                $registerForm['emailSalt'].value = response.salt;
                $registerForm['email'].setDisabled(true);
                $registerForm['emailCodeSendButton'].setDisabled(true);
                $registerForm['emailCode'].setDisabled(false);
                $registerForm['emailCodeVerifyButton'].setDisabled(false);
                $registerForm['emailCode'].focus();
                dialog.showSimpleOk('인증번호 전송', '입력하신 이메일로 인증버호를 전송하였습니다. 해당 인증번호는 10분간만 유효하니 유의해 주세요.');
                break;
            case 'failure_duplicate':
                dialog.showSimpleOk('인증번호 전송', `입력하시 이메일 '${$registerForm['email'].value}' 은/는 이미 사용중입니다.`,
                    {
                        onOkCallback: () => {
                            $registerForm['email'].focus();
                            $registerForm['email'].select();
                        }
                    });
                break;
            default:
                break;
        }

    };
    xhr.open('POST', "/user/register-email");
    xhr.send(formData);

    $loading.show();
})
$registerForm['emailCodeVerifyButton'].addEventListener('click', () => {
    const $emailLabel = $registerForm.querySelector('.--object-label:has(input[name="emailCode"])');
    if ($registerForm['emailCode'].value == '') {
        $emailLabel.setValid(false, '인증번호를 입력해주세요.');
        $registerForm['emailCode'].focus();
        return;
    }
    if (!emailCodeRegex.test($registerForm['emailCode'].value)) {
        $emailLabel.setValid(false, '올바른 인증번호를 입력해 주세요.');
        $registerForm['emailCode'].focus();
        $registerForm['emailCode'].select();
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', $registerForm['email'].value);
    formData.append('code', $registerForm['emailCode'].value);
    formData.append('salt', $registerForm['emailSalt'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('인증번호 확인', '요청을 처리하는 도중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.')
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'failure_expired':
                $registerForm['emailSalt'].value = '';
                $registerForm['email'].setDisabled(false);
                $registerForm['emailCodeSendButton'].setDisabled(false);
                $registerForm['emailCode'].value = '';
                $registerForm['emailCode'].setDisabled(true);
                $registerForm['emailCodeVerifyButton'].setDisabled(true);
                $registerForm['email'].focus();
                dialog.showSimpleOk('인증번호 확인', '인증저보가 만료되었습니다. 이메일 인증을 다시 진행해 주세요');
                break;
            case 'success':
                $registerForm['emailCode'].setDisabled(true);
                $registerForm['emailCodeVerifyButton'].setDisabled(true);
                $registerForm['password'].setDisabled(false);
                $registerForm['passwordCheck'].setDisabled(false);
                $registerForm['password'].focus();
                dialog.showSimpleOk('인증번호 확인', '이메일이 정상적으로 인증되었습니다.');
                break;
            default:
                dialog.showSimpleOk('인증번호 확인', '인증버호가 올바르지 않습니다. 자시 확인해 주세요.', () => {
                    $registerForm['emailCode'].focus();
                    $registerForm['emailCode'].select();
                });
                break;
        }
    };
    xhr.open('PATCH', '/user/register-email');
    xhr.send(formData);
})
$registerForm['nicknameCheckButton'].addEventListener('click', () => {
    const $nicknameLabel = $registerForm.querySelector('.--object-label:has(input[name="nickname"])');
    if ($registerForm['nickname'].value === '') {
        $nicknameLabel.setValid(false, '닉네임을 입력해주세요.');
        $registerForm['nickname'].focus();
        return;
    }
    if (!nicknameRegex.test($registerForm['nickname'].value)) {
        $nicknameLabel.setValid(false, '올바른 닉네임을 입력해주세요.');
        $registerForm['nickname'].focus();
        $registerForm['nickname'].select();
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('nickname', $registerForm['nickname'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('닉네임 중복 확인', '요청을 처리하는 도중 오류가 발생하였습니다. 잠시후 다시 시도해 주세요.');
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'success':
                dialog.show({
                    title: '닉네임 중복 확인',
                    content: `압력하신 닉네임 '${$registerForm['nickname'].value}' 은/는 사용가능합니다. 이 닉네임을 사용할까요?`,
                    buttons: [
                        {
                            caption: '아니요',
                            onclick: ($modal) => {
                                dialog.hide($modal)
                            }
                        },
                        {
                            caption: '네',
                            color: 'green',
                            onclick: ($modal) => {
                                dialog.hide($modal);
                                $registerForm['nickname'].setDisabled(true);
                                $registerForm['nicknameCheckButton'].setDisabled(true);
                            }
                        }
                    ]
                });
                break;
            case 'failure_duplicate':
                dialog.showSimpleOk('닉네임 중복 확인', `입력하신 닉네임 '${$registerForm['nickname'].value}'는 이미 사용중입니다.`, () => {
                    $registerForm['nickname'].focus();
                    $registerForm['nickname'].select();
                });
                break;
            default:
                dialog.showSimpleOk('닉네임 중복 확인', '알 수 없는 이유로 닉네임 중복을 확인하지 못하였습니다. 잠시후 다시 시도해 주세요.');
        }
    };
    xhr.open('POST', '/user/nickname-check');
    xhr.send(formData);
})

$registerForm['addressFindButton'].addEventListener('click', () => {
    const $addressFindDialog = document.getElementById('addressFindDialog');
    const $modal = $addressFindDialog.querySelector(':scope>.modal');
    $addressFindDialog.onclick = () => {
        $addressFindDialog.hide();
    }
    new daum.Postcode({
        width: '100%',
        height: '100%',
        oncomplete: (data) => {
            console.log(data);
            $addressFindDialog.hide();
            $registerForm['addressPostal'].value = data['zonecode'];
            $registerForm['addressPrimary'].value = data['roadAddress'];
            $registerForm['addressSecondary'].focus();
            $registerForm['addressSecondary'].select();
        }
    }).embed($modal);//주소찾기 위함
    $addressFindDialog.show();
})

$registerForm['previous'].onclick = () => {
    currentStep -= 2;
    $registerForm.dispatchEvent(new Event('submit')); // submit을 강제로 발생시킨다.
}
$registerForm.onsubmit = (e) => {
    e.preventDefault();
    if (currentStep === 0) {
        if (!$registerForm['agreesServiceTerm'].checked) {
            dialog.showSimpleOk('회원가입', '서비스 이용약관에 동의해 주세요.');
            return;
        }
        if (!$registerForm['agreePrivacyTerm'].checked) {
            dialog.showSimpleOk('회원가입', '개인정보 처리방침에 동의해 주세요.');
            return;
        }
        currentStep++;//1 -> 약관에서 개인으로 넘어갔다
    } else if (currentStep === 1) {
        const $emailLabel = $registerForm.querySelector('.--object-label:has(input[name="email"])');
        const $passwordLabel = $registerForm.querySelector('.--object-label:has(input[name="password"])');
        const $nicknameLabel = $registerForm.querySelector('.--object-label:has(input[name="nickname"])');
        const $nameLabel = $registerForm.querySelector('.--object-label:has(input[name="name"])');
        const $contactLabel = $registerForm.querySelector('.--object-label:has(input[name="contactSecond"])');
        const $addressLabel = $registerForm.querySelector('.--object-label:has(input[name="addressPostal"])');
        const $labels = [$emailLabel, $passwordLabel, $nicknameLabel, $nameLabel, $contactLabel, $addressLabel]
        $labels.forEach(($label) => $label.setValid(true));
        if (!$registerForm['emailCodeSendButton'].hasAttribute('disabled') ||
            !$registerForm['emailCodeVerifyButton'].hasAttribute('disabled')) {
            $emailLabel.setValid(false, '이메일 인증을 완료해 주세요..');
            return;
        }
        if ($registerForm['password'].value === '') {
            $passwordLabel.setValid(false, '비밀번호를 입력해 주세요.');
            $registerForm['password'].focus();
            return;
        }
        if (!passwordRegex.test($registerForm['password'].value)) {
            $passwordLabel.setValid(false, '올바른 비밀번호를 입력해주세요.');
            $registerForm['password'].focus();
            $registerForm['password'].select();
            return;
        }
        if ($registerForm['passwordCheck'].value === '') {
            $passwordLabel.setValid(false, '비밀번호를 한 번 더 입력해주세요.');
            $registerForm['passwordCheck'].focus();
            return;
        }
        if ($registerForm['password'].value!==$registerForm['passwordCheck'].value) {
            $passwordLabel.setValid(false, '비밀번호가 일치하지 않습니다.');
            $registerForm['passwordCheck'].focus();
            $registerForm['passwordCheck'].select();
            return;
        }
        if (!$registerForm['nicknameCheckButton'].hasAttribute('disabled')) {
            $nicknameLabel.setValid(false, '닉네임 중복 확인을 해주세요.');
            $registerForm['nickname'].focus();
            return;
        }
        if ($registerForm['name'].value === '') {
            $nameLabel.setValid(false, '이름을 입력해 주세요.');
            $registerForm['name'].focus();
            return;
        }
        if (!nameRegex.test($registerForm['name'].value)) {
            $nameLabel.setValid(false, '올바른 이름을 입력해 주세요.');
            $registerForm['name'].focus();
            return;
        }
        if ($registerForm['birth'].value === '') {
            $nameLabel.setValid(false, '생일을 입력해 주세요.');
            $registerForm['birth'].focus();
            return;
        }
        if ($registerForm['gender'].value === '') {
            $nameLabel.setValid(false, '성별을 선택해 주세요.');
            return;
        }
        if ($registerForm['contactMvno'].value === '-1') {
            $contactLabel.setValid(false, '통신사를 선택해 주세요.');
            $registerForm['contactMvno'].focus();
            return;
        }
        if ($registerForm['contactSecond'].value === '') {
            $contactLabel.setValid(false, '연락처를 입력해 주세요.');
            $registerForm['contactSecond'].focus();
            return;
        }
        if (!contactSecondRegex.test($registerForm['contactSecond'].value)) {
            $contactLabel.setValid(false, '올바른 연락처를 입력해 주세요.');
            $registerForm['contactSecond'].focus();
            $registerForm['contactSecond'].select();
            return;
        }

        if ($registerForm['contactThird'].value === '') {
            $contactLabel.setValid(false, '연락처를 입력해 주세요.')
            $registerForm['contactThird'].focus();
            return;
        }
        if (!contactThirdRegex.test($registerForm['contactThird'].value)) {
            $contactLabel.setValid(false, '올바른 연락처를 입력해 주세요.');
            $registerForm['contactThird'].focus();
            $registerForm['contactThird'].select();
            return;
        }
        if ($registerForm['addressPostal'].value === '') {
            $addressLabel.setValid(false, '주소를 입력해 주세요.');
            return;
        }
        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('email', $registerForm['email'].value);
        formData.append('code', $registerForm['emailCode'].value);
        formData.append('salt', $registerForm['emailSalt'].value)
        formData.append('password', $registerForm['password'].value)
        formData.append('nickname', $registerForm['nickname'].value)
        formData.append('name', $registerForm['name'].value)
        formData.append('birth', $registerForm['birth'].value)
        formData.append('gender', $registerForm['gender'].value)
        formData.append('contactMvnoCode', $registerForm['contactMvno'].value)
        formData.append('contactFirst', $registerForm['contactFirst'].value);
        formData.append('contactSecond', $registerForm['contactSecond'].value)
        formData.append('contactThird', $registerForm['contactThird'].value)
        formData.append('addressPostal', $registerForm['addressPostal'].value)
        formData.append('addressPrimary', $registerForm['addressPrimary'].value)
        formData.append('addressSecondary', $registerForm['addressSecondary'].value)
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            $loading.hide();
            if (xhr.status < 200 || xhr.status >= 300) {
                dialog.showSimpleOk('회원가입', '요청을 처리하는 도중 오류가 발생하였습니다. 잠시후 다시 시도해 주세요.')
                return;
            }
            const response = JSON.parse(xhr.responseText);
            switch (response.result) {
                case 'failure_duplicate_email':
                    dialog.showSimpleOk('회원가입',`입력하신 이메일 '${$registerForm['email'].value}'은/는 이미 사용중입니다.`);
                    break;
                case 'failure_duplicate_nickname':
                    dialog.showSimpleOk('회원가입',`입력하신 이메일 '${$registerForm['nickname'].value}'은/는 이미 사용중입니다.`);
                    break;
                case 'failure_duplicate_contact':
                    dialog.showSimpleOk('회원가입',`입력하신 이메일 '${$registerForm['contactFirst'].value}-${$registerForm['contactSecond'].value}-${$registerForm['contactThird'].value}'은/는 이미 사용중입니다.`);
                    break;
                case 'success':
                    currentStep++;
                    $registerForm.dispatchEvent(new Event('submit'));
                    break;
                default:
                    dialog.showSimpleOk('회원가입', '알수 없는 이유로 회원가입을 하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
                    break;
            }
        };
        xhr.open('POST', '/user/register');
        xhr.send(formData);
        $loading.show();
        return;
    }
    $registerForm['previous'].setAttribute('disabled', '');
    switch (currentStep) {
        case 0:
            break;
        case 1:
            $registerForm['previous'].removeAttribute('disabled');
            break;
        case 2:
            $registerForm.querySelector('a[href="/user/login"]').style.display = 'none';
            $registerForm['submit'].querySelector(':scope>.---caption').innerText = '로그인';
            $registerForm['submit'].onclick = () => location.href = '/user/login';
            break;
        default:
            location.href = '/user/login';
            return;

    }

    $stepItems.forEach(($item) => $item.classList.remove('-selected'));
    $stepItems[currentStep].classList.add('-selected');
    $containers.forEach(($container) => $container.classList.remove('-visible'));
    $containers[currentStep].classList.add('-visible');
}
