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

## User Guide

### 1. Maven dependency
In order to use the fluent XML writer, add following dependency to your maven pom.xml:
```xml
<dependency>
    <groupId>foundation.fluent.api</groupId>
    <artifactId>fluent-xml-writer</artifactId>
    <version>1.0</version>
</dependency>
```

### 2. Create XML writer instance

Create XML writer instances using factory methods:

```java
// Create writer using default configuration.
DocumentWriterFactory.document(new FileWriter("output.xml"));

// Create writer with cunstom configuration, e.g. pretty printed with indentation of 4 spaces.
DocumentWriterFactory.document(new FileWriter("output.xml"), DocumentWriterConfig.config().indentSpaces(4));
```

### 3. Write document content

Fluent API will guide you, how to write the content.

On the top (document) level you have available methods to define
- XML version
- encoding
- doctype
- processing instruction
- text (only if provided with whitespaces)

```java
document(writer).version(1.0).encoding("UTF-8");
```

You open element with method `tag(name)`, and specify attributes and namespaces on it.
```java
document(writer).version(1.0)
    .tag("root").attribute("name", "value")
    .end();
```

Within content you can specify any text, cdata, processing instruction or nested tag.

```java
document(writer).version(1.0)
    .tag("root")
        .text("Hello")
        .cdata("Unescaped &")
        .tag("child")
            text("Escaped &")
        .end()
    .end();
```

### 3. Configuration

You can configure now following things:
- How attribute values are quoted
- tag indentation
- additional attribute indentation (not yet supported)
```java
// Quote attributes using single apostrophe
config().singleQuoteValue();

// Quote attributes using double quotes (default)
config().doubleQuoteValue();

// Indent using 4 spaces
config().indentSpaces(4);

// Indent using tab
config().indentUsingTab();

// Indent using 2 tabs
config().indentUsingTabs(2);
```

Config uses a fluent API too, so you can chain your directives:
```java
config().singleQuoteValue().indentSpaces(2);
```

### 4. Run unit tests on your own

This project has delivered not only the library, but also a module with tests, which anybody can run
directly from his/her command line.

It's packaged as Maven plugin, which doesn't require project. So feel free to run the
test suite on your own using following command:
```
mvn foundation.fluent.api:fluent-xml-writer-tests:1.0:run
```

## Releases

#### Version 1.0 (August 9th 2018)
- Support for fluent document level attributes (xml version, encoding, doctype)
- Support for fluent element definition (tag, attributes, namespaces)
- Support for fluent element's content creation (text, cdata, processing instructions)
- Support for keeping hierarchy level (invoking method on an object closes an open child first)
- Support for escaping special XML characters (&, <, >)
- Support for handling of invalid XML 1.0 characters
- Support for pretty printing and custom indentation level