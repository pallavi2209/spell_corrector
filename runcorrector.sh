#!/bin/bash
#./runcorrector <uniform | empirical> <query file> <extra>(optional) <gold file>(optional)

java -Xmx4096m -cp "bin:jars/*" edu.stanford.cs276.RunCorrector $@
