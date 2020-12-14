#!/bin/bash

mkdir -p "$HOME/.sbt/1.0/plugins"

cat >>"$HOME/.sbt/1.0/plugins/sbt-updates.sbt" <<'EOF'

// https://github.com/rtimush/sbt-updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "latest.integration")

EOF

# now run `sbt`, wait for plugin download
# and in the sbt-console run `dependencyUpdates`

# when install fails, try
#   rm -rf "$HOME/.sbt/1.0/plugins"/{project,target}
# and run `sbt` again
