import React, { useState, useRef, useEffect } from 'react'; // useRef와 useEffect 추가
import { useNavigate } from 'react-router-dom';
import RegisterPage from './RegisterPage';
import styles from '../App.module.css';

function LoginPage() {
  const [loginData, setLoginData] = useState({
    email: '', // loginId를 email로 변경 (백엔드 필드명과 통일)
    password: '',
  });
  const [isLoggingIn, setIsLoggingIn] = useState(false);
  const [loginError, setLoginError] = useState('');
  const [isRegisterModalOpen, setIsRegisterModalOpen] = useState(false);
  const navigate = useNavigate();
  const modalOverlayRef = useRef(null); // 모달 오버레이 ref 생성

  const { email, password } = loginData; // loginId를 email로 변경

  const handleChange = (e) => {
    const { name, value } = e.target;
    setLoginData({
      ...loginData,
      [name]: value,
    });
  };


// ❗로컬스토리지로 로그인
//     const storedData = localStorage.getItem('userData');
//     if (storedData) {
//       const userData = JSON.parse(storedData);
//       if (userData.email === loginId && userData.password === password) {
//         console.log('로그인 성공!');
//         setIsLoggingIn(false);
//         navigate('/Basicpage');
//         return;
//       }
//     }

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setIsLoggingIn(true);
    setLoginError('');

    try {
      const response = await fetch('http://localhost:8080/api/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }), // loginId 대신 email 사용
      });

      if (response.ok) {
        const data = await response.text(); // 또는 response.json() 형태에 따라
        console.log('로그인 성공:', data);
        setIsLoggingIn(false);
        // TODO: 로그인 성공 후 처리 (예: 토큰 저장, 상태 업데이트)
        navigate('/Basicpage');
      } else if (response.status === 401) {
        const errorData = await response.text(); // 또는 response.json() 형태에 따라
        console.error('로그인 실패:', errorData);
        setLoginError(errorData || '아이디 또는 비밀번호가 틀렸습니다.');
      } else {
        console.error('로그인 실패 - 상태 코드:', response.status);
        setLoginError('로그인에 실패했습니다. 서버 오류를 확인해주세요.');
      }
    } catch (error) {
      console.error('로그인 요청 중 에러 발생:', error);
      setLoginError('서버와 통신 중 오류가 발생했습니다.');
    } finally {
      setIsLoggingIn(false);
    }
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
      <label htmlFor="email">이메일</label> {/* label for 속성 변경 */}
      <input
        type="text"
        id="email" /* id 속성 변경 */
        name="email" /* name 속성 변경 */
        placeholder="이메일을 입력하세요"
        value={email}
        onChange={handleChange}
      />
    </div>
    <div className={styles.formGroup}>
      <label htmlFor="password">비밀번호</label> {/* label for 속성 유지 */}
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