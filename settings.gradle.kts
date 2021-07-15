rootProject.name = "YCR"

// libs
include(":lib_ycr_annotation")
project(":lib_ycr_annotation").projectDir = File(rootDir, "libs/lib_ycr_annotation/")
include(":lib_ycr_apt")
project(":lib_ycr_apt").projectDir = File(rootDir, "libs/lib_ycr_apt/")
include(":lib_ycr_plugin")
project(":lib_ycr_plugin").projectDir = File(rootDir, "libs/lib_ycr_plugin/")
//include(":lib_ycr_api")
//project(":lib_ycr_api").projectDir = File(rootDir, "libs/lib_ycr_api/")
include(":lib_ycr_api_dev")
project(":lib_ycr_api_dev").projectDir = File(rootDir, "libs/lib_ycr_api_dev/")

// demo
include(":app")
include(":lib_base")
include(":module_m1")
include(":module_java")