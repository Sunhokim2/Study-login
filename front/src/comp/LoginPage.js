import React, { useState, useRef, useEffect } from 'react'; // useRef와 useEffect 추가
import { useNavigate } from 'react-router-dom';
import RegisterPage from './RegisterPage';
import styles from '../App.module.css';

function LoginPage() {
  const [loginData, setLoginData] = useState({
    loginId: '',
    password: '',
  });
  const [isLoggingIn, setIsLoggingIn] = useState(false);
  const [loginError, setLoginError] = useState('');
  const [isRegisterModalOpen, setIsRegisterModalOpen] = useState(false);
  const navigate = useNavigate();
  const modalOverlayRef = useRef(null); // 모달 오버레이 ref 생성

  const { loginId, password } = loginData;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setLoginData({
      ...loginData,
      [name]: value,
    });
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setIsLoggingIn(true);
    setLoginError('');

    const storedData = localStorage.getItem('userData');
    if (storedData) {
      const userData = JSON.parse(storedData);
      if (userData.email === loginId && userData.password === password) {
        console.log('로그인 성공!');
        setIsLoggingIn(false);
        navigate('/Basicpage');
        return;
      }
    }

    setLoginError('아이디 또는 비밀번호가 틀렸습니다.');
    setIsLoggingIn(false);
  };

  const openRegisterModal = () => {
    setIsRegisterModalOpen(true);
  };

  const closeRegisterModal = () => {
    setIsRegisterModalOpen(false);
  };

  // 모달 오버레이 클릭 시 모달 닫기
  const handleOverlayClick = (e) => {
    if (modalOverlayRef.current === e.target) {
      closeRegisterModal();
    }
  };

  return (
    <div className={styles.authPage}>
      <h1>로그인</h1>
      <form onSubmit={handleLoginSubmit} className={styles.authForm}>
        <div className={styles.formGroup}>
          <label for="loginId">이메일</label>
          <input
            type="text"
            id="loginId"
            name="loginId"
            placeholder="이메일을 입력하세요"
            value={loginId}
            onChange={handleChange}
          />
        </div>
        <div className={styles.formGroup}>
          <label for="password">비밀번호</label>
          <input
            type="password"
            id="password"
            name="password"
            placeholder="비밀번호를 입력하세요"
            value={password}
            onChange={handleChange}
          />
        </div>
        {loginError && <p className={styles.errorMessage}>{loginError}</p>}
        <button type="submit" disabled={isLoggingIn} className={styles.submitButton}>
          {isLoggingIn ? '로그인 중...' : '로그인'}
        </button>
        <p className={styles.signupLink}>
          아직 계정이 없으신가요?
          <button type="button" onClick={openRegisterModal} className={styles.linkButton}>
            회원가입
          </button>
        </p>
      </form>

      {isRegisterModalOpen && (
        <div
          className={styles.modalOverlay}
          ref={modalOverlayRef} // ref 연결
          onClick={handleOverlayClick} // 클릭 이벤트 리스너 추가
        >
          <RegisterPage onClose={closeRegisterModal} />
        </div>
      )}
    </div>
  );
}

export default LoginPage;