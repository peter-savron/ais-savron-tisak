# File type determination with Apache Tika Core

Implement the [file type determiner](../../features/DocumentProcessor.md#interfaces) with the Java dependency `org.apache.tika:tika-core`. Use its built-in MIME registry with a buffered, mark-capable stream and the supplied file’s basename as the resource name. Reuse the registry across calls and close the stream opened for each detection.

Tika combines file signatures, XML root recognition and filename hints. This provides broad format coverage using the beginning of the file, with no document extraction or parser bundle required. Container subtypes sharing a signature may resolve only to a generic container type. Return that detected type as supplied. The choice prioritizes coverage and bounded inspection. Actual latency depends on the workload. [Apache Tika content detection](https://tika.apache.org/3.2.3/detection.html)

Return the detected base MIME type. Propagate file-access errors separately from the unknown-type result. [Tika Detector contract](https://tika.apache.org/3.2.3/api/org/apache/tika/detect/Detector.html)
