import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LoginPage from './comp/LoginPage';
import RegisterPage from './comp/RegisterPage';
import BasicPage from './comp/BasicPage.js'; // 로그인 후 보여질 기본 페이지
import styles from './App.module.css';

function App() {

  return (
    <BrowserRouter>
      <div className={styles.appContainer}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/regist" element={<RegisterPage />} />
          <Route path="/Basicpage" element={<BasicPage />} />
          <Route path="/" element={<LoginPage />} /> {/* 기본 경로를 로그인 페이지로 설정 (선택 사항) */}
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;