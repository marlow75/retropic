### Commodore Plus4

121 colors total, 2 modes, 2 useful for graphics

* 320x200 - uses 2 colors in 8x8 screen cell.
* 160x200 - uses 4 colors in 4x8 screen cell, average or brightest color are choosen when shrinking 320->160.

Exports to executable PRG.

![Hires](venusPlus4.png)

### Plus4 extra

Trick modes, you need to experiment with setting to produce decent picture. Noise dithering works very well with MCI, bayer with Interlaced Hires.

MCI try to avoid pictures with big empty areas.

Converter mode

* Hires interlaced - 3 colors in a cell 8x8
* Multicolor interlaced - 8 colors in a cell 8x8

Color aproximation for hires interlaced

* linear - most distant colors in RGB cube
* cube - cube color approximation

Flickering for multicolor interlaced (MCI) - eliminate picture flashing.

luma threshold - how far apart can be the brightness of used colors.

![Plus4Extra](plus4Extra.png)

Hires interlaced

![Plus4ExtraMCI](plus4ExtraMCI.png)

MCI
