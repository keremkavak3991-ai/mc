"""Static sound registration, subtitles and resource integrity checks."""
import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/quietledger'
JAVA = ROOT / 'src/main/java/dev/quietledger/LedgerSounds.java'

class SoundResourceTest(unittest.TestCase):
    def test_registered_sound_assets(self):
        definitions = json.loads((ASSETS / 'sounds.json').read_text())
        translations = json.loads((ASSETS / 'lang/en_us.json').read_text())
        registered = set(re.findall(r'register\("([a-z_]+)"\)', JAVA.read_text()))
        self.assertEqual(set(definitions), registered)
        self.assertEqual(len(registered), 5)
        for name, data in definitions.items():
            with self.subTest(sound=name):
                self.assertIn(data['subtitle'], translations)
                self.assertTrue(translations[data['subtitle']])
                self.assertEqual(data['sounds'], [f'quietledger:{name}'])
                contents = (ASSETS / 'sounds' / f'{name}.ogg').read_bytes()
                self.assertTrue(contents.startswith(b'OggS'))
                self.assertIn(b'\x01vorbis', contents[:100])
                self.assertGreater(len(contents), 1000)

if __name__ == '__main__':
    unittest.main()
