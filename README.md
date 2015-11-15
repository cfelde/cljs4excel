# cljs4excel
Run a ClojureScript REPL within Microsoft Excel.

![](https://www.cljs4excel.com/img/preview-1.gif)

This is a bootstrapped ClojureScript as shown on http://clojurescript.net/
running within an Office Add-in, which is essentially a browser running within
Microsoft Excel. Using Office.js and some JS->CLJS->JS code you can then
interact with the Excel document from ClojureScript.

## How to install locally

The latest release can be found on https://www.cljs4excel.com/app/latest.
To add the XML manifest from a shared location in Excel, please see below link:

https://msdn.microsoft.com/en-us/library/office/fp123503.aspx

You'll find the manifest file under app/cljs4excel.xml. You can keep the
SourceLocation URL in it as-is, or change it to a local URL if you want to make
local changes.

Consider this as an early alpha/proof of concept release at the moment. There
are plenty of things I'd like to improve, so I'll put up a list of things
we can work on soon.

## Related links

Prepackaged cljs4excel: https://cljs4excel.com/ (No proper page yet).

Office Add-ins: https://msdn.microsoft.com/en-us/library/office/fp123503.aspx.

## License

Copyright © 2015 Christian Felde & Contributors

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
