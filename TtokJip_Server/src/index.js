require('dotenv').config(); // 환경 변수 로드
const express = require('express');
const bodyParser = require('body-parser');
const { MongoClient } = require('mongodb');
const jwt = require('jsonwebtoken');  // JWT 패키지 추가

const app = express();
const port = 5000;

app.use(bodyParser.json()); // JSON 요청 본문을 파싱하기 위한 미들웨어

const uri = process.env.MONGODB_URI;
const client = new MongoClient(uri);

// JWT 토큰 검증 미들웨어
const authenticateToken = (req, res, next) => {
    const token = req.headers['authorization']?.split(' ')[1]; // "Bearer <token>" 형식에서 토큰을 분리

    if (!token) {
        return res.status(401).json({ message: '토큰이 없습니다.' });
    }

    jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
        if (err) {
            return res.status(403).json({ message: '토큰이 유효하지 않습니다.' });
        }
        req.user = user;  // 토큰에서 user 정보를 요청 객체에 추가
        next();
    });
};

// 로그인 API
app.post('/login', async (req, res) => {
    const { userId, pw } = req.body;

    try {
        await client.connect();
        const database = client.db('ttokjip');
        const collection = database.collection('users');

        const user = await collection.findOne({ userId: userId });

        if (!user) {
            return res.status(401).json({ message: '아이디 또는 비밀번호가 잘못되었습니다.' });
        }

        if (user.pw !== pw) {
            return res.status(401).json({ message: '아이디 또는 비밀번호가 잘못되었습니다.' });
        }

        const token = jwt.sign({ userId: userId, houseId: user.houseId }, process.env.JWT_SECRET, { expiresIn: '1h' });

        res.status(200).json({ message: '로그인 성공!', token, houseId: user.houseId });
    } catch (error) {
        console.error("로그인 오류: ", error);  // 구체적인 오류를 로그로 출력
        res.status(500).json({ message: '서버 오류', error: error.message });
    } finally {
        await client.close();
    }
});

// 디바이스 정보를 가져오는 API
app.get('/devices', authenticateToken, async (req, res) => {
    const { houseId } = req.user;  // 요청자의 houseId를 사용
  
    try {
      await client.connect();
      const database = client.db('ttokjip');
      const collection = database.collection('devices');  // 'devices' 컬렉션
  
      // houseId와 일치하는 디바이스 정보를 가져옵니다.
      const devices = await collection.find({ houseId: houseId }).toArray();
  
      if (devices.length > 0) {
        res.status(200).json(devices); // 디바이스 정보를 반환
      } else {
        res.status(404).json({ message: '해당 사용자의 디바이스가 없습니다.' });
      }
    } catch (error) {
      console.error(error);
      res.status(500).json({ message: '서버 오류' });
    } finally {
      await client.close();
    }
  });

// 서버 시작
app.listen(port, () => {
    console.log(`서버가 http://localhost:${port}에서 실행 중입니다.`);
});