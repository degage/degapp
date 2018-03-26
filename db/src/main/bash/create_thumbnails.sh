find /degapp/uploads/carphotos -name "*.*" -exec \
sh -c 'convert -thumbnail x80 {} /degapp/uploads/thumbnail/carphotos/$(basename {})' \;
