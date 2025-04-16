import React, { useState } from 'react';
import styles from '../App.module.css';

function RegisterPage({ onClose }) {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    passwordConfirm: '',
    // 인증은 이제 이메일확인버튼으로 대체됩니다.
    // verificationCode: '', // 인증 코드 필드 추가
  });
  const [emailError, setEmailError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [passwordConfirmError, setPasswordConfirmError] = useState('');
  const [isSendingEmail, setIsSendingEmail] = useState(false);
  // const [verificationCodeError, setVerificationCodeError] = useState('');
  // const [isSendingCode, setIsSendingCode] = useState(false);
  // const [isVerifyingCode, setIsVerifyingCode] = useState(false);
  const [isSigningUp, setIsSigningUp] = useState(false);
  const [signupError, setSignupError] = useState('');
  const [signupSuccess, setSignupSuccess] = useState('');
  // const [verificationSuccess, setVerificationSuccess] = useState(false); // 인증 성공 여부 상태

  const { email, password, passwordConfirm } = formData;

  const validatePassword = (pw) => {
    const hasUpperCase = /[A-Z]/.test(pw);
    const hasNumber = /[0-9]/.test(pw);
    const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pw);
    return pw.length >= 8 && hasUpperCase && hasNumber && hasSpecialChar;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleSendVerificationEmail = async () => {
    setIsSendingEmail(true);
    setEmailError('');
    setSignupSuccess(''); // 인증 이메일 요청 성공 메시지 초기화
    try {
      const response = await fetch('http://localhost:8080/api/send-verification-code', { // API 엔드포인트 수정
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email }),
      });
      const data = await response.json();
      if (response.ok) {
        setSignupSuccess(data.message + ' 이메일함을 확인하여 인증을 완료해주세요.'); // 성공 메시지 업데이트
      } else {
        setEmailError(data.message || '인증 이메일 발송에 실패했습니다.');
      }
    } catch (error) {
      console.error('인증 이메일 요청 중 에러 발생:', error);
      setEmailError('서버와 통신 중 오류가 발생했습니다.');
    } finally {
      setIsSendingEmail(false);
    }
  };

  // const handleVerifyCode = async () => {
  //   setIsVerifyingCode(true);
  //   setVerificationCodeError('');
  //   setSignupSuccess(''); // 인증 성공 메시지 초기화
  //   try {
  //     const response = await fetch('http://localhost:8080/api/verify-code', {
  //       method: 'POST',
  //       headers: {
  //         'Content-Type': 'application/json',
  //       },
  //       body: JSON.stringify({ email, code: verificationCode }),
  //     });
  //     const data = await response.json();
  //     if (response.ok) {
  //       setVerificationSuccess(true); // 인증 성공 상태 업데이트
  //       setSignupSuccess(data.message); // 인증 성공 메시지 표시
  //     } else {
  //       setVerificationCodeError(data.message || '인증에 실패했습니다.');
  //       setVerificationSuccess(false); // 인증 실패 시 상태 업데이트
  //     }
  //   } catch (error) {
  //     console.error('인증 코드 확인 중 에러 발생:', error);
  //     setVerificationCodeError('서버와 통신 중 오류가 발생했습니다.');
  //     setVerificationSuccess(false);
  //   } finally {
  //     setIsVerifyingCode(false);
  //   }
  // };

  const handleSignupSubmit = async (e) => {
    e.preventDefault();
    setIsSigningUp(true);
    setSignupError('');

    let isValid = true;
    if (!password) {
      setPasswordError('비밀번호를 입력해주세요.');
      isValid = false;
    }
    if (!validatePassword(password)) {
      setPasswordError('비밀번호는 최소 8자 이상, 대문자, 숫자, 특수문자를 포함해야 합니다.');
      isValid = false;
    }
    if (password !== passwordConfirm) {
      setPasswordConfirmError('비밀번호가 일치하지 않습니다.');
      isValid = false;
    }

    if (isValid) {
      try {
        const response = await fetch('http://localhost:8080/api/register', { // API 엔드포인트 수정
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ email, password }),
        });

        const data = await response.json();
        if (response.ok) {
          console.log('회원가입 성공:', data);
          setSignupSuccess(data.message);
          setTimeout(() => {
            setIsSigningUp(false);
            onClose();
          }, 1500);
        } else {
          console.error('회원가입 실패:', data);
          setSignupError(data.message || '회원가입에 실패했습니다.');
        }
      } catch (error) {
        console.error('회원가입 요청 중 에러 발생:', error);
        setSignupError('서버와 통신 중 오류가 발생했습니다.');
      } finally {
        setIsSigningUp(false);
      }
      return;
    }

    setSignupError('회원가입에 실패했습니다. 입력 내용을 확인해주세요.');
    setIsSigningUp(false);
  };

  return (
      <div className={`${styles.authPage} ${styles.modal}`}>
        <h2>회원가입</h2>
        <button type="button" onClick={onClose} className={styles.closeButton}>
          X
        </button>
        <form onSubmit={handleSignupSubmit} className={styles.authForm}>
          <div className={styles.formGroup}>
            <label htmlFor="email">이메일</label>
            <input
                type="email"
                id="email"
                name="email"
                placeholder="이메일 주소를 입력하세요"
                value={email}
                onChange={handleChange}
                readOnly={signupSuccess.includes('인증을 완료해주세요')} // 인증 이메일 발송 후 수정 불가
            />
            {emailError && <p className={styles.errorMessage}>{emailError}</p>}
            {!signupSuccess.includes('인증을 완료해주세요') && (
                <button type="button" onClick={handleSendVerificationEmail} disabled={isSendingEmail}>
                  {isSendingEmail ? '인증 이메일 전송 중...' : '인증 이메일 받기'}
                </button>
            )}
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="password">비밀번호</label>
            <input
                type="password"
                id="password"
                name="password"
                placeholder="비밀번호를 입력하세요"
                value={password}
                onChange={handleChange}
            />
            {passwordError && <p className={styles.errorMessage}>{passwordError}</p>}
          </div>
          <div className={styles.formGroup}>
            <label htmlFor="passwordConfirm">비밀번호 확인</label>
            <input
                type="password"
                id="passwordConfirm"
                name="passwordConfirm"
                placeholder="비밀번호를 다시 입력하세요"
                value={passwordConfirm}
                onChange={handleChange}
            />
            {passwordConfirmError && <p className={styles.errorMessage}>{passwordConfirmError}</p>}
          </div>

          {signupError && <p className={styles.errorMessage}>{signupError}</p>}
          {signupSuccess && <p className={styles.successMessage}>{signupSuccess}</p>}

          <button type="submit" disabled={isSigningUp || !signupSuccess.includes('인증을 완료해주세요')} className={styles.submitButton}>
            {isSigningUp ? '회원가입 중...' : '회원가입'}
          </button>
        </form>
      </div>
  );
}

export default RegisterPage;