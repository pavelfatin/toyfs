ToyFS README
============

ToyFS is a simple [FAT](http://en.wikipedia.org/wiki/File_Allocation_Table "File Allocation Table - Wikipedia")-like file system implementation
on top of a general file system [SPI](https://github.com/pavelfatin/toyfs/tree/master/src/main/scala/com/pavelfatin/fs "File system SPI package"), plus a toy dual-pane file manager.

The main interest is `internal.toyfs` package which holds:

* an object-oriented [implementation](https://github.com/pavelfatin/toyfs/tree/master/src/main/scala/com/pavelfatin/fs/internal/toyfs "ToyFS implementation package") of the file system (with ScalaDocs);
* a complete set of ScalaTest BDD-style [specifications](https://github.com/pavelfatin/toyfs/tree/master/src/test/scala/com/pavelfatin/fs/internal/toyfs "ToyFS specificatins package").

Additionaly, you may run a [toy file manager](https://github.com/pavelfatin/toyfs/tree/master/src/main/scala/com/pavelfatin/fs/manager "Toy File Manager package") to see the file system in action ([screenshots](https://github.com/pavelfatin/toyfs/tree/master/images "Toy File Manager screenshots")).

Pavel Fatin, [http://pavelfatin.com](http://pavelfatin.com/)
