import json

import cv2
import numpy as np


class Drawer:
    def __init__(self, length: int, width: int, terrain_type: dict):
        self.length = length
        self.width = width
        self.terrain_type = terrain_type
        self.tiles = {
            "WATER": cv2.imread('square_tiles/water/water.png'),
            "PLAIN": cv2.imread('square_tiles/plain/plain.png'),
            "MOUNTAIN": cv2.imread('square_tiles/mountains/mount1.png'),
            "SAND": cv2.imread('square_tiles/desert/sand.png'),
            "COAST": cv2.imread('square_tiles/desert/sand.png'),
        }
        self.tile_shape = self.tiles["PLAIN"].shape

    def draw(self):
        img = np.zeros((self.width * self.tile_shape[0], self.length * self.tile_shape[1], 3), np.uint8)
        img[:] = (0, 0, 0)
        for y in range(self.width):
            for x in range(self.length):
                ter_type = self.terrain_type[(x, y)]
                tile = self.tiles[ter_type]
                x_offset = tile.shape[1] * x
                y_offset = tile.shape[0] * y
                img[y_offset: y_offset + tile.shape[0], x_offset: x_offset + tile.shape[1]] = tile
        cv2.imwrite("terrain.png", img)


def unpack_tile_set(length=32, width=48):
    img = cv2.imread("hex_tiles/tiles.png", cv2.IMREAD_UNCHANGED)
    cnt = 0
    for x in range(int(img.shape[1] / length)):
        for y in range(int(img.shape[0] / width)):
            x_offset = length * x
            y_offset = width * y
            tile = img[y_offset: y_offset + width, x_offset: x_offset + length]
            cv2.imwrite(f"hex_tiles/tile_{cnt}.png", tile)
            cnt += 1


if __name__ == "__main__":
    with open("terrain.json") as file:
        terrain_data = json.load(file)
    terrain_len = int(terrain_data["terrainConfig"]["length"])
    terrain_wid = int(terrain_data["terrainConfig"]["width"])
    terrain_type = {}
    for x, row in enumerate(terrain_data["terrain"]):
        for y, cell in enumerate(row):
            terrain_type[(x, y)] = cell["terrainType"]
    drawer = Drawer(terrain_len, terrain_wid, terrain_type)
    drawer.draw()
    print("Cool")
