# RetroPIC

**RetroPIC** is a simple and user-friendly graphics converter for retro computers. With just a few clicks, you can transform any modern image into a retro-style graphic, ready to display on real hardware like the Commodore 64 or Amiga. Simply drag and drop your picture onto the machine options panel to get started.

- **Supported formats:** JPG, PNG  
- **Download:** [retropic.jar](retropic.jar)  
- **Requirements:** Java Runtime Environment (JRE) 16 or newer

---

## Version History

- **1.18c** - New color distance - Mahalonobis (palette) metric for machines with arbitrary palette, minor bug fixes
- **1.18b** – Reworked Lanchos scaling, minor bug fixes in charset generator, advanced PAL simulation with bloom, moire, scanlines, viniete
- **1.18** – Added Lanchos scaling, minor bug fixes
- **1.17** – Improved neural algorithms, faster and more robust; reworked VIC-20 charset modes; minor bug fixes; removed third-party libraries
- **1.16** – New noise dithering; added C128 VDC machine (640x200, 2 colors in 8x2 tile mode); improved MCI modes and edge filter
- **1.15** – Autoencoder denoiser for charset modes; improved neural network matchers; disabled unproductive dithering modes for certain machines
- **1.14** – Bug fixes; blue noise dithering for all machines
- **1.12b** – Minor bug fixes
- **1.12** – New filters: lowpass, sharpen, emboss, edge (HE); faster neural character matcher
- **1.11b** – Bug fixes; charset renderers fixed
- **1.11a** – Luminance mode for monochrome displays
- **1.11** – Added hires and lowres modes for VIC-20
- **1.10** – Added PC and VIC-20 machines
- **1.9a** – Improved PAL viewer; fixed IFF file issues from version 1.7; experimental CGA text mode
- **1.9** – Improvements to MCI modes; low pass filter for 8-bit machines
- **1.8** – Reworked MCI mode for C64; new modes for TED machines (hires & MCI)
- **1.7** – New PAL preview for all machines; support for Commodore TED machines (Plus4, C16, C116); scanlines removed
- **1.6** – Bayer dithering for all machines
- **1.5** – New hires interlace and noisy MCI for C64; screenshots for 8-bit machines
- **1.4** – Refactoring; new pixel formats
- **1.3** – Scanline view for C64, ZX, CPC, ST; bug fixes
- **1.2** – Improved palette and color handling for C64, CPC, Amiga 500; experimental C64 extra mode
- **1.1** – Minor bug fixes; new CPC palette
- **1.0** – Initial versioning
- **0.1** – First release; bug fixes

---

## Supported Machines

- **Commodore 64:** Hires 320x200, multicolor 160x200, standard text mode 40x25, interlaced hires and MCI
- **C16, C116, Plus4 (TED):** Hires 320x200, multicolor 160x200, interlaced hires and MCI (experimental)
- **ZX Spectrum 48/+**: 256x192, 16 colors
- **Amstrad CPC:** Mode 0 (160x200, 16 colors), Mode 1 (320x200, 4 colors)
- **Atari ST:** 320x200, 16 colors from a 512-color palette
- **Amiga 500/1000:** PAL 320x256 and 320x512, 32 colors or HAM6 (4096 colors)
- **Amiga 1200/4000:** PAL 320x256, 320x512, 640x512, 256 colors or HAM8 (16 million colors)
- **PC:** CGA & VESA 10bh text modes
- **VIC-20:** Text (unexpanded), hires 176x184, lowres 88x184 (with 8kB expansion)
- **C128:** VDC 640x200 (16 colors, 64kB)

---

## Supported Graphics Formats

- Executable PRG for TED machines
- Executable PRG for VIC-20
- Executable PRG, Koala Paint, ArtStudio for C64
- ZX SCR format for Spectrum
- ArtStudio for CPC
- DEGAS for Atari
- IFF (Deluxe Paint – Amiga, RLE option)
- COM executable for CGA & VGA
- Executable PRG for C128

---

## Conversion Options

Explore all available options to get the best results:

- **Dithering:** Choose from Bayer, Blue Noise, Floyd-Steinberg, Noise, or Atkinson algorithms
- **Error:** Set error tolerance for algorithms
- **Color Distance:** Select how color distance is measured (Euclidean, Redmean, Mahalonobis, or nearest luminance)
- **Contrast Processing:** Experimental luma histogram equalizer for 8-bit machines (HE for global, CLAHE & SWAHE for local contrast)
- **Aspect:** Preserve the original aspect ratio
- **PAL:** Preview PAL-encoded output in S-Video quality (separate luma & chroma)
- **BW:** Render monochrome PAL images; use luminance distance for more shades of grey
- **Image Filters:** Preprocessing filters: none, low pass, sharpen (3x3), emboss (3x3), edge combined

---

## Learn More

Find detailed information for each supported machine:

- [Commodore 64](assets/c64.md)
- [Commodore Plus4](assets/plus4.md)
- [ZX Spectrum +/48](assets/zx.md)
- [Amstrad CPC](assets/cpc.md)
- [Atari ST](assets/st.md)
- [Commodore Amiga](assets/amiga.md)
- [PC XT](assets/pc.md)
- [Commodore VIC20](assets/vic20.md)
- [Commodore C128](assets/c128.md)

---

If you have any questions or want to learn more, check the links above or contact the author!
