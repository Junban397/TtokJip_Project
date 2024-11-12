const express = require('express');
const router = express.Router();
const { authenticateToken } = require('../middleware/authMiddleware');
const { getDevices, updateDeviceStatus, updateDeviceFavorite, modeSetting, modeSettingDeviceSwitch } = require('../api/deviceApi');

// 디바이스 정보를 가져오는 API
router.get('/', authenticateToken, getDevices);

// 디바이스 상태 변경 API
router.put('/updateStatus', authenticateToken, updateDeviceStatus);  // PUT 메소드에서 body로 status 받기

// 디바이스 즐겨찾기 상태 변경 API
router.put('/updateFavorite', authenticateToken, updateDeviceFavorite);  // PUT 메소드에서 body로 isFavorite 받기

//모드셋팅 정보를 가져오는 API
router.get('/modeSetting',authenticateToken,modeSetting)

// 모드셋팅 상태 변경 API
router.put('/updateModeSetting', authenticateToken, modeSettingDeviceSwitch); 

module.exports = router;