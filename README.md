# java compiler demo [![Github Workflow Build Status](https://img.shields.io/github/workflow/status/oldratlee/java-compiler-demo/CI/master?logo=github&logoColor=white)](https://github.com/oldratlee/java-compiler-demo/actions/workflows/ci.yaml) [![Java support](https://img.shields.io/badge/Java-8+-green?logo=OpenJDK&logoColor=white)](https://openjdk.java.net/)

Creating dynamic applications with java compiler(`javax.tools.JavaCompiler`).

- [`Plotter.java`](src/main/java/examples/plotter/Plotter.java)
  - GUI main application.
- [`CharSequenceCompiler.java`](src/main/java/javaxtools/compiler/CharSequenceCompiler.java)
  - java compiler encapsulation of `javax.tools.JavaCompiler`.


> demo code of article [Create dynamic applications with javax.tools](http://www.ibm.com/developerworks/java/library/j-jcomp/index.html).  
> \# simple chinese version: [使用 javax.tools 创建动态应用程序](http://www.ibm.com/developerworks/cn/java/j-jcomp/)

## how to run

```bash
./mvnw install exec:java -Dexec.mainClass=examples.plotter.Plotter
```

## screenshot

![image](https://user-images.githubusercontent.com/1063891/179347305-7513c8a4-40b6-4518-a4d2-7cd418b635a9.png)
