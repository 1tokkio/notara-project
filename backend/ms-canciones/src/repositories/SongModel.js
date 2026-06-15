const mongoose = require('mongoose');

const songSchema = new mongoose.Schema(
  {
    spotifyId: { type: String, required: true, unique: true },
    title: { type: String, required: true },
    artist: { type: String, required: true },
    album: { type: String },
    duration: { type: Number }, // en milisegundos
    imageUrl: { type: String },
    previewUrl: { type: String },
    lyrics: { type: String, default: null },
    lyricsUpdatedAt: { type: Date, default: null },
  },
  { timestamps: true }
);

const Song = mongoose.model('Song', songSchema);

module.exports = Song;
