const { MongoClient } = require('mongodb');

// MongoDB URI를 환경 변수에서 가져오기
const uri = process.env.MONGODB_URI;

// 클라이언트 생성 (useUnifiedTopology는 최신 드라이버에서 기본값이라 생략 가능)
const client = new MongoClient(uri);

// MongoDB 연결 유지 및 재사용
const connectDB = async () => {
    try {
        // 클라이언트가 연결되지 않은 경우 연결 수행
        if (!client.topology || client.topology.isDestroyed()) {
            await client.connect();
            console.log('MongoDB 연결 성공!');
        }
    } catch (error) {
        console.error('MongoDB 연결 실패:', error);
        process.exit(1); // 실패 시 프로세스 종료
    }
};

module.exports = { connectDB, client }; // 클라이언트 객체도 함께 내보냄
