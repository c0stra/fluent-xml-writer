# Fluent XML writer
[![Build Status](https://travis-ci.org/c0stra/fluent-xml-writer.svg?branch=master)](https://travis-ci.org/c0stra/fluent-xml-writer)

Fluent, hierarchical, XML writer is a tool that should make writing XML data
programmatically convenient and less error prone.

### 1. Fluent
Fluent stands for API, that allows chaining. So compared to e.g. `StreamWriter`, you can chain your pieces
to be written:

```java
document(new FileWriter("output.xml"))
    .version(1.0).encoding("UTF-8")
    .open('root').attribute("id", "53gf543")
        .open("child")
            .text("My text")
        .close()
    .close()
.finish();
```

Compared to `StreamWriter`, it is hierarchical. So at any point, the current
writer object is stick to certain level and state. This makes following pattern
available:

```java
ElementWriter<DocumentWriter> root = document(new FileWriter("output.xml"))
    .open("root");

root.open("first").text("first text");
root.open("second").text("second text");
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
