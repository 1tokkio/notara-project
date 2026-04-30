const mongoose = require('mongoose');
const config = require('../config/config');

let isConnected = false;

const connectMongo = async () => {
  if (isConnected) return;
  await mongoose.connect(config.mongodb.uri);
  isConnected = true;
  console.log('Conectado a MongoDB');
};

module.exports = { connectMongo };
