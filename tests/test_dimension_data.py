"""Static checks only; Minecraft still must decode and load these files."""
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/data/quietledger/dimension'

class DimensionDataTest(unittest.TestCase):
    def test_flat_dimension_definitions(self):
        paths = sorted(ROOT.glob('*.json'))
        self.assertEqual([p.name for p in paths], ['empty_expanse.json', 'forgotten_corridor.json'])
        for path in paths:
            with self.subTest(path=path.name):
                value = json.loads(path.read_text())
                self.assertEqual(value['type'], 'minecraft:the_end')
                self.assertEqual(value['generator']['type'], 'minecraft:flat')
                settings = value['generator']['settings']
                self.assertEqual(settings['biome'], 'minecraft:the_void')
                self.assertEqual(settings['structure_overrides'], [])
                self.assertFalse(settings['features'])
                self.assertFalse(settings['lakes'])
                self.assertGreaterEqual(sum(layer['height'] for layer in settings['layers']), 1)
                self.assertLess(sum(layer['height'] for layer in settings['layers']), 256)
                self.assertEqual(settings['layers'][0]['block'], 'minecraft:bedrock')

if __name__ == '__main__':
    unittest.main()
