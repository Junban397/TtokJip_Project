const jwt = require('jsonwebtoken');
const { client } = require('../connectionDb');

const login = async (req, res) => {
    const { userId, pw } = req.body;

    console.log('로그인 요청:', { userId, pw });  // 요청 정보 로그 추가

    try {
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
    }
};

const getUserInfo = async (req, res) => {
    const { userId } = req.user;

    try {
        const database = client.db('ttokjip');
        const collection = database.collection('users');

        const userInfo = await collection.find({ userId: userId }).toArray();
        console.log('Info 전달됨:', userInfo); // 로그 추가
        if (userInfo.length > 0) {
            res.status(200).json(userInfo);
        } else {
            res.status(404).json({ message: '해당 사용자가 없습니다.' });
        }
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류' });
    }
};

const changePw = async (req, res) => {
    const { userId } = req.user;
    const { newPw } = req.body;

    try {
        const database = client.db('ttokjip');
        const collection = database.collection('users');

        // 데이터베이스에서 userId를 기준으로 비밀번호 업데이트
        const updateResult = await collection.updateOne(
            { userId: userId }, // 조건: userId 일치
            { $set: { pw: newPw } } // 업데이트할 필드
        );

        // 업데이트 결과 확인
        if (updateResult.matchedCount > 0) {
            res.status(200).json({ message: '비밀번호가 성공적으로 변경되었습니다.' });
        } else {
            res.status(404).json({ message: '해당 사용자가 없습니다.' });
        }
    } catch (error) {
        console.error('비밀번호 변경 오류:', error);
        res.status(500).json({ message: '서버 오류' });
    }
};

module.exports = { login, getUserInfo, changePw };