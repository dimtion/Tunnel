import numpy as np
from scipy import fftpack

def sampling(rate, data, sample_duration):
    duration = len(data) / rate
    sample_len = int((sample_duration / duration) * len(data))
    alpha = len(data) // sample_len
    # print(sample_duration, duration, alpha, len(data))
    return np.split(data[: ((len(data) // alpha) * alpha)], alpha)

def analysis(rate, amplitude):
    duration = len(amplitude) / rate
    fft = fftpack.rfft(amplitude)
    d = len(fft) // 2  # you only need half of the fft list (real signal symmetry)
    freq = np.arange(len(amplitude)) * rate / len(amplitude) / 2
    pos_real = abs(fft[: (d - 1)])
    freq = freq[: (d - 1)]
    return freq, pos_real