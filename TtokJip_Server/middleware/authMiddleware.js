const jwt = require('jsonwebtoken');

const authenticateToken = (req, res, next) => {
    const token = req.headers['authorization']?.split(' ')[1]; // "Bearer <token>" 형식에서 토큰을 추출
    console.log('토큰이 전달됨:', token); // 로그 추가

    if (!token) {
        return res.status(401).json({ message: '토큰이 없습니다.' });
    }

    jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
        if (err) {
            return res.status(403).json({ message: '토큰이 유효하지 않습니다.' });
        }
        req.user = user;  // 유효한 토큰일 경우 사용자 정보를 req.user에 저장
        next();  // 다음 미들웨어로 진행
    });
};

module.exports = { authenticateToken };