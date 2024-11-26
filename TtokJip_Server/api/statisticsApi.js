const { MongoClient } = require('mongodb');
const { MONGODB_URI } = process.env;

const client = new MongoClient(MONGODB_URI);

const uploadSensorData = async (req, res) => {
    const { houseId } = req.user;
    const { date, temperature, humidity, totalWattage } = req.body;

    if (!date || temperature == null || humidity == null || totalWattage == null) {
        return res.status(400).json({ message: "필수 데이터가 누락되었습니다." });
    }

    try {
        // 기존 데이터를 확인
        const existingLog = await collection.findOne({ houseId, date });

        if (existingLog) {
            const updatedLog = {
                temperature: (existingLog.temperature + temperature) / 2,
                humidity: (existingLog.humidity + humidity) / 2,
                totalWattage: existingLog.totalWattage + totalWattage,
            };

            await collection.updateOne(
                { houseId, date },
                { $set: updatedLog }
            );
            res.status(200).json({ message: "로그 데이터가 업데이트되었습니다." });
        } else {
            // 기존 데이터가 없으면 새로 추가
            const logData = {
                houseId,
                date,
                temperature,
                humidity,
                totalWattage,
            };



            await collection.insertOne(logData);

            res.status(201).json({ message: "로그 데이터가 성공적으로 저장되었습니다." });
        }
    } catch (error) {
        console.error("로그 데이터 저장 오류:", error);
        res.status(500).json({ message: "서버 오류로 인해 저장에 실패했습니다." });
    } finally {
        await client.close();
    }
};

const getStatistics = async (req, res) => {
    const { houseId } = req.user;
    const { date } = req.query;

    if (!date) {
        return res.status(400).json({ message: "날짜 데이터가 누락되었습니다." });
    }

    try {
        await client.connect();
        const database = client.db('ttokjip');
        const collection = database.collection('logs');

        // 문자열 형식으로 날짜 계산
        const currentDate = new Date(date);
        const startOfWeek = new Date(currentDate);
        startOfWeek.setDate(currentDate.getDate() - 6); // 6일 전
        const startOfMonth = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}-01`; // 이번 달 1일
        const startOfLastMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1);
        const endOfLastMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 0);

        const startOfWeekStr = startOfWeek.toISOString().split('T')[0];
        const endOfLastMonthStr = endOfLastMonth.toISOString().split('T')[0];

        // 1. 일주일간의 온습도 변화
        const weeklyLogs = await collection.find({
            houseId,
            date: { $gte: startOfWeekStr, $lte: date } // 문자열 비교
        }).toArray();

        const weeklyData = weeklyLogs.map(log => ({
            date: log.date,
            temperature: log.temperature,
            humidity: log.humidity
        }));

        // 2. 이번 달 총 전력량
        const monthlyLogs = await collection.find({
            houseId,
            date: { $gte: startOfMonth, $lte: date }
        }).toArray();

        const monthlyTotalWattage = monthlyLogs.reduce((sum, log) => sum + log.totalWattage, 0);

        // 3. 저번 달 총 전력량
        const lastMonthLogs = await collection.find({
            houseId,
            date: { $gte: startOfLastMonth.toISOString().split('T')[0], $lte: endOfLastMonthStr }
        }).toArray();

        const lastMonthTotalWattage = lastMonthLogs.reduce((sum, log) => sum + log.totalWattage, 0);

        // 4. 모든 데이터의 평균 총 전력량
        const allLogs = await collection.find({ houseId }).toArray();
        const totalWattageSum = allLogs.reduce((sum, log) => sum + log.totalWattage, 0);
        const totalWattageAvg = allLogs.length ? totalWattageSum / allLogs.length : 0;

        // 응답 데이터 작성
        const responseData = {
            weeklyData,
            monthlyTotalWattage,
            lastMonthTotalWattage,
            totalWattageAvg
        };

        res.status(200).json(responseData);
    } catch (error) {
        console.error("통계 데이터 계산 오류:", error);
        res.status(500).json({ message: "서버 오류로 인해 데이터를 가져올 수 없습니다." });
    } finally {
        await client.close();
    }
};




module.exports = { uploadSensorData,getStatistics };