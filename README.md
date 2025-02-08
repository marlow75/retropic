# RetroPIC

RetroPIC is a simple graphics converter for retro machines. It can turn your modern picture into a retro graphic which can be displayed on real C64 or Amiga. Just drag and drop your picture on the machine options panel.

Accepts JPG, PNG file formats.

You can download executable jar [here](retropic.jar).

Requirements: JRE16 installed.

## Versions

* 1.10 - New PC & VIC-20 machines
* 1.9a - PAL viewer improved, fixed IFF file problems introduced in 1.7 version, CGA text mode (experimental) 
* 1.9 - Small improvements for MCI modes, low pass filter for 8 bit machines
* 1.8 - Reworked MCI mode for C64, extra modes for TED machines (hires & MCI)
* 1.7 - New simple PAL view for all machines, Commodore TED machines (Plus4, C16, C116), scanlines removed
* 1.6 - Bayer dithering for all machines
* 1.5 - New hires interlace and noisy MCI for C64, screenshots for 8 bit machines
* 1.4 - Refactoring, new pixel formats
* 1.3 - Scanline view for C64, ZX, CPC, ST, bug fixes
* 1.2 - Improved palette & color handling for C64, CPC, Amiga 500, experimental C64 extra 
* 1.1 - Minor bug fixes, CPC new palette
* 1.0 - Minor changes, time to start versioning
* 0.1 - Initial version, bug fixes

## Supported machines

* C64, hires 320x200, multicolor 160x200, standard text mode 40x25, interlaced hires and MCI (now usable).
* C16, C116, Plus4 TED machines, hires 320x200, multicolor 160x200, interlaced hires and MCI (experimental).
* ZX Spectrum 48/+ 256x192 in 16 colors.
* Amstrad CPC series mode0 160x200 16 colors, mode1 320x200 in 4 colors.
* Atari ST, 320x200 in 16 colors on screen from 512 palette colors.
* Amiga 500/1000, PAL 320x256 and 320x512 in 32 colors or in HAM6 encoding, 4096 palette colors.
* Amiga 1200/4000, PAL 320x256, 320x512, 640x512 in 256 colors or in HAM8 encoding, 16M palette colors.
* CGA text mode (experimental)
* PC CGA & VESA 10bh text modes
* VIC-20 text (unexpanded) and hires 176x184 mode (8kB expanded) 

## Graphics formats

* executable PRG for TED machines
* executable PRG for VIC-20 
* executable PRG, Koala Paint, ArtStudio, TruePaint for C64
* ZX SCR format
* ArtStudio for CPC machines
* DEGAS for Atari
* IFF format for Delux Paint – Amiga (RLE option)
* True Paint for MCI (experimental)
* COM executable for CGA

## Conversion options

Try all options available.

* Dithering - pictures are dithered using Bayer, Floyds-Steinberg or Atkinson algorithms.
* Error - error tolerance for algorithms.
* Color distance – how color distance in the RGB cube is measured: euclidean, redmean simple approximation (close to human perception), picking highest luminance color.
* Contrast processing - experimental luma histogram equalizer designed for 8 bit machines primarly, now enabled for every available machine. HE - standard global equalization, CLAHE & SWAHE - clipped local equalization, first fast method for local contrast enhancer, second slow but for more demanding.
* Aspect - keeps aspect ratio of original picture.
* PAL - renders PAL encoded preview in S-Video quality (separated luma & chroma signals).
* BW - renders monochrome PAL image.

### Commodore C64

16 colors total, 2 modes, 2 useful for graphics

* 320x200 - uses 2 colors in 8x8 screen cell.
* 160x200 - uses 4 colors in 4x8 screen cell, average or brightest color are choosen when shrinking 320->160.

Exports to executable PRG or to Art Studio (hires) and Koala Paint (multicolor).

![Hires](venusC64.png)

### PETSCII

16 foreground colors, 1 common background color for every character on the screen. Orginal PETSCII characterset.

* One hidden layer, sigmoid activation - neural net character matcher with single hidden layer, preffers semigraphics.
* Two hidden layers, sigmoid activation - neural net character matcher with two hidden layers, preffers characters.

Exports to executable PRG.

![Hires](petscii.png)

### Commodore Plus4

121 colors total, 2 modes, 2 useful for graphics

* 320x200 - uses 2 colors in 8x8 screen cell.
* 160x200 - uses 4 colors in 4x8 screen cell, average or brightest color are choosen when shrinking 320->160.

Exports to executable PRG.

![Hires](venusPlus4.png)

### ZX Spectrum 48/+

16 colors total, 1 screen mode.

* bayer or apple dithering - produces more tinty picture insted of hue explosion.
* 256x192 - uses 2 colors in 8x8 screen cell.

Exports to native SCR snapshoot.

![REAL colors](venusZX.png)

### Amstrad CPC

27 colors total, 3 modes, 2 useful.

* dithering – Bayer or Atkinson dithering, colors of the original picture are replaced by retro machine palette
* high contrast - replaces brightest/dimmest color with white and black

* 320x200 - uses 4 colors on whole screen.
* 160x200 - uses 16 colors on whole screen, average or brightest color are choosen when shrinking 320->160.

Palette is result of Kohonen pixel classification. Exports to Advanced Art Studio with standalone palette file. All files generated with AMSDOS headers.

![16 colors](venusCPC.png)

### Atari ST

512 colors total, 1 mode useful for graphics. 

* 320x200 - uses 16 colors, palette is result of Kohonen pixel classification.

Export to DEGAS paint program.

![16 colors](venusST.png)

### Amiga 500

4096 colors total, 4 modes useful for graphics.

* 320x256, 320x512 (lace) - uses 32 colors, palette is result of Kohonen pixel classification.
* 320x256, 320x512 (lace) - uses HAM6 coding, 16 color palette as a result of Kohonen classification.
* export with RLE compression - use RLE compression run1byte.

Export to Delux Paint IFF file format.

![HAM6 encoding](venusAMIGA.png)

### Amiga 1200

16M colors total, 4 modes useful for graphics.

* 320x256, 320x512 (lace), 640x512 (lace) - uses 256 colors, palette is result of Kohonen pixel classification.
* 320x256, 320x512 (lace), 640x512 (lace) - uses HAM8 coding, 64 color palette as a result of Kohonen classification.
* export with RLE compression - use RLE compression run1byte.

Export to Delux Paint IFF file format.

![HAM8 encoding](venusAMIGA1200.png)

### Commodore C64 extra

Experimental interlace modes - 136 colors. You can select max luma difference in blending colors together. Adjusted for C64C (9 luma levels), best experience with PEPTO [calculated](https://www.pepto.de/projects/colorvic/) palette.

* Hires - uses max 3 colors in 8x8 screen cell.
* MCI 320x200 - uses max 8 colors in 8x8 screen cell.

* luma threshold - interlace is about blending, only colors with the same luminance can produce non flickering new one. White has luma set to 32 black is 0, choose luminance difference which is not annoying for you.
* error - max color error you can accept.
* color approximation - linear or cubic.

Exports to executable PRG or True Paint file format.

Below interlace hires played on VICE...

![hiresC64](c64Extra.png)

... and following MCI picture.

![MCIC64](c64ExtraMCI.png)

### Commodore Plus4 extra

Experimental interlace modes - 14k colors. You can select max luma difference in blending colors together. 

* Hires - uses max 3 colors in 8x8 screen cell.
* MCI 320x200 - uses max 8 colors in 8x8 screen cell.

* luma threshold - interlace is about blending, only colors with the same luminance can produce non flickering new one. White has luma set to 32 black is 0, choose luminance difference which is not annoying for you.
* error - max color error you can accept.
* color approximation - linear or cubic.

Exports to executable PRG.

Below interlace hires played on VICE...

![HiresPlus4](plus4Extra.png)

... and following MCI picture.

![MCIPlus4](plus4ExtraMCI.png)

### PC

PC text modes.

* CGA 80x25 - uses max 16 foreground colors, one common background color with standard CGA character set.
* VESA 132x50 - uses max 16 foreground colors, one common background color with standard CGA character set.

* One hidden layer, sigmoid activation - neural net character matcher with single hidden layer, preffers semigraphics.
* Two hidden layers, sigmoid activation - neural net character matcher with two hidden layers, preffers characters.

Exports to executable COM.

![CGA](cga.png)
![VESA](vesa.png)

### VIC-20

16 colors total, 1 text mode.

* 22x23 - uses 2 colors - petscii charset.
* 176x184 - uses 2 colors and regenerated charset

Exports to executable COM.

![VIC20-txt](vic20txt.png)
![VIC20-gen](vic20gfx.png)