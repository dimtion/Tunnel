#!/usr/bin/env python
# coding: utf-8

import numpy as np
from scipy.fftpack import rfft
import matplotlib.pyplot as plt
import scipy

import pyaudio
import wave
import sys
import struct

FORMAT = pyaudio.paInt16
CHANNELS = 1
RATE = 44100
CHUNK = 1024
RECORD_SECONDS = 15
WAVE_OUTPUT_FILENAME = "file.wav"

def analysis(rate, amplitude):
    duration = len(amplitude) / rate
    time = np.linspace(0, duration, len(amplitude))
    # print(f"{len(amplitude)} ech, at {rate}Hz for {time[-1]}s")
    c = rfft(amplitude)
    d = len(c) // 2  # you only need half of the fft list (real signal symmetry)
    freq = np.arange(len(amplitude)) * rate / len(amplitude) / 2
    pos_real = abs(c[: (d - 1)])
    freq = freq[: (d - 1)]
    maxes = np.arange(len(pos_real) - 2, len(pos_real))
    note_freq = freq[np.argpartition(pos_real, maxes)[-len(maxes) :]]
    return freq, pos_real, note_freq

audio = pyaudio.PyAudio()

# start Recording
stream = audio.open(
    format=FORMAT, channels=CHANNELS, rate=RATE, input=True, frames_per_buffer=CHUNK
)
print("recording 🎤")
frames = []

notes = []
# while True:
for i in range(0, int(RATE / CHUNK * RECORD_SECONDS)):
    data = stream.read(CHUNK)
    data = scipy.array(struct.unpack("%dB" % (CHUNK * 2), data))
    freq, pos_real, note_freq = analysis(RATE, data)
    notes.append(note_freq)
    # plt.scatter(i, note_freq[0], zorder=2)
    # plt.pause(0.0001)

print("done reccording 👌")
note_freqs = np.transpose(np.array(notes))
for n in note_freqs:
    plt.plot(np.arange(len(notes)), n)

plt.show()

# for i in range(0, int(RATE / CHUNK * RECORD_SECONDS)):
#     data = stream.read(CHUNK)
#     frames.append(data)
# print("finished recording")
#
#
# # stop Recording
# stream.stop_stream()
# stream.close()
# audio.terminate()
#
# waveFile = wave.open(WAVE_OUTPUT_FILENAME, 'wb')
# waveFile.setnchannels(CHANNELS)
# waveFile.setsampwidth(audio.get_sample_size(FORMAT))
# waveFile.setframerate(RATE)
# waveFile.writeframes(b''.join(frames))
# waveFile.close()
