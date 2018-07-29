# Fluent XML writer
[![Build Status](https://travis-ci.org/c0stra/fluent-xml-writer.svg?branch=master)](https://travis-ci.org/c0stra/fluent-xml-writer)

Fluent, hierarchical, XML writer is a tool that should make writing XML data
programmatically convenient and less error prone.

### 1. Streaming
This XML writer is streaming. It means, it's not creating any XML document representation in memory, but immediately writes the data to the XML, similarly as XmlStreamWriter. So it's good choice e.g. for XML logging facilities.

### 2. Fluent
Fluent stands for API, that allows chaining. So compared to e.g. `StreamWriter`, you can chain your pieces
to be written:

```java
document(new FileWriter("output.xml"))
    .version(1.0).encoding("UTF-8")
    .tag('root').attribute("id", "53gf543")
        .tag("child")
            .text("My text")
        .end()
    .end()
.close();
```

### 3. Hierarchical
Compared to `StreamWriter`, it is hierarchical. So at any point, the current
writer object is stick to certain level and state. This makes following pattern
available:

```java
RootElementWriter root = document(new FileWriter("output.xml"))
    .tag("root");

root.tag("first").text("first text");
root.tag("second").text("second text");
root.finish();
```
With `StreamWriter` similar situation would result in nesting. But with the hierarchical writer
`root` writer is stick to root element, and thanks to the state it knows, when to close it's
unclosed children.

Result will be:
```xml
<root>
    <first>first text</first>
    <second>second text</second>
</root>
```
This makes it again good for XML logging facilities. Because it will always make sure to close any unclosed elements for you.
