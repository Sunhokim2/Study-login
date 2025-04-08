import React from 'react';
import styles from '../App.module.css';

function BasicPage() {
  return (
    <div className={styles.landingPage}>
      <h1>로그인 성공!</h1>
      <p>환영합니다!</p>
      {/* 여기에 로그인 후 보여질 내용을 추가하세요. */}
    </div>
  );
}

export default BasicPage;