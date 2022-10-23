set shell := ["powershell.exe", "-c"]

run +args:
  gradle -q --console=plain run --args="-r cause {{args}}"

run-jar +args:
  java -jar build\libs\adventofcode2019cau-1.0-SNAPSHOT-all.jar -r cause {{args}}

update-cause:
  gradle -p ..\causelang\ktcause jar shadowJar
  cp ..\causelang\ktcause\build\libs\ktcause-0.1-SNAPSHOT.jar lib
  cp ..\causelang\ktcause\build\libs\ktcause-0.1-SNAPSHOT-all.jar lib

build-host: update-cause
  gradle shadowJar