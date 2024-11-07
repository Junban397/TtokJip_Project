const express = require('express');
const { authenticateToken } = require('../middleware/authMiddleware');
const { getDevices,updateDeviceStatus, updateDeviceFavorite } = require('../api/deviceApi');
const router = express.Router();

// 디바이스 정보를 가져오는 API
router.get('/', authenticateToken, getDevices);

// 디바이스 상태 변경 API
router.put('/updateStatus', authenticateToken, updateDeviceStatus);

// 디바이스 즐겨찾기 상태 변경 API
router.put('/updateFavorite', authenticateToken, updateDeviceFavorite);

module.exports = router;