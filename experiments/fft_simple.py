#!/usr/bin/env python3

import numpy as np
import matplotlib.pyplot as plt
from matplotlib import cm
from mpl_toolkits.mplot3d import Axes3D
from scipy.fftpack import rfft
from scipy.io import wavfile  # get the api


def analysis(rate, amplitude):
    duration = len(amplitude) / rate
    time = np.linspace(0, duration, len(amplitude))
    print(f"{len(amplitude)} ech, at {rate}Hz for {time[-1]}s")
    c = rfft(amplitude)
    d = len(c) // 2  # you only need half of the fft list (real signal symmetry)
    freq = np.arange(len(amplitude)) * rate / len(amplitude) / 2
    pos_real = abs(c[: (d - 1)])
    freq = freq[: (d - 1)]
    maxes = np.arange(len(pos_real) - 2, len(pos_real))
    note_freq = freq[np.argpartition(pos_real, maxes)[-len(maxes) :]]
    return freq, pos_real, note_freq


def sampling(rate, data, sample_duration):
    o = len(data.T)
    duration = len(data.T) / rate
    sample_len = int((sample_duration / duration) * o)
    D = o // sample_len
    print(sample_duration, duration, D, o)
    return D, np.split(data.T[: ((o // D) * D)], D)


rate, data = wavfile.read("sounds/si.wav")  # load the data
# rate, data = wavfile.read('sounds/la440.wav') # load the data
print(rate)
o = len(data.T)
D, amplitudes = sampling(rate, data, 20 / 1000)
print(len(amplitudes))
pos_reals = []
note_freqs = []
for amplitude in amplitudes:
    freq, pos_real, note_freq = analysis(rate, amplitude)
    # for n in note_freq:
    #     plt.axvline(x=n)
    print(f"Note freq: {note_freq}")
    # plt.loglog(freq, pos_real, 'r')
    # plt.show()
    note_freqs.append(note_freq)
    pos_reals.append(pos_real)


fig = plt.figure()
ax = fig.gca(projection="3d")
p = np.array(pos_reals)
X, Y = np.meshgrid(freq, np.arange(D))
surf = ax.plot_surface(
    np.log10(X), Y, np.log10(p), cmap=cm.coolwarm, linewidth=0, antialiased=False
)
fig.colorbar(surf, shrink=0.5, aspect=5)
plt.show()

note_freqs = np.transpose(np.array(note_freqs))
for n in note_freqs:
    plt.plot(np.arange(D), n)

plt.show()
