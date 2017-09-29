## This is direct cannibalisation of the excellent cordova-plugin-qrscanner by BitPay Inc.

#### Changes basically boil down to:
- [ ] Making it single-platform (Android),
- [ ] Forfeiting flashlight controls,
- [ ] Making frontal camera the only choice,
- [ ] Setting resolution to something really low and
- [ ] Substituting primitive image analysis (`watch` method) for the existing `scan` method.
