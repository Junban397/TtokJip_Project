const jwt = require('jsonwebtoken');

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

module.exports = { authenticateToken };