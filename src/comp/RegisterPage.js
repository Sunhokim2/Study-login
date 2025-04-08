import React, { useState } from 'react';
import styles from '../App.module.css';

function RegisterPage({ onClose }) {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    passwordConfirm: '',
  });
  const [emailError, setEmailError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [passwordConfirmError, setPasswordConfirmError] = useState('');
  const [isSigningUp, setIsSigningUp] = useState(false);
  const [signupError, setSignupError] = useState('');
  const [signupSuccess, setSignupSuccess] = useState('');

  const { email, password, passwordConfirm } = formData;

//   패스워드검증 식은 역시gpt
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

  const handleSignupSubmit = async (e) => {
    // 연쇄발동 방지
    e.preventDefault();
    setIsSigningUp(true);
    setSignupError('');
    setSignupSuccess('');
    setEmailError('');
    setPasswordError('');
    setPasswordConfirmError('');

    if (!email) setEmailError('이메일을 입력해주세요.');
    if (!validatePassword(password)) setPasswordError('비밀번호는 최소 8자 이상, 대문자, 숫자, 특수문자를 포함해야 합니다.');
    if (password !== passwordConfirm) setPasswordConfirmError('비밀번호가 일치하지 않습니다.');

    if (email && validatePassword(password) && password === passwordConfirm) {
      const { passwordConfirm, ...signupData } = formData; // passwordConfirm 제외하고 저장
      localStorage.setItem('userData', JSON.stringify(signupData));
      console.log('회원가입 성공:', signupData);
      setSignupSuccess('회원가입이 완료되었습니다!');
      setTimeout(() => {
        setIsSigningUp(false);
        onClose();
      }, 1500);
      return;
    }

    setSignupError('회원가입에 실패했습니다. 입력 내용을 확인해주세요.');
    setIsSigningUp(false);
  };

  return (
    <div className={`${styles.authPage} ${styles.modal}`}> {/* 모덜 스타일 적용 */}
      <h2>회원가입</h2> {/* 제목 변경 */}
      <button type="button" onClick={onClose} className={styles.closeButton}>
        X
      </button>
      <form onSubmit={handleSignupSubmit} className={styles.authForm}>
        {/* --- 기존 회원가입 폼 내용 --- */}
        <div className={styles.formGroup}>
          <label htmlFor="email">이메일</label>
          <input
            type="email"
            id="email"
            name="email"
            placeholder="이메일 주소를 입력하세요"
            value={email}
            onChange={handleChange}
          />
          {emailError && <p className={styles.errorMessage}>{emailError}</p>}
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
        <button type="submit" disabled={isSigningUp} className={styles.submitButton}>
          {isSigningUp ? '회원가입 중...' : '회원가입'}
        </button>
      </form>
    </div>
  );
}

export default RegisterPage;