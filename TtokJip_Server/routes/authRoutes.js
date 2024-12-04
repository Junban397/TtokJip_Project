const express = require('express');
const router = express.Router();
const { authenticateToken } = require('../middleware/authMiddleware');
const { login, getUserInfo, changePw } = require('../api/authApi');

// 로그인 API
router.post('/login', login);

// 로그인 API
router.get('/getUserInfo', authenticateToken, getUserInfo);

router.put('/changePw', authenticateToken, changePw)

module.exports = router;