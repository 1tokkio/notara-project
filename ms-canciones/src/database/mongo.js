const mongoose = require('mongoose');
<<<<<<< HEAD
=======
const config = require('../config/config');
>>>>>>> origin/panxo

let isConnected = false;

const connectMongo = async () => {
  if (isConnected) return;
<<<<<<< HEAD

  const uri = process.env.MONGODB_URI || 'mongodb://localhost:27017/linguaflow';
  await mongoose.connect(uri);
=======
  await mongoose.connect(config.mongodb.uri);
>>>>>>> origin/panxo
  isConnected = true;
  console.log('Conectado a MongoDB');
};

module.exports = { connectMongo };
