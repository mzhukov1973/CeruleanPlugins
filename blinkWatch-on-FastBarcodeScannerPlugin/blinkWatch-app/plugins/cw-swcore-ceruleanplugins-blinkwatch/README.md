## This is direct cannibalisation of the excellent FastBarcodeScanningPlugin by Thomas Schaumburg <thomas@schaumburg-it.dk>.

#### Changes basically boil down to:
- [ ] Making it single-platform (Android),
- [ ] Making frontal camera the only choice,
- [ ] Setting resolution to something really low and
- [ ] Substituting my primitive image analysis (`watch` method) for the existing barcode scanning method.
- [ ] Remove Camera support from it, since only Camera2 seems to be fast enough to work as CeruleanWhisper Pub app needs it to.
