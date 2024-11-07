const { MongoClient } = require('mongodb');

const uri = process.env.MONGODB_URI;
const client = new MongoClient(uri);

const connectDB = async () => {
    try {
        await client.connect();
        console.log('MongoDB 연결 성공!');
    } catch (error) {
        console.error('MongoDB 연결 실패:', error);
        process.exit(1);  // 연결 실패 시 프로세스 종료
    }
};

module.exports = { connectDB };