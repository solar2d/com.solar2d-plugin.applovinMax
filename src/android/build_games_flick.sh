#! /bin/bash
# Builds the Android plugin and deploys it as the games.flick plugin.applovinMax
# Android package in ~/Solar2DPlugins, the same way the in-house vibrator and
# mixpanel plugins are deployed. iOS is not built here: the games.flick iphone
# package is a copy of the upstream solar2d release.
set -e
cd "$(dirname "$0")"

PLUGIN_NAME=applovinMax
DEST=/Users/alanthomson/Solar2DPlugins/games.flick/plugin.$PLUGIN_NAME/android
UPSTREAM_ASSETS=../../plugins/2018.3326/android
OUT=build/games_flick_output

./gradlew :plugin:assembleRelease --no-daemon --console=plain

FILE=plugin/build/outputs/aar/plugin-release.aar
if [ ! -f "$FILE" ]; then
    echo "$FILE does not exist."
    exit 1
fi

rm -rf "$OUT"
mkdir -p "$OUT"
cp "$FILE" "$OUT/plugin-$PLUGIN_NAME.aar"
cp "$UPSTREAM_ASSETS/corona.gradle" "$OUT/corona.gradle"
# upstream ships a jar; we ship the aar, so the metadata format differs
sed 's/format = "jar"/format = "aar"/' "$UPSTREAM_ASSETS/metadata.lua" > "$OUT/metadata.lua"
grep -q 'format = "aar"' "$OUT/metadata.lua"

if [ -f "$DEST/data.tgz" ]; then
    STAMP=$(date +%Y_%m_%d_%H%M)
    mkdir -p "$DEST/../old/android_$STAMP"
    mv "$DEST/data.tgz" "$DEST/../old/android_$STAMP/data.tgz"
    echo "previous android/data.tgz archived to old/android_$STAMP"
fi
mkdir -p "$DEST"
(cd "$OUT" && COPYFILE_DISABLE=true tar -czvf data.tgz --exclude=".*" --exclude="*.tgz" *)
mv "$OUT/data.tgz" "$DEST/data.tgz"
echo "deployed: $DEST/data.tgz"
tar -tzf "$DEST/data.tgz"
