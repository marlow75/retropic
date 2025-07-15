# RetroPIC

RetroPIC is a simple graphics converter for retro machines. It can turn your modern picture into a retro graphic which can be displayed on real C64 or Amiga. Just drag and drop your picture on the machine options panel.

Accepts JPG, PNG file formats.

You can download executable jar [here](retropic.jar).
Requirements: JRE16 installed.

## Versions

* 1.15  - Autoencoder denoiser for characterset modes, reworked neural net matchers, disabled unproductive dither modes for particular machines  
* 1.14  - Bug fixes, blue noise dithering for all machines
* 1.12b - Minor bug fixes
* 1.12  - Kernel, combined filters: lowpass, sharpen, emboss, edge (HE), parametrized faster neuro char matcher 
* 1.11b - Bug fixes, charset renderers did not work at all
* 1.11a - Usable luminance mode for monochrome displays
* 1.11  - VIC-20 hires and lowres modes
* 1.10  - New PC & VIC-20 machines
* 1.9a  - PAL viewer improved, fixed IFF file problems introduced in 1.7 version, CGA text mode (experimental) 
* 1.9   - Small improvements for MCI modes, low pass filter for 8 bit machines
* 1.8   - Reworked MCI mode for C64, extra modes for TED machines (hires & MCI)
* 1.7   - New simple PAL view for all machines, Commodore TED machines (Plus4, C16, C116), scanlines removed
* 1.6   - Bayer dithering for all machines
* 1.5   - New hires interlace and noisy MCI for C64, screenshots for 8 bit machines
* 1.4   - Refactoring, new pixel formats
* 1.3   - Scanline view for C64, ZX, CPC, ST, bug fixes
* 1.2   - Improved palette & color handling for C64, CPC, Amiga 500, experimental C64 extra 
* 1.1   - Minor bug fixes, CPC new palette
* 1.0   - Minor changes, time to start versioning
* 0.1   - Initial version, bug fixes

## Supported machines

* C64, hires 320x200, multicolor 160x200, standard text mode 40x25, interlaced hires and MCI (now usable).
* C16, C116, Plus4 TED machines, hires 320x200, multicolor 160x200, interlaced hires and MCI (experimental).
* ZX Spectrum 48/+ 256x192 in 16 colors.
* Amstrad CPC series mode0 160x200 16 colors, mode1 320x200 in 4 colors.
* Atari ST, 320x200 in 16 colors on screen from 512 palette colors.
* Amiga 500/1000, PAL 320x256 and 320x512 in 32 colors or in HAM6 encoding, 4096 palette colors.
* Amiga 1200/4000, PAL 320x256, 320x512, 640x512 in 256 colors or in HAM8 encoding, 16M palette colors.
* PC CGA & VESA 10bh text modes
* VIC-20 text (unexpanded) and hires 176x184, lowres 88x184 modes (8kB expanded) 

## Graphics formats

* executable PRG for TED machines
* executable PRG for VIC-20 
* executable PRG, Koala Paint, ArtStudio, TruePaint for C64
* ZX SCR format
* ArtStudio for CPC machines
* DEGAS for Atari
* IFF format for Delux Paint – Amiga (RLE option)
* True Paint for MCI (experimental)
* COM executable for CGA & VGA

## Conversion options

Try all options available.

* Dithering - pictures are dithered using Bayer, Blue Noise, Floyds-Steinberg or Atkinson algorithms.
* Error - error tolerance for algorithms.
* Color distance – how color distance in the RGB cube is measured: euclidean, redmean simple approximation (close to human perception), nearest luminance color.
* Contrast processing - experimental luma histogram equalizer designed for 8 bit machines primarly, now enabled for every available machine. HE - standard global equalization, CLAHE & SWAHE - clipped local equalization, first fast method for local contrast enhancer, second slow but for more demanding.
* Aspect - keeps aspect ratio of original picture.
* PAL - renders PAL encoded preview in S-Video quality (separated luma & chroma signals).
* BW - renders monochrome PAL image, use with luminance distance to get more shades of grey.
* Image filters - preprocess filters: none, low pass filter, sharpen kernel 3x3 filter, emboss kernel 3x3 filter, edge combined filter.

## More info for all machines in following hyper links

![C64](assets/c64.md)
![Plus4](assets/plus4.md)
![ZX](assets/zx.md)
![CPC](assets/cpc.md)
![ST](assets/st.md)
![Amiga](assets/amiga.md)
![PC](assets/pc.md)
![VIC20](assets/vic20.md)