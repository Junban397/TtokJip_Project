require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const { MongoClient } = require('mongodb');

const app = express();
const port = 5000;

app.use(bodyParser.json()); // JSON 요청 본문을 파싱하기 위한 미들웨어

const uri = process.env.MONGODB_URI;
const client = new MongoClient(uri);

app.post('/login', async (req, res) => {
    const { userId, pw } = req.body;

    try {
        await client.connect();
        const database = client.db('ttokjip');
        const collection = database.collection('users');

        // 사용자 인증
        const user = await collection.findOne({ userId: userId, pw: pw });

        if (user) {
            res.status(200).json({ message: '로그인 성공!', userName: user.UserName });
        } else {
            res.status(401).json({ message: '아이디 또는 비밀번호가 잘못되었습니다.' });
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