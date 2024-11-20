const mongoose = require('mongoose');

const logSchema = new mongoose.Schema({
    timestamp: { type: Date, default: Date.now }, // 로그 생성 시간
    log: { type: String, required: true },        // 로그 내용
});

module.exports = mongoose.model('Log', logSchema);