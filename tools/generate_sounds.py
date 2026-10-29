#!/usr/bin/env python3
"""Generate original mono horror cues. Requires numpy and soundfile (libsndfile Vorbis)."""
from pathlib import Path
import numpy as np
import soundfile as sf

RATE = 22050
OUT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/quietledger/sounds'
RNG = np.random.default_rng(20261029)


def noise(n, smoothing):
    # Integrator makes synthetic, unvoiced air/wood textures, not recorded speech.
    x = RNG.standard_normal(n)
    return np.convolve(x, np.ones(smoothing) / smoothing, mode='same')


def env(t, start, duration):
    x = np.clip((t - start) / duration, 0, 1)
    return np.where((t >= start) & (t <= start + duration), np.sin(np.pi * x) ** 1.5, 0)


def knock(t, start, strength=1):
    x = np.maximum(t - start, 0)
    active = (t >= start).astype(float)
    return strength * active * np.exp(-x * 17) * (np.sin(2*np.pi*91*x) + 0.48*np.sin(2*np.pi*147*x))


def write(name, samples):
    # Short fades and a conservative peak leave headroom and avoid click transients.
    samples = np.asarray(samples, dtype=float)
    fade = min(int(RATE * .08), len(samples)//2)
    samples[:fade] *= np.linspace(0, 1, fade)
    samples[-fade:] *= np.linspace(1, 0, fade)
    peak = np.max(np.abs(samples))
    if peak:
        samples *= 0.42 / peak
    sf.write(OUT / (name + '.ogg'), samples, RATE, format='OGG', subtype='VORBIS')


def time(seconds):
    return np.arange(int(seconds * RATE)) / RATE


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    t = time(3.3)
    # A distant creaking timber: slow, wavering partials and filtered friction.
    phase = 2*np.pi*(145*t + 22*np.sin(2*np.pi*.22*t))
    door = env(t, .25, 2.55) * (.65*np.sin(phase) + .22*np.sin(phase*1.51) + .8*noise(len(t), 30))
    write('distant_creak', door)

    t = time(5.0)
    # Low detuned tones and airy filtered noise, with a gentle rise and fall.
    cave = env(t, .05, 4.9) * (.55*np.sin(2*np.pi*62*t) + .35*np.sin(2*np.pi*65.2*t) + .7*noise(len(t), 65))
    write('hollow_drone', cave)

    t = time(2.8)
    deep = knock(t, .3) + knock(t, .72, .7) + knock(t, 1.32, .9)
    deep += np.roll(deep, int(.23*RATE)) * .24
    deep += env(t, .1, 2.5) * noise(len(t), 65) * .15
    write('deep_knocks', deep)

    t = time(4.1)
    breath = (env(t, .2, 1.5) + .8*env(t, 2.0, 1.65)) * noise(len(t), 18)
    breath += .12 * env(t, .2, 3.7) * np.sin(2*np.pi*78*t)
    write('near_breath', breath)

    t = time(3.5)
    steps = sum(knock(t, start, strength) for start, strength in [(0.35,.55),(.92,.7),(1.55,.8),(2.25,.6)])
    steps += .2 * np.roll(steps, int(.16*RATE))
    steps += .10 * noise(len(t), 20) * env(t, .2, 2.4)
    write('unseen_steps', steps)


if __name__ == '__main__':
    main()
