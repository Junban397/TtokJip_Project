const { MongoClient } = require('mongodb');
const { MONGODB_URI } = process.env;

const client = new MongoClient(MONGODB_URI);

const getDevices = async (req, res) => {
    const { houseId } = req.user;  // 요청자의 houseId를 사용

    try {
        await client.connect();
        const database = client.db('ttokjip');
        const collection = database.collection('devices');

        const devices = await collection.find({ houseId: houseId }).toArray();

        if (devices.length > 0) {
            res.status(200).json(devices);
        } else {
            res.status(404).json({ message: '해당 사용자의 디바이스가 없습니다.' });
        }
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류' });
    } finally {
        await client.close();
    }
};

const updateDeviceStatus = async (req, res) => {
    const { deviceId, status } = req.body;

    // deviceId가 없으면 400 에러 처리
    if (!deviceId) {
        return res.status(400).json({ message: 'deviceId가 제공되지 않았습니다.' });
    }

    try {
        await client.connect();
        const database = client.db('ttokjip');
        const collection = database.collection('devices');

        const result = await collection.updateOne(
            { deviceId: deviceId },  // deviceId로 찾기
            { $set: { deviceStatus: status } }
        );

        if (result.modifiedCount > 0) {
            res.status(200).json({ message: '디바이스 상태가 성공적으로 변경되었습니다.' });
        } else {
            res.status(404).json({ message: '디바이스를 찾을 수 없습니다.' });
        }
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류' });
    } finally {
        await client.close();
    }
};
const updateDeviceFavorite = async (req, res) => {
    const { deviceId, isFavorite } = req.body; 

    // deviceId가 없으면 400 에러 처리
    if (!deviceId) {
        return res.status(400).json({ message: 'deviceId가 제공되지 않았습니다.' });
    }

    try {
        await client.connect();
        const database = client.db('ttokjip');
        const collection = database.collection('devices');

        const result = await collection.updateOne(
            { deviceId: deviceId}, 
            { $set: { isFavorite: isFavorite } }
        );

        if (result.modifiedCount > 0) {
            res.status(200).json({ message: '디바이스 즐겨찾기 상태가 성공적으로 변경되었습니다.' });
        } else {
            res.status(404).json({ message: '디바이스를 찾을 수 없습니다.' });
        }
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류' });
    } finally {
        await client.close();
    }
};

const modeSetting = async (req, res) => {
    const { houseId } = req.user;
    const { mode } = req.query;

    try {
        await client.connect();
        const database = client.db('ttokjip');

        // 1. houseId에 맞는 device 데이터 가져오기
        const devices = await database.collection('devices').find({ houseId: houseId }).toArray();

        // 2. houseId와 mode에 맞는 mode 데이터 가져오기
        const modeData = await database.collection('mode').findOne({ houseId: houseId, mode: mode });

        // modeData가 없을 경우 처리
        if (!modeData) {
            return res.status(404).json({ message: '해당 모드 데이터가 없습니다.' });
        }

        // 3. modeData.devices의 상태를 devices에 적용
        const modeDevices = devices.map(device => {
            // modeData.devices에서 해당 deviceId와 일치하는 상태 찾기
            const modeDevice = modeData.devices.find(modeDevice => modeDevice.deviceId === device.deviceId);

            // modeDevice가 없거나 상태가 없을 경우 기본 상태로 처리
            const modeStatus = modeDevice ? modeDevice.status : device.deviceStatus;

            return {
                ...device,               // 기존 device 정보
                modeStatus: modeStatus,  // modeData에서 가져온 상태 또는 기본 상태
                mode: mode               // 현재 모드 추가
            };
        });

        // 4. 상태가 업데이트된 디바이스 정보 반환
        res.status(200).json(modeDevices);

    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류' });
    } finally {
        await client.close();
    }
};

const modeSettingDeviceSwitch=async(req,res)=>{
    const {houseId, deviceId, mode, newStatus } = req.body;
    try{
        await client.connect();
        const database = client.db('ttokjip');
        const collection = database.collection('mode');

        const result = await collection.updateOne(
            { houseId:houseId, mode:mode, "devices.deviceId": deviceId },  
            { $set: { "devices.$.status": newStatus } }
        );

        if (result.modifiedCount > 0) {
            res.status(200).json({ message: '디바이스 즐겨찾기 상태가 성공적으로 변경되었습니다.' });
        } else {
            res.status(404).json({ message: '디바이스를 찾을 수 없습니다.' });
        }

    }catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류' });
    } finally {
        await client.close();
    }
};

const modeSettingAllDeviceSwitch = async (req, res) => {
    const { houseId } = req.user;
    const { mode } = req.body;
    console.log('Received request for mode:', mode);  // 추가된 로그
    try {
        await client.connect();
        const database = client.db('ttokjip');


        //houseId와 mode에 맞는 mode 데이터 가져오기
        const modeData = await database.collection('mode').findOne({ houseId: houseId, mode: mode });

        // modeData가 없을 경우 처리
        if (!modeData) {
            return res.status(404).json({ message: '해당 모드 데이터가 없습니다.' });
        }

        const bulkDeviceStatus=modeData.devices.map(modeDevice =>({
            updateOne:{
                filter: { houseId: houseId, deviceId: modeDevice.deviceId }, // houseId와 deviceId로 필터링
                update: { $set: { deviceStatus: modeDevice.status } }         // mode의 상태로 업데이트
            }

        }));
        const result = await database.collection('devices').bulkWrite(bulkDeviceStatus);

        // 4. 상태가 업데이트된 디바이스 정보 반환
        res.status(200).json({
            message: '모든 디바이스의 상태가 모드에 맞게 변경되었습니다.',
            matchedCount: result.matchedCount,
            modifiedCount: result.modifiedCount
        });

    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류' });
    } finally {
        await client.close();
    }
};


module.exports = { 
    getDevices,
    updateDeviceStatus,
    updateDeviceFavorite,
    modeSetting ,
    modeSettingDeviceSwitch,
    modeSettingAllDeviceSwitch
};