const express = require('express');
const router = express.Router();
const { MongoClient } = require('mongodb');
const { MONGODB_URI } = process.env;

const client = new MongoClient(MONGODB_URI);
router.post('/', async (req, res) => {
    try {
        await client.connect(); // 연결 열기
        const database = client.db('ttokjip');
        const logsCollection = database.collection('logs');

        const logData = req.body;
        await logsCollection.insertOne({ log: logData.log });

        res.status(200).send({ message: "Log saved successfully" });
    } catch (error) {
        console.error("Error saving log:", error);
        res.status(500).send({ message: "Failed to save log" });
    } finally {
        await client.close(); // 연결 닫기
    }
});

module.exports = router;