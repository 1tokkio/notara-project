const mongoose = require('mongoose');

let isConnected = false;

const connectMongo = async () => {
  if (isConnected) return;

  const uri = process.env.MONGODB_URI || 'mongodb://localhost:27017/linguaflow';
  await mongoose.connect(uri);
  isConnected = true;
  console.log('Conectado a MongoDB');
};

module.exports = { connectMongo };
