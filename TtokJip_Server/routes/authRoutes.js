const express = require('express');
const { login } = require('../api/authApi');
const router = express.Router();

// 로그인 API
router.post('/login', login);

module.exports = router;