# Insurance keeps for the JSON pipeline (kotlinx.serialization through
# retrofit2-kotlinx-serialization-converter). The libraries ship their own
# consumer R8 rules, but a silent regression here would surface as an empty
# station list on the phone and an empty Android Auto browse tree, so the
# whole model layer is kept outright — it is a handful of tiny classes and
# costs nothing in APK size.
-keep class com.bestradio.app.data.model.** { *; }
-keep interface com.bestradio.app.data.remote.StationsApi { *; }
