fojava -Xmx1500m -cp "D:\record-and-replay\symber-transformer\bin\";"d:\record-and-replay\symber-common\bin\";"d:\record-and-replay\javagrande\bin\";"d:\record-and-replay\symber-transformer\lib\sootclasses-2.5.0.jar";"d:\record-and-replay\symber-transformer\lib\sootclasses-2.4.0.mine.jar";"d:\record-and-replay\symber-transformer\lib\polyglotclasses-1.3.5.jar";"d:\record-and-replay\symber-transformer\lib\jasminclasses-2.4.0.jar" edu.ist.symber.Main section1.JGFSyncBench 2
  r /L %%A in (1,1,1) do ( 
    java -Xmx1500m -cp "d:\record-and-replay\symber-recorder\bin\";"d:\record-and-replay\symber-common\bin\";"d:\record-and-replay\symber-recorder\lib\json";"d:\record-and-replay\symber-transformer\tmp\" edu.ist.symber.Main section1.JGFSyncBench 2
    java -Xmx1500cm -cp "d:\record-and-replay\offline-resolver\bin\";"d:\record-and-replay\symber-common\bin\";"d:\record-and-replay\offline-resolver\lib\z3.exe";"d:\record-and-replay\offline-resolver\lib\com.microsoft.z3.jar";"d:\record-and-replay\offline-resolver\lib\json-simple-1.1.1.jar" edu.ist.symber.resolver.Resolver
    java -XMX1500cm -cp "d:\record-and-replay\symber-replayer\bin\","d:\record-and-replay\symber-common\bin\","d:\record-and-replay\symber-replayer\lib\json-simple-1.1.1.jar","d:\record-and-replay\symber-transformer\tmp\replay\" edu.ist.symber.Main --main-class section1.JGFSyncBench 2 --num-shared 7 --num-sync 2
  )
)
