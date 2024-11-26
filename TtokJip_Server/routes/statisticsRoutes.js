const express = require('express');
const router = express.Router();
const { authenticateToken } = require('../middleware/authMiddleware');
const { uploadSensorData,getStatistics } = require('../api/statisticsApi');

// 센서 데이터 업로드 API
router.post('/uploadData', authenticateToken, uploadSensorData);

router.get('/statistics', authenticateToken, getStatistics);

module.exports = router;