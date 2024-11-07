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

    try {
        await client.connect();
        const database = client.db('ttokjip');
        const collection = database.collection('devices');

        const result = await collection.updateOne(
            { _id: new MongoClient.ObjectId(deviceId) },
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

const updateDeviceFavorite=async(req, res) => {
    const{ deviceId, isFavorite }=req.body;
    const { houseId } = req.user;
    try{
        await client.connect();
        const database = client.db('ttokjip');
        const collection = database.collection('devices');
    }catch(error){
        console.error(error)
        res.status(500).json({message :'서버 오류'})
    }finally {
        await client.close();
    }
}
module.exports = {     
    getDevices,
    updateDeviceStatus,
    updateDeviceFavorite };