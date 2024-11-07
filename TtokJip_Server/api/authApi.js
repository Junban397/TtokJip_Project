const { MongoClient } = require('mongodb');
const jwt = require('jsonwebtoken');
const { MONGODB_URI } = process.env;

const client = new MongoClient(MONGODB_URI);

const login = async (req, res) => {
    const { userId, pw } = req.body;

    console.log('로그인 요청:', { userId, pw });  // 요청 정보 로그 추가

    try {
        await client.connect();
        const database = client.db('ttokjip');
        const collection = database.collection('users');

        const user = await collection.findOne({ userId: userId });

        if (!user) {
            console.log('사용자 없음:', userId);  // 사용자 없음 로그 추가
            return res.status(401).json({ message: '아이디 또는 비밀번호가 잘못되었습니다.' });
        }

        console.log('사용자 정보:', user);  // 사용자 정보 로그 추가

        if (user.pw !== pw) {
            console.log('비밀번호 불일치:', userId);  // 비밀번호 불일치 로그 추가
            return res.status(401).json({ message: '아이디 또는 비밀번호가 잘못되었습니다.' });
        }

        const token = jwt.sign({ userId: userId, houseId: user.houseId }, process.env.JWT_SECRET, { expiresIn: '1h' });

        console.log('토큰 발급:', token);  // 발급된 토큰 로그 추가

        res.status(200).json({ message: '로그인 성공!', token, houseId: user.houseId });
    } catch (error) {
        console.error("로그인 오류: ", error);  // 오류 발생 시 로그 추가
        res.status(500).json({ message: '서버 오류', error: error.message });
    } finally {
        await client.close();
    }
};

module.exports = { login };