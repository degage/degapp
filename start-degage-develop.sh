#!/bin/sh

cd /Users/dries/projects/degage-private

oxynade_folder=`pwd`

osascript -e "tell application \"Terminal\"" \
    -e "tell application \"System Events\" to keystroke \"t\" using {command down}" \
    -e "do script \"cd /Users/dries/projects/Degage/src/webapp; sbt 'run 9090';\" in front window" \
    -e "end tell"
    > /dev/null

osascript -e "tell application \"Terminal\"" \
    -e "tell application \"System Events\" to keystroke \"t\" using {command down}" \
    -e "do script \"cd /Users/dries/projects/Degage/src/webapp;watchify -t [ babelify --presets [ es2015 react stage-2 ] ]  -t [ browserify-css --output [bundledries.css] ] app/assets/javascripts/main.jsx -o /Users/dries/projects/Degage/src/webapp/target/web/browserify/main.js;\" in front window" \
    -e "end tell"
    > /dev/null
