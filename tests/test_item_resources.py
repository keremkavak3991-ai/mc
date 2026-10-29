import json
from pathlib import Path
import struct
import unittest

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'

class ItemResourceTest(unittest.TestCase):
    def test_torn_page_resources(self):
        assets = ROOT / 'assets/quietledger'
        recipe = json.loads((ROOT / 'data/quietledger/recipe/torn_page.json').read_text())
        self.assertEqual(recipe['result']['id'], 'quietledger:torn_page')
        self.assertEqual({x['item'] for x in recipe['ingredients']}, {'minecraft:paper', 'minecraft:charcoal'})
        model = json.loads((assets / 'models/item/torn_page.json').read_text())
        self.assertEqual(model['textures']['layer0'], 'quietledger:item/torn_page')
        self.assertIn('item.quietledger.torn_page', json.loads((assets / 'lang/en_us.json').read_text()))
        texture = (assets / 'textures/item/torn_page.png').read_bytes()
        self.assertEqual(texture[:8], b'\x89PNG\r\n\x1a\n')
        self.assertEqual(struct.unpack('>II', texture[16:24]), (16, 16))

if __name__ == '__main__':
    unittest.main()
