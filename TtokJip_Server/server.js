require('dotenv').config(); // 환경 변수 로드
const express = require('express');
const bodyParser = require('body-parser');
const { connectDB } = require('./connectionDb');
const authRoutes = require('./routes/authRoutes');
const deviceRoutes = require('./routes/deviceRoutes');
const statisticsRoutes = require('./routes/statisticsRoutes');

const app = express();
const port = process.env.PORT || 8080;

app.use(bodyParser.json()); // JSON 요청 본문을 파싱하기 위한 미들웨어

// MongoDB 연결
connectDB();

// 라우트 설정
app.use('/auth', authRoutes);  // 인증 관련 라우트
app.use('/devices', deviceRoutes);  // 디바이스 관련 라우트
app.use('/log', statisticsRoutes); // 로그 관련 라우트

// 서버 시작
app.listen(port, () => {
    console.log(`서버가 ${port}에서 실행 중입니다.`);
});