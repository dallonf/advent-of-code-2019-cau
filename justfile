set shell := ["powershell.exe", "-c"]

run +args:
  java -jar build\libs\adventofcode2019cau-1.0-SNAPSHOT-all.jar -r cause {{args}}